package eu.xenit.apix.utils.java8;

/**
 * Copy of the java 8 comsumer interface
 */
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);


}
