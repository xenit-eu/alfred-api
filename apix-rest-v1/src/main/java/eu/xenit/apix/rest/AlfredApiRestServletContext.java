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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spring.web.WebMvcRequestHandler;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebMvc
@EnableSwagger2
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        ActiveMQAutoConfiguration.class,
        FreeMarkerAutoConfiguration.class
})
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

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.ant("/api/**"))
                .apis(RequestHandlerSelectors.basePackage("eu.xenit.apix"))
                .build();
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/api/swagger-ui.html**")
//                .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
//        registry.addResourceHandler("/api/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addRedirectViewController(
//                "/apix/v2/api-docs",
//                "/v2/api-docs"
//        );
//        registry.addRedirectViewController(
//                "/apix/swagger-resources/configuration/ui",
//                "/swagger-resources/configuration/ui"
//        );
//        registry.addRedirectViewController(
//                "/apix/swagger-resources/configuration/security",
//                "/swagger-resources/configuration/security"
//        );
//        registry.addRedirectViewController(
//                "/apix/swagger-resources",
//                "/swagger-resources"
//        );
//    }

    @Bean
    public InitializingBean removeSpringfoxHandlerProvider(DocumentationPluginsBootstrapper bootstrapper) {
        return () -> bootstrapper.getHandlerProviders().removeIf(WebMvcRequestHandlerProvider.class::isInstance);
    }

    @Bean
    public RequestHandlerProvider customRequestHandlerProvider(Optional<ServletContext> servletContext,
                                                               HandlerMethodResolver methodResolver,
                                                               List<RequestMappingInfoHandlerMapping> handlerMappings) {
        String contextPath = servletContext.map(ServletContext::getContextPath).orElse(Paths.ROOT);
        return () -> handlerMappings.stream()
                .filter(mapping -> !mapping.getClass().getSimpleName()
                                        .equals("IntegrationRequestMappingHandlerMapping"))
                .map(mapping -> mapping.getHandlerMethods().entrySet())
                .flatMap(Set::stream)
                .map(entry -> new WebMvcRequestHandler(contextPath, methodResolver,
                                                        tweakInfo(entry.getKey()), entry.getValue()))
                .sorted(RequestHandler.byPatternsCondition())
                .collect(Collectors.toList());
    }

    RequestMappingInfo tweakInfo(RequestMappingInfo info) {
        if (info.getPathPatternsCondition() == null) return info;
        String[] patterns = info.getPathPatternsCondition().getPatternValues().toArray(String[]::new);
        return info.mutate().options(new RequestMappingInfo.BuilderConfiguration()).paths(patterns).build();
    }
}