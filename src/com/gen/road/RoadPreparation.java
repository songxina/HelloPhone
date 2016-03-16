package com.gen.road;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.gen.trajectory.Group;

import de.fhpotsdam.unfolding.geo.Location;


/* 1.��ȡ����·����ϢallRoads
 * 2.��¼ÿ����վ��Ӧ�ľ�������Ľ���,nearestPointToCell-��cell_pointid
 * 3.����·����
 * 		ÿ������Ϊ�ڵ㣬�����·��Ϊ�ߣ�·�γ���ΪȨ�ء�roadGraph[][]
 * 		����֮���·��id��roadBetweenPoint[][]
 * 4.��������ÿ��Ĺ켣�����ࣩ������㵽��P*Pת�Ƹ��ʣ��̶�ת����·�γ��ֵĸ��ʡ�
*/
public class RoadPreparation {

	ArrayList<Road> allRoads = new ArrayList<Road>();//ÿһ��road���������
	int roadSize;
	double roadGraph[][];//·����roadGraph��¼��������
	String roadBetweenPoint[][];//roadBetweenPoint��¼������·����š�
	
	ArrayList<String> commonPoint = new ArrayList<String>();//��¼���н���
	HashMap<String, Integer> pointToIndex = new HashMap<String, Integer>();// ��¼��������
	int pointSize;
	
	ArrayList<String> allStation = new ArrayList<String>();//��¼���л�վλ��
	HashMap<String, String> cellToCoor = new HashMap<String, String>();// ��stationID���ճ�������
	HashMap<String,String> nearestPointToCell = new HashMap<String, String>();//��¼ÿ����վ��Ӧ�ľ�������Ľ���,cell_pointid
	
	DealWithFile deal = new DealWithFile();
	ArrayList<String> allShortPath = new ArrayList<String>();//��¼�������·��
	
	public RoadPreparation(){
		commonPoint = deal.getFile("E:/aSmartCity/Map/allCommon.txt");
		pointSize = commonPoint.size();
		fetchAllRoads();
		generateRoadGraph();
		nearestPointToCell = fetchNearestPointToCell();
		allShortPath = deal.getFile("E:/aSmartCity/Map/allShortPath.txt");
	}
	
	public double[][] getRoadGraph() {
		return roadGraph;
	}

	public String[][] getRoadBetweenPoint() {
		return roadBetweenPoint;
	}
	
	public HashMap<String, String> getNearestPointToCell() {
		return nearestPointToCell;
	}
	
	public ArrayList<Road> getAllRoads() {
		return allRoads;
	}
	
	public ArrayList<String> getCommonPoint() {
		return commonPoint;
	}

	//����Ŀ���(��ֹ��)������·����
	public ArrayList<Integer> getThePath(int begin,int end){
		String temp[] = allShortPath.get(begin).split("_");
		int[] prev = new int[pointSize];
		for(int i=0;i<pointSize;i++)
			prev[i] = Integer.parseInt(temp[i]);
		ArrayList<Integer> result = new ArrayList<Integer>();
		Stack stack = new Stack<Integer>();
		stack.push(end);
		while(true){
			if(end==-1){
				return null;
			}
			end = prev[end];
			stack.push(end);
			if(end==begin)
				break;
		}
		while(!stack.isEmpty())
			result.add((Integer) stack.pop());
		return result;
	}
	
	//����·��
	public void generateRoadGraph(){
		roadGraph = new double[pointSize][pointSize];
		roadBetweenPoint = new String[pointSize][pointSize];
		for(int i=0;i<pointSize;i++)
			for(int j=0;j<pointSize;j++){
				roadGraph[i][j] = Double.MAX_VALUE;
				roadBetweenPoint[i][j] = "";
			}
		
		for(Road road:allRoads){
			String pointa = road.startPoint;
			String pointb = road.endPoint;
			
			if(commonPoint.contains(pointa) && commonPoint.contains(pointb)){
				int pointaIndex = pointToIndex.get(pointa);
				int pointbIndex = pointToIndex.get(pointb);
				roadGraph[pointaIndex][pointbIndex] = road.length;
				roadGraph[pointbIndex][pointaIndex] = road.length;
//				System.out.println(road.length);
				roadBetweenPoint[pointaIndex][pointbIndex] = road.id;
				roadBetweenPoint[pointbIndex][pointaIndex] = road.id;
			}
		}
	}
	
	//��ȡ���е�·��Ϣ
	public void fetchAllRoads(){
		for(int i=0;i<pointSize;i++)
			pointToIndex.put(commonPoint.get(i), i);
		
		ArrayList<String> list = deal.getFile("E:/aSmartCity/Map/roadAfterMerged.txt");
		for(int i=0;i<list.size();i++){
			String[] temp = list.get(i).split(",");
			ArrayList<String> point = new ArrayList<String>();
			for(int j=1;j<temp.length;j++)
				point.add(temp[j]);
			Road road = new Road(i+"", point);
			road.calLength();
			allRoads.add(road);
		}
		roadSize = allRoads.size();
	}
	//���ļ��л�ȡ����ÿ����վ����Ľ���
	public HashMap<String,String> fetchNearestPointToCell(){
		ArrayList<String> list = deal.getFile("E:/aSmartCity/Map/nearestPointToCell.txt");
		for(String s:list){
			String[] temp = s.split("_");
			nearestPointToCell.put(temp[0]+"_"+temp[1], temp[2]);
		}
		
		return nearestPointToCell;
	}
	
