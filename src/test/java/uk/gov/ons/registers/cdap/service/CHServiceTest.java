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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Test for {@link CHService}.
 */
public class CHServiceTest extends TestBase {

    private static final String TEST_CH_NUMBER = "11240759";
    private static final String TEST_CH_NAME_COLUMN = "companyname";
    private static final String TEST_CH_NAME = "ANIMAL MICROCHIPS LTD";
    private JsonElement TEST_JSON;

    private ServiceManager serviceManager;

    @Before
    public void setUp() throws Exception {
        super.beforeTest();

        // SetUp JSON Object
        HashMap<String, String> testMap = new HashMap<>();
        testMap.put(TEST_CH_NAME_COLUMN, TEST_CH_NAME);
        Gson gson = new Gson();
        TEST_JSON = gson.toJsonTree(testMap);

        // Deploy the Sic07 application
        ApplicationManager appManager = deployApplication(Sic07.class);

        // Get the CompanyHouse dataset
        DataSetManager<Table> datasetManager = getDataset(Sic07.CH_DATASET_NAME);
        Table sicCodes = datasetManager.get();

        // Add a Business Number and name
        Put put = new Put(TEST_CH_NUMBER);
        put.add(TEST_CH_NAME_COLUMN, TEST_CH_NAME);
        sicCodes.put(put);

        // Commit our new row to the dataset
        datasetManager.flush();

        // Start Sic07Service service
        serviceManager = appManager.getServiceManager(CHService.SERVICE_NAME).start();

        // Wait service startup
        serviceManager.waitForStatus(true);
    }

    @After
    public void tearDown() throws Exception {
        super.afterTest();
    }

    @Test
    public void testFound() throws Exception {
        URL url = new URL(serviceManager.getServiceURL(), "CH/number/11240759");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        String response;
        try {

            response = new String(ByteStreams.toByteArray(connection.getInputStream()), Charsets.UTF_8);
        } finally {
            connection.disconnect();
        }
        Assert.assertEquals(TEST_JSON.toString(), response);
    }

    @Test
    public void testNotFound() throws Exception {
        URL url = new URL(serviceManager.getServiceURL(), "CH/000000");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
    }
}
