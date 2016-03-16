package com.gen.locAndTrajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.gen.trajectory.*;

import de.fhpotsdam.unfolding.geo.Location;

//������������ѡ����һ����ѡ�㣺
//1.�Ƕȣ���������ת�Ƶ�group���߶Աȣ���
//2.���루����������Ҫ���֮��ľ��룩��
public class FindTrajectoryCombine {

	HashMap<String, String> cellToGroup = new HashMap<String, String>();// ��¼��վ��Ӧ�����index, cell_hour,idIndex
	ArrayList<Group> cellGroup[] = new ArrayList[24];// ��¼ÿСʱ����
	double groupTransferByHour[][][];// �Ծ���Ϊ��λ��ÿСʱת�ƾ��󡣼�¼ת�Ƹ��ʣ����������е��¼֮�ͣ�
	public double distanceBetweenCells[][];// ��¼��ͬ��վ��ľ���
	Map<String, String> cellToCoordinate = new HashMap<String, String>();// ��վ��Ӧ��γ��
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();// ��¼���л�վ��index�Ķ�Ӧ��ϵ����Ӧdifferentcell��

	// ÿ��������ֵĸ��ʣ���¼��group���е�possibility��
	int cellSize = 0;// ���豸�����������в�ͬ��վ��
	MatrixForGroup matrixForGroup;
	String[] startGroup = new String[24];// ÿСʱ��Ƶ������ʼ�㣬����groupID
	String[] endGroup = new String[24];// ÿСʱ��Ƶ������ֹ�㣬����groupID
	ArrayList<String> path[] = new ArrayList[24];// ��¼ÿСʱ��ȡ��·����cellID��

	String startValueGroup[];// ����grouplist,����ÿСʱ���ʼ��groupID�����飩
	String endValueGroup[];// hour+index

	ArrayList<Integer>[] cellNextCells;// ��¼ÿ������ĺ�ѡ����� ��¼index
	GeneticWay genetic;

	public FindTrajectoryCombine(MatrixForGroup matrixForGroup) {
		this.matrixForGroup = matrixForGroup;
		cellToGroup = matrixForGroup.getCellToGroup();
		this.cellGroup = matrixForGroup.calAllGroup();
		this.groupTransferByHour = matrixForGroup.getGroupTransferByHour();
		this.distanceBetweenCells = matrixForGroup.getDistanceBetweenCells();
		cellSize = matrixForGroup.getSize();
		startGroup = matrixForGroup.getStartGroup();
		endGroup = matrixForGroup.getEndGroup();
		cellToCoordinate = matrixForGroup.getCellToCoordinate();
		cellIndexMap = matrixForGroup.getCellIndexMap();
		genetic = new GeneticWay(matrixForGroup);
		startValueGroup = matrixForGroup.getStartGroup();// ��ʼλ��
		endValueGroup = matrixForGroup.getEndGroup();// ��ֹλ��

		for (int i = 0; i < startGroup.length; i++)
			System.out.println(startGroup[i] + "_" + endGroup[i]);

		// for(int hour=0;hour<24;hour++){
		// path[hour] = new ArrayList<String>();
		// path[hour].addAll(calRegularPath(hour));
		// }
	}