	//�������ÿ����վ����Ľ��㣬���뵽�ļ���
	public void searchNearestPointToCell(){
		getTransMap();
		int size = allStation.size();
		int n=0;
		for(String cell:allStation){
			
			if(n%100==0)
				System.out.println(n+"/"+size);
			n++;
			String[] coor = cellToCoor.get(cell).split("_");
			double lat = Double.parseDouble(coor[0]);
			double lng = Double.parseDouble(coor[1]);
			int nearestPointIndex = 0;
			double minDistance = Double.MAX_VALUE;
			for(int i=0;i<pointSize;i++){
				String[] coori = commonPoint.get(i).split("_");
				double lati = Double.parseDouble(coori[1]);
				double lngi = Double.parseDouble(coori[0]);
				double distance = distance(lat,lng,lati,lngi);
				if(distance < minDistance){
					minDistance = distance;
					nearestPointIndex = i;
				}
			}
			nearestPointToCell.put(cell, nearestPointIndex+"");
		}
		ArrayList<String> result = new ArrayList<String>();
		for(String key:nearestPointToCell.keySet()){
			result.add(key+"_"+nearestPointToCell.get(key));
		}
		deal.exportResString(result, "E:/aSmartCity/Map/nearestPointToCell.txt");
	}
	
// ��ȡ�ļ�����ȡmap���Ա㽫stationID���ճ�������
	public void getTransMap() {
		File file = new File("./data/data/base_station_GPS.txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			try {
				String line = br.readLine();
				String[] rs = null;
				while ((line = br.readLine()) != null) {
					rs = line.split("\\|");
					cellToCoor.put(rs[0] + "_" + rs[1], rs[4] + "_" + rs[5]);
					allStation.add(rs[0] + "_" + rs[1]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
// �������������
	public double distance(double lat1, double longt1, double lat2,	double longt2) {
		double PI = 3.14159265358979323; // Բ����
		double R = 6371229; // ����İ뾶
		double x, y, distance;
		x = (longt2 - longt1) * PI * R
				* Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
		y = (lat2 - lat1) * PI * R / 180;
		distance = Math.hypot(x, y);//sqrt(x2 +y2) 
		return distance;
	}
	
	//��������ÿ��Ĺ켣�����ࣩ������㵽��P*Pת�Ƹ��ʣ��̶�ת����·�γ��ֵĴ�����
	public int[] roadFrequency(ArrayList<Group> cellGroup,ArrayList<ArrayList<String>> groupIDFromHomeToWork){
		int[] roadFrequency = new int[roadSize];
		int[][] pointTransfer = new int[pointSize][pointSize];
		
		for(ArrayList<String> list:groupIDFromHomeToWork){
			for(int i=0;i<list.size()-1;i++){
				Group pre = cellGroup.get(Integer.parseInt(list.get(i)));
				Group next = cellGroup.get(Integer.parseInt(list.get(i+1)));
				//ȡ������佻���
				ArrayList<String> prePoints = cellListToPoints(pre.getCellGroup());
				ArrayList<String> nextPoints = cellListToPoints(next.getCellGroup());
//				System.out.println(pre.getGourpID()+"_"+prePoints);
//				System.out.println(next.getGourpID()+"_"+nextPoints);
//				System.out.println("-------------------------------");
				double normalizing = 1/(prePoints.size()+nextPoints.size());
				ArrayList<Integer> leaveTime = next.getAllTime();
				for(String prep:prePoints)
					for(String nextp:nextPoints){
//						System.out.println(prep+"--"+nextp);
						if(prep.equals(nextp))
							continue;
//						Dijkstra d = new Dijkstra(roadGraph,pointSize,Integer.parseInt(prep),Integer.parseInt(nextp));
//						ArrayList<Integer> path = d.getTheTrajectory();
						ArrayList<Integer> path = getThePath(Integer.parseInt(prep), Integer.parseInt(nextp));
						if(path==null)
							continue;
						for(int k=0;k<path.size()-1;k++){
							int a = path.get(k);
							int b = path.get(k+1);
							pointTransfer[a][b]+=normalizing;
							int roadIndex = Integer.parseInt(roadBetweenPoint[a][b]);
							roadFrequency[roadIndex]++;
							allRoads.get(roadIndex).addTime(leaveTime);
//							System.out.println(allRoads.get(roadIndex).getMedianTime());
						}
					}
			}
		}
		return roadFrequency;
	}
	//����վ���б�Ϊ��������
	public ArrayList<String> cellListToPoints(ArrayList<String> cells){
		ArrayList<String> result = new ArrayList<String>();
		for(String c:cells){
			String p = nearestPointToCell.get(c);
			if(!result.contains(p))
				result.add(p);
		}
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		RoadPreparation r = new RoadPreparation();
//		r.searchNearestPointToCell();
	}

}
