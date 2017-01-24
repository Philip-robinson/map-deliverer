/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_BGR;
import java.util.Objects;
import org.slf4j.Logger;
import uk.co.rpl.mapgen.mapinstances.TileCacheManager;
import uk.co.rpl.mapgen.mapinstances.TileException;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author philip
 */
public class ScaledTile implements Tile {
    public static Logger LOG = getLogger(ScaledTile.class);
    private final Tile oTile;
    private final XYD relScale;
    private final TileCacheManager cacheManager;

    public ScaledTile(Tile tile, XYD relScale, TileCacheManager cacheManager) {
        oTile = tile;
        this.relScale = relScale;
        this.cacheManager = cacheManager;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.oTile);
        hash = 43 * hash + Objects.hashCode(this.relScale);
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
        final ScaledTile other = (ScaledTile) obj;
        if (!Objects.equals(this.oTile, other.oTile)) {
            return false;
        }
        if (!Objects.equals(this.relScale, other.relScale)) {
            return false;
        }
        return true;
    }

    @Override
    public String getIdent() throws TileException {
        return oTile.getIdent()+"*"+relScale.toString();
    }

    @Override
    public BufferedImage imageData() throws TileException {
        while (true){
            try{
                if (oTile==null) return null;
                BufferedImage bi = oTile.imageData();
                if (relScale.x==1 && relScale.y==1) return bi;
                BufferedImage nbi = new BufferedImage(
                    (int)(bi.getWidth()/relScale.x), 
                    (int)(bi.getHeight()/relScale.y),
                    TYPE_INT_BGR);
                nbi.getGraphics().drawImage(bi, 0, 0, 
                    nbi.getWidth(), nbi.getHeight(), null);
                return nbi;
            }catch(OutOfMemoryError e){
                LOG.info("Out of memory Exception - recovering");
                try {
                    cacheManager.partialCacheFlush();
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new TileException(ex);
                }
            }
        }
        
    }

    @Override
    public XYD scale() throws TileException {
        return oTile.scale().mul(relScale);
    }

    @Override
    public XYD origin() throws TileException {
        return oTile.origin();
    }

    @Override
    public XY size() throws TileException {
        return oTile.size().xyd().div(relScale).xy();
    }

    @Override
    public String toString() {
        return "ScaledTile{" + "oTile=" + oTile + ", relScale=" + relScale + '}';
    }

    @Override
    public void flushCache() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
