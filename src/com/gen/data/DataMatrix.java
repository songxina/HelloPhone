package com.gen.data;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gen.location.CellForm;
import com.gen.location.HttpUtil;
import com.gen.location.PhoneRecordDAO;
import com.google.gson.Gson;

import de.fhpotsdam.unfolding.geo.Location;


	//����Ϊ��λ����������ڣ�24Сʱ�����Գ��ֵĲ�ͬ��վ�������������
public class DataMatrix {

	private String dayID;
	private String deviceID;
	private ArrayList<Location> stationsByHour[];  //ÿ��Сʱ���ֵ����в�ͬ��վ
	private Map<Location,Integer> stationNumByHour[];   //ÿСʱ��ÿ����վ���ֵĴ���
	private ArrayList<Location> stationsByDay;     //ȫ����ֵ����в�ͬ��վ
	private Map<Location,Integer> stationNumByDay;   //24Сʱ��ÿ����վ���ֵ��ܴ���
	private Map<String,String> map = new HashMap<String,String>();//stationתlocation
	private Map<Location,Integer> stationHourNum;//ȫ��ÿ����վ���ֵ�Сʱ��Ŀ
	private int allRecordNum = 0; //���������м�¼������


	public DataMatrix(String dayID,String deviceID){
		this.dayID = dayID;
		this.deviceID = deviceID;
		stationsByHour = new ArrayList[24];
		stationNumByHour = new HashMap[24];
		for(int i=0;i<24;i++){
			stationsByHour[i] = new ArrayList<Location>();
			stationNumByHour[i] = new HashMap<Location,Integer>();
		}
		stationHourNum = new HashMap<Location,Integer>();
		stationsByDay = new ArrayList();
		stationNumByDay = new HashMap();
	}
	
	public void setData() {
		getTransMap() ;
		Gson gson = new Gson();
		ArrayList<PhoneRecordDAO> pDaos = new ArrayList<PhoneRecordDAO>();
		String uriAPI,formatHour="",formatHourPlus="";
	//��ÿ��Сʱ���ֱ�ȡ���ݣ���������stationsByHour��stationNumByHour��ֵ
		int hour=0;
		for(;hour<24;hour++){
			
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
			
			uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId="+deviceID+"&startTime="+dayID+formatHour+"0000&&endTime="+dayID+formatHourPlus+"0000";
			String jsonString = HttpUtil.sendPost(uriAPI, "");			
			CellForm cell =  gson.fromJson(jsonString, CellForm.class);
			pDaos.addAll(cell.getPList());
			
			calMapNumber(pDaos,stationNumByHour[hour]);
			for(Location loc : stationNumByHour[hour].keySet()){
				stationsByHour[hour].add(loc);
			}
			
		}
	//ͳ��һ���������
		Set<Location> locs = new HashSet<Location>();
		for(int i=0;i<24;i++){
			for(Location l:stationsByHour[i])
				locs.add(l);
			//����ȫ���map
			for(Location key:stationNumByHour[i].keySet()){
				int n = stationNumByHour[i].get(key);
				if(stationNumByDay.containsKey(key)){
					int number = stationNumByDay.get(key);
					number+=n;
					stationNumByDay.remove(key);
					stationNumByDay.put(key, number);
				}
				else{
					stationNumByDay.put(key, n);
				}
			}
		}
		for(Location l:locs)
			stationsByDay.add(l);
		//���м�¼����
		for(Location loc:stationsByDay)
			allRecordNum += stationNumByDay.get(loc);
//		print();
	}
	//���
	public void print(){
	
		for(int i=0;i<24;i++){
			for(Location p:stationsByHour[i])
				System.out.println(p.x+"_"+p.y);
			for(Location key:stationNumByHour[i].keySet())
				System.out.println(key.x+"_"+key.y+"_"+stationNumByHour[i].get(key));
		}
		System.out.println("allday");
		for(Location l:stationsByDay)
			System.out.println(l.x+"_"+l.y);
		for(Location key:stationNumByDay.keySet())
			System.out.println(key.x+"_"+key.y+"_"+stationNumByDay.get(key));
		for(Location key:stationHourNum.keySet())
			System.out.println(key.x+"_"+key.y+"_"+stationHourNum.get(key));
	}
	
	public void setStationHourNumber(){
		for(Location key:stationNumByDay.keySet()){
			stationHourNum.put(key, 0);
		}
		for(int i=0;i<24;i++){
			for(Location loc:stationsByHour[i]){
				int n = stationHourNum.get(loc);
				n++;
				stationHourNum.remove(loc);
				stationHourNum.put(loc, n);
			}
		}
	}
	
	//���������в�ͬ��ַ���ֵĴ���
	public void calMapNumber(ArrayList<PhoneRecordDAO> all,Map<Location,Integer> m){
		ArrayList<Location> list =  tranDaoToLoc(all);
		for(Location loc:list){
			if(m.containsKey(loc)){
				int number = m.get(loc);
				number++;
				m.remove(loc);
				m.put(loc, number);
			}
			else{
				m.put(loc, 1);
			}
		}
	}
	
	//���õ���PhoneRecordDAO���ݱ�Ϊlocation����
	public  ArrayList<Location> tranDaoToLoc(ArrayList<PhoneRecordDAO> pdao){
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
	
	//��stringת����location
	public  ArrayList<Location> transLocations(ArrayList<String> list){
		ArrayList<Location> loc = new ArrayList<Location>();
		String temp[];
		for(String lc :list){
			temp = lc.split("_");
			Location a = new Location(Float.parseFloat(temp[0]),Float.parseFloat(temp[1]));
			loc.add(a);
		}
		return loc;
	}
	
	
	//��ȡ�ļ�����ȡmap���Ա㽫stationID���ճ�������
		public  void getTransMap() {
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
		
		
		public  String getDayID() {
			return dayID;
		}

		public  void setDayID(String dayID) {
			this.dayID = dayID;
		}

		public String getDeviceID() {
			return deviceID;
		}

		public void setDeviceID(String deviceID) {
			this.deviceID = deviceID;
		}

		public ArrayList<Location>[] getStationsByHour() {
			return stationsByHour;
		}

		public Map<Location, Integer>[] getStationNumByHour() {
			return stationNumByHour;
		}

		public ArrayList<Location> getStationsByDay() {
			return stationsByDay;
		}

		public Map<Location, Integer> getStationNumByDay() {
			return stationNumByDay;
		}

		public int getAllRecordNum() {
			return allRecordNum;
		}

		public Map<Location, Integer> getStationHourNum() {
			return stationHourNum;
		}

		public static void main(String[] args) throws ParseException {
			
			DataMatrix d = new DataMatrix("20131220","99702511931846659");
			d.setData();
			d.getStationHourNum();
			d.print();
		}
}
