/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public abstract class SubTileSupport implements TileSet{
    private static Logger LOG = getLogger(SubTileSupport.class);

    @Override
    public BufferedImage getImage() {
        LOG.trace("getImage");
        BufferedImage out = new BufferedImage(getTilesetSizePx().x, 
                                              getTilesetSizePx().y, 
                                              BufferedImage.TYPE_INT_RGB);
        LOG.debug("Created image {}x{}", getTilesetSizePx().x, getTilesetSizePx().y);
        for (int x=0; x<noTilesInTilset().x; x++){
            for (int y=0; y<noTilesInTilset().y; y++){
                Tile t = getTile(x, y);
                if (t!=null){
                    try{
                        final XYD en = getTilsetEastNorth();
                        final XYD or = t.origin();
                        final XYD sc = getScaleMpPx();
                        final XYD om = or.sub(en);
                        final XYD op = om.div(sc);
                        final XY point =op.ceil();
                        if (LOG.isDebugEnabled()){
                            LOG.debug("Image origin  = {}", en);
                            LOG.debug("Tile origin   = {}", or);
                            LOG.debug("Offset meters = {}", om);
                            LOG.debug("Scale         = {}", sc);
                            LOG.debug("Offset pixels = {}", op);
                            LOG.debug("Offset int px = {}", point);
                        }
                        LOG.debug("Draw at {}", point);
                    
                        out.getGraphics().drawImage(t.imageData(), 
                                    point.x, 
                                    point.y, 
                                    null);
                    } catch (TileException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                }else{
                    LOG.debug("Tile not exists {} {}", x, y);
                }
            }
        }
        return out;
    }
}
