package com.gen.road;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import com.gen.locAndTrajectory.DataPreparation;
import com.gen.locAndTrajectory.Device;
import com.gen.trajectory.Group;
import com.gen.trajectory.MatrixForGroup;
import com.gen.trajectory.QueryTrajectory;
import com.widgets.Control;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PApplet;

public class ShowRoads extends PApplet{

	ArrayList<Road> allRoads = new ArrayList<Road>();//ÿһ��road���������
	ArrayList<String> commonPoint = new ArrayList<String>();//��¼���н���
	HashMap<String, String> pointToIndex = new HashMap<String, String>();//��¼ÿ����������
	ArrayList<Location> allStation = new ArrayList<Location>();//��¼���л�վλ��
	public ArrayList<ArrayList<String>> groupIDFromHomeToWork ;//��¼ÿ��home��work֮��ľ�������
	
	boolean showRoad = false;//r������
	boolean showCommonPoint = true;//p������
	boolean showStation = false;//s������
	boolean showText = false;//t������
	boolean showGroup = false;//g������
	boolean showPassedCell = false;//c������
	boolean showGroupTranfer = false;//l������
	
	private UnfoldingMap map;
	private int initZoomLevel = 11;
	int[] roadFrequene;//��¼ÿ��·���ߵĴ���
	HashMap<String,Integer> cellNumMap = new HashMap<String, Integer>();//��վ��Ҫ�����ֵ�map��ʽ
	ArrayList<Group> allGroups = new ArrayList<Group>();//·�������в�ͬ����(���־���)
	
	Device device = null;
//��¼����road�����������ʱ��
	double earliestTime;
	double latestTime;

	public void fetchAllRoads(){
		DealWithFile deal = new DealWithFile();
		allStation = deal.getAllStationPosition(allStation);
	}
	
	public static void main(String[] agrs) {
		PApplet.main(new String[] { "com.gen.road.ShowRoads" });
	}

