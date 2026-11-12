# Student UI Documentation

<!-- docsync:begin ui-fields sources=frontend/index.html -->
## Form fields

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| studentName | text | Yes | Student name |
| studentEmail | email | Yes | Valid email address required |
| studentDepartment | select | Yes | Department dropdown |

The validationRules object in app.js validates the same UI fields.

<!-- docsync:end ui-fields -->

<!-- docsync:begin ui-validation sources=frontend/app.js -->
## Validation rules

- studentName: required, minimum 2 chars
- studentEmail: required and must match standard email pattern
- studentDepartment: required

validationRules: studentName, studentEmail, studentDepartment

<!-- docsync:end ui-validation -->
