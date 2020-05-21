package com.game.cache.source;

import com.game.cache.dao.DataDaoManager;
import com.game.cache.dao.IDataMapDao;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.key.KeyValueHelper;
import com.game.db.mongodb.MongoDbManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jodd.util.ThreadUtil;
import org.bson.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class CacheRunner {

    @Test
    public void item(){
        MongoDatabase database = MongoDbManager.get("cache").getDb("demo");
        MongoCollection<Document> collection = database.getCollection("material");
        collection.drop();
        long userId = 1L;
//        Document queryDocument = new Document("userId", new Document("$exists", false));
//        Document document = new Document("$replace", new Document("item", Collections.emptyList()));
//        collection.updateMany(queryDocument, document);

//        collection.insertOne(new Document("userId", userId).append("item", Collections.emptyList()));

        IDataLoadPredicate<Long> loadPredicate = new IDataLoadPredicate<Long>() {
            @Override
            public void onPredicateLoaded(Long primaryKey) {
            }

            @Override
            public boolean predicateFirstTime(Long primaryKey) {
                return true;
            }
        };

        IDataMapDao<Long, Long, UserItem> itemDao = DataDaoManager.getInstance()
                .newDataMapDaoBuilder(UserItem.class, KeyValueHelper.LongBuilder, KeyValueHelper.LongBuilder)
                .setLoadPredicate(loadPredicate).buildCache();
        IDataMapDao<Long, Integer, UserCurrency> currencyDao = DataDaoManager.getInstance()
                .newDataMapDaoBuilder(UserCurrency.class, KeyValueHelper.LongBuilder, KeyValueHelper.IntegerBuilder)
                .setLoadPredicate(loadPredicate).buildCache();

        itemDao.getAll(userId);
        currencyDao.getAll(userId);
        Collection<UserItem> updateItemList = new ArrayList<>();
        Collection<UserCurrency> updateCurrencyList = new ArrayList<>();

        for (int i = 1; i <= 15; i++) {
            UserItem userItem = new UserItem(userId, i, 10);
            updateItemList.add(userItem);
            UserCurrency userCurrency = new UserCurrency(userId, i, 10);
            updateCurrencyList.add(userCurrency);
        }
        itemDao.replaceBatch(userId, updateItemList);
        currencyDao.replaceBatch(userId, updateCurrencyList);

        for (int i = 0; i < 5; i++) {
            updateItemList.forEach(userItem -> userItem.decCount(1));
            itemDao.replaceBatch(userId, updateItemList);

            updateCurrencyList.forEach(userCurrency -> userCurrency.decCount(1));
            currencyDao.replaceBatch(userId, updateCurrencyList);
        }

        ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(10));

//        itemDao.deleteBatch(userId, updateItemList.stream().map(UserItem::secondaryKey).collect(Collectors.toList()));
//        currencyDao.deleteBatch(userId, updateCurrencyList.stream().map(UserCurrency::secondaryKey).collect(Collectors.toList()));
//        ThreadUtil.sleep(TimeUnit.MINUTES.toMillis(2));
    }

    @Test
    public void player(){

    }
}
