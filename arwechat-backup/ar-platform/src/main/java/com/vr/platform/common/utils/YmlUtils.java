package com.vr.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * yml文件工具
 * @author Gao
 *
 */
@Slf4j
public class YmlUtils {

	/**
	 * 读取yml文件
	 * @param fileName 文件名称 例如application.yml
	 * @param key yml属性key
	 * @return
	 */
    public static Object getYmlProperty(String fileName,Object key){
        Resource resource = new ClassPathResource(fileName);
        Properties properties = null;
        try {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties =  yamlFactory.getObject();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
	            log.error("读取yml文件属性错误 "+fileName,e);
	        }
            return null;
        }
        return properties.get(key);
    }
    
    /**
	 * 读取yml文件
	 * @param fileName 文件名称 例如application.yml
	 * @return
	 */
    public static Properties getYml(String fileName){
        Resource resource = new ClassPathResource(fileName);
        Properties properties = null;
        try {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties =  yamlFactory.getObject();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("读取yml文件错误 "+fileName,e);
	        }
            return null;
        }
        return properties;
    }
}
