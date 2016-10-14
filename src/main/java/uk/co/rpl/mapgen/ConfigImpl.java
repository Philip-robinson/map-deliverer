package uk.co.rpl.mapgen;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.mapinstances.FixedMap;
import uk.co.rpl.mapgen.mapinstances.TFWMap;
import uk.co.rpl.mapgen.mapinstances.TileCacheManager;
import uk.co.rpl.mapgen.mapinstances.TileException;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author philip
 */
public class ConfigImpl implements Config{

    private static Logger LOG = getLogger(ConfigImpl.class);
    private final Properties prop;
    private MapConfig[] maps;
    private final Object mapsLock = new Object();
    private final TileCacheManager cacheManager;

    public ConfigImpl(){
        this.prop = new Properties();
        try(InputStream is = getClass().getResourceAsStream(
                                            "/config.properties")){
            prop.load(is);
            for (String fn: new String[]{
                    "/etc/mapgen/mapgen.properties",
                    "~/mapgen.properties",
                    "~/.mapgen.properties",
                    "mapgen.properties"}){
                File f = new File(fn);
                if (f.exists()){
                   try(InputStream is2=new FileInputStream(f)){
                       prop.load(is2);
                   }
                }
            }
        }catch(IOException e){
            LOG.error(e.getMessage(), e);
        }
        LOG.info("Configured");
        for (Object key: new TreeSet(prop.keySet())){
            LOG.info(String.format(" data %-20s => %s", key, prop.get(key)));
        }
        cacheManager = new TileCacheManager(this);
    }
    
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
                    new Thread(()->{
                        try{
                            mc.allTiles();
                            LOG.info("Map {} loaded", mapId);
                        } catch (TileException ex) {
                            LOG.error("Problem starting mapId={}: {}", 
                                      mapId, ex.getMessage(), ex);
                        }
                        }).start();
                    if (mc != null) clist.add(mc);
                }
                maps=clist.toArray(new MapConfig[clist.size()]);
            }
        }
        return maps;
    }

    private MapConfig getMapConfig(String mapId) {
        String type = get(mapId+".scale-type");
        switch(type){
            case "FIXED": return new FixedMap(this, mapId, cacheManager);
            case "TFW": return new TFWMap(this, mapId, cacheManager);
            default: return null;
        }
    }

    public TileCacheManager getCacheManager() {
        return cacheManager;
    }
    
}
