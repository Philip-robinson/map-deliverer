/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_BGR;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public class SubTileSetImpl implements TileSet{

    private static final Logger LOG = getLogger(SubTileSetImpl.class);
    private final TileSet origin;
    private final XY size;
    private final XY pixelOffset;
    private final XYD scale;
    private final XYD relativeScale;
    private final XY noTiles;
    private final XYD eastNorth;
    private final XY tileOffset;
    private final XY tileSize;

    public SubTileSetImpl(TileSet origin, XY size, XYD scale, XYD eastNorth) {
        this.origin = origin;
        this.size = size;
        this.scale = scale;
        this.eastNorth = eastNorth;
        XYD eastNorthDiff = eastNorth.sub(origin.getEastNorth());
        LOG.debug("o eastnorth {}, eastnort {}, diff {}",
                  origin.getEastNorth(), eastNorth, eastNorthDiff);
        XYD tileSizeMeters =origin.getTileSize().xyd().times(origin.getScale());
        LOG.debug("Tile size in meters {}", tileSizeMeters);
        tileOffset = eastNorthDiff.div(tileSizeMeters).xy();
        LOG.debug("tile off set {}", tileOffset);
        this.relativeScale = scale.div(origin.getScale());
        LOG.debug("Relative scale is {}", relativeScale);
        XYD offsetMetersWithinFirsTile = eastNorthDiff.sub(
                                tileOffset.xyd().mul(tileSizeMeters));
        LOG.debug("Offset in meters - first tile {}", offsetMetersWithinFirsTile);
        XYD sizeMeters = size.xyd().mul(scale);
        XYD metersOfTilesRequired = sizeMeters.add(offsetMetersWithinFirsTile);
        noTiles = metersOfTilesRequired.div(tileSizeMeters).ceil();
        LOG.debug("Number of tiles {}", noTiles);
        pixelOffset = offsetMetersWithinFirsTile.div(scale).xy();
        tileSize = origin.getTileSize().xyd().div(relativeScale).xy();
    }

    @Override
    public XY getTileSize() {
        return tileSize;
    }

    @Override
    public XY getPixelOffset() {
        return pixelOffset;
    }

    @Override
    public XY getPixelSize() {
        return size;
    }

    @Override
    public XYD getScale() {
        return scale;
    }

    @Override
    public XY noTiles() {
        return noTiles;
    }

    @Override
    public Tile getTile(XY xy) {
        return new ScaledTile(origin.getTile(xy), relativeScale);
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new XY(x, y));
    }

    @Override
    public XYD getEastNorth() {
        return eastNorth;
    }

    @Override
    public TileSet sub(XY pixelSize, XYD scale, XYD eastNorth) {
        return new SubTileSetImpl(this, pixelSize, scale, eastNorth);
    }

    @Override
    public BufferedImage getImage() {
        BufferedImage out = new BufferedImage(getPixelSize().x, 
                                              getPixelSize().y, 
                                              TYPE_INT_BGR);
        for (int x=0; x<noTiles().x; x++){
            LOG.debug("x="+x);
            for (int y=0; y<noTiles().y; y++){
                LOG.debug("y="+y);

                XY xy = new XY(x, y);
                XY tileNo = xy.add(tileOffset);
                XY offset = xy.mul(getTileSize()).sub(pixelOffset);
                Tile t = getTile(tileNo);
                LOG.debug("Tile no {} = {}", tileNo, t);
                if (t!=null){
                    try{
                        out.getGraphics().drawImage(t.imageData(), 
                                        offset.x, 
                                        offset.y, 
                                        null);
                    } catch (TileException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                }else{
                    Graphics g = out.getGraphics();
                    g.setColor(Color.blue);
                    g.fillRect(offset.x, 
                               offset.y, 
                               getPixelSize().x,
                               getPixelSize().y);
                    char[] data = ("X="+x+", y="+y).toCharArray();
                    g.drawChars(data, 0, data.length, 5, 5);
                }
            }
        }
        return out;
    }

}
