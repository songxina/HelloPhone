package com.gen.trajectory;

public class CellInfo {

	String lacID;
	String cellID;
	String timestamp;
	double possbility = 0;
	
	public CellInfo(String lac,String cell){
		lacID = lac;
		cellID = cell;
	}
	
	public String getLacID() {
		return lacID;
	}

	public void setLacID(String lacID) {
		this.lacID = lacID;
	}

	public String getCellID() {
		return cellID;
	}

	public void setCellID(String cellID) {
		this.cellID = cellID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public double getPossbility() {
		return possbility;
	}

	public void setPossbility(double possbility) {
		this.possbility = possbility;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
