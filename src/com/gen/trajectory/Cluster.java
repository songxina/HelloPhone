package com.gen.trajectory;

import java.util.ArrayList;
import java.util.HashMap;

public class Cluster {

	ArrayList<Group> cellGroup[] = new ArrayList[24];
	double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();
	double radius = 450;
	double locations[][];//λ�þ��󣬼�¼���ָ���
	ArrayList<String> differentCell = new ArrayList<String>();//���ֹ������л�վ
	
	public Cluster(HashMap<String, Integer> cellIndexMap
			,double distanceBetweenCells[][],double locations[][],ArrayList<String> differentCell){
		this.distanceBetweenCells = distanceBetweenCells;
		this.cellIndexMap = cellIndexMap;
		this.locations = locations;
		this.differentCell = differentCell;
		for(int i=0;i<24;i++)
			cellGroup[i] = new ArrayList<Group>();
	}
	
	public ArrayList<Group>[] doCluster(){
		
		ArrayList<String> cells = new ArrayList<String>();
		int size = cellIndexMap.size();
		
		for(int hour=0;hour<24;hour++){
			cells.clear();
			//��Сʱ���ֹ������л�վ
			for(int i=0;i<size;i++)
				if(locations[hour][i]!=0)
					cells.add(differentCell.get(i));
			
			int idCount = 0;
			while(cells.size()!=0){

				//Ѱ�ҳ��ָ������ĵ�
				int maxIndex = 0;
				double maxPossible = 0;
				for(String s:cells){	
					int index = cellIndexMap.get(s);
					double pos = locations[hour][index];
					if( pos > maxPossible){
						maxPossible = pos;
						maxIndex = index;
					}
				}
				String cell = differentCell.get(maxIndex);
				cells.remove(cell);
				int preindex = cellIndexMap.get(cell);
				//����
				Group group = new Group();
				group.setGourpID(hour+"_"+idCount);
				idCount++;
				group.add(cell);
//				System.out.println("possibility:"+maxPossible);
				ArrayList<String> usedCellStrings = new ArrayList<String>();
				for(int index=0;index<size;index++){
//					double value = distanceBetweenCells[preindex][index];
					double value =calAverageDistance(group, index);
					String s = differentCell.get(index);
					if(value!=0 && value<=radius && cells.contains(s)){		
						group.add(s);
						usedCellStrings.add(s);
					}
				}
				cells.removeAll(usedCellStrings);
				cellGroup[hour].add(group);
//				System.out.println("cells"+cells.size());
//				System.out.println("cellGroup[hour]"+cellGroup[hour].size());
//				System.out.println("_______________________");
			}
			
		}
		
		return cellGroup;
	}
	//����group�����е㵽Ŀ����ƽ������
	public double calAverageDistance(Group group,int targetIndex){
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
	
	public ArrayList<Group>[] getCellGroup() {
		return cellGroup;
	}
	
}
