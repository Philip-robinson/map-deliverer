/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.rpl.mapgen.mapinstances;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import uk.co.rpl.mapgen.Config;
import uk.co.rpl.mapgen.Tile;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author philip
 */
public class TileCacheManager {
    private static Logger LOG = getLogger(TileCacheManager.class);
    private final int memoryLimit;
    private final int maxEntryCount;
    private final int maxImageCount;
    private final LinkedHashSet<Tile> tileAccess;
    private long imageHits;
    private long imageMisses;
    private long tileHits;
    private long tileMisses;
    public TileCacheManager(Config config){
        tileAccess = new LinkedHashSet<>();
        memoryLimit = config.getInt("tile-cache.memory-limit", 10);
        maxEntryCount = config.getInt("tile-cache.max-entry-count", 500);
        maxImageCount = config.getInt("tile-cache.max-image-count", 2000);
    }
    
    public void accessed(Tile t){
        synchronized(tileAccess){
            if (tileAccess.remove(t)) tileHits+=1;
            else tileMisses+=1;
            tileAccess.add(t);
            boolean tooManyEntries = tileAccess.size()>maxEntryCount;
            boolean tooMuchMemory = memoryLimitExceeded();
            if (tooMuchMemory || tooManyEntries){
                LOG.info("Flushing tile cache memory {}, entries {}",
                         tooMuchMemory, tooManyEntries);
                partialCacheFlush();
            }
        }
    }

    private boolean memoryLimitExceeded() {
        Runtime rt = Runtime.getRuntime();
        long mm = rt.maxMemory();
        long cm = rt.totalMemory()-rt.freeMemory();
        boolean tooMuchMemory = (mm-cm)*100/mm<memoryLimit;
        return tooMuchMemory;
    }

    private boolean flushInProgress;
    private final Object progressLock = new Object();
    public void partialCacheFlush() {
        synchronized(progressLock){
            if (flushInProgress) return;
            LOG.info("Starting cache flush");
            flushInProgress=true;
        }
        try{
            synchronized(tileAccess){
                int count = tileAccess.size()/10;
                if (count==0) count = 2;
                LOG.debug("Purging tile cache has {} removing {}",
                          tileAccess.size(), count);
                Iterator<Tile> itter = tileAccess.iterator();
                while (itter.hasNext() && count-- > 0){
                    Tile t = itter.next();
                    t.flushCache();
                    itter.remove();
                }
            }
            synchronized(imageCache){
                int count = imageCache.size()/10;
                if (count == 0) count = 2;
                LOG.debug("Purging image cache has {} removing {}",
                          imageCache.size(), count);
                Iterator<String> itter = imageCache.keySet().iterator();
                while (itter.hasNext() && count-- > 0){
                    itter.next();
                    itter.remove();
                }
            }
        }finally{
            flushInProgress=false;
            LOG.info("Cache flush complete");
        }
    }

    private final LinkedHashMap<String, byte[]> imageCache = new LinkedHashMap<>();
    public void addImage(String key, byte[] data){
        synchronized(imageCache){
            imageCache.remove(key);
            imageCache.put(key, data);
            if (imageCache.size()>maxImageCount || memoryLimitExceeded()){
                LOG.info("Purging image cache size = {} mem limit ", imageCache.size(), memoryLimitExceeded());
                new Thread(()->partialCacheFlush()).start();
            }
        }
    }
    public byte[] getImage(String key){
        synchronized(imageCache){
            // remove and re add to ensure it is at the end of the list;
            byte[] data = imageCache.remove(key);
            if (data != null){
                imageCache.put(key, data);
                imageHits+=1;
            }else{
                imageMisses +=1;
            }
            return data;
        }
    }

    @Override
    public String toString(){
        return "image hits "+imageHits+" misses "+imageMisses+" count "+imageCache.size()+
               " tile hits "+tileHits+" misses "+tileMisses+" count "+tileAccess.size();
    }
}
