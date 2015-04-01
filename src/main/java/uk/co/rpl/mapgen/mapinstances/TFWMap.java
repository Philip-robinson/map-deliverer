/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.rpl.mapgen.Config;
import uk.co.rpl.mapgen.MapConfig;
import static uk.co.rpl.mapgen.MapConfig.SCALE_TYPE.TFW;
import uk.co.rpl.mapgen.Tile;
import uk.co.rpl.mapgen.TileSet;
import uk.co.rpl.mapgen.TileSetImpl;
import uk.co.rpl.mapgen.XY;
import uk.co.rpl.mapgen.XYD;

/**
 *
 * @author philip
 */
public class TFWMap implements MapConfig{
    private static final Logger LOG = LoggerFactory.getLogger(TFWMap.class);
    private final Config config;
    private final String base;
    public TFWMap(Config config, String baseName){
        this.config=config;
        this.base=baseName;
    }
    @Override
    public File tileDir() {
        return new File(config.get(base+".tile-dir"));
    }

    @Override
    public File dataDir() {
        return new File(config.get(base+".data-dir"));
    }

    @Override
    public SCALE_TYPE scaleType() {
        return TFW;
    }

    @Override
    public XY tileSize() {
       return null; 
    }

    @Override
    public XYD tileScale() {
        return null;
    }

    @Override
    public XYD tile0Origin() {
        return null;
    }

    @Override
    public String tileFilename() {
        return config.get(base+".tile-filename");
    }

    @Override
    public String dataFilename() {
        return config.get(base+".data-filename");
    }

    @Override
    public TileSet allTiles() throws TileException {
        String dfn = dataFilename();
        String pat1 = dfn.replaceAll("\\(", "\\(").replaceAll("\\)", "\\)").
                replaceAll("\\$\\{[^\\}]*\\}", "\\\\\\$\\\\{([^\\\\}]*)\\\\}");
        LOG.debug("Pat 1 {}", pat1);
        String pat2 = dfn.replaceAll("\\(", "\\(").replaceAll("\\)", "\\)").
                replaceAll("\\$\\{[^\\}\\{]*\\}", "(.*)");
        LOG.debug("Pat 2 {}", pat2);
        final Pattern fnmasterp = Pattern.compile(pat1);
        final Pattern fnp = Pattern.compile(pat2);
        
        Matcher m = fnmasterp.matcher(dfn);
        LOG.debug("Config pattern ? {} cnt={}", m.matches(), m.groupCount());
        TreeMap<Double, Map<Double, Tile>> grid = new TreeMap<>();
        XYD scale = null;
        XY size = null;
        TreeSet<Double> xind = new TreeSet<>();
        for (File f: dataDir().listFiles()){
            try{
                Matcher m1 = fnp.matcher(f.getName());
                String tileId=null;
                String newFn = tileFilename();
                if (m1.matches() && m1.groupCount()==m.groupCount()){
                    for (int i=1; i<=m1.groupCount(); i++){
                        if ("tileid".equals(m.group(i))) tileId = m1.group(i);
                        newFn = newFn.replaceAll("\\$\\{"+m.group(i)+"\\}",
                                                 m1.group(i));
                    }
                }
                LOG.debug("tileid -{}, new fn ={}", tileId, newFn);
                File tf = new File(tileDir(), newFn);
                LOG.debug("tile file is {}+{} {}", tileDir(), newFn, tf);
                TFWTile t = new TFWTile(tf, tileId, f);
                if (scale == null) scale = t.scale();
                if (size==null) size = t.size();
                if (scale.equals(t.scale)){
                    XYD o = t.origin();
                    Map<Double, Tile> row = grid.get(t.origin().y);
                    if (row == null){
                        row = new TreeMap<>();
                        LOG.debug("add y index to grid {}", o.y);
                        grid.put(o.y, row);
                    }
                    row.put(o.x, t);
                    xind.add(o.x);
                    LOG.debug("add x to xind {}", o.x);
                }else{
                    LOG.error("Tileset with variable tile scales "+
                              "cur {} new {} tile ignored", scale, t.scale());
                }
            }catch(TileException e){
                LOG.error(e.getMessage(), e);
            }
        }
        if (!grid.isEmpty() && !xind.isEmpty()){
            XY tileCnt = new XY(xind.size(), grid.size());
            Tile[][] tiles = new Tile[tileCnt.y][tileCnt.x];
            int y=0;
            for (Entry<Double, Map<Double, Tile>> row: grid.entrySet()){
                int x=0;
                for (Double east: xind){
                    Tile t = row.getValue().get(east);
                    tiles[y][x]=t;
                    x+=1;
                }
                y+=1;
            }
            return new TileSetImpl(scale, size, 
                new XYD(xind.first(), grid.lastKey()), 
                tiles,  new XY(0,0), new XY(0,0), tileCnt.multiply(size));
        }else throw new TileException("no tiles");
    }
    
    public class TFWTile implements Tile{
        private final File tileFile;
        private final String ident;
        private final XYD scale;
        private final XYD eastNorth;
        private XY size;

        public TFWTile(File tileFile, String ident, File dataFile)
            throws TileException {
            this.tileFile = tileFile;
            this.ident = ident;
            try(BufferedReader br = new BufferedReader(new FileReader(dataFile))){
                double scaleX=Double.parseDouble(br.readLine());
                br.readLine();br.readLine();
                double scaleY=Double.parseDouble(br.readLine());
                double east = Double.parseDouble(br.readLine());
                double north = Double.parseDouble(br.readLine());
                scale = new XYD(scaleX, scaleY);
                eastNorth = new XYD(east, north);
            } catch (NumberFormatException | IOException ex) {
                LOG.error("Problem reading tile data "+ex.getMessage(), ex);
                throw new TileException(ex);
            }
        }

        @Override
        public String getIdent() {
            return ident;
        }

        @Override
        public BufferedImage imageData() throws TileException {
            try {
                LOG.debug("Reading {}", tileFile);
                return ImageIO.read(tileFile);
            } catch (IOException ex) {
                LOG.warn("Faied reading tile "+ex.getMessage(), ex);
                throw new TileException(ex);
            }
        }

        @Override
        public XYD scale() {
            return scale;
        }

        @Override
        public XYD origin() {
            return eastNorth;
        }

        @Override
        public XY size() throws TileException {
            if (size==null){
                BufferedImage id = imageData();
                size = new XY(id.getWidth(), id.getHeight());
            }
            return size;
        }

        @Override
        public String toString() {
            return "TFWTile{" + "tileFile=" + tileFile + ", ident=" + ident +
                   ", scale=" + scale + ", eastNorth=" + eastNorth + 
                   ", size=" + size + '}';
        }
        
    }
}
