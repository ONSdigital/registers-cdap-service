package uk.gov.ons.registers.cdap.service;

import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.dataset.table.Get;
import co.cask.cdap.api.dataset.table.Row;
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
import java.util.HashMap;
import java.util.Map;

public class CHService extends AbstractService {

    public static final String SERVICE_NAME = "CHService";
    private static final String SERVICE_DESC = "Service that returns A JSON object of company data based on CompanyNumber";

    @Override
    protected void configure() {
        setName(SERVICE_NAME);
        setDescription(SERVICE_DESC);
        addHandler(new CHdataHandler());
    }

    /**
     * Handler which defines HTTP endpoints to access information stored in the
     * {@number CHdata} Dataset.
     */
    public static class CHdataHandler extends AbstractHttpServiceHandler {
        private static final Logger LOG = LoggerFactory.getLogger(CHdataHandler.class);

        @UseDataSet(Sic07.CH_DATASET_NAME)
        private Table chData;

        /**
         * Returns The Business Information as a JSON object from an entered Company Number
         */
        @GET
        @Path("/CH/{number}")
        public void getSic07(HttpServiceRequest request, HttpServiceResponder responder,
                @PathParam("number") String number) {

            Row chRow = chData.get(new Get(number));

            //Error Handing of Null results
            if (chRow.isEmpty()) {
                LOG.debug("No record for Business with Company Number, {} found", number);
                responder.sendStatus(HttpURLConnection.HTTP_NOT_FOUND);
                return;
            }

            //Loads Results into byte[] hashMap object
            Map<byte[], byte[]> chMapString = chRow.getColumns();

            HashMap<String, String> chBusinessData = new HashMap<>();

            // Iterates thought results and casts the byte[] objects to Strings to a <String, String> HashMap
            for (Map.Entry<byte[], byte[]> entry : chMapString.entrySet()) {
                String keyString = new String(entry.getKey());
                String valueString = new String(entry.getValue());

                chBusinessData.put(keyString, valueString);
            }

            // Creating JSON Object from HashMap
            Gson gson = new Gson();
            JsonElement json = gson.toJsonTree(chBusinessData);

            responder.sendJson(json);

        }
    }
}