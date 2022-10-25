//package eu.xenit.apix.rest.v1;
//
//import com.fasterxml.jackson.databind.type.SimpleType;
//import eu.xenit.apix.data.NodeRef;
//import eu.xenit.apix.data.QName;
//import eu.xenit.apix.rest.v1.bulk.BulkWebscript1;
//import eu.xenit.apix.rest.v1.categories.CategoryWebScript1;
//import eu.xenit.apix.rest.v1.configuration.ConfigurationWebscript1;
//import eu.xenit.apix.rest.v1.dictionary.DictionaryWebScript1;
//import eu.xenit.apix.rest.v1.nodes.NodesWebscript1;
//import eu.xenit.apix.rest.v1.people.PeopleWebscript1;
//import eu.xenit.apix.rest.v1.properties.PropertiesWebScript1;
//import eu.xenit.apix.rest.v1.search.SearchWebScript1;
//import eu.xenit.apix.rest.v1.sites.SitesWebscript1;
//import eu.xenit.apix.rest.v1.temp.WIPWebscript;
//import eu.xenit.apix.rest.v1.translation.TranslationsWebscript1;
//import eu.xenit.apix.rest.v1.versionhistory.VersionHistoryWebScript1;
//import eu.xenit.apix.rest.v1.workingcopies.WorkingcopiesWebscript1;
//import eu.xenit.apix.rest.v2.RestV2Config;
//import eu.xenit.apix.rest.v2.nodes.NodesWebscriptV2;
//import eu.xenit.apix.version.IVersionService;
//import eu.xenit.apix.web.IWebUtils;
//import eu.xenit.swagger.reader.Reader;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
//import io.swagger.converter.ModelConverter;
//import io.swagger.converter.ModelConverterContext;
//import io.swagger.converter.ModelConverters;
//import io.swagger.models.Model;
//import io.swagger.models.ModelImpl;
//import io.swagger.models.Operation;
//import io.swagger.models.Path;
//import io.swagger.models.Response;
//import io.swagger.models.Scheme;
//import io.swagger.models.Swagger;
//import io.swagger.models.properties.FileProperty;
//import io.swagger.models.properties.Property;
//import io.swagger.util.Json;
//import java.io.IOException;
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.extensions.surf.util.URLEncoder;
//import org.springframework.extensions.webscripts.WebScriptRequest;
//import org.springframework.extensions.webscripts.WebScriptResponse;
//import org.springframework.stereotype.Component;
//
//@WebScript(
//        baseUri = RestV1Config.BaseUrl + "/docs",
//        families = {RestV1Config.Family},
//        defaultFormat = "json",
//        description = "Access API Documentation",
//        value = "Documentation")
//@Component("eu.xenit.apix.rest.v1.DocumentationWebscript")
//@Authentication(AuthenticationType.USER)
//public class DocumentationWebscript extends ApixV1Webscript {//implements BeanFactoryAware{
//
//    Logger logger = LoggerFactory.getLogger(DocumentationWebscript.class);
//
//    private IVersionService versionService;
//    private IWebUtils webUtils;
//
//    @Autowired
//    public DocumentationWebscript(IVersionService versionService, IWebUtils webUtils) {
//        this.versionService = versionService;
//        this.webUtils = webUtils;
//    }
//
//    @Uri(value = "/swagger.json", method = HttpMethod.GET)
//    @ApiOperation("The Swagger Spec for Alfred API")
//    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
//    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
//        webScriptResponse.setContentType("json");
//        Swagger s = generateSwagger();
//        Json.mapper().writeValue(webScriptResponse.getOutputStream(), s);
//    }
//
//    @Uri(value = "/ui", method = HttpMethod.GET)
//    public void redirectToSwaggerUi(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) {
//        webScriptResponse.setStatus(302);
//        String swaggerUi = webScriptRequest.getServiceContextPath() + "/swagger/ui/";
//        String servicePath = webScriptRequest.getServicePath();
//        String swaggerJsonUrl = servicePath.substring(0, servicePath.lastIndexOf(47) + 1) + "swagger.json";
//        webScriptResponse.setHeader("Location", swaggerUi + "?url=" + URLEncoder.encode(swaggerJsonUrl));
//    }
//
//    public Swagger generateSwagger() {
//        Reader r = new Reader();
//        // Correctly sets QName, Noderef, etc because swagger reader doesnt understand
//        ModelConverters.getInstance().addConverter(new ModelConverter() {
//            @Override
//            public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations,
//                    Iterator<ModelConverter> chain) {
//                if (chain.hasNext()) {
//                    return chain.next().resolveProperty(type, context, annotations, chain);
//                }
//                return null;
//            }
//
//            @Override
//            public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
//                if (type instanceof SimpleType) {
//                    Class cls = ((SimpleType) type).getRawClass();// No clue why this happens
//
//                    if (cls.equals(NodeRef.class)) {
//                        ModelImpl noderefModel = new ModelImpl();
//                        noderefModel.setName("NodeRef");
//                        noderefModel.setType("string");
//                        noderefModel.setExample("workspace://SpacesStore/987-978-79-797-797-978");
//                        return noderefModel;
//                    }
//                    if (cls.equals(QName.class)) {
//                        ModelImpl noderefModel = new ModelImpl();
//                        noderefModel.setName("QName");
//                        noderefModel.setType("string");
//                        noderefModel.setExample("{http://www.alfresco.org/model/content/1.0}content");
//                        return noderefModel;
//                    }
//                }
//
//                if (chain.hasNext()) {
//                    return chain.next().resolve(type, context, chain);
//                }
//                return null;
//            }
//        });
//        // Ignore special DE webscript
//        r.setIgnoredRoutes(new String[]{"/apix/v1/docs/ui"});
//
//        r.read(ApixSwaggerDescription.class);
//        r.read(BulkWebscript1.class);
//        r.read(CategoryWebScript1.class);
//        r.read(ConfigurationWebscript1.class);
//        r.read(DictionaryWebScript1.class);
//        r.read(DocumentationWebscript.class);
//        r.read(GeneralWebscript.class);
//        r.read(NodesWebscript1.class);
//        r.read(SearchWebScript1.class);
//        r.read(SitesWebscript1.class);
//        r.read(TranslationsWebscript1.class);
//        r.read(VersionHistoryWebScript1.class);
//        r.read(WIPWebscript.class);
//        r.read(WorkingcopiesWebscript1.class);
//        r.read(PeopleWebscript1.class);
//        r.read(PropertiesWebScript1.class);
//        r.read(eu.xenit.apix.rest.v2.people.PeopleWebscript.class);
//        r.read(eu.xenit.apix.rest.v2.groups.GroupsWebscript.class);
//        r.read(NodesWebscriptV2.class);
//
//        addSwaggerUIOperation(r);
//
//        r.getSwagger().getInfo().setVersion(versionService.getVersionDescription().getVersion());
//
//        r.getSwagger().setSchemes(getSchemes());
//        r.getSwagger().setBasePath("/alfresco/s" + RestV1Config.ApixUrl);
//        Map<String, Path> paths = r.getSwagger().getPaths();
//        HashMap<String, Path> newPaths = new HashMap<>();
//        for (Map.Entry<String, Path> entry : paths.entrySet()) {
//            if (!entry.getKey().startsWith(RestV1Config.BaseUrl) && !entry.getKey().startsWith(RestV2Config.BaseUrl)) {
//                throw new RuntimeException(
//                        "Extract an operation from a webscript which does not start with the BaseUrl: "
//                                + entry.getKey());
//            }
//            newPaths.put(entry.getKey().substring(RestV1Config.ApixUrl.length()), entry.getValue());
//        }
//        r.getSwagger().setPaths(newPaths);
//        return r.getSwagger();
//    }
//
//    private void addSwaggerUIOperation(Reader r) {
//        Operation op = new Operation();
//        Response response = new Response().description("Swagger UI interface");
//        response.schema(new FileProperty());
//        op.addResponse(String.valueOf(200), response);
//        op.tag("Documentation");
//        Path p = new Path();
//        op.setSummary("Shows this swagger spec in a user interface");
//        p.setGet(op);
//        r.getSwagger().path("/apix/v1/docs/ui", p);
//    }
//
//    public List<Scheme> getSchemes() {
//        ArrayList<Scheme> ret = new ArrayList<>();
//        // This doesn't seem to work with the current setup at xenit,
//        // using https://xxx.dev.xenit.eu gives me http protocol instead of https
//
//        // So, always use https
//        // Seems to work on 12/12/17, adding both. TODO: use from location instead of option
//        ret.add(Scheme.HTTPS);
//        ret.add(Scheme.HTTP);
//        return ret;
//    }
//}
