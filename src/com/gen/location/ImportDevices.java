package com.gen.location;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import de.fhpotsdam.unfolding.geo.Location;
//此类可被SinglePerson类代替
public class ImportDevices {

	private static ArrayList<Location> locListWeekdays,locListWeekends;
	private static int sizeWeekdays=0,sizeWeekends=0;
	private static Map<String,String> map = new HashMap<String,String>();
	private static Map<Location,Integer> mapNumberWeekends = new HashMap<Location,Integer>();//记录每个坐标出现的数量
	private static Map<Location,Integer> mapNumberWeekdays = new HashMap<Location,Integer>();//记录每个坐标出现的数量
	
	public static void init(){
		locListWeekdays = new ArrayList<Location>();locListWeekends = new ArrayList<Location>();
		sizeWeekdays=0;sizeWeekends=0;
		map = new HashMap<String,String>();
		 mapNumberWeekends = new HashMap<Location,Integer>();
		mapNumberWeekdays = new HashMap<Location,Integer>();
	}
	//计算链表中不同地址出现的次数
	public static void calMapNumber(ArrayList<PhoneRecordDAO> all,Map<Location,Integer> m){
		ArrayList<Location> list =  tranDaoToLoc(all);
		for(Location loc:list){
			if(m.containsKey(loc)){
				int number = m.get(loc);
				number++;
//				m.remove(loc);
				m.put(loc, number);
			}
			else{
				m.put(loc, 1);
			}
		}
	}
	//获取设备号
	public static ArrayList<String> getDevices() {		
		ArrayList<String> devices = new ArrayList<String>();
		BufferedReader br = null;
		try {
			File file = new File("E:/aSmartCity/数据/WuxiSmartCity/devices.txt");
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "utf-8"));
			String line = "";
			int count =0;
			while ((line = br.readLine()) != null) {
				if (line.length() < 8)
					continue;
				devices.add(line);
				count++;
			}			
			System.out.println(count);			
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
		return devices;
	}
	//得到已存入数据库中的日期
	public static ArrayList<String> getDays(){
		ArrayList<String> days = new ArrayList<String>();
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
//		System.out.println(days);
		return days;
	}
	
	public static void getCertainHour(int hour,String device) throws ParseException{
		init();
		getTransMap() ;
		//99885120582072638  //两个居住地，两个工作地
		//99436363125020112
		//99694278034672874
		//99702514941785022
		//99911000285858952
		//99911080816054974
		//99694274696390598
		//99694255103806125  夜班
//		String device = "99694255103806125";
		Gson gson = new Gson();
		ArrayList<PhoneRecordDAO> rebyhourWeekdays = new ArrayList<PhoneRecordDAO>();
		ArrayList<PhoneRecordDAO> rebyhourWeekends = new ArrayList<PhoneRecordDAO>();
//		int datehour;
		ArrayList<String> days = getDays();
		String uriAPI,formatHour="",formatHourPlus="";
		if(hour<9){
			formatHour ="0"+hour;
			formatHourPlus = "0"+(hour+1);
		}
		else if(hour==9)
		{
			formatHour ="0"+hour;
			formatHourPlus = "10";
		}
		else{
			formatHour = hour+"";
			formatHourPlus = ""+(hour+1);
		}
		for(String d :days){
			uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId="+device+"&startTime="+d+formatHour+"0000&&endTime="+d+formatHourPlus+"0000";
			String jsonString = HttpUtil.sendPost(uriAPI, "");			
			CellForm cell =  gson.fromJson(jsonString, CellForm.class);
			if(isWeekends(d))
				rebyhourWeekends.addAll(cell.getPList());
			else
				rebyhourWeekdays.addAll(cell.getPList());
		}
		calMapNumber(rebyhourWeekdays,mapNumberWeekdays);calMapNumber(rebyhourWeekends,mapNumberWeekends);
		sizeWeekdays = rebyhourWeekdays.size();
		sizeWeekends = rebyhourWeekends.size();
		System.out.println(hour+":"+sizeWeekends+"/"+sizeWeekdays);
		HashSet<PhoneRecordDAO> setWeekdays = new HashSet<PhoneRecordDAO>();
		HashSet<PhoneRecordDAO> setWeekends = new HashSet<PhoneRecordDAO>();
		setWeekdays.addAll(rebyhourWeekdays);setWeekends.addAll(rebyhourWeekends);
		ArrayList<PhoneRecordDAO> weekday = new ArrayList<PhoneRecordDAO>();weekday.addAll(setWeekdays);
		ArrayList<PhoneRecordDAO> weekend = new ArrayList<PhoneRecordDAO>();weekend.addAll(setWeekends);
		locListWeekdays = tranDaoToLoc(weekday);
		locListWeekends = tranDaoToLoc(weekend);
	}
	public static Map<Location, Integer> getMapNumberWeekends() {
		return mapNumberWeekends;
	}
	public static Map<Location, Integer> getMapNumberWeekdays() {
		return mapNumberWeekdays;
	}
	public static int getWeekdaysSize(){
		return sizeWeekdays;
	}
	public static int getWeekendsSize(){
		return sizeWeekends;
	}
	public static ArrayList<Location> getLocListWeekdays() {
		return locListWeekdays;
	}
	public static ArrayList<Location> getLocListWeekends() {
		return locListWeekends;
	}

	//将得到的PhoneRecordDAO数据变为location链表
	public static  ArrayList<Location> tranDaoToLoc(ArrayList<PhoneRecordDAO> pdao){
		ArrayList<Location> result = new ArrayList<Location>();
		ArrayList<String> list = new ArrayList<String>();
		for(PhoneRecordDAO p : pdao)
			list.add(p.getAreaID()+'_'+p.getCellID());
		
		Gson gson = new Gson();
		CellForm c = null ;
		ArrayList<String> re = new ArrayList<String>();
		for(String s :list)
		{
			if(s.equals("0_0"))
				continue;
			String lc =null;
			lc= map.get(s);
			if(lc!=null)
				re.add(lc);
//			else{
//			String[] temp = s.split("_");
//			String uri ="http://219.224.169.45:8080/GsmService/cellwuxi.action?type=addressFLL&password=mima&lac="+temp[0]+"&cell="+temp[1];
//			String string = HttpUtil.sendPost(uri, "");
//			c =  gson.fromJson(string, CellForm.class); 
//			lc = c.getList().get(0);
//			if(lc.equals("0_0"))
//				System.out.println("Dont exit:"+s);
//			else
//				re.add(lc);
//			}
			
		}
		result = transLocations(re);
		return result;
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
	public static void exportRes(List<PhoneRecordDAO> result,String name){
		File file = new File("E:/aSmartCity/数据/WuxiSmartCity/轨迹/"+name+".txt");
		 try {  
	            FileWriter fileWriter = new FileWriter(file);  
	            for(PhoneRecordDAO s:result)  
	            fileWriter.write(s.getAreaID()+'_'+s.getCellID()+"\n");  
	            System.out.println("DONE!");
	            fileWriter.close(); // 关闭数据流                
	  
	        } catch (IOException e) {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	        }  
	}
	public static void exportResString(List<String> result,String name){
		File file = new File("E:/aSmartCity/数据/WuxiSmartCity/轨迹/"+name+".txt");
		 try {  
	            FileWriter fileWriter = new FileWriter(file);  
	            for(String s:result){
	            	String[] temp = s.split("_");
	            fileWriter.write(temp[0]+'_'+temp[1]+"\n");  
	            }
	            System.out.println("DONE!");
	            fileWriter.close(); // 关闭数据流                
	  
	        } catch (IOException e) {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	        }  
	}
	
	//读取文件，获取map表，以便将stationID对照成其坐标
	public static void getTransMap() {
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
					map.put(rs[0] +"_"+ rs[1], rs[4]+"_"+rs[5]);
				}
//				System.out.println(map);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//判断是否是周末
	public static boolean isWeekends(String day) throws ParseException{
		DateFormat format1 = new SimpleDateFormat("yyyyMMdd");         
		Date bdate = format1.parse(day); 
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(bdate);
		 if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
		    	return true;
		 else 
			 return false;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		getDevices();
//		getTraceAllDay();
//		getTraceNight();
		getDays();
	}
	/*	public static ArrayList<Location> getTraceAllDay(){
	getTransMap() ;
	String device = "99523444613519162";
	String uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId="+device+"&startTime=20131111000000&&endTime=20131119000000";
	String jsonString = HttpUtil.sendPost(uriAPI, "");
//	System.out.println(jsonString);
	Gson gson = new Gson();
	CellForm cell =  gson.fromJson(jsonString, CellForm.class); 
			
	ArrayList<String> set = new ArrayList<String>();
	for(PhoneRecordDAO p : cell.getPList())
		set.add(p.getAreaID()+'_'+p.getCellID());
	
	System.out.println(set.size());
	CellForm c = null ;
	ArrayList<String> re = new ArrayList<String>();
	int count=0;
	for(String s :set)
	{
		count++;
		if(count%1000==0)
			System.out.println(count);
		
		String lc =null;
		lc= map.get(s);
		if(lc!=null)
			re.add(lc);
		else{			
		String[] temp = s.split("_");
		String uri ="http://219.224.169.45:8080/GsmService/cellwuxi.action?type=addressFLL&password=mima&lac="+temp[0]+"&cell="+temp[1];
		String string = HttpUtil.sendPost(uri, "");
		c =  gson.fromJson(string, CellForm.class); 
//		System.out.println(c.getLacCell().get(0));
		lc = c.getList().get(0);
		if(lc.equals("0_0"))
			System.out.println("Dont exit:"+s);
		else
			re.add(lc);
		}
	}
//	exportRes(cell.getList(),"99357603871889250");
//	exportResString(re,device);
	locList = transLocations(re);
	System.out.println("INPUT DONE!");
	return locList;
	}
*/
/*
public static ArrayList<Location> getTraceNight(){
	getTransMap() ;
	String device = "99357630326031986";
	Gson gson = new Gson();
	ArrayList<PhoneRecordDAO> rebynight = new ArrayList<PhoneRecordDAO>();
	for(int i=1;i<=8;i++){
		String uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId="+device+"&startTime=2013111"+i+"190000&&endTime=2013111"+(i+1)+"080000";
//		System.out.println(uriAPI);
		String jsonString = HttpUtil.sendPost(uriAPI, "");
//		System.out.println(jsonString);
		
		CellForm cell =  gson.fromJson(jsonString, CellForm.class); 
		rebynight.addAll(cell.getPList());
	}
//	HashSet<String> set = new HashSet<String>();
	ArrayList<String> set = new ArrayList<String>();
	for(PhoneRecordDAO p : rebynight)
		set.add(p.getAreaID()+'_'+p.getCellID());
	
	System.out.println(set.size());
	CellForm c = null ;
	ArrayList<String> re = new ArrayList<String>();
	int count=0;
	for(String s :set)
	{
		count++;
		if(count%1000==0)
			System.out.println(count);
		
		String lc =null;
		lc= map.get(s);
		if(lc!=null)
			re.add(lc);
		else{
		String[] temp = s.split("_");
//		System.out.println(s);
		String uri ="http://219.224.169.45:8080/GsmService/cellwuxi.action?type=addressFLL&password=mima&lac="+temp[0]+"&cell="+temp[1];
		String string = HttpUtil.sendPost(uri, "");
		c =  gson.fromJson(string, CellForm.class); 
//		System.out.println(c.getLacCell().get(0));
		lc = c.getList().get(0);
		if(lc.equals("0_0"))
			System.out.println("Dont exit:"+s);
		else
			re.add(lc);
		}
		
	}
//	exportResString(re,device+"N");
	locList = transLocations(re);
	System.out.println("INPUT DONE!");
	return locList;
}
*/
}
