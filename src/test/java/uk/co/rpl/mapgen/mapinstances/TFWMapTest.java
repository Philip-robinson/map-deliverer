package uk.co.rpl.mapgen.mapinstances;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import uk.co.rpl.mapgen.Config;
import static uk.co.rpl.mapgen.MapConfig.SCALE_TYPE.TFW;
import uk.co.rpl.mapgen.TileSet;
import uk.co.rpl.mapgen.XY;
import uk.co.rpl.mapgen.XYD;

/**
 *
 * @author philip
 */
public class TFWMapTest {

    Config config;
    TFWMap inst;
    String BASE="XYZZY";

    double EAST=30;
    double NORTH=750000;

    String TILE_DIR="mapTestTFW";
    String DATA_DIR="mapTestTFW/TFW";
    String TILE_FN="${tileid}.png";
    String DATA_FN="${tileid}.TFW";
    public TFWMapTest() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }

    @Before
    public void start(){
        config = createMock(Config.class);
        expect(config.get(BASE+".tile-dir")).andReturn(TILE_DIR).anyTimes();
        expect(config.get(BASE+".data-dir")).andReturn(DATA_DIR).anyTimes();
        expect(config.getXYD(BASE+".origin-x", BASE+".origin-y", null)).
                                andReturn(new XYD(EAST, NORTH)).anyTimes();
        expect(config.get(BASE+".tile-filename")).andReturn(TILE_FN).anyTimes();
        expect(config.get(BASE+".data-filename")).andReturn(DATA_FN).anyTimes();
        replay(config);

        inst = new TFWMap(config, BASE);
    }
    @Test
    public void testTileDir() {
        System.out.println("tileDir");
        assertEquals(new File(TILE_DIR), inst.tileDir());
    }

    @Test
    public void testDataDir() {
        System.out.println("dataDir");
        assertEquals(new File(DATA_DIR), inst.dataDir());
    }

    @Test
    public void testScaleType() {
        System.out.println("scaleType");
        assertEquals(TFW, inst.scaleType());
    }

    @Test
    public void testTileSize() {
        System.out.println("tileSize");
        assertNull(inst.tileSize());
    }

    @Test
    public void testTileScale() {
        System.out.println("tileScale");
        assertNull(inst.tileScale());
    }

    @Test
    public void testTile0Origin() {
        System.out.println("tile0Origin");
        assertNull(inst.tile0Origin());
    }

    @Test
    public void testTileFilename() {
        System.out.println("tileFilename");
        assertEquals(TILE_FN, inst.tileFilename());
    }

    @Test
    public void testDataFilename() {
        System.out.println("dataFilename");
        assertEquals(DATA_FN, inst.dataFilename());
    }

    @Test
    public void testAllTiles() throws TileException {
        System.out.println("allTiles");
        TileSet ts = inst.allTiles();
        assertEquals(100012.5, ts.getEastNorth().x, 0.000001);
        assertEquals(799987.5, ts.getEastNorth().y, 0.000001);
        assertEquals(new XY(0,0), ts.getPixelOffset());
        assertEquals(new XY(16000, 12000), ts.getPixelSize());
        assertEquals(new XY(4000, 4000), ts.getTileSize());
        XY nt = ts.noTiles();
        for (int y=0; y<nt.y; y++) {
            for (int x=0; x<nt.x; x++){
                System.out.println("x"+x+", y"+y+", tile "+ts.getTile(x, y));
            }
        }

        assertNull(ts.getTile(0, 0));
        assertEquals("NX", ts.getTile(1,0).getIdent());
        assertEquals("NY", ts.getTile(2,0).getIdent());
        assertNull(ts.getTile(3, 0));

        assertNull(ts.getTile(0, 1));
        assertEquals("NS", ts.getTile(1,1).getIdent());
        assertEquals("NT", ts.getTile(2,1).getIdent());
        assertEquals("NU", ts.getTile(3,1).getIdent());

        assertEquals("NM", ts.getTile(0,2).getIdent());
        assertEquals("NN", ts.getTile(1,2).getIdent());
        assertEquals("NO", ts.getTile(2,2).getIdent());
        assertNull(ts.getTile(3, 2));
    }
    
    @Test
    public void testImage() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("A");
        BufferedImage bi = inst.allTiles().getImage();
        System.out.println("B");
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File("aaa.png"))){
            System.out.println("C");
            ImageIO.write(bi, "png", out);
            System.out.println("D");
        }
        System.out.println("E");
        
    }
}
