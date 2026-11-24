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
};

function validateStudentForm(fields) {
  const errors = {};
  for (const [fieldName, rule] of Object.entries(validationRules)) {
    const value = String(fields[fieldName] ?? '').trim();
    if (rule.required && !value) {
      errors[fieldName] = rule.message;
      continue;
    }
    if (!value && !rule.required) {
      continue;
    }
    if (rule.minLength && value.length < rule.minLength) {
      errors[fieldName] = rule.message;
      continue;
    }
    if (rule.pattern && !rule.pattern.test(value)) {
      errors[fieldName] = rule.message;
    }
  }
  return errors;
}

async function submitStudentForm(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const formData = Object.fromEntries(new FormData(form).entries());
  const errors = validateStudentForm(formData);
  if (Object.keys(errors).length > 0) {
    window.alert(Object.values(errors)[0]);
    return;
  }

  const payload = new URLSearchParams({
    name: formData.studentName,
    email: formData.studentEmail,
    department: formData.studentDepartment,
    phone: formData.studentPhone
  });

  try {
    const response = await fetch('/students', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: payload.toString()
    });

    const text = await response.text();
    if (!response.ok) {
      throw new Error(text || 'Submission failed');
    }

    window.alert('Student record created successfully.');
    form.reset();
  } catch (error) {
    window.alert(error.message || 'Submission failed');
  }
}

const form = document.getElementById('student-form');
if (form) {
  form.addEventListener('submit', submitStudentForm);
}