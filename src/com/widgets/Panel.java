package com.widgets;

import java.util.List;

import processing.core.PApplet;

import de.fhpotsdam.unfolding.geo.Location;

public class Panel implements Control {
	List<Location> showLoc  = null;
	boolean isSelected = false;
	float xpos, ypos;
	float width, height;
	boolean isOver = false;
	boolean isDisplay = true;
	PApplet context;

	public Panel(PApplet context, float xpos, float ypos, float width,
			float height, List<Location> showLoc) {
		this.context = context;
		this.showLoc = showLoc;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean overEvent() {
		if (context.mouseX > xpos && context.mouseX < xpos + width
				&& context.mouseY > ypos && context.mouseY < ypos + height) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update() {
	}

	@Override
	public void display() {
		if (this.isDisplay) {
			context.fill(0, 0, 0, 0.65f * 255);
			context.rect(xpos, ypos, width, height);
		}
	}

	@Override
	public void setIsDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}
}