package com.ms.inventory.utils;


import org.json.JSONObject;

public class JsonValidator {

      public String getString(JSONObject object, String key) {
            try {
                  return object.getString(key);

            } catch (Exception e) {
                  //e.printStackTrace();
                  return "";
            }
      }

      public int getInt(JSONObject object, String key) {
            try {
                  return object.getInt(key);

            } catch (Exception e) {
                  //e.printStackTrace();
                  return 0;
            }
      }

      public boolean getBoolean(JSONObject object, String key) {

            try {
                 return object.getBoolean(key);

            } catch (Exception e) {
                  //e.printStackTrace();
                  return false;

            }
      }

      public double getDouble(JSONObject object, String key) {
            try {
                  return object.getDouble(key);
            } catch (Exception e) {
                  //e.printStackTrace();
                  return 0.0;
            }
      }
}
