# registers-cdap-service

Currently contains Two services:
 - Sic07Service retrieving SIC descriptions for codes
 - CompanyHouseService
    - Returns a JSON object of company data based on CompanyNumber 
    - Returns JSON objects of company data based on Post Code area
 

## Prerequsites
Cask CDAP sandbox sandbox environment installed

SIC 07 data (available from [ONS](https://www.ons.gov.uk/methodology/classificationsandstandards/ukstandardindustrialclassificationofeconomicactivities/uksic2007)) loaded into a Cask CDAP table with the following schema

| sic_code      | description   |
| :------------:|:-------------:|
|  10110 | Processing and preserving of meat |

Company Data from Companies House (available from [Companies House](http://download.companieshouse.gov.uk/en_output.html)) loaded into a Cask CDAP table with the following schema

| companyname                 | companynumber | regaddresscareof | regaddresspobox | regaddressaddressline1   | regaddressaddressline2 | regaddressposttown | regaddresscounty | regaddresscountry | regaddresspostcode | companycategory         | companystatus | countryoforigin | dissolutiondate | incorporationdate | accountsaccountrefday | accountsaccountrefmonth | accountsnextduedate | accountslastmadeupdate | accountsaccountcategory | returnsnextduedate | returnslastmadeupdate | mortgagesnummortcharges | mortgagesnummortoutstanding | mortgagesnummortpartsatisfied | mortgagesnummortsatisfied | siccodesictext_1                                                 | siccodesictext_2 | siccodesictext_3 | siccodesictext_4 | limitedpartnershipsnumgenpartners | limitedpartnershipsnumlimpartners | uri                                             | previousname_1condate | previousname_1companyname | previousname_2condate | previousname_2companyname | previousname_3condate | previousname_3companyname | previousname_4condate | previousname_4companyname | previousname_5condate | previousname_5companyname | previousname_6condate | previousname_6companyname | previousname_7condate | previousname_7companyname | previousname_8condate | previousname_8companyname | previousname_9condate | previousname_9companyname | previousname_10condate | previousname_10companyname | confstmtnextduedate | confstmtlastmadeupdate |
|-----------------------------|---------------|------------------|-----------------|--------------------------|------------------------|--------------------|------------------|-------------------|--------------------|-------------------------|---------------|-----------------|-----------------|-------------------|-----------------------|-------------------------|---------------------|------------------------|-------------------------|--------------------|-----------------------|-------------------------|-----------------------------|-------------------------------|---------------------------|------------------------------------------------------------------|------------------|------------------|------------------|-----------------------------------|-----------------------------------|-------------------------------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|-----------------------|---------------------------|------------------------|----------------------------|---------------------|------------------------|
| ALBERT BOWLING CLUB LIMITED | 00009117          |                  |                 | 39-41 OLD LANSDOWNE ROAD | WEST DIDSBURY          | MANCHESTER         |                  |                   | M20 2PA            | Private Limited Company | Active        | United Kingdom  |                 | 16/01/1875        | 31                    | 12                      | 29/09/14            | 30/12/12               | TOTAL EXEMPTION FULL    | 19/06/13           | 22/05/12              | 5                       | 0                           | 0                             | 5                         | 68209 - Other letting and operating of own or leased real estate |                  |                  |                  | 0                                 | 0                                 | http://business.data.gov.uk/id/company/00009117 |                       |                           |                       |                           |                       |                           |                       |                           |                       |                           |                       |                           |                       |                           |                       |                           |                       |                           |                        |                            |                     |                        |

## Build Instructions

```
mvn clean package
```

## Deployment Instructions

```
cdap sandbox start

cdap cli deploy app target/registers-cdap-service-0.0.1-SNAPSHOT.jar

cdap cli start service sic07_lookup.Sic07Service

cdap cli start service sic07_lookup.CompanyHouseService
```

## Testing the Service

SIC Service
```
curl -v localhost:11015/v3/namespaces/default/apps/sic07_lookup/services/Sic07Service/methods/sic07/{code}
```
##### Company House Service

Find company based on Business Number
```
curl -v localhost:11015/vp/services/CompanyHouseService/methods/CH/bussinesnumber/{bussinesnumber}
```

Find companies based on Post Code Area
```
curl -v localhost:11015/v3/namespaces/default/apps/sic07_lookup/services/CompanyHouseService/methods/CH/postcodearea/{postcodearea}
```