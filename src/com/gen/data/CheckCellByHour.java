package com.gen.data;

import java.util.ArrayList;

import com.gen.location.CellForm;
import com.gen.location.HttpUtil;
import com.google.gson.Gson;

public class CheckCellByHour {

	public static void getNum(){
		
		String lac = "20525";
		String cell = "10427";
		String uriAPI =null;
		ArrayList<String> list = new ArrayList<String>();
		for(int i=1;i<8;i++){
		uriAPI="http://219.224.169.45:8080/GsmService/cellwuxi.action?password=mima&type=numByHour&lac="+lac+"&cell="+cell+"&hour=2013111"+i+"10";
		String jsonString = HttpUtil.sendPost(uriAPI, "");
//		System.out.println(jsonString);
		Gson gson = new Gson();
		CellForm cellform =  gson.fromJson(jsonString, CellForm.class); 
				

		for(String p : cellform.getList()){
			list.add(p.split("\\|")[2]);
		}
		
		}
		System.out.print(lac+"|"+cell+"|");
		System.out.println(list);
		}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getNum();
	}

}
