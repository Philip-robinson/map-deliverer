/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.io.File;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public class MapConfigTest {
    
    MapConfig inst;
    public MapConfigTest() {
    }

    @Before
    public void setup(){
        inst = new MapConfig() {

             @Override
             public String getInstance() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public File tileDir() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public File dataDir() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public MapConfig.SCALE_TYPE scaleType() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public XY tileSize() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public XYD tileScale() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public XYD maxTileScale() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public XYD minTileScale() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public XYD tile0Origin() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public String tileFilename() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public String dataFilename() {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public TileSet allTiles() throws TileException {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }
         };
    }

     @Test
     public void gvaActToMax(){
          assertEquals(1.0, inst.gva(1, 1, 0.5, 10), 0.01);
          assertEquals(0.95, inst.gva(2, 1, 0.5, 10), 0.01);
          assertEquals(0.91, inst.gva(3, 1, 0.5, 10), 0.01);
          assertEquals(0.86, inst.gva(4, 1, 0.5, 10), 0.01);
          assertEquals(0.82, inst.gva(5, 1, 0.5, 10), 0.01);
          assertEquals(0.77, inst.gva(6, 1, 0.5, 10), 0.01);
          assertEquals(0.73, inst.gva(7, 1, 0.5, 10), 0.01);
          assertEquals(0.69, inst.gva(8, 1, 0.5, 10), 0.01);
          assertEquals(0.65, inst.gva(9, 1, 0.5, 10), 0.01);
          assertEquals(0.6, inst.gva(10, 1, 0.5, 10), 0.01);
     }

     @Test
     public void gvaMinTo0(){
          assertEquals(0.6, inst.gva(15, 25, 15, 50), 0.01);
          assertEquals(0.5, inst.gva(12, 25, 15, 50), 0.01);
          assertEquals(0.45, inst.gva(10, 25, 15, 50), 0.01);
          assertEquals(0.39, inst.gva(7, 25, 15, 50), 0.01);
          assertEquals(0.36, inst.gva(5, 25, 15, 50), 0.01);
          assertEquals(0.31, inst.gva(1, 25, 15, 50), 0.01);
     }

     @Test
     public void gvaActToMin(){
          assertEquals(1.0, inst.gva(25, 25, 15, 50), 0.01);
          assertEquals(0.92, inst.gva(23, 25, 15, 50), 0.01);
          assertEquals(0.84, inst.gva(21, 25, 15, 50), 0.01);
          assertEquals(0.76, inst.gva(19, 25, 15, 50), 0.01);
          assertEquals(0.68, inst.gva(17, 25, 15, 50), 0.01);
          assertEquals(0.60, inst.gva(15, 25, 15, 50), 0.01);
     }

     @Test
     public void gvaMaxUp(){
          assertEquals(0.6, inst.gva(10, 1, 0.5, 10), 0.01);
          assertEquals(0.4, inst.gva(15, 1, 0.5, 10), 0.01);
          assertEquals(0.3, inst.gva(20, 1, 0.5, 10), 0.01);
          assertEquals(0.24, inst.gva(25, 1, 0.5, 10), 0.01);
          assertEquals(0.2, inst.gva(30, 1, 0.5, 10), 0.01);
          assertEquals(0.17, inst.gva(35, 1, 0.5, 10), 0.01);
          assertEquals(0.15, inst.gva(40, 1, 0.5, 10), 0.01);
          assertEquals(0.06, inst.gva(100, 1, 0.5, 10), 0.01);
     }
}
