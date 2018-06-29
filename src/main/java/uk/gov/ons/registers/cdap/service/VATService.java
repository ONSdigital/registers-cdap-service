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
import uk.gov.ons.registers.cdap.service.tablecolumns.VATTable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

public class VATService extends AbstractService {

    static final String SERVICE_NAME = "VATService";
    private static final String SERVICE_DESC = "Service that returns A JSON object of company data based on VAT data";

    @Override
    protected void configure() {
        setName(SERVICE_NAME);
        setDescription(SERVICE_DESC);
        addHandler(new VATService.VATdataHandler());
    }

    private static Function<String, String> mapAndFilterKeys = (sourceColumnName) -> {
        String destinationColumnName;

        switch (sourceColumnName) {
            case VATTable.PERIOD_COLUMN:
                destinationColumnName = VATTable.PERIOD_COLUMN;
                break;
            case VATTable.VAT_REF_COLUMN:
                destinationColumnName = VATTable.ID_COLUMN;
                break;
            default:
                destinationColumnName = "";
        }

        return destinationColumnName;
    };


    /**
     * Handler which defines HTTP endpoints to access information stored in the
     * { @number vatData} Dataset.
     */
    public static class VATdataHandler extends AbstractHttpServiceHandler {
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private Gson gson = new Gson();

        @UseDataSet(VATTable.DATASET_NAME)
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

            responder.sendJson(gson.toJsonTree(JSONHelper.byteMapToGenericJSON(vatRow.getColumns(), mapAndFilterKeys)));

        }
    }
}
