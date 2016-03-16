package com.gen.trajectory;

public class luanqibazao {

	//给定均值、方差以及x，返回x的概率
		public double normalDistribution(double mean,double deviation,double x){
			double possiblity = 0;
			double expValue = (-0.5*(1/deviation))*(Math.pow((x-mean), 2));
			possiblity = Math.exp(expValue) *(1/Math.sqrt(2*Math.PI*deviation));
			return possiblity;
		}
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		luanqibazao l = new luanqibazao();
		for(double x=1;x<=10;x++){
			double r = l.normalDistribution(10, 10, x);
			System.out.println(r);
		}
		
	}

}
