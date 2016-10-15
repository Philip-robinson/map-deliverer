/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import uk.co.rpl.mapgen.mapinstances.TileException;
import static org.slf4j.LoggerFactory.getLogger;
import uk.co.rpl.mapgen.mapinstances.TileCacheManager;

/**
 *
 * @author philip
 */
public class MapGen {


    static Logger LOG;
    static Map<Double, Map<Double, File>> map = new TreeMap<>();
    static Date startTime = new Date();
    static int cacheExpirySeconds;
    static BlockingQueue<Req> requests = new LinkedBlockingQueue<>(50);
    static long ref = startTime.getTime()-1476312000000L;
    static TileCacheManager cacheManager;
    static AtomicInteger runningThreads = new AtomicInteger();

    static int NUMBER_OF_THREADS = 100; 

    static void reporter() {
        try {
            for (;;) {
                Thread.sleep(5000);
                if (LOG != null && LOG.isInfoEnabled()) {
                    Runtime rt = Runtime.getRuntime();
                    LOG.info("Memory total {} Mb, "
                             + "or which {} Mb is free, "
                             + "max is {} Mb queue length is {}, running entries {}",
                             (rt.totalMemory()/1000)/1000.0,
                             (rt.freeMemory()/1000)/1000.0,
                             (rt.maxMemory()/1000)/1000.0,
                             requests.size(),
                             runningThreads.get());
                    LOG.info("Cache stats {} ", cacheManager);
                }
                Thread.sleep(5000);
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


    static void add(double x, double y, File f) {
        Map<Double, File> map2 = map.get(y);
        if (map2 == null) {
            map2 = new TreeMap<>();
            map.put(y, map2);
        }
        map2.put(x, f);
    }

    public static void main(String[] argv) throws IOException {
        
        startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
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
        for (int i=0; i<NUMBER_OF_THREADS; i++) new Thread(()->{
            for (;;){
                try{
                    Req req = requests.take();
                    runningThreads.addAndGet(1);
                    long start=System.currentTimeMillis();
                    req.run.run();
                    long fin = System.currentTimeMillis();
                    runningThreads.addAndGet(-1);
                    LOG.info("Completed: queued {}ms, ran for {}ms",
                             start-req.startTime,
                             fin-req.startTime);
                }catch(InterruptedException e){
                    LOG.error("interrupted");
                    return;
                }catch(Throwable e1){
                    LOG.error(e1.getMessage(), e1);
                }
            }}).start();
        try {
            Config con = new ConfigImpl();
            cacheManager = con.getCacheManager();
            final MapConfig[] maps = con.maps();
            LOG.info("Loaded " + Arrays.asList(maps));

            cacheExpirySeconds = con.getInt("cache-age", 86400);

            int port = con.getInt("port", 7664);
            HttpServer server = HttpServer.create(
                new InetSocketAddress(port), 100);
            server.setExecutor(r->{
                try{
                    requests.put(new Req(r));
                }catch (InterruptedException e){
                    LOG.error("Interrupted");
                }});
            server.createContext("/", e -> listener(e, maps));
            server.start();
            LOG.info("Server started on port {}", port);
            System.out.println("Server started on port " + port);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private static void listener(HttpExchange httpEx, MapConfig[] maps) 
        throws IOException, NumberFormatException {
        long start = System.currentTimeMillis();
        URI uri = httpEx.getRequestURI();
        LOG.info("Received request {}", uri);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = sdf.format(c.getTime());
        c.add(Calendar.SECOND, cacheExpirySeconds);
        String expires = sdf.format(c.getTime());
        String modified =sdf.format(startTime);

        Headers reqH = httpEx.getRequestHeaders();
        Headers respH = httpEx.getResponseHeaders();
        respH.add("Cache-Control", "max-age="+cacheExpirySeconds);
        respH.add("Date", date);
        respH.add("Expires", expires);
        respH.add("Last-Modified", modified);
        String since = reqH.getFirst("If-Modified-Since");
        Date dSince = null;
        if (since!=null){
            try {
                dSince = sdf.parse(since);
            } catch (ParseException ex) {
                LOG.error("Failed to parse {}", since);
            }
        }
        if (LOG.isDebugEnabled()){
            LOG.debug("Set headers");
            LOG.debug("max-age {}", ""+cacheExpirySeconds);
            LOG.debug("Date {}", date);
            LOG.debug("Expires {}", expires);
            LOG.debug("Last-Modified {}", modified);
        }
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
                String etag = "\""+(ref)+":"+spec+"\"";
                respH.add("Etag", etag);
                String setIfMatch = reqH.getFirst("If-None-Match");
                if (LOG.isDebugEnabled()){
                    LOG.debug("eastnorth {}, sacle {}, size {}, centre {}",
                                  eastnorth, scaling, size, enTC);
                    LOG.debug("sc*siz={} /2={}", scaling.mul(
                           size.xyd()), scaling.mul(size.xyd()).divide(2));
                    LOG.debug("dSince {}, startTime {}", dSince, startTime);
                }
                if (setIfMatch == null || etag.equals(setIfMatch)){
                    if (dSince != null && dSince.after(startTime)){
                        httpEx.sendResponseHeaders(304, 0);
                        httpEx.close();
                        return;
                    }
                }
                try {
                    MapConfig cur = getBestMap(maps, enTC, scaling, size);
                    if (cur == null) {
                        addErr.accept("Cannot find a suitable map");
                    } else {
                        try {
                            byte[] imgData = cacheManager.getImage(etag);
                            if (imgData == null){
                                final TileSet ts = cur.allTiles();
                                final TileSet tsO = ts.sub(size, scaling, enTC);
                                BufferedImage img = tsO.getImage();
                                ByteArrayOutputStream baos = 
                                        new ByteArrayOutputStream();
                                try (final ImageOutputStream io = 
                                        ImageIO.createImageOutputStream(baos)) {
                                    ImageIO.write(img, "png", io);
                                    imgData = baos.toByteArray();
                                }
                                cacheManager.addImage(etag, imgData);
                            }
                            httpEx.getResponseHeaders().add(
                                "Content-type", "image/png");
                            httpEx.sendResponseHeaders(200, imgData.length);
                            httpEx.getResponseBody().write(imgData);
                            httpEx.close();
                            LOG.info("Completed in {}ms", System.currentTimeMillis()-start);
                            return;
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
        httpEx.getResponseHeaders().add("Content-type", "text/plain");
        LOG.warn("Sending " + error);
        httpEx.sendResponseHeaders(400, erres.length);
        httpEx.getResponseBody().write(erres);
        httpEx.close();
        LOG.info("Aborted in {}ms", System.currentTimeMillis()-start);
    }


    private static MapConfig getBestMap(MapConfig[] maps,
                                        XYD enTC, XYD scaling, XY size 
                                        ) throws TileException {
        MapConfig cur = null;
        int curSuitability= 100;
        for (MapConfig mc : maps) {
            int s = mc.suitability(enTC, scaling, size);
            LOG.debug("Map config {}:{} score= {}",
                                  mc.scaleType(), mc.getInstance(), s);
            if (cur == null || curSuitability < s) {
                curSuitability = s;
                cur = mc;
            }
        }
        return cur;
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
        @Override
        public String toString(){
            double sc = scale==null?1:scale;
            return ((long)(north/sc/height))+":"+((long)(east/sc/width))+":"+
                   width+":"+height+":"+scale;
        }
    }
    static class Req{
        Runnable run;
        long startTime;
        Req(Runnable run){
            this.run = run;
            startTime = System.currentTimeMillis();
        }
    } 
}
