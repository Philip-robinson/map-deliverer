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
    XY getTilesSize();
    XY getPixelOffset();
    XY getPixelSize();
    Tile getTile(XY xy);
    Tile getTile(int x, int y);
    XYD getEastNorth();

    XY noTiles();
    BufferedImage getImage();
}
