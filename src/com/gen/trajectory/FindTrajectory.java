package com.gen.trajectory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.fhpotsdam.unfolding.geo.Location;

	//根据三个因素选择路径：
	//1.角度（与最大概率转移的group连线对比）；
	//2.距离（聚类中最重要点的之间的距离）；
	//3.出现概率（以聚类为单位）
public class FindTrajectory {

	HashMap<String, String> cellToGroup = new HashMap<String, String>();//记录基站对应聚类的index,  cell_hour idIndex
	ArrayList<Group> cellGroup[] = new ArrayList[24];//记录每小时聚类
	double groupTransferByHour[][][];//以聚类为单位，每小时转移矩阵。记录转移概率（聚类中所有点记录之和）
	public double distanceBetweenCells[][];//记录不同基站间的距离
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//基站对应经纬度
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//记录所有基站与index的对应关系，对应differentcell中
	
	//每个聚类出现的概率，记录在group类中的possibility中
	int cellSize = 0;//该设备所经过的所有不同基站数
	MatrixForGroup matrixForGroup;
	String[] startGroup= new String[24];//每小时最频繁的起始点，返回groupID
	String[] endGroup= new String[24];//每小时最频繁的终止点，返回groupID
	ArrayList<String> path[] = new ArrayList[24];//记录每小时提取的路径，cellID链
	
	String startValueGroup[];//给定grouplist,返回每小时最常起始的groupID（数组）
	String endValueGroup[];//hour+index
	
	GeneticWay genetic;
	
	public FindTrajectory(MatrixForGroup matrixForGroup){
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
		startValueGroup = matrixForGroup.getStartGroup();//起始位置
		endValueGroup = matrixForGroup.getEndGroup();//终止位置
		
		for(int i=0;i<startGroup.length;i++)
			System.out.println(startGroup[i]+"_"+endGroup[i]);
		
		for(int hour=0;hour<24;hour++){
			path[hour] = new ArrayList<String>();
			path[hour].addAll(calRegularPath(hour));
		}
	}
	public static void main(String[] args) throws ParseException {
//		// TODO Auto-generated method stub
//		Location a = new Location(0,0);	
//		Location b = new Location(1,1);	
//		Location c = new Location(1,0);	
//		double x = calCosAngel(a, b, c);
//		System.out.println(x);
		
		Date date=new Date();
		DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time=format.format(date);
		System.out.println("开始时间："+time);
		
		//99249788048010590
		//99249764168730152
		String device = "99249764168730152";
		MatrixForGroup matrixGroup= new MatrixForGroup(device);
		FindTrajectory find = new FindTrajectory(matrixGroup);
		int hour=7;
		ArrayList<String> path = find.calRegularPath(hour);
		for(String s:path)
			System.out.println(s);
		find.getPathByTraverse();
		
		Date date1=new Date();
		DateFormat format1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time1=format1.format(date1);
		System.out.println("结束时间："+time1);
	}
	public ArrayList<String> getRegularPath(int hour){
		return path[hour];
	}
	
	//给定Group链表，返回以起始点为起始的路径(GroupID链表)
	public ArrayList<String> calRegularPath(int hour){
		ArrayList<Group> glist = cellGroup[hour];
		
		String startGroupID = startGroup[hour];//该小时最经常的起始
		int groupStartIndex = Integer.parseInt(startGroupID.split("_")[1]);
		Group groupStart = cellGroup[hour].get(groupStartIndex);
//		String groupNextID = getNextGroupCenter(groupStart);
//		int groupNextIndex = Integer.parseInt(groupNextID.split("_")[1]);
		//
		
		String tempID = groupStart.getCenterCell();//group中心基站
		ArrayList<Integer> pathIndex = new ArrayList<Integer>();
		pathIndex.add(groupStartIndex);
		
		ArrayList<String> pathThrough = new ArrayList<String>();
		pathThrough.add(tempID);
		while(true){
			String gindex = cellToGroup.get(tempID+"_"+hour);
			Group g = cellGroup[hour].get(Integer.parseInt(gindex));
			tempID = calNextPossibility(g, glist,groupStart.getCenterCell());
			pathIndex.add(Integer.parseInt(cellToGroup.get(tempID+"_"+hour)));
			if(pathThrough.contains(tempID))
				break;
			else
				pathThrough.add(tempID);
		}
		//最佳路径位置序列
//		ArrayList<String> path = new ArrayList<String>();
//		System.out.println("Path!:");
//		for(int in:pathIndex){
//			System.out.print(in+"_");
//			path.add(in+"");
//		}
			
//		System.out.println();
//		double value = genetic.optimiticScore(path, hour);
//		System.out.println("最佳路径误差值："+value);
		return pathThrough;
	}
	
