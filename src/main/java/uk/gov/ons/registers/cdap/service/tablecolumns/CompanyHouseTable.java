package uk.gov.ons.registers.cdap.service.tablecolumns;

public class CompanyHouseTable {
    private CompanyHouseTable(){
        throw new AssertionError("This class is not meant to be instantiated");
    }

    /**
     * Column names used for accessing and tests for the Company House Service
     */

    public static final String DATASET_NAME = "CompanyData";

    //Column names used for accessing and tests
    public static final String POSTCODE_COLUMN = "regaddress_postcode";
    public static final String COMPANY_NAME_COLUMN = "companyname";
    public static final String COMPANY_NUMBER_COLUMN = "companynumber";

    //Generic Columns use with the Admin Data Service JSON structure
    public static final String PERIOD_COLUMN = "period";
    public static final String ID_COLUMN = "id";
    public static final String VARIABLES_COLUMN = "variables";
}