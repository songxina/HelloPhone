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

//记录所有可读取信息，包括在MatrixForPersonAndCell中
//以及要提取的信息，通过ImportLocationDetect，包括Homedetection和ImportantLocDetection

public class Device {

	public  String deviceID;
	public  int daySize;//获取数据的天数
	int cellNum ;//不同基站的个数
	QueryTrajectory queryTrajectory;
	ArrayList<String> cellAll[][];//目前存的数据是20131205-20131231 共27天
	ArrayList<Integer> cellTimeAll[][];//每个基站对应的时间
	ArrayList<String> differentCell = new ArrayList<String>();//所有不同的基站
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//记录所有基站与坐标的对应关系
	String homeGroupID = "";
	String workGroupID = "";
	
	public ArrayList<Group> cellGroup = new ArrayList<Group>();//记录该设备出现过的所有点的聚类
	int[][] groupTransferMatrix;//记录聚类到聚类转移矩阵
	public ArrayList<ArrayList<String>> groupIDFromHomeToWork ;//记录每日home与work之间的聚类序列
	
	public double importantScore[];//记录每个基站的重要性评分
	public int cellnum[];//记录每个基站记录的次数
	HashMap<String,Integer> cellNumMap = new HashMap<String, Integer>();//基站记录总次数的map形式
	
	
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
	//得到居住地中心基站id(lac_cell)
	public String getHomeGroupCentralCell(){
		return cellGroup.get(Integer.parseInt(homeGroupID)).getCenterCell();
	}
	//得到工作地地中心基站id(lac_cell)
		public String getWorkGroupCentralCell(){
			return cellGroup.get(Integer.parseInt(workGroupID)).getCenterCell();
		}
	
	//将string转换成location
	public Location stringToLoc(String cell){
		String lc =null;String temp[];
		lc= cellToCoordinate.get(cell);
		temp = lc.split("_");
		Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
		return a;
	}
	//获得基站重要性评分的map形式
	public HashMap<String,Integer> fillCellScoreMap(){
		for(int i=0;i<differentCell.size();i++){
			String cell = differentCell.get(i);
			int score = cellnum[i];
			cellNumMap.put(cell, score);
		}
		return cellNumMap;
	}
	//输出不同基站及其个数
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
//		//得到每日home与work之间的聚类序列
//		device.groupIDFromHomeToWork = dataPreparation.extractTrajectoryFromHomeToWork();
//		for(ArrayList<String> list:device.groupIDFromHomeToWork)
//			System.out.println(list);
		//得到聚类间转移次数矩阵
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
