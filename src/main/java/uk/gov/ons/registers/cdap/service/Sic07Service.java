package uk.gov.ons.registers.cdap.service;

import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.dataset.table.Get;
import co.cask.cdap.api.dataset.table.Row;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.api.service.AbstractService;
import co.cask.cdap.api.service.http.AbstractHttpServiceHandler;
import co.cask.cdap.api.service.http.HttpServiceRequest;
import co.cask.cdap.api.service.http.HttpServiceResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.HttpURLConnection;

/**
 * Service that exposes endpoints to retrieve page view counts.
 */
class Sic07Service extends AbstractService {

    public static final String SERVICE_NAME = "Sic07Service";
    private static final String SERVICE_DESC = "Service that returns descriptions for industry classification codes for SIC07";

    @Override
    protected void configure() {
        setName(SERVICE_NAME);
        setDescription(SERVICE_DESC);
        addHandler(new Sic07Handler());
    }

    /**
     * Handler which defines HTTP endpoints to access information stored in the {@code sic07} Dataset.
     */
    public static class Sic07Handler extends AbstractHttpServiceHandler {
        private static final Logger LOG = LoggerFactory.getLogger(Sic07Handler.class);
        static final String DESC_COLUMN = "description";

        @UseDataSet(Sic07.DATASET_NAME)
        private Table sicCodes;

        /**
         * Returns SIC 07 description for a code
         */
        @GET
        @Path("/sic07/{code}")
        public void getSic07(HttpServiceRequest request,
                             HttpServiceResponder responder,
                             @PathParam("code") String code) {

            Row sic07 = sicCodes.get(new Get(code));
            if (sic07.isEmpty()) {
                LOG.debug("No record for SIC code {} found", code);
                responder.sendStatus(HttpURLConnection.HTTP_NOT_FOUND);
                return;
            }
            responder.sendString(sic07.getString(DESC_COLUMN));
        }
    }
}
