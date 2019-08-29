package ru.comdev.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.comdev.common.MondrianConnectionFactory;
import ru.comdev.model.CellSetWrapperType;

import javax.annotation.PostConstruct;
import java.io.IOException;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

@RestController
public class MondrianRestController {

    private final Log log = LogFactory.getLog(MondrianRestController.class);
    private MondrianConnectionFactory connectionFactory;
    private Cache<Integer, CellSetWrapperType> queryCache;

    //@Resource(name="${requestAuthorizerBeanName}")
    //private RequestAuthorizer requestAuthorizer;

    @Value("${removeDemoConnections}")
    private boolean removeDemoConnections;

    @PostConstruct
    public void init() throws IOException {
        connectionFactory = new MondrianConnectionFactory();
        connectionFactory.init(removeDemoConnections);
        Configuration cacheConfig = new XmlConfiguration(getClass().getResource("/cache/ehcache.xml"));
        CacheManager cacheManager = CacheManagerBuilder.newCacheManager(cacheConfig);
        cacheManager.init();
        queryCache = cacheManager.getCache("query-cache", Integer.class, CellSetWrapperType.class);
//        log.info("Successfully registered request authorizer class "
//                + requestAuthorizer.getClass().getName());
    }

    @RequestMapping(value="/getConnections", method= RequestMethod.GET, produces="application/json")
    public String getConnections() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(MondrianConnectionFactory.MondrianConnection.class, SchemaContentHidingMixIn.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectionFactory.getConnections());
    }

    static final class SchemaContentHidingMixIn {
        @JsonIgnore
        @JsonProperty("MondrianSchemaContent")
        public String getMondrianSchemaContent() {
            return null;
        }
    }

    public void setRemoveDemoConnections(boolean removeDemoConnections) {
        this.removeDemoConnections = removeDemoConnections;
    }
}
