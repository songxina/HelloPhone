package com.gen.trajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.fhpotsdam.unfolding.geo.Location;

public class MatrixForGroup {

	ArrayList<String> differentCell = new ArrayList<String>();//���ֹ������л�վ
	public HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//��վID�Ծ������
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//��¼���л�վ������Ķ�Ӧ��ϵ����Ӧdifferentcell��
	double locations[][];//λ�þ��󣬼�¼���ָ���
	double transferAllDay[][];//ȫ��ת�ƾ��󡣼�¼ת�Ƹ���
	double transferByHour[][][];//ÿСʱת�ƾ��󡣼�¼ת�Ƹ���
	int size = 0;//��ͬ��վ�ĸ���
	public double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	MatrixForPersonAndCell matrix;
	String deviceID;
	
	//����
	int daySize = 24;
	ArrayList<String> cellAll[][] = new ArrayList[daySize][25];	//Ŀǰ���������20131205-20131228 ��25��
	Cluster cluster;
	HashMap<String, String> cellToGroup = new HashMap<String, String>();//��¼��վ��Ӧ����
	ArrayList<Group> cellGroup[] = new ArrayList[24];//��¼ÿСʱ����
	double groupTransferByHour[][][];//�Ծ���Ϊ��λ��ÿСʱת�ƾ��󡣼�¼ת�Ƹ��ʣ����������е��¼֮�ͣ�
	//ÿ��������ֵĸ��ʣ���¼��group���е�possibility��
	
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
	//��cluster��cellGroup����cellLocationGroup
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
	//��stringת����location(list)
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
	//��stringת����location
	public Location stringToLoc(String cell){
		String lc =null;String temp[];
		lc= cellToCoordinate.get(cell);
		temp = lc.split("_");
		Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
		return a;
	}
	
	//����ÿ��cluster��possibility
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
	//����cell����groupID
	public String getGroupIDByCell(String cell,int hour) {
		String gid = "";
		for(Group g:cellGroup[hour])
			if(g.hasCell(cell))
				gid = g.gourpID;
		return gid;
	}
	
	//����ÿ��cluster��ת�Ƹ��ʣ���СʱΪ��λ
	public void calClusterTransfer(){
		//����cellToGroup
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
	
	//����һ��groupID������������ת�Ƶ�group���Ļ�վ������
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
	
	//����grouplist,����ÿСʱ���ֹ��groupID�����飩
	public String[] getEndGroup(){
		String group[] = new String[24];
		//����ÿ��group��endValue
		for(int day=0;day<daySize;day++){
			for(int hour=0;hour<24;hour++){
				//ȡǰ�������ֱ��group��startValueֵ����һ��cell��3���ڶ�����2����������1.
				int number = 3;
				int nsize = cellAll[day][hour].size();
				if(number>nsize)
					number = nsize;
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
		//ѡȡ���endValue��group
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
	
	//����grouplist,����ÿСʱ���ʼ��groupID�����飩
		public String[] getStartGroup(){
			String group[] = new String[24];
			//����ÿ��group��startValue
			for(int day=0;day<daySize;day++){
				for(int hour=0;hour<24;hour++){
					//ȡ���������ֱ��group��startValueֵ����һ��cell��3���ڶ�����2����������1.
					int number = 3;
					int nsize = cellAll[day][hour].size();
					if(number>nsize)
						number = nsize;
					if(nsize>5)
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
			//ѡȡ���startValue��group
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
