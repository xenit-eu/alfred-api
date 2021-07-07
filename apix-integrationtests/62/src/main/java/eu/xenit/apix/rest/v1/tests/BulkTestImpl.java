package eu.xenit.apix.rest.v1.tests;

import org.json.JSONObject;

public class BulkTestImpl extends BulkTest {

    @Override
    public String jsonObjectGetStringFromInt(JSONObject targetObject, String key) {
        return targetObject.getString(key);
    }
}
