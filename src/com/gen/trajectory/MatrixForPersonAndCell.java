package com.gen.trajectory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MatrixForPersonAndCell {

	QueryTrajectory queryTrajectory;
	String deviceID ="";
	public int daySize ;//����queryTrajectory
	@SuppressWarnings("unchecked")
	ArrayList<String> 	cellAll[][];//Ŀǰ���������20131205-20131231 ��27��
	//cellAllCut��������ת�ƾ���
	ArrayList<String> 	cellAllCut[][];//Ŀǰ���������20131205-20131231 ��27�죬ȥ����������ʼ�����ֹ��� ����
	ArrayList<String> differentCell = new ArrayList<String>();//���в�ͬ�Ļ�վ
	Map<String,String> cellToCoordinate = new HashMap<String,String>();//��¼���л�վ������Ķ�Ӧ��ϵ
	
	ArrayList<CellInfo> personLoc[] = new ArrayList[24];//������洢λ����Ϣ
	double locations[][];//λ�þ��󣬼�¼���ָ���
	double transferAllDay[][];//ȫ��ת�ƾ��󡣼�¼ת�Ƹ���
	double transferByHour[][][];//ÿСʱת�ƾ��󡣼�¼ת�Ƹ���
	HashMap<String, Integer> cellIndexMap = new HashMap<String, Integer>();//��վID�Ծ������
	int size = 0;//��ͬ��վ�ĸ���
	double distanceBetweenCells[][];//��¼��ͬ��վ��ľ���
	
	public MatrixForPersonAndCell(String deviceID) throws ParseException{
		this.deviceID = deviceID;
		queryTrajectory = new QueryTrajectory(deviceID);
		daySize = queryTrajectory.dayNum;
		cellAllCut = new ArrayList[daySize][25];
		cellAll = new ArrayList[daySize][25];
		
		queryTrajectory.getAllTrajectory();
		cellAll = queryTrajectory.getCellAll();
		HashSet<String> differentCelltemp = queryTrajectory.getDifferentCell();
		cellToCoordinate = QueryTrajectory.getMap();
		
		differentCell.addAll(differentCelltemp);
		size = differentCell.size();
//		cellAllCut = queryTrajectory.getCellAll();
		initialMatrix();
		
		cutCellAll();		//����cellAllCut��ȥ����������ʼ�����ֹ��� ����
		fillTransferAllDay();
		fillTranferByHour();
		fillLocations();
		fillDistance();
	}
	
	//��ʼ������
	public void initialMatrix(){
		locations = new double[24][size];
		transferAllDay = new double[size][size];
		transferByHour = new double[24][size][size];
		distanceBetweenCells = new double[size][size];
		for(int i=0;i<24;i++)
			for(int j=0;j<size;j++)
				locations[i][j]= 0;
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++){
				distanceBetweenCells[i][j] = 0;
				transferAllDay[i][j]= 0;
			}

		for(int i=0;i<24;i++)
			for(int j=0;j<size;j++)
				for(int k=0;k<size;k++)
					transferByHour[i][j][k]=0;
		
		for(int i=0;i<size;i++)
			cellIndexMap.put(differentCell.get(i), i);
		
		for(int day=0;day<daySize;day++){
			for(int hour=0;hour<25;hour++){
				cellAllCut[day][hour] = new ArrayList<String>();
				cellAllCut[day][hour].addAll(cellAll[day][hour]);
			}
		}
		
	}
	//����ȫ��ת�ƾ���
	public void fillTransferAllDay(){
		ArrayList<String> allTrajectory = new ArrayList<String>();
		for(int day=0;day<daySize;day++)
				allTrajectory.addAll(cellAll[day][24]);
		int allSize = allTrajectory.size();
		String preCell = allTrajectory.get(0);
		for(int i=1;i<allSize;i++){
			String cell = allTrajectory.get(i);
			int index = cellIndexMap.get(cell);
			int preIndex =cellIndexMap.get(preCell);
			transferAllDay[preIndex][index]++;
			preCell = cell;
		}
		//�ɴ�����Ϊ����
		for(int i=0;i<size;i++){
			double sum =0;
			for(int j=0;j<size;j++)
				sum+=transferAllDay[i][j];
			for(int j=0;j<size;j++)
				transferAllDay[i][j] = transferAllDay[i][j]  / sum;
		}
			
	}
	
	//����ÿСʱת�ƾ���
	public void fillTranferByHour(){
		for(int hour=0;hour<24;hour++){
			for(int day=0;day<daySize;day++){
				String thisCell=null,nextCell=null;
				ArrayList<String> trajectory = cellAllCut[day][hour];
				int tSize = trajectory.size();
				for(int i=0;i<tSize;i++){
					boolean hasNext = true;
					thisCell = trajectory.get(i);
					if(i==tSize-1){
						if(hour==23){
							if(cellAllCut[(day+1)%daySize][0].size()!=0)
								nextCell = cellAllCut[(day+1)%daySize][0].get(0);
							else 
								hasNext = false;
						}							
						else{
							if(cellAllCut[day][hour+1].size()!=0)
								nextCell = cellAllCut[day][hour+1].get(0);
							else 
								hasNext = false;
						}						
					}
					else 
						nextCell = trajectory.get(i+1);
					if(hasNext)
						transferByHour[hour][cellIndexMap.get(thisCell)][cellIndexMap.get(nextCell)]++;
				}
			}	
		}
		//�ɴ����������
		for(int k=0;k<24;k++){
			for(int i=0;i<size;i++){
				double sum =0;
				for(int j=0;j<size;j++)
					sum+=transferByHour[k][i][j];
				if(sum!=0)
				for(int j=0;j<size;j++)
					transferByHour[k][i][j] = transferByHour[k][i][j]  / sum;
			}
		}
			
	}
	
	//����λ�þ��󡣼�ĳСʱ�ڣ�������վ������
	public void fillLocations(){
		for(int i=0;i<24;i++)
			personLoc[i] = new ArrayList<CellInfo>();
		for(int day=0;day<daySize;day++){
			for(int hour=0;hour<24;hour++)
				personLoc[hour].addAll(stringToCellInfo(cellAll[day][hour]));
		}
		for(int hour=0;hour<24;hour++){
			personLoc[hour] = calculatePossibility(personLoc[hour]);
		}
		
		for(int hour=0;hour<24;hour++)
			for(CellInfo c :personLoc[hour]){
				String lacCell = c.lacID+"_"+c.cellID;
				int index = cellIndexMap.get(lacCell);
				locations[hour][index] = c.getPossbility();
			}
		
	}
	//�����վ֮��ľ���
	public void fillDistance(){
		for(String s:differentCell){
			int i = cellIndexMap.get(s);
			String coor = cellToCoordinate.get(s);
			String temps[] = coor.split("_");
			
			for(String l:differentCell){
				
				String coors = cellToCoordinate.get(l);
				String templ[] = coors.split("_");
				double d = distance(Double.parseDouble(temps[0]),Double.parseDouble(temps[1])
						,Double.parseDouble(templ[0]),Double.parseDouble(templ[1]));
				int j = cellIndexMap.get(l);
				distanceBetweenCells[i][j] = d;
				
			}
		}
	}
	
	//�������������
	public double distance(double lat1, double longt1, double lat2,double longt2){
		 double PI = 3.14159265358979323; // Բ����
		 double R = 6371229; // ����İ뾶
		 double x, y, distance;
		 x = (longt2 - longt1) * PI * R * Math.cos(((lat1 + lat2) / 2) * PI / 180) / 180;
		 y = (lat2 - lat1) * PI * R / 180;
		 distance = Math.hypot(x, y);
//		double x = Math.pow((lat1-lat2), 2);
//		double y = Math.pow((longt1-longt2), 2);
//		double distance = Math.sqrt(x+y);
		return distance;
	}
	
	//�������������л�վ����
	public ArrayList<CellInfo> calculatePossibility(ArrayList<CellInfo> cellAll){
		HashMap<String, Double> map = new HashMap<String,Double>();
		ArrayList<CellInfo> result = new ArrayList<CellInfo>();
		for(CellInfo c:cellAll){
			String temp = c.lacID+"_"+c.cellID;
			addMap(map,temp);
		}
		double sum = cellAll.size();
		for(String s:map.keySet()){
			String temp[] = s.split("_");
			CellInfo cell = new CellInfo(temp[0], temp[1]);
			double possibility = map.get(s)/sum;
			cell.setPossbility(possibility);
			result.add(cell);
		}
		
		return result;
	}
	
	//��������
		public void addMap(HashMap<String,Double> map,String s){
			if(map.containsKey(s)){
				double c = map.get(s);
				c++;
				map.remove(s);
				map.put(s, c);				
			}
			else
				map.put(s, (double) 1);
		}
	
	//Stringתcellinfo
	public ArrayList<CellInfo> stringToCellInfo(ArrayList<String> list){
		ArrayList<CellInfo> result = new ArrayList<CellInfo>();
		for(String s:list){
			String temp[] = s.split("_");
			result.add(new CellInfo(temp[0], temp[1]));
		}
		return result;
	} 
	//��ӡ
	public void print(){
		
//		for(int i=0;i<24;i++){
//			for(int j=0;j<size;j++)
//				System.out.print(locations[i][j]+" ");
//			System.out.println();
//		}
//		System.out.println("-----------------------------------------------------------------");
//		for(int i=0;i<size;i++){
//			for(int j=0;j<size;j++)
//				if(transferAllDay[i][j]!=0)
//				System.out.print(transferAllDay[i][j]+" ");
//			System.out.println();
//		}
//		System.out.println("-----------------------------------------------------------------");
		for(int hour=0;hour<24;hour++){
			System.out.println("--------------------------------"+hour);
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++)
					if(transferByHour[hour][i][j]!=0)
						System.out.print(transferByHour[hour][i][j]+" ");
				System.out.println();
			}
		}
