/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.awt.image.BufferedImage;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public interface Tile {
    /** unique identifier WITHIN THIS TileType
     * 
     * @return 
     */
    String getIdent() throws TileException;
    BufferedImage imageData() throws TileException;
    XYD scale() throws TileException;
    XYD origin() throws TileException;
    XY size() throws TileException;
    
}
