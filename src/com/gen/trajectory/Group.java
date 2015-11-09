package com.gen.trajectory;

import java.util.ArrayList;

import de.fhpotsdam.unfolding.geo.Location;

public class Group {

	ArrayList<String> cellGroup = new ArrayList<String>();//��վ��
	ArrayList<Location> cellLocationGroup = new ArrayList<Location>();//��վ�����	
	double possibility = 0;
	String gourpID;//hour+idCount
	String centerCellLocaion;
	public double startValue = 0;//������groupΪ��ʼgroup�ĸ���
	public double endValue = 0;//������groupΪ��ֹgroup�ĸ���
	
	//��ѯgroup�Ƿ����cell
	public boolean hasCell(String cell){
		return cellGroup.contains(cell);
	}
	//���ص�һ��cell�������ĵ�
	public String getCenterCell(){
		return cellGroup.get(0);
	}
	
	//���ص�һ��cell�������ĵ������
	public Location getCenterLocation(){
		return cellLocationGroup.get(0);
	}
	public int getIDHour(){
		String temp[] = gourpID.split("_");
		return Integer.parseInt(temp[0]);
	}
	public int getIDIndex(){
		String temp[] = gourpID.split("_");
		return Integer.parseInt(temp[1]);
	}
	//���cell
	public void add(String cell) {
		cellGroup.add(cell);
	}

	public ArrayList<String> getCellGroup() {
		return cellGroup;
	}

	public void setCellGroup(ArrayList<String> cellGroup) {
		this.cellGroup = cellGroup;
	}

	public double getPossibility() {
		return possibility;
	}

	public void setPossibility(double possibility) {
		this.possibility = possibility;
	}

	public String getGourpID() {
		return gourpID;
	}

	public void setGourpID(String gourpID) {
		this.gourpID = gourpID;
	}

	public ArrayList<Location> getCellLocationGroup() {
		return cellLocationGroup;
	}
	public void setCellLocationGroup(ArrayList<Location> cellLocationGroup) {
		this.cellLocationGroup = cellLocationGroup;
	}

	
}
