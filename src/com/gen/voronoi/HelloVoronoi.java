package com.gen.voronoi;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PApplet;

public class HelloVoronoi extends PApplet{

	private static final long serialVersionUID = 1L;
	private static final String DATA_DIRECTORY = "./data";
	private UnfoldingMap map;
	private int initZoomLevel = 11;
	Location wuxiLocation = new Location(31.587756f, 120.313505f);
	Voronoi myVoronoi;
	MPolygon[] myRegions;
	
	public void test(){
		float[][] points = new float[3][2];
		
//		points[0][0] = (float) 31.4379606068423; // first point, x
//		points[0][1] = (float) 120.4984387722; // first point, y
		points[0][0] = (float) 31.81571; // first point, x
		points[0][1] = (float) 120.198845; // first point, y
		points[1][0] = (float) 31.4469283003; // second point, x
		points[1][1] = (float) 120.507395773034; // second point, y
		points[2][0] = (float) 31.4405905153115; // third point, x
		points[2][1] = (float) 120.4311123166; // third point, y
		for(int i=0;i<3;i++){
			Location l = new Location(points[i][0],points[i][1]);
			ScreenPosition pos = map.getScreenPosition(l);
			System.out.println(l.x+"_"+l.y+"______"+pos.x+"_"+pos.y);
			points[i][0] = pos.x;
			points[i][1] = pos.y;
			ellipse(pos.x, pos.y , 10, 10);
		}

		myVoronoi = new Voronoi( points );
		myRegions = myVoronoi.getRegions();
		for(int i=0; i<myRegions.length; i++){
			noFill();
			stroke(255,255,255);
			  myRegions[i].draw(this);
		}
		
	}
	public void region(){
		myRegions = myVoronoi.getRegions();

		for(int i=0; i<myRegions.length; i++)
//		for(int i=0; i<1; i++)
		{
		  // an array of points
		  float[][] regionCoordinates = myRegions[i].getCoords();

//		  fill(255,0,0);
		  noFill();
		  myRegions[i].draw(this); // draw this shape
		}
	}
	@Override
	public void setup() {
	
		frame.setResizable(true);
//		size(1150, 780);
		size(500, 500);
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
	@Override
	public void draw() {
		background(0);
		this.frame.setTitle("Wuxi GSM");	
		map.draw();
		test();
//		region();
		
	}
	
	public static void main(String[] agrs) {
		PApplet.main(new String[] { "com.gen.voronoi.HelloVoronoi" });
	}

}
