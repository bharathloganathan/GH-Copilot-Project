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

<!-- docsync:begin api-list-response sources=src/main/java/org/example/api/StudentApiServer.java -->
## Successful `GET /students` response

Source: `src/main/java/org/example/api/StudentApiServer.java`

Derived from the `/students` GET dispatch through `listStudents`, `sendJson`, `studentsAsJson`, `appendJsonField` and `appendJsonString`.

- Status: `200`
- Media type: `application/json`
- Top-level JSON value: array
- Empty collection serialization: `[]`
- Serialized object fields (source order): id, name, email, department, phone, status
<!-- docsync:end api-list-response -->

## Manual notes

Request fields are form-encoded parameters read by the create endpoint.
