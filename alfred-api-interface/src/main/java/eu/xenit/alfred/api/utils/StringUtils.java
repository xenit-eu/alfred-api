package eu.xenit.alfred.api.utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Created by Stan on 24-Feb-16.
 */
public class StringUtils {

    public static SimpleDateFormat sortable = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss"); //Or whatever format fits best your needs.

    public static java.lang.String join(CharSequence delimiter, CharSequence... elements) {
        return join(delimiter, Arrays.asList(elements));
    }

    public static java.lang.String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        StringBuilder sb = new StringBuilder();

        Boolean first = true;
        for (CharSequence element : elements) {
            if (!first) {
                sb.append(delimiter);
            }

            sb.append(element);

            if (first) {
                first = false;
            }
        }

        return sb.toString();
    }

    public static String formattedDate(java.util.Date date) {
        return sortable.format(date);
    }
}
