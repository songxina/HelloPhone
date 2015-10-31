package com.gen.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fhpotsdam.unfolding.geo.Location;

public class HomeDetection {

	private  Map<String,Double> allStationAndNum = new HashMap<String,Double>();//所有的station（loc.x+"_"+loc.y+"_"+i）的得分
	private  Set<String> allStations = new HashSet<String>();		//8pm-8am所有的station  loc.x+"_"+loc.y
	private  ArrayList<Location> locByHour[];
	private  int recordSize[],stationSize[];
	private  Map<String,Integer> stationNumAllDay;
	
	public HomeDetection(ArrayList<Location> locByHour[],int recordSize[],Map<String,Integer> stationNumAllDay){
		this.locByHour = locByHour;
		this.recordSize = recordSize;
		this.stationNumAllDay = stationNumAllDay;
		stationSize = new int[24];
	}
	
	//24小时每小时基站及数目，WuxiGsm中的mapNumberWeekdays   mapNumberWeekdays.put(loc.x+"_"+loc.y+"_"+i, x.get(loc));
	public void getAllStations(){
		String temp[];
		int hour,num;
		for(String key :stationNumAllDay.keySet()){
			temp = key.split("_");
			hour = Integer.parseInt(temp[2]);
			if( (hour>=20 && hour<=23)|| (hour>=0 && hour<=8)){
				//将出现次数为1 的基站删掉
				num = stationNumAllDay.get(key);
				if(num==1){
					locByHour[hour].remove(new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1])));
					continue;
				}	
				allStations.add(temp[0]+"_"+temp[1]);
			}
			
		}
		for(int i=0;i<24;i++){
			stationSize[i] = locByHour[i].size();
		}
		for(String s: allStations){
			allStationAndNum.put(s, (double) 0);
		}
	}
	
	public void setSingleHourScore(int hour){
		int allRecordNum = recordSize[hour];
		double timeWeight = 1;
		if(hour==23|| (hour>=0 && hour<=8))
			timeWeight = 1.5;
		double numWeight = stationSize[hour]*0.2;//((Double)10.0/stationSize[hour]);
		double g;
		double singleNum;
		for(Location loc: locByHour[hour]){
			singleNum = (int)stationNumAllDay.get(loc.x+"_"+loc.y+"_"+hour);
			singleNum= singleNum*2;
			g = singleNum / allRecordNum;
			g = g*timeWeight;//*numWeight;
			double score = allStationAndNum.get(loc.x+"_"+loc.y);
			score+=g;
//			allStationAndNum.remove(loc.x+"_"+loc.y);
			allStationAndNum.put(loc.x+"_"+loc.y, score);
		}
	}
	
	public void setSumScore(){
		for(int i=20;i<=23;i++)
			setSingleHourScore(i);
		for(int i=0;i<=8;i++)
			setSingleHourScore(i);
		for(String key:allStationAndNum.keySet()){
			System.out.println(key+"__"+allStationAndNum.get(key));
		}
	}
	//取最大值
	public String findMax(){
		String max = null;
		double maxNum=0,tempNum;
		for(String key:allStationAndNum.keySet()){
			tempNum = allStationAndNum.get(key);
			if(tempNum>maxNum){
				max = key;
				maxNum = tempNum;
			}
		}
		return max;
	}
	
	public String calculate(){
		getAllStations();
		setSumScore();
		String max = findMax();
		return max;
	}  
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
