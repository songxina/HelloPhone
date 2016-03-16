package com.gen.trajectory;

import java.util.ArrayList;

public class Traverse {
	static ArrayList<ArrayList<String>> permutationResult  = new ArrayList<ArrayList<String>>();
    
//    public static String[] text = { "a","b","c","d","e" };
    public static String[] text = { "0","1","2","3","4" };
    
    public static void main(String[] args) {
//        permutation(text, 0, text.length);
//    	test();
    	ArrayList<String> a = new ArrayList<String>();
    	for(int i=0;i<text.length;i++)
    		a.add(text[i]);
    	searchAll(a);
        System.exit(0);
    }
    //遍历
    public static ArrayList<ArrayList<String>> searchAll(ArrayList<String> set){
    	String[] group = new String[set.size()];
    	for(int i=0;i<set.size();i++)
    		group[i] = set.get(i);
    	ArrayList<ArrayList<String>> a = getAllCombinations(group);
    	ArrayList<ArrayList<String>> result = combine(a);
    	return result;
    }
    
    //生成组合
    public static ArrayList<ArrayList<String>> getAllCombinations(String[] numSet) {
    	ArrayList<ArrayList<String>> result  = new ArrayList<ArrayList<String>>();
            long max = 1<<numSet.length;
            for (int i = 1; i < max; i++) {
            	ArrayList<String> temp = new ArrayList<String>();
                for (int j = 0; j < numSet.length; j++) {
                    if ((i&(1<<j))!=0) {
//                        System.out.print(numSet[j] + ", ");
                    	temp.add(numSet[j]);
                    }
                }
//                System.out.println();
                result.add(temp);
            }
            return result;
        }
    
    /**
    * 全排列输出
    * 
    * @param a[] 要输出的字符数组
    * @param m 输出字符数组的起始位置
    * @param n 输出字符数组的长度
    */
    public static ArrayList<ArrayList<String>> permutation(String a[], int m, int n) {
    	 int i;
        String t;
        if (m != n - 1) {
            permutation(a, m + 1, n);
            for (i = m + 1; i < n; i++) {
                t = a[m]; 
                a[m] = a[i];
                a[i] = t;
                permutation(a, m + 1, n);
                t = a[m];
                a[m] = a[i];
                a[i] = t;
            }
        } else {
//            printResult(a);
            ArrayList<String> list = new ArrayList<String>();
            for(int k=0;k<a.length;k++)
            	list.add(a[k]);
           
            permutationResult.add(list);
        }
        return permutationResult;
    }
 
    /**
    * 输出指定字符数组
    * 
    * @param text 将要输出的字符数组
    */
    public static void printResult(String[] text) {
        for (int i = 0; i < text.length; i++) {
            System.out.print(text[i]+"_");
        }
        System.out.println();
    }
    
    //对每种组合，生成全排列
    public static ArrayList<ArrayList<String>> combine(ArrayList<ArrayList<String>> clist){
    	ArrayList<ArrayList<String>> result  = new ArrayList<ArrayList<String>>();
    	for(ArrayList<String> list:clist){
//    		if(list.size()!=7)//数量固定在7个
//    			continue;
    		
    		String[] temp = new String[list.size()];
    		for(int i=0;i<list.size();i++)
    			temp[i] = list.get(i);
//    		System.out.print("aaaaaaa  ");
//    		printResult(temp);
    		result.addAll(permutation(temp,0,list.size()));
    	}
//    	System.out.println("Traverse排序个数："+result.size());
    	return result;
    }
}