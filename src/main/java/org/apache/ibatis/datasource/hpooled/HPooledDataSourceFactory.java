package org.apache.ibatis.datasource.hpooled;

import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HPooledDataSourceFactory implements DataSourceFactory {

    private HDataSourceConfig hDataSourceConfig;

    private DataSource dataSource;

    public HPooledDataSourceFactory() {
        hDataSourceConfig = new HDataSourceConfig();
    }

    @Override
    public void setProperties(Properties properties) {
        MetaObject metaDataSource = SystemMetaObject.forObject(hDataSourceConfig);
        for (Object key : properties.keySet()) {
            String propertyName = (String) key;
            if (metaDataSource.hasSetter(propertyName)) {
                String value = (String) properties.get(propertyName);
                Object convertedValue = convertValue(metaDataSource, propertyName, value);
                metaDataSource.setValue(propertyName, convertedValue);
            } else {
                throw new DataSourceException("Unknown DataSource property: " + propertyName);
            }
        }
        hDataSourceConfig.init();
        dataSource = new HPooledDataSource(hDataSourceConfig);
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    private Object convertValue(MetaObject metaDataSource, String propertyName, String value) {
        Object convertedValue = value;
        Class<?> targetType = metaDataSource.getSetterType(propertyName);
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }
}
