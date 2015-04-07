/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_BGR;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public class ScaledTile implements Tile {
    private final Tile oTile;
    private final XYD relScale;

    public ScaledTile(Tile tile, XYD relScale) {
        oTile = tile;
        this.relScale = relScale;
    }

    @Override
    public String getIdent() throws TileException {
        return oTile.getIdent()+"*"+relScale.toString();
    }

    @Override
    public BufferedImage imageData() throws TileException {
        if (oTile==null) return null;
        BufferedImage bi = oTile.imageData();
        if (relScale.x==1 && relScale.y==1) return bi;
        BufferedImage nbi = new BufferedImage((int)(bi.getWidth()/relScale.x), 
                                              (int)(bi.getHeight()/relScale.y),
                                              TYPE_INT_BGR);
        nbi.getGraphics().drawImage(bi, 0, 0, nbi.getWidth(), nbi.getHeight(),
                                                              null);
        return nbi;
        
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
    
}
