package com.gen.voronoi;

import processing.core.*;

public class IntArray {

	int[] data;
	int length;

	public IntArray(){
		this(1);
	}

	public IntArray( int l ){
		data = new int[l];
		length = 0;
	}

	public void add( int d ){
		int dlen = data.length;
		if( length==dlen )
			data = PApplet.expand(data);
		System.out.println("dataLen:"+data.length+" len:"+length);
		data[length++] = d;
	}

	public int get( int i ){
		return data[i];
	}

	public boolean contains( int d ){
		for(int i=0; i<length; i++)
			if(data[i]==d)
				return true;
		return false;
	}

}