/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author philip
 */
public class BaseTileSetImpl  extends SubTileSupport{
    private static final Logger LOG = getLogger(BaseTileSetImpl.class);
    // meters per pixel
    private final XYD scale;
    // size of a tile in pixels
    private final XY tileSize;
    private final Tile[][] tiles;
    private final XYD eastNorth;
    private final XY noTiles;
    private final XY pixels;

    public BaseTileSetImpl(XYD scale, XY tileSize, XYD eastWest, Tile[][] tiles){
        this.scale = scale;
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.noTiles = new XY(tiles[0].length, tiles.length);
        this.pixels = noTiles.mul(tileSize);
        this.eastNorth=eastWest;
    }

    @Override
    public XY getTileSizePx() {
        return tileSize;
    }

    @Override
    public XY getTilesetSizePx() {
        return pixels;
    }

    @Override
    public Tile getTile(XY xy) {
        LOG.debug("tile  {}, size {}", xy, noTiles);
        if (xy.x<0 || xy.y<0) return null;
        if (xy.x>noTiles.x || xy.y>noTiles.y)return null;
        if (xy.y>=tiles.length) return null;
        if (tiles[xy.y]==null) return null;
        if (xy.x>=tiles[xy.y].length) return null;
        return tiles[xy.y][xy.x];
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new XY(x,y));
    }

    @Override
    public XYD getTilsetEastNorth() {
        return eastNorth;
    }

    @Override
    public XY noTilesInTilset() {
        return noTiles;
    }

    @Override
    public String toString() {
        return "BaseTileSetImpl{" + "scale=" + scale +
               ", tileSize=" + tileSize + ", tiles=" + tiles +
               ", eastWest=" + eastNorth + ", noTiles=" + noTiles +
               ", pixels=" + pixels + '}';
    }

    @Override
    public TileSet sub(XY pixelSize, XYD scale, XYD eastNorth) {
        return new SubTileSetImpl(this, pixelSize, scale, eastNorth);
    }

    @Override
    public XYD getScaleMpPx() {
        return scale;
    }

    @Override
    public XY getOffsetFromBasePx() {
        return XY.ZERO;
    }

    @Override
    public XYD getTileSizeM() {
        return getTileSizePx().xyd().mul(getScaleMpPx());
    }
    
}
