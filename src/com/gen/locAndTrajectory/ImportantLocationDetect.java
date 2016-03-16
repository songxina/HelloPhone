package com.gen.locAndTrajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.gen.trajectory.Group;
import com.gen.trajectory.QueryTrajectory;

/*
 * ���ؾ������� �����Ӧ�� ��Ҫ�Ե÷�
 * 
 * ��Ҫ����Ϣ��
 * ÿ����վÿ����ֵ��ܴ���
 * ���ֵ�����
 * ���ֵ�Сʱ��

 * ��ס��ʶ��
 * ���ݣ�����11�㵽����6��֮����ִ�����
 * ����ÿ��������ܴ�����Ϊ����
 * ����������־����GroupID

 * ������ʶ��
 * ���ݣ�����11�㵽����5��֮����ִ�����
 * ����ÿ��������ܴ�����Ϊ����
 * ����������־����GroupID
 */
public class ImportantLocationDetect {

	ArrayList<String> cellAll[][];//Ŀǰ���������20131205-20131231 ��27��
	ArrayList<String> differentCell = new ArrayList<String>();//���в�ͬ�Ļ�վ
	public double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	public HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//��վID�Ծ������
	public Map<String,String> cellToCoordinate = new HashMap<String,String>();//��¼���л�վ������Ķ�Ӧ��ϵ����Ӧdifferentcell��
	public int numOfEachCell[][];//��¼ȫ����ִ���[daySize][cellSize]
	public int hourOfEachCell[][];//��¼���ֵ�Сʱ��[daySize][cellSize]
	public int dayOfEachCell[];//��¼���ֵ�����[cellSize]
	
	public double importantScore[];//��¼ÿ����վ����Ҫ������
	public int cellnum[];//��¼ÿ����վ���ֹ������д���
	ArrayList<Group> cellGroup = new ArrayList<Group>();//��¼���豸���ֹ������е�ľ���
	int cellSize;//���ֹ��Ĳ�ͬ��վ����
	public int daySize;//�������ݵ�����
	
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

	//�õ�ÿ����վ��¼�Ĵ���
	public int[] getcellnumber() {
		cellnum = new int[cellSize];
		for(int d=0;d<daySize;d++){
			for(int i=0;i<cellSize;i++)
				cellnum[i] += numOfEachCell[d][i];
		}
		return cellnum;
	}

	//��¼���г��ִ���
	public void calNumOfEachCell(){
		numOfEachCell = new int[daySize][cellSize];
		for(int d=0;d<daySize;d++){
			ArrayList<String> allDayTrajectory = cellAll[d][24];
			for(String s:allDayTrajectory)
				numOfEachCell[d][cellIndexMap.get(s)]++;
		}
	}
	
	//��¼���ֵ�Сʱ��
	public void calHourOfEachCell(){
		hourOfEachCell = new int[daySize][cellSize];
		for(int d=0;d<daySize;d++){
			for(int h=0;h<24;h++){
				ArrayList<String> thisHourTrajectory = cellAll[d][h];
				//ȥ��
				HashSet<String> hourTrajectorySet = new HashSet<String>();
				hourTrajectorySet.addAll(thisHourTrajectory);
				for(String s:hourTrajectorySet){
					hourOfEachCell[d][cellIndexMap.get(s)]++;
				}
			}
		}
	}
	//��¼���ֵ�����
	public void calDayOfEachCell(){
		dayOfEachCell = new int[cellSize];
		for(int d=0;d<daySize;d++){
			ArrayList<String> allDayTrajectory = cellAll[d][24];
			//ȥ��
			HashSet<String> allDayTrajectorySet = new HashSet<String>();
			allDayTrajectorySet.addAll(allDayTrajectory);
			for(String s:allDayTrajectorySet)
				dayOfEachCell[cellIndexMap.get(s)]++;
		}
	}
	//�������л�վ����Ҫ������
	// sum(ÿ�մ���/ÿ���ܴ��� ) * sum(Сʱ��) * ����
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
	 * �õ����о���
	 * ��ʵ�ϣ�cellgroup�и�����������к��������
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
	 * ��ס��ʶ��
	 * ���ݣ�����11�㵽����6��֮����ִ�����
	 * ����ÿ��������ܴ�����Ϊ����
	 * ����������־����GroupID
	 */
	public String HomeDetect(){
		
		//�������л�վ���������ڣ�Ŀ��ʱ�γ��ִ�������
		int[] cellHomeScore = new int[cellSize];
		for(int day=0;day<daySize;day++){
			for(int hour=0;hour<=6;hour++){
				for(String cell:cellAll[day][hour])
					cellHomeScore[cellIndexMap.get(cell)]++;
			}
			//11��
			for(String cell:cellAll[day][11])
				cellHomeScore[cellIndexMap.get(cell)]++;
		}
		
		//����ÿ����������
		for(Group g:cellGroup){
			double score = 0;
			for(String cell:g.getCellGroup())
				score+=cellHomeScore[cellIndexMap.get(cell)];
			g.setHomeScore(score);
		}
		
		//ѡ��HomeScore���ľ���
		String maxID="";
		double maxScore = 0;
		for(Group g:cellGroup){
			if(g.getHomeScore()>maxScore){
				maxScore = g.getHomeScore();
				maxID =g.getGourpID();
			}
		}
		//���Ϊ�գ�˵��ʱ����û�����ݣ���Ĭ��Ϊ����Ҫ�ľ���
		if(maxID.equals(""))
			maxID = 0+"";
		return maxID;
	}
	/*
	 * ������ʶ��
	 * ���ݣ�����11�㵽����5��֮����ִ�����
	 * ����ÿ��������ܴ�����Ϊ����
	 * ����������־����GroupID
	 */
	public String WorkDetect(){
		
		//�������л�վ���������ڣ�Ŀ��ʱ�γ��ִ�������
		int[] cellWorkScore = new int[cellSize];
		for(int day=0;day<daySize;day++){
			for(int hour=11;hour<=17;hour++){
				for(String cell:cellAll[day][hour])
					cellWorkScore[cellIndexMap.get(cell)]++;
			}
		}
		
		//����ÿ����������
		for(Group g:cellGroup){
			double score = 0;
			for(String cell:g.getCellGroup())
				score+=cellWorkScore[cellIndexMap.get(cell)];
			g.setWorkScore(score);
		}
		
		//ѡ��HomeScore���ľ���
		String maxID="";
		double maxScore = 0;
		for(Group g:cellGroup){
			if(g.getWorkScore()>maxScore){
				maxScore = g.getWorkScore();
				maxID =g.getGourpID();
			}
		}
		//���Ϊ�գ�˵��ʱ����û�����ݣ���Ĭ��Ϊ����Ҫ�ľ���
		if(maxID.equals(""))
			maxID = 0+"";
		return maxID;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