	// DFS ���ѣ���������·��
	public ArrayList<ArrayList<Integer>> dfsForAllTrajectories(int hour) {
		ArrayList<ArrayList<Integer>> allPath = new ArrayList<ArrayList<Integer>>();
		// �õ����е����һ��ѡ��
		cellNextCells = calCellNextCells(hour);// ��¼ÿ������ĺ�ѡ����� ��¼index

		// �õ���ʼ�����ֹ��
		String start = startValueGroup[hour];// hour+index
		String end = endValueGroup[hour];
		int startIndex = Integer.parseInt(start.split("_")[1]);
		int endIndex = Integer.parseInt(end.split("_")[1]);
//System.out.println("aaakaishia a a "+startIndex+" "+endIndex);
		//��������·����stack
		Stack<ArrayList<Integer>> pathStack = new Stack<ArrayList<Integer>>();
		ArrayList<Integer> t = new ArrayList<Integer>();
		t.add(startIndex);
		pathStack.push(t);
		
//		Stack<Integer> stack = new Stack<Integer>();
//		stack.push(startIndex);

		// DFS
		// ����������1������end���ࣻ 2��ľ����һ���� 3����һ���Ѿ�������·���У�
		while (!pathStack.isEmpty()) {
			ArrayList<Integer> pathNow = new ArrayList<Integer>();
			pathNow.addAll(pathStack.pop());
			int now = pathNow.get(pathNow.size()-1);
			// �Ƿ񵽴�end�ڵ�
			if (now == endIndex) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.addAll(pathNow);
				allPath.add(temp);
				continue;
			}
			ArrayList<Integer> nexts = cellNextCells[now];

			for (int p : nexts){
//				stack.push(p);
				//�л�
				if(pathNow.contains(p)){
//					ArrayList<Integer> temp = new ArrayList<Integer>();
//					temp.addAll(pathNow);
//					allPath.add(temp);
					continue;
				}
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.addAll(pathNow);
				temp.add(p);
				pathStack.push(temp);
			}
				
		}

//		for (ArrayList<Integer> l : allPath) {
//			for (int i : l)
//				System.out.print(i + "__");
//			System.out.println();
//		}

