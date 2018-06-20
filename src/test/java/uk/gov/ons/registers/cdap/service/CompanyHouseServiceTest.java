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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

/**
 * Test for {@link CompanyHouseService}.
 */
public class CompanyHouseServiceTest extends TestBase {

    private static final String TEST_CH_NUMBER = "11240759";

    private static final String TEST_CH_NAME_COLUMN = "companyname";
    private static final String TEST_CH_NAME = "ANIMAL MICROCHIPS LTD";
    private static final String TEST_CH_POSTCODE_COLUMN = "regaddress_postcode";
    private static final String TEST_CH_POSTCODE = "TA4 3NA";
    private JsonElement TEST_JSON;
    private ArrayList<JsonElement> TEST_JSON_ARRAYLIST;

    private ServiceManager serviceManager;

    @Before
    public void setUp() throws Exception {
        super.beforeTest();

        Map<String, String> testMap = new HashMap<>();
        testMap.put(TEST_CH_NAME_COLUMN, TEST_CH_NAME);
        testMap.put(TEST_CH_POSTCODE_COLUMN, TEST_CH_POSTCODE);
        Gson gson = new Gson();
        TEST_JSON = gson.toJsonTree(testMap);

        TEST_JSON_ARRAYLIST = new ArrayList<>();
        TEST_JSON_ARRAYLIST.add(TEST_JSON);

        // Deploy the Sic07 application
        ApplicationManager appManager = deployApplication(Sic07.class);

        // Get the CompanyHouse dataset
        DataSetManager<Table> datasetManager = getDataset(Sic07.CH_DATASET_NAME);
        Table sicCodes = datasetManager.get();

        // Add a Business Number, name and PostCode
        Put put = new Put(TEST_CH_NUMBER);
        put.add(TEST_CH_NAME_COLUMN, TEST_CH_NAME);
        put.add(TEST_CH_POSTCODE_COLUMN, TEST_CH_POSTCODE);
        sicCodes.put(put);

        // Commit our new row to the dataset
        datasetManager.flush();

        // Start Sic07Service service
        serviceManager = appManager.getServiceManager(CompanyHouseService.SERVICE_NAME).start();

        // Wait service startup
        serviceManager.waitForStatus(true);
    }

    @After
    public void tearDown() throws Exception {
        super.afterTest();
    }

    /**
     * Business Number Search Test
     * Returns The Business Information as a JSON object from an entered Company Number
     */

    @Test
    public void testChNumberFound() throws Exception {
        HttpURLConnection connection = httpConnectionHelper("CH/bussinesnumber/11240759", Response.Status.OK);

        String response;
        try (AutoCloseable ignored = connection::disconnect) {
            response = new String(ByteStreams.toByteArray(connection.getInputStream()), Charsets.UTF_8);
        }

        assertThat(response, is(TEST_JSON.toString()));
    }

    @Test
    public void testChNumberNotFound() throws Exception {
        httpConnectionHelper("CH/bussinesnumber/000000", Response.Status.NOT_FOUND);
    }

    /**
     * Postcode Area Search Test
     * Returns The Businesses Information as a JSON objects Based on Post Code Area
     */

    @Test
    public void testChPostcodeFound() throws Exception {
        HttpURLConnection connection = httpConnectionHelper("CH/postcodearea/TA4", Response.Status.OK);

        String response;
        try (AutoCloseable ignored = connection::disconnect) {
            response = new String(ByteStreams.toByteArray(connection.getInputStream()), Charsets.UTF_8);
        }

        assertThat(response, is(TEST_JSON_ARRAYLIST.toString()));
    }

    @Test
    public void testChPostcodeNotFound() throws Exception {
        httpConnectionHelper("CH/postcodearea/NP20", Response.Status.NOT_FOUND);
    }

    private HttpURLConnection httpConnectionHelper(String testURL, Response.Status expectedStatus) throws Exception{
        URL url = new URL(serviceManager.getServiceURL(), testURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        assertThat(connection.getResponseCode(), is(expectedStatus.getStatusCode()));
        return connection;
    }

}
