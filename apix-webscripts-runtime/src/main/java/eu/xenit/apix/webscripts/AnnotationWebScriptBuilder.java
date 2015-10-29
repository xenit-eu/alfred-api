package eu.xenit.apix.webscripts;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Before;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Cache;
import com.github.dynamicextensionsalfresco.webscripts.annotations.ExceptionHandler;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.webscripts.arguments.HandlerMethodArgumentsResolver;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.DescriptionImpl;
import org.springframework.extensions.webscripts.TransactionParameters;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

//TODO: remove these
// END


/**
 * Creates [AnnotationWebScript] instances from beans defined in a [BeanFactory].
 *
 * @author Laurens Fridael
 * @author Laurent Van der Linden
 */
@Component("eu.xenit.apix.webscripts.AnnotationWebScriptBuilder")//("apixAnnotationWebScriptBuilder")
@OsgiService
public class AnnotationWebScriptBuilder {

    private HandlerMethodArgumentsResolver handlerMethodArgumentsResolver;
    //private ConfigurableListableBeanFactory beanFactory;
    private String trailingSlashExpression = "/$";


    /* Dependencies */

    //protected ConfigurableListableBeanFactory beanFactory?=null;
    private String leadingSlashExpression = "^/";


    @Autowired
    public AnnotationWebScriptBuilder(
            HandlerMethodArgumentsResolver handlerMethodArgumentsResolver) {

        this.handlerMethodArgumentsResolver = handlerMethodArgumentsResolver;
    }

    /* Main operations */

//    /**
//     * Creates [AnnotationWebScript]s from a given named bean by scanning methods annotated with [Uri].
//     *
//     * @param beanName *
//     * @return The [AnnotationWebScript] or null if the implementation does not consider the bean to be a handler
//     * *         for an [AnnotationWebScript].
//     */
//    public List<org.springframework.extensions.webscripts.WebScript> createWebScripts(final String beanName) {
//        Assert.hasText(beanName, "Bean name cannot be empty.");
//
//        Class<?> beanType = beanFactory.getType(beanName);
//
//        if (beanType == null) return new ArrayList<>();
//        //WebScript webScriptAnnotationt = beanFactory.findAnnotationOnBean(beanName, WebScript.class);
//        WebScript webScriptAnnotationt = beanType.getAnnotation(WebScript.class);// beanFactory.findAnnotationOnBean(beanName, WebScript.class);
//
//        if (webScriptAnnotationt == null) webScriptAnnotationt = getDefaultWebScriptAnnotation();
//        final WebScript webScriptAnnotation = webScriptAnnotationt;
//
//        String baseUri = webScriptAnnotation.baseUri();
//
//
//        if (StringUtils.hasText(baseUri) && baseUri.startsWith("/") == false) {
//            throw new RuntimeException("@WebScript baseUri for class '$beanType' does not start with a slash: '$baseUri'");
//        }
//
//        final HandlerMethods handlerMethods = new HandlerMethods();
//
//        addWebscriptGlobalHandlers(beanType, handlerMethods);
//
//        final ArrayList<org.springframework.extensions.webscripts.WebScript> webScripts = new ArrayList<org.springframework.extensions.webscripts.WebScript>();
//
//        ReflectionUtils.doWithMethods(beanType, new ReflectionUtils.MethodCallback() {
//            @Override
//            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
//                Uri uri = AnnotationUtils.findAnnotation(method, Uri.class);
//                if (uri == null) return;
//                AnnotationWebScript webScript = createWebScript(beanName, webScriptAnnotation, uri, handlerMethods.createForUriMethod(method));
//                webScripts.add(webScript);
//            }
//        });
//
//        HashSet<String> ids = new HashSet<String>();
//
//        for (org.springframework.extensions.webscripts.WebScript webScript : webScripts) {
//            String webscriptId = webScript.getDescription().getId();
//
//            boolean notContained = ids.add(webscriptId);
//
//            if (!notContained) {
//                throw new IllegalStateException("Duplicate Web Script ID \"" + webscriptId + "\" Make sure handler methods of annotation-based Web Scripts have unique names.");
//            }
//        }
//
//        return webScripts;
//    }

