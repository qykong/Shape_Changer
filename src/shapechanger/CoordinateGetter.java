/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shapechanger;

import java.util.ArrayList;

/**
 * This class is used to get the y coordinate of a chosen shape based on its
 * x coordinate.
 * @author quyu_kong
 */
public class CoordinateGetter {
    private boolean isTriangle = false;
    private boolean isRectangle = false;
    private boolean isEllipse = false;
    private boolean isPolygon = false;
    private ArrayList<Point> trianglePoints = new ArrayList<Point>();
    private double minX,maxX,minY,maxY,interval;
    public double a,b,radius;
    
    public CoordinateGetter(Point left, Point right, Point up) {
        this.isTriangle = true;
        this.trianglePoints.add(left);
        this.trianglePoints.add(right);
        this.trianglePoints.add(up);
    }
    
    public CoordinateGetter(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.a = (maxX-minX)/2;
        this.b = (maxY-minY)/2;
        this.isEllipse = true;
    }
    
    public CoordinateGetter(double minX, double maxX, double minY, double maxY, int edgeNum) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.radius = Math.sqrt(Math.pow((maxX-minX)/2,2)+Math.pow((maxY-minY)/2,2));
        this.interval = Math.toRadians(360d/edgeNum);
        this.isPolygon = true;
    }
    
    public ArrayList<Double> getY(double x) {
        ArrayList<Double> ys = new ArrayList<Double>();

        if (isTriangle == true) {
            ys.add(trianglePoints.get(0).y);
            if (x < trianglePoints.get(2).x) {
                ys.add((trianglePoints.get(2).y-trianglePoints.get(0).y)*(x-trianglePoints.get(0).x)/(trianglePoints.get(2).x-trianglePoints.get(0).x)+trianglePoints.get(0).y);
            } else {
                ys.add((trianglePoints.get(2).y-trianglePoints.get(1).y)*(x-trianglePoints.get(1).x)/(trianglePoints.get(2).x-trianglePoints.get(1).x)+trianglePoints.get(1).y);
            }
        }
        
        if (isEllipse == true) {
            double x2 = x-a-minX;
            if (b*b-b*b*x2*x2/(a*a)<0) {
                System.out.println(b*b-b*b*x2*x2/(a*a));
                System.out.println(b+" "+a+" "+minX+" "+x2+" "+x);
                ys.add((minY+maxY)/2);
                ys.add((minY+maxY)/2);
            } else{
                ys.add(Math.sqrt(b*b-b*b*x2*x2/(a*a))+(minY+maxY)/2);
                ys.add(-1*Math.sqrt(b*b-b*b*x2*x2/(a*a))+(minY+maxY)/2);
            }
        }

        return ys;
    }
    
    //For polygons, it is easier to calculate the coordinates in a polar
    //coordinate system.
    public double getL(double theta) {
        int k = (int)(theta/interval);
        double l = radius*radius*Math.sin(interval)/(radius*Math.sin(theta-k*interval)-radius*Math.sin(theta-(k+1)*interval));
        return l;
    }
}
