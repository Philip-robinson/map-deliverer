/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.mapinstances.TileCacheManager;
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
    private final XY tileSizePx;
    private final XY firstTile;
    private final TileCacheManager cacheManager;
    private static final Color PALE_BLUE = new Color(127, 127, 255);

    public SubTileSetImpl(TileSet origin, XY size,
                          XYD scale, XYD eastNorth,
                          TileCacheManager cacheManager) {
        LOG.debug("Origin={}, size={}, scale={}, eastnorth={}", 
                  origin, size, scale, eastNorth);
        this.origin = origin;
        this.size = size;
        this.scale = scale;
        this.eastNorth = eastNorth;
        final XYD origEN = origin.getTilsetEastNorth();
        final XYD eastNorthDiff = eastNorth.sub(origEN);
        final XYD origScale = origin.getScaleMpPx();
        final XYD tileSizeM = origin.getTileSizeM();
        final XYD offsetTiles = eastNorthDiff.div(tileSizeM);
        firstTile = offsetTiles.xy();
        relativeScale = scale.div(origScale);
        final XYD offsetInFirstTileM = offsetTiles.sub(firstTile.xyd()).mul(tileSizeM);
        final XYD sizeMeters = size.xyd().mul(scale);
        final XYD metersOfTilesRequired = sizeMeters.add(offsetInFirstTileM);
        noTiles = metersOfTilesRequired.div(tileSizeM).ceil();
        pixelOffset = offsetInFirstTileM.div(scale).round();
        tileSizePx = tileSizeM.div(scale).round();
        this.cacheManager=cacheManager;
        if (LOG.isTraceEnabled()){
            LOG.trace("base tileset origin eastnorth {}", origEN);
            LOG.trace("this tileset origin eastnorth {}", eastNorth);
            LOG.trace("Offset to new origin m        {}", eastNorthDiff);
            LOG.trace("Tile size in meters           {}", tileSizeM);
            LOG.trace("offset from base tiles        {}", offsetTiles);
            LOG.trace("Original scale                {}", origScale);
            LOG.trace("Relative scale                {}", relativeScale);
            LOG.trace("Offset in m within first tile {}", offsetInFirstTileM);
            LOG.trace("Tile Set size in m            {}", sizeMeters);
            LOG.trace("Number of tiles               {}", noTiles);
            LOG.trace("Offset from first tile px     {}", pixelOffset);
            LOG.trace("Tile size px                  {}", tileSizePx);
        }
    }

    @Override
    public XY getTileSizePx() {
        return tileSizePx;
    }

    @Override
    public XY getOffsetFromBasePx() {
        return pixelOffset;
    }

    @Override
    public XY getTilesetSizePx() {
        return size;
    }

    @Override
    public XYD getScaleMpPx() {
        return scale;
    }

    @Override
    public XY noTilesInTilset() {
        return noTiles;
    }

    @Override
    public Tile getTile(XY xy) {
        return new ScaledTile(origin.getTile(xy), relativeScale, cacheManager);
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new XY(x, y));
    }

    @Override
    public XYD getTilsetEastNorth() {
        return eastNorth;
    }

    @Override
    public TileSet sub(XY pixelSize, XYD scale, XYD eastNorth) {
        return new SubTileSetImpl(this, pixelSize, scale, eastNorth, cacheManager);
    }

    @Override
    public BufferedImage getImage() {
        LOG.trace("SubTileSet.getImage");
        BufferedImage out = new BufferedImage(getTilesetSizePx().x, 
                                              getTilesetSizePx().y, 
                                              TYPE_INT_RGB);
        for (int x=0; x<noTilesInTilset().x; x++){
            for (int y=0; y<noTilesInTilset().y; y++){

                XY xy = new XY(x, y);
                XY tileNo = xy.add(firstTile);
                XY offset = xy.mul(getTileSizePx()).sub(pixelOffset);
                Tile t = getTile(tileNo);
                LOG.debug("Tile no {}, offset {} = {}", tileNo, offset, t);
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
                    g.setColor(PALE_BLUE);
                    g.fillRect(offset.x, 
                               offset.y, 
                               getTilesetSizePx().x,
                               getTilesetSizePx().y);
                    g.setColor(Color.BLUE);
                    char[] data = ("X="+x+", y="+y).toCharArray();
                    g.drawChars(data, 0, data.length, 5, 5);
                }
            }
        }
        return out;
    }

    @Override
    public XYD getTileSizeM() {
        return getTileSizePx().xyd().mul(getScaleMpPx());
    }

}
