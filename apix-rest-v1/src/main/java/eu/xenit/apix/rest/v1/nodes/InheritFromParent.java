package eu.xenit.apix.rest.v1.nodes;

class InheritFromParent {

    private boolean inheritFromParent;

    public InheritFromParent() {

    }
    public InheritFromParent(boolean inheritFromParent) {
        this.inheritFromParent = inheritFromParent;
    }

    public boolean isInheritFromParent() {
        return inheritFromParent;
    }

    public void setInheritFromParent(boolean inheritFromParent) {
        this.inheritFromParent = inheritFromParent;
    }
}