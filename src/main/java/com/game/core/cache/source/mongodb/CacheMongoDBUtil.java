package com.game.core.cache.source.mongodb;

import com.game.core.cache.CacheName;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志：
 * db.system.profile.find( { op: { $ne : 'command' }, 'appName':'cache' } ).limit(50).sort( { ts : -1 } ).pretty()
 *
 */
public class CacheMongoDBUtil {

    public static final UpdateOptions UPDATE_OPTIONS = new UpdateOptions().upsert(true);
    public static final String DB_NAME = "demo";

    public static UpdateOneModel<Document> createUpdateOneModel(int primarySharedId, Collection<Map.Entry<String, Object>> keyValue, Collection<Map.Entry<String, Object>> cache2Values) {
        Document queryDocument = getQueryDocument(primarySharedId, keyValue);
        Document document = toDocument(cache2Values);
        return new UpdateOneModel<>(queryDocument, document, UPDATE_OPTIONS);
    }

    public static DeleteOneModel<Document> createDeleteOneModel(int primarySharedId, List<Map.Entry<String, Object>> keyValue) {
        Document document = getQueryDocument(primarySharedId, keyValue);
        return new DeleteOneModel<>(document);
    }

    public static List<DeleteOneModel<Document>> createDeleteOneModelList(int primarySharedId, Collection<List<Map.Entry<String, Object>>> key2ValuesList) {
        return key2ValuesList.stream().map(keyValue-> createDeleteOneModel(primarySharedId, keyValue)).collect(Collectors.toList());
    }

    public static Document getQueryDocument(int primarySharedId, Collection<Map.Entry<String, Object>>  keyValue){
        Document document = new Document();
        for (Map.Entry<String, Object> entry : keyValue) {
            document.append(entry.getKey(), entry.getValue());
        }
        if (primarySharedId > 0){
            document.append(CacheName.PrimaryId.getKeyName(),primarySharedId);
        }
        return document;
    }

    public static Document getQueryDocument(Collection<Integer> primarySharedIds, Collection<Map.Entry<String, Object>> keyValue){
        Document document = new Document();
        for (Map.Entry<String, Object> entry : keyValue) {
            document.append(entry.getKey(), entry.getValue());
        }
        primarySharedIds = primarySharedIds.stream().filter( primarySharedId -> primarySharedId > 0).collect(Collectors.toList());
        document.append(CacheName.PrimaryId.getKeyName(), new Document("$in", primarySharedIds));
        return document;
    }

    public static Document toDocument(Collection<Map.Entry<String, Object>> cacheValue){
        Document document = new Document();
        for (Map.Entry<String, Object> entry : cacheValue) {
            if (CacheName.Names.contains(entry.getKey())) {
                continue;
            }
            document.put(entry.getKey(), entry.getValue());

        }
        return new Document("$set", document);
    }
}
