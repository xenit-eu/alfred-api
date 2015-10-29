package eu.xenit.apix.webscriptGeneration;

import java.lang.reflect.Method;

/**
 * Definition for an alfresco webscript, Used as base datatype to represent a desc.xml + spring config + java webscript
 * <p/>
 * TODO: Idea: this could be merged with the swagger gen since alot of the code is similar Created by Michiel Huygen on
 * 29/03/2016.
 */
public class WebscriptDefinition {

    private String id;
    private String method;
    private String shortName;
    private String description;
    private String url;
    private String authentication;
    private String family;
    private String aPackage;
    private Class<?> clazz;
    private Method targetMethod;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getPackage() {
        return aPackage;
    }

    public void setPackage(String aPackage) {
        this.aPackage = aPackage;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getFullclassname() {
        return clazz.getCanonicalName();
    }

    public String getTargetmethodname() {
        return targetMethod.getName();
    }


    @Override
    public String toString() {
        return "WebscriptDefinition{" +
                "id='" + id + '\'' +
                ", method='" + method + '\'' +
                ", shortName='" + shortName + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", authentication='" + authentication + '\'' +
                ", family='" + family + '\'' +
                ", aPackage='" + aPackage + '\'' +
                ", clazz=" + clazz +
                ", targetMethod=" + targetMethod +
                '}';
    }
}
