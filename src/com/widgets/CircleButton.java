package com.widgets;

import processing.core.PApplet;

public class CircleButton implements Control {
	Boolean[] showLoc = null;
	boolean isSelected = false;
	float xpos, ypos;
	float width, height;
	boolean isOver = false;
	boolean isDisplay = true;
	String id = "";
	PApplet context;

	public CircleButton(PApplet context, float xpos, float ypos, float width,
			float height, Boolean[] showLoc, String id) {
		this.context = context;
		this.showLoc = showLoc;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.id = id;
	}

	@Override
	public boolean overEvent() {
		float dis = PApplet.pow((context.mouseX - xpos), 2)
				+ PApplet.pow((context.mouseY - ypos), 2);
		if (dis <= PApplet.pow(this.width / 2, 2)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update() {

		if (overEvent()) {
			isOver = true;
		} else {
			isOver = false;
		}
		if (isOver) {
			if (!(id.equals("weekend")||id.equals("station")))
			for(int i=0;i<25;i++)
				showLoc[i] = false;
			
			if (id.equals("ALL"))
				showLoc[24] = true;
			else  if (id.equals("weekend"))
				showLoc[25] = !showLoc[25];
			else  if (id.equals("station"))
				showLoc[26] = !showLoc[26];
			else
				showLoc[Integer.parseInt(id)] = true;
		}
			isSelected = true;
	}

	public void reset(PApplet context, float xpos, float ypos, float width,
			float height, Boolean[] showLoc, String id) {
		this.context = context;
		this.showLoc = showLoc;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.id = id;
		display();
	}

	@Override
	public void display() {
			
		//是否被选中，用于display	
			if (id.equals("ALL"))
				isSelected = showLoc[24];
			else if (id.equals("weekend"))
				isSelected = showLoc[25];
			else if (id.equals("station"))
				isSelected = showLoc[26];
			else
				isSelected = showLoc[Integer.parseInt(id)];							
	
		if (isDisplay) {			
			context.strokeWeight(1);
			if (isSelected) {
				context.noStroke();
				context.fill(255, 196, 13);
				context.ellipse(xpos, ypos, width, height);
			} else {
				context.noStroke();
				context.fill(196, 196, 196, 50);
				context.ellipse(xpos, ypos, width, height);
			}
			context.fill(255);
			context.textSize(10);
			context.text(id, xpos - width / 2, ypos + height);
		}
	}

	@Override
	public void setIsDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}

	

}
