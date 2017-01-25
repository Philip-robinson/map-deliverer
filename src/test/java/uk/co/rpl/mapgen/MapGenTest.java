/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import static java.lang.System.out;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author philip
 */
public class MapGenTest {
    
    public MapGenTest() {
    }

    /**
     * Test of reporter method, of class MapGen.
     */
    @Test
    public void testCacheKeyGen(){
        out.println("testCacheKeyGen");
         MapGen.NEWHS inst = new MapGen.NEWHS();

         inst.north=575000D;
         inst.east=115000D;
         inst.height=100;
         inst.width=100;
         inst.scale=2300D;
         
         MapGen.NEWHS inst2 = new MapGen.NEWHS();

         inst2.north=575000D;
         inst2.east=-115000D;
         inst2.height=100;
         inst2.width=100;
         inst2.scale=2300D;

         assertNotEquals(inst.toString(), inst2.toString());

    }
    
}
