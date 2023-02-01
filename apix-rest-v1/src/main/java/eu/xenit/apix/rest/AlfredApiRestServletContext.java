package eu.xenit.apix.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.rest.config.DefaultAlfrescoMvcServletContextConfiguration;
import eu.xenit.apix.rest.jackson.Jackson2ApixNodeRefDeserializer;
import eu.xenit.apix.rest.jackson.Jackson2ApixNodeRefSerializer;
import eu.xenit.apix.rest.jackson.Jackson2ApixQnameDeserializer;
import eu.xenit.apix.rest.jackson.Jackson2ApixQnameSerializer;
import eu.xenit.apix.search.json.SearchNodeJsonParser;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebMvc
@PropertySource(value = { "classpath:application.properties" })
// should pick up other controllers from the same package by default
@ComponentScan(basePackages = { "eu.xenit.apix" })
public class AlfredApiRestServletContext extends DefaultAlfrescoMvcServletContextConfiguration {


    public AlfredApiRestServletContext(RestJsonModule alfrescoRestJsonModule, NamespaceService namespaceService) {
        super(alfrescoRestJsonModule, namespaceService);
    }

    @Override
    protected List<JsonDeserializer<?>> customJsonDeserializers() {
        return Arrays.asList(
            new Jackson2ApixNodeRefDeserializer(),
            new Jackson2ApixQnameDeserializer()
        );
    }

    @Override
    protected List<JsonSerializer<?>> customJsonSerilizers() {
        return Arrays.asList(
                new Jackson2ApixNodeRefSerializer(),
                new Jackson2ApixQnameSerializer()
        );
    }

    @Bean
    @Primary
    @Override
    public ObjectMapper objectMapper() {
        ObjectMapper om = new SearchNodeJsonParser().getObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jackson2ObjectMapperBuilder().configure(om);
        return om;
    }

    @Override
    protected MultipartResolver createMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver() {
            @Override
            public boolean isMultipart(HttpServletRequest request) {
                String method = request.getMethod().toLowerCase();
                //By default, only POST is allowed. Since this is an 'update' we should accept PUT.
                if (!Arrays.asList("put", "post").contains(method)) {
                    return false;
                }
                String contentType = request.getContentType();
                return (contentType != null &&contentType.toLowerCase().startsWith("multipart/"));
            }
        };
        resolver.setMaxUploadSize(-1);
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }
}