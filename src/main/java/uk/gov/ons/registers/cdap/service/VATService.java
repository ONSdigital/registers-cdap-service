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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;

public class VATService extends AbstractService {

    static final String SERVICE_NAME = "VATService";
    private static final String SERVICE_DESC = "Service that returns A JSON object of company data based on VAT data";
    //private static final String POSTCODE_COLUMN = "regaddress_postcode";

    @Override
    protected void configure() {
        setName(SERVICE_NAME);
        setDescription(SERVICE_DESC);
        addHandler(new VATService.VATdataHandler());
    }

    /**
     * Handler which defines HTTP endpoints to access information stored in the
     * { @number vatData} Dataset.
     */
    public static class VATdataHandler extends AbstractHttpServiceHandler {
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private Gson gson = new Gson();

        @UseDataSet(Sic07.VAT_DATASET_NAME)
        private Table vatData;

        /**
         * Returns The VAT information as a JSON object from an entered vat ref vatref
         */
        @GET
        @Path("/vat/{vatref}")
        public void getBusinessByNumber(HttpServiceRequest request, HttpServiceResponder responder,
                                        @PathParam("vatref") String vatref) {

            Row vatRow = vatData.get(new Get(vatref));

            //Error Handing of empty results
            if (vatRow.isEmpty()) {
                LOG.debug("No record for Business with Company Number, {} found", vatref);
                responder.sendStatus(Response.Status.NOT_FOUND.getStatusCode());
                return;
            }

            responder.sendJson(gson.toJsonTree(JSONHelper.byteMapToGenericJSON(vatRow.getColumns())));

        }
    }
}
