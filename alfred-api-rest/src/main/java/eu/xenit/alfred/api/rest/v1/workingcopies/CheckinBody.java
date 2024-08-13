package eu.xenit.alfred.api.rest.v1.workingcopies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
class CheckinBody {

    public String comment;
    public boolean majorVersion;

    @JsonCreator
    public CheckinBody(@JsonProperty("comment") String comment,
            @JsonProperty("majorVersion") boolean majorVersion) {
        this.comment = comment;
        this.majorVersion = majorVersion;
    }

    public CheckinBody() {
    }

    public String getComment() {
        return comment;
    }

    public boolean getMajorVersion() {
        return majorVersion;
    }
}
