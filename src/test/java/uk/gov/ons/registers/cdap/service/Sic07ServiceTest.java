package uk.gov.ons.registers.cdap.service;

import co.cask.cdap.api.dataset.table.Put;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.test.*;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.junit.*;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Test for {@link Sic07Service}.
 */
public class Sic07ServiceTest extends TestBase {

    private static final String TEST_SIC_CODE = "10110";
    private static final String TEST_SIC_DESC = "Processing and preserving of meat";
    private ServiceManager serviceManager;

    @Before
    public void setUp() throws Exception {
        super.beforeTest();

        // Deploy the Sic07 application
        ApplicationManager appManager = deployApplication(Sic07.class);

        // Get the Sic07 dataset
        DataSetManager<Table> datasetManager = getDataset(Sic07.DATASET_NAME);
        Table sicCodes = datasetManager.get();

        // Add a SIC code and description
        Put put = new Put(TEST_SIC_CODE);
        put.add(Sic07Service.Sic07Handler.DESC_COLUMN, TEST_SIC_DESC);
        sicCodes.put(put);

        // Commit our new row to the dataset
        datasetManager.flush();

        // Start Sic07Service service
        serviceManager = appManager.getServiceManager(Sic07Service.SERVICE_NAME).start();

        // Wait service startup
        serviceManager.waitForStatus(true);
    }

    @After
    public void tearDown() throws Exception {
        super.afterTest();
    }

    @Test
    public void testFound() throws Exception {
        URL url = new URL(serviceManager.getServiceURL(), "sic07/10110");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        String response;
        try {
            response = new String(ByteStreams.toByteArray(connection.getInputStream()), Charsets.UTF_8);
        } finally {
            connection.disconnect();
        }
        Assert.assertEquals(TEST_SIC_DESC, response);
    }

    @Test
    public void testNotFound() throws Exception {
        URL url = new URL(serviceManager.getServiceURL(), "sic07/00000");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
    }
}
