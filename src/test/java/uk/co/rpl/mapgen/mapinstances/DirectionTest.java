/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.awt.Color;
import static java.awt.Color.GREEN;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import uk.co.rpl.mapgen.XY;
import static uk.co.rpl.mapgen.mapinstances.Direction.NE;
import static uk.co.rpl.mapgen.mapinstances.Direction.NW;
import static uk.co.rpl.mapgen.mapinstances.Direction.SE;
import static uk.co.rpl.mapgen.mapinstances.Direction.SW;

/**
 *
 * @author philip
 */
public class DirectionTest {
    
    File dir = new File("/tmp/TEST-MAP-IMAGES/DirectionTest/");
    
    public DirectionTest() {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }

    @Before
    public void start(){
        if (!dir.exists())dir.mkdirs();
    }
 
    /**
     * Test of getOrigin method, of class Direction.
     */
    @Test
    public void testGetOriginNE() {
        System.out.println("getOrigin");
        Rectangle r = new Rectangle(100, 200, 300, 400);
        Direction inst = Direction.NE;
        XY result = inst.getBaseOrigin(r);
        assertEquals(100+16, result.x);
        assertEquals(200-400-16, result.y);
    }

    @Test
    public void testGetOriginNW() {
        System.out.println("getOrigin");
        Rectangle r = new Rectangle(100, 200, 300, 400);
        Direction inst = Direction.NW;
        XY result = inst.getBaseOrigin(r);
        assertEquals(100-16-300, result.x);
        assertEquals(200-400-16, result.y);
    }

    @Test
    public void testGetOriginSE() {
        System.out.println("getOrigin");
        Rectangle r = new Rectangle(100, 200, 300, 400);
        Direction inst = Direction.SE;
        XY result = inst.getBaseOrigin(r);
        assertEquals(100+16, result.x);
        assertEquals(200+16, result.y);
    }

    @Test
    public void testGetOriginSW() {
        System.out.println("getOrigin");
        Rectangle r = new Rectangle(100, 200, 300, 400);
        Direction inst = Direction.SW;
        XY result = inst.getBaseOrigin(r);
        assertEquals(100-16-300, result.x);
        assertEquals(200+16, result.y);
    }

    /**
     * Test of draw method, of class Direction.
     */
    @Test
    public void testDrawNW() throws IOException{
        testDraw("ArrowNW", NW);
    }
    @Test
    public void testDrawNE() throws IOException{
        testDraw("ArrowNE", NE);
    }
    @Test
    public void testDrawSW() throws IOException{
        testDraw("ArrowSW", SW);
    }
    @Test
    public void testDrawSE() throws IOException{
        testDraw("ArrowSE", SE);
    }
    @Test
    public void testDrawNETxt() throws IOException{
        testDrawText("ArrowNETxt", NE, "=A Text Message");
    }
    @Test
    public void testDrawSETxt() throws IOException{
        testDrawText("ArrowSETxt", SE, "=A Text Message\nAnother line\n"+
                                       "and Another");
    }
    @Test
    public void testDrawSWTxt() throws IOException{
        testDrawText("ArrowSWTxt", SW, "A Text Message\n"+
                                       "=Another line which is blod\n"+
                                       "and Another");
    }
    @Test
    public void testDrawNWTxt() throws IOException{
        testDrawText("ArrowNWTxt", NW, "A Text Message\n"+
                                       "=Another line which is blod\n"+
                                       "and Another");
    }
    public void testDraw(String name, Direction direction) throws IOException {
        System.out.println(name);
        BufferedImage bi = drawArrow(direction);
        File out = new File(dir, name+".png");
        try(ImageOutputStream outp  = ImageIO.createImageOutputStream(out)){
            ImageIO.write(bi, "png", outp);
        }
    }

    private BufferedImage drawArrow(Direction direction) {
        BufferedImage bi = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 400, 200);
        g.setColor(Color.blue);
        g.drawLine(200, 0, 200, 200);
        g.drawLine(0, 100, 400, 100);
        XY at = new XY(200,100);
        Color colour = Color.red;
        Direction instance = direction;
        instance.draw(g, at, colour);
        return bi;
    }
    public void testDrawText(String name, Direction direction, String txt)
        throws IOException{
        BufferedImage bi = drawArrow(direction);
        Graphics2D g = bi.createGraphics();
        direction.drawMessage(g, new XY(200, 100),
                              txt, g.getFont().deriveFont(10), 
                              GREEN, 2);
        File out = new File(dir, name+".png");
        try(ImageOutputStream outp  = ImageIO.createImageOutputStream(out)){
            ImageIO.write(bi, "png", outp);
        }
    }

    /**
     * Test of enclosingRect method, of class Direction.
     */
    @Test
    public void testEnclosingRect() {
        System.out.println("enclosingRect");
        XY at = new XY(100, 100);
        Direction instance = Direction.NE;
        Rectangle result = instance.enclosingRect(at);
        assertEquals(-5, result.x);
        assertEquals(-5, result.x);
        assertEquals(24, result.width);
        assertEquals(24, result.height);
    }

}
