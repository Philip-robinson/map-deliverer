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
import org.junit.Ignore;
import uk.co.rpl.mapgen.Config;
import static uk.co.rpl.mapgen.MapConfig.SCALE_TYPE.TFW;
import uk.co.rpl.mapgen.Tile;
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
    File dir = new File("/tmp/TEST-MAP-IMAGES/TFWMapTestImages/");
    public TFWMapTest() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }

    @Before
    public void start(){
        if (!dir.exists()) dir.mkdirs();
        config = createMock(Config.class);
        expect(config.get(BASE+".tile-dir")).andReturn(TILE_DIR).anyTimes();
        expect(config.get(BASE+".data-dir")).andReturn(DATA_DIR).anyTimes();
        expect(config.getXYD(BASE+".origin-x", BASE+".origin-y", null)).
                                andReturn(new XYD(EAST, NORTH)).anyTimes();
        expect(config.get(BASE+".tile-filename")).andReturn(TILE_FN).anyTimes();
        expect(config.get(BASE+".data-filename")).andReturn(DATA_FN).anyTimes();
        expect(config.getXY(BASE+".width", BASE+".height", null)).
            andReturn(new XY(4000, 4000)).anyTimes();
        expect(config.getXYD(BASE+".scale-x", BASE+".scale-y", null)).
            andReturn(new XYD(25.0, 25.0)).anyTimes();
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
        assertEquals(4000, inst.tileSize().x);
        assertEquals(4000, inst.tileSize().y);
    }

    @Test
    public void testTileScale() {
        System.out.println("tileScale");
        assertEquals(25.0, inst.tileScale().x, 0.01);
        assertEquals(25.0, inst.tileScale().y, 0.01);
    }

    @Test @Ignore
    public void testTile0Origin() {
        System.out.println("tile0Origin");
        assertEquals(100012.5, inst.tile0Origin().x, 0.01);
        assertEquals(799987.5, inst.tile0Origin().y, 0.01);
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

    @Test @Ignore
    public void testAllTiles() throws TileException {
        System.out.println("allTiles");
        TileSet ts = inst.allTiles();
        assertEquals(100012.5, ts.getTilsetEastNorth().x, 0.000001);
        assertEquals(799987.5, ts.getTilsetEastNorth().y, 0.000001);
        assertEquals(new XY(0,0), ts.getOffsetFromBasePx());
        assertEquals(new XY(16000, 12000), ts.getTilesetSizePx());
        assertEquals(new XY(4000, 4000), ts.getTileSizePx());
        XY nt = ts.noTilesInTilset();
        for (int y=0; y<nt.y; y++) {
            for (int x=0; x<nt.x; x++){
                System.out.println("x"+x+", y"+y+", tile "+ts.getTile(x, y));
            }
        }


        assertNull(ts.getTile(0, 2));
        assertEquals("NX", ts.getTile(1,2).getIdent());
        assertEquals("NY", ts.getTile(2,2).getIdent());
        assertNull(ts.getTile(3, 2));

        assertNull(ts.getTile(0, 1));
        assertEquals("NS", ts.getTile(1,1).getIdent());
        assertEquals("NT", ts.getTile(2,1).getIdent());
        assertEquals("NU", ts.getTile(3,1).getIdent());

        assertEquals("NM", ts.getTile(0,0).getIdent());
        assertEquals("NN", ts.getTile(1,0).getIdent());
        assertEquals("NO", ts.getTile(2,0).getIdent());
        assertNull(ts.getTile(3, 0));
    }
    
//    @Test
//    public void imageTile00() throws TileException, IOException{
//        for (int x =0; x<4; x++){
//            for (int y=0; y<3; y++){
//                imageTile(x, y);
//
//            }
//        }
//    }
    public void imageTile(int x, int y) throws TileException, IOException{
        TileSet ts = inst.allTiles();
        Tile t = ts.getTile(x, y);
        if (t!=null){
            try{
                BufferedImage bi = t.imageData();
                try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                            new File(dir, "tile-"+x+"-"+y+".png"))){
                    ImageIO.write(bi, "png", out);
                }
            }catch(TileException e){
                System.out.println("No tile at "+x+", "+y);
            }
        }else{
            System.out.println("No tile at "+x+", "+y);

        }
    }
    @Test @Ignore
    public void testWholeImage() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("TestWholeImage");
        if (!dir.exists()) dir.mkdirs();
        System.out.println("A");
        BufferedImage bi = inst.allTiles().getImage();
        System.out.println("B");
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "all-8-tiles.png"))){
            System.out.println("C");
            ImageIO.write(bi, "png", out);
            System.out.println("D");
        }
        System.out.println("E");
        
    }
    @Test @Ignore
    public void testSubImageAtOriginSameScale() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("testSubImageAtOriginSameScale");
        System.out.println("A");
        TileSet base = inst.allTiles();
        TileSet sub = base.sub(new XY(410,410), new XYD(25,-25), 
                                                base.getTilsetEastNorth());
        BufferedImage bi = sub.getImage();
        System.out.println("B");
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "410x410+0x0.png"))){
            System.out.println("C");
            ImageIO.write(bi, "png", out);
            System.out.println("D");
        }
        System.out.println("E");
        
    }
    @Test @Ignore
    public void testSubImageAtOriginSameScale2() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("testSubImageAtOriginSameScale2");
        TileSet base = inst.allTiles();
        TileSet sub = base.sub(new XY(500,800), new XYD(25,-25), 
                                      base.getTilsetEastNorth());
        BufferedImage bi = sub.getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "500x800@0x0.png"))){
            ImageIO.write(bi, "png", out);
        }
    }
    @Test @Ignore
    public void testSubImageAtOriginS100x100ale2() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("testSubImageAtOriginS100x100ale2");
        TileSet base = inst.allTiles();
        TileSet sub = base.sub(new XY(500,800), new XYD(25,-25), 
                                      base.getTilsetEastNorth().add(1000.0, -1000.0));
        BufferedImage bi = sub.getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "500x800@100x100.png"))){
            ImageIO.write(bi, "png", out);
        }
    }
    @Test @Ignore
    public void testSubImageCrossTileBoundaryS93750x90000ale2() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("testSubImageCrossTileBoundaryS93750x90000ale2");
        TileSet base = inst.allTiles();
        TileSet sub = base.sub(new XY(500,800), new XYD(25,-25), 
                                      base.getTilsetEastNorth().add(93750.0, -90000.0));
        BufferedImage bi = sub.getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "500x800@93750x90000.png"))){
            ImageIO.write(bi, "png", out);
        }
    }
    @Test @Ignore
    public void testSubImageCrossTileBoundaryS97500x96000scale10x10() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("testSubImageCrossTileBoundaryS97500x96000scale10x10");
        TileSet base = inst.allTiles();
        TileSet sub = base.sub(new XY(500,800), new XYD(10,-10), 
                                      base.getTilsetEastNorth().add(97500.0, -96000.0));
        BufferedImage bi = sub.getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "500x800@97500x96000.png"))){
            ImageIO.write(bi, "png", out);
        }
    }
    @Test @Ignore
    public void testSubImageCrossTileBoundaryS87500x80000scale50x50() throws TileException,
                                   FileNotFoundException, 
                                   IOException{
        System.out.println("testSubImageCrossTileBoundaryS97500x96000scale10x10");
        TileSet base = inst.allTiles();
        TileSet sub = base.sub(new XY(500,800), new XYD(50,-50), 
                                      base.getTilsetEastNorth().add(87500.0, -80000.0));
        BufferedImage bi = sub.getImage();
        try(ImageOutputStream out  = ImageIO.createImageOutputStream(
                                        new File(dir, "500x800@87500x80000.png"))){
            ImageIO.write(bi, "png", out);
        }
    }
}
