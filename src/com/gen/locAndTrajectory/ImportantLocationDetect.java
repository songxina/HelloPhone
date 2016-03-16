package com.gen.locAndTrajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.gen.trajectory.Group;
import com.gen.trajectory.QueryTrajectory;

/*
 * 返回聚类链表 及其对应的 重要性得分
 * 
 * 需要的信息：
 * 每个基站每天出现的总次数
 * 出现的天数
 * 出现的小时数

 * 居住地识别
 * 依据：下午11点到早上6点之间出现次数多
 * 计算每个聚类的总次数作为评分
 * 返回最大评分聚类的GroupID

 * 工作地识别
 * 依据：上午11点到下午5点之间出现次数多
 * 计算每个聚类的总次数作为评分
 * 返回最大评分聚类的GroupID
 */
public class ImportantLocationDetect {

	ArrayList<String> cellAll[][];//目前存的数据是20131205-20131231 共27天
	ArrayList<String> differentCell = new ArrayList<String>();//所有不同的基站
	public double distanceBetweenCells[][];//记录不同基站间的距离
	public HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//基站ID对矩阵序号
	public Map<String,String> cellToCoordinate = new HashMap<String,String>();//记录所有基站与坐标的对应关系，对应differentcell中
	public int numOfEachCell[][];//记录全天出现次数[daySize][cellSize]
	public int hourOfEachCell[][];//记录出现的小时数[daySize][cellSize]
	public int dayOfEachCell[];//记录出现的天数[cellSize]
	
	public double importantScore[];//记录每个基站的重要性评分
	public int cellnum[];//记录每个基站出现过的所有次数
	ArrayList<Group> cellGroup = new ArrayList<Group>();//记录该设备出现过的所有点的聚类
	int cellSize;//出现过的不同基站个数
	public int daySize;//输入数据的天数
	
	QueryTrajectory queryTrajectory;
	
	public ImportantLocationDetect(QueryTrajectory queryTrajectory) {
		this.queryTrajectory = queryTrajectory;
		daySize = queryTrajectory.dayNum;
		cellAll = queryTrajectory.getCellAll();
		
		differentCell.addAll(queryTrajectory.getDifferentCell());
		cellToCoordinate = QueryTrajectory.getMap();
		cellSize = differentCell.size();
		for(int i=0;i<cellSize;i++)
			cellIndexMap.put(differentCell.get(i), i);
		importantScore = new double[cellSize];
	}
	
	public double[] getImportantScore() {
		return importantScore;
	}

	//得到每个基站记录的次数
	public int[] getcellnumber() {
		cellnum = new int[cellSize];
		for(int d=0;d<daySize;d++){
			for(int i=0;i<cellSize;i++)
				cellnum[i] += numOfEachCell[d][i];
		}
		return cellnum;
	}

	//记录所有出现次数
	public void calNumOfEachCell(){
		numOfEachCell = new int[daySize][cellSize];
		for(int d=0;d<daySize;d++){
			ArrayList<String> allDayTrajectory = cellAll[d][24];
			for(String s:allDayTrajectory)
				numOfEachCell[d][cellIndexMap.get(s)]++;
		}
	}
	
	//记录出现的小时数
	public void calHourOfEachCell(){
		hourOfEachCell = new int[daySize][cellSize];
		for(int d=0;d<daySize;d++){
			for(int h=0;h<24;h++){
				ArrayList<String> thisHourTrajectory = cellAll[d][h];
				//去重
				HashSet<String> hourTrajectorySet = new HashSet<String>();
				hourTrajectorySet.addAll(thisHourTrajectory);
				for(String s:hourTrajectorySet){
					hourOfEachCell[d][cellIndexMap.get(s)]++;
				}
			}
		}
	}
	//记录出现的天数
	public void calDayOfEachCell(){
		dayOfEachCell = new int[cellSize];
		for(int d=0;d<daySize;d++){
			ArrayList<String> allDayTrajectory = cellAll[d][24];
			//去重
			HashSet<String> allDayTrajectorySet = new HashSet<String>();
			allDayTrajectorySet.addAll(allDayTrajectory);
			for(String s:allDayTrajectorySet)
				dayOfEachCell[cellIndexMap.get(s)]++;
		}
	}
	//计算所有基站的重要性评分
	// sum(每日次数/每日总次数 ) * sum(小时数) * 天数
	public void calImportantScoreForEachCell(){
		double numScore[] = new double[cellSize];
		for(int i=0;i<cellSize;i++)
			numScore[i]=0;
		int hourScore[] = new int[cellSize];
		int dayScore[] = dayOfEachCell;
		
		for(String cell:differentCell){
			int index = cellIndexMap.get(cell);
			for(int d=0;d<daySize;d++){
				int totalNum = cellAll[d][24].size();
				if(totalNum!=0)
					numScore[index] += numOfEachCell[d][index]/(double)totalNum;
				hourScore[index] += hourOfEachCell[d][index];
			}
		}
		for(int i=0;i<cellSize;i++){
			importantScore[i] = numScore[i] * hourScore[i] * dayScore[i];
//			System.out.println(differentCell.get(i)+" : "+importantScore[i]
//					+" "+numScore[i] +" "+ hourScore[i] +" "+ dayScore[i]);
		}

	}
	
