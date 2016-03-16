package com.gen.locAndTrajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.gen.trajectory.*;

import de.fhpotsdam.unfolding.geo.Location;

//根据三个因素选择下一跳候选点：
//1.角度（与最大概率转移的group连线对比）；
//2.距离（聚类中最重要点的之间的距离）；
public class FindTrajectoryCombine {

	HashMap<String, String> cellToGroup = new HashMap<String, String>();// 记录基站对应聚类的index, cell_hour,idIndex
	ArrayList<Group> cellGroup[] = new ArrayList[24];// 记录每小时聚类
	double groupTransferByHour[][][];// 以聚类为单位，每小时转移矩阵。记录转移概率（聚类中所有点记录之和）
	public double distanceBetweenCells[][];// 记录不同基站间的距离
	Map<String, String> cellToCoordinate = new HashMap<String, String>();// 基站对应经纬度
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();// 记录所有基站与index的对应关系，对应differentcell中

	// 每个聚类出现的概率，记录在group类中的possibility中
	int cellSize = 0;// 该设备所经过的所有不同基站数
	MatrixForGroup matrixForGroup;
	String[] startGroup = new String[24];// 每小时最频繁的起始点，返回groupID
	String[] endGroup = new String[24];// 每小时最频繁的终止点，返回groupID
	ArrayList<String> path[] = new ArrayList[24];// 记录每小时提取的路径，cellID链

	String startValueGroup[];// 给定grouplist,返回每小时最常起始的groupID（数组）
	String endValueGroup[];// hour+index

	ArrayList<Integer>[] cellNextCells;// 记录每个聚类的候选点聚类 记录index
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
		startValueGroup = matrixForGroup.getStartGroup();// 起始位置
		endValueGroup = matrixForGroup.getEndGroup();// 终止位置

		for (int i = 0; i < startGroup.length; i++)
			System.out.println(startGroup[i] + "_" + endGroup[i]);

