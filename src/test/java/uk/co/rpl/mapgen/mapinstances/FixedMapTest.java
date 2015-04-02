/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static org.easymock.EasyMock.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import uk.co.rpl.mapgen.Config;
import static uk.co.rpl.mapgen.MapConfig.SCALE_TYPE.FIXED;
import uk.co.rpl.mapgen.Tile;
import uk.co.rpl.mapgen.TileSet;
import uk.co.rpl.mapgen.XY;
import uk.co.rpl.mapgen.XYD;

/**
 *
 * @author philip
 */
public class FixedMapTest {
    
    Config config;
    FixedMap inst;
    String BASE="333";
    int TILE_WIDE=543;
    int TILE_HIGH=367;
    double SCALE_X=3.3;
    double SCALE_Y=2.2;
    double EAST=50;
    double NORTH=600000;
    
    public FixedMapTest() {

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }

    @Before
    public void start(){
        config = createMock(Config.class);
        expect(config.get(BASE+".tile-dir")).andReturn("fixedMapTest");
        expect(config.getXY(BASE+".width", BASE+".height", null)).
                                andReturn(new XY(TILE_WIDE, TILE_HIGH)).anyTimes();
        expect(config.getXYD(BASE+".scale-x", BASE+".scale-y", null)).
                                andReturn(new XYD(SCALE_X, SCALE_Y)).anyTimes();
        expect(config.getXYD(BASE+".origin-x", BASE+".origin-y", null)).
                                andReturn(new XYD(EAST, NORTH)).anyTimes();
        expect(config.get(BASE+".tile-filename")).andReturn(
                                "MiniScale${y}x${x}.png").anyTimes();
        replay(config);

        inst = new FixedMap(config, BASE);
    }
    @Test
    public void testTileDir() {
        System.out.println("tileDir");
        assertEquals(new File("fixedMapTest"), inst.tileDir());
    }

    @Test
    public void testDataDir() {
        System.out.println("dataDir");
        assertNull(inst.dataDir());
    }

    @Test
    public void testScaleType() {
        System.out.println("scaleType");
        assertEquals(FIXED, inst.scaleType());
    }

    @Test
    public void testTileSize() {
        System.out.println("tileSize");
        XY xy = inst.tileSize();
        assertEquals(TILE_WIDE, xy.x);
        assertEquals(TILE_HIGH, xy.y);
    }

    @Test
    public void testTileScale() {
        System.out.println("tileScale");
        XYD xy = inst.tileScale();
        assertEquals(SCALE_X, xy.x, 0.000001);
        assertEquals(SCALE_Y, xy.y, 0.000001);
    }

    @Test
    public void testTile0Origin() {
        System.out.println("tile0Origin");
        XYD xy = inst.tile0Origin();
        assertEquals(EAST, xy.x, 0.000001);
        assertEquals(NORTH, xy.y, 0.000001);
    }

    @Test
    public void testTileFilename() {
        System.out.println("tileFilename");
        assertEquals("MiniScale${y}x${x}.png", inst.tileFilename());
    }

    @Test
    public void testDataFilename() {
        System.out.println("dataFilename");
        assertNull(inst.dataFilename());
    }
    
    @Test
    public void testAllTiles() throws TileException{
        System.out.println("allTiles");
        TileSet ts = inst.allTiles();
        assertEquals(0, ts.getPixelOffset().x);
        assertEquals(0, ts.getPixelOffset().y);
        assertEquals(19*TILE_WIDE, ts.getPixelSize().x);
        assertEquals(3*TILE_HIGH, ts.getPixelSize().y);
        assertEquals(TILE_WIDE, ts.getTilesSize().x);
        assertEquals(TILE_HIGH, ts.getTilesSize().y);
        assertEquals(EAST, ts.getEastNorth().x, 0.000001);
        assertEquals(NORTH, ts.getEastNorth().y, 0.000001);

        for (int x=0; x<16; x++)
            for (int y=0; y<3; y++){
                assertNull(ts.getTile(x, y));
                assertNull(ts.getTile(new XY(x, y)));
        }
        for (int y=0; y<3; y++)for (int x=16; x<19; x++){
            Tile t = ts.getTile(x, y);
            System.out.println("Got x="+x+", y="+y+" =>"+t);
            assertTrue("testing x="+x+" y="+y, t.getIdent().contains(""+x));
            assertTrue("testing x="+x+" y="+y, t.getIdent().contains(""+y));
            assertEquals(t.getIdent(), ts.getTile(new XY(x, y)).getIdent());
            assertNotNull(t.imageData());
            BufferedImage bi = t.imageData();
            assertNotNull(bi);
            assertEquals(200, bi.getHeight());
            assertEquals(200, bi.getWidth());
        }
    }

    @Test
    public void testImage() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        reset(config);
        expect(config.get(BASE+".tile-dir")).andReturn("fixedMapTest");
        expect(config.getXY(BASE+".width", BASE+".height", null)).
                                andReturn(new XY(200, 200)).anyTimes();
        expect(config.getXYD(BASE+".scale-x", BASE+".scale-y", null)).
                                andReturn(new XYD(25.0, 25.0)).anyTimes();
        expect(config.getXYD(BASE+".origin-x", BASE+".origin-y", null)).
                                andReturn(new XYD(0, 1000)).anyTimes();
        expect(config.get(BASE+".tile-filename")).andReturn(
                                "MiniScale${y}x${x}.png").anyTimes();
        replay(config);

        inst = new FixedMap(config, BASE);

        BufferedImage bi = inst.allTiles().getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File("/tmp/fixedFullTest.png"))){
            ImageIO.write(bi, "png", out);
        }
    }

    @Test
    public void testSubImage() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        reset(config);
        expect(config.get(BASE+".tile-dir")).andReturn("fixedMapTest");
        expect(config.getXY(BASE+".width", BASE+".height", null)).
                                andReturn(new XY(200, 200)).anyTimes();
        expect(config.getXYD(BASE+".scale-x", BASE+".scale-y", null)).
                                andReturn(new XYD(25.0, 25.0)).anyTimes();
        expect(config.getXYD(BASE+".origin-x", BASE+".origin-y", null)).
                                andReturn(new XYD(0, 1000)).anyTimes();
        expect(config.get(BASE+".tile-filename")).andReturn(
                                "MiniScale${y}x${x}.png").anyTimes();
        replay(config);

        inst = new FixedMap(config, BASE);

        BufferedImage bi = inst.allTiles().getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File("/tmp/fixedFullPart.png"))){
            ImageIO.write(bi, "png", out);
        }
    }

}