//		System.out.println("----------------------------------------------------------------����");
//		for(int i=0;i<size;i++){
//			for(int j=0;j<size;j++)
//				System.out.print(distanceBetweenCells[i][j]+" ");
//			System.out.println();
//		}
	}
	
	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		MatrixForPersonAndCell m = new MatrixForPersonAndCell("99249788048010590");System.out.println("ssss");
		m.print();
		double a = m.distance(31.53681, 120.32961, 31.514254,120.363525);
		System.out.println(a);
		double aa = m.distance(31.517479, 120.36028, 31.514254,120.363525);
		System.out.println(aa);
	}

	public ArrayList<String> getDifferentCell() {
		return differentCell;
	}

	public Map<String, String> getCellToCoordinate() {
		return cellToCoordinate;
	}

	public double[][] getLocations() {
		return locations;
	}

	public double[][] getTransferAllDay() {
		return transferAllDay;
	}

	public double[][][] getTransferByHour() {
		return transferByHour;
	}

	public HashMap<String, Integer> getCellIndexMap() {
		return cellIndexMap;
	}

	public double[][] getDistanceBetweenCells() {
		return distanceBetweenCells;
	}

	public ArrayList<String>[][] getCellAll() {
		return cellAll;
	}

	//����grouplist,����ÿСʱ���ֹ��cellID�����飩
		public String[] getEndCell(){	
			//����ÿ��cell��endValue
			int cellsEndValue[][] = new int[24][size];//��¼ÿСʱ���л�վ������
			String endCell[] = new String[24]; //��¼ÿСʱ��ֹcell
			
			for(int day=0;day<daySize;day++){
				for(int hour=0;hour<24;hour++){
					//ȡ���������ֱ��group��startValueֵ����һ��cell��3���ڶ�����2����������1.
					int number = 3;
					int nsize = cellAll[day][hour].size();
					if(number>nsize)
						number = nsize;
					if(nsize>5)						//��������5��·��������Ч·��
					for(int i=nsize-number;i<nsize;i++){
						String cell = cellAll[day][hour].get(i);
						int index = cellIndexMap.get(cell);
						cellsEndValue[hour][index] += 4-(nsize-i);
					}
				}
			}
			//ѡȡ���endValue��cell
			for(int hour=0;hour<24;hour++){
				int maxIndex = 0;
				double maxValue = 0;
				for(int i=0;i<size;i++){
					double v = cellsEndValue[hour][i];
					if(v > maxValue){
						maxValue = v;
						maxIndex = i;
					}
				}
				String maxCell = differentCell.get(maxIndex);
				endCell[hour] = maxCell;
			}
			return endCell;
		}
		
		//����grouplist,����ÿСʱ���ʼ��cellID�����飩
			public String[] getStartCell(){
				//����ÿ��cell��startValue
				int cellsStartValue[][] = new int[24][size];//��¼ÿСʱ���л�վ������
				String startCell[] = new String[24]; //��¼ÿСʱ��ʼcell
				
				for(int day=0;day<daySize;day++){
					for(int hour=0;hour<24;hour++){
						//ȡǰ�������ֱ��group��startValueֵ����һ��cell��3���ڶ�����2����������1.
						int number = 3;
						int nsize = cellAll[day][hour].size();
						if(number>nsize)
							number = nsize;
						if(nsize>5)           //��������5��·��������Ч·��
							for(int i=nsize-number;i<nsize;i++){
								String cell = cellAll[day][hour].get(i);
//								System.out.println(cell);sd
								int index = cellIndexMap.get(cell);
								cellsStartValue[hour][index] += 3-i;
							}
					}
				}
				//ѡȡ���startValue��group
				for(int hour=0;hour<24;hour++){
					int maxIndex = 0;
					double maxValue = 0;
					for(int i=0;i<size;i++){
						double v = cellsStartValue[hour][i];
						if(v > maxValue){
							maxValue = v;
							maxIndex = i;
						}
					}
					String maxCell = differentCell.get(maxIndex);
					startCell[hour] = maxCell;
				}
				return startCell;
			}
	
	//����cellAll��ȥ����������ʼ�����ֹ��� ����
		public void cutCellAll(){
			
			String[] startCell = getStartCell();
			String[] endCell = getEndCell();
//			for(int i=0;i<24;i++)
//				System.out.println(startCell[i]+"_"+endCell[i]);
			HashSet<String> set = new HashSet<String>();
			
			for(int hour=0;hour<24;hour++){
				//��ÿ���Сʱ���ֹ��Ĳ�ͬ��վ��ƽ������
				int averageNum = 0;
				for(int day=0;day<daySize;day++){
					set.clear();
					set.addAll(cellAllCut[day][hour]);
					averageNum += set.size();
				}
				averageNum /= daySize;
				for(int day=0;day<daySize;day++){
//					if(cellAllCut[day][hour].contains(startCell[hour]) && cellAllCut[day][hour].contains(endCell[hour]))
					if(cellAllCut[day][hour].size()>averageNum+2 && cellAllCut[day][hour].contains(endCell[hour]))
//					if(cellAllCut[day][hour].size()>averageNum+2 )
						continue;
					else {
						cellAllCut[day][hour].clear();
					}
				}
			}
		}
}
