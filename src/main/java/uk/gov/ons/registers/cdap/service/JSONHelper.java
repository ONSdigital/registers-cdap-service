package uk.gov.ons.registers.cdap.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

class JSONHelper {


    /**
     * Method that takes byte[] HashMap of results and returns a decoded JSON object with the results
     * Following the JSON structure of Admin Data Service
     */
    static JsonElement byteMapToGenericJSON(Map<byte[], byte[]> hashMap){

        Gson gson = new Gson();
        Map<String, String> chBusinessData = new HashMap<>();

        // Iterates thought results and casts the byte[] objects to Strings to a <String, String> HashMap
        for (Map.Entry<byte[], byte[]> entry : hashMap.entrySet()) {
            String keyString = null;
            String valueString = null;

            try {
                keyString = new String(entry.getKey(), "UTF-8");
                valueString = new String(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            chBusinessData.put(keyString, valueString);
        }

        // Returned JSON Object created from HashMap
        return gson.toJsonTree(chBusinessData);
    }
}
