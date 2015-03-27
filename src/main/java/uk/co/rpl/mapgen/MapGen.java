/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;

/**
 *
 * @author philip
 */
public class MapGen {
    static final String mini="/home/philip/OS_Data/minisc_gb/data/"+
                             "RGB_TIF_COMPRESSED/DATA/"+
                             "MiniScale_(relief1)_R15.tif";
    static final String miniTAB="/home/philip/OS_Data/minisc_gb/data/"+
                                "RGB_TIF_COMPRESSED/DATA/"+
                                "World_Files/mapinfo_TAB_files/"+
                                "MiniScale_(relief1)_R15.TAB";

    static final String map250MAPS="/home/philip/OS_Data/ras250_gb/data/";
    static final String map250TABS="/home/philip/OS_Data/ras250_gb/data/"+
                                   "georeferencing files/TAB/";
    static final String map250TFW="/home/philip/OS_Data/ras250_gb/data/"+
                                   "georeferencing files/TFW/";
    static double[] coords(File f){
        double[] res = new double[2];
        try(InputStream is = new FileInputStream(f)){
            try(Reader ir = new InputStreamReader(is)){
                try(BufferedReader br = new BufferedReader(ir)){
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    DoubleSupplier read = ()->{
                        try{
                            return Double.parseDouble(br.readLine().replaceAll(
                                                    "[^0-9.]", ""));
                        }catch(IOException e){
                            return -1.0;
                        }};
                    res[0]=read.getAsDouble();
                    res[1]=read.getAsDouble();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return res;
    }
    
    static Map<Double, Map<Double, File>> map = new TreeMap<>();

    static void add(double x, double y, File f){
        Map<Double, File> map2 = map.get(y);
        if (map2==null){
            map2 = new TreeMap<>();
            map.put(y, map2);
        }
        map2.put(x, f);
    }
    
    public static void main(String[] argv){
        File dir = new File(map250TFW);
        for (File f: dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".TFW") ||
                       name.endsWith(".tfw");
            }})){
            double[] xy = coords(f);
            File ff = new File(new File(map250MAPS), 
                f.getName().replaceAll(".TFW", ".tif"));
            add(xy[0], xy[1], ff);
        }
        for (Entry<Double, Map<Double, File>> e: map.entrySet()){
            System.out.print(e.getKey()+": ");
            for(Entry<Double, File> e1: e.getValue().entrySet()){
                System.out.print(e1.getKey()+"("+e1.getValue().getName()+"), ");
            }
            System.out.println();
        }
    }
}
