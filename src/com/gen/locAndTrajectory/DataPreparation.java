package com.gen.locAndTrajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.gen.data.DataMatrix;
import com.gen.location.PhoneRecordDAO;
import com.gen.trajectory.Group;

/*
 * 1.��ȡhome��work֮��ľ�������
 * 2.������ൽ����ת�ƾ���
 * 3.�����վ����վת�ƾ���
 * 
 * 4.����home��work���������У����ֵ����в�ͬ���ࡣ
 * */

public class DataPreparation {

	public ArrayList<Group> cellGroup = new ArrayList<Group>();//��¼���豸���ֹ������е�ľ���
	ArrayList<String> 	cellAll[][];//Ŀǰ���������20131205-20131231 ��27��
	ArrayList<Integer> cellTimeAll[][];//ÿ����վ��Ӧ��ʱ��
	Device device ;
	String homeGroupID = "";
	String workGroupID = "";
	int daySize ;
	
	HashMap<String, String> mapCellToGroupID = new HashMap<String, String>();//��¼ÿ����վ��Ӧ�ľ���
	ArrayList<ArrayList<String>> groupIDFromHomeToWork ;//��¼ÿ��home��work֮��ľ�������
	int[][] groupTransferMatrix;//��¼���ൽ����ת�ƾ���
	
	
	
	public DataPreparation(Device device){
		this.device = device;
		cellGroup = device.getCellGroup();
		cellAll = device.getCellAll();
		cellTimeAll = device.getCellTimeAll();
		homeGroupID = device.homeGroupID;
		workGroupID = device.workGroupID;
		daySize = device.daySize;
	}
	
	
	
	/*
	 * ��¼ÿ����վ��Ӧ�ľ���
	 */
	public void fillMapCellToGroupID(){
		for(Group g:cellGroup){
			String gid = g.getGourpID();
			for(String cell:g.getCellGroup())
				mapCellToGroupID.put(cell, gid);
		}
	}
	/*
	 * ��վ���б�ΪgroupID����
	 */
	public ArrayList<String> cellToGroupIDList(ArrayList<String> cellList){
		ArrayList<String> groupidList = new ArrayList<String>();
		for(String cell:cellList)
			groupidList.add(mapCellToGroupID.get(cell));
		return groupidList;
	}
	/*
	 * ��ȡÿ��home��work֮��ľ�������
	 * �޸�ÿ��Group�е�ʱ�䡣ShowRoad���е�allGroup���Ա��࣬��ˣ�ֻҪ�޸ı����е�allGroup��Group����
	 */
	public ArrayList<ArrayList<String>> extractTrajectoryFromHomeToWork(){
		groupIDFromHomeToWork = new ArrayList<ArrayList<String>>();
		fillMapCellToGroupID();
		for(int day=0;day<daySize;day++){
			int endIndex = 0;
			//�õ�ȫ�����ת������
			ArrayList<String> groupidList = cellToGroupIDList(cellAll[day][24]);
//			System.out.println("groupidList:"+groupidList);
			ArrayList<String> subList =new ArrayList<String>();
			subList.add(homeGroupID);
			if(groupidList.contains(homeGroupID) && groupidList.contains(workGroupID)){
				//�ҵ���ʼλ��
				int begin = 0;
				for(String id:groupidList){
					if(id.equals(homeGroupID))
						break;
					begin++;
				}
				//��һ������idҪ��ǰһ��id��ͬ��ȥ�������ظ���
				for(int i=begin;i<groupidList.size();i++){
					String id = groupidList.get(i);
					if(!subList.get(subList.size()-1).equals(id))
						subList.add(id);
					if(id.equals(workGroupID)){
						endIndex = i;
						break;	
					}
				}
			}
			//��sublist��һ�´���ȡ���һ��homegroupID��workGroupID֮�������
			ArrayList<String> subListTemp =new ArrayList<String>();
			int subSize = subList.size();
			if(subSize>2){
				int last = subSize-1;
				for(int i=subSize-1;i>=0;i--){
					if(subList.get(i).equals(homeGroupID)){
						last = i;
						break;
					}
				}
				for(int i=last;i<subSize;i++)
					subListTemp.add(subList.get(i));
				groupIDFromHomeToWork.add(subListTemp);
//				System.out.println("subList:"+subListTemp);
			}
			//��ÿ�������ʱ����и���
//			System.out.println(day+":"+endIndex);
			if(endIndex!=0){
				for(int i=endIndex;i>=0;i--){
					String cell = cellAll[day][24].get(i);
					int time = cellTimeAll[day][24].get(i);
					String groupID = mapCellToGroupID.get(cell);
					Group group = cellGroup.get(Integer.parseInt(groupID));
					group.addTime(time);
					if(groupID.equals(homeGroupID))
						break;
				}
			}
		}
//		for(Group g:cellGroup){
//			System.out.println(g.getGourpID()+":"+g.getAllTime());
//		}
		return groupIDFromHomeToWork;
	}
	public void recordTheGroupTime(){
		
	}
	/*
	 * ������ൽ����ת�ƾ���
	 */
	public int[][] calGroupTransferMatrix(){
		int groupSize = cellGroup.size();
		groupTransferMatrix = new int[groupSize][groupSize];
		for(ArrayList<String> list:groupIDFromHomeToWork){
			for(int i=0;i<list.size()-1;i++){
				int pre = Integer.parseInt(list.get(i));
				int next = Integer.parseInt(list.get(i+1));
				groupTransferMatrix[pre][next]++;
			}
		}
		
		return groupTransferMatrix;
	}
	//����·�������в�ͬ����
	public ArrayList<Group> getDifferentGroupInAllPath(){
		HashSet<String> allGroupID = new HashSet<String>();
		for(ArrayList<String> list:device.groupIDFromHomeToWork)
			allGroupID.addAll(list);
		ArrayList<Group> allGroups = new ArrayList<Group>();
		for(String id:allGroupID){
			Group group = cellGroup.get(Integer.parseInt(id));
			if(id.equals(workGroupID)||id.equals(homeGroupID))
				group.isImportLoc = true;
			allGroups.add(group);
		}
		
		return allGroups;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}