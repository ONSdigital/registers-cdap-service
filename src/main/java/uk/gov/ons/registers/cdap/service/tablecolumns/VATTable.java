package uk.gov.ons.registers.cdap.service.tablecolumns;

public class VATTable {
    private VATTable(){
        throw new AssertionError("This class is not meant to be instantiated");
    }

    /**
     * Column names used for accessing and tests for the VAT Service
     */

    public static final String DATASET_NAME = "VAT_DATA";

    //Column names used for accessing and tests
    public static String VAT_REF_COLUMN = "vatref";
    public static String UBRN_COLUMN = "ubrn";
    public static String NAME_LINE_COLUMN = "nameline";
    public static String LEGAL_STATUS_COLUMN = "legalstatus";

    //Generic Columns use with the Admin Data Service JSON structure
    public static String PERIOD_COLUMN = "period";
    public static String ID_COLUMN = "id";
    public static String VARIABLES_COLUMN = "variables";
}