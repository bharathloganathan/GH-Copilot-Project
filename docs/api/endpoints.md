# API Route Documentation

<!-- docsync:begin api-routes sources=src/main/java/org/example/api/StudentApiServer.java -->
Routes: GET /students, POST /students, GET /students/{id}, DELETE /students/{id}.

| Method | Path | Summary | Request Type | Response Type |
| --- | --- | --- | --- | --- |
| GET | /students | List all students | none | Student[] |
| POST | /students | Create a student | StudentRequest | Student |
| GET | /students/{id} | Fetch a student by id | none | Student |
| DELETE | /students/{id} | Delete a student | none | DeleteResponse |
<!-- docsync:end api-routes -->
