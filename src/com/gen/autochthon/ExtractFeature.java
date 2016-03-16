package com.gen.autochthon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.gen.locAndTrajectory.Device;
import com.gen.road.DealWithFile;

public class ExtractFeature {

	ArrayList<User> autochthonU = new ArrayList<User>();
	ArrayList<User> unAutochthonU = new ArrayList<User>();
	ArrayList<User> allUsers = new ArrayList<User>();

	ArrayList<String> autochthonHome = new ArrayList<String>();
	HashMap<String,Double> autochthonHomeNum = new HashMap<String, Double>();
	ArrayList<String> autochthonWork = new ArrayList<String>();
	HashMap<String,Double> autochthonWorkNum = new HashMap<String, Double>();
	ArrayList<String> unAutochthonHome = new ArrayList<String>();
	HashMap<String,Double> unAutochthonHomeNum = new HashMap<String, Double>();
	ArrayList<String> unAutochthonWork = new ArrayList<String>();
	HashMap<String,Double> unAutochthonWorkNum = new HashMap<String, Double>();
	
	HashMap<String,Double> autochthonAreaNum = new HashMap<String, Double>();
	HashMap<String,Double> unAutochthonAreaNum = new HashMap<String, Double>();
	
	HashMap<String, Integer> indexMap = new HashMap<String, Integer>();//基站序号
	HashMap<String, String> coorMap = new HashMap<String, String>();//基站坐标
	
	//获取每个设备的所有信息，包括统计信息
	public void outputInfo(){
		ArrayList<String> devices = DealWithFile.getFile("E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/goodDevices2.txt");
		ArrayList<String> info = new ArrayList<String>();
		try {
			int count=0;
			for(String deviceID:devices){
				if(count>=22500){
					if(count==22517)
						continue;
					Device device = new Device(deviceID);
					if(device.cellGroup.size()==0)
						continue;
					String line = device.printSpace();
					info.add(line);
					System.out.println(count+"/"+devices.size());
					if(count%100==0){
						DealWithFile.exportResStringTrue(info, "E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/goodDevices2Info.txt");
						info.clear();
//						System.out.println(count+"/"+devices.size()+"--------------------------------------------------------------------------------------");
					}
				}
				count++;
			}
			DealWithFile.exportResStringTrue(info, "E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/goodDevices2Info.txt");
		} catch (Exception e) {	e.printStackTrace();}
	}
	//获取所有用户信息，赋值autochthonU,unAutochthonU
	public void fetchInfo(){
		getTransMap(indexMap,coorMap);
		ArrayList<String> autochthon = DealWithFile.getFile("E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/autochthons2.txt");
		ArrayList<String> unAutochthon = DealWithFile.getFile("E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/unautochthons2.txt");
		ArrayList<String> info = DealWithFile.getFile("E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/goodDevices2Info.txt");
		for(String line:info){
			String temp[] = line.split(",");
			String id = temp[0];
			User u = new User(id);
			u.fillInfo(line);
			u.sort();//按照出现次数，对基站排序
			allUsers.add(u);
			if(autochthon.contains(id)){
				u.type = 1;
				autochthonU.add(u);
			}
			if(unAutochthon.contains(id)){
				u.type = 0;
				unAutochthonU.add(u);
			}
		}
		
	}
	//计算工作地、居住地基站数量
	public void statisticInfo(){
		for(User u:autochthonU){
			autochthonHome.add(u.getHomeCellID());
			addMap(autochthonHomeNum, u.getHomeCellID(),1);
			autochthonWork.add(u.getWorkCellID());
			addMap(autochthonWorkNum, u.getWorkCellID(),1);
			for(String cell:u.getCellNum().keySet())
				addMap(autochthonAreaNum, cell, u.getCellNum().get(cell));
		}
		for(User u:unAutochthonU){
			unAutochthonHome.add(u.getHomeCellID());
			addMap(unAutochthonHomeNum, u.getHomeCellID(),1);
			unAutochthonWork.add(u.getWorkCellID());
			addMap(unAutochthonWorkNum, u.getWorkCellID(),1);
			for(String cell:u.getCellNum().keySet())
				addMap(unAutochthonAreaNum, cell, u.getCellNum().get(cell));
		}
	}
	
//计算数量
	public void addMap(HashMap<String,Double> map,String s,double value){
		if(map.containsKey(s)){
			double c = map.get(s);
			c+=value;
			map.remove(s);
			map.put(s, c);				
		}
		else
			map.put(s, value);
	}
	
//读取文件，获取基站序号
	public void getTransMap(HashMap<String, Integer> indexMap,HashMap<String, String> coorMap) {
		File file = new File("./data/data/base_station_GPS.txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			try {
				int index = 1;
				String line = br.readLine();
				String[] rs = null;
				while((line = br.readLine()) != null) {
					rs = line.split("\\|");					
					coorMap.put(rs[0] +"_"+ rs[1], rs[4]+"_"+rs[5]);
					indexMap.put(rs[0] +"_"+ rs[1],index);
					index++;
				}
			} catch (IOException e) {
				e.printStackTrace();}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();}
	}
	
   //产生文件格式：基站序号（index），经度-119，纬度-31, y(0:unnative  1:native)
	public void readyForTheInput(){
				
		DecimalFormat df = new DecimalFormat("######0.00000");   
		ArrayList<String> info =  new ArrayList<String>();
		for(User user:allUsers){			
			String line = user.type+",";
			for(int i=0;i<user.differentCell.size();i++){
				String cell = user.differentCell.get(i);
				if(!coorMap.keySet().contains(cell))
					continue;
				String latLng[] = coorMap.get(cell).split("_");
				if(i!=0)
					line+=",";
				line+= (double)indexMap.get(cell)/10000+","+df.format(Double.parseDouble(latLng[0])-31)+","+df.format(Double.parseDouble(latLng[1])-119)+","+(double)user.getCellNum().get(cell)/100;				
			}
			info.add(line);
//			System.out.println(line);
		}
		DealWithFile.exportResString(info, "E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/goodDevices2RandomOrder.txt");
	}
	
//统计参数
	public void analysis(){
		ArrayList<String> infoNative =  new ArrayList<String>();//所有人出现的基站数量
		ArrayList<String> infoUnNative =  new ArrayList<String>();//所有人出现的基站数量
		int nativeN[] = new int[500];
		int unNativeN[] = new int[500];
		for(User user:autochthonU){
			infoNative.add((user.differentCell.size()/10)*10+"");
//			nativeN[user.differentCell.size()/10]++;
			int sum = 0;
			for(String c:user.cellNum.keySet()){
				sum+=user.cellNum.get(c);
			}
			nativeN[sum/10]++;
		}
		for(User user:unAutochthonU){
			infoUnNative.add((user.differentCell.size()/10)*10+"");
//			unNativeN[user.differentCell.size()/10]++;
			int sum = 0;
			for(String c:user.cellNum.keySet()){
				sum+=user.cellNum.get(c);
			}
			unNativeN[sum/10]++;
		}
		infoNative.clear();
		for(int n:nativeN)
			infoNative.add(n+"");
		infoUnNative.clear();
		for(int n:unNativeN)
			infoUnNative.add(n+"");
		DealWithFile.exportResString(infoNative,  "E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/infoNative.txt");
		DealWithFile.exportResString(infoUnNative,  "E:/aSmartCity/DataWorkSpace/HelloTest/data/data/devices/infoUnNative.txt");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExtractFeature extractFeature = new ExtractFeature();
		extractFeature.fetchInfo();
//		extractFeature.outputInfo();
		extractFeature.readyForTheInput();
//		extractFeature.analysis();
	}

}
