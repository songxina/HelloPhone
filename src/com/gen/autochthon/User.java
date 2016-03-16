package com.gen.autochthon;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

	String deviceID = "";
	HashMap<String,Double> cellNum = new HashMap<String, Double>();//基站及其数量
	ArrayList<String> differentCell = new ArrayList<String>();//记录出现过的不同基站
	String homeCellID = "";
	String workCellID = "";
	int type = 1; //(0:unnative  1:native)
	
	public User(String deviceID){
		this.deviceID = deviceID;
	}
	
	public void fillInfo(String data){
		String info[] = data.split(",");
		deviceID = info[0];
		homeCellID = info[1];
		workCellID = info[2];
		int size = info.length;
		for(int i=3;i<size;i++){
			String temp[] = info[i].split("-");
			cellNum.put(temp[0],Double.parseDouble(temp[1]));
			if(!differentCell.contains(temp[0]))
				differentCell.add(temp[0]);
		}
	}
	
	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public HashMap<String, Double> getCellNum() {
		return cellNum;
	}

	public void setCellNum(HashMap<String, Double> cellNum) {
		this.cellNum = cellNum;
	}

	public String getHomeCellID() {
		return homeCellID;
	}

	public void setHomeCellID(String homeCellID) {
		this.homeCellID = homeCellID;
	}

	public String getWorkCellID() {
		return workCellID;
	}

	public void setWorkCellID(String workCellID) {
		this.workCellID = workCellID;
	}

//给定map和链表，对链表排序
	public ArrayList<String> sortByMap(ArrayList<String> list,HashMap<String,Double> map){
		ArrayList<String> result = new ArrayList<String>();
		int length = list.size();
		int[] isUsed = new int[length];
		for(int i=0;i<isUsed.length;i++)
			isUsed[i] = 0;
		//不断选取最大值，加入到result中
		double maxValue=0;
		int maxIndex=0;
		int numCount=0;
		while(true){
			if(numCount>=length)
				break;
			numCount++;
			maxValue=0;
			for(int i=0;i<length;i++){   //找最大值
				double tempValue = map.get(list.get(i));
				if(tempValue>maxValue && isUsed[i]!=1){
					maxValue = tempValue;						
					maxIndex = i;	
				}
			}
			isUsed[maxIndex] = 1;
			result.add(list.get(maxIndex));
		}
		return result;
	}

	public void sort(){
		differentCell = sortByMap(differentCell, cellNum);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
