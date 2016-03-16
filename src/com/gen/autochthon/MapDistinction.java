package com.gen.autochthon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class MapDistinction {

	ArrayList<String> cells = new ArrayList<String>();
	HashMap<String,String> map = new HashMap<String, String>();
	ArrayList<Double> lng = new ArrayList<Double>();//����
	ArrayList<Double> lat = new ArrayList<Double>();//γ��
	double maxLng = 0, maxLat = 0;
	double minLng = 999,minLat = 999;
	
	//ѡ����γ�ȵ������С�ĸ�ֵ
	public void searchForMost(){
		for(int i=0;i<lng.size();i++){
			double lngTemp = lng.get(i);
			double latTemp = lat.get(i);
			if(maxLng<lngTemp)
				maxLng = lngTemp;
			if(maxLat<latTemp)
				maxLat = latTemp;
			if(minLng>lngTemp)
				minLng = lngTemp;
			if(minLat>latTemp)
				minLat = latTemp;
		}
		System.out.println("maxLat: "+maxLat				
				+"\nminLat: "+minLat
				+"\nmaxLng: "+maxLng
				+"\nminLng: "+minLng);
	}
	
//��ȡ�ļ�����ֵlng,lat
	public void getTransMap() {
		File file = new File("./data/data/base_station_GPS.txt");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			try {
				String line = br.readLine();
				String[] rs = null;
				while((line = br.readLine()) != null) {
					rs = line.split("\\|");
					cells.add(rs[0] +"_"+ rs[1]);
					map.put(rs[0] +"_"+ rs[1], rs[4]+"_"+rs[5]);
					lat.add(Double.parseDouble(rs[4]));
					lng.add(Double.parseDouble(rs[5]));
				}
			} catch (IOException e) {
				e.printStackTrace();}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();}
	}

//�����л�վӳ�䵽������
//���ݵõ������ܵ㣬��ͼ��ָ�ɾ���
//500*500�ף�size��176*303
	
	public int[][] calDistinctMatrix(){
		int length = 600;
		double latSize = (maxLat-minLat)*100000/length +1;
		double lngSize = (maxLng-minLng)*100000/length +1;
		System.out.println((int)latSize+"_"+(int)lngSize);
		int[][] mapMatrix = new int[(int)latSize][(int)lngSize];
		minLat = 3111742;
		minLng = 11948693;
		for(int i=0;i<lng.size();i++){
			
			double lngTemp = lng.get(i)*100000;
			double latTemp = lat.get(i)*100000;
			int a = (int) ((latTemp-minLat)/length);
			int b = (int) ((lngTemp-minLng)/length);
			mapMatrix[a][b] ++;
		}
		int count=0;
		for(int i=0;i<(int)latSize;i++){
			for(int j=0;j<(int)lngSize;j++)
				if(mapMatrix[i][j]!=0)
					count++;
//				System.out.print(mapMatrix[i][j]+" ");
//			System.out.println();
		}
		System.out.println(count);
		return mapMatrix;
	}
	
	
	public static void main(String[] args) {
		MapDistinction m = new MapDistinction();
		m.getTransMap();
		m.searchForMost();
		m.calDistinctMatrix();
	}

}
