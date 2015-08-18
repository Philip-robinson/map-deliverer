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
public interface MapAO {
    /**get a tileset for origin eastNorth at scale m/px for size pixels
     * 
     * @param eastNorth origin UK easting and Northings
     * @param scale output metters per pixel
     * @param size output size in pixels
     * @return Telset as above
     */
    TileSet tilesFor(XYD eastNorth, XYD scale, XY size);
    
}
