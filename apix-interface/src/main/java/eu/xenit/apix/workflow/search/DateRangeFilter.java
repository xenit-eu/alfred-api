package eu.xenit.apix.workflow.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class DateRangeFilter implements IQueryFilter {

    public static final String TYPE = "DateRangeFilter";
    private String property;
    private Date startDate;
    private Date endDate;

    @JsonCreator
    public DateRangeFilter(@JsonProperty("startDate") Date startDate, @JsonProperty("endDate") Date endDate,
            @JsonProperty("property") String property, @JsonProperty("type") String type) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.property = property;
    }

    public Date getStartDate() {
        return this.startDate;
    }
//    public Date getStartDateParsed() {
//        if(this.startDate == null) return null;
//        try {
//            return ISO8601DateFormat.getDateTimeInstance().parse(this.startDate);
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//            return null;
//        }
//    }

    public Date getEndDate() {
        return this.endDate;
    }
//    public Date getEndDateParsed() {
//        if(this.endDate == null) return null;
//        try {
//            return ISO8601DateFormat.getDateTimeInstance().parse(this.endDate);
//        } catch (ParseException e) {
//            logger.error(e.getMessage());
//            return null;
//        }
//    }

    public String getProperty() {
        return this.property;
    }

    public String getType() {
        return this.TYPE;
    }

    public boolean matches(Date dateToVerify) {
        if (dateToVerify == null) {
            return true;
        }
        if (this.endDate != null && this.endDate.before(dateToVerify)) { // endLimit > date
            return false;
        }
        if (this.startDate != null && this.startDate.after(dateToVerify)) { // startLimit < date
            return false;
        }
        return true;
    }
}
