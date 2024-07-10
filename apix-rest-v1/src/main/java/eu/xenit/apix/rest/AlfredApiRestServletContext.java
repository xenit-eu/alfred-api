package eu.xenit.apix.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.annotation.EnableAlfrescoMvcAop;
import com.gradecak.alfresco.mvc.rest.config.DefaultAlfrescoMvcServletContextConfiguration;
import eu.xenit.apix.rest.jackson.Jackson2ApixNodeRefDeserializer;
import eu.xenit.apix.rest.jackson.Jackson2ApixNodeRefSerializer;
import eu.xenit.apix.rest.jackson.Jackson2ApixQnameDeserializer;
import eu.xenit.apix.rest.jackson.Jackson2ApixQnameSerializer;
import eu.xenit.apix.rest.staging.workflow.WorkflowWebscript;
import eu.xenit.apix.rest.v0.categories.ClassificationGetWebscript;
import eu.xenit.apix.rest.v0.dictionary.DictionaryServiceChecksumWebscript;
import eu.xenit.apix.rest.v0.metadata.MetadataBulkWebscript;
import eu.xenit.apix.rest.v0.metadata.MetadataGetWebscript;
import eu.xenit.apix.rest.v0.metadata.MetadataPostWebscript;
import eu.xenit.apix.rest.v0.search.SearchWebScript0;
import eu.xenit.apix.rest.v1.GeneralWebscript;
import eu.xenit.apix.rest.v1.bulk.BulkWebscript1;
import eu.xenit.apix.rest.v1.categories.CategoryWebScript1;
import eu.xenit.apix.rest.v1.configuration.ConfigurationWebscript1;
import eu.xenit.apix.rest.v1.dictionary.DictionaryWebScript1;
import eu.xenit.apix.rest.v1.nodes.NodesWebscript1;
import eu.xenit.apix.rest.v1.people.PeopleWebscript1;
import eu.xenit.apix.rest.v1.properties.PropertiesWebScript1;
import eu.xenit.apix.rest.v1.search.SearchWebScript1;
import eu.xenit.apix.rest.v1.sites.SitesWebscript1;
import eu.xenit.apix.rest.v1.temp.LogsWebscript;
import eu.xenit.apix.rest.v1.temp.WIPWebscript;
import eu.xenit.apix.rest.v1.translation.TranslationsWebscript1;
import eu.xenit.apix.rest.v1.versionhistory.VersionHistoryWebScript1;
import eu.xenit.apix.rest.v1.workingcopies.WorkingcopiesWebscript1;
import eu.xenit.apix.rest.v2.groups.GroupsWebscript;
import eu.xenit.apix.rest.v2.nodes.NodesWebscriptV2;
import eu.xenit.apix.rest.v2.people.PeopleWebscript;
import eu.xenit.apix.search.json.SearchNodeJsonParser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.service.namespace.NamespaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@PropertySource(value = {"classpath:application.properties"})
// should pick up other controllers from the same package by default
@ComponentScan(basePackages = {"eu.xenit.apix"})
@EnableAlfrescoMvcAop(basePackageClasses = {
        BulkWebscript1.class,
        CategoryWebScript1.class,
        ConfigurationWebscript1.class,
        ClassificationGetWebscript.class,
        DictionaryWebScript1.class,
        DictionaryServiceChecksumWebscript.class,
        GeneralWebscript.class,
        GroupsWebscript.class,
        LogsWebscript.class,
        MetadataPostWebscript.class,
        MetadataBulkWebscript.class,
        MetadataGetWebscript.class,
        NodesWebscriptV2.class,
        NodesWebscript1.class,
        PropertiesWebScript1.class,
        PeopleWebscript1.class,
        PeopleWebscript1.class,
        PeopleWebscript.class,
        SearchWebScript1.class,
        SearchWebScript0.class,
        SitesWebscript1.class,
        TranslationsWebscript1.class,
        VersionHistoryWebScript1.class,
        WorkingcopiesWebscript1.class,
        WorkflowWebscript.class,
        WIPWebscript.class
})
public class AlfredApiRestServletContext extends DefaultAlfrescoMvcServletContextConfiguration {


    private static final Logger log = LoggerFactory.getLogger(AlfredApiRestServletContext.class);

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

//// TODO - Verify this works
    // Print outs say that it returns true for incomming post/put requests so works?
    @Override
    protected MultipartResolver createMultipartResolver() {
        log.error("AlfredApiRestServletContext.java called, is alternative implementation");
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver(){
            @Override
            public boolean isMultipart(HttpServletRequest request) {
                String method = request.getMethod().toLowerCase();
                //By default, only POST is allowed. Since this is an 'update' we should accept PUT.
                if (!Arrays.asList("put", "post").contains(method)) {
                    return false;
                }
                String contentType = request.getContentType();
                System.out.println("createMultipartResolver - isMultipart : " + request.getContentType());
                return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
            }
        };
        return resolver;
    }
}