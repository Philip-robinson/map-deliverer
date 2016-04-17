/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.mapinstances.TileException;

/**
 *
 * @author philip
 */
public class MapGen {

    static Logger LOG;

    static void reporter() {
        try {
            for (;;) {
                Thread.sleep(10000);
                if (LOG != null && LOG.isInfoEnabled()) {
                    LOG.info("Memory total {} Gb, "
                             + "or which {} Gb is free, "
                             + "max is {} Gb",
                             (Runtime.getRuntime().totalMemory() / 10000) / 100.0,
                             (Runtime.getRuntime().freeMemory() / 10000) / 100.0,
                             (Runtime.getRuntime().maxMemory() / 10000) / 100.0);
                }
                Thread.sleep(3000000);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    static double[] coords(File f) {
        double[] res = new double[2];
        try (InputStream is = new FileInputStream(f)) {
            try (Reader ir = new InputStreamReader(is)) {
                try (BufferedReader br = new BufferedReader(ir)) {
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    DoubleSupplier read = () -> {
                        try {
                            return Double.parseDouble(br.readLine().replaceAll(
                                "[^0-9.]", ""));
                        } catch (IOException e) {
                            return -1.0;
                        }
                    };
                    res[0] = read.getAsDouble();
                    res[1] = read.getAsDouble();
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return res;
    }

    static Map<Double, Map<Double, File>> map = new TreeMap<>();

    static void add(double x, double y, File f) {
        Map<Double, File> map2 = map.get(y);
        if (map2 == null) {
            map2 = new TreeMap<>();
            map.put(y, map2);
        }
        map2.put(x, f);
    }

    public static void main(String[] argv) throws IOException {
        Properties l4jprops = new Properties();
        l4jprops.load(MapGen.class.getResourceAsStream("/log4j.properties"));
        System.out.println("/log4j.properties loaded");
        File f = new File("/etc/mapgen/log4j.properties");
        if (f.exists()) {
            try (InputStream is = new FileInputStream(f)) {
                l4jprops.load(is);
                System.out.println(f + "loaded");

            }
        }
        PropertyConfigurator.configure(l4jprops);
        LOG = getLogger(MapGen.class);
        new Thread(MapGen::reporter).start();
        try {
            Config con = new ConfigImpl();
            MapConfig[] maps = con.maps();
            LOG.info("Loaded " + Arrays.asList(maps));
            System.out.println("maps is " + Arrays.asList(maps));

            int port = con.getInt("port", 7664);
            HttpServer server = HttpServer.create(
                new InetSocketAddress(port), 100);
            server.createContext("/", e -> {
                long start = System.currentTimeMillis();
                URI uri = e.getRequestURI();
                LOG.info("Received request {}", uri);

                String q = uri.getQuery();
                StringBuilder error = new StringBuilder();
                Consumer<String> addErr = err -> {
                    synchronized (error) {
                        if (error.length() > 0) {
                            error.append(", ");
                        }
                        error.append(err);
                    }
                };
                String length = e.getRequestHeaders().getFirst("content-length");
                JSONObject data = new JSONObject();
                if (length != null
                    && length.length() != 0
                    && !"0".equals(length)) {
                    data = getBody(length, e);
                }
                if (q != null) {
                    NEWHS spec = new NEWHS();
                    String[] params = q.split("&");

                    for (String pair : params) {
                        parseQuery(pair, spec, addErr);
                    }
                    spec.checkAllPresent(error, addErr);
                    if (error.length() == 0) {
                        XY size = spec.size();
                        XYD eastnorth = spec.eastnorth();
                        XYD scaling = spec.scaling();
                        XYD enTC = spec.enTC();
                        LOG.debug("TL {}, sc {}, siz {}, centre {}",
                                  eastnorth, scaling, size, enTC);
                        LOG.debug("sc*siz={} /2={}", scaling.mul(
                                  size.xyd()), scaling.mul(
                                      size.xyd()).divide(2));

                        MapConfig cur = null;
                        int curSuitability = 100;
                        try {
                            for (MapConfig mc : maps) {
                                int s = mc.suitability(enTC, scaling, size);
                                LOG.debug("Map config {}:{} score= {}",
                                          mc.scaleType(), mc.getInstance(), s);
                                if (cur == null || curSuitability < s) {
                                    curSuitability = s;
                                    cur = mc;
                                }
                            }
                            if (cur == null) {
                                addErr.accept("Cannot find a suitable map");
                            } else {
                                try {
                                    TileSet ts = cur.allTiles();
                                    TileSet tsO = ts.sub(size, scaling, enTC);
                                    ByteArrayOutputStream baos
                                                          = new ByteArrayOutputStream();
                                    BufferedImage img = tsO.getImage();
                                    writeData(img, data, eastnorth,
                                              size, scaling);
                                    try (ImageOutputStream io =
                                         ImageIO.createImageOutputStream(baos)) {
                                        ImageIO.write(img, "png", io);
                                        byte[] res = baos.toByteArray();
                                        e.getResponseHeaders().add(
                                            "Content-type", "image/png");
                                        e.sendResponseHeaders(200, res.length);
                                        e.getResponseBody().write(res);
                                        e.close();
                                        LOG.info("Completed in {}ms", System.currentTimeMillis()-start);
                                        return;
                                    }
                                } catch (IOException | TileException te) {
                                    addErr.accept(te.getMessage());
                                }
                            }
                        } catch (Throwable th) {
                            LOG.error(th.getMessage(), th);
                            addErr.accept(th.getMessage());
                        }
                    }
                } else {
                    addErr.accept("No parameters provided");
                }
                byte[] erres = error.toString().getBytes("UTF-8");
                e.getResponseHeaders().add("Content-type", "text/plain");
                LOG.warn("Sending " + error);
                e.sendResponseHeaders(400, erres.length);
                e.getResponseBody().write(erres);
                e.close();
                LOG.info("Aborted in {}ms", System.currentTimeMillis()-start);
            });
            server.start();
            LOG.info("Server started on port {}", port);
            System.out.println("Server started on port " + port);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private static JSONObject getBody(String length, HttpExchange e) throws IOException, NumberFormatException {
        JSONObject data;
        final int len = Integer.parseInt(length);
        int remaining = len;
        try (InputStream is = e.getRequestBody()) {
            try (ByteArrayOutputStream bos
                             = new ByteArrayOutputStream(len)) {
                byte[] buff = new byte[1000];
                int red;
                while (remaining > 0 && (red = is.read(buff)) >= 0) {
                    remaining -= red;
                    bos.write(buff, 0, red);
                }
                String boss = bos.toString("utf-8").trim();
                data = JSON.parseObject(boss);
            }
        }
        return data;
    }

    private static void parseQuery(String pair, NEWHS spec, Consumer<String> addErr) {
        try {
            String[] nv = pair.split("=", 2);
            if (nv.length == 2) {
                switch (nv[0]) {
                    case "north":
                        spec.north = Double.parseDouble(nv[1]);
                        break;
                    case "east":
                        spec.east = Double.parseDouble(nv[1]);
                        break;
                    case "scale":
                        spec.scale = Double.parseDouble(nv[1]);
                        break;
                    case "width":
                        spec.width = Integer.parseInt(nv[1]);
                        break;
                    case "height":
                        spec.height = Integer.parseInt(nv[1]);
                        break;
                    default:
                        LOG.warn("Unknown parameter " + pair);
                }
            } else {
                addErr.accept("No Parameter value for " + pair);
            }
        } catch (NumberFormatException e1) {
            addErr.accept("Failed to process " + pair);
        }
    }

    private static void writeData(BufferedImage img, JSONObject data,
                                  XYD eastnorth, XY size, XYD scaling) {
        JSONArray pathNames = data.getJSONArray("paths");
        JSONArray pointNames = data.getJSONArray("paths");
        if (pathNames != null) {
            XY last = null;
            for (int i = 0; i < pathNames.size(); i++) {
                String name = pathNames.getString(i);
                JSONArray path = data.getJSONArray(name);
                for (int j = 0; j < path.size(); i++) {
                    JSONArray pair = path.getJSONArray(j);
                    XY thisO = getAsPxl(pair, eastnorth, scaling);
                    if (last != null) {
                        img.getGraphics().setColor(Color.blue);
                        img.getGraphics().drawLine(last.x, last.y, thisO.x, thisO.y);
                    }
                    last = thisO;
                }
            }
        }
    }

    private static XY getAsPxl(JSONArray pair, XYD eastnorth, XYD scaling) {
        XYD xyd = new XYD(pair.getLongValue(0), pair.getLongValue(1));
        return xyd.sub(eastnorth).div(scaling).xy();
    }

    private static class NEWHS {

        Double north = null;
        Double east = null;
        Integer width = null;
        Integer height = null;
        Double scale = null;

        void checkAllPresent(StringBuilder error,
                             Consumer<String> addErr) {
            if (error.length() == 0) {
                if (north == null) {
                    addErr.accept("north must be provided");
                }
                if (east == null) {
                    addErr.accept("east must be provided");
                }
                if (scale == null) {
                    addErr.accept("scale must be provided");
                }
                if (width == null) {
                    addErr.accept("width must be provided");
                }
                if (height == null) {
                    addErr.accept("height must be provided");
                }
            }
        }

        XY size() {
            return new XY(width, height);
        }

        XYD eastnorth() {
            return new XYD(east, north);
        }

        XYD scaling() {
            return new XYD(scale, -scale);
        }

        XYD enTC() {
            return eastnorth().sub(scaling().mul(
                size().xyd()).divide(2));
        }
    }

}