	@Override
	public void setup() {

		// size(displayWidth,displayHeight,P2D);
		frame.setResizable(true);
		size(1150, 780);
		frameRate(20);
		String mbTilesString = sketchPath("./data/map/WuxiRoad10-17.mbtiles");
//		String mbTilesString = sketchPath("./data/map/wuxiRoadClip-7-16.mbtiles");
		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));
		map.zoomToLevel(initZoomLevel);
		Location wuxiLocation = new Location(31.587756f, 120.313505f);
		map.panTo(wuxiLocation); 
		map.setZoomRange(10, 17);
		MapUtils.createDefaultEventDispatcher(this, map);

		//��ȡ����·�� allRoads
		fetchAllRoads();
		deviceDataSetUp();
	}
	
	public void deviceDataSetUp(){
		try {
			//99249788048010590
			//99249764168730152
			//99702516988779459
			//99168971352354021
			device = new Device("99168971352354021");			
		} catch (ParseException e) {e.printStackTrace();}
		cellNumMap = device.fillCellScoreMap();
		DataPreparation dataPreparation = new DataPreparation(device);
		//�õ�ÿ��home��work֮��ľ�������
		device.groupIDFromHomeToWork = dataPreparation.extractTrajectoryFromHomeToWork();
		groupIDFromHomeToWork = device.groupIDFromHomeToWork;
		allGroups = dataPreparation.getDifferentGroupInAllPath();
//		for(Group group:allGroups){
//		System.out.println(group.getGourpID()+": "+group.getAllTime());
//	}
		for(ArrayList<String> list:device.groupIDFromHomeToWork)
			System.out.println(list);
		RoadPreparation road = new RoadPreparation();
		//dataPreparation���м�¼ÿ������� ʱ��
		roadFrequene = road.roadFrequency(dataPreparation.cellGroup,device.groupIDFromHomeToWork);
		commonPoint = road.getCommonPoint();
		allRoads = road.getAllRoads();
		latestTime = device.getWorkGroup().calMedianTime();
		earliestTime = device.getHomeGroup().calMedianTime();
//		for(Road r:allRoads){
//			int time = r.getMedianTime();
//			double a = (time-earliestTime)/(latestTime-earliestTime);
//			a = a*255;
//			if(time!=0)
//				System.out.println(time+"  "+earliestTime+"  "+latestTime+"  "+a);
//		}
	}
	
	@Override
	public void draw() {
		background(0);
		this.frame.setTitle("Show Roads");
		map.draw();
		
		if(showCommonPoint)
			drawCommonPoint();
		if(showStation)
			drawAllStations();
		if(showRoad)
			drawTheRoad();
		if(showGroupTranfer)
			drawGroupTranfer();
//		if(showGroup)
			drawGroup();
	}
	//�������н���
	public void drawCommonPoint(){
		int i=0;
		for(String p:commonPoint){
			String temp[] = p.split("_");
			Location location = new Location(Double.parseDouble(temp[1]),Double.parseDouble(temp[0]));
			ScreenPosition pos = map.getScreenPosition(location);
			stroke(255,255,255);
//			noStroke();
			strokeWeight(1);
			fill(0, 0, 255, 255);
			int zoom = map.getZoomLevel()-10;
			ellipse(pos.x, pos.y , 1+zoom, 1+zoom);
			//��ע�������
			if(showText){
				fill(255,255,255,255);
				this.textSize(10);
				text(i, pos.x+5,  pos.y+5);
				i++;
			}
		}
	}
	//�����л�վ
	public void drawAllStations(){
		for(Location loc:allStation){
			ScreenPosition pos = map.getScreenPosition(loc);
			noStroke();
			fill(255, 255, 0, 100);
			int zoom = (map.getZoomLevel()-10)/2;
			ellipse(pos.x, pos.y , 1+zoom, 1+zoom);
		}
	}
	
	//�����е�·
	public void drawTheRoad(){
		for(int i=0;i<allRoads.size();i++){
			Road road = allRoads.get(i);
			int medianTime = road.getMedianTime();
			if(medianTime<earliestTime)
				medianTime = (int)earliestTime;
			if(medianTime>latestTime)
				medianTime = (int)latestTime;
//			int roadColor = ((latestTime-medianTime)*5)%255;
			int roadColor = (int) ((((double)medianTime-earliestTime)/(latestTime-earliestTime))*255);

			int frequence = roadFrequene[i];			
			if(frequence==0)
				continue;
			if(frequence!=0)
				System.out.println(frequence);
			ArrayList<Location> points = road.roadPointToLocatoins();
			Location leftLoc = points.get(0);
			for(Location p:points){
//				stroke(255,255,255);
//				noStroke();
//				fill(255, 255, 255, 10);
				ScreenPosition pos = map.getScreenPosition(p);
//				int zoom = map.getZoomLevel()-11;
//				ellipse(pos.x, pos.y , 1+zoom, 1+zoom);
				if(leftLoc != p){
					ScreenPosition leftPos = map.getScreenPosition(leftLoc);			
					stroke(255-roadColor,255-roadColor,roadColor,255);//��ɫ��������������
					
//					frequence = frequence%50;
					if(frequence>=30)
						frequence = 30;
					strokeWeight(frequence);
					line(leftPos.x,leftPos.y,pos.x,pos.y);
				}
				leftLoc = p;
			}
		}
	}
	//�������е�;���
	public void drawGroup(){
		int groupSize = allGroups.size();
		for(int i=0;i<groupSize;i++){
			Group group = allGroups.get(i);
			//��ע��������
			String cc =group.getCenterCell();
			ScreenPosition cpos = map.getScreenPosition(device.stringToLoc(cc));
			fill(0, 0,255, 130);
//			stroke(0,0,0);
			noStroke();
			if(showGroup)
			ellipse(cpos.x, cpos.y, 35, 35);
			if(group.isImportLoc){
				fill(0, 0,255, 200);
				stroke(255,255,255);
				strokeWeight(1);
				ellipse(cpos.x, cpos.y, 40, 40);
			}
			//��ע������
			if(i<groupSize){
				fill(200);
				this.textSize(21);
				if(showGroup)
				text(group.getGourpID(), cpos.x, cpos.y);
			}
			Location leftLoc = null;
			for (String w:group.getCellGroup()) {
				Location l = device.stringToLoc(w);
				noStroke();
				fill(100, 255, 0, 255);
				ScreenPosition pos = map.getScreenPosition(l);
				int pointSize = cellNumMap.get(w);
				
				pointSize = (int)(2*Math.log(pointSize)+4);
				if(showPassedCell)
					ellipse(pos.x, pos.y, pointSize, pointSize);
				if(leftLoc!=null){
					ScreenPosition leftPos = map.getScreenPosition(leftLoc);			
					stroke(200);
//					noStroke();
					strokeWeight(1);
					if(showGroup)
					line(leftPos.x,leftPos.y,pos.x,pos.y);
				}
				leftLoc = l;
			}
		}
	}
	//������վ�����ߣ�ÿ���·����
	public void drawGroupTranfer(){
		int pathSize = groupIDFromHomeToWork.size();
		int color = 0;
		int a = 255/pathSize;

		for(ArrayList<String> list:groupIDFromHomeToWork){
			for(int i=0;i<list.size()-1;i++){
				int pindex = Integer.parseInt(list.get(i));
				Group pre = device.cellGroup.get(pindex);
				int nindex = Integer.parseInt(list.get(i+1));
				Group next = device.cellGroup.get(nindex);
				//��ע��������
				String pc =pre.getCenterCell();
				ScreenPosition ppos = map.getScreenPosition(device.stringToLoc(pc));
				String nc =next.getCenterCell();
				ScreenPosition npos = map.getScreenPosition(device.stringToLoc(nc));
				stroke(255-color, 255-color,color, 250);
				strokeWeight(4);
				if(showGroup)
					line(ppos.x,ppos.y,npos.x,npos.y);
			}
			color+=a;
		}
	}
	
	@Override
	public void keyPressed() {
		if (keyPressed) {			
			if (key == 'r') 
		    	showRoad = !showRoad;		    	
		    else if (key == 'p') 
		    	showCommonPoint = !showCommonPoint;		    	
		    else if (key == 's') 
		    	showStation = !showStation;
		    else if (key == 't') 
		    	showText = !showText;
		    else if (key == 'g') 
		    	showGroup = !showGroup;
		    else if (key == 'c') 
		    	showPassedCell = !showPassedCell;
		    else if (key == 'l') 
		    	showGroupTranfer = !showGroupTranfer;
		  }
	}
}
