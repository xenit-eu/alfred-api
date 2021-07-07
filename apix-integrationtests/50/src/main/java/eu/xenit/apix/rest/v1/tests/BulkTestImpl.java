package eu.xenit.apix.rest.v1.tests;

import org.json.JSONException;
import org.json.JSONObject;

public class BulkTestImpl extends BulkTest {


    @Override
    public String jsonObjectGetStringFromInt(JSONObject targetObject, String key) {
        try {
            return targetObject.getString(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
