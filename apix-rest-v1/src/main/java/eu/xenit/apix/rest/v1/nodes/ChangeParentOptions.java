package eu.xenit.apix.rest.v1.nodes;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
class ChangeParentOptions {

    private String parent;


    public ChangeParentOptions(String parent) {
        this.parent = parent;
    }

    public ChangeParentOptions() {
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getParent() {
        return parent;
    }
}
