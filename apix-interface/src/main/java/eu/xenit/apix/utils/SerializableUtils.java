package eu.xenit.apix.utils;

import java.text.SimpleDateFormat;

public class SerializableUtils {

    public static java.lang.String toString(java.io.Serializable serializable) {
        if (serializable == null) {
            return null;
        }
        if (serializable instanceof java.util.Date) {
            return StringUtils.formattedDate((java.util.Date) serializable);
        }
        return serializable.toString();
    }
}
