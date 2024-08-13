package eu.xenit.alfred.api.workflow.search;

public class FacetValue {

    public String value;
    public int amount;

    public FacetValue() {

    }

    public FacetValue(String value, int amount) {
        this.value = value;
        this.amount = amount;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
