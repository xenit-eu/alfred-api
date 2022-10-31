package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.data.QName;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
public class CreateNodeOptions {

    public static final QName PROP_NAME_QNAME = new QName(ContentModel.PROP_NAME.toString());
    @ApiModelProperty(required = true)
    public String parent;
    public String name;
    public String type;
    public Map<QName, String[]> properties;

    public QName[] aspectsToAdd;
    public QName[] aspectsToRemove;
    public String copyFrom;
    private ObjectMapper mapper = new ObjectMapper();

    @JsonCreator
    public CreateNodeOptions(@JsonProperty("parent") String parent,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("properties") Map<QName, String[]> properties,
            @JsonProperty("aspectsToAdd") QName[] aspectsToAdd,
            @JsonProperty("aspectsToRemove") QName[] aspectsToRemove,
            @JsonProperty("copyFrom") String copyFrom) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.properties = properties;
        if (this.properties == null) {
            this.properties = new HashMap<>(1);
        }
        if ((name != null && !this.properties.containsKey(PROP_NAME_QNAME))) {
            this.properties.put(PROP_NAME_QNAME, new String[]{name});
        }

        if (aspectsToAdd == null) {
            this.aspectsToAdd = new QName[0];
        }
        else {
            this.aspectsToAdd = aspectsToAdd;
        }

        if (aspectsToRemove == null) {
            this.aspectsToRemove = new QName[0];
        }
        else {
            this.aspectsToRemove = aspectsToRemove;
        }

        this.copyFrom = copyFrom;
    }

    public String getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<QName, String[]> getProperties() {
        return properties;
    }

    public QName[] getAspectsToAdd() {
        return aspectsToAdd;
    }

    public QName[] getAspectsToRemove() {
        return aspectsToRemove;
    }

    public String getCopyFrom() {
        return copyFrom;
    }
}
