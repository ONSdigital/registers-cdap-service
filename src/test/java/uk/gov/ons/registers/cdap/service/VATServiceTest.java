package uk.gov.ons.registers.cdap.service;

import co.cask.cdap.api.dataset.table.Put;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.test.ApplicationManager;
import co.cask.cdap.test.DataSetManager;
import co.cask.cdap.test.ServiceManager;
import co.cask.cdap.test.TestBase;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ons.registers.cdap.service.tablecolumns.VATTable;

import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Test for {@link VATService}.
 */

public class VATServiceTest extends TestBase {

    //TEST VAT data row
    private static final String TEST_VAT_REF = "868504062000";
    private static final String TEST_UBRN = "1000012345000103";
    private static final String TEST_NAME_LINE = "R ASMTEPHENSON";
    private static final String TEST_LEGAL_STATUS = "3";

    private JsonObject TEST_JSON;
    private JsonParser jsonParser = new JsonParser();

    private ServiceManager serviceManager;

    @Before
    public void setUp() throws Exception {
        super.beforeTest();

        JsonObject testVariableJson = new JsonObject();
        Gson gson = new Gson();
        TEST_JSON = new JsonObject();

        testVariableJson.add(VATTable.VAT_REF_COLUMN, gson.toJsonTree(TEST_VAT_REF));
        testVariableJson.add(VATTable.UBRN_COLUMN, gson.toJsonTree(TEST_UBRN));
        testVariableJson.add(VATTable.NAME_LINE_COLUMN, gson.toJsonTree(TEST_NAME_LINE));
        testVariableJson.add(VATTable.LEGAL_STATUS_COLUMN, gson.toJsonTree(TEST_LEGAL_STATUS));

        TEST_JSON.add(VATTable.ID_COLUMN, gson.toJsonTree(TEST_VAT_REF));

        TEST_JSON.add(VATTable.VARIABLES_COLUMN, testVariableJson);



        // Deploy the Sic07 application
        ApplicationManager appManager = deployApplication(Sic07.class);

        // Get the CompanyHouse dataset
        DataSetManager<Table> datasetManager = getDataset(VATTable.DATASET_NAME);
        Table companyDataset = datasetManager.get();

        // Add a Business Number, name and PostCode
        Put put = new Put(TEST_VAT_REF);
        put.add(VATTable.VAT_REF_COLUMN, TEST_VAT_REF);
        put.add(VATTable.UBRN_COLUMN, TEST_UBRN);
        put.add(VATTable.NAME_LINE_COLUMN, TEST_NAME_LINE);
        put.add(VATTable.LEGAL_STATUS_COLUMN, TEST_LEGAL_STATUS);
        companyDataset.put(put);

        // Commit our new row to the dataset
        datasetManager.flush();

        // Start Sic07Service service
        serviceManager = appManager.getServiceManager(VATService.SERVICE_NAME).start();

        // Wait service startup
        serviceManager.waitForStatus(true);
    }

    @After
    public void tearDown() throws Exception {
        super.afterTest();
    }

    /**
     * VATREF Search Test
     * Returns The VAT information as a JSON object from an entered VATREF Number
     */

    @Test
    public void testChNumberFound() throws Exception {
        HttpURLConnection connection = httpConnectionHelper("vat/868504062000", Response.Status.OK);

        String response;
        try (AutoCloseable ignored = connection::disconnect) {
            response = new String(ByteStreams.toByteArray(connection.getInputStream()), Charsets.UTF_8);
        }

        JsonElement responseJSON = jsonParser.parse(response);
        assertThat(responseJSON, is(TEST_JSON));
    }

    @Test
    public void testChNumberNotFound() throws Exception {
        httpConnectionHelper("vat/000000", Response.Status.NOT_FOUND);
    }

    private HttpURLConnection httpConnectionHelper(String testURL, Response.Status expectedStatus) throws Exception{
        URL url = new URL(serviceManager.getServiceURL(), testURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        assertThat(connection.getResponseCode(), is(expectedStatus.getStatusCode()));
        return connection;
    }
}
