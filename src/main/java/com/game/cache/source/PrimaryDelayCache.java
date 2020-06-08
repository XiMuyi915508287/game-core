package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.common.config.Configs;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PrimaryDelayCache<PK, K, V extends IData<K>>{

    private final PK primaryKey;
    private volatile long expiredTime;
    private final Map<K, KeyDataValue<K, V>> keyDataValuesMap;

    public PrimaryDelayCache(PK primaryKey, int duration) {
        this.primaryKey = primaryKey;
        if (duration == 0){
            duration = Configs.getInstance().getInt("cache.flush.expiredDuration");
        }
        this.expiredTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);
        this.keyDataValuesMap = new ConcurrentHashMap<>();
    }

    public PrimaryDelayCache(PK primaryKey) {
        this(primaryKey, 0);
    }

    public PK getPrimaryKey() {
        return primaryKey;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isEmpty(){
        return keyDataValuesMap.isEmpty();
    }

    /**
     * @param secondaryKey
     * @return
     */
    public V getDataValue(K secondaryKey){
        KeyDataValue<K, V> keyDataValue = keyDataValuesMap.get(secondaryKey);
        if (keyDataValue == null || keyDataValue.isDeleted()){
            return null;
        }
        return keyDataValue.getDataValue();
    }

    /**
     * @param secondaryKey
     * @return
     */
    public V deleteCacheValue(K secondaryKey){
        KeyDataValue<K, V> oldKeyDataValue = keyDataValuesMap.remove(secondaryKey);
        if (oldKeyDataValue == null){
            keyDataValuesMap.put(secondaryKey, KeyDataValue.createDelete(secondaryKey));
            return null;
        }
        if (oldKeyDataValue.isInsert()) {
            return oldKeyDataValue.getDataValue();
        }
        if (oldKeyDataValue.isDeleted()) {
            return null;
        }
        if (oldKeyDataValue.isUpdate()){
            keyDataValuesMap.put(secondaryKey, KeyDataValue.createDelete(secondaryKey));
            return oldKeyDataValue.getDataValue();
        }
        throw new CacheException(oldKeyDataValue.getCacheCommand().name());
    }

    /**
     * @param keyDataValue
     */
    public void add(KeyDataValue<K, V> keyDataValue){
        keyDataValuesMap.putIfAbsent(keyDataValue.getKey(), keyDataValue);
    }

    public KeyDataValue<K, V> get(K secondaryKey){
        return keyDataValuesMap.get(secondaryKey);
    }

    public void addAll(Collection<KeyDataValue<K, V>> keyDataValues){
        for (KeyDataValue<K, V> keyDataValue : keyDataValues) {
            keyDataValuesMap.putIfAbsent(keyDataValue.getKey(), keyDataValue);
        }
    }

    /**
     * @param keyDataValues
     */
    public void rollbackAll(Collection<KeyDataValue<K, V>> keyDataValues){
        for (KeyDataValue<K, V> keyDataValue : keyDataValues) {
            keyDataValuesMap.putIfAbsent( keyDataValue.getKey(), keyDataValue);
        }
    }

    public Collection<KeyDataValue<K, V>> getAll(){
        return keyDataValuesMap.values();
    }
}