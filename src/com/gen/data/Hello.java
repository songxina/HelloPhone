package com.gen.data;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;
import megamu.mesh.MPolygon;
import megamu.mesh.Voronoi;


public class Hello extends PApplet{

	private static final long serialVersionUID = 1L;
	private static final String DATA_DIRECTORY = "./data";
	private UnfoldingMap map;
	private int initZoomLevel = 11;
	Location wuxiLocation = new Location(31.587756f, 120.313505f);
	
	public void test(){
		float[][] points = new float[3][2];

		points[0][0] = 120; // first point, x
		points[0][1] = 230; // first point, y
		points[1][0] = 150; // second point, x
		points[1][1] = 105; // second point, y
		points[2][0] = 320; // third point, x
		points[2][1] = 113; // third point, y

		Voronoi myVoronoi = new Voronoi( points );
		
		MPolygon[] myRegions = myVoronoi.getRegions();

		for(int i=0; i<myRegions.length; i++)
		{
		  // an array of points
		  float[][] regionCoordinates = myRegions[i].getCoords();

		  fill(255,0,0);
		  myRegions[i].draw(this); // draw this shape
		}
	}
	
	public void setup() {

	
		frame.setResizable(true);
		size(1150, 780);
		String mbTilesString = sketchPath(DATA_DIRECTORY+ "/map/Wuxi-blue-7-16.mbtiles");
		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));
		// map = new UnfoldingMap(this);
		map.zoomToLevel(initZoomLevel);
		map.panTo(wuxiLocation);
		map.setZoomRange(10, 15);
		// map.setPanningRestriction(wuxiLocation, 50);
		MapUtils.createDefaultEventDispatcher(this, map);
		frameRate(60);
		
		test();
	}
	public void draw() {
		background(0);
		this.frame.setTitle("Wuxi GSM");	
		map.draw();
	}
	
	public static void main(String[] agrs) {
		PApplet.main(new String[] { "com.gen.trace.WuxiGsm" });
	}

}
