/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.lang.reflect.Field;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import uk.co.rpl.mapgen.Config;
import uk.co.rpl.mapgen.Tile;

/**
 *
 * @author philip
 */
public class TileCacheManagerTest {
    
    public TileCacheManagerTest() {
    }

    Config config;
    TileCacheManager instance;
    @Before
    public void setup(){
        config = createMock(Config.class);
        expect(config.getInt("tile-cache.memory-limit", 10)).andReturn(10);
        expect(config.getInt("tile-cache.max-entry-count", 500)).andReturn(500);
        expect(config.getInt("tile-cache.max-image-count", 2000)).andReturn(500);
        replay(config);
        instance = new TileCacheManager(config);

    }
    /**
     * Test of accessed method, of class TileCacheManager.
     */
    @Test
    public void testAccessed() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        System.out.println("accessed");
        Tile [] ts = new Tile[40];
        for (int i=0; i<40; i++){
            ts[i] = createMock(Tile.class);
            if (i<4){
                ts[i].flushCache();
                expectLastCall().once();
            }
            replay(ts[i]);
        }

        for (Tile t: ts){
            instance.accessed(t);
        }
        instance.partialCacheFlush();
        for (Tile t: ts){
            verify(t);
        }
        Field thit = instance.getClass().getDeclaredField("tileHits");
        thit.setAccessible(true);
        Field tmiss = instance.getClass().getDeclaredField("tileMisses");
        tmiss.setAccessible(true);
        assertEquals(0, thit.getLong(instance));
        assertEquals(40, tmiss.getLong(instance));
        for (int i = 0; i<20; i++){
            instance.accessed(ts[i]);
        }
        assertEquals(16, thit.getLong(instance));
        assertEquals(44, tmiss.getLong(instance));
    }

    /**
     * Test of addImage method, of class TileCacheManager.
     */
    @Test
    public void testAddImage() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        System.out.println("addImage");
        String k1 = "uuit";
        String k2 = "uuiXXt";
        String k3 = "uuitoiuyt";
        
        byte[] b1 = new byte[]{};
        byte[] b2 = new byte[]{};
        byte[] b3 = new byte[]{};
        
        instance.addImage(k1, b1);
        assertNull(instance.getImage(k3));
        assertTrue(instance.getImage(k1)==b1);
        instance.addImage(k2, b2);
        assertNull(instance.getImage(k3));
        assertTrue(instance.getImage(k1)==b1);
        assertTrue(instance.getImage(k2)==b2);


        Field thit = instance.getClass().getDeclaredField("imageHits");
        thit.setAccessible(true);
        Field tmiss = instance.getClass().getDeclaredField("imageMisses");
        tmiss.setAccessible(true);
        assertEquals(3, thit.getLong(instance));
        assertEquals(2, tmiss.getLong(instance));
    }

}
