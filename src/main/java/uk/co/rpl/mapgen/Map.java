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
public interface Map {
    TileSet tilesFor(XYD eastWest, double scale, XYD size);
    
}
