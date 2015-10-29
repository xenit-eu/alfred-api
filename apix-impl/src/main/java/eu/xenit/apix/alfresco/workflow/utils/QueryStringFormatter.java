package eu.xenit.apix.alfresco.workflow.utils;

public class QueryStringFormatter {

    public static boolean isExactMatch(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    public static String apply(String value) {
        return value.contains("*") ? value.replaceAll("\\*", "%") : "%" + value + "%";
    }
}