		// for(int hour=0;hour<24;hour++){
		// path[hour] = new ArrayList<String>();
		// path[hour].addAll(calRegularPath(hour));
		// }
	}

	// DFS 深搜，遍历所有路径
	public ArrayList<ArrayList<Integer>> dfsForAllTrajectories(int hour) {
		ArrayList<ArrayList<Integer>> allPath = new ArrayList<ArrayList<Integer>>();
		// 得到所有点的下一跳选择
		cellNextCells = calCellNextCells(hour);// 记录每个聚类的候选点聚类 记录index

		// 得到起始点和终止点
		String start = startValueGroup[hour];// hour+index
		String end = endValueGroup[hour];
		int startIndex = Integer.parseInt(start.split("_")[1]);
		int endIndex = Integer.parseInt(end.split("_")[1]);
//System.out.println("aaakaishia a a "+startIndex+" "+endIndex);
		//保存完整路径的stack
		Stack<ArrayList<Integer>> pathStack = new Stack<ArrayList<Integer>>();
		ArrayList<Integer> t = new ArrayList<Integer>();
		t.add(startIndex);
		pathStack.push(t);
		
//		Stack<Integer> stack = new Stack<Integer>();
//		stack.push(startIndex);

		// DFS
		// 结束条件：1、到达end聚类； 2、木有下一跳； 3、下一跳已经出现在路径中；
		while (!pathStack.isEmpty()) {
			ArrayList<Integer> pathNow = new ArrayList<Integer>();
			pathNow.addAll(pathStack.pop());
			int now = pathNow.get(pathNow.size()-1);
			// 是否到达end节点
			if (now == endIndex) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.addAll(pathNow);
				allPath.add(temp);
				continue;
			}
			ArrayList<Integer> nexts = cellNextCells[now];

			for (int p : nexts){
//				stack.push(p);
				//有环
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
	// 计算每个聚类的 候选聚类 记录index, 赋值cellNextCells
	public ArrayList<Integer>[] calCellNextCells(int hour) {

		String startGroupID = startGroup[hour];//该小时最经常的起始
		int groupStartIndex = Integer.parseInt(startGroupID.split("_")[1]);
		Group groupStart = cellGroup[hour].get(groupStartIndex);
		
		ArrayList<Group> glist = cellGroup[hour];
		//计算每个聚类距离最近聚类距离的 平均值 和 方差
		double averageAndDeviation[] = new double[2];//第一个是平均数，第二个是方差
		averageAndDeviation = calAverageClosestDistanceBetweenGroups(glist);
		double meanClosestDistance=0, ClosestDistanceDeviation = 0;
		meanClosestDistance = averageAndDeviation[0];
		ClosestDistanceDeviation = averageAndDeviation[1];
		System.out.println("meanClosestDistance："+meanClosestDistance);
		System.out.println("ClosestDistanceDeviation："+ClosestDistanceDeviation);
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

	// 给定groupStartIndex和groupNextIndex，计算链表中所有group的转移权重，即角度、距离、存在概率之和；返回到每个点的概率值
	public ArrayList<Integer> calNextPossibility(Group start,ArrayList<Group> glist,String homeCenter,
												double meanClosestDistance,double ClosestDistanceDeviation) {
		int size = glist.size();
		double np[] = new double[size];
		int maxI = 0;
		double maxValue = 0;
		String gStartCellCenter = start.getCenterCell();

		for (int i = 0; i < size; i++) {
			np[i] = 0;// 并没有用
			// 两种特殊情况，自己和最大概率。自己，就如下处理；最大概率，不需要处理。。。搞什么
			Group gi = glist.get(i);
			String giCellCenter = gi.getCenterCell();
			if (giCellCenter.equals(gStartCellCenter)) {
				np[i] = 0;// gi.getPossibility();
				continue;
			}
			// 角度，返回夹角cos值
			
			double cos = 0;
			String groupNextID = getNextGroupCenter(start);
			String gnextCellCenter = groupNextID;
//			int groupNextIndex = Integer.parseInt(groupNextID.split("_")[1]);
//			String gnextCellCenter = glist.get(groupNextIndex).getCenterCell();//最大概率去向
			Location a = matrixForGroup.stringToLoc(gStartCellCenter);
			Location b = matrixForGroup.stringToLoc(giCellCenter);
			Location c = matrixForGroup.stringToLoc(gnextCellCenter);
			//计算home与当前点的连线  与  当前点与最大概率下一跳之间连线 的夹角，若大于135度，则标杆方向改为前者
			//即其补角小于45度
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

			// 距离，中心点间的距离
			double distance = 0;
			int prei = cellIndexMap.get(gStartCellCenter);
			int nexti = cellIndexMap.get(giCellCenter);
			distance = distanceBetweenCells[prei][nexti];
			double distance2 = normalDistribution(meanClosestDistance, ClosestDistanceDeviation, distance);

			//位置存在概率
//			double possibility=0;
//			possibility = gi.getPossibility();
//			System.out.println(i+"_"+distance2+"_"+cos);
//			double pi = (cos*10) + ((12000)/distance)+ possibility*10;//各种权重相加		
			double pi = cos * distance2;
			np[i] = pi;
		}
		// 统计大于平均概率的聚类,> max*0.8的被选中
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
	
	//计算每个聚类距离最近聚类距离的平均值 and 方差（group到group最短距离序列的方差）
	public double[] calAverageClosestDistanceBetweenGroups(ArrayList<Group> glist){
		double result[] = new double[2];//第一个是平均数，第二个是方差
		double averageDistance=0;
		double deviation = 0;
		int size = glist.size();
		double[] allClosestDistance = new double[size];
		for (int i = 0; i < size; i++) {
			String giCellCenter = glist.get(i).getCenterCell();
			int groupCenteri = cellIndexMap.get(giCellCenter);
			//求该聚类到其他聚类的最短距离
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
		//计算方差
		for(int i=0;i<size;i++){
			deviation += Math.pow((allClosestDistance[i] - averageDistance),2);
		}
		deviation /= size;
		result[0] = averageDistance;
		result[1] = deviation;
		return result;
	}

	//给定均值、方差以及x，返回x的概率
	public double normalDistribution(double mean,double deviation,double x){
		double possiblity = 0;
		double expValue = (-0.5*(1/deviation))*(Math.pow((x-mean), 2));
		possiblity = Math.exp(expValue) *(1/Math.sqrt(2*Math.PI*deviation));
		return possiblity;
	}
	
	// 给定一个group，返回最大概率转移的下一个group中心基站
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
			if (value > maxp && i != index) {// 排除自己
				maxi = i;
				maxp = value;
			}
		}
		if (maxp != 0) { // 如果并没有转移到其他group过，则返回自己。
			Group gtarget = cellGroup[hour].get(maxi);
			center = gtarget.getCenterCell();
		} else {
			center = group.getCenterCell();
		}
		return center;
	}

	// 已知三点，求夹角cos,(-1,1)
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
			matrixGroup = new MatrixForGroup(device);// 聚类信息
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FindTrajectoryCombine f = new FindTrajectoryCombine(matrixGroup);
		ArrayList<ArrayList<Integer>> allpath = f.dfsForAllTrajectories(hour);
		GeneticWay gen = new GeneticWay(matrixGroup);
		//计算候选路径平均长度----------
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
