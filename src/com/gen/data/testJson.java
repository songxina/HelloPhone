package com.gen.data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class testJson {

	
	public static ArrayList<PhoneRecordDAO> getJson(){
	
		
//	String uriAPI ="http://219.224.169.45:8080/GsmService/cellwuxi.action?password=mima&type=numByDay&lac=20512&cell=797&day=20131111";
//		String uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findByRan&password=mima&startTime=20131205000000&&endTime=20131205010000";
	
		String uriAPI ="http://219.224.169.45:8080/GsmService/phonewuxi.action?type=findById&password=mima&deviceId=99070818249470658&startTime=20131211000000&&endTime=20131212000000";
		String jsonString = HttpUtil.sendPost(uriAPI, "");
	Gson gson = new Gson();
	
    CellForm cell =  gson.fromJson(jsonString, CellForm.class);  

//	 for (PhoneRecordDAO temp:cell.getPList()) {
//			System.out.println(temp.getTime());
//		}

	return  cell.getPList();
	}
	
	public static String getPhoneHome() throws UnsupportedEncodingException{
//		String uriAPI ="http://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=13812120000";
		String uriAPI ="http://virtual.paipai.com/extinfo/GetMobileProductInfo?mobile=13812120000&amount=1000";
//		System.out.println(SendGET(uriAPI));
//		String jsonString = HttpUtil.sendPost(uriAPI, "");
		String jsonString =SendGET(uriAPI);
		Gson gson = new Gson();
//		System.out.println(jsonString);
		String city = getCity(jsonString);
//System.out.println(city);
		return  city;
		}
	
	public static String getCity(String str){
		String city; 
        int m=0,n=0; 
        for(int i=0;i<str.length();i++){ 
            if(str.charAt(i)=='{')
                    m=i;                     
            if(str.charAt(i)=='}')
                	n=i; 
             } 
        String temp = str.substring(m,n+1);        
//        System.out.println(temp);
        String[] s = temp.split(",");
        String cityString  = s[7];
        city = cityString.substring(10, cityString.length()-2);
        return city;
	}
	
	public static String SendGET(String url){
		   String result="";//���ʷ��ؽ��
		   BufferedReader read=null;//��ȡ���ʽ��
		    
		   try {
		    //����url
		    URL realurl=new URL(url);
		    //������
		    URLConnection connection=realurl.openConnection();
		     // ����ͨ�õ���������
		             connection.setRequestProperty("accept", "*/*");
		             connection.setRequestProperty("connection", "Keep-Alive");
		             connection.setRequestProperty("user-agent",
		                     "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		             //��������
		             connection.connect();
		          // ��ȡ������Ӧͷ�ֶ�
		             Map<String, List<String>> map = connection.getHeaderFields();
		             // �������е���Ӧͷ�ֶΣ���ȡ��cookies��
//		             for (String key : map.keySet()) {
//		                 System.out.println(key + "--->" + map.get(key));
//		             }
		             // ���� BufferedReader����������ȡURL����Ӧ
		             read = new BufferedReader(new InputStreamReader(
		                     connection.getInputStream(),"GBK"));
		             String line;//ѭ����ȡ
		             while ((line = read.readLine()) != null) {
		                 result += line;
		             }
		   } catch (IOException e) {
		    e.printStackTrace();
		   }finally{
		    if(read!=null){//�ر���
		     try {
		      read.close();
		     } catch (IOException e) {
		      e.printStackTrace();
		     }
		    }
		   }
		     
		   return result; 
		 }
	
	public static void exportRes(List<PhoneRecordDAO> result,String fileName){
		File file = new File(fileName);
		 try {  
	            FileWriter fileWriter = new FileWriter(file);  
	            for(PhoneRecordDAO s:result)  
	            fileWriter.write(s.getDeviceID()+'_'+s.getTime()+'_'+s.getTag()+'_'+s.getAreaID()+'_'+s.getCellID()+"\n");  
	            System.out.println("DONE!");
	            fileWriter.close(); // �ر�������                
	  System.out.println("DONE!");
	        } catch (IOException e) {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	        }  
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

//		getPhoneHome();
//		String fileName = "E:/aSmartCity/����/deviceInfo_10000_20days.txt";
		List<PhoneRecordDAO> result = getJson();
		for(int i=0;i<result.size();i++)
			System.out.println(result.get(i).getTime());
//		exportRes(result,fileName);
	}
}
