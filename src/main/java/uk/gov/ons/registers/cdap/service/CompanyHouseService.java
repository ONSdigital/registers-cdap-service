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
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.registers.cdap.service.TableColumns.CompanyHouseTable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompanyHouseService extends AbstractService {

    static final String SERVICE_NAME = "CompanyHouseService";
    private static final String SERVICE_DESC = "Service that returns A JSON object of company data based on CompanyNumber";

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
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private Gson gson = new Gson();

        @UseDataSet(Sic07.CH_DATASET_NAME)
        private Table chData;

        /**
         * Returns The Business Information as a JSON object from an entered Company Number
         */
        @GET
        @Path("/CH/bussinesnumber/{bussinesnumber}")
        public void getBusinessByNumber(HttpServiceRequest request, HttpServiceResponder responder,
                                        @PathParam("bussinesnumber") String number) {

            Row chRow = chData.get(new Get(number));

            //Error Handing of empty results
            if (chRow.isEmpty()) {
                LOG.debug("No record for Business with Company Number, {} found", number);
                responder.sendStatus(Response.Status.NOT_FOUND.getStatusCode());
                return;
            }

            responder.sendJson(byteMapToJSON(chRow.getColumns()));

        }

        /**
         * Returns The Businesses Information as a JSON objects Based on Post Code Area
         */
        @GET
        @Path("/CH/postcodearea/{postcode}")
        public void getBusinessByPostCodeArea(HttpServiceRequest request, HttpServiceResponder responder,
                             @PathParam("postcode") String postcodeArea) {

            List<JsonObject> jsonElementArrayList = new ArrayList<>();
            Row row;

            try (Scanner scanner = chData.scan(null, null)) {
                while ((row = scanner.next()) != null) {
                    String postCode = row.getString(CompanyHouseTable.POSTCODE_COLUMN);

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
                responder.sendStatus(Response.Status.NOT_FOUND.getStatusCode());
                //responder.sendStatus(HttpURLConnection.HTTP_NOT_FOUND);
                return;
            }

            responder.sendJson(gson.toJsonTree(jsonElementArrayList));

        }

        /**
         * Method that takes byte[] HashMap from results and returns a decoded JSON object with the results
         * Following the JSON structure of Admin Data Service
         */
        private JsonObject byteMapToJSON(Map<byte[], byte[]> hashMap) {

            JsonObject chJsonData = new JsonObject();
            JsonObject chJsonVariables = new JsonObject();

            // Iterates thought results and casts the byte[] objects to Strings to a JSON Object
            for (Map.Entry<byte[], byte[]> entry : hashMap.entrySet()) {
                String keyString = "";
                String valueString = "";

                try {
                    keyString = new String(entry.getKey(), "UTF-8");
                    valueString = new String(entry.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //Adds id and period based on JSON structure
                if (keyString.equals(CompanyHouseTable.PERIOD_COLUMN)) {
                    chJsonData.add(CompanyHouseTable.PERIOD_COLUMN, gson.toJsonTree(valueString));
                } else if (keyString.equals(CompanyHouseTable.COMPANY_NUMBER_COLUMN)) {
                    chJsonData.add(CompanyHouseTable.ID_COLUMN, gson.toJsonTree(valueString));
                }

                //All other values to separate JSON object
                chJsonVariables.add(keyString, gson.toJsonTree(valueString));
            }

            //Adding Variables to the "variables" element in the main JSON Object
            chJsonData.add(CompanyHouseTable.VARIABLES_COLUMN, chJsonVariables);

            // Returned JSON Object created from HashMap
            return chJsonData;
        }


    }



}