	//给定groupStartIndex和groupNextIndex，计算链表中所有group的转移权重，即角度、距离、存在概率之和；返回cell
	public String calNextPossibility(Group start,ArrayList<Group> glist,String homeCenter){
		int size = glist.size();
		double np[] = new double[size];
		int maxI = 0;
		double maxValue = 0;
		String gStartCellCenter = start.getCenterCell();
		
		for(int i=0;i<size;i++){
			np[i]=0;//并没有用
			//两种特殊情况，自己和最大概率。自己，就如下处理；最大概率，不需要处理。。。搞什么
			Group gi = glist.get(i);
			String giCellCenter = gi.getCenterCell();
			if(giCellCenter.equals(gStartCellCenter)){
				np[i] = 0.0000001;//gi.getPossibility();
				continue;
			}
			//角度，返回夹角cos值
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
			if(cos>=0)
				cos=0.5+cos/3;
			//距离，中心点间的距离
			double distance=0;
			int prei = cellIndexMap.get(gStartCellCenter);
			int nexti = cellIndexMap.get(giCellCenter);
			distance = distanceBetweenCells[prei][nexti];
			
			//位置存在概率
			double possibility=0;
			possibility = gi.getPossibility();
//			if(possibility>0.3)
//			System.out.println("cos:"+cos+"___distance:"+distance+"___possibility:"+possibility);
//			double pi = (cos+0.5) ;//* ((5000)/distance) + possibility*10;//各种权重相加
			double pi = (cos*10) + ((8000)/distance);// + possibility*10;//各种权重相加
			np[i] = pi;
		}
		for(int i=0;i<size;i++){
			if(maxValue<np[i]){
				maxValue = np[i];
				maxI = i;
			}
		}
		String result = glist.get(maxI).getCenterCell();
		return result;
	}
	
	//给定一个group，返回最大概率转移的下一个group中心基站
		public String getNextGroupCenter(Group group){
			String center;
			String preID = group.getGourpID();
			String temp[] = preID.split("_");
			int hour = Integer.parseInt(temp[0]);
			int index = Integer.parseInt(temp[1]);
			
			int maxi = 0;
			double maxp =0;
			for(int i=0;i<cellSize;i++){
				double value = groupTransferByHour[hour][index][i];
				if(value>maxp && i!=index){//排除自己
					maxi = i;
					maxp = value;
				}
			}
			if(maxp!=0){//如果并没有转移到其他group过，则返回自己。
				Group gtarget = cellGroup[hour].get(maxi);
				center = gtarget.getCenterCell();
			}
			else {
				center = group.getCenterCell();
			}
			return center;
		}
		
	//已知三点，求夹角cos,(-1,1)
	public static double calCosAngel(Location cen,Location first,Location second){
		double dx1,dx2,dy1,dy2;
		double result;
		dx1 = first.x-cen.x;
		dy1 = first.y - cen.y;
		dx2 = second.x - cen.x;
		dy2 = second.y - cen.y;
		double c = Math.sqrt(dx1*dx1+dy1*dy1) * Math.sqrt(dx2*dx2 + dy2*dy2);
		if(c==0)
			return -1;
		result = (dx1*dx2 + dy1*dy2)/c;
//		angle = (double)Math.acos((dx1*dx2 + dy1*dy2)/c);
		
		return result;
	}

