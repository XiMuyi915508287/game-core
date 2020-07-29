package com.game.cache.data;

import com.game.cache.source.executor.ICacheSource;
import com.game.common.config.Configs;
import com.game.common.config.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

public class DataSourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtil.class);


    public static <K, V extends IData<K>> IDataSource<K, V> createDataSource(ICacheSource<K, V> cacheSource){
        IDataSource<K, V> dataSource = new DataSource<>(cacheSource);
        IConfig dataConfig = Configs.getInstance().getConfig("cache.data");
        List<String> decorators = dataConfig.getList("decorators");
        for (String decorator : decorators) {
            String className = DataSource.class.getName() + decorator.toUpperCase().charAt(0) + decorator.toLowerCase().substring(1);
            try {
                logger.info("init decorator: {}", className);
                Class<?> decoratorClass = Class.forName(className);
                Constructor<?> constructor = decoratorClass.getConstructor(IDataSource.class);
                constructor.setAccessible(true);
                //noinspection unchecked
                dataSource =  (IDataSource<K, V>)constructor.newInstance(dataSource);
                logger.info("finish decorator: {}", className);
            }
            catch (Throwable t) {
                logger.error("decorator error, decorator is {}", className, t);
            }
        }
        return dataSource;
    }
}