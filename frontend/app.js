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
    await loadStudents();
  } catch (error) {
    window.alert(error.message || 'Submission failed');
  }
}

function isStudentListResponse(value) {
  return Array.isArray(value) && value.every(student =>
    student !== null &&
    typeof student === 'object' &&
    !Array.isArray(student) &&
    typeof student.name === 'string' &&
    typeof student.email === 'string' &&
    typeof student.department === 'string' &&
    (student.phone === undefined || student.phone === null || typeof student.phone === 'string')
  );
}

function renderStudents(students) {
  const tableContainer = document.getElementById('students-table-container');
  const tableBody = document.getElementById('registered-students-body');
  const status = document.getElementById('student-list-status');
  tableBody.replaceChildren();

  if (students.length === 0) {
    setStudentListState('empty', 'No students registered yet.');
    return;
  }

  for (const student of students) {
    const row = document.createElement('tr');
    for (const value of [student.name, student.email, student.department, student.phone ?? '']) {
      const cell = document.createElement('td');
      cell.textContent = value;
      row.appendChild(cell);
    }
    tableBody.appendChild(row);
  }

  status.dataset.state = 'populated';
  status.textContent = '';
  status.hidden = true;
  tableContainer.hidden = false;
}

function setStudentListState(state, message) {
  const status = document.getElementById('student-list-status');
  const tableContainer = document.getElementById('students-table-container');
  status.dataset.state = state;
  status.textContent = message;
  status.hidden = false;
  tableContainer.hidden = true;
}

async function loadStudents() {
  setStudentListState('loading', 'Loading students...');
  try {
    const response = await fetch('/students');
    if (!response.ok) {
      throw new Error('Student list request failed.');
    }
    const contentType = response.headers.get('Content-Type') || '';
    const mediaType = contentType.split(';', 1)[0].trim();
    if (mediaType.toLowerCase() !== 'application/json') {
      throw new Error('Student list response was not JSON.');
    }
    const students = await response.json();
    if (!isStudentListResponse(students)) {
      throw new Error('Student list response had an incompatible format.');
    }
    renderStudents(students);
  } catch (error) {
    setStudentListState('error', 'Unable to load registered students. Please try again.');
  }
}

const form = document.getElementById('student-form');
if (form) {
  form.addEventListener('submit', submitStudentForm);
  loadStudents();
}