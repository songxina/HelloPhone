package com.gen.trajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.fhpotsdam.unfolding.geo.Location;

public class MatrixForGroup {

	ArrayList<String> differentCell = new ArrayList<String>();//出现过的所有基站
	public HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//基站ID对矩阵序号
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//记录所有基站与坐标的对应关系，对应differentcell中
	double locations[][];//位置矩阵，记录出现概率
	double transferAllDay[][];//全天转移矩阵。记录转移概率
	double transferByHour[][][];//每小时转移矩阵。记录转移概率
	int size = 0;//不同基站的个数
	public double distanceBetweenCells[][];//记录不同基站间的距离
	MatrixForPersonAndCell matrix;
	String deviceID;
	
	//聚类
	int daySize = 24;
	ArrayList<String> cellAll[][] = new ArrayList[daySize][25];	//目前存的数据是20131205-20131228 共25天
	Cluster cluster;
	HashMap<String, String> cellToGroup = new HashMap<String, String>();//记录基站对应聚类
	ArrayList<Group> cellGroup[] = new ArrayList[24];//记录每小时聚类
	double groupTransferByHour[][][];//以聚类为单位，每小时转移矩阵。记录转移概率（聚类中所有点记录之和）
	//每个聚类出现的概率，记录在group类中的possibility中
	
	public MatrixForGroup(String device) throws ParseException{
		deviceID = device;//"99249764168730152";
		matrix = new MatrixForPersonAndCell(deviceID);
		differentCell = matrix.getDifferentCell();
		cellToCoordinate = matrix.getCellToCoordinate();
		locations = matrix.getLocations();
		transferAllDay = matrix.getTransferAllDay();
		transferByHour = matrix.getTransferByHour();
		cellIndexMap = matrix.getCellIndexMap();
		distanceBetweenCells = matrix.getDistanceBetweenCells();
		cellAll = matrix.getCellAll();
		cluster = new Cluster(cellIndexMap, distanceBetweenCells, locations, differentCell);
		size = differentCell.size();
		groupTransferByHour = new double[24][size][size];
		for(int hour=0;hour<24;hour++)
			for(int i=0;i<size;i++)
				for(int j=0;j<size;j++)
					groupTransferByHour[hour][i][j]=0;
	}
	
	public ArrayList<Group>[] calAllGroup(){
		calClusterPossibility();
		calClusterTransfer();
		calCellLocationGroup();
		return cellGroup;
	}
	//将cluster的cellGroup生成cellLocationGroup
	public void calCellLocationGroup(){
		
		for(int hour=0;hour<24;hour++){
			for(Group g:cellGroup[hour]){
				ArrayList<Location> cellLocationGroup = new ArrayList<Location>();
				ArrayList<String> cellg = g.getCellGroup();
				cellLocationGroup = transLocations(cellg);
				g.setCellLocationGroup(cellLocationGroup);				
			}
			
		}
		
	}
	//将string转换成location(list)
	public ArrayList<Location> transLocations(ArrayList<String> list){
		ArrayList<Location> loc = new ArrayList<Location>();
		String temp[];
		for(String s :list){
			if(s.equals("0_0"))
				continue;
			String lc =null;
			lc= cellToCoordinate.get(s);
			if(lc!=null){
				temp = lc.split("_");
				Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
				loc.add(a);
			}				
		}
		return loc;
	}
	//将string转换成location
	public Location stringToLoc(String cell){
		String lc =null;String temp[];
		lc= cellToCoordinate.get(cell);
		temp = lc.split("_");
		Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
		return a;
	}
	
