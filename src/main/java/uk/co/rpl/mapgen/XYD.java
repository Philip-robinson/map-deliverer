/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

/**
 *
 * @author philip
 */
public class XYD {
    public final double x;
    public final double y;

    public XYD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public XYD divide(double div){
        return new XYD(x/div, y/div);
    }

    public XYD multiply(double mul){
        return new XYD(x*mul, y*mul);
    }
    public XYD times(XYD mul){
        return new XYD(x*mul.x, y*mul.y);
    }

    public XYD mul(XYD mul){
        return new XYD(x*mul.x, y*mul.y);
    }
    public XYD negate(){
        return new XYD(-x, -y);
    }
    
    public XYD add(XYD add){
        return new XYD(x+add.x, y+add.y);
    }
    
    public XYD add(double addx, double addy){
        return new XYD(x+addx, y+addy);
    }

    public XYD div(XYD div) {
        return new XYD(x/div.x, y/div.y);
    }

    public XYD sub(XYD sub) {
        return new XYD(x-sub.x, y-sub.y);
    }
    
    public XYD sub(double sub) {
        return new XYD(x-sub, y-sub);
    }

    public XY xy() {
        return new XY((int)x, (int)y);
    }

    public XY round() {
        return new XY((int)(x+0.5), (int)(y+0.5));
    }

    @Override
    public String toString() {
        return "XYD{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^
                                  (Double.doubleToLongBits(this.x) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^
                                  (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XYD other = (XYD) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        return true;
    }

    public XYD frac() {
        return new XYD(x-Math.floor(x), y-Math.floor(y));
    }

    public XY ceil() {
        return new XY((int)Math.ceil(x), (int)Math.ceil(y));
    }

    public XY entier() {
        return new XY((int)(x>0?Math.ceil(x):x), (int)(y>0?Math.ceil(y):y));
    }

    public XYD abs() {
        if (x>=0 && y>=0) return this;
        double xx = x>=0?x:-x;
        double yy = y>=0?y:-y;
        return new XYD(xx, yy);
    }
    
}