    public AnnotationWebScript createWebscriptForMethod(String methodName, Object webscriptBean)
            throws NoSuchMethodException {
        Class<?> beanType = webscriptBean.getClass();

        WebScript webScriptAnnotation = beanType.getAnnotation(WebScript.class);
        HandlerMethods handlerMethods = new HandlerMethods();
        addWebscriptGlobalHandlers(beanType, handlerMethods);

        Method[] methods = beanType.getMethods();
        Method method = null;
        for (Method m : methods) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            if (method != null) {
                throw new RuntimeException("Given methodname has multiple overloads with same name, not supported! " +
                        "Use unique method names " + methodName + " " + beanType.getCanonicalName());
            }
            method = m;
        }

        Uri uri = AnnotationUtils.findAnnotation(method, Uri.class);

        return createWebScript(webscriptBean, webScriptAnnotation, uri, handlerMethods.createForUriMethod(method));
    }

    public AnnotationWebScript createWebscriptForMethod(String methodName, String beanName)
            throws NoSuchMethodException {
        throw new UnsupportedOperationException("Not supported in certifiable builder due to requiring spring context");
//        Object bean = beanFactory.getBean(beanName);
//        return createWebscriptForMethod(methodName, bean);
//        Class<?> beanType = beanFactory.getType(beanName);
//
//        WebScript webScriptAnnotation = beanType.getAnnotation(WebScript.class);
//        HandlerMethods handlerMethods = new HandlerMethods();
//        addWebscriptGlobalHandlers(beanType, handlerMethods);
//
//
//        Method[] methods = beanType.getMethods();
//        Method method = null;
//        for (Method m : methods) {
//            if (!m.getName().equals(methodName)) continue;
//            if (method != null)
//                throw new RuntimeException("Given methodname has multiple overloads with same name, not supported! " +
//                        "Use unique method names " + methodName + " " + beanName);
//            method = m;
//        }
//
//        Uri uri = AnnotationUtils.findAnnotation(method, Uri.class);
//
//
//        return createWebScript(beanName, webScriptAnnotation, uri, handlerMethods.createForUriMethod(method));
    }

    public void addWebscriptGlobalHandlers(Class<?> beanType, final HandlerMethods handlerMethods) {
        ReflectionUtils.doWithMethods(beanType, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                Before before = AnnotationUtils.findAnnotation(method, Before.class);

                if (before != null) {
                    if (AnnotationUtils.findAnnotation(method, Attribute.class) != null
                            || AnnotationUtils.findAnnotation(method, Uri.class) != null) {
                        throw new RuntimeException(
                                "Cannot combine @Before, @Attribute and @Uri on a single method. Method: ${ClassUtils.getQualifiedMethodName(method)}");
                    }
                    handlerMethods.getBeforeMethods().add(method);
                }
            }
        });
        ReflectionUtils.doWithMethods(beanType, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                Attribute attribute = AnnotationUtils.findAnnotation(method, Attribute.class);

                if (attribute != null) {
                    if (AnnotationUtils.findAnnotation(method, Before.class) != null
                            || AnnotationUtils.findAnnotation(method, Uri.class) != null) {
                        throw new RuntimeException(
                                ("Cannot combine @Before, @Attribute and @Uri on a single method. Method: ${ClassUtils.getQualifiedMethodName(method)}"));
                    }
                    if (method.getReturnType() == Void.TYPE) {
                        throw new RuntimeException("@Attribute methods cannot have a void return type.");
                    }
                    handlerMethods.getAttributeMethods().add(method);
                }
            }
        });
        ReflectionUtils.doWithMethods(beanType, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                ExceptionHandler exceptionHandler = AnnotationUtils.findAnnotation(method, ExceptionHandler.class);

                if (exceptionHandler != null) {
                    if (AnnotationUtils.findAnnotation(method, Attribute.class) != null
                            || AnnotationUtils.findAnnotation(method, Before.class) != null
                            || AnnotationUtils.findAnnotation(method, Uri.class) != null) {
                        throw new RuntimeException(
                                "Cannot combine @Before, @Attribute @ExceptionHandler or @Uri on a single method. Method: ${ClassUtils.getQualifiedMethodName(method)}");
                    }
                    handlerMethods.getExceptionHandlerMethods()
                            .add(new ExceptionHandlerMethod(exceptionHandler, method));
                }
            }
        });
    }

    /* Utility operations */

    protected AnnotationWebScript createWebScript(Object webscriptBean, WebScript webScript, Uri uri,
            HandlerMethods handlerMethods) {
        DescriptionImpl description = new DescriptionImpl();

        if (hasText(webScript.defaultFormat())) {
            description.setDefaultFormat(webScript.defaultFormat());
        }
        String baseUri = webScript.baseUri();

        handleHandlerMethodAnnotation(uri, handlerMethods.getUriMethod(), description, baseUri);
        handleTypeAnnotations(webscriptBean, webScript, description);
        String id = "%s.%s.%s".format(generateId(webscriptBean), handlerMethods.getUriMethod().getName(),
                description.getMethod().toLowerCase());
        description.setId(id);
        Object handler = webscriptBean;//beanFactory.getBean(beanName);
        description.setStore(new DummyStore());
        return createWebScript(description, handler, handlerMethods);
    }

    protected AnnotationWebScript createWebScript(Description description, Object handler,
            HandlerMethods handlerMethods) {
        return new AnnotationWebScript(description, handler, handlerMethods, handlerMethodArgumentsResolver);
    }

    protected void handleHandlerMethodAnnotation(Uri uri, Method method, DescriptionImpl description, String baseUri) {
        Assert.notNull(uri, "Uri cannot be null.");
        Assert.notNull(method, "HttpMethod cannot be null.");
        Assert.notNull(description, "Description cannot be null.");

        List<String> uris = new ArrayList<>();
        if (uri.value().length > 0) {
            for (String it : uri.value()) {
                uris.add(baseUri.replace(trailingSlashExpression, "") + "/" + it.replace(leadingSlashExpression, ""));
            }
        } else if (StringUtils.hasText(baseUri)) {
            uris = Arrays.asList(baseUri.replace(trailingSlashExpression, ""));
        } else {
            throw new RuntimeException(
                    "No value specified for @Uri on method '%s' and no base URI found for @WebScript on class."
                            .format(ClassUtils.getQualifiedMethodName(method)));
        }
        description.setUris(uris.toArray(new String[uris.size()]));
        /*
         * For the sake of consistency we translate the HTTP method from the HttpMethod enum. This also shields us from
         * changes in the HttpMethod enum names.
         */
        switch (uri.method()) {

            case GET:
                description.setMethod("GET");
                break;
            case POST:
                description.setMethod("POST");
                break;
            case PUT:
                description.setMethod("PUT");
                break;
            case DELETE:
                description.setMethod("DELETE");
                break;
            case OPTIONS:
                description.setMethod("OPTIONS");
                break;
            default:
                throw new UnsupportedOperationException();
        }
        /*
         * Idem dito for FormatStyle.
         */
        switch (uri.formatStyle()) {
            case ANY:
                description.setFormatStyle(Description.FormatStyle.any);
                break;
            case ARGUMENT:
                description.setFormatStyle(Description.FormatStyle.argument);
                break;
            case EXTENSION:
                description.setFormatStyle(Description.FormatStyle.extension);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        if (hasText(uri.defaultFormat())) {
            description.setDefaultFormat(uri.defaultFormat());
        }
        description.setMultipartProcessing(uri.multipartProcessing());

        Authentication methodAuthentication = method.getAnnotation(Authentication.class);

        if (methodAuthentication != null) {
            handleAuthenticationAnnotation(methodAuthentication, description);
        }

        Transaction methodTransaction = method.getAnnotation(Transaction.class);

        if (methodTransaction != null) {
            handleTransactionAnnotation(methodTransaction, description);
        }
    }

    protected void handleTypeAnnotations(Object webscriptBean, WebScript webScript, DescriptionImpl description) {
        handleWebScriptAnnotation(webScript, webscriptBean, description);

        if (description.getRequiredAuthentication() == null) {
            Authentication authentication = AnnotationUtils
                    .findAnnotation(webscriptBean.getClass(), Authentication.class);

            if (authentication == null) {
                authentication = getDefaultAuthenticationAnnotation();
            }
            handleAuthenticationAnnotation(authentication, description);
        }

        if (description.getRequiredTransactionParameters() == null) {
            Transaction transaction = AnnotationUtils.findAnnotation(webscriptBean.getClass(), Transaction.class);

            if (transaction == null) {
                if (description.getMethod().equals("GET")) {
                    transaction = getDefaultReadonlyTransactionAnnotation();
                } else {
                    transaction = getDefaultReadWriteTransactionAnnotation();
                }
            }
            handleTransactionAnnotation(transaction, description);
        }

        Cache cache = AnnotationUtils.findAnnotation(webscriptBean.getClass(), Cache.class);
        if (cache == null) {
            cache = getDefaultCacheAnnotation();
        }
        handleCacheAnnotation(cache, webscriptBean, description);

        description.setDescPath("");
    }

    protected void handleWebScriptAnnotation(WebScript webScript, Object webscriptBean, DescriptionImpl description) {
        Assert.notNull(webScript, "Annotation cannot be null.");
        //Assert.hasText(beanName, "Bean name cannot be empty.");
        Assert.notNull(description, "Description cannot be null.");
        Assert.hasText(description.getMethod(), "Description method is not specified.");

        if (hasText(webScript.value())) {
            description.setShortName(webScript.value());
        } else {
            description.setShortName(generateShortName(webscriptBean));
        }
        if (hasText(webScript.description())) {
            description.setDescription(webScript.description());
        } else {
            description.setDescription(
                    "Annotation-based WebScript for class %s".format(webscriptBean.getClass().getName()));
        }
        if (webScript.families().length > 0) {
            description.setFamilys(new LinkedHashSet(Arrays.asList(webScript.families())));
        }
        switch (webScript.lifecycle()) {
            case NONE:
                description.setLifecycle(Description.Lifecycle.none);
                break;
            case DRAFT:
                description.setLifecycle(Description.Lifecycle.draft);
                break;
            case DRAFT_PUBLIC_API:
                description.setLifecycle(Description.Lifecycle.draft_public_api);
                break;
            case DEPRECATED:
                description.setLifecycle(Description.Lifecycle.deprecated);
                break;
            case INTERNAL:
                description.setLifecycle(Description.Lifecycle.internal);
                break;
            case PUBLIC_API:
                description.setLifecycle(Description.Lifecycle.public_api);
                break;
            case SAMPLE:
                description.setLifecycle(Description.Lifecycle.sample);
                break;
            default:
                throw new UnsupportedOperationException();
        }


    }

    private boolean hasText(String str) {
        if (str.isEmpty()) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    protected void handleAuthenticationAnnotation(Authentication authentication, DescriptionImpl description) {
        Assert.notNull(authentication, "Annotation cannot be null.");
        Assert.notNull(description, "Description cannot be null.");
        if (hasText(authentication.runAs())) {
            description.setRunAs(authentication.runAs());
        }
        switch (authentication.value()) {
            case NONE:
                description.setRequiredAuthentication(Description.RequiredAuthentication.none);
                break;
            case GUEST:
                description.setRequiredAuthentication(Description.RequiredAuthentication.guest);
                break;
            case USER:
                description.setRequiredAuthentication(Description.RequiredAuthentication.user);
                break;
            case ADMIN:
                description.setRequiredAuthentication(Description.RequiredAuthentication.admin);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void handleTransactionAnnotation(Transaction transaction, DescriptionImpl description) {
        Assert.notNull(transaction, "Annotation cannot be null.");
        Assert.notNull(description, "Description cannot be null.");

        TransactionParameters transactionParameters = new TransactionParameters();

        switch (transaction.value()) {
            case NONE:
                transactionParameters.setRequired(Description.RequiredTransaction.none);
                break;
            case REQUIRED:
                transactionParameters.setRequired(Description.RequiredTransaction.none);
                break;
            case REQUIRES_NEW:
                transactionParameters.setRequired(Description.RequiredTransaction.requiresnew);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        if (transaction.readOnly()) {
            transactionParameters.setCapability(Description.TransactionCapability.readonly);
        } else {
            transactionParameters.setCapability(Description.TransactionCapability.readwrite);
        }
        transactionParameters.setBufferSize(transaction.bufferSize());
        description.setRequiredTransactionParameters(transactionParameters);
    }

    protected void handleCacheAnnotation(Cache cache, Object webscriptBean, DescriptionImpl description) {
        Assert.notNull(cache, "Annotation cannot be null.");
        //Assert.hasText(beanName, "Bean name cannot be empty.");
        Assert.notNull(description, "Description cannot be null.");

        org.springframework.extensions.webscripts.Cache requiredCache = new org.springframework.extensions.webscripts.Cache();

        requiredCache.setNeverCache(cache.neverCache());
        requiredCache.setIsPublic(cache.isPublic());
        requiredCache.setMustRevalidate(cache.mustRevalidate());
        description.setRequiredCache(requiredCache);
    }

    //    protected String generateId(String beanName) {
//        Assert.hasText(beanName, "Bean name cannot be empty");
//        Class<?> clazz = beanFactory.getType(beanName);
//
//        return clazz.getName();
//    }
    protected String generateId(Object webscriptBean) {
        //Assert.hasText(beanName, "Bean name cannot be empty");
        Class<?> clazz = webscriptBean.getClass();

        return clazz.getCanonicalName();
    }

    //    protected String generateShortName(String beanName) {
//        Assert.hasText(beanName, "Bean name cannot be empty");
//        Class<?> clazz = beanFactory.getType(beanName);
//
//        return ClassUtils.getShortName(clazz);
//    }
    protected String generateShortName(Object webscriptBean) {
        Class<?> clazz = webscriptBean.getClass();

        return ClassUtils.getShortName(clazz);
    }

    /*
     * These methods use local classes to obtain annotations with default settings.
     */
    private Authentication getDefaultAuthenticationAnnotation() {

        return DefaultAuthentication.class.getAnnotation(Authentication.class);
    }

    private Transaction getDefaultReadWriteTransactionAnnotation() {

        return DefaultTransactionRW.class.getAnnotation(Transaction.class);
    }

    private Transaction getDefaultReadonlyTransactionAnnotation() {
        return DefaultTransactionRead.class.getAnnotation(Transaction.class);
    }

    private Cache getDefaultCacheAnnotation() {

        return DefaultCache.class.getAnnotation(Cache.class);
    }

    private WebScript getDefaultWebScriptAnnotation() {
        return DefaultWebscript.class.getAnnotation(WebScript.class);

    }

    @Authentication
    class DefaultAuthentication {

    }

    @Transaction()
    class DefaultTransactionRW {

    }

    @Transaction(readOnly = true)
    class DefaultTransactionRead {

    }

    @Cache
    class DefaultCache {

    }

    @WebScript
    class DefaultWebscript {

    }

    /* Dependencies */

//    public void setBeanFactory(BeanFactory beanFactory) {
//        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory, "BeanFactory is not of type ConfigurableListableBeanFactory.");
//        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
//    }
}
