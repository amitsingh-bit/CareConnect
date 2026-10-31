export type Row = Record<string, any>;

export interface Field {
  key: string;
  label: string;
  type?: string;
  required?: boolean;
  options?: string;
  placeholder?: string;
}

export interface Column {
  label: string;
  path: string;
}

export interface Resource {
  key: string;
  endpoint: string;
  label: string;
  singular: string;
  icon: string;
  roles: string[];
  columns: Column[];
  fields: Field[];
  canCreate?: boolean;
  canEdit?: boolean;
  canDelete?: boolean;
}

export const navigationByRole: Record<string, string[]> = {
  PATIENT: ['appointments', 'medical-records', 'diagnoses', 'prescriptions', 'lab-reports', 'vitals', 'doctors'],
  DOCTOR: ['appointments', 'patients', 'medical-records', 'prescriptions', 'lab-reports', 'diagnoses', 'vitals', 'doctors', 'medications'],
  NURSE: ['appointments', 'patients', 'medical-records', 'lab-reports', 'vitals', 'doctors'],
  ADMIN: ['appointments', 'patients', 'doctors', 'nurses', 'medications', 'users', 'admins', 'audit-logs']
};

const roleGroups = {
  all: ['PATIENT', 'DOCTOR', 'NURSE', 'ADMIN'],
  clinical: ['DOCTOR', 'NURSE', 'ADMIN'],
  doctors: ['PATIENT', 'DOCTOR', 'NURSE', 'ADMIN'],
  admin: ['ADMIN']
};