	//计算每个cluster的possibility
	public void calClusterPossibility(){
		cellGroup = cluster.doCluster();
		
		for(int hour=0;hour<24;hour++){
			for(Group g:cellGroup[hour]){
				ArrayList<String> cellsInGroup = g.getCellGroup();
				double sumPossibility = 0;
				for(String sg:cellsInGroup){
					int index = cellIndexMap.get(sg);
					double ptemp = locations[hour][index];
					sumPossibility+=ptemp;
				}
				g.setPossibility(sumPossibility);
			}
		}
	}
	//给定cell返回groupID
	public String getGroupIDByCell(String cell,int hour) {
		String gid = "";
		for(Group g:cellGroup[hour])
			if(g.hasCell(cell))
				gid = g.gourpID;
		return gid;
	}
	
	//计算每个cluster的转移概率，以小时为单位
	public void calClusterTransfer(){
		//计算cellToGroup
		for(int hour=0;hour<24;hour++)
			for(int i=0;i<size;i++){
				if(locations[hour][i]!=0){
					String cid = differentCell.get(i);
					String gid = getGroupIDByCell(cid, hour);
//					System.out.println(cid+"  "+gid.split("_")[1]);
					cellToGroup.put(cid+"_"+hour, gid.split("_")[1]);//cell_hour idIndex
				}					
			}
		
		for(int hour=0;hour<24;hour++){
			for(int i=0;i<size;i++){
				String ci = differentCell.get(i);
				for(int j=0;j<size;j++){
					double t = transferByHour[hour][i][j];
					if(t!=0){
						String cj = differentCell.get(j);
						int preGIndex = 0;
						int GIndex = 0;
//						System.out.println("---"+ci);
						preGIndex = Integer.parseInt(cellToGroup.get(ci+"_"+hour));
						if(cellToGroup.containsKey(cj+"_"+hour)){
							GIndex = Integer.parseInt(cellToGroup.get(cj+"_"+hour));
							groupTransferByHour[hour][preGIndex][GIndex]+=t;
						}
					}
				}
			}
		}
		
	}
	
	//给定一个groupID，返回最大概率转移的group中心基站的坐标
	public Location getNextGroupCenter(Group group){
		Location center = null;
		String preID = group.getGourpID();
		String temp[] = preID.split("_");
		int hour = Integer.parseInt(temp[0]);
		int index = Integer.parseInt(temp[1]);
		
		int maxi = 0;
		double maxp =0;
		for(int i=0;i<size;i++){
			double value = groupTransferByHour[hour][index][i];
			if(value>maxp && i!=index){
				maxi = i;
				maxp = value;
			}
		}
		if(maxp!=0){
			Group gtarget = cellGroup[hour].get(maxi);
			center = gtarget.getCenterLocation();
		}
		else {
			center = group.getCenterLocation();
		}
		return center;
	}
	
	//给定grouplist,返回每小时最常终止的groupID（数组）
	public String[] getEndGroup(){
		String group[] = new String[24];
		//计算每个group的endValue
		for(int day=0;day<daySize;day++){
			for(int hour=0;hour<24;hour++){
				//取前三个，分别对group加startValue值，第一个cell加3，第二个加2，第三个加1.
				int number = 3;
				int nsize = cellAll[day][hour].size();
				if(number>nsize)
					number = nsize;
				if(nsize>5)						//数量大于5的路径算作有效路径
				for(int i=nsize-number;i<nsize;i++){
					String cell = cellAll[day][hour].get(i);
					String gid = getGroupIDByCell(cell, hour);
					int gindex = Integer.parseInt(gid.split("_")[1]);
					Group groupTemp = new Group();
					groupTemp = cellGroup[hour].get(gindex);
					groupTemp.endValue += 4-(nsize-i);
				}
			}
		}
		//选取最大endValue的group
		for(int hour=0;hour<24;hour++){
			int maxIndex = 0;
			double maxValue = 0;
			for(int i=0;i<cellGroup[hour].size();i++){
				Group g = cellGroup[hour].get(i);
				double v = g.endValue;
				if(v > maxValue){
					maxValue = v;
					maxIndex = i;
				}
			}
			group[hour] = hour+"_"+maxIndex;
		}
		return group;
	}
	
