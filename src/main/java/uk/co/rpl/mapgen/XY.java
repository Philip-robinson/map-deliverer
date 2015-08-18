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
    public static final XY ZERO=new XY(0,0);
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

    public XY div(int div){
        return new XY(x/div, y/div);
    }

    public XY mul(int mul){
        return new XY(x*mul, y*mul);
    }
    public XY mul(XY mul){
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

    public XY div(XY div) {
        return new XY(x/div.x, y/div.y);
    }

    public XY sub(XY sub) {
        return new XY(x-sub.x, y-sub.y);
    }
    
    public XY sub(int sub) {
        return new XY(x-sub, y-sub);
    }
    
    public XYD xyd(){
        return new XYD(x,y);
    }

    @Override
    public String toString() {
        return "XY{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.x;
        hash = 37 * hash + this.y;
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
        final XY other = (XY) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

}
