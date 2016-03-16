package com.gen.locAndTrajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gen.trajectory.Group;
/*

* 重要性评分最高的作为聚类中心
*
*/
public class LeaderCluster {

	ArrayList<Group> cellGroup = new ArrayList<Group>();//记录该设备出现过的所有点的聚类
	double distanceBetweenCells[][];//记录不同基站间的距离
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();
	ArrayList<String> differentCell = new ArrayList<String>();//出现过的所有基站
	public Map<String,String> cellToCoordinate = new HashMap<String,String>();//记录所有基站与坐标的对应关系，对应differentcell中
	public double importantScore[];//记录每个基站的重要性评分
	
	double radius = 450;
	int cellSize;//出现过的不同基站个数
	
	public LeaderCluster(double importantScore[],HashMap<String, Integer> cellIndexMap
			,ArrayList<String> differentCell,Map<String,String> cellToCoordinate){
		
		this.importantScore = importantScore;
		this.cellIndexMap = cellIndexMap;
		this.differentCell = differentCell;
		this.cellToCoordinate = cellToCoordinate;
		cellSize = differentCell.size();
	}
	
	//计算基站之间的距离
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
	//计算两坐标距离
	public double distance(double lat1, double longt1, double lat2,double longt2){
		double PI = 3.14159265358979323; // 圆周率
		double R = 6371229; // 地球的半径
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

			//寻找出现概率最大的点
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
			//聚类
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
	//计算group中所有点到目标点的平均距离
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
