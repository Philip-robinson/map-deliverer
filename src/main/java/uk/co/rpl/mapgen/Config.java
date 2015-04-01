/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

/**
 *
 * @author philip
 */
public interface Config {
    String get(String name);
    int getInt(String name, int def);
    double getDbl(String name, double def);
    XY getXY(String nameX, String nameY, XY def);
    XYD getXYD(String nameX, String nameY, XYD def);
    MapConfig[] maps();
}
