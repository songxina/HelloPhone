package com.gen.location;

import java.awt.Image;
import java.awt.Toolkit;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gen.data.HomeDetection;
import com.gen.data.ImportantLocDetection;
import com.gen.trajectory.FindTrajectory;
import com.gen.trajectory.MatrixForGroup;
import com.gen.trajectory.Group;
import com.gen.voronoi.MPolygon;
import com.gen.voronoi.Voronoi;
import com.widgets.Button;
import com.widgets.CircleButton;
import com.widgets.Control;
import com.widgets.Panel;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.event.MouseEvent;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

public class WuxiGsm extends PApplet {

	private static final long serialVersionUID = -7202118212240992970L;
	private static final String DATA_DIRECTORY = "./data";
	Location wuxiLocation = new Location(31.587756f, 120.313505f);
	boolean buttonVisible= false;
	private Image icon;
	private UnfoldingMap map;
	private int initZoomLevel = 11;
	private ArrayList<Location>  weekdaysPart1, weekdaysPart2, weekdaysPart3, weekdaysPart4,weekdaysAll;
	private ArrayList<Location>  weekendsPart1, weekendsPart2, weekendsPart3, weekendsPart4,weekendsAll;
	CircleButton cbutton = null,ctype=null,cstation;
	CircleButton cbuttons[] = new CircleButton[24];
	Panel panel = null;
	List<Control> controls = new ArrayList<Control>();
	Boolean[] showLoc = new Boolean[27];
	ArrayList<Location> locByHourWeekdays[] = new ArrayList[24];
	ArrayList<Location> locByHourWeekends[] = new ArrayList[24];
	int sizeWeekdays[] = new int[25];
	int sizeWeekends[] = new int[25];
	String numberWeekdays="",numberWeekends="";
	Map<String,Integer> mapNumberWeekdays = new HashMap<String,Integer>();
	Map<String,Integer> mapNumberWeekends = new HashMap<String,Integer>();
	CalLocation calLocation = new CalLocation();
	Voronoi myVoronoi;
	MPolygon[] myRegions;
	boolean firstVorDraw = true;
	String home;
	Map<Location,Double> importantLocations = new  HashMap<Location,Double>();
	Set<Location> weekdaysAllDistinctSet = new HashSet<Location>();
	Set<Location> weekendsAllDistinctSet = new HashSet<Location>();
	ArrayList<Location> weekdaysAllDistinct  = new ArrayList<Location>();
	ArrayList<Location> weekendsAllDistinct  = new ArrayList<Location>();
	//聚类信息
	MatrixForGroup matrixGroup;
	FindTrajectory find;
	ArrayList<Group> cellGroup[] = new ArrayList[24];//记录每小时聚类
	boolean showGroup = true;//s键控制
	boolean showLineBetweenGroup = false;//w键控制
	boolean showPath = true;//q键控制
	boolean showImportantLoc = false;//q键控制
	String startValueGroup[];//给定grouplist,返回每小时最常起始的groupID（数组）
	String endValueGroup[];

	public static void main(String[] agrs) {
		PApplet.main(new String[] { "com.gen.location.WuxiGsm" });
	}

