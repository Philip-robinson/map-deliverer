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
public class XY {
    public final int x;
    public final int y;

    public XY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public XY divide(int div){
        return new XY(x/div, y/div);
    }

    public XY multiply(int mul){
        return new XY(x*mul, y*mul);
    }
    public XY multiply(XY mul){
        return new XY(x*mul.x, y*mul.y);
    }

    public XY negate(){
        return new XY(-x, -y);
    }
    
    public XY add(XY add){
        return new XY(x+add.x, y+add.y);
    }
    
    public XY add(int addx, int addy){
        return new XY(x+addx, y+addy);
    }

    public XY divide(XY div) {
        return new XY(x/div.x, y/div.y);
    }

    public XY subtract(XY sub) {
        return new XY(x-sub.x, y-sub.y);
    }
    
    public XY subtract(int sub) {
        return new XY(x-sub, y-sub);
    }
    
    public XYD xyd(){
        return new XYD(x,y);
    }
}
