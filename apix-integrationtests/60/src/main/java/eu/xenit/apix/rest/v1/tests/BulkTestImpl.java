package eu.xenit.apix.rest.v1.tests;

import org.json.JSONObject;

public class BulkTestImpl extends BulkTest {

    @Override
    public String jsonObject_getString_fromInt_abridged(JSONObject targetObject, String key) {
        return targetObject.getString(key);
    }
}
