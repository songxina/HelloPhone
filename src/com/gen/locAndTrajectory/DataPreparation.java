package com.gen.locAndTrajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.gen.data.DataMatrix;
import com.gen.location.PhoneRecordDAO;
import com.gen.trajectory.Group;

/*
 * 1.获取home与work之间的聚类序列
 * 2.计算聚类到聚类转移矩阵
 * 3.计算基站到基站转移矩阵
 * 
 * 4.返回home与work多天序列中，出现的所有不同聚类。
 * */

public class DataPreparation {

	public ArrayList<Group> cellGroup = new ArrayList<Group>();//记录该设备出现过的所有点的聚类
	ArrayList<String> 	cellAll[][];//目前存的数据是20131205-20131231 共27天
	ArrayList<Integer> cellTimeAll[][];//每个基站对应的时间
	Device device ;
	String homeGroupID = "";
	String workGroupID = "";
	int daySize ;
	
	HashMap<String, String> mapCellToGroupID = new HashMap<String, String>();//记录每个基站对应的聚类
	ArrayList<ArrayList<String>> groupIDFromHomeToWork ;//记录每日home与work之间的聚类序列
	int[][] groupTransferMatrix;//记录聚类到聚类转移矩阵
	
	
	
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
	 * 记录每个基站对应的聚类
	 */
	public void fillMapCellToGroupID(){
		for(Group g:cellGroup){
			String gid = g.getGourpID();
			for(String cell:g.getCellGroup())
				mapCellToGroupID.put(cell, gid);
		}
	}
	/*
	 * 基站序列变为groupID序列
	 */
	public ArrayList<String> cellToGroupIDList(ArrayList<String> cellList){
		ArrayList<String> groupidList = new ArrayList<String>();
		for(String cell:cellList)
			groupidList.add(mapCellToGroupID.get(cell));
		return groupidList;
	}
	/*
	 * 获取每日home与work之间的聚类序列
	 * 修改每个Group中的时间。ShowRoad类中的allGroup来自本类，因此，只要修改本类中的allGroup中Group即可
	 */
	public ArrayList<ArrayList<String>> extractTrajectoryFromHomeToWork(){
		groupIDFromHomeToWork = new ArrayList<ArrayList<String>>();
		fillMapCellToGroupID();
		for(int day=0;day<daySize;day++){
			int endIndex = 0;
			//得到全天聚类转移序列
			ArrayList<String> groupidList = cellToGroupIDList(cellAll[day][24]);
//			System.out.println("groupidList:"+groupidList);
			ArrayList<String> subList =new ArrayList<String>();
			subList.add(homeGroupID);
			if(groupidList.contains(homeGroupID) && groupidList.contains(workGroupID)){
				//找到起始位置
				int begin = 0;
				for(String id:groupidList){
					if(id.equals(homeGroupID))
						break;
					begin++;
				}
				//下一个聚类id要跟前一个id不同（去连续的重复）
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
			//对sublist做一下处理，取最后一个homegroupID和workGroupID之间的序列
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
			//对每个聚类的时间进行更新
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
	 * 计算聚类到聚类转移矩阵
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
	//返回路径中所有不同聚类
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