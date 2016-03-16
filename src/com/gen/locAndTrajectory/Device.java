package com.gen.locAndTrajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.gen.road.DealWithFile;
import com.gen.road.Road;
import com.gen.road.RoadPreparation;
import com.gen.trajectory.Group;
import com.gen.trajectory.QueryTrajectory;

import de.fhpotsdam.unfolding.geo.Location;

//��¼���пɶ�ȡ��Ϣ��������MatrixForPersonAndCell��
//�Լ�Ҫ��ȡ����Ϣ��ͨ��ImportLocationDetect������Homedetection��ImportantLocDetection

public class Device {

	public  String deviceID;
	public  int daySize;//��ȡ���ݵ�����
	int cellNum ;//��ͬ��վ�ĸ���
	QueryTrajectory queryTrajectory;
	ArrayList<String> cellAll[][];//Ŀǰ���������20131205-20131231 ��27��
	ArrayList<Integer> cellTimeAll[][];//ÿ����վ��Ӧ��ʱ��
	ArrayList<String> differentCell = new ArrayList<String>();//���в�ͬ�Ļ�վ
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//��¼���л�վ������Ķ�Ӧ��ϵ
	String homeGroupID = "";
	String workGroupID = "";
	
	public ArrayList<Group> cellGroup = new ArrayList<Group>();//��¼���豸���ֹ������е�ľ���
	int[][] groupTransferMatrix;//��¼���ൽ����ת�ƾ���
	public ArrayList<ArrayList<String>> groupIDFromHomeToWork ;//��¼ÿ��home��work֮��ľ�������
	
	public double importantScore[];//��¼ÿ����վ����Ҫ������
	public int cellnum[];//��¼ÿ����վ��¼�Ĵ���
	HashMap<String,Integer> cellNumMap = new HashMap<String, Integer>();//��վ��¼�ܴ�����map��ʽ
	
	
	public Device(String deviceID) throws ParseException{
		this.deviceID = deviceID;
		queryTrajectory = new QueryTrajectory(deviceID);
		daySize = queryTrajectory.dayNum;
		cellAll = new ArrayList[daySize][25];
		
		queryTrajectory.getAllTrajectory();
		cellAll = queryTrajectory.getCellAll();
		cellTimeAll = queryTrajectory.getCellTimeAll();
		HashSet<String> differentCelltemp = queryTrajectory.getDifferentCell();
		differentCell.addAll(differentCelltemp);
		cellToCoordinate = QueryTrajectory.getMap();
		cellNum = differentCell.size();
		ImportantLocationDetect importantLocationDetect = new ImportantLocationDetect(queryTrajectory);
		cellGroup = importantLocationDetect.generateAllTheGroups();
		homeGroupID = importantLocationDetect.HomeDetect();
		workGroupID = importantLocationDetect.WorkDetect();
		importantScore = importantLocationDetect.getImportantScore();
		cellnum = importantLocationDetect.getcellnumber();
	}
	public ArrayList<Group> getCellGroup(){
		return cellGroup;
	}
	
	public ArrayList<String>[][] getCellAll() {
		return cellAll;
	}
	
	public ArrayList<Integer>[][] getCellTimeAll() {
		return cellTimeAll;
	}
	
	public String getWorkGroupID() {
		return workGroupID;
	}
	public String getHomeGroupID() {
		return homeGroupID;
	}
	public Group getWorkGroup(){
		return cellGroup.get(Integer.parseInt(workGroupID));
	}
	public Group getHomeGroup(){
		return cellGroup.get(Integer.parseInt(homeGroupID));
	}
	//�õ���ס�����Ļ�վid(lac_cell)
	public String getHomeGroupCentralCell(){
		return cellGroup.get(Integer.parseInt(homeGroupID)).getCenterCell();
	}
	//�õ������ص����Ļ�վid(lac_cell)
		public String getWorkGroupCentralCell(){
			return cellGroup.get(Integer.parseInt(workGroupID)).getCenterCell();
		}
	
	//��stringת����location
	public Location stringToLoc(String cell){
		String lc =null;String temp[];
		lc= cellToCoordinate.get(cell);
		temp = lc.split("_");
		Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
		return a;
	}
	//��û�վ��Ҫ�����ֵ�map��ʽ
	public HashMap<String,Integer> fillCellScoreMap(){
		for(int i=0;i<differentCell.size();i++){
			String cell = differentCell.get(i);
			int score = cellnum[i];
			cellNumMap.put(cell, score);
		}
		return cellNumMap;
	}
	//�����ͬ��վ�������
	public String printSpace(){
//		for(int i=0;i<cellNum;i++)
//			System.out.println(differentCell.get(i)+": "+cellnum[i]);
		String infoLine = "";
		infoLine+=deviceID+","+getHomeGroupCentralCell()+","+getWorkGroupCentralCell()+",";
		for(int i=0;i<cellNum;i++){
			infoLine+=differentCell.get(i)+"-"+cellnum[i];
			if(i!=cellNum-1)
				infoLine+=",";
		}
//		System.out.println(infoLine);
		return infoLine;
	}
	
	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		ArrayList<String> devices = DealWithFile.getFile("E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/unautochthons.txt");
		String id = devices.get(3693);
//		Device device = new Device("99249764168730152");
		System.out.println(id);
		Device device = new Device(id);
		System.out.println("All The Groups:");
		for(Group g:device.cellGroup)
			g.print();
		System.out.println("HOME:"+device.homeGroupID+"_"+device.getHomeGroupCentralCell()); 
		System.out.println("Work:"+device.workGroupID+"_"+device.getWorkGroupCentralCell());
		device.printSpace();
		//		DataPreparation dataPreparation = new DataPreparation(device);
//		//�õ�ÿ��home��work֮��ľ�������
//		device.groupIDFromHomeToWork = dataPreparation.extractTrajectoryFromHomeToWork();
//		for(ArrayList<String> list:device.groupIDFromHomeToWork)
//			System.out.println(list);
		//�õ������ת�ƴ�������
//		device.groupTransferMatrix = dataPreparation.calGroupTransferMatrix();
		/*int groupSize = device.cellGroup.size();
		for(int i=0;i<groupSize;i++){
			for(int j=0;j<groupSize;j++)
				System.out.print(device.groupTransferMatrix[i][j]+" ");
			System.out.println();
		}*/
		//
//		RoadPreparation road = new RoadPreparation();
		
	}
}
