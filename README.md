# registers-cdap-service

Currently contains Two services:
 - Sic07Service retrieving SIC descriptions for codes
 - CHService returns a JSON object of company data based on CompanyNumber 
 

## Prerequsites
Cask CDAP sandbox sandbox environment installed

SIC 07 data (available from [ONS](https://www.ons.gov.uk/methodology/classificationsandstandards/ukstandardindustrialclassificationofeconomicactivities/uksic2007)) loaded into a Cask CDAP table with the following schema

| sic_code      | description   |
| :------------:|:-------------:|
|  10110 | Processing and preserving of meat |

Company Data from Companies House (available from [Companies House](http://download.companieshouse.gov.uk/en_output.html)) loaded into a Cask CDAP table with the following schema

## Build Instructions

```
mvn clean package
```

## Deployment Instructions

```
cdap sandbox start

cdap cli deploy app target/registers-cdap-service-0.0.1-SNAPSHOT.jar

cdap cli start service sic07_lookup.Sic07Service

cdap cli start service sic07_lookup.CHService
```

## Testing the Service

SIC Service
```
curl -v localhost:11015/v3/namespaces/default/apps/sic07_lookup/services/Sic07Service/methods/sic07/{code}
```
Company House Service
```
curl -v localhost:11015/v3/namespaces/default/apps/sic07_lookup/services/CHService/methods/CH/{number}
```