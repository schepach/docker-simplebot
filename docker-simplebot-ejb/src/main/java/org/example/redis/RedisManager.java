package org.example.redis;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisManager {

    private static final Logger logger = Logger.getLogger(RedisManager.class.getSimpleName());
    public static final Jedis REDIS_STORE = new Jedis("redis", 6379);

    public static void checkRedisStore(String chatId) {
        List<String> redisList = REDIS_STORE.lrange("CHATID", 0, -1);

        boolean isContains = false;

        if (redisList == null || redisList.isEmpty()) {
            logger.log(Level.INFO, "Redis list CHATID is empty, put first element");
            REDIS_STORE.rpush("CHATID", chatId);
            return;
        }

        for (String elemOfRedis : redisList) {
            if (chatId.equals(elemOfRedis)) {
                isContains = true;
                break;
            }
        }

        if (!isContains) {
            logger.log(Level.INFO, "chatId - {0} does not exist in redis, put it", chatId);
            REDIS_STORE.rpush("CHATID", chatId);
        } else {
            logger.log(Level.INFO, "chatId - {0} already exist in redis, go on...", chatId);
        }
    }
}