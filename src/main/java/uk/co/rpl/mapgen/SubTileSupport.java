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
            BufferedImage out = new BufferedImage(getPixelSize().x, 
                                              getPixelSize().y, 
                                              BufferedImage.TYPE_INT_BGR);
        for (int x=0; x<noTiles().x; x++){
            for (int y=0; y<noTiles().y; y++){
                Tile t = getTile(x, y);
                if (t!=null) try{
                    out.getGraphics().drawImage(t.imageData(), 
                                    getTileSize().x*x, 
                                    getTileSize().y*y, 
                                    null);
                } catch (TileException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        return out;
    }
}
