# Student UI Documentation

<!-- docsync:begin ui-fields sources=frontend/index.html -->
## Form fields

Source: `frontend/index.html`

| Field ID | Type | Required |
| --- | --- | --- |
| studentName | text | Yes |
| studentEmail | email | Yes |
| studentDepartment | select | Yes |
| studentPhone | tel | No |
<!-- docsync:end ui-fields -->

<!-- docsync:begin ui-validation sources=frontend/app.js -->
## Validation rules

Source: `frontend/app.js`

```javascript
const validationRules = {
  studentName: {
    required: true,
    minLength: 2,
    message: 'Student name must be at least 2 characters.'
  },
  studentEmail: {
    required: true,
    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    message: 'Email must be a valid address.'
  },
  studentDepartment: {
    required: true,
    message: 'Department is required.'
  },
  studentPhone: {
    required: false,
    pattern: /^(?=.{7,20}$)\+?[0-9 -]+$/,
    message: 'Phone must contain 7 to 20 digits, spaces or hyphens, with an optional leading +.'
  }
}
```
<!-- docsync:end ui-validation -->
