/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.io.File;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public interface MapConfig {
    File tileDir();
    File dataDir();
    SCALE_TYPE scaleType();
    XY tileSize();
    XYD tileScale();
    XYD tile0Origin();
    String tileFilename();
    String dataFilename();

    TileSet allTiles() throws TileException;
    public enum SCALE_TYPE{
        FIXED, TFW
    }
}
