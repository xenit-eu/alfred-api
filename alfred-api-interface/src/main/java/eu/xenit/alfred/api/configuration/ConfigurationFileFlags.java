package eu.xenit.alfred.api.configuration;

public class ConfigurationFileFlags {

    public boolean addContent;
    public boolean addPath;
    public boolean addParsedContent;
    public boolean addMetadata;
    public boolean addNodeRef;

    public ConfigurationFileFlags() {
    }

    public ConfigurationFileFlags(boolean addContent, boolean addPath, boolean addParsedContent, boolean addMetadata,
            boolean addNodeRef) {
        this.addContent = addContent;
        this.addPath = addPath;
        this.addParsedContent = addParsedContent;
        this.addMetadata = addMetadata;
        this.addNodeRef = addNodeRef;
    }

    public boolean isAddContent() {
        return addContent;
    }

    public void setAddContent(boolean addContent) {
        this.addContent = addContent;
    }

    public boolean isAddPath() {
        return addPath;
    }

    public void setAddPath(boolean addPath) {
        this.addPath = addPath;
    }

    public boolean isAddParsedContent() {
        return addParsedContent;
    }

    public void setAddParsedContent(boolean addParsedContent) {
        this.addParsedContent = addParsedContent;
    }

    public boolean isAddMetadata() {
        return addMetadata;
    }

    public void setAddMetadata(boolean addMetadata) {
        this.addMetadata = addMetadata;
    }

    public boolean isAddNodeRef() {
        return addNodeRef;
    }

    public void setAddNodeRef(boolean addNodeRef) {
        this.addNodeRef = addNodeRef;
    }
}
