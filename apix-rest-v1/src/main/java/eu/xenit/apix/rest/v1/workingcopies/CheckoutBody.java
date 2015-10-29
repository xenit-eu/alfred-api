package eu.xenit.apix.rest.v1.workingcopies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.xenit.apix.data.NodeRef;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
class CheckoutBody {

    @ApiModelProperty(required = true)
    public NodeRef original;
    @ApiModelProperty("Optional, if not specified uses the original node's folder. " +
            "If current user does not have permissions for this folder, the working copy is created in the user's home folder")
    public NodeRef destinationFolder;

    @JsonCreator
    public CheckoutBody(@JsonProperty("original") NodeRef original,
            @JsonProperty("destinationFolder") NodeRef destinationFolder) {
        this.original = original;
        this.destinationFolder = destinationFolder;
    }

    public NodeRef getOriginal() {
        return original;
    }

    public NodeRef getDestinationFolder() {
        return destinationFolder;
    }
}