	public void setup() {

		// size(displayWidth,displayHeight,P2D);
		frame.setResizable(true);
		size(1150, 780);

		String mbTilesString = sketchPath(DATA_DIRECTORY+ "/map/wuxi-7-14.mbtiles");
//		String mbTilesString = sketchPath(DATA_DIRECTORY+ "/map/Wuxi-blue-7-16.mbtiles");
		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));
		// map = new UnfoldingMap(this);
		map.zoomToLevel(initZoomLevel);
		map.panTo(wuxiLocation);
		map.setZoomRange(10, 15);
		// map.setPanningRestriction(wuxiLocation, 50);
		MapUtils.createDefaultEventDispatcher(this, map);

		frameRate(10);
		//showLoc0-23代表每个小时，24代表所有点，25代表周末/工作日，true是工作日,26代表显示station信息
		for(int i=0;i<27;i++)
			showLoc[i] = false;
		showLoc[24]=true;
			//获取设备信息
		//99249788048010590
		//99249764168730152
		String device = "99249764168730152";
		try {
			getLocations(device);
			matrixGroup = new MatrixForGroup(device);//聚类信息
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cellGroup = matrixGroup.calAllGroup();//聚类信息
		startValueGroup = matrixGroup.getStartGroup();//起始位置
		endValueGroup = matrixGroup.getEndGroup();//终止位置
		find = new FindTrajectory(matrixGroup);
		
		calLocation.setStationData();//查询有关station的数据
		setControlPanel();
		this.registerMethod("mouseEvent", this);
		
		//每小时数据数量
		for(int i=0;i<24;i++){
		 numberWeekdays +=i+":"+sizeWeekdays[i]+"\n";
		 numberWeekends +=i+":"+sizeWeekends[i]+"\n";
		}
		numberWeekdays+="ALL:"+sizeWeekdays[24];
		numberWeekends+="ALL:"+sizeWeekends[24];
	}

	public void getLocations(String device) throws ParseException {
		weekdaysPart1 = new ArrayList<Location>();	weekdaysPart2 = new ArrayList<Location>();	weekdaysPart3 = new ArrayList<Location>();	weekdaysPart4 = new ArrayList<Location>();
		weekendsPart1 = new ArrayList<Location>();	weekendsPart2 = new ArrayList<Location>();	weekendsPart3 = new ArrayList<Location>();	weekendsPart4 = new ArrayList<Location>();	
		weekdaysAll = new ArrayList<Location>();
		weekendsAll = new ArrayList<Location>();
		
		for (int j = 0; j < 24; j++){
			locByHourWeekdays[j] = new ArrayList<Location>();
			locByHourWeekends[j] = new ArrayList<Location>();
		}
		for (int i = 0; i < 24; i++) {
			///////////////////////////////////////////////////////////
			ImportDevices.getCertainHour(i,device);
			locByHourWeekdays[i] = ImportDevices.getLocListWeekdays();
			locByHourWeekends[i] = ImportDevices.getLocListWeekends();
			sizeWeekdays[i] = ImportDevices.getWeekdaysSize();
			sizeWeekends[i] = ImportDevices.getWeekendsSize();		
			Map<Location,Integer> x = ImportDevices.getMapNumberWeekdays();
			for(Location loc : x.keySet())
				mapNumberWeekdays.put(loc.x+"_"+loc.y+"_"+i, x.get(loc));
			
			Map<Location,Integer> y = ImportDevices.getMapNumberWeekends();
			for(Location loc : y.keySet())
				mapNumberWeekends.put(loc.x+"_"+loc.y+"_"+i, y.get(loc));
		}
		setParts(locByHourWeekdays,weekdaysPart1,weekdaysPart2,weekdaysPart3,weekdaysPart4,weekdaysAll,weekdaysAllDistinctSet);
		setParts(locByHourWeekends,weekendsPart1,weekendsPart2,weekendsPart3,weekendsPart4,weekendsAll,weekendsAllDistinctSet);
		for(Location loc : weekdaysAllDistinctSet)
			weekdaysAllDistinct.add(loc);
		for(Location loc : weekendsAllDistinctSet)
			weekendsAllDistinct.add(loc);
		
		//计算24小时各个基站总数
		Map<Location,Integer> mapTempWeekdays = new HashMap<Location,Integer>();
		Map<Location,Integer> mapTempWeekends = new HashMap<Location,Integer>();
		calMapNumber(weekdaysAll,mapTempWeekdays);
		calMapNumber(weekendsAll,mapTempWeekends);
		for (Location loc:mapTempWeekdays.keySet())
			mapNumberWeekdays.put(loc.x+"_"+loc.y+"_"+24, mapTempWeekdays.get(loc));
		for (Location loc:mapTempWeekends.keySet())
			mapNumberWeekends.put(loc.x+"_"+loc.y+"_"+24, mapTempWeekends.get(loc));
			

		
		sizeWeekdays[24]=weekdaysPart1.size() + weekdaysPart2.size() + weekdaysPart3.size() + weekdaysPart4.size();
		sizeWeekends[24]=weekendsPart1.size() + weekendsPart2.size() + weekendsPart3.size() + weekendsPart4.size();
//		System.out.prinstln(mapNumberWeekends);
		System.out.println("Home detecting...");
		HomeDetection  h = new HomeDetection(locByHourWeekends,sizeWeekends,mapNumberWeekends);
		home = h.calculate();
		
//		System.out.println("Important locatoin detecting...");
//		ImportantLocDetection im = new ImportantLocDetection(device);
//		im.calculate();
//		im.print();
//		importantLocations = im.getImportantLoc();
//		
//		for (Location loca:mapTempWeekdays.keySet())
//			if(mapTempWeekdays.get(loca)!=1)
//			System.out.println(loca.x+"_"+loca.y);
	}

	
	public void setParts(ArrayList<Location> a[],ArrayList<Location> p1,ArrayList<Location> p2,ArrayList<Location> p3,ArrayList<Location> p4,ArrayList<Location> pall,Set<Location> s){
	
		for(int i=0;i<24;i++){
			pall.addAll(a[i]);
			s.addAll(a[i]);
			if (i >= 7 && i < 10)
				p1.addAll(a[i]);
			else if (i >= 10 && i < 19)
				p2.addAll(a[i]);
			else if (i >= 19 && i < 22)
				p3.addAll(a[i]);
			else
				p4.addAll(a[i]);
		}		
	}
	//计算链表中不同地址出现的次数
	public static void calMapNumber(ArrayList<Location> all,Map<Location,Integer> m){

			for(Location loc:all){
				if(m.containsKey(loc)){
					int number = m.get(loc);
					number++;
//					m.remove(loc);
					m.put(loc, number);
				}
				else{
					m.put(loc, 1);
				}
			}
		}
		
	public void setControlPanel() {

		for (int i = 0; i < controls.size(); i++)
			controls.remove(i);

		cbutton = new CircleButton(this, 1 * width / 20 + 70, 4 * height / 5 + 20, 25, 25,	showLoc,"ALL");
		controls.add(cbutton);		
		ctype = new CircleButton(this, 1 * width / 20 + 70, 4 * height / 5 + 20, 25, 25,showLoc,"weekend");
		controls.add(ctype);
		cstation = new CircleButton(this, 1 * width / 20 + 40, 4 * height / 5 + 20, 25, 25,showLoc,"station");
		controls.add(cstation);
		
		int half = 12;

		for (int i = 0; i < half; i++) {
			cbuttons[i] = new CircleButton(this, 1 * width / 20 + 120
					+ i * 35, 4 * height / 5 + 20, 25, 25, showLoc, "" + i);
			controls.add(cbuttons[i]);
		}

		for (int i = half; i < 24; i++) {
			cbuttons[i] = new CircleButton(this, 1 * width / 20 + 120
					+ (i - half) * 35, 19 * height / 20 - 30, 25, 25, showLoc,
					"" + i);
			controls.add(cbuttons[i]);
		}
		for (Control control : controls) {
			control.display();
		}
	}
	
	
	public void updateButtonPos(){
	cbutton.reset(this, 1 * width / 20 + 80, 4 * height / 5 + 20, 25, 25,showLoc,"ALL");
	ctype.reset(this, 1 * width / 20 + 75, 4 * height / 5 + 20+75, 25, 25,showLoc,"weekend");
	cstation.reset(this, 1 * width / 20 + 40, 4 * height / 5 + 20+75, 25, 25,showLoc,"station");
	int half = 12;
	for (int i = 0; i < half; i++) 
		cbuttons[i].reset(this, 1 * width / 20 + 120+ i * 35, 4 * height / 5 + 20, 25, 25, showLoc, "" + i);			
	for (int i = half; i < 24; i++) 
		cbuttons[i].reset(this, 1 * width / 20 + 120+ (i - half) * 35, 19 * height / 20 - 30, 25, 25, showLoc,"" + i);

}

	public void draw() {
		background(0);
		this.frame.setTitle("Wuxi GSM");
		this.frame.setIconImage(icon);
	
		map.draw();
		if(buttonVisible)
			updateButtonPos();

		for(int i=0;i<24;i++){
			if(showLoc[i]){
				if(!showLoc[26]){
					if(!showLoc[25])
						properDraw(locByHourWeekdays[i],mapNumberWeekdays,i,255, 255, 0);
					else
						properDraw(locByHourWeekends[i],mapNumberWeekends,i,255, 255, 0);
				}
				else{
					if(!showLoc[25])
						properStationDraw(calLocation.getStationLocByHourWeekdays()[i],calLocation.getStationMapNumberWeekdays(),i,255, 255, 0);
					else
						properStationDraw(calLocation.getStationLocByHourWeekends()[i],calLocation.getStationMapNumberWeekends(),i,255, 255, 0);
				}
			}	
		}/////////////////////////////////////////////////////////////////////////////////////////////
		if(showLoc[24]){
			if(!showLoc[25]){
				properDraw(weekdaysAllDistinct,mapNumberWeekdays,24,255, 255, 0);
//				properDraw(weekdaysAll,mapNumberWeekdays,24,255, 255, 0);
//				randomDraw(weekdaysPart1, 255, 255, 0);//yellow
//				randomDraw(weekdaysPart2, 255, 255, 255);//white		  
//				randomDraw(weekdaysPart3, 255, 0, 0);//red
//				randomDraw(weekdaysPart4, 0, 0, 0);//black
			}
			else
			{
				properDraw(weekendsAllDistinct,mapNumberWeekends,24,255, 255, 0);
//				properDraw(weekendsAll,mapNumberWeekends,24,255, 255, 0);
//				randomDraw(weekendsPart1, 255, 255, 0);//yellow
//				randomDraw(weekendsPart1, 255, 255, 255);//white		
//				randomDraw(weekendsPart1, 255, 0, 0);//red
//				randomDraw(weekendsPart1, 0, 0, 0);//black
			}
		}
		if(!showLoc[26])
		if(buttonVisible){
			fill(255);
			this.textSize(13);
			if(!showLoc[25])
				text(numberWeekdays, 35, 50);
			else
				text(numberWeekends, 35, 50);
		}
	}

	private void properDraw(ArrayList<Location> loc,Map<String,Integer> m,int hour, int a, int b, int c){

		//所有点
		if(loc.size()!=0){
			HashSet<Location> sss = new HashSet<Location>();
			sss.addAll(loc);
			int cou=0;
			for (Location l : sss) {
				noStroke();				
				int n;
				n = m.get(l.x+"_"+l.y+"_"+hour);
				double d = 4*Math.log10(n)+6;
				int color = (int)(180*Math.log10(n))+65;
				fill(a, b, c, color);
				ScreenPosition pos = map.getScreenPosition(l);
				ellipse(pos.x, pos.y , (int)d, (int)d);
			}
			//home		
			String maxloc[] = home.split("_");
			Location mloc = new Location(Float.parseFloat(maxloc[0]),Float.parseFloat(maxloc[1]));
			ScreenPosition mpos = map.getScreenPosition(mloc);
			fill(0, 255,255, 50);
			stroke(255,255,255);
			ellipse(mpos.x, mpos.y, 35, 35);
			
			//聚类
			if(hour<24&&showGroup){
				ArrayList<Group> groups = cellGroup[hour];
				ArrayList<String> ce = new ArrayList<String>();
				for(int i=0;i<groups.size();i++){
					ce.addAll(groups.get(i).getCellGroup());
				}
				//每个聚类内部
				for(int i=0;i<groups.size();i++){
					Group group =  groups.get(i);
					//标注聚类中心
					String cc =group.getCenterCell();
					ScreenPosition cpos = map.getScreenPosition(matrixGroup.stringToLoc(cc));
					fill(0, 0,255, 70);
					stroke(0,0,0);
					ellipse(cpos.x, cpos.y, 35, 35);
					
					Location leftLoc = null;

					for (String w:group.getCellGroup()) {
						Location l = matrixGroup.stringToLoc(w);
						noStroke();
						fill(100, 255, 0, 50);
						ScreenPosition pos = map.getScreenPosition(l);
//						ellipse(pos.x, pos.y, 15, 15);
						if(leftLoc!=null){
							ScreenPosition leftPos = map.getScreenPosition(leftLoc);			
							stroke(126);
							line(leftPos.x,leftPos.y,pos.x,pos.y);
						}
						leftLoc = l;
					}
				}
				//聚类之间画线
				if(showLineBetweenGroup)
				for(int i=0;i<groups.size();i++){
					Group group =  groups.get(i);
					Location preloc = group.getCenterLocation();
					ScreenPosition leftPos = map.getScreenPosition(preloc);
					Location nextloc = matrixGroup.getNextGroupCenter(group);
					if(nextloc!=null){
						ScreenPosition pos= map.getScreenPosition(nextloc);			
						stroke(230);
						fill(255, 255, 255, 50);
						line(leftPos.x,leftPos.y,pos.x,pos.y);
						float y = (float) (pos.y +0.2*(leftPos.y-pos.y));
						float x = (float) (pos.x +0.2*(leftPos.x-pos.x));
						fill(255, 255, 0, 300);
						ellipse(x,y,5,5);
					}
				}
				//标注每小时最大概率起始点 和 终止点
				String id = startValueGroup[hour];
				int index = Integer.parseInt(id.split("_")[1]);
				Group sg = groups.get(index);
				String cc =sg.getCenterCell();
				ScreenPosition cpos = map.getScreenPosition(matrixGroup.stringToLoc(cc));
				fill(0, 255,255, 50);
				stroke(255,255,255);
				ellipse(cpos.x, cpos.y, 40, 40);
				
				//标注每小时最大概率 终止点
				String eid = endValueGroup[hour];
				int eindex = Integer.parseInt(eid.split("_")[1]);
				Group eg = groups.get(eindex);
				String ecc =eg.getCenterCell();
				ScreenPosition ecpos = map.getScreenPosition(matrixGroup.stringToLoc(ecc));
				fill(0, 255,255, 50);
				stroke(255,255,255);
				ellipse(ecpos.x, ecpos.y, 40, 40);
//				System.out.println("起始和终止点："+id+"_"+eid);
				
				//路径
				ArrayList<String> path = find.getRegularPath(hour);
				String begin = path.get(0);
				Location beginLoc = matrixGroup.stringToLoc(begin);//非常优秀的功能
				ScreenPosition leftPos = map.getScreenPosition(beginLoc);
				if(showPath)
				for(int i=1;i<path.size();i++){
					String n = path.get(i);
					Location nextloc = matrixGroup.stringToLoc(n);
					if(nextloc!=null){
						ScreenPosition pos= map.getScreenPosition(nextloc);			
						stroke(230);
						fill(255, 0, 0, 100);
						line(leftPos.x,leftPos.y,pos.x,pos.y);
						float y = (float) (pos.y +0.2*(leftPos.y-pos.y));
						float x = (float) (pos.x +0.2*(leftPos.x-pos.x));
						fill(255, 255, 0, 300);
						ellipse(x,y,5,5);
						leftPos = pos;
					}					
				}
			}//24
		}
		
		//重要节点
		if(showImportantLoc)
		for(Location key:importantLocations.keySet()){
			double score = importantLocations.get(key);
			fill(255, 255,0, 50);
			 stroke(255,0,0);
			 ScreenPosition tloc = map.getScreenPosition(key);
			 int s = (int) (25*score);
			ellipse(tloc.x, tloc.y, s, s);
		}
	}
	
	//输出某一范围基站信息
	public void printLoc(){
//		weekdaysAll,mapNumberWeekdays
		ScreenPosition pos = null;
		float distance;
		int n;
		ArrayList<Location> array = null;
		Map<String,Integer> m = null;
		if(!showLoc[25]){
			array = weekdaysAllDistinct;
			m = mapNumberWeekdays;
		}
		else{
			array = weekendsAllDistinct;
			m = mapNumberWeekends;
		}
		System.out.println(mouseX+"_"+mouseY);
			for(Location loc:array){
				pos = map.getScreenPosition(loc);
				distance = pos.dist(new ScreenPosition(mouseX,mouseY));
				if(distance<=20){
					n = m.get(loc.x+"_"+loc.y+"_"+"24");	
					System.out.println(loc.x+"_"+loc.y+"_"+n);
					fill(0, 0,255, 100);
					stroke(255,255,255);
					ellipse(mouseX,mouseY, 20, 20);
				}
				
			}
					
		
	}
	private void properStationDraw(ArrayList<Location> loc,Map<String,Double> m,int time, int a, int b, int c){
		
		if(loc.size()!=0){
//			System.out.println(loc.size());
//			float[][] points = new float[loc.size()][2];
			ArrayList<Float> lat = new ArrayList<Float>();
			ArrayList<Float> lng = new ArrayList<Float>();
			
			for (Location l : loc) {

				noStroke();
				fill(a, b, c, 150);
				double n;
				if(!m.containsKey(l.x+"_"+l.y+"_"+time))
					continue;
				n = m.get(l.x+"_"+l.y+"_"+time);
				double d = 2*Math.log10(n)+2;
				ScreenPosition pos = map.getScreenPosition(l);
				if(pos.x!=0&&pos.y!=0){
					ellipse(pos.x, pos.y , (int)d, (int)d);
					lat.add(pos.x);
					lng.add(pos.y);
				}
			
			}
			int size = lat.size();
			float[][] points = new float[500][2];
			for(int i=0;i<500;i++){
				points[i][0] = lat.get(i);
				points[i][1] = lng.get(i);
				if(points[i][0]==0||points[i][1]==0)
					System.out.println("find 0");
			}
//			if(firstVorDraw){
//				myVoronoi = new Voronoi(points);
//				myRegions = myVoronoi.getRegions();
//
//				for(int i=0; i<myRegions.length; i++){
//					 noFill();
//					 stroke(255,255,255);
//					 myRegions[i].draw(this); // draw this shape
//				}	
//				firstVorDraw = false;
//			}
//			if(map.getZoomLevel()!=initZoomLevel)
//				firstVorDraw = true;
		}
		
	}
	
	private void randomDraw(ArrayList<Location> loc, int a, int b, int c) {
		for (Location l : loc) {
			ScreenPosition pos = map.getScreenPosition(l);
			noStroke();
			fill(a, b, c, 100);
			 ellipse(pos.x+ random(0, 4), pos.y + random(0, 4), 12, 12);
		}
	}
	public void mouseEvent(MouseEvent event) {
		int action = event.getAction();

		if (event.getButton() == PConstants.LEFT) {
			switch (action) {
			case MouseEvent.CLICK:
				mouseClicked();
				for (Control control : controls) {
					control.update();
				}				
//				System.out.println(mouseX+mouseY);
				printLoc();
				break;
			case MouseEvent.DRAG:
				mouseDragged();
				for (Control control : controls) {
					control.update();
				}
				break;
			case MouseEvent.MOVE:
				mouseMoved();
				break;
			default:
				break;
			}
		}
		else if (event.getButton() == PConstants.RIGHT) {
			switch (action) {
			case MouseEvent.CLICK:
				mouseClicked();
				buttonVisible = !buttonVisible;				
				break;
			case MouseEvent.DRAG:
				mouseDragged();
				buttonVisible = !buttonVisible;	
				break;
			case MouseEvent.MOVE:
				mouseMoved();
				break;
			default:
				break;
			}
		}
		if ((action == MouseEvent.DRAG) || (action == MouseEvent.MOVE)) {
			emouseX = mouseX;
			emouseY = mouseY;			
		}
		
	}
	
	public int whoIsChoosen(){
		int pos=0;
		for(int i=0;i<showLoc.length;i++){
			if(showLoc[i] && i!=25)
				pos=i;
		}
		return pos;
	}
	public void keyPressed() {
		int pos=0,nextpos=0,length = showLoc.length;  
		if (keyPressed) {			
			pos = whoIsChoosen();			
		    if (key == 'd') {
		    	nextpos = (pos+1+length)%length;
		    	if(nextpos==25) nextpos++;
		    	showLoc[nextpos] = true;
		    	showLoc[pos] = false;
		    } else if (key == 'a') {
		    	nextpos = (pos-1+length)%length;
		    	if(nextpos==25) nextpos--;
		    	showLoc[nextpos] = true;
		    	showLoc[pos] = false;
		    	
		    }
		    else if (key == 's') {
		    	showGroup = !showGroup;		    	
		    } 
		    else if (key == 'w') {
		    	showLineBetweenGroup = !showLineBetweenGroup;		    	
		    }
		    else if (key == 'q') {
		    	showPath = !showPath;		    	
		    }
		    else if (key == 'e') {
		    	showImportantLoc = !showImportantLoc;		    	
		    }
		  }
		for (Control control : controls) {
			control.update();
		}
	}
	

	
}
