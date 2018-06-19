package uk.gov.ons.registers.cdap.service;

import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.dataset.table.Get;
import co.cask.cdap.api.dataset.table.Row;
import co.cask.cdap.api.dataset.table.Scanner;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.api.service.AbstractService;
import co.cask.cdap.api.service.http.AbstractHttpServiceHandler;
import co.cask.cdap.api.service.http.HttpServiceRequest;
import co.cask.cdap.api.service.http.HttpServiceResponder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CHService extends AbstractService {

    static final String SERVICE_NAME = "CHService";
    private static final String SERVICE_DESC = "Service that returns A JSON object of company data based on CompanyNumber";
    private static final String POSTCODE_COLUMN = "regaddress_postcode";

    @Override
    protected void configure() {
        setName(SERVICE_NAME);
        setDescription(SERVICE_DESC);
        addHandler(new CHdataHandler());
    }

    /**
     * Handler which defines HTTP endpoints to access information stored in the
     * { @number CHdata} Dataset.
     */
    public static class CHdataHandler extends AbstractHttpServiceHandler {
        private static final Logger LOG = LoggerFactory.getLogger(CHdataHandler.class);

        private Gson gson = new Gson();

        @UseDataSet(Sic07.CH_DATASET_NAME)
        private Table chData;

        /**
         * Returns The Business Information as a JSON object from an entered Company Number
         */
        @GET
        @Path("/CH/number/{number}")
        public void getBusinessByNumber(HttpServiceRequest request, HttpServiceResponder responder,
                                        @PathParam("number") String number) {

            Row chRow = chData.get(new Get(number));

            //Error Handing of empty results
            if (chRow.isEmpty()) {
                LOG.debug("No record for Business with Company Number, {} found", number);
                responder.sendStatus(HttpURLConnection.HTTP_NOT_FOUND);
                return;
            }

            responder.sendJson(gson.toJsonTree(byteMapToJSON(chRow.getColumns())));

        }

        /**
         * Returns The Businesses Information as a JSON objects Based on Post Code Area
         */
        @GET
        @Path("/CH/postcodearea/{postcode}")
        public void getBusinessByPostCodeArea(HttpServiceRequest request, HttpServiceResponder responder,
                             @PathParam("postcode") String postcodeArea) {

            ArrayList<JsonElement> jsonElementArrayList = new ArrayList<>();
            Row row;

            try (Scanner scanner = chData.scan(null, null)) {
                while ((row = scanner.next()) != null) {
                    String postCode = row.getString(POSTCODE_COLUMN);

                    if (postCode != null) {
                        String scanPostCode[] = postCode.split(" ");

                        if (scanPostCode[0].equals(postcodeArea)){
                            jsonElementArrayList.add(byteMapToJSON(row.getColumns()));
                        }
                    }
                }
            }

            //Error Handing of empty results
            if (jsonElementArrayList.isEmpty()) {
                LOG.debug("No records found for Businesses in PostCode Area: {}", postcodeArea);
                responder.sendStatus(HttpURLConnection.HTTP_NOT_FOUND);
                return;
            }

            responder.sendJson(gson.toJsonTree(jsonElementArrayList));

        }

        /**
         * Method that takes byte[] HashMap from results and returns a decoded JSON object with the results
         */
        private JsonElement byteMapToJSON(Map<byte[], byte[]> hashMap){

            HashMap<String, String> chBusinessData = new HashMap<>();

            // Iterates thought results and casts the byte[] objects to Strings to a <String, String> HashMap
            for (Map.Entry<byte[], byte[]> entry : hashMap.entrySet()) {
                String keyString = new String(entry.getKey());
                String valueString = new String(entry.getValue());

                chBusinessData.put(keyString, valueString);
            }

            // Returned JSON Object created from HashMap
            return gson.toJsonTree(chBusinessData);
        }


    }



}