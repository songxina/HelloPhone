package com.gen.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gen.data.HourDayDist;

import de.fhpotsdam.unfolding.geo.Location;

public class CalLocation {

	ArrayList<Location> stationLocByHourWeekdays[] = new ArrayList[24];
	ArrayList<Location> stationLocByHourWeekends[] = new ArrayList[24];
	Map<String,Double> stationMapNumberWeekdays = new HashMap<String,Double>();
	Map<String,Double> stationMapNumberWeekends = new HashMap<String,Double>();
	HourDayDist h ;
	
	public CalLocation(){
		h = new HourDayDist();
	}
	public void setStationData(){
		
		for (int j = 0; j < 24; j++){
			stationLocByHourWeekdays[j] = new ArrayList<Location>();
			stationLocByHourWeekends[j] = new ArrayList<Location>();
		}		
		Map<String,Double> tdays,tends;//加载全部基站分布的时间
		for(int i=0;i<=0;i++){			
			h.calStationNumber(i);			
			tdays = h.getStationNumMapWeekdays();
			tends = h.getStationNumMapWeekends();
			String temp[];
			for(String k: tdays.keySet()){
				
				temp = k.split("_");
				Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
				
				stationMapNumberWeekdays.put(k+"_"+i, tdays.get(k));
				stationLocByHourWeekdays[i].add(a);
			}
			for(String k: tends.keySet()){
				stationMapNumberWeekends.put(k+"_"+i, tends.get(k));
				temp = k.split("_");
				Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
				stationLocByHourWeekends[i].add(a);
			}
			System.out.println("station:"+i);
		}
		System.out.println("DONE!");
		
	}
	//将string转换成location
		public static ArrayList<Location> transLocations(ArrayList<String> list){
			ArrayList<Location> loc = new ArrayList<Location>();
			String temp[];
			for(String lc :list){
				temp = lc.split("_");
				Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
				loc.add(a);
			}
			return loc;
		}
				
		public ArrayList<Location>[] getStationLocByHourWeekdays() {
			return stationLocByHourWeekdays;
		}
		public ArrayList<Location>[] getStationLocByHourWeekends() {
			return stationLocByHourWeekends;
		}
		public Map<String, Double> getStationMapNumberWeekdays() {
			return stationMapNumberWeekdays;
		}
		public Map<String, Double> getStationMapNumberWeekends() {
			return stationMapNumberWeekends;
		}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
