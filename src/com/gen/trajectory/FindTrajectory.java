package com.gen.trajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import de.fhpotsdam.unfolding.geo.Location;

	//������������ѡ��·����
	//1.�Ƕȣ���������ת�Ƶ�group���߶Աȣ���
	//2.���루����������Ҫ���֮��ľ��룩��
	//3.���ָ��ʣ��Ծ���Ϊ��λ��
public class FindTrajectory {

	HashMap<String, String> cellToGroup = new HashMap<String, String>();//��¼��վ��Ӧ�����index
	ArrayList<Group> cellGroup[] = new ArrayList[24];//��¼ÿСʱ����
	double groupTransferByHour[][][];//�Ծ���Ϊ��λ��ÿСʱת�ƾ��󡣼�¼ת�Ƹ��ʣ����������е��¼֮�ͣ�
	public double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//��վ��Ӧ��γ��
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//��¼���л�վ��index�Ķ�Ӧ��ϵ����Ӧdifferentcell��
	
	//ÿ��������ֵĸ��ʣ���¼��group���е�possibility��
	int cellSize = 0;//���豸�����������в�ͬ��վ��
	MatrixForGroup matrixForGroup;
	String[] startGroup= new String[24];//ÿСʱ��Ƶ������ʼ�㣬����groupID
	ArrayList<String> path[] = new ArrayList[24];//��¼ÿСʱ��ȡ��·����cellID��
	
	public FindTrajectory(MatrixForGroup matrixForGroup){
		this.matrixForGroup = matrixForGroup;
		cellToGroup = matrixForGroup.getCellToGroup();
		this.cellGroup = matrixForGroup.calAllGroup();
		this.groupTransferByHour = matrixForGroup.getGroupTransferByHour();
		this.distanceBetweenCells = matrixForGroup.getDistanceBetweenCells();
		cellSize = matrixForGroup.getSize();
		startGroup = matrixForGroup.getStartGroup();
		cellToCoordinate = matrixForGroup.getCellToCoordinate();
		cellIndexMap = matrixForGroup.getCellIndexMap();
		
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
		//99249788048010590
		//99249764168730152
		String device = "99249788048010590";
		MatrixForGroup matrixGroup= new MatrixForGroup(device);
		FindTrajectory find = new FindTrajectory(matrixGroup);
		int hour=7;
		ArrayList<String> path = find.calRegularPath(hour);
		for(String s:path)
			System.out.println(s);
	}
	public ArrayList<String> getRegularPath(int hour){
		return path[hour];
	}
	
	//����Group��������������ʼ��Ϊ��ʼ��·��(GroupID����)
	public ArrayList<String> calRegularPath(int hour){
		ArrayList<Group> glist = cellGroup[hour];
		
		String startGroupID = startGroup[hour];//��Сʱ�������ʼ
		int groupStartIndex = Integer.parseInt(startGroupID.split("_")[1]);
		Group groupStart = cellGroup[hour].get(groupStartIndex);
//		String groupNextID = getNextGroupCenter(groupStart);
//		int groupNextIndex = Integer.parseInt(groupNextID.split("_")[1]);
		//
		
		String tempID = groupStart.getCenterCell();//group���Ļ�վ
		ArrayList<String> pathThrough = new ArrayList<String>();
		pathThrough.add(tempID);
		while(true){
			String gindex = cellToGroup.get(tempID+"_"+hour);
			Group g = cellGroup[hour].get(Integer.parseInt(gindex));
			tempID = calNextPossibility(g, glist,groupStart.getCenterCell());
			if(pathThrough.contains(tempID))
				break;
			else
				pathThrough.add(tempID);
		}
		return pathThrough;
	}
	
	//����groupStartIndex��groupNextIndex����������������group��ת��Ȩ�أ����Ƕȡ����롢���ڸ���֮�ͣ�����cell
	public String calNextPossibility(Group start,ArrayList<Group> glist,String homeCenter){
		int size = glist.size();
		double np[] = new double[size];
		int maxI = 0;
		double maxValue = 0;
		String gStartCellCenter = start.getCenterCell();
		
		for(int i=0;i<size;i++){
			np[i]=0;//��û����
			//��������������Լ��������ʡ��Լ��������´����������ʣ�����Ҫ������������ʲô
			Group gi = glist.get(i);
			String giCellCenter = gi.getCenterCell();
			if(giCellCenter.equals(gStartCellCenter)){
				np[i] = 0.0000001;//gi.getPossibility();
				continue;
			}
			//�Ƕȣ����ؼн�cosֵ
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
					System.out.println(cosHome+"   cosHome");
					cos = -1*calCosAngel(a, b, home);
				} 					
				System.out.println(cos+"   aaaaaaaaaa");
			}
			else 
				cos = calCosAngel(a, b, c);
			if(cos>=0)
				cos=0.5+cos/3;
			//���룬���ĵ��ľ���
			double distance=0;
			int prei = cellIndexMap.get(gStartCellCenter);
			int nexti = cellIndexMap.get(giCellCenter);
			distance = distanceBetweenCells[prei][nexti];
			
			//λ�ô��ڸ���
			double possibility=0;
			possibility = gi.getPossibility();
//			if(possibility>0.3)
//			System.out.println("cos:"+cos+"___distance:"+distance+"___possibility:"+possibility);
//			double pi = (cos+0.5) ;//* ((5000)/distance) + possibility*10;//����Ȩ�����
			double pi = (cos*10) + ((8000)/distance);// + possibility*10;//����Ȩ�����
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
	
	//����һ��group������������ת�Ƶ���һ��group���Ļ�վ
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
				if(value>maxp && i!=index){//�ų��Լ�
					maxi = i;
					maxp = value;
				}
			}
			if(maxp!=0){//�����û��ת�Ƶ�����group�����򷵻��Լ���
				Group gtarget = cellGroup[hour].get(maxi);
				center = gtarget.getCenterCell();
			}
			else {
				center = group.getCenterCell();
			}
			return center;
		}
		
	//��֪���㣬��н�cos,(-1,1)
	public static double calCosAngel(Location cen,Location first,Location second){
		double dx1,dx2,dy1,dy2;
		double result;
		dx1 = first.x-cen.x;
		dy1 = first.y - cen.y;
		dx2 = second.x - cen.x;
		dy2 = second.y - cen.y;
		double c = (double)Math.sqrt(dx1*dx1+dy1*dy1) * (double)Math.sqrt(dx2*dx2 + dy2*dy2);
		if(c==0)
			return -1;
		result = (dx1*dx2 + dy1*dy2)/c;
//		angle = (double)Math.acos((dx1*dx2 + dy1*dy2)/c);
		
		return result;
	}

}