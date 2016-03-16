package com.gen.road;

import java.util.ArrayList;
import java.util.Stack;

public class Dijkstra {

	static double max = Double.MAX_VALUE;
	double[] dist;
	public int[] prev;
	double[][] c;
	int size;
	int begin;//起始节点
	int end;
	
	public Dijkstra(double[][] c,int size,int begin,int end){
		this.size = size;
		this.c = c;
		dist = new double[size];
		prev = new int[size];
		this.begin = begin;
		this.end = end;
		run();
	}
	
	public int[] getPrev() {
		return prev;
	}

	public void run() {

		// 初始化 dist/prev/s
		boolean[] s = new boolean[size];
		for (int i = 0; i < size; i++) {

			dist[i] = c[begin][i];
			if (c[begin][i] != max)
				prev[i] = begin;
			else
				prev[i] = -1;
			s[i] = false;

		}
		s[begin] = true;
		prev[begin] = -1;
		dist[begin] = 0;

		// 每次找出一点，一边
		for (int a = 0; a < size; a++) {

			// 找出dist[v]里最小值
			double temp = max;
			int u = begin;

			for (int j = 0; j < size; j++)
				if (!s[j] && dist[j] < temp) {
					temp = dist[j];
					u = j;
				}
			s[u] = true;

			// 松弛
			for (int i = 0; i < size; i++) {
				if (!s[i] && c[u][i] < max) {
					if (dist[u] + c[u][i] < dist[i]) {
						dist[i] = dist[u] + c[u][i];
						prev[i] = u;
					}
				}
			}
			
		}

	}

	//给定目标点(终止点)，返回路径。
	public ArrayList<Integer> getTheTrajectory(){
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		Stack stack = new Stack<Integer>();
		stack.push(end);
		while(true){
			if(end==-1){
				return null;
			}
			end = prev[end];
			stack.push(end);
			if(end==begin)
				break;
		}
		while(!stack.isEmpty())
			result.add((Integer) stack.pop());
//		System.out.println("Distance: "+dist[end]);
		return result;
	}
	
	//将prev转换成list
	public String prevToString(){
		String result = prev[0]+"";
		for(int i=1;i<size;i++)
			result+= "_"+prev[i];
		return result;
	}
	
	
	
	public static void main(String[] args) {

//		double[][] a = { { max, 10, max, max, 5 }, { max, max, 1, max, 2 },
//				{ max, max, max, 4, max }, { 7, max, 6, max, max },
//				{ max, 3, 9, 2, max } };
//		
//		Dijkstra d = new Dijkstra(a,5,0,2);
//		System.out.println(d.prevToList());
		
		RoadPreparation road = new RoadPreparation();
		double roadGraph[][] = road.getRoadGraph();
//		Dijkstra d = new Dijkstra(roadGraph,road.pointSize,1384,10587);
//		System.out.println(d.getTheTrajectory());
		/*for(int i=0;i<road.pointSize;i++)
			if(roadGraph[8308][i]!=Double.MAX_VALUE)
			System.out.println(roadGraph[8308][i]);*/
		
		ArrayList<String> allPath = new ArrayList<String>();
		int size = road.pointSize;
		for(int i=0;i<size;i++){
			System.out.println(i+"/"+size);
			Dijkstra d = new Dijkstra(roadGraph,size,i,i);
			allPath.add(d.prevToString());
		}
		DealWithFile deal = new DealWithFile();
		deal.exportResString(allPath, "E:/aSmartCity/Map/allShortPath.txt");
//		System.out.println(d.getTheTrajectory());
//		System.out.println(d.prevToString());
	}
}
