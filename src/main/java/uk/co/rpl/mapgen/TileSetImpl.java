/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

/**
 *
 * @author philip
 */
public class TileSetImpl  implements TileSet{
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

    public TileSetImpl(XYD scale, XY tileSize, XYD baseEastWest, XYD eastWest,
                       Tile[][] tiles, XY tile0, XY noTiles, 
                       XY pixel0, XY pixels) {
        this.scale = scale;
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.tile0 = tile0;
        this.noTiles = noTiles;
        this.pixel0 = pixel0;
        this.pixels = pixels;
        this.baseEastWest=baseEastWest;
        this.eastWest=eastWest;
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
        if (xy.x>=noTiles.x || xy.y>noTiles.y)return null;
        XY tid = xy.add(tile0);
        if (tid.x>=tiles.length) return null;
        if (tid.y>=tiles[tid.x].length) return null;
        return tiles[tid.x][tid.y];
    }

    @Override
    public Tile getTile(int x, int y) {
        return getTile(new XY(x,y));
    }

    @Override
    public XYD getEastWest() {
        return eastWest;
    }
    
}
