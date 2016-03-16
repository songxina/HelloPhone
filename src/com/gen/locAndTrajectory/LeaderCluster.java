package com.gen.locAndTrajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gen.trajectory.Group;
/*

* ��Ҫ��������ߵ���Ϊ��������
*
*/
public class LeaderCluster {

	ArrayList<Group> cellGroup = new ArrayList<Group>();//��¼���豸���ֹ������е�ľ���
	double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();
	ArrayList<String> differentCell = new ArrayList<String>();//���ֹ������л�վ
	public Map<String,String> cellToCoordinate = new HashMap<String,String>();//��¼���л�վ������Ķ�Ӧ��ϵ����Ӧdifferentcell��
	public double importantScore[];//��¼ÿ����վ����Ҫ������
	
	double radius = 450;
	int cellSize;//���ֹ��Ĳ�ͬ��վ����
	
	public LeaderCluster(double importantScore[],HashMap<String, Integer> cellIndexMap
			,ArrayList<String> differentCell,Map<String,String> cellToCoordinate){
		
		this.importantScore = importantScore;
		this.cellIndexMap = cellIndexMap;
		this.differentCell = differentCell;
		this.cellToCoordinate = cellToCoordinate;
		cellSize = differentCell.size();
	}
	
	//�����վ֮��ľ���
	public void fillDistance(){
		distanceBetweenCells = new double[cellSize][cellSize];
		
		for(String s:differentCell){
			int i = cellIndexMap.get(s);
			String coor = cellToCoordinate.get(s);
			String temps[] = coor.split("_");
			
			for(String l:differentCell){				
				String coors = cellToCoordinate.get(l);
				String templ[] = coors.split("_");
				double d = distance(Double.parseDouble(temps[0]),Double.parseDouble(temps[1])
						,Double.parseDouble(templ[0]),Double.parseDouble(templ[1]));
				int j = cellIndexMap.get(l);
				distanceBetweenCells[i][j] = d;				
			}
		}
	}
	//�������������
	public double distance(double lat1, double longt1, double lat2,double longt2){
		double PI = 3.14159265358979323; // Բ����
		double R = 6371229; // ����İ뾶
		double x, y, distance;
		x = (longt2 - longt1) * PI * R * Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
		y = (lat2 - lat1) * PI * R / 180;
		distance = Math.hypot(x, y);
		return distance;
	}
	
	public ArrayList<Group> doCluster(){	
		fillDistance();
		ArrayList<String> cells = new ArrayList<String>();
		cells.addAll(differentCell);
	
		int groupID = 0;
		while(cells.size()!=0){

			//Ѱ�ҳ��ָ������ĵ�
			int maxIndex = 0;
			double maxPossible = 0;
			for(String s:cells){	
				int index = cellIndexMap.get(s);
				double pos = importantScore[index];
				if( pos > maxPossible){
					maxPossible = pos;
					maxIndex = index;
				}
			}
			String cell = differentCell.get(maxIndex);
			cells.remove(cell);
			int preindex = maxIndex;
			//����
			Group group = new Group();
			group.setGourpID(groupID+"");
			groupID++;
			group.add(cell);
//				System.out.println("possibility:"+maxPossible);
			ArrayList<String> usedCellStrings = new ArrayList<String>();
			for(String scell:cells){
				double value =calAverageDistance(group, scell);
				if(value!=0 && value<=radius){		
					group.add(scell);
					usedCellStrings.add(scell);
				}
			}
			cells.removeAll(usedCellStrings);
			cellGroup.add(group);
//				System.out.println("cells"+cells.size());
//				System.out.println("cellGroup[hour]"+cellGroup[hour].size());
//				System.out.println("_______________________");
		}
		
		return cellGroup;
	}
	//����group�����е㵽Ŀ����ƽ������
	public double calAverageDistance(Group group,String targetCell){
		int targetIndex = cellIndexMap.get(targetCell);
		double distance = 0;
		ArrayList<String> cells = group.getCellGroup();
		for(String c:cells){
			int index = cellIndexMap.get(c);
			distance += distanceBetweenCells[index][targetIndex];
		}
		distance/=cells.size();
		distance = distanceBetweenCells[cellIndexMap.get(cells.get(0))][targetIndex];
		return distance;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
