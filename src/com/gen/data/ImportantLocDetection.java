package com.gen.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fhpotsdam.unfolding.geo.Location;

public class ImportantLocDetection {

	public  DataMatrix dataMatrix[];
	public  String deviceID;
	public  ArrayList<String> days = new ArrayList<String>();
	public  Map<Location,Double> stationScore = new HashMap<Location,Double>();//所有的station的得分
	public  Map<Location,Integer> stationDayNum = new HashMap<Location,Integer>();//每个station出现的天数
	public  ArrayList<Location> allStations = new ArrayList<Location>();
	public  int dayNum;

	public ImportantLocDetection(String deviceID){
		this.deviceID = deviceID;
		getDays();
		dayNum = days.size();
		dataMatrix = new DataMatrix[dayNum];
		calculate();
	}
	
	public void getDataMatrix()  {
		String day;	
		for(int count=0;count<dayNum;count++){
			day = days.get(count);
			dataMatrix[count] = new DataMatrix(day,deviceID);
			dataMatrix[count].setData();
			dataMatrix[count].setStationHourNumber();
		}
		//获取出现过的所有station
		Set<Location> stations = new HashSet<Location>();
		Map<Location,Integer> stationNumByDayTemp;
		for(int i=0;i<dayNum;i++){
			stationNumByDayTemp = dataMatrix[i].getStationNumByDay();
			int a = dataMatrix[i].getAllRecordNum();
			for(Location loc : dataMatrix[i].getStationsByDay()){
//				if(stationNumByDayTemp.get(loc)>=a/50)//去除一些不重要的点
				stations.add(loc);
			}

		}
		for(Location l:stations){
			allStations.add(l);
			stationScore.put(l, 0.0);
		}

	}
	
	public void getEachStationDayNum(){
		
		for(Location loc:allStations){
			stationDayNum.put(loc, 0);
		}
		
		for(int i=0;i<dayNum;i++){
			for(Location key:stationDayNum.keySet()){
				if(dataMatrix[i].getStationsByDay().contains(key)){
					int n = stationDayNum.get(key);
					n++;
//					stationDayNum.remove(key);
					stationDayNum.put(key, n);
				}					
			}
		}
	}
	
	public void getScore(){
//		String day;
		for(Location loc:allStations){
			for(int d=0;d<dayNum;d++){
//				day = days.get(d);
				if(dataMatrix[d].getStationNumByDay().containsKey(loc)){
					double n = stationScore.get(loc);
					int recordNum = dataMatrix[d].getStationNumByDay().get(loc);
					int allRecordNum = dataMatrix[d].getAllRecordNum();
					double g = (double)recordNum/allRecordNum;//某天出现了多少次
					double hourWeight = dataMatrix[d].getStationHourNum().get(loc);//某天出现了多少小时
					n = g + hourWeight/24;
//					stationScore.remove(loc);
					stationScore.put(loc, n);
				}
			}
			double s = stationScore.get(loc);
			double dayWeight = stationDayNum.get(loc);//总共出现了几天
			s+=dayWeight/dayNum;
			stationScore.put(loc, s);
		}

	}
	public void calculate() {
		this.getDataMatrix();
		this.getEachStationDayNum();
		this.getScore();
	}
	public void print(){
//		for(Location l:allStations)
//			System.out.println(l.x+"_"+l.y);
		for(Location l:stationScore.keySet())
			System.out.println(l.x+"_"+l.y+"__"+stationScore.get(l));
	}
	//得到已存入数据库中的日期
	public ArrayList<String> getDays(){

		BufferedReader br = null;
		try {
			File file = new File("./data/data/days.txt");
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "utf-8"));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.length() < 8)
					continue;
				days.add(line);	
			}						
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
					br = null;
				} catch (IOException e) {
					e.printStackTrace();
				}}}
		return days;
	}
	public  Map<Location,Double> getImportantLoc(){
		return stationScore;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImportantLocDetection im = new ImportantLocDetection("99702511301865963");
		im.calculate();
		im.print();
	}

}
