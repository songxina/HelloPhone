package com.gen.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fhpotsdam.unfolding.geo.Location;


//���ļ��ж�ȡ���ݣ�ͳ��ÿСʱ��������.
//ͳ��ÿ����վ��ĳСʱ������
//��ȡ����Ҫ����豸��
public class HourDayDist {

	private static Map<String,Double> stationNumMapWeekdays = new HashMap<String,Double>();
	private static Map<String,Double> stationNumMapWeekends = new HashMap<String,Double>();
	private static Map<String,String> mapStationToCoor = new HashMap<String,String>();
	private static Set<String> cannotfind = new HashSet<String>();//��¼�Ҳ�������Ļ�վ
	public HourDayDist(){
		getTransMap();
	}
	
	public Map<String, Double> getStationNumMapWeekdays() {
		return stationNumMapWeekdays;
	}
	public Map<String, Double> getStationNumMapWeekends() {
		return stationNumMapWeekends;
	}

	//������Ӧ�ļ��е������ֲ�	
	public static void getDaysDist(String filename){
		int weekdays[] = new int[22];
		int weekends[] = new int[22];
		for(int i=0;i<22;i++){
			weekdays[i]=0;weekends[i]=0;
		}
		ArrayList<String> d =getFile(filename);
		
			for(String line:d) {
				if (line.length() < 8)
					continue;
				String temp[] = line.split("_");
				weekdays[Integer.parseInt(temp[1])]++;
				weekends[Integer.parseInt(temp[2])]++;
			}	
			System.out.println("Weekdays:");
			for(int i=0;i<22;i++)
				System.out.println(i+"_"+weekdays[i]);
			System.out.println("Weekends:");
			for(int i=0;i<9;i++)
				System.out.println(i+"_"+weekends[i]);			
		} 
	
	//����ÿ����վ������
	public static void calMapNumber(double total,String[] station , Map<String,Double> m){

		String temp[];
		String loc;
		double n,number;		
//		m.clear();
		for(int i=0;i<station.length;i++){
			String coor=null;
			 
			temp = station[i].split("-");	
			loc = temp[0]+"_"+temp[1];
//			System.out.println("stationID:"+loc);
			n = Double.parseDouble(temp[2]);
			n=n/total;
			if(loc.equals("0_0"))
				continue;
			
			coor = mapStationToCoor.get(loc);
			if(coor==null){
//				cannotfind.add(temp[0]+","+temp[1]);
//				System.out.println(loc);
				continue;
			}		
			String t[] = coor.split("_");
			Location location = new Location(Float.parseFloat(t[0]),Float.parseFloat(t[1]));
			coor = location.x+"_"+location.y;

			if(m.containsKey(coor)){
					number = m.get(coor);
					number+=n;
					m.remove(coor);
					m.put(coor, number);
				}
				else{
					m.put(coor, n);
				}			
		}						
	}
	
	//��ȡ�����ճ��ֵ��豸�ţ����������station
	public void calStationNumber(int hour){
		ArrayList<String> data = getFile("E:/aSmartCity/����/WuxiSmartCity/PeopleLocByHour/"+hour+".txt");
		String[] temp,daytemp,endtemp;
		stationNumMapWeekdays.clear();
		stationNumMapWeekends.clear();
		for(String line :data){
			temp = line.split("_");
			if(!(temp[1].equals("0")||temp[2].equals("0"))){
				daytemp = temp[4].split("\\|");
				endtemp = temp[6].split("\\|");				
				calMapNumber(Double.parseDouble(temp[3]),daytemp,stationNumMapWeekdays);
				calMapNumber(Double.parseDouble(temp[5]),endtemp,stationNumMapWeekends);
			}
		}	
//		ArrayList a = new ArrayList();
//		for(String s:cannotfind)
//			a.add(s);
//		exportResString(a,"E:/aSmartCity/����/WuxiSmartCity/PeopleLocByHour/cannotfind.txt");
//		System.out.println(cannotfind);
	}
	
	//��ȡ�ļ�
	public static ArrayList<String> getFile(String filename){
		
		ArrayList<String> data = new ArrayList<String>();
		BufferedReader br = null;
		try {
			File file = new File(filename);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "utf-8"));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.length() < 8)
					continue;
				data.add(line);	
//				System.out.println(count);				
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
		return data;
	}
	//������ļ�
	public static void exportResString(List<String> result,String name){
		File file = new File(name);
		 try {  
	            FileWriter fileWriter = new FileWriter(file);  
	            for(String s:result){
	            fileWriter.write(s+"\r\n");  
	            }
	            System.out.println("DONE!");
	            fileWriter.close(); // �ر�������                
	  
	        } catch (IOException e) {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	        }  
	}
	
	//��ȡ�ļ�����ȡmap���Ա㽫stationID���ճ�������
	public static void getTransMap() {
			File file = new File("./data/data/base_station_GPS.txt");
			try {
				BufferedReader br = new BufferedReader(	new InputStreamReader(	new FileInputStream(file), "utf-8")	);
				try {
					String line = br.readLine();
					String[] rs = null;
					while((line = br.readLine()) != null) {
						rs = line.split("\\|");
						mapStationToCoor.put(rs[0] +"_"+ rs[1], rs[4]+"_"+rs[5]);
					}
//					System.out.println(mapStationToCoor);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for(int i=22;i<=23;i++){
			String f ="E:/aSmartCity/����/WuxiSmartCity/PeopleLocByHour/"+i+".txt";
		getDaysDist(f);
		System.out.println();
		}
//		HourDayDist	h = new HourDayDist();
//		h.calStationNumber(22);
//		for(String k:stationNumMapWeekdays.keySet())
//		System.out.println(k+":"+stationNumMapWeekdays.get(k));
//		System.out.println(stationNumMapWeekdays.size());
	}

}
