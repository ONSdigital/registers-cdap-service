package uk.gov.ons.registers.cdap.service.tablecolumns;

public class Sic07Table {
    private Sic07Table(){
        throw new AssertionError("This class is not meant to be instantiated");
    }

    /**
     * Column names used for accessing and tests for the Sic07 Service
     */

    public static final String DATASET_NAME = "sic07";

    //Column names used for accessing and tests
    public static final String DESC_COLUMN = "description";
}
