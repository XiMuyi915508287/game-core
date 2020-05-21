package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassDescription;
import com.game.cache.mapper.annotation.CacheClass;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.Map;

public abstract class CacheSource<PK, K, V extends IData<K>> implements ICacheSource<PK, K, V> {

    private static final Object[] EMPTY = new Object[0];

    private final Class<V> aClass;
    private final CacheClass cacheClass;
    private final LockKey lockKey;
    private final CacheKeyValueBuilder<PK, K> keyValueBuilder;

    public CacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        this.aClass = aClass;
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        ClassDescription description = ClassDescription.get(aClass);
        this.keyValueBuilder = new CacheKeyValueBuilder<>(description, primaryBuilder, secondaryBuilder);
        this.cacheClass = getKeyValueBuilder().getClsDescription().getCacheClass();
    }

    @Override
    public LockKey getLockKey() {
        return lockKey;
    }

    @Override
    public Class<V> getAClass() {
        return aClass;
    }

    @Override
    public CacheClass getCacheClass() {
        return cacheClass;
    }

    @Override
    public Map<String, Object> get(PK primaryKey, K secondaryKey) {
        Map<String, Object> key2Value = getKeyValueBuilder().createPrimarySecondaryKeyValue(primaryKey, secondaryKey);
        return get0(key2Value);
    }

    protected abstract Map<String, Object> get0(Map<String, Object> key2Value);

    @Override
    public Collection<Map<String, Object>> getAll(PK primaryKey) {
        Map<String, Object> key2Value = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        return getAll0(key2Value);
    }

    protected abstract Collection<Map<String, Object>> getAll0(Map<String, Object> key2Value);

    @Override
    public final CacheCollection getCollection(PK primaryKey) {
        Map<String, Object> key2Value = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        return getCollection0(key2Value);
    }

    protected abstract CacheCollection getCollection0(Map<String, Object> key2Value);

    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return keyValueBuilder;
    }

    protected String getCacheName() {
        return cacheClass.cacheName();
    }

    protected int getPrimarySharedId() {
        return cacheClass.primarySharedId();
    }
}
