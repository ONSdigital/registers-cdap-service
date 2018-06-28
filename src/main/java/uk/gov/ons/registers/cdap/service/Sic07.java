package uk.gov.ons.registers.cdap.service;

import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.dataset.table.Table;
import uk.gov.ons.registers.cdap.service.tablecolumns.CompanyHouseTable;
import uk.gov.ons.registers.cdap.service.tablecolumns.Sic07Table;
import uk.gov.ons.registers.cdap.service.tablecolumns.VATTable;

/**
 * An application to provide access to SIC (industry) codes
 */
public class Sic07 extends AbstractApplication {

  private static final String APP_NAME = "sic07_lookup";
  private static final String APP_DESC = "An application to lookup SIC descriptions for codes";

  @Override
  public void configure() {
    setName(APP_NAME);
    setDescription(APP_DESC);

    createDataset(Sic07Table.DATASET_NAME, Table.class);
    createDataset(CompanyHouseTable.DATASET_NAME, Table.class);
    createDataset(VATTable.DATASET_NAME, Table.class);

    addService(new Sic07Service());
    addService(new CompanyHouseService());
    addService(new VATService());
  }

}
