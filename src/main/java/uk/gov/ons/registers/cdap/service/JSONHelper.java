package uk.gov.ons.registers.cdap.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.gov.ons.registers.cdap.service.tablecolumns.CompanyHouseTable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class JSONHelper {
    private JSONHelper(){
        throw new AssertionError("This class is not meant to be instantiated");
    }

    /**
     * Method that takes byte[] HashMap of results and returns a decoded JSON object with the results
     * Following the JSON structure of Admin Data Service
     *
     * String referenceColumnName is used to set the ID of the JSON object from the rowkey column in dataset being used
     */
    static JsonElement byteMapToGenericJSON(Map<byte[], byte[]> hashMap, String referenceColumnName){

        Gson gson = new Gson();
        JsonObject jsonData = new JsonObject();
        JsonObject jsonVariables = new JsonObject();

        // Iterates thought results and casts the byte[] objects to Strings to a JSON Object
        for (Map.Entry<byte[], byte[]> entry : hashMap.entrySet()) {

            String keyString = decodeByteArrToString(entry.getKey());
            String valueString = decodeByteArrToString(entry.getValue());

            //Adds id and period based on JSON structure
            if (keyString.equals(CompanyHouseTable.PERIOD_COLUMN)) {
                jsonData.add(CompanyHouseTable.PERIOD_COLUMN, gson.toJsonTree(valueString));
            }
            if (keyString.equals(referenceColumnName)) {
                jsonData.add(CompanyHouseTable.ID_COLUMN, gson.toJsonTree(valueString));
            }

            //All other values to separate JSON object
            jsonVariables.add(keyString, gson.toJsonTree(valueString));
        }

        //Adding Variables to the "variables" element in the main JSON Object
        jsonData.add(CompanyHouseTable.VARIABLES_COLUMN, jsonVariables);

        // Returned JSON Object created from HashMap
        return jsonData;
    }

    static String decodeByteArrToString(byte[] byteInput) {
        return new String(byteInput, StandardCharsets.UTF_8);
    }
}
