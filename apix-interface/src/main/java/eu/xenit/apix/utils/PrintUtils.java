package eu.xenit.apix.utils;

public class PrintUtils {

    /**
     * indent a multiline string for each line with a prefix.
     *
     * @param prefix The prefix add before each line
     * @param s The multiline string
     * @return A multiline string which is prefixed.
     */
    public static String indent(String prefix, String s) {
        return prefix + s.replaceAll("\n", "\n" + prefix);
    }
}
