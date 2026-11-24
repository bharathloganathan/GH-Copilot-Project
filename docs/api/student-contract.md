# Student Contract Documentation

<!-- docsync:begin api-contract sources=src/main/java/org/example/model/Student.java -->
## Student model

Source: `src/main/java/org/example/model/Student.java`

| Field | Type |
| --- | --- |
| id | String |
| name | String |
| email | String |
| department | String |
| phone | String |
| status | String |
<!-- docsync:end api-contract -->

<!-- docsync:begin api-request sources=src/main/java/org/example/api/StudentApiServer.java -->
## Create request form parameters

Source: `src/main/java/org/example/api/StudentApiServer.java`

| Parameter |
| --- |
| name |
| email |
| department |
| phone |
<!-- docsync:end api-request -->

## Manual notes

Request fields are form-encoded parameters read by the create endpoint.
