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
public class TileSetImpl  implements TileSet{
    private static final Logger LOG = getLogger(TileSetImpl.class);
    // meters per pixel
    private final XYD scale;
    // size of a tile in pixels
    private final XY tileSize;
    private final Tile[][] tiles;
    private final XYD baseEastWest;
    private final XYD eastWest;
    // identify tile 0,0 as indexes into tiles
    private final XY tile0;
    // identify size of tile grid
    private final XY noTiles;
    // pixel origin of required rectangle within first tile
    private final XY pixel0;
    private final XY pixels;

    TileSetImpl(TileSetImpl origin, XY pixel0, XY pixelSize){
        tiles=origin.tiles;
        scale = origin.scale;
        tileSize=origin.tileSize;
        baseEastWest=origin.baseEastWest;
        eastWest=baseEastWest.add(pixel0.xyd().multiply(scale));

        tile0 = pixel0.divide(tileSize).add(origin.tile0);
        this.pixel0 = pixel0.subtract(tile0.multiply(tileSize)).add(origin.pixel0);
        XY pnoTiles = pixel0.divide(tileSize);
        XY noTilesize = pnoTiles.multiply(tileSize);
        if (noTilesize.x<pixelSize.x) pnoTiles = pnoTiles.add(1, 0);
        if (noTilesize.y<pixelSize.y) pnoTiles = pnoTiles.add(0, 1);
        noTiles=pnoTiles;
        pixels = pixelSize;
    }

    public TileSetImpl(XYD scale, XY tileSize, XYD baseEastWest, 
                       Tile[][] tiles, XY tile0, 
                       XY pixel0, XY pixels) {
        this.scale = scale;
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.tile0 = tile0;
        this.noTiles = new XY(tiles[0].length, tiles.length);
        this.pixel0 = pixel0;
        this.pixels = pixels;
        this.baseEastWest=baseEastWest;
        eastWest=baseEastWest.add(pixel0.xyd().multiply(scale));
    }

    @Override
    public XY getTilesSize() {
        return tileSize;
    }

    @Override
    public XY getPixelOffset() {
        return pixel0;
    }

    @Override
    public XY getPixelSize() {
        return pixels;
    }

    @Override
    public Tile getTile(XY xy) {
        LOG.debug("tile  {}, size {}", xy, noTiles);
        if (xy.x>noTiles.x || xy.y>noTiles.y)return null;
        XY tid = xy.add(tile0);
        if (tid.y>=tiles.length) return null;
        if (tid.x>=tiles[tid.y].length) return null;
        return tiles[tid.y][tid.x];
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new XY(x,y));
    }

    @Override
    public XYD getEastNorth() {
        return eastWest;
    }

    @Override
    public XY noTiles() {
        return noTiles;
    }

    @Override
    public String toString() {
        return "TileSetImpl{" + "scale=" + scale + ", tileSize=" + tileSize +
               ", tiles=" + tiles + ", baseEastWest=" + baseEastWest + 
               ", eastWest=" + eastWest + ", tile0=" + tile0 + 
               ", noTiles=" + noTiles + ", pixel0=" + pixel0 + 
               ", pixels=" + pixels + '}';
    }

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
                                    tileSize.x*x, 
                                    tileSize.y*y, 
                                    null);
                } catch (TileException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        return out;
    }
    
}
