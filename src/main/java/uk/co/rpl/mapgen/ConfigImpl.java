/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import uk.co.rpl.mapgen.mapinstances.FixedMap;
import uk.co.rpl.mapgen.mapinstances.TFWMap;

/**
 *
 * @author philip
 */
public class ConfigImpl implements Config{

    Properties prop;
    private MapConfig[] maps;
    private final Object mapsLock = new Object();

    @Override
    public String get(String name){
        return prop.getProperty(name);
    }

    @Override
    public int getInt(String name, int def){
        String v = get(name);
        if (v==null) return def;
        try{
            return Integer.parseInt(v);
        }catch (NumberFormatException e){
            return def;
        }
    }
    
    @Override
    public double getDbl(String name, double def){
        String v = get(name);
        if (v==null) return def;
        try{
            return Double.parseDouble(v);
        }catch (NumberFormatException e){
            return def;
        }
    }
    
    @Override
    public XY getXY(String name, String name2, XY def){
        String x = get(name);
        String y = get(name2);
        if (x==null || y==null) return def;
        try{
            return new XY(Integer.parseInt(x), Integer.parseInt(y));
        }catch (NumberFormatException e){
            return def;
        }
    }
    
    @Override
    public XYD getXYD(String name, String name2, XYD def){
        String x = get(name);
        String y = get(name2);
        if (x==null || y==null) return def;
        try{
            return new XYD(Double.parseDouble(x), Double.parseDouble(y));
        }catch (NumberFormatException e){
            return def;
        }
    }
    
    @Override
    public MapConfig[] maps() {
        synchronized(mapsLock){
            if (maps==null){
                final List<MapConfig> clist = new ArrayList<>();
                for(String mapId: get("maps").split("\\s*,\\s*")){
                    final MapConfig mc = getMapConfig(mapId);
                    if (mc != null) clist.add(mc);
                }
            }
        }
        return maps;
    }

    private MapConfig getMapConfig(String mapId) {
        String type = get(mapId+".scale-type");
        switch(type){
            case "FIXED": return new FixedMap(this, mapId);
            case "TFW": return new TFWMap(this, mapId);
            default: return null;
        }
    }
    
}
