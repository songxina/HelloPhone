package com.gen.autochthon;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

public class ShowOffline extends PApplet{
	
	private static final long serialVersionUID = -7202118212240992970L;
	private static final String DATA_DIRECTORY = "./data";
	Location wuxiLocation = new Location(31.587756f, 120.313505f);
	boolean buttonVisible= false;
	private Image icon;
	private UnfoldingMap map;
	private int initZoomLevel = 11;
	HashMap<String,Location> cellIDToLoc = new HashMap<String,Location>();
	
	HashMap<String,Double> autochthonHomeNum = new HashMap<String, Double>();
	HashMap<String,Double> autochthonWorkNum = new HashMap<String, Double>();
	HashMap<String,Double> unAutochthonHomeNum = new HashMap<String, Double>();
	HashMap<String,Double> unAutochthonWorkNum = new HashMap<String, Double>();
	HashMap<String,Double> autochthonAreaNum = new HashMap<String, Double>();
	HashMap<String,Double> unAutochthonAreaNum = new HashMap<String, Double>();
	ExtractFeature extractFeature = new ExtractFeature();
	
	public static void main(String[] agrs) {
		PApplet.main(new String[] { "com.gen.autochthon.ShowOffline" });
	}

	@Override
	public void setup() {

		// size(displayWidth,displayHeight,P2D);
		frame.setResizable(true);
		size(1150, 780);
		frameRate(30);
//		String mbTilesString = sketchPath(DATA_DIRECTORY+ "/map/wuxi-7-14.mbtiles");
		String mbTilesString = sketchPath("./data/map/WuxiRoad10-17.mbtiles");
		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));
		map.zoomToLevel(initZoomLevel);
		map.panTo(wuxiLocation);
		map.setZoomRange(10, 14);
		MapUtils.createDefaultEventDispatcher(this, map);

		getTransMap();
		extractFeature.fetchInfo();
		extractFeature.statisticInfo();
		autochthonHomeNum = extractFeature.autochthonHomeNum;
		autochthonWorkNum = extractFeature.autochthonWorkNum;
		unAutochthonHomeNum = extractFeature.unAutochthonHomeNum;
		unAutochthonWorkNum = extractFeature.unAutochthonWorkNum;
		
		autochthonAreaNum = extractFeature.autochthonAreaNum;
		unAutochthonAreaNum = extractFeature.unAutochthonAreaNum;
	}

	@Override
	public void draw() {
		background(0);
		this.frame.setTitle("Wuxi GSM");
		this.frame.setIconImage(icon);
	
		map.draw();
		
		properDraw(255, 255, 0,unAutochthonHomeNum);
		
	}
	
	//读取文件，获取map表，以便将stationID对照成其坐标
	public void getTransMap() {
		File file = new File("./data/data/base_station_GPS.txt");
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(file), "utf-8")
					);
			try {
				String line = br.readLine();
				String[] rs = null;
				while((line = br.readLine()) != null) {
					rs = line.split("\\|");
					Location a = new Location(Float.parseFloat(rs[4]),Float.parseFloat(rs[5]));
					cellIDToLoc.put(rs[0] +"_"+ rs[1], a);
				}
	//						System.out.println(map);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void properDraw(int a, int b, int c,HashMap<String, Double> listNum){

		for ( String cell: listNum.keySet()) {
			Location l = cellIDToLoc.get(cell);
			noStroke();				
			double num = listNum.get(cell);
			double d = 6*Math.log10(num)+1;
//			d=10;
//			int color = (int)(180*Math.log10(num))+65;
			int color =  200;
			fill(a, b, c, color);
			ScreenPosition pos = map.getScreenPosition(l);
			ellipse(pos.x, pos.y , (int)d, (int)d);
		}
	}
	@Override
	public void keyPressed() {

		if (keyPressed) {						
//		    if (key == 'd')
//		    	hour = (hour+1)%24;
//		    else if (key == 'a') 
//		    	hour = (hour+23)%24;
//		    else if (key == 'e') 
//		    	date = (date+1)%dayNum;
//		    else if (key == 'q') 
//		    	date = (date+dayNum-1)%dayNum;
//		    else if (key == 's')
//		    	showSingleHour = !showSingleHour;
		}
	}
	
}

