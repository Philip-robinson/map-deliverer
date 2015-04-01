/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.rpl.mapgen.Config;
import uk.co.rpl.mapgen.MapConfig;
import static uk.co.rpl.mapgen.MapConfig.SCALE_TYPE.FIXED;
import uk.co.rpl.mapgen.Tile;
import uk.co.rpl.mapgen.TileSet;
import uk.co.rpl.mapgen.TileSetImpl;
import uk.co.rpl.mapgen.XY;
import uk.co.rpl.mapgen.XYD;

/**
 *
 * @author philip
 */
public class FixedMap implements MapConfig{
    private static final Logger LOG = LoggerFactory.getLogger(FixedMap.class);
    private final Config config;
    private final String base;

    public FixedMap(Config config, String base){
        this.config = config;
        this.base = base;
    }
    @Override
    public File tileDir() {
        return new File(config.get(base+".tile-dir"));
    }

    @Override
    public File dataDir() {
        return null;
    }

    @Override
    public SCALE_TYPE scaleType() {
        return FIXED;
    }

    @Override
    public XY tileSize() {
        return config.getXY(base+".width", base+".height", null);
    }

    @Override
    public XYD tileScale() {
        return config.getXYD(base+".scale-x", base+".scale-y", null);
    }

    @Override
    public XYD tile0Origin() {
        return config.getXYD(base+".origin-x", base+".origin-y", null);
    }

    @Override
    public String tileFilename() {
        return config.get(base+".tile-filename");
    }

    @Override
    public String dataFilename() {
        return null;
    }

    @Override
    public TileSet allTiles(){
        final Map<Integer, Map<Integer, Tile>> maps = new TreeMap<>();
        final String tilefn = tileFilename();
        String pat1 = tilefn.replaceAll("\\(", "\\(").replaceAll("\\)", "\\)").
                replaceAll("\\$\\{[^\\}]*\\}", "\\\\\\$\\\\{([^\\\\}]*)\\\\}");
        LOG.debug("Pat 1 {}", pat1);
        String pat2 = tilefn.replaceAll("\\(", "\\(").replaceAll("\\)", "\\)").
                replaceAll("\\$\\{[^\\}\\{]*\\}", "(.*)");
        LOG.debug("Pat 2 {}", pat2);
        final Pattern fnmasterp = Pattern.compile(pat1);
        final Pattern fnp = Pattern.compile(pat2);
        
        Matcher m = fnmasterp.matcher(tilefn);
        LOG.debug("Config pattern ? {} cnt={}", m.matches(), m.groupCount());
        Map<String, String> pairs = new HashMap<>();
        int minx=0;
        int miny=-1;
        int maxx=0;
        int maxy=-1;
        for (final File f: tileDir().listFiles()){
            try{
                Matcher m1 = fnp.matcher(f.getName());
                LOG.debug("file {} ? {} cnt={}", f.getName(),
                                                 m1.matches(), 
                                                 m1.groupCount());
                if (m1.matches() && m1.groupCount()==m.groupCount()){
                    pairs.clear();
                    for (int i=1; i<=m1.groupCount(); i++){
                        pairs.put(m.group(i), m1.group(i));
                    }
                    int x = Integer.parseInt(pairs.get("x"));
                    int y = Integer.parseInt(pairs.get("y"));
                    Map<Integer, Tile> row = maps.get(y);
                    if (row == null){
                        row = new TreeMap<>();
                        maps.put(y, row);
                    }
                    row.put(x, new FixedTile(f, x, y));
                    if (x>maxx) maxx=x;
                    if (y>maxy) maxy=y;
                    if (x<minx || minx==-1) minx=x;
                    if (y<miny || miny==-1) miny=y;
                }
            }catch(NumberFormatException e){
                LOG.warn("Data error "+e.getMessage(), e);
            }
        }
        LOG.debug("max x, y {} {}, min x y {} {}",maxx, maxy, minx, miny);
        Tile[][] tiles = new Tile[maxy+1][maxx+1];
        for (Entry<Integer, Map<Integer, Tile>> er: maps.entrySet()){
            for (Entry<Integer, Tile> et: er.getValue().entrySet()){
                LOG.debug("putting y={}, x={} = {}", er.getKey(), 
                                                     et.getKey(), 
                                                     et.getValue());
                tiles[er.getKey()][et.getKey()]=et.getValue();
            }
        }

        return new TileSetImpl(tileScale(), tileSize(), tile0Origin(), tiles,
            new XY(0,0), new XY(0,0), new XY(maxx+1, maxy+1).multiply(tileSize()));
    }

    public class FixedTile implements Tile{
        private final File f;
        private final XY pos;
        
        FixedTile(File f, int x, int y){
            this.f = f;
            pos = new XY(x, y);
        }
        @Override
        public String getIdent() {
            return f.getName();
        }
        @Override
        public BufferedImage imageData() throws TileException {
            try{
                return ImageIO.read(f);
            }catch (IOException e){
                throw new TileException(e);
            }
        }

        @Override
        public XYD scale() {
            return FixedMap.this.tileScale();
        }

        @Override
        public XYD origin() {
            return pos.xyd().multiply(scale()).multiply(size().xyd()).
                add(FixedMap.this.tile0Origin());
        }

        @Override
        public XY size() {
            return FixedMap.this.tileSize();
        }

        @Override
        public String toString() {
            return "FixedTile{" + "f=" + f + ", pos=" + pos + '}';
        }
        
    }

    @Override
    public String toString() {
        return "FixedMap{" + "base=" + base + '}';
    }
    
}
