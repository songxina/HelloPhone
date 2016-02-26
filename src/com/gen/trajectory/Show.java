package com.gen.trajectory;

import java.awt.Image;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

public class Show extends PApplet{

	public QueryTrajectory q;
	int dayNum=24;
	private ArrayList<Location> locAll[][] = new ArrayList[dayNum][25];	
	private static final long serialVersionUID = -7202118212240992970L;
	private static final String DATA_DIRECTORY = "./data";
	Location wuxiLocation = new Location(31.587756f, 120.313505f);
	boolean buttonVisible= false;
	private Image icon;
	private UnfoldingMap map;
	private int initZoomLevel = 11;
	private boolean showSingleHour = false;
	
	public int date = 0;
	public int hour = 23;
	Map<Location,Double> importantLocations = new  HashMap<Location,Double>();
	
	//聚类信息
	MatrixForGroup find ;
	ArrayList<Group> cellGroup[] = new ArrayList[24];//记录每小时聚类
	
	public static void main(String[] agrs) {
		PApplet.main(new String[] { "com.gen.trajectory.Show" });
	}

	@Override
	public void setup() {

		// size(displayWidth,displayHeight,P2D);
		frame.setResizable(true);
		size(1150, 780);

		String mbTilesString = sketchPath(DATA_DIRECTORY+ "/map/wuxi-7-14.mbtiles");
//		String mbTilesString = sketchPath(DATA_DIRECTORY+ "/map/Wuxi-blue-7-16.mbtiles");
		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));
		map.zoomToLevel(initZoomLevel);
		map.panTo(wuxiLocation);
		map.setZoomRange(10, 15);
		MapUtils.createDefaultEventDispatcher(this, map);

		frameRate(30);
		//获取设备信息
		//99070818249470658	各种路径
		//99249789030016916  长路径
		//99249764168730152
		//99249788048010590
		//99702516988779459
		//99168971352354021
		String device = "99249764168730152";
		q = new QueryTrajectory(device);
		try {
			locAll = q.getAllTrajectory();
			find = new MatrixForGroup(device);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		cellGroup = find.calAllGroup();
		
//		System.out.println("Important locatoin detecting...");
//		ImportantLocDetection im = new ImportantLocDetection(device);
//		im.calculate();
//		im.print();
//		importantLocations = im.getImportantLoc();
		
	}

	@Override
	public void draw() {
		background(0);
		this.frame.setTitle("Wuxi GSM");
		this.frame.setIconImage(icon);
	
		map.draw();
		
		properDraw(255, 255, 0);
		
		//显示文本
		String day = QueryTrajectory.getDays().get(date);
		String week = "";
		if(isWeekends(day))
			week = "WEEKENDS";
		else
			week = "WEEKDAYS";
		String time = ""+hour;
		if(!showSingleHour)
			time = "0-"+hour;
		String text = "Date:"+day+"\n"
				+"Hour:"+time+"\n"
				+week;
	    fill(255);
		this.textSize(22);
			text(text, 35, 50);
	}

	private void properDraw(int a, int b, int c){

		ArrayList<Location> loc = null;
		Location leftLoc = null;
		if(!locAll[date][0].isEmpty())
			leftLoc = locAll[date][0].get(0);
		int i = 0;
		if(showSingleHour)
			i = hour;
		for(;i<=hour;i++){
			loc = locAll[date][i];
			for (Location l : loc) {
				noStroke();
				fill(a-i*10, b, c+i*10, 100);
				ScreenPosition pos = map.getScreenPosition(l);
				ellipse(pos.x, pos.y , 15, 15);
				if(leftLoc != l && leftLoc!=null){
					ScreenPosition leftPos = map.getScreenPosition(leftLoc);			
					stroke(126);
					line(leftPos.x,leftPos.y,pos.x,pos.y);
					float y = (float) (pos.y +0.1*(leftPos.y-pos.y));
					float x = (float) (pos.x +0.1*(leftPos.x-pos.x));
					fill(255, 255, 0, 300);
					ellipse(x,y,5,5);
				}
				leftLoc = l;
			}
		}
//		for(Location key:importantLocations.keySet()){
//			double score = importantLocations.get(key)*25;
//			fill(255, 255,0, 30);
//			 stroke(255,0,0);
//			 ScreenPosition tloc = map.getScreenPosition(key);
//			 int s = (int) (3*Math.log10(score));
////			 if(s>7)
//			 	ellipse(tloc.x, tloc.y, s, s);
//		}
	}
	@Override
	public void keyPressed() {

		if (keyPressed) {						
		    if (key == 'd')
		    	hour = (hour+1)%24;
		    else if (key == 'a') 
		    	hour = (hour+23)%24;
		    else if (key == 'e') 
		    	date = (date+1)%dayNum;
		    else if (key == 'q') 
		    	date = (date+dayNum-1)%dayNum;
		    else if (key == 's')
		    	showSingleHour = !showSingleHour;
		}
	}
	//判断是否是周末
		public static boolean isWeekends(String day) {
			DateFormat format = new SimpleDateFormat("yyyyMMdd");         
			Date bdate = null;
			try {
				bdate = format.parse(day);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(bdate);
			 if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
			    	return true;
			 else 
				 return false;
		}
}