	//遍历所有可能，得到所有得分
	public ArrayList<String> getPathByTraverse(){
		int hour=7;
		
		String[] goodPath2 = {"6","14","15","11","9","4","10","13","12","5","3"};
		ArrayList<String> temp2 = new ArrayList<String>();
		for(String s:goodPath2)
			temp2.add(s);
		double valuet2 = genetic.optimiticScore(temp2, hour);
		System.out.println("期待最佳路径误差值："+valuet2);
		
//		String[] goodPath = {"0","1","2","3","4","5","6","7","8","9","10","11","12"};
		String[] goodPath = {"0","2","6","8","9","11","13","14","15","16","17","18","19","21"};
		
		ArrayList<String> temp = new ArrayList<String>();
		for(String s:goodPath)
			temp.add(s);
		double valuet = genetic.optimiticScore(temp, hour);
		System.out.println("奇怪最佳路径误差值："+valuet);
		
		int averageLength = genetic.getAverageGroupNumOfPaths(hour);
		System.out.println("sample平均聚类个数："+averageLength);
		//-----------------------------------------------------------------------------
		//生成该小时聚类数组
		ArrayList<Group> groupByHour = cellGroup[hour];
		String start = startValueGroup[hour];
		String end = endValueGroup[hour];
		
		//去掉start和end
		ArrayList<Group> groupByHourWithoutSE = new ArrayList<Group>();
		for(Group g:groupByHour){
			if(g.getGourpID().equals(start) || g.getGourpID().equals(end))
				continue;
			groupByHourWithoutSE.add(g);
		}
		//得到groupID链表
		int groupSize=groupByHourWithoutSE.size();//保留个数，除掉start和end之后的个数	
		String[] groupByHourIDs = new String[groupSize];
		for(int i=0;i<groupSize;i++){
			groupByHourIDs[i] = groupByHourWithoutSE.get(i).getGourpID().split("_")[1];
		}
		
		//得到所有聚类组合
		ArrayList<ArrayList<String>> allPathsCTemp = Traverse.getAllCombinations(groupByHourIDs);
		System.out.println("全部组合个数："+allPathsCTemp.size()+" 聚类个数："+groupByHourIDs.length);
		
		//过滤组合
		ArrayList<ArrayList<String>> allPathsC = new ArrayList<ArrayList<String>>();
 		for(ArrayList<String> list:allPathsCTemp){
 			if(list.size()>=averageLength && list.size()<averageLength+4)   //只保留个数位于 平均长度 到 平均+4
 				allPathsC.add(list);
 		}
		
// 		allPathsC.clear();
//// 		String[] goodPath3 = {"14","15","11","9","4","10","13","12","5"};
// 		.0
//		ArrayList<String> ltemp = new ArrayList<String>();
// 		for(int i=0;i<goodPath3.length;i++)
//			ltemp.add(goodPath3[i]);
// 		allPathsC.add(ltemp);
 		System.out.println("剩余组合个数："+allPathsC.size()+" 聚类个数："+groupByHourIDs.length);

		//得到所有组合的排序序列
// 		ArrayList<ArrayList<String>> allPathTemp = Traverse.combine(allPathsC);
// 		System.out.println("全部排序个数："+allPathTemp.size());
 		ArrayList<ArrayList<String>> allPathTemp = allPathsC;
 		
 		//加上起始和终止节点
 		ArrayList<ArrayList<String>> allPaths = new ArrayList<ArrayList<String>>();
 		for(ArrayList<String> list:allPathTemp){
 			ArrayList<String> l = new ArrayList<String>(); 
 			l.add(start.split("_")[1]);
 			l.addAll(list);
 			l.add(end.split("_")[1]);
 			allPaths.add(l);
// 			for(String s:l)
// 				System.out.print(s+" ");
// 			System.out.println();
 		}
 		
		int size = allPaths.size();
		double score[] = new double[size];
		for(int i=0;i<size;i++)
			score[i] = Double.MAX_VALUE;
		//-----------------------------------------------------------------------------
		//得到所有路径的误差值
		
		int count=0;
		for(int i=0;i<size;i++){
			ArrayList<String> path = allPaths.get(i);
			double value = genetic.optimiticScore(path, hour);
//			for(String s:path)
//				System.out.print(s+"-");
//			System.out.println(size+"_"+i+"---"+value);
			score[i] = value;
		count++;
		if(count%50000==0)
			System.out.println(count);
		}
		//选出误差值最小路径
		int minIndex = 0;
		double minValue = Double.MAX_VALUE;
		for(int i=0;i<size;i++){
			if(minValue>score[i]){
				minIndex = i;
				minValue = score[i];
			}
				
		}
		ArrayList<String> minPath = allPaths.get(minIndex);
		//groupID变为Center cell
		ArrayList<String> minPathCell = new ArrayList<String>();
		for(String s:minPath){
			int i=Integer.parseInt(s);
			Group g = cellGroup[hour].get(i);
			minPathCell.add(g.getCenterCell());
		}
//		for(String s:minPath)
//			System.out.print(s+"-");
//		System.out.println("_"+minValue);
		
		int countbest=0;
		for(int i=0;i<size;i++){
			if(score[i]==minValue){
				ArrayList<String> bests = allPaths.get(i);
				for(String s:bests)
					System.out.print(s+"-");
				System.out.println("_"+minValue);
				countbest++;
			}
		}
		System.out.println(countbest);
		return minPathCell;
	}
	
	
}
