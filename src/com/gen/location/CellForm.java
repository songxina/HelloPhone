package com.gen.location;
import java.util.ArrayList;

public class CellForm {

	private ArrayList<PhoneRecordDAO> plist = new ArrayList<PhoneRecordDAO>();
	private ArrayList<String> list = new ArrayList<String>();
	

	public ArrayList<String> getList() {
		return list;
	}
	public void setLacCell(ArrayList<String> list) {
		this.list = list;
	}
	public ArrayList<PhoneRecordDAO> getPList() {
		return plist;
	}
	public void setList(ArrayList<PhoneRecordDAO> plist) {
		this.plist = plist;
	}

}
