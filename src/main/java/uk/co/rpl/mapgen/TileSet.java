/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.image.BufferedImage;

/**
 *
 * @author philip
 */
public interface TileSet {
    /** size of the tile in pixels
     * 
     * @return 
     */
    XY getTileSize();
    /** pixel offset from origin of the first tile to
     * the origin of the map rectangle
     * @return 
     */
    XY getPixelOffset();
    /**
     * overall size of the map rectangle in pixels
     * @return 
     */
    XY getPixelSize();
    /**
     * get the scale in meters per pixel
     * @return X will be positive and y will be negative (indicating 
     * pixel 2 is below pixel 1
     */
    XYD getScale();
    /** the number of tiles required to wholy include the rectangle
     * 
     * @return 
     */
    XY noTiles();
    /** get tile xy
     * tile 0,0 is top left hand side
     * tile 1, 1 is one down and one right
     * @param xy
     * @return 
     */
    Tile getTile(XY xy);
    Tile getTile(int x, int y);
    /** eastings/northings of the pixel offset point of tile 0,0
     * 
     * @return 
     */
    XYD getEastNorth();

    /** get a sub tile
     * 
     * @param pixelSize the size of the map rectangle in pixels
     * @param scale in meters per pixel (y negative)
     * @param eastNorth easting/nothings of the map rectangle
     * @return 
     */
    TileSet sub(XY pixelSize, XYD scale, XYD eastNoth);

    /** get a buffered image, this will be a rectangular image of pixelSize
     * pixels
     * @return 
     */
    BufferedImage getImage();
}
