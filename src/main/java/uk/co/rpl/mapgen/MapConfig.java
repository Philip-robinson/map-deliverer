/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.io.File;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public interface MapConfig {
    String getInstance();
    File tileDir();
    File dataDir();
    SCALE_TYPE scaleType();
    XY tileSize();
    XYD tileScale();
    XYD maxTileScale();
    XYD minTileScale();
    XYD tile0Origin();
    String tileFilename();
    String dataFilename();

    TileSet allTiles() throws TileException;

    
    default int suitability(XYD eastnorth, XYD scale, XY size) throws TileException{
        if (scale.x==0||scale.y==0||size.x==0||size.y==0) return 100;
        TileSet all = allTiles();
        XYD rSize = scale.mul(size.xyd());
        XYD rbr = rSize.add(eastnorth);
        
        XYD act =  all.getScaleMpPx().abs();
        XYD min = minTileScale().abs();
        XYD max = maxTileScale().abs();
        double vax = gva(scale.x, act.x, min.x, max.x);
        double vay = gva(scale.y, act.y, min.y, max.y);
        double sf = Math.max(vax,vay)*100;
        return (int)(sf);
    };
    default double gva(double req, double act, double min, double max){
        if (req<act){
            if (req<min){
                return 0.6*min/(2*min-req);
            }else{
                return (req-min)/(act-min)*0.4+0.6;
            }
        }else{
            if (req>max){
                return 0.6*max/(max+req-max);
            }else{
                return (max-req)/(max-act)*0.4+0.6;
            }
        }
    }
    public enum SCALE_TYPE{
        FIXED, TFW
    }
}
