package com.gen.trajectory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//计算路径评分
public class GeneticWay {

	ArrayList<String> cellAll[][] ;	//目前存的数据是20131205-20131228 共25天
	HashMap<String, String> cellToGroup = new HashMap<String, String>();//记录基站对应聚类,cell_hour idIndex
	ArrayList<Group> cellGroup[] = new ArrayList[24];//记录每小时聚类
	int daySize;
	int averageSize = 0;
	String startValueGroup[];//给定grouplist,返回每小时最常起始的groupID（数组）
	String endValueGroup[];

	Map<String,String> cellToCoordinate = new HashMap<String,String>();//基站对应经纬度
	public double distanceBetweenCells[][];//记录不同基站间的距离
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//记录所有基站与index的对应关系，对应differentcell中
	
	public GeneticWay(MatrixForGroup matrix){
		cellAll = matrix.cellAll;
		cellToGroup = matrix.cellToGroup;
		cellGroup = matrix.cellGroup;
		daySize = matrix.daySize;
		startValueGroup = matrix.getStartGroup();//起始位置
		endValueGroup = matrix.getEndGroup();//终止位置
		
		cellToCoordinate = matrix.getCellToCoordinate();
		this.distanceBetweenCells = matrix.getDistanceBetweenCells();
		cellIndexMap = matrix.getCellIndexMap();
		getAverageGroupNumOfPaths(7);
	}
	//计算该小时所有路径出现的不同聚类的平均个数
	public int getAverageGroupNumOfPaths(int hour){
		int number=0;
		int dayCount=0;
		for(int day=0;day<daySize;day++){
			ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);
			//只计算包含最大概率起始点和终止点的路径
			String start = startValueGroup[hour];//hour_index
			String end = endValueGroup[hour];
			if(!(isContainGivenGroupID(sample, start) && isContainGivenGroupID(sample, end)))
				continue;
			Set<Group> g = new HashSet<Group>();
			g.addAll(sample);
			number+=g.size();
			dayCount++;
		}
		averageSize = number/dayCount;
		return averageSize;
	}
	//计算该小时所有路径出现的不同聚类个数的 中位数 和 不同聚类的个数
		public String getMedianGroupNumOfPaths(int hour){
			ArrayList<Integer> allNum = new ArrayList<Integer>();
			HashSet<Group> allDistinctGroupByWatch = new HashSet<Group>();
			int median;
			int dayCount=0;
			for(int day=0;day<daySize;day++){
				ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);
				//只计算包含最大概率起始点和终止点的路径
				String start = startValueGroup[hour];//hour_index
				String end = endValueGroup[hour];
//				System.out.println("start end"+start+"_"+end);
				if(!(isContainGivenGroupID(sample, start) && isContainGivenGroupID(sample, end)))
					continue;
				HashSet<Group> g = new HashSet<Group>();
				g.addAll(sample);
				allNum.add(g.size());
				dayCount++;
				allDistinctGroupByWatch.addAll(sample);
			}
			Collections.sort(allNum);
			median = allNum.get((dayCount/2)+1);
			String result = median+"_"+allDistinctGroupByWatch.size();
			return result;
		}
		
	// scor by hmm
