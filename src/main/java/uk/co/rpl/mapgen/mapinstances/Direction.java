/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import static java.awt.Font.BOLD;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.rpl.mapgen.XY;
import uk.co.rpl.mapgen.XYD;

/**
 *
 * @author philip
 */
public enum Direction {
    NE(1, -1){
        @Override
        XY getBaseOrigin(Rectangle r){
            final XY start = textOffset.mul(scale).entier();
            return new XY(start.x+r.x, start.y-r.height+r.y);
        }
    },
    NW(-1, -1){
        @Override
        XY getBaseOrigin(Rectangle r){
            final XY start = textOffset.mul(scale).entier();
            return new XY(start.x-r.width+r.x, start.y-r.height+r.y);
        }
    },
    SE(1, 1){
        @Override
        XY getBaseOrigin(Rectangle r){
            final XY start = textOffset.mul(scale).entier();
            return new XY(start.x+r.x, start.y+r.y);
        }
    },
    SW(-1, 1){
        @Override
        XY getBaseOrigin(Rectangle r){
            final XY start = textOffset.mul(scale).entier();
            return new XY(start.x-r.width+r.x, start.y+r.y);
        }
    };
    public static final Logger LOG = LoggerFactory.getLogger(Direction.class);
    public final XYD scale;
    private static XYD[] shape = {new XYD(6,6), new XYD(19, 14), 
                         new XYD(14,19), new XYD(6,6)};
    private static int circleRadius=5;
    private static int circleDia=circleRadius*2;
    private static XYD botL = new XYD(-5, -5);
    private static XYD topR = new XYD(19, 19);
    private static XYD textOffset = new XYD(16,16);
    Direction(double x, double y){
        scale = new XYD(x, y);
    }

    abstract XY getBaseOrigin(Rectangle r);

    public void draw(Graphics g, XY at, Color colour){
        g.setColor(colour);
        Polygon poly = new Polygon();
        for (XYD poff : shape){
            XY np = poff.mul(scale).add(at.xyd()).entier();
            poly.addPoint(np.x, np.y);
        }
        final XY circStart = at.sub(circleRadius);
        g.fillOval(circStart.x, circStart.y, circleDia, circleDia);
        g.fillPolygon(poly);
        g.setColor(colour.darker().darker());
        g.drawOval(circStart.x, circStart.y, circleDia, circleDia);
        g.drawPolygon(poly);
    }

    public Rectangle enclosingRect(XY at){
        final XYD mTR = topR.mul(scale);
        final XYD mBL = botL.mul(scale);
        final int bLx = (int)Math.min(mTR.x, mBL.x);
        final int bLy = (int)Math.min(mTR.y, mBL.y);
        final int tRx = (int)Math.max(mTR.x, mBL.x);
        final int tRy = (int)Math.max(mTR.y, mBL.y);
        return new Rectangle(bLx, tRy, tRx-bLx, tRy-bLy);
    }

    class SizedString{
        final String data;
        final Rectangle2D size;
        final Font font;
        final FontMetrics metrics;

        public SizedString(String data, Rectangle2D size, 
                            Font font, FontMetrics metrics) {
            this.data = data;
            this.size = size;
            this.font = font;
            this.metrics=metrics;
        }

        
    }
    public void drawMessage(Graphics g, XY pointXY, String text, 
                            Font baseFont, Color colour, int margin){
        String[] lines = text.split("\\n");
        List<SizedString> listSS = new ArrayList<>();
        double height=margin;
        double width=0;
        for (String l: lines){
            boolean head = false;
            if (l.startsWith("=")){
                l=l.substring(1);
                head=true;
            }
            Font font = head?baseFont.deriveFont(BOLD, 12.0F):baseFont;
            FontMetrics fm = g.getFontMetrics(font);
            Rectangle2D rect = fm.getStringBounds(l, g);
            listSS.add(new SizedString(l, rect, font, fm));
            height+=fm.getLeading()+fm.getAscent()+fm.getDescent();
            width= Math.max(width, rect.getWidth());
        }
        height += margin;
        width += margin + margin;
        LOG.debug("label bounds is {} {}", width, height);
        XY topLeft = getBaseOrigin(new Rectangle(pointXY.x, pointXY.y,
                                   (int)width, (int)height));
        g.setColor(WHITE);
        g.fillRect(topLeft.x, topLeft.y, (int)width, (int)height);
        g.setColor(colour);
        g.drawRect(topLeft.x, topLeft.y, (int)width, (int)height);
        for (SizedString ss: listSS){
            g.setFont(ss.font);
            g.drawString(ss.data, topLeft.x+margin,
                                  topLeft.y+(int)(ss.metrics.getAscent()+0.5));
            topLeft = topLeft.add(0, (int)(ss.metrics.getLeading()+
                                           ss.metrics.getAscent()+
                                           ss.metrics.getDescent()+0.5));
        }
    }
    
}