export const resources: Resource[] = [
  {
    key: 'appointments',
    endpoint: 'appointments',
    label: 'Appointments',
    singular: 'appointment',
    icon: '◷',
    roles: roleGroups.all,
    columns: [
      { label: 'Patient', path: 'patientName' },
      { label: 'Doctor', path: 'doctorName' },
      { label: 'Date & time', path: 'appointmentDateTime' },
      { label: 'Status', path: 'status' },
      { label: 'Reason', path: 'reason' }
    ],
    fields: [
      { key: 'patientId', label: 'Patient', type: 'select', options: 'patients', required: true },
      { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors', required: true },
      { key: 'appointmentDateTime', label: 'Date & time', type: 'datetime-local', required: true },
      { key: 'status', label: 'Status', type: 'select', options: 'appointmentStatus', required: true },
      { key: 'reason', label: 'Reason', required: true, placeholder: 'Reason for visit' }
    ]
  },
  {
    key: 'patients',
    endpoint: 'patients',
    label: 'Patients',
    singular: 'patient',
    icon: '♡',
    roles: [...roleGroups.clinical, 'ADMIN'],
    columns: [
      { label: 'Name', path: 'name' },
      { label: 'Age', path: 'age' },
      { label: 'Gender', path: 'gender' },
      { label: 'Health concern', path: 'disease' },
      { label: 'Blood group', path: 'bloodGroup' },
      { label: 'Phone', path: 'phone' }
    ],
    fields: [
      { key: 'name', label: 'Full name', required: true },
      { key: 'age', label: 'Age', type: 'number', required: true },
      { key: 'disease', label: 'Health concern', required: true },
      { key: 'gender', label: 'Gender' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'phone', label: 'Phone', type: 'tel' },
      { key: 'bloodGroup', label: 'Blood group' },
      { key: 'address', label: 'Address' },
      { key: 'emergencyContact', label: 'Emergency contact' }
    ]
  },
  {
    key: 'medical-records',
    endpoint: 'medical-records',
    label: 'Medical records',
    singular: 'medical record',
    icon: '▤',
    roles: ['PATIENT', 'DOCTOR', 'NURSE'],
    columns: [
      { label: 'Patient', path: 'patientName' },
      { label: 'Diagnosis', path: 'diagnosis' },
      { label: 'Treatment', path: 'treatment' },
      { label: 'Recorded', path: 'recordedAt' }
    ],
    fields: [
      { key: 'patientId', label: 'Patient', type: 'select', options: 'patients', required: true },
      { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors' },
      { key: 'diagnosis', label: 'Diagnosis', required: true },
      { key: 'treatment', label: 'Treatment plan', required: true },
      { key: 'notes', label: 'Clinical notes', type: 'textarea' },
      { key: 'recordedAt', label: 'Date & time', type: 'datetime-local', required: true }
    ]
  },
  {
    key: 'prescriptions',
    endpoint: 'prescriptions',
    label: 'Prescriptions',
    singular: 'prescription',
    icon: '✚',
    roles: ['PATIENT', 'DOCTOR'],
    columns: [
      { label: 'Medication', path: 'medicationName' },
      { label: 'Dosage', path: 'dosage' },
      { label: 'Frequency', path: 'frequency' },
      { label: 'Duration', path: 'duration' },
      { label: 'Status', path: 'status' },
      { label: 'Prescribed', path: 'prescribedDate' }
    ],
    fields: [
      { key: 'medicalRecordId', label: 'Medical record', type: 'select', options: 'records', required: true },
      { key: 'doctorId', label: 'Prescribing doctor', type: 'select', options: 'doctors' },
      { key: 'medicationId', label: 'Medication', type: 'select', options: 'medications', required: true },
      { key: 'dosage', label: 'Dosage', required: true, placeholder: 'e.g. 500 mg' },
      { key: 'frequency', label: 'Frequency', required: true, placeholder: 'e.g. Twice daily' },
      { key: 'duration', label: 'Duration', required: true, placeholder: 'e.g. 7 days' },
      { key: 'status', label: 'Status', type: 'select', options: 'prescriptionStatus', required: true },
      { key: 'instructions', label: 'Instructions', type: 'textarea' },
      { key: 'prescribedDate', label: 'Prescribed date', type: 'date', required: true }
    ]
  },
  {
    key: 'lab-reports',
    endpoint: 'lab-reports',
    label: 'Lab reports',
    singular: 'lab report',
    icon: '⌁',
    roles: ['PATIENT', 'DOCTOR', 'NURSE'],
    columns: [
      { label: 'Patient', path: 'patientName' },
      { label: 'Test', path: 'testName' },
      { label: 'Result', path: 'result' },
      { label: 'Reference range', path: 'referenceRange' },
      { label: 'Status', path: 'status' },
      { label: 'Report date', path: 'reportDate' }
    ],
    fields: [
      { key: 'medicalRecordId', label: 'Medical record', type: 'select', options: 'records', required: true },
      { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors' },
      { key: 'testName', label: 'Test name', required: true },
      { key: 'result', label: 'Result', required: true },
      { key: 'referenceRange', label: 'Reference range' },
      { key: 'status', label: 'Status', type: 'select', options: 'labStatus', required: true },
      { key: 'reportDate', label: 'Report date', type: 'date', required: true }
    ]
  },
  {
    key: 'diagnoses',
    endpoint: 'diagnoses',
    label: 'Diagnoses',
    singular: 'diagnosis',
    icon: '⊕',
    roles: ['PATIENT', 'DOCTOR'],
    columns: [
      { label: 'Patient', path: 'patientName' },
      { label: 'Diagnosis', path: 'diagnosis' },
      { label: 'Doctor', path: 'doctorName' },
      { label: 'Date', path: 'diagnosedAt' },
      { label: 'Notes', path: 'notes' }
    ],
    fields: [
      { key: 'medicalRecordId', label: 'Medical record', type: 'select', options: 'records', required: true },
      { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors' },
      { key: 'diagnosis', label: 'Diagnosis', required: true },
      { key: 'notes', label: 'Notes', type: 'textarea' },
      { key: 'diagnosedAt', label: 'Date & time', type: 'datetime-local', required: true }
    ]
  },
  {
    key: 'doctors',
    endpoint: 'doctors',
    label: 'Care team',
    singular: 'doctor',
    icon: '⚕',
    roles: roleGroups.doctors,
    columns: [
      { label: 'Name', path: 'name' },
      { label: 'Department', path: 'department' },
      { label: 'Experience', path: 'experience' }
    ],
    fields: [
      { key: 'name', label: 'Full name', required: true },
      { key: 'department', label: 'Department', required: true },
      { key: 'experience', label: 'Years of experience', type: 'number', required: true }
    ],
    canCreate: false,
    canEdit: false,
    canDelete: false
  },
  {
    key: 'nurses',
    endpoint: 'nurses',
    label: 'Nursing team',
    singular: 'nurse',
    icon: '✚',
    roles: roleGroups.admin,
    columns: [
      { label: 'Name', path: 'name' },
      { label: 'Department', path: 'department' },
      { label: 'Phone', path: 'phoneNumber' }
    ],
    fields: [
      { key: 'name', label: 'Full name', required: true },
      { key: 'department', label: 'Department', required: true },
      { key: 'phoneNumber', label: 'Phone number', type: 'tel' }
    ]
  },
  {
    key: 'medications',
    endpoint: 'medications',
    label: 'Medication catalog',
    singular: 'medication',
    icon: '✣',
    roles: ['DOCTOR', 'ADMIN'],
    columns: [
      { label: 'Name', path: 'name' },
      { label: 'Strength', path: 'strength' },
      { label: 'Form', path: 'form' }
    ],
    fields: [
      { key: 'name', label: 'Medication name', required: true },
      { key: 'strength', label: 'Strength', required: true, placeholder: 'e.g. 250 mg' },
      { key: 'form', label: 'Form', required: true, placeholder: 'e.g. Tablet' }
    ]
  },
  {
    key: 'vitals',
    endpoint: 'vitals',
    label: 'Vitals',
    singular: 'vital record',
    icon: '♥',
    roles: ['PATIENT', 'DOCTOR', 'NURSE'],
    columns: [
      { label: 'Patient', path: 'patientName' },
      { label: 'Blood pressure', path: 'bloodPressure' },
      { label: 'Heart rate', path: 'heartRate' },
      { label: 'Temperature °C', path: 'temperature' },
      { label: 'Oxygen %', path: 'oxygenSaturation' },
      { label: 'Weight kg', path: 'weight' },
      { label: 'Recorded', path: 'recordedAt' }
    ],
    fields: [
      { key: 'patientId', label: 'Patient', type: 'select', options: 'patients', required: true },
      { key: 'bloodPressure', label: 'Blood pressure', placeholder: 'e.g. 120/80' },
      { key: 'heartRate', label: 'Heart rate (bpm)', type: 'number' },
      { key: 'temperature', label: 'Temperature (°C)', type: 'number' },
      { key: 'oxygenSaturation', label: 'Oxygen saturation (%)', type: 'number' },
      { key: 'weight', label: 'Weight (kg)', type: 'number' },
      { key: 'height', label: 'Height (cm)', type: 'number' },
      { key: 'recordedAt', label: 'Recorded date & time', type: 'datetime-local', required: true }
    ]
  },
  {
    key: 'users',
    endpoint: 'admins/users',
    label: 'User accounts',
    singular: 'user',
    icon: '♙',
    roles: roleGroups.admin,
    columns: [
      { label: 'Name', path: 'name' },
      { label: 'Email', path: 'email' },
      { label: 'Role', path: 'role' },
      { label: 'Profile ID', path: 'profileId' }
    ],
    fields: [],
    canCreate: false,
    canEdit: false,
    canDelete: false
  },
  {
    key: 'admins',
    endpoint: 'admins',
    label: 'Administrators',
    singular: 'administrator',
    icon: '⚙',
    roles: roleGroups.admin,
    columns: [
      { label: 'Name', path: 'name' },
      { label: 'Email', path: 'email' }
    ],
    fields: [
      { key: 'name', label: 'Full name', required: true },
      { key: 'email', label: 'Email address', type: 'email', required: true }
    ]
  },
  {
    key: 'audit-logs',
    endpoint: 'admins/audit-logs',
    label: 'Audit log',
    singular: 'audit event',
    icon: '▤',
    roles: roleGroups.admin,
    columns: [
      { label: 'Time', path: 'occurredAt' },
      { label: 'Actor ID', path: 'accountId' },
      { label: 'Role', path: 'actorRole' },
      { label: 'Action', path: 'action' },
      { label: 'Resource', path: 'resourceType' },
      { label: 'Resource ID', path: 'resourceId' },
      { label: 'Status', path: 'status' },
      { label: 'Correlation ID', path: 'correlationId' }
    ],
    fields: [],
    canCreate: false,
    canEdit: false,
    canDelete: false
  }
];

const translationKeys: Record<string, string> = {
  appointments: 'nav.appointments',
  patients: 'nav.patients',
  doctors: 'nav.doctors',
  nurses: 'nav.nurses',
  'medical-records': 'nav.records',
  diagnoses: 'nav.diagnoses',
  prescriptions: 'nav.prescriptions',
  'lab-reports': 'nav.labs',
  vitals: 'nav.vitals',
  medications: 'nav.medications',
  users: 'nav.users',
  admins: 'nav.admins',
  'audit-logs': 'nav.audit'
};

export function resourceTranslationKey(key: string): string {
  return translationKeys[key] || key;
}
