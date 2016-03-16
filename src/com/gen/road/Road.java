package com.gen.road;

import java.util.ArrayList;
import java.util.Collections;

import de.fhpotsdam.unfolding.geo.Location;

public class Road {

	String id = "";
	ArrayList<String> allPoints = new ArrayList<String>();
	double length = 0;
	String startPoint ="";
	String endPoint = "";
	int pointSize = 0;
	int medianTime = 0;//所有时间的中位数
	ArrayList<Integer> allTime = new ArrayList<Integer>();//记录所有到达他的基站的时间
	
	public Road(String id,ArrayList<String> allPoints){
		this.id = id;
		this.allPoints = allPoints;
//		calLength();
		pointSize = allPoints.size();
		startPoint = allPoints.get(0);
		endPoint = allPoints.get(pointSize-1);
	}
	
	public void addTime(ArrayList<Integer> list){
		allTime.addAll(list);
		calMedianTime();
	}
	//获取time的中位数medianTime
	public void calMedianTime(){
		Collections.sort(allTime);
		int size = allTime.size();
		medianTime = allTime.get(size/2);
	}
	
	public int getMedianTime() {
		return medianTime;
	}

	//120.318084_31.5705033
	public void calLength(){
		for(int i=0;i<pointSize-1;i++){
			String a[] = allPoints.get(i).split("_");
			String b[] = allPoints.get(i+1).split("_");
			double distance= distance(Double.parseDouble(a[1]), Double.parseDouble(a[0])
					, Double.parseDouble(b[1]), Double.parseDouble(b[0]));
			length+=distance;
//			System.out.println("distance: "+distance);
		}
	}
	
	/* 返回一条路段的所有点
	 * ,120.1142253_31.5515778,120.1142072_31.5515824
	*/
	public ArrayList<Location> roadPointToLocatoins(){
		ArrayList<Location> points = new ArrayList<Location>();
		for(int i=0;i<pointSize;i++){
			String temp[] = allPoints.get(i).split("_");
			Location location = new Location(Double.parseDouble(temp[1]),Double.parseDouble(temp[0]));
			points.add(location);
		}
		return points;
	}
	
// 计算两坐标距离
	public double distance(double lat1, double longt1, double lat2,	double longt2) {
		double PI = 3.14159265358979323; // 圆周率
		double R = 6371229; // 地球的半径
		double x, y, distance;
		x = (longt2 - longt1) * PI * R
				* Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
		y = (lat2 - lat1) * PI * R / 180;
		distance = Math.hypot(x, y);//sqrt(x2 +y2) 
		return distance;
	}
}