		return allPath;
	}

	// -------------------------------------------------------------------------------
	// ����ÿ������� ��ѡ���� ��¼index, ��ֵcellNextCells
	public ArrayList<Integer>[] calCellNextCells(int hour) {

		String startGroupID = startGroup[hour];//��Сʱ�������ʼ
		int groupStartIndex = Integer.parseInt(startGroupID.split("_")[1]);
		Group groupStart = cellGroup[hour].get(groupStartIndex);
		
		ArrayList<Group> glist = cellGroup[hour];
		//����ÿ�������������������� ƽ��ֵ �� ����
		double averageAndDeviation[] = new double[2];//��һ����ƽ�������ڶ����Ƿ���
		averageAndDeviation = calAverageClosestDistanceBetweenGroups(glist);
		double meanClosestDistance=0, ClosestDistanceDeviation = 0;
		meanClosestDistance = averageAndDeviation[0];
		ClosestDistanceDeviation = averageAndDeviation[1];
		System.out.println("meanClosestDistance��"+meanClosestDistance);
		System.out.println("ClosestDistanceDeviation��"+ClosestDistanceDeviation);
		int size = glist.size();
		cellNextCells = new ArrayList[size];
		ArrayList<Integer>[] result = new ArrayList[size];
		for (int i = 0; i < size; i++) {
			result[i] = new ArrayList<Integer>();
			Group g = cellGroup[hour].get(i);
			result[i].addAll(calNextPossibility(g, glist,groupStart.getCenterCell(),meanClosestDistance,ClosestDistanceDeviation));
			System.out.print(i+"___");
			for(int in:result[i])
				System.out.print(in+"---");
			System.out.println();
		}
		
		return result;
	}

	// ����groupStartIndex��groupNextIndex����������������group��ת��Ȩ�أ����Ƕȡ����롢���ڸ���֮�ͣ����ص�ÿ����ĸ���ֵ
	public ArrayList<Integer> calNextPossibility(Group start,ArrayList<Group> glist,String homeCenter,
												double meanClosestDistance,double ClosestDistanceDeviation) {
		int size = glist.size();
		double np[] = new double[size];
		int maxI = 0;
		double maxValue = 0;
		String gStartCellCenter = start.getCenterCell();

		for (int i = 0; i < size; i++) {
			np[i] = 0;// ��û����
			// ��������������Լ��������ʡ��Լ��������´��������ʣ�����Ҫ����������ʲô
			Group gi = glist.get(i);
			String giCellCenter = gi.getCenterCell();
			if (giCellCenter.equals(gStartCellCenter)) {
				np[i] = 0;// gi.getPossibility();
				continue;
			}
			// �Ƕȣ����ؼн�cosֵ
			
			double cos = 0;
			String groupNextID = getNextGroupCenter(start);
			String gnextCellCenter = groupNextID;
//			int groupNextIndex = Integer.parseInt(groupNextID.split("_")[1]);
//			String gnextCellCenter = glist.get(groupNextIndex).getCenterCell();//������ȥ��
			Location a = matrixForGroup.stringToLoc(gStartCellCenter);
			Location b = matrixForGroup.stringToLoc(giCellCenter);
			Location c = matrixForGroup.stringToLoc(gnextCellCenter);
			//����home�뵱ǰ�������  ��  ��ǰ������������һ��֮������ �ļнǣ�������135�ȣ����˷����Ϊǰ��
			//���䲹��С��45��
			if(!homeCenter.equals(gStartCellCenter)){
				Location home = matrixForGroup.stringToLoc(homeCenter);
				double cosHome = calCosAngel(a, c, home);
				if(cosHome>-0.5){					
					cos = calCosAngel(a, b, c);
				}
				else{
//					System.out.println(cosHome+"   cosHome");
					cos = -1*calCosAngel(a, b, home);
				} 					
//				System.out.println(cos+"   aaaaaaaaaa");
			}
			else 
				cos = calCosAngel(a, b, c);
			double cos2 = cos;
			if(cos>=0){
				cos2=0.5+cos/3;
				cos = Math.exp(0.3*cos);					
			}		

			// ���룬���ĵ��ľ���
			double distance = 0;
			int prei = cellIndexMap.get(gStartCellCenter);
			int nexti = cellIndexMap.get(giCellCenter);
			distance = distanceBetweenCells[prei][nexti];
			double distance2 = normalDistribution(meanClosestDistance, ClosestDistanceDeviation, distance);

			//λ�ô��ڸ���
//			double possibility=0;
//			possibility = gi.getPossibility();
//			System.out.println(i+"_"+distance2+"_"+cos);
//			double pi = (cos*10) + ((12000)/distance)+ possibility*10;//����Ȩ�����		
			double pi = cos * distance2;
			np[i] = pi;
		}
		// ͳ�ƴ���ƽ�����ʵľ���,> max*0.8�ı�ѡ��
		ArrayList<Integer> result = new ArrayList<Integer>();
		double averageScore = 0;
		double max=0;
		for (int i = 0; i < size; i++) {
			averageScore += np[i];
			if(np[i]>max)
				max = np[i];
		}
		averageScore = averageScore / size;
		for (int i = 0; i < size; i++) {
			if (np[i] > max*0.8)
				result.add(i);
		}
		
		return result;
	}
	
	//����ÿ��������������������ƽ��ֵ and ���group��group��̾������еķ��
	public double[] calAverageClosestDistanceBetweenGroups(ArrayList<Group> glist){
		double result[] = new double[2];//��һ����ƽ�������ڶ����Ƿ���
		double averageDistance=0;
		double deviation = 0;
		int size = glist.size();
		double[] allClosestDistance = new double[size];
		for (int i = 0; i < size; i++) {
			String giCellCenter = glist.get(i).getCenterCell();
			int groupCenteri = cellIndexMap.get(giCellCenter);
			//��þ��ൽ�����������̾���
			double closest = Double.MAX_VALUE;
			for (int j = 0; j < size; j++) {
				if(i==j)
					continue;
				String gjCellCenter = glist.get(j).getCenterCell();
				int groupCenterj = cellIndexMap.get(gjCellCenter);
				double distance = distanceBetweenCells[groupCenteri][groupCenterj];
				if(distance!=0 && distance<closest)
					closest = distance;
			}
			allClosestDistance[i] = closest;
			averageDistance += closest;
		}
		averageDistance/=size;
		//���㷽��
		for(int i=0;i<size;i++){
			deviation += Math.pow((allClosestDistance[i] - averageDistance),2);
		}
		deviation /= size;
		result[0] = averageDistance;
		result[1] = deviation;
		return result;
	}

	//������ֵ�������Լ�x������x�ĸ���
	public double normalDistribution(double mean,double deviation,double x){
		double possiblity = 0;
		double expValue = (-0.5*(1/deviation))*(Math.pow((x-mean), 2));
		possiblity = Math.exp(expValue) *(1/Math.sqrt(2*Math.PI*deviation));
		return possiblity;
	}
	
	// ����һ��group������������ת�Ƶ���һ��group���Ļ�վ
	public String getNextGroupCenter(Group group) {
		String center;
		String preID = group.getGourpID();
		String temp[] = preID.split("_");
		int hour = Integer.parseInt(temp[0]);
		int index = Integer.parseInt(temp[1]);

		int maxi = 0;
		double maxp = 0;
		for (int i = 0; i < cellSize; i++) {
			double value = groupTransferByHour[hour][index][i];
			if (value > maxp && i != index) {// �ų��Լ�
				maxi = i;
				maxp = value;
			}
		}
		if (maxp != 0) { // �����û��ת�Ƶ�����group�����򷵻��Լ���
			Group gtarget = cellGroup[hour].get(maxi);
			center = gtarget.getCenterCell();
		} else {
			center = group.getCenterCell();
		}
		return center;
	}

	// ��֪���㣬��н�cos,(-1,1)
	public static double calCosAngel(Location cen, Location first,
			Location second) {
		double dx1, dx2, dy1, dy2;
		double result;
		dx1 = first.x - cen.x;
		dy1 = first.y - cen.y;
		dx2 = second.x - cen.x;
		dy2 = second.y - cen.y;
		double c = Math.sqrt(dx1 * dx1 + dy1 * dy1)
				* Math.sqrt(dx2 * dx2 + dy2 * dy2);
		if (c == 0)
			return -1;
		result = (dx1 * dx2 + dy1 * dy2) / c;
		// angle = (double)Math.acos((dx1*dx2 + dy1*dy2)/c);

		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Stack<Integer> stack = new Stack<Integer>();
		// stack.push(1);
		// stack.push(2);
		// stack.push(3);
		// Object[] a = stack.toArray();
		// for(int i=0;i<a.length;i++)
		// System.out.println((Integer)a[i]);
		MatrixForGroup matrixGroup = null;
		//99249788048010590
		//99249764168730152
		//99702516988779459
		String device = "99249788048010590";
		int hour=7;///----------------------------------------------------------atttention!
		try {
			matrixGroup = new MatrixForGroup(device);// ������Ϣ
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FindTrajectoryCombine f = new FindTrajectoryCombine(matrixGroup);
		ArrayList<ArrayList<Integer>> allpath = f.dfsForAllTrajectories(hour);
		GeneticWay gen = new GeneticWay(matrixGroup);
		//�����ѡ·��ƽ������----------
		int averageSize = 5;
		int size = allpath.size();
		int sumSize = 0;
		for(ArrayList<Integer> l : allpath)
			sumSize+=l.size();
		averageSize = sumSize/size;
		//------------
		for (ArrayList<Integer> l : allpath) {
			for (int i : l)
				System.out.print(i + "__");
			ArrayList<String> temp = new ArrayList<String>();
			for(int a:l)
				temp.add(a+"");
//			System.out.println(f.endValueGroup[hour]);
			if(temp.contains(f.endValueGroup[hour].split("_")[1])){
//				double score = gen.optimiticScore(temp, hour);
				double score = gen.scoreByHmm(temp, hour,averageSize);
				System.out.println("---"+score);
			}

		}
	}

}
