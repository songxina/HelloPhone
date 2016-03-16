package com.gen.trajectory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//����·������
public class GeneticWay {

	ArrayList<String> cellAll[][] ;	//Ŀǰ���������20131205-20131228 ��25��
	HashMap<String, String> cellToGroup = new HashMap<String, String>();//��¼��վ��Ӧ����,cell_hour idIndex
	ArrayList<Group> cellGroup[] = new ArrayList[24];//��¼ÿСʱ����
	int daySize;
	int averageSize = 0;
	String startValueGroup[];//����grouplist,����ÿСʱ���ʼ��groupID�����飩
	String endValueGroup[];

	Map<String,String> cellToCoordinate = new HashMap<String,String>();//��վ��Ӧ��γ��
	public double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//��¼���л�վ��index�Ķ�Ӧ��ϵ����Ӧdifferentcell��
	
	public GeneticWay(MatrixForGroup matrix){
		cellAll = matrix.cellAll;
		cellToGroup = matrix.cellToGroup;
		cellGroup = matrix.cellGroup;
		daySize = matrix.daySize;
		startValueGroup = matrix.getStartGroup();//��ʼλ��
		endValueGroup = matrix.getEndGroup();//��ֹλ��
		
		cellToCoordinate = matrix.getCellToCoordinate();
		this.distanceBetweenCells = matrix.getDistanceBetweenCells();
		cellIndexMap = matrix.getCellIndexMap();
		getAverageGroupNumOfPaths(7);
	}
	//�����Сʱ����·�����ֵĲ�ͬ�����ƽ������
	public int getAverageGroupNumOfPaths(int hour){
		int number=0;
		int dayCount=0;
		for(int day=0;day<daySize;day++){
			ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);
			//ֻ���������������ʼ�����ֹ���·��
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
	//�����Сʱ����·�����ֵĲ�ͬ��������� ��λ�� �� ��ͬ����ĸ���
		public String getMedianGroupNumOfPaths(int hour){
			ArrayList<Integer> allNum = new ArrayList<Integer>();
			HashSet<Group> allDistinctGroupByWatch = new HashSet<Group>();
			int median;
			int dayCount=0;
			for(int day=0;day<daySize;day++){
				ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);
				//ֻ���������������ʼ�����ֹ���·��
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
//		����۲���ʣ�����������״̬��
//		�۲⵽��
//		�۲���ھ��෶Χ��
//		�۲���ھ��෶Χ��
	public double scoreByHmm(ArrayList<String> groupIDList,int hour,int averageSize){
		double score = 0;
		double pnull,pin,pout;
		String mAndN = getMedianGroupNumOfPaths(hour);//�۲����в�ͬ�����������λ�� he ��ͬ����ĸ���
		int median = Integer.parseInt(mAndN.split("_")[0]);				
		int allDistinctGroupByWatch = Integer.parseInt(mAndN.split("_")[1]);
		int groupSize = groupIDList.size();
		double pinPre = (double)averageSize/allDistinctGroupByWatch; 
		pinPre = pinPre + 0.5*(1-pinPre);
//		System.out.println("aaaa"+groupSize+"_ "+allDistinctGroupByWatch+"_ "+pinPre);
		//����۲����
		if(groupSize<=median)//С����λ�������Ĺ켣��������Ϊ���ĸ��ʺܵ�
			pnull = 1;
		else
			pnull = 1-(median/(double)groupSize);
		pin = pinPre*(1-pnull);
		pout = (1-pinPre)*(1-pnull);
//		System.out.println("median pnull pin pout"+median+"_"+pnull+"_"+pin+"_"+pout);
		DecimalFormat df = new DecimalFormat("0.000");
		System.out.print("---"+median+"_ "+df.format(pnull)+"_ "+df.format(pin)+"_ "+df.format(pout)+"_ "+df.format(pinPre));
		//��������
		for(int day=0;day<daySize;day++){
			ArrayList<Group> sample = cellListToGroupList(cellAll[day][hour], hour);	
			String start = startValueGroup[hour];
			String end = endValueGroup[hour];
			//��sample���м�֧
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
					//����������֣����groupδ�۲⵽��,���򣬹۲⵽һ���Ǿ����ڲ��㡣
					if(groupIDList.subList(groupListIndex, groupSize).contains(sampleid))
						psample*=pnull;
					else
						psample*=pout;
				}
				groupListIndex++;
				if(groupListIndex>groupSize)
					break;
			}
			//����۲�״̬û�����꣬˵�����һ������״̬�۲⵽�˺���һ�ѷ����ڲ��ĵ�
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
		
	//�����֣�distance�����룩��number����������orientation������˳��
	//	1��distance�����룩������·����ÿ�����������ľ���֮�ͣ�����ͬ���������
	//	2��number������������ѡ·���о���ĸ�������̫�ࣨ�ͷ�������
	//							��·���������֮��ľ���ֵ
	//	3��orientation������˳�򣩣���ͬ����������ÿ���������֮��ĺ�
	//								���վ�����ֵ�˳����ӣ�������ظ�ֵ��
	//groupidlist ������ֻ�Ǹ�Сʱ�� cell group �е�index
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
			//С��ƽ��������test��������
			if(groupSize<5)
				continue;
			String start = startValueGroup[hour];
			String end = endValueGroup[hour];
		//��sample���м�֧
			if(!(isContainGivenGroupID(sample, start) && isContainGivenGroupID(sample, end)))
				continue;
//			printGroup(sample);
			//ֻ������ͬ����ĸ���
				for(Group g:sample){
					String id = g.getGourpID().split("_")[1];
						if(groupIDList.contains(id)){
//							System.out.print(id+"_-_");
							distanceCount++;
						}							
				}
//			System.out.println();
//			distance += (groupSize-distanceCount)/groupSize;//���㲻��ͬ����ĸ���,��sample��û�б������ĵ�
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
	
	//�ж�group�����У����޸���groupID
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
	
	//�������������ƶ�
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
//�������У�����һ�ԣ�˳��ߵ������һ
	public double topologicalForBeforAndAfter(ArrayList<String> groupIDList1,ArrayList<Group> groupList2){
		double value = 0;
		ArrayList<String> listID1 = new ArrayList<String>();
		ArrayList<String> listID2 = new ArrayList<String>();
		ArrayList<String> listCommen = new ArrayList<String>();
		
		//ת��ΪString list
		listID1.addAll(groupIDList1); 
		for(Group g:groupList2)
			if(!listID2.contains(g.getGourpID()))
				listID2.add(g.getGourpID().split("_")[1]);
		//���㹲��groupID
		for(String s:listID1)
			if(listID2.contains(s))
				listCommen.add(s);
		
		//ͳ��˳��ߵ�����Ŀ
		for(String id:listCommen){
			int before = 0 ,after = 0;
			ArrayList<String> testBeforeList = new ArrayList<String>();
			ArrayList<String> sampleAferList = new ArrayList<String>();
			//test ��id��λ��
			for(int i=0;i<listID1.size();i++)
				if(id.equals(listID1.get(i)))
					before = i;
			for(int i=0;i<before;i++)
				testBeforeList.add(listID1.get(i));
			
			//sample ��id��λ��
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
	
	//����һ��·�������ڽڵ�����֮��
	public double adjacentDistance(int hour,ArrayList<String> groupIDList){
		double distance = 0;
		//�õ���������
		ArrayList<Group> groupList = new ArrayList<Group>();
		for(String s:groupIDList){
			int index = Integer.parseInt(s);
			groupList.add(cellGroup[hour].get(index));
		}
		//�������ڽڵ�����֮��
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
	
	//��cell�����Ϊ���ظ���group����˳��ȥ����β����֮�����������
		public ArrayList<Group> cellListToGroupList(ArrayList<String> cells,int hour){
			ArrayList<Group> groupList = new ArrayList<Group>();
			ArrayList<String> gidList = new ArrayList<String>();
			String startGroupID = startValueGroup[hour].split("_")[1];
			String endGroupID = endValueGroup[hour].split("_")[1];
			//�����endgroup��ֹͣ
			//�������startGroup��ȥ��֮ǰ������
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

	//���һ������
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
