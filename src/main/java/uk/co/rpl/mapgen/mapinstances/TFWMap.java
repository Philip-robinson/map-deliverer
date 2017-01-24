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
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.Config;
import uk.co.rpl.mapgen.MapConfig;
import static uk.co.rpl.mapgen.MapConfig.SCALE_TYPE.TFW;
import uk.co.rpl.mapgen.Tile;
import uk.co.rpl.mapgen.TileSet;
import uk.co.rpl.mapgen.BaseTileSetImpl;
import uk.co.rpl.mapgen.XY;
import uk.co.rpl.mapgen.XYD;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author philip
 */
public class TFWMap implements MapConfig{
    private static final Logger LOG = getLogger(TFWMap.class);
    private final Config config;
    private final String base;
    private BaseTileSetImpl all;
    private final TileCacheManager cacheManager;
    public TFWMap(Config config, String baseName, TileCacheManager cacheManager){
        this.config=config;
        this.base=baseName;
        this.cacheManager = cacheManager;
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
       return config.getXY(base+".width", base+".height", null); 
    }

    @Override
    public XYD tileScale() {
        return config.getXYD(base+".scale-x", base+".scale-y", null);
    }

    @Override
    public XYD minTileScale() {
        return config.getXYD(base+".min-scale-x", base+".min-scale-y", null);
    }

    @Override
    public XYD maxTileScale() {
        return config.getXYD(base+".max-scale-x", base+".max-scale-y", null);
    }

    private XYD tile00Origin;
    @Override
    public synchronized XYD tile0Origin() {
        if (tile00Origin==null){
            try {
                tile00Origin=allTiles().getTilsetEastNorth();
            } catch (TileException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return tile00Origin;
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
        if (all!=null) return all;
        String dfn = dataFilename();
        String pat1 = dfn.replaceAll("\\(", "\\(").
                          replaceAll("\\)", "\\)").
                          replaceAll("\\$\\{[^\\}]*\\}",
                                     "\\\\\\$\\\\{([^\\\\}]*)\\\\}");
        LOG.debug("Pat 1 {}", pat1);
        String pat2 = dfn.replaceAll("\\(", "\\(").
                          replaceAll("\\)", "\\)").
                          replaceAll("\\$\\{[^\\}\\{]*\\}", "(.*)");
        LOG.debug("Pat 2 {}", pat2);
        final Pattern fnmasterp = Pattern.compile(pat1);
        final Pattern fnp = Pattern.compile(pat2);
        
        Matcher m = fnmasterp.matcher(dfn);
        LOG.debug("Config pattern ? {} cnt={}", m.matches(), m.groupCount());
        TreeMap<Double, Map<Double, Tile>> grid = new TreeMap<>();
        XYD scale = tileScale();
        XYD size = tileSize().xyd().mul(scale).abs();
        LOG.info("Loading map {} scale {} size {}", base, scale, size);
        TreeSet<Double> xind = new TreeSet<>();
        for (File f: dataDir().listFiles(fx->fnp.matcher(fx.getName()).matches())){
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
                LOG.debug("tile file is {}", tf);
                TFWTile t = new TFWTile(tf, tileId, f);
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
            double first = xind.first();
            double last = xind.last();
            double bottom=grid.firstKey();
            double top=grid.lastKey();
            int tilesEW=(int)Math.round((last-first)/size.x)+1;
            int tilesNS=(int)Math.round((top-bottom)/size.y)+1;
            LOG.debug("from east {} to west {} = {} tiles", first, last, tilesEW);
            LOG.debug("from bottom {} to top {} = {} tiles", bottom, top, tilesNS);
            Tile[][] tiles = new Tile[tilesNS][tilesEW];
            int y=0;
            for (double north = top; north >=bottom; north -= size.y){
                Map<Double, Tile>row = grid.get(north);
                int x=0;
                for (Double east=first; east <=last; east += size.x){
                    Tile t = row==null?null:row.get(east);
                    tiles[y][x]=t;
                    x+=1;
                }
                y+=1;
            }
            all = new BaseTileSetImpl(scale, tileSize(), 
                new XYD(xind.first(), grid.lastKey()), tiles,
                    cacheManager);
            return all;
        }else throw new TileException("no tiles");
    }

    @Override
    public String getInstance() {
        return base;
    }
    
    public class TFWTile implements Tile{
        private final File tileFile;
        private final String ident;
        private final XYD scale;
        private final XYD eastNorth;
        private XY size;
        private BufferedImage data;

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
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.tileFile);
            hash = 47 * hash + Objects.hashCode(this.ident);
            hash = 47 * hash + Objects.hashCode(this.scale);
            hash = 47 * hash + Objects.hashCode(this.eastNorth);
            hash = 47 * hash + Objects.hashCode(this.size);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TFWTile other = (TFWTile) obj;
            if (!Objects.equals(this.ident, other.ident)) {
                return false;
            }
            if (!Objects.equals(this.tileFile, other.tileFile)) {
                return false;
            }
            if (!Objects.equals(this.scale, other.scale)) {
                return false;
            }
            if (!Objects.equals(this.eastNorth, other.eastNorth)) {
                return false;
            }
            if (!Objects.equals(this.size, other.size)) {
                return false;
            }
            return true;
        }

        @Override
        public String getIdent() {
            return ident;
        }

        @Override
        public BufferedImage imageData() throws TileException {
            try {
                synchronized(tileFile){
                    if (data == null) data = ImageIO.read(tileFile);
                    return data;
                }
            } catch (IOException ex) {
                throw new TileException(ex);
            }finally{
                cacheManager.accessed(this);
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
            return TFWMap.this.tileSize();
        }

        @Override
        public String toString() {
            return "TFWTile{" + "tileFile=" + tileFile + ", ident=" + ident +
                   ", scale=" + scale + ", eastNorth=" + eastNorth + 
                   ", size=" + size + '}';
        }
        
        @Override
        public void flushCache() {
            synchronized(tileFile){
                data = null;
            }
        }
    }

    @Override
    public String toString() {
        return "TFWMap{" + "base=" + base + '}';
    }
    
}
