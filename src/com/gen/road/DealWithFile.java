package com.gen.road;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fhpotsdam.unfolding.geo.Location;

public class DealWithFile {

	//读取文件
		public static ArrayList<String> getFile(String filename){
			System.out.print(filename+"  ");
			ArrayList<String> data = new ArrayList<String>();
			BufferedReader br = null;
			int count=0;
			try {
				File file = new File(filename);
				br = new BufferedReader(new InputStreamReader(new FileInputStream(
						file), "GB2312"));
				String line = "";
				while ((line = br.readLine()) != null) {
					if (line.length() < 8)
						continue;
					data.add(line);	
					count++;
//					if(count%1000==0)
//					System.out.println(count);				
				}				
				System.out.println(count+" Input Done!");		
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
		//输出到文件
		public static void exportResString(List<String> result,String name){
			File file = new File(name);
			 try {  
		            FileWriter fileWriter = new FileWriter(file);  
		            for(String s:result){
		            	fileWriter.write(s+"\r\n");  
		            }
		            System.out.println("OUTPUT DONE!");
		            fileWriter.close(); // 关闭数据流                
		  
		        } catch (IOException e) {  
		            // TODO Auto-generated catch block  
		            e.printStackTrace();  
		        }  
		}
		//输出到文件,已存在文件时，从文件尾继续写
				public static void exportResStringTrue(List<String> result,String name){
					File file = new File(name);
					 try {  
				            FileWriter fileWriter = new FileWriter(file,true);  
				            for(String s:result){
				            	fileWriter.write(s+"\r\n");  
				            }
				            System.out.println("OUTPUT DONE!");
				            fileWriter.close(); // 关闭数据流                
				  
				        } catch (IOException e) {  
				            // TODO Auto-generated catch block  
				            e.printStackTrace();  
				        }  
				}
	
		public void changFileStructure(){
			ArrayList<String> data =getFile("E:/aSmartCity/数据/WuxiSmartCity/devices.txt");
			String line[];
			ArrayList<String>  newdata = new ArrayList<String>();
			int count=1;
			for(String s :data){
				newdata.add(s);
				if(count%1000==0)
				System.out.println(count);
				if(count>200000)
					break;
				count++;
			}
			
			 exportResString(newdata,"E:/aSmartCity/数据/WuxiSmartCity/devices_200000.txt");
		}
		
		public void calDistinctNum(){
			String temp[];
			Set<String> diffDevice = new HashSet<String>(); 
			BufferedReader br = null;;
			try {
				File file = new File("E:/aSmartCity/数据/WuxiSmartCity/deviceInfo10000_20.txt");
				br = new BufferedReader(new InputStreamReader(new FileInputStream(
						file), "GB2312"));
				String line = "";
				while ((line = br.readLine()) != null) {
					if (line.length() < 8)
						continue;
					temp = line.split("_");
					diffDevice.add(temp[0]);
					if(diffDevice.size()%100==0)
					System.out.println(diffDevice.size());				
				}				
				System.out.println("Input Done!");		
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
			
			
		}
	//读取文件，获取map表，以便将stationID对照成其坐标
	public ArrayList<Location>  getAllStationPosition(ArrayList<Location> allStation) {
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
					Location location = new Location(Double.parseDouble(rs[4]),Double.parseDouble(rs[5]));
					allStation.add(location);
//							map.put(rs[0] +"_"+ rs[1], rs[4]+"_"+rs[5]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allStation;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DealWithFile d = new DealWithFile();
//		d.changFileStructure();
		d.calDistinctNum();
	}

}
