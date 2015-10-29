package io.swagger.sample;


/**
 * Created by Michiel Huygen on 19/01/2016.
 */
public class NodeRef {

    private String value;


    public NodeRef(String s) {
        value = s;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
