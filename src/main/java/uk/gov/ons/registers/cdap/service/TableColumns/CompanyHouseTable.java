package uk.gov.ons.registers.cdap.service.TableColumns;

public class CompanyHouseTable {
    private CompanyHouseTable(){
        throw new AssertionError("This class is not meant to be instantiated");
    }

    /**
     * Column names used for accessing and tests for the Company House Service
     */

    //Column names used for accessing and tests
    public static String POSTCODE_COLUMN = "regaddress_postcode";
    public static String COMPANY_NAME_COLUMN = "companyname";
    public static String COMPANY_NUMBER_COLUMN = "companynumber";

    //Generic Columns use with the Admin Data Service JSON structure
    public static String PERIOD_COLUMN = "period";
    public static String ID_COLUMN = "id";
    public static String VARIABLES_COLUMN = "variables";
}