//		定义观测概率（对任意隐藏状态）
//		观测到空
//		观测点在聚类范围内
//		观测点在聚类范围外
	public double scoreByHmm(ArrayList<String> groupIDList,int hour,int averageSize){
		double score = 0;
		double pnull,pin,pout;
		String mAndN = getMedianGroupNumOfPaths(hour);//观测序列不同聚类个数的中位数 he 不同聚类的个数
		int median = Integer.parseInt(mAndN.split("_")[0]);				
		int allDistinctGroupByWatch = Integer.parseInt(mAndN.split("_")[1]);
		int groupSize = groupIDList.size();
		double pinPre = (double)averageSize/allDistinctGroupByWatch; 
		pinPre = pinPre + 0.5*(1-pinPre);
//		System.out.println("aaaa"+groupSize+"_ "+allDistinctGroupByWatch+"_ "+pinPre);
		//计算观测概率
		if(groupSize<=median)//小于中位数个数的轨迹，我们认为他的概率很低
			pnull = 1;
		else
			pnull = 1-(median/(double)groupSize);
		pin = pinPre*(1-pnull);
		pout = (1-pinPre)*(1-pnull);
//		System.out.println("median pnull pin pout"+median+"_"+pnull+"_"+pin+"_"+pout);
		DecimalFormat df = new DecimalFormat("0.000");
		System.out.print("---"+median+"_ "+df.format(pnull)+"_ "+df.format(pin)+"_ "+df.format(pout)+"_ "+df.format(pinPre));
		//计算评分
		for(int day=0;day<daySize;day++){
			ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);	
			String start = startValueGroup[hour];
			String end = endValueGroup[hour];
			//对sample进行减支
			if(!(isContainGivenGroupID(sample, start) && isContainGivenGroupID(sample, end)))
				continue;
			ArrayList<String> sampleGroupID = new ArrayList<String>();
			for(Group g:sample){
				String idstring = g.getGourpID().split("_")[1];	
				sampleGroupID.add(idstring);
//				System.out.print(idstring+"_");
			}
//			System.out.println();
			int sampleGroupSize = sampleGroupID.size();
			int groupListIndex = 0;
			double psample=1;
			int i=0;
			for(;i<sampleGroupSize;i++){
				String sampleid = sampleGroupID.get(i);
				String hStateID = groupIDList.get(groupListIndex);
				if(sampleid.equals(hStateID))
					psample*=pin;
				else{
					//后续如果出现，则该group未观测到点,否则，观测到一个非聚类内部点。
					if(groupIDList.subList(groupListIndex, groupSize).contains(sampleid))
						psample*=pnull;
					else
						psample*=pout;
				}
				groupListIndex++;
				if(groupListIndex>groupSize)
					break;
			}
			//如果观测状态没遍历完，说明最后一个隐藏状态观测到了后续一堆非其内部的点
			if(groupListIndex>groupSize){
				for(int j=i;j<sampleGroupSize;j++)
					psample*=pout;
			}
			else {
				for(int k=groupListIndex;k<groupSize;k++)
					psample*=pnull;
			}
//			System.out.println(psample);
			score+=psample;
		}
		return score*Math.pow(10, median);
	}		
		
	//三部分：distance（距离），number（个数），orientation（方向，顺序）
	//	1、distance（距离）：样本路径中每个点与最近点的距离之和（或相同聚类个数）
	//	2、number（个数）：备选路径中聚类的个数不能太多（惩罚函数）
	//							两路径聚类个数之差的绝对值
	//	3、orientation（方向，顺序）：相同聚类序列中每个聚类序号之差的和
	//								按照聚类出现的顺序添加，不添加重复值。
	//groupidlist 给定的只是该小时的 cell group 中的index
	public double optimiticScore(ArrayList<String> groupIDList,int hour){
		double score = 0;
		double distance = 0;
		double number = 0;
		double orientation = 0;
		double topological = 0;
		double adjacentLength = 0;
//		printString(groupIDList);
		
		for(int day=0;day<daySize;day++){
			ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);
		
		//distance
			double distanceCount = 0;
			int groupSize = sample.size();
			//小于平均数量的test样本考虑
			if(groupSize<5)
				continue;
			String start = startValueGroup[hour];
			String end = endValueGroup[hour];
		//对sample进行减支
			if(!(isContainGivenGroupID(sample, start) && isContainGivenGroupID(sample, end)))
				continue;
//			printGroup(sample);
			//只计算相同聚类的个数
				for(Group g:sample){
					String id = g.getGourpID().split("_")[1];
						if(groupIDList.contains(id)){
//							System.out.print(id+"_-_");
							distanceCount++;
						}							
				}
//			System.out.println();
//			distance += (groupSize-distanceCount)/groupSize;//计算不相同聚类的个数,即sample中没有被包括的点
			distance += (groupSize-distanceCount);
		//number
			number += Math.abs(groupIDList.size() - groupSize-13);
		
		//orientation
			orientation += calOrientation(groupIDList,sample);
			
		//topologicalForBeforAndAfter
			topological += topologicalForBeforAndAfter(groupIDList,sample);
			
		//adjacentDistance
			adjacentLength += adjacentDistance(hour,groupIDList);
			
//			System.out.println(distanceCount+"_"+"distance:"+distance+" number:"+number+" orienation:"+orientation+"___");
		}
//		System.out.println();
		score = distance*2 +topological + (int)(adjacentLength*0.001);
//		score = distance*2 + number + topological + adjacentLength;//orientation ;
//		System.out.println("distance:"+distance+" number:"+number+" orienation:"+orientation+" topological:"+topological+" adjacentLength"+adjacentLength);
		return score;
	}
	
	//判断group序列中，有无给定groupID
	public boolean isContainGivenGroupID(ArrayList<Group> list,String groupID){
		boolean result = false;
		for(Group g:list){
//			System.out.println(g.getGourpID());
			if(g.getGourpID().equals(groupID)){
				result = true;
				
			}
		}
		return result;
	}
	
	//计算两序列相似度
	public double calOrientation(ArrayList<String> groupIDList1,ArrayList<Group> groupList2){
		double value = 0;
		ArrayList<String> listID1 = new ArrayList<String>();
		ArrayList<String> listID2 = new ArrayList<String>();
		ArrayList<String> listCommen = new ArrayList<String>();
		
		listID1.addAll(groupIDList1); 
		for(Group g:groupList2)
			if(!listID2.contains(g.getGourpID()))
				listID2.add(g.getGourpID().split("_")[1]);
		for(String s:listID1)
			if(listID2.contains(s))
				listCommen.add(s);
		ArrayList<String> listIDdistinct1 = new ArrayList<String>();
		ArrayList<String> listIDdistinct2 = new ArrayList<String>();
		for(String s:listID1)
			if(listCommen.contains(s))
				listIDdistinct1.add(s);
		for(String s:listID2)
			if(listCommen.contains(s))
				listIDdistinct2.add(s);
				
		for(String id:listCommen){
			int index1 = 0 ,index2 = 0;
			for(int i=0;i<listIDdistinct1.size();i++)
				if(id.equals(listIDdistinct1.get(i)))
					index1 = i;
			for(int i=0;i<listIDdistinct2.size();i++)
				if(id.equals(listIDdistinct2.get(i)))
					index2 = i;
			value+=Math.abs(index1-index2);
		}
		
		return value;
	}
