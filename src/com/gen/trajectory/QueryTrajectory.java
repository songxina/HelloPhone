package com.gen.trajectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Map;

import com.gen.location.CellForm;
import com.gen.location.HttpUtil;
import com.gen.location.PhoneRecordDAO;
import com.google.gson.Gson;

import de.fhpotsdam.unfolding.geo.Location;

//查询并记录存入的所有天数中，每小时该设备的轨迹 
public class QueryTrajectory {
	
	private String deviceID;
	int dayNum=24;
	//目前存的数据是20131205-20131225 共21天
	private ArrayList<Location> locAll[][] = new ArrayList[dayNum][25];
	private ArrayList<String> cellAll[][] = new ArrayList[dayNum][25];//
	private static Map<String,String> map = new HashMap<String,String>();//将stationID对照成其坐标
	HashSet<String> differentCell = new HashSet<String>();
	
	
	public static Map<String, String> getMap() {
		return map;
	}

	public ArrayList<String>[][] getCellAll() {
		return cellAll;
	}

	public HashSet<String> getDifferentCell() {
		return differentCell;
	}

	public QueryTrajectory(String deviceID){
		this.deviceID = deviceID;
		for(int i=0;i<dayNum;i++)
			for(int j=0;j<25;j++){
				locAll[i][j] = new ArrayList();
				cellAll[i][j] = new ArrayList();
			}
				
	}
	
	//得到dayNum天当中，每天24小时的轨迹，25为全天所有轨迹
	public ArrayList<Location>[][] getAllTrajectory() throws ParseException{
		getTransMap() ;
		Gson gson = new Gson();
		ArrayList<PhoneRecordDAO> phoneRecordDao = new ArrayList<PhoneRecordDAO>();
		ArrayList<String> days = getDays();
		String uriAPI,formatHour="",formatHourPlus="";
		int hour=0;
		//hour=25时表示全天的轨迹序列
	for(;hour<25;hour++){
		System.out.println("----"+hour);
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
		int count=0;
		for(String d :days){
//			if(!isWeekends(d)){
				if(hour==24)
					uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId="+deviceID+"&startTime="+d+"000000&&endTime="+d+"240000";
				else
					uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId="+deviceID+"&startTime="+d+formatHour+"0000&&endTime="+d+formatHourPlus+"0000";
					
					String jsonString = HttpUtil.sendPost(uriAPI, "");			
					CellForm cell =  gson.fromJson(jsonString, CellForm.class);
					phoneRecordDao.clear();
					phoneRecordDao.addAll(cell.getPList());
					//去掉没有记录location的cell
					phoneRecordDao = weDontKnowWhere(phoneRecordDao);
					locAll[count][hour].addAll(tranDaoToLoc(phoneRecordDao));	
					
					ArrayList<String> temp = tranDaoToString(phoneRecordDao);
					differentCell.addAll(temp);
					cellAll[count][hour].addAll(temp);	
					count++;
//					if(count==1)
//						for(PhoneRecordDAO p : phoneRecordDao)
//							System.out.println(p.getAreaID()+'_'+p.getCellID()+"_"+p.getTime());
			}
		
//		}	
	}
	return locAll;
	//print
//		String out="";
//		for(int i=0;i<1;i++){
//			out = "";
//			System.out.println("Day:"+days.get(i));
//			for(int j=0;j<10;j++){
//				System.out.println("Hour:"+j);
//				for(Location l:locAll[i][j])
//					out += l.x+"_"+l.y+" ";
//				System.out.println(out);
//			}
//		}
		
	}
	//去掉没有记录location的cell
	public ArrayList<PhoneRecordDAO> weDontKnowWhere(ArrayList<PhoneRecordDAO> phoneRecordDao){
		String key="";
		ArrayList<PhoneRecordDAO> result = new ArrayList<PhoneRecordDAO>();
		for(PhoneRecordDAO p : phoneRecordDao){
			key = p.getAreaID()+'_'+p.getCellID();
			if(map.containsKey(key)){
				result.add(p);
			}
		}
		return result;
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
//					System.out.println(map);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
//			System.out.println(days);
			return days;
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
				
			}
			result = transLocations(re);
			return result;
		}
		
		//将得到的PhoneRecordDAO数据变为location链表
	public static  ArrayList<String> tranDaoToString(ArrayList<PhoneRecordDAO> pdao){
		ArrayList<String> result = new ArrayList<String>();
		for(PhoneRecordDAO p : pdao)
			result.add(p.getAreaID()+'_'+p.getCellID());

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
	public static void main(String[] args) throws ParseException {
		QueryTrajectory q  = new QueryTrajectory("99911000285858952");
		q.getAllTrajectory();
	}

}
