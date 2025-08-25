package com.vr.platform.common.utils;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class MemCacheUtils {

    private static final Map<String, String> INSTANCE = new ConcurrentHashMap<>();
    private static final Map<String, Long> KEY_INSTANCE = new ConcurrentHashMap<>();
    private static final AtomicInteger LOCK = new AtomicInteger(0);
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(1, 1, 60L,TimeUnit.SECONDS, new SynchronousQueue<>());
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(0.03);
    private static List<String> LRU_LIST = new LinkedList();
    private static Integer MAX_SIZE = 10000;

    public static void printSize(){
        if (log.isDebugEnabled()){
            log.debug("INSTANCE.size = {}", INSTANCE.size());
            log.debug("KEY_INSTANCE.size = {}", KEY_INSTANCE.size());
            log.debug("LRU_LIST.size = {}", LRU_LIST.size());
        }
    }

    public static void set(String key, String value, TimeUnit timeUnit, long timeout){
        if (key == null){
            log.warn("key is null");
            return;
        }
        INSTANCE.put(key, value);
        KEY_INSTANCE.put(key, System.currentTimeMillis() + timeUnit.toMillis(timeout));
        LRU_LIST.add(key);
        lazyClear();
    }

    public static String getAndExpire(String key, TimeUnit timeUnit, long timeout){
        String v = get(key);
        if (null != v){
            KEY_INSTANCE.put(key, System.currentTimeMillis() + timeUnit.toMillis(timeout));
        }
        return v;
    }

    public static String get(String key){
        if (null == key){
            return null;
        }
        Long expire = KEY_INSTANCE.get(key);
        if (null == expire){
            return null;
        }
        if (expire < System.currentTimeMillis()){
            delete(key);
            return null;
        }else {
            LRU_LIST.remove(key);
            LRU_LIST.add(key);
            return INSTANCE.get(key);
        }
    }

    public static Set<String> keys(String regex){
        Set<String> result = new HashSet<>();
        Set<String> keySet = KEY_INSTANCE.keySet();
        Pattern p = Pattern.compile(regex);
        Iterator<String> iterator = keySet.iterator();

        while (iterator.hasNext()){
            String key = iterator.next();
            Matcher matcher = p.matcher(key);
            if (matcher.find()){
                Long expire = KEY_INSTANCE.get(key);
                if (null == expire){
                    result.add(key);
                    continue;
                }
                if (expire < System.currentTimeMillis()){
                    delete(key);
                    iterator.remove();
                }else {
                    result.add(key);
                }
            }
        }
        return result;
    }

    private static void lazyClear(){
        if (!RATE_LIMITER.tryAcquire()){
            return;
        }
        if (LOCK.incrementAndGet() > 1){
            return;
        }
        if (log.isDebugEnabled()){
            log.debug("lazy clear cache start...");
        }
        THREAD_POOL.execute(() -> {
            Iterator<Map.Entry<String, Long>> iterator = KEY_INSTANCE.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Long> entry = iterator.next();
                if (entry.getValue() < System.currentTimeMillis()){
                    log.info("清除过期key = {}", entry.getKey());
                    iterator.remove();
                    INSTANCE.remove(entry.getKey());
                    LRU_LIST.remove(entry.getKey());
                }
            }
            printSize();
            while (INSTANCE.size() > MAX_SIZE){
                String key = LRU_LIST.get(0);
                log.info("超过容量，移除key: {}", key);
                delete(key);
            }
            LOCK.set(0);
        });
    }

    /**
     * 立即清理缓存
     * @param key
     */
    public static void delete(String key){
        INSTANCE.remove(key);
        KEY_INSTANCE.remove(key);
        LRU_LIST.remove(key);
    }

    public static void main(String[] args) throws InterruptedException {
        MemCacheUtils.set("aaa","bb",TimeUnit.SECONDS, 5);
        log.info(MemCacheUtils.get("aaa"));
        MemCacheUtils.set("bb","bab",TimeUnit.SECONDS, 5);
        log.info(MemCacheUtils.get("bb"));

        MemCacheUtils.set("cc","bdb",TimeUnit.SECONDS, 10);
        log.info(MemCacheUtils.get("cc"));

        TimeUnit.SECONDS.sleep(6);
        log.info(MemCacheUtils.get("aaa"));
        log.info(MemCacheUtils.get("bb"));
        log.info(MemCacheUtils.get("cc"));

        printSize();

        System.exit(0);
    }

}
