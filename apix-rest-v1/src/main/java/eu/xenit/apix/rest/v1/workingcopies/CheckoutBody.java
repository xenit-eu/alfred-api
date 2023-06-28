package eu.xenit.apix.rest.v1.workingcopies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.xenit.apix.data.NodeRef;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
class CheckoutBody {

    public NodeRef original;
    public NodeRef destinationFolder;

    public CheckoutBody() {
    }

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
