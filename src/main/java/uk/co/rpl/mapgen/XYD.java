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
    public XYD multiply(XYD mul){
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

    public XYD divide(XYD div) {
        return new XYD(x/div.x, y/div.y);
    }

    public XYD subtract(XYD sub) {
        return new XYD(x-sub.x, y-sub.y);
    }
    
    public XYD subtract(double sub) {
        return new XYD(x-sub, y-sub);
    }
    
}