	//给定grouplist,返回每小时最常起始的groupID（数组）
		public String[] getStartGroup(){
			String group[] = new String[24];
			//计算每个group的startValue
			for(int day=0;day<daySize;day++){
				for(int hour=0;hour<24;hour++){
					//取后三个，分别对group加startValue值，第一个cell加3，第二个加2，第三个加1.
					int number = 3;
					int nsize = cellAll[day][hour].size();
					if(number>nsize)
						number = nsize;
					if(nsize>5)           //数量大于5的路径算作有效路径
					for(int i=0;i<number;i++){
						String cell = cellAll[day][hour].get(i);
						String gid = getGroupIDByCell(cell, hour);
						int gindex = Integer.parseInt(gid.split("_")[1]);
						Group groupTemp = new Group();
						groupTemp = cellGroup[hour].get(gindex);
						groupTemp.startValue +=3-i;
					}
				}
			}
			//选取最大startValue的group
			for(int hour=0;hour<24;hour++){
				int maxIndex = 0;
				double maxValue = 0;
				for(int i=0;i<cellGroup[hour].size();i++){
					Group g = cellGroup[hour].get(i);
					double v = g.startValue;
					if(v > maxValue){
						maxValue = v;
						maxIndex = i;
					}
				}
				group[hour] = hour+"_"+maxIndex;
			}
			return group;
		}
	
	public ArrayList<String> getDifferentCell() {
		return differentCell;
	}

	public void setDifferentCell(ArrayList<String> differentCell) {
		this.differentCell = differentCell;
	}

	public HashMap<String, Integer> getCellIndexMap() {
		return cellIndexMap;
	}

	public void setCellIndexMap(HashMap<String, Integer> cellIndexMap) {
		this.cellIndexMap = cellIndexMap;
	}

	public Map<String, String> getCellToCoordinate() {
		return cellToCoordinate;
	}

	public void setCellToCoordinate(Map<String, String> cellToCoordinate) {
		this.cellToCoordinate = cellToCoordinate;
	}

	public double[][] getLocations() {
		return locations;
	}

	public void setLocations(double[][] locations) {
		this.locations = locations;
	}

	public double[][] getTransferAllDay() {
		return transferAllDay;
	}

	public void setTransferAllDay(double[][] transferAllDay) {
		this.transferAllDay = transferAllDay;
	}

	public double[][][] getTransferByHour() {
		return transferByHour;
	}

	public void setTransferByHour(double[][][] transferByHour) {
		this.transferByHour = transferByHour;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public double[][] getDistanceBetweenCells() {
		return distanceBetweenCells;
	}

	public void setDistanceBetweenCells(double[][] distanceBetweenCells) {
		this.distanceBetweenCells = distanceBetweenCells;
	}

	public MatrixForPersonAndCell getMatrix() {
		return matrix;
	}

	public void setMatrix(MatrixForPersonAndCell matrix) {
		this.matrix = matrix;
	}

	public int getDaySize() {
		return daySize;
	}

	public void setDaySize(int daySize) {
		this.daySize = daySize;
	}

	public ArrayList<String>[][] getCellAll() {
		return cellAll;
	}

	public void setCellAll(ArrayList<String>[][] cellAll) {
		this.cellAll = cellAll;
	}

	public HashMap<String, String> getCellToGroup() {
		return cellToGroup;
	}

	public void setCellToGroup(HashMap<String, String> cellToGroup) {
		this.cellToGroup = cellToGroup;
	}

	public ArrayList<Group>[] getCellGroup() {
		return cellGroup;
	}

	public void setCellGroup(ArrayList<Group>[] cellGroup) {
		this.cellGroup = cellGroup;
	}

	public double[][][] getGroupTransferByHour() {
		return groupTransferByHour;
	}

	public void setGroupTransferByHour(double[][][] groupTransferByHour) {
		this.groupTransferByHour = groupTransferByHour;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
