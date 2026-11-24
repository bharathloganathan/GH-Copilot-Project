---
applyTo: "frontend/**/*.{html,css,js}"
---

# Frontend Instructions

- Keep the demo dependency-free: plain HTML, CSS and JavaScript.
- Keep UI validation rules in the `validationRules` object in `frontend/app.js`; the documentation extractor currently expects one object declaration and does not support template literals there.
- Give each form control a unique `id`; use semantic labels and matching `for` attributes. Required state and input type should be represented in the HTML.
- Keep request parameter names consistent with `StudentApiServer` and the API contract.
- Do not add secrets or trust client-side validation as a substitute for backend validation.
- Add or update UI documentation through `DocSyncCli`; never hand-edit generated blocks.