	/* 
	 * 得到所有聚类
	 * 事实上，cellgroup中各个聚类的序列号是有序的
	 */
	public ArrayList<Group> generateAllTheGroups(){
		System.out.println("Grouping...");
		calNumOfEachCell();
		calDayOfEachCell();
		calHourOfEachCell();
		calImportantScoreForEachCell();
		LeaderCluster leaderCluster = new LeaderCluster(importantScore, cellIndexMap, differentCell, cellToCoordinate);
		cellGroup = leaderCluster.doCluster();
		System.out.println("GROUP DONE!");
		return cellGroup;
	}
	
	/*
	 * 居住地识别
	 * 依据：下午11点到早上6点之间出现次数多
	 * 计算每个聚类的总次数作为评分
	 * 返回最大评分聚类的GroupID
	 */
	public String HomeDetect(){
		
		//计算所有基站，所有天内，目标时段出现次数总量
		int[] cellHomeScore = new int[cellSize];
		for(int day=0;day<daySize;day++){
			for(int hour=0;hour<=6;hour++){
				for(String cell:cellAll[day][hour])
					cellHomeScore[cellIndexMap.get(cell)]++;
			}
			//11点
			for(String cell:cellAll[day][11])
				cellHomeScore[cellIndexMap.get(cell)]++;
		}
		
		//计算每个聚类评分
		for(Group g:cellGroup){
			double score = 0;
			for(String cell:g.getCellGroup())
				score+=cellHomeScore[cellIndexMap.get(cell)];
			g.setHomeScore(score);
		}
		
		//选出HomeScore最大的聚类
		String maxID="";
		double maxScore = 0;
		for(Group g:cellGroup){
			if(g.getHomeScore()>maxScore){
				maxScore = g.getHomeScore();
				maxID =g.getGourpID();
			}
		}
		//如果为空，说明时段内没有数据，则默认为最重要的聚类
		if(maxID.equals(""))
			maxID = 0+"";
		return maxID;
	}
	/*
	 * 工作地识别
	 * 依据：上午11点到下午5点之间出现次数多
	 * 计算每个聚类的总次数作为评分
	 * 返回最大评分聚类的GroupID
	 */
	public String WorkDetect(){
		
		//计算所有基站，所有天内，目标时段出现次数总量
		int[] cellWorkScore = new int[cellSize];
		for(int day=0;day<daySize;day++){
			for(int hour=11;hour<=17;hour++){
				for(String cell:cellAll[day][hour])
					cellWorkScore[cellIndexMap.get(cell)]++;
			}
		}
		
		//计算每个聚类评分
		for(Group g:cellGroup){
			double score = 0;
			for(String cell:g.getCellGroup())
				score+=cellWorkScore[cellIndexMap.get(cell)];
			g.setWorkScore(score);
		}
		
		//选出HomeScore最大的聚类
		String maxID="";
		double maxScore = 0;
		for(Group g:cellGroup){
			if(g.getWorkScore()>maxScore){
				maxScore = g.getWorkScore();
				maxID =g.getGourpID();
			}
		}
		//如果为空，说明时段内没有数据，则默认为最重要的聚类
		if(maxID.equals(""))
			maxID = 0+"";
		return maxID;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
