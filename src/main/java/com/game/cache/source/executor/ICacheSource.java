package com.game.cache.source.executor;

import com.game.cache.source.CacheCollection;
import com.game.cache.source.KeyCacheValue;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ICacheSource<PK, K, V> {

    LockKey getLockKey();

    Class<V> getAClass();

    Map<String, Object> get(PK primaryKey, K secondaryKey);

    Collection<Map<String, Object>> getAll(PK primaryKey);

    CacheCollection getCollection(PK primaryKey);

    boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue);

    boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList);

    boolean deleteOne(PK primaryKey, K secondaryKey);

    boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys);
}