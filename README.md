# registers-cdap-service

Currently contains one service for retrieving SIC descriptions for codes

## Prerequsites
Cask CDAP sandbox sandbox environment installed

SIC 07 data (available from [ONS](https://www.ons.gov.uk/methodology/classificationsandstandards/ukstandardindustrialclassificationofeconomicactivities/uksic2007)) loaded into a Cask CDAP table with the following schema

| sic_code      | description   |
| :------------:|:-------------:|
|  10110 | Processing and preserving of meat |

## Build Instructions

```
mvn clean package
```

## Deployment Instructions

```
cdap sandbox start

cdap cli deploy app target/registers-cdap-service-0.0.1-SNAPSHOT.jar

cdap cli start service Sic07.Sic07Service
```

## Testing the Service
```
curl -v localhost:11015/v3/namespaces/default/apps/Sic07/services/Sic07Service/methods/sic07/{code}
```