//两聚类中，任意一对，顺序颠倒，则加一
	public double topologicalForBeforAndAfter(ArrayList<String> groupIDList1,ArrayList<Group> groupList2){
		double value = 0;
		ArrayList<String> listID1 = new ArrayList<String>();
		ArrayList<String> listID2 = new ArrayList<String>();
		ArrayList<String> listCommen = new ArrayList<String>();
		
		//转换为String list
		listID1.addAll(groupIDList1); 
		for(Group g:groupList2)
			if(!listID2.contains(g.getGourpID()))
				listID2.add(g.getGourpID().split("_")[1]);
		//计算共有groupID
		for(String s:listID1)
			if(listID2.contains(s))
				listCommen.add(s);
		
		//统计顺序颠倒的数目
		for(String id:listCommen){
			int before = 0 ,after = 0;
			ArrayList<String> testBeforeList = new ArrayList<String>();
			ArrayList<String> sampleAferList = new ArrayList<String>();
			//test 该id的位置
			for(int i=0;i<listID1.size();i++)
				if(id.equals(listID1.get(i)))
					before = i;
			for(int i=0;i<before;i++)
				testBeforeList.add(listID1.get(i));
			
			//sample 该id的位置
			for(int i=0;i<listID2.size();i++)
				if(id.equals(listID2.get(i)))
					after = i;
			for(int i=after+1;i<listID2.size();i++)
				sampleAferList.add(listID2.get(i));
			
			for(String s:sampleAferList){
				if(testBeforeList.contains(s))
					value++;
			}
		}
		
		return value;
	}
	
	//计算一个路径，相邻节点间距离之和
	public double adjacentDistance(int hour,ArrayList<String> groupIDList){
		double distance = 0;
		//得到聚类链表
		ArrayList<Group> groupList = new ArrayList<Group>();
		for(String s:groupIDList){
			int index = Integer.parseInt(s);
			groupList.add(cellGroup[hour].get(index));
		}
		//计算相邻节点间距离之和
		for(int i=0;i<groupList.size()-1;i++){
			String gCenterCellOne = groupList.get(i).getCenterCell();
			String gCenterCellTwo = groupList.get(i+1).getCenterCell();
			int cellOneIndex = cellIndexMap.get(gCenterCellOne);
			int cellTwoIndex = cellIndexMap.get(gCenterCellTwo);
			double d = distanceBetweenCells[cellOneIndex][cellTwoIndex];
			distance += d;
		}
		return distance;
	}
	
	//将cell链表改为不重复的group链表，顺便去掉结尾聚类之后的噪声聚类
		public ArrayList<Group> cellListToGroupList(ArrayList<String> cells,int hour){
			ArrayList<Group> groupList = new ArrayList<Group>();
			ArrayList<String> gidList = new ArrayList<String>();
			String startGroupID = startValueGroup[hour].split("_")[1];
			String endGroupID = endValueGroup[hour].split("_")[1];
			//如果到endgroup则停止
			//如果包括startGroup则去掉之前的序列
			for(String cell:cells){
				String gid = cellToGroup.get(cell+"_"+hour);
				if(!gidList.contains(gid))
					gidList.add(gid);
				if(gid.equals(endGroupID))
					break;
			}
			ArrayList<String> gidListCut = new ArrayList<String>();
			if(gidList.contains(startGroupID)){
				int i=0;
				for(;i<gidList.size();i++)
					if(gidList.get(i).equals(startGroupID))
						break;
				gidListCut.addAll(gidList.subList(i, gidList.size()));
			}
			for(String id:gidListCut){
				int index = Integer.parseInt(id);
				groupList.add(cellGroup[hour].get(index));
//				System.out.print(index+" "+cellGroup[hour].get(index).getGourpID()+"-");
			}
//			System.out.println();
			return groupList;
		}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	//输出一个链表
	public void printString(ArrayList<String> list){
		for(String s:list)
			System.out.print(s+",");
		System.out.println();
	}
	public void printGroup(ArrayList<Group> list){
		for(Group g:list)
			System.out.print(g.getGourpID().split("_")[1]+",");
		System.out.println();
	}
}
