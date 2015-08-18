/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

/**
 *
 * @author philip
 */
public class TileException extends Exception {

    public TileException(String msg) {
        super(msg);
    }
    
    public TileException(Exception ex) {
        super(ex);
    }
    
}
