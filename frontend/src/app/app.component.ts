import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { forkJoin } from 'rxjs';
import { AuthService, Account, SignupPayload } from './auth.service';
import { TranslatePipe } from './i18n/translate.pipe';
import hi from '../assets/i18n/hi.json';

type AuthMode = 'login' | 'signup';
type Row = Record<string, any>;
type Field = { key: string; label: string; type?: string; required?: boolean; options?: string; placeholder?: string };
type Column = { label: string; path: string };
type Resource = { key: string; endpoint: string; label: string; singular: string; icon: string; roles: string[]; columns: Column[]; fields: Field[]; canCreate?: boolean; canEdit?: boolean; canDelete?: boolean };
type AssistantEntry = { role: 'user' | 'assistant'; content: string; urgent?: boolean };

export interface PatientDashboardData { patient: Row; appointments: Row[]; records: Row[]; prescriptions: Row[]; labReports: Row[]; careTeam: Row[]; diagnoses: Row[]; vitals: Row[]; notifications: Row[]; }
export interface DoctorDashboardData { todayAppointments: Row[]; upcomingAppointments: Row[]; patients: Row[]; records: Row[]; labReports: Row[]; prescriptions: Row[]; vitals: Row[]; notifications: Row[]; }
export interface NurseDashboardData { todayAppointments: Row[]; pendingTasks: Row[]; patients: Row[]; records: Row[]; labReports: Row[]; careTeam: Row[]; vitals: Row[]; notifications: Row[]; }
export interface AdminDashboardData { appointments: Row[]; patients: Row[]; doctors: Row[]; nurses: Row[]; notifications: Row[]; users: Row[]; }
export interface PatientProfileData { patient: Row; appointments: Row[]; records: Row[]; diagnoses: Row[]; prescriptions: Row[]; labReports: Row[]; vitals: Row[]; }

const navigationByRole: Record<string, string[]> = {
  PATIENT: ['appointments', 'medical-records', 'diagnoses', 'prescriptions', 'lab-reports', 'vitals', 'doctors'],
  DOCTOR: ['appointments', 'patients', 'medical-records', 'prescriptions', 'lab-reports', 'diagnoses', 'vitals', 'doctors', 'medications'],
  NURSE: ['appointments', 'patients', 'medical-records', 'lab-reports', 'vitals', 'doctors'],
  ADMIN: ['appointments', 'patients', 'doctors', 'nurses', 'medications', 'users', 'admins', 'audit-logs']
};

const roles = {
  all: ['PATIENT', 'DOCTOR', 'NURSE', 'ADMIN'],
  clinical: ['DOCTOR', 'NURSE', 'ADMIN'],
  doctors: ['PATIENT', 'DOCTOR', 'NURSE', 'ADMIN'],
  admin: ['ADMIN']
};

const resources: Resource[] = [
  { key: 'appointments', endpoint: 'appointments', label: 'Appointments', singular: 'appointment', icon: '◷', roles: roles.all, columns: [{ label: 'Patient', path: 'patientName' }, { label: 'Doctor', path: 'doctorName' }, { label: 'Date & time', path: 'appointmentDateTime' }, { label: 'Status', path: 'status' }, { label: 'Reason', path: 'reason' }], fields: [{ key: 'patientId', label: 'Patient', type: 'select', options: 'patients', required: true }, { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors', required: true }, { key: 'appointmentDateTime', label: 'Date & time', type: 'datetime-local', required: true }, { key: 'status', label: 'Status', type: 'select', options: 'appointmentStatus', required: true }, { key: 'reason', label: 'Reason', required: true, placeholder: 'Reason for visit' }] },
  { key: 'patients', endpoint: 'patients', label: 'Patients', singular: 'patient', icon: '♡', roles: [...roles.clinical, 'ADMIN'], columns: [{ label: 'Name', path: 'name' }, { label: 'Age', path: 'age' }, { label: 'Gender', path: 'gender' }, { label: 'Health concern', path: 'disease' }, { label: 'Blood group', path: 'bloodGroup' }, { label: 'Phone', path: 'phone' }], fields: [{ key: 'name', label: 'Full name', required: true }, { key: 'age', label: 'Age', type: 'number', required: true }, { key: 'disease', label: 'Health concern', required: true }, { key: 'gender', label: 'Gender' }, { key: 'email', label: 'Email', type: 'email' }, { key: 'phone', label: 'Phone', type: 'tel' }, { key: 'bloodGroup', label: 'Blood group' }, { key: 'address', label: 'Address' }, { key: 'emergencyContact', label: 'Emergency contact' }] },
  { key: 'medical-records', endpoint: 'medical-records', label: 'Medical records', singular: 'medical record', icon: '▤', roles: ['PATIENT', 'DOCTOR', 'NURSE'], columns: [{ label: 'Patient', path: 'patientName' }, { label: 'Diagnosis', path: 'diagnosis' }, { label: 'Treatment', path: 'treatment' }, { label: 'Recorded', path: 'recordedAt' }], fields: [{ key: 'patientId', label: 'Patient', type: 'select', options: 'patients', required: true }, { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors' }, { key: 'diagnosis', label: 'Diagnosis', required: true }, { key: 'treatment', label: 'Treatment plan', required: true }, { key: 'notes', label: 'Clinical notes', type: 'textarea' }, { key: 'recordedAt', label: 'Date & time', type: 'datetime-local', required: true }] },
  { key: 'prescriptions', endpoint: 'prescriptions', label: 'Prescriptions', singular: 'prescription', icon: '✚', roles: ['PATIENT', 'DOCTOR'], columns: [{ label: 'Medication', path: 'medicationName' }, { label: 'Dosage', path: 'dosage' }, { label: 'Frequency', path: 'frequency' }, { label: 'Duration', path: 'duration' }, { label: 'Status', path: 'status' }, { label: 'Prescribed', path: 'prescribedDate' }], fields: [{ key: 'medicalRecordId', label: 'Medical record', type: 'select', options: 'records', required: true }, { key: 'doctorId', label: 'Prescribing doctor', type: 'select', options: 'doctors' }, { key: 'medicationId', label: 'Medication', type: 'select', options: 'medications', required: true }, { key: 'dosage', label: 'Dosage', required: true, placeholder: 'e.g. 500 mg' }, { key: 'frequency', label: 'Frequency', required: true, placeholder: 'e.g. Twice daily' }, { key: 'duration', label: 'Duration', required: true, placeholder: 'e.g. 7 days' }, { key: 'status', label: 'Status', type: 'select', options: 'prescriptionStatus', required: true }, { key: 'instructions', label: 'Instructions', type: 'textarea' }, { key: 'prescribedDate', label: 'Prescribed date', type: 'date', required: true }] },
  { key: 'lab-reports', endpoint: 'lab-reports', label: 'Lab reports', singular: 'lab report', icon: '⌁', roles: ['PATIENT', 'DOCTOR', 'NURSE'], columns: [{ label: 'Patient', path: 'patientName' }, { label: 'Test', path: 'testName' }, { label: 'Result', path: 'result' }, { label: 'Reference range', path: 'referenceRange' }, { label: 'Status', path: 'status' }, { label: 'Report date', path: 'reportDate' }], fields: [{ key: 'medicalRecordId', label: 'Medical record', type: 'select', options: 'records', required: true }, { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors' }, { key: 'testName', label: 'Test name', required: true }, { key: 'result', label: 'Result', required: true }, { key: 'referenceRange', label: 'Reference range' }, { key: 'status', label: 'Status', type: 'select', options: 'labStatus', required: true }, { key: 'reportDate', label: 'Report date', type: 'date', required: true }] },
  { key: 'diagnoses', endpoint: 'diagnoses', label: 'Diagnoses', singular: 'diagnosis', icon: '⊕', roles: ['PATIENT', 'DOCTOR'], columns: [{ label: 'Patient', path: 'patientName' }, { label: 'Diagnosis', path: 'diagnosis' }, { label: 'Doctor', path: 'doctorName' }, { label: 'Date', path: 'diagnosedAt' }, { label: 'Notes', path: 'notes' }], fields: [{ key: 'medicalRecordId', label: 'Medical record', type: 'select', options: 'records', required: true }, { key: 'doctorId', label: 'Doctor', type: 'select', options: 'doctors' }, { key: 'diagnosis', label: 'Diagnosis', required: true }, { key: 'notes', label: 'Notes', type: 'textarea' }, { key: 'diagnosedAt', label: 'Date & time', type: 'datetime-local', required: true }] },
  { key: 'doctors', endpoint: 'doctors', label: 'Care team', singular: 'doctor', icon: '⚕', roles: roles.doctors, columns: [{ label: 'Name', path: 'name' }, { label: 'Department', path: 'department' }, { label: 'Experience', path: 'experience' }], fields: [{ key: 'name', label: 'Full name', required: true }, { key: 'department', label: 'Department', required: true }, { key: 'experience', label: 'Years of experience', type: 'number', required: true }], canCreate: false, canEdit: false, canDelete: false },
  { key: 'nurses', endpoint: 'nurses', label: 'Nursing team', singular: 'nurse', icon: '✚', roles: roles.admin, columns: [{ label: 'Name', path: 'name' }, { label: 'Department', path: 'department' }, { label: 'Phone', path: 'phoneNumber' }], fields: [{ key: 'name', label: 'Full name', required: true }, { key: 'department', label: 'Department', required: true }, { key: 'phoneNumber', label: 'Phone number', type: 'tel' }] },
  { key: 'medications', endpoint: 'medications', label: 'Medication catalog', singular: 'medication', icon: '✣', roles: ['DOCTOR', 'ADMIN'], columns: [{ label: 'Name', path: 'name' }, { label: 'Strength', path: 'strength' }, { label: 'Form', path: 'form' }], fields: [{ key: 'name', label: 'Medication name', required: true }, { key: 'strength', label: 'Strength', required: true, placeholder: 'e.g. 250 mg' }, { key: 'form', label: 'Form', required: true, placeholder: 'e.g. Tablet' }] },
  { key: 'vitals', endpoint: 'vitals', label: 'Vitals', singular: 'vital record', icon: '♥', roles: ['PATIENT', 'DOCTOR', 'NURSE'], columns: [{ label: 'Patient', path: 'patientName' }, { label: 'Blood pressure', path: 'bloodPressure' }, { label: 'Heart rate', path: 'heartRate' }, { label: 'Temperature °C', path: 'temperature' }, { label: 'Oxygen %', path: 'oxygenSaturation' }, { label: 'Weight kg', path: 'weight' }, { label: 'Recorded', path: 'recordedAt' }], fields: [{ key: 'patientId', label: 'Patient', type: 'select', options: 'patients', required: true }, { key: 'bloodPressure', label: 'Blood pressure', placeholder: 'e.g. 120/80' }, { key: 'heartRate', label: 'Heart rate (bpm)', type: 'number' }, { key: 'temperature', label: 'Temperature (°C)', type: 'number' }, { key: 'oxygenSaturation', label: 'Oxygen saturation (%)', type: 'number' }, { key: 'weight', label: 'Weight (kg)', type: 'number' }, { key: 'height', label: 'Height (cm)', type: 'number' }, { key: 'recordedAt', label: 'Recorded date & time', type: 'datetime-local', required: true }] },
  { key: 'users', endpoint: 'admins/users', label: 'User accounts', singular: 'user', icon: '♙', roles: roles.admin, columns: [{ label: 'Name', path: 'name' }, { label: 'Email', path: 'email' }, { label: 'Role', path: 'role' }, { label: 'Profile ID', path: 'profileId' }], fields: [], canCreate: false, canEdit: false, canDelete: false },
  { key: 'admins', endpoint: 'admins', label: 'Administrators', singular: 'administrator', icon: '⚙', roles: roles.admin, columns: [{ label: 'Name', path: 'name' }, { label: 'Email', path: 'email' }], fields: [{ key: 'name', label: 'Full name', required: true }, { key: 'email', label: 'Email address', type: 'email', required: true }] },
  { key: 'audit-logs', endpoint: 'admins/audit-logs', label: 'Audit log', singular: 'audit event', icon: '▤', roles: roles.admin, columns: [{ label: 'Time', path: 'occurredAt' }, { label: 'Actor ID', path: 'accountId' }, { label: 'Role', path: 'actorRole' }, { label: 'Action', path: 'action' }, { label: 'Resource', path: 'resourceType' }, { label: 'Resource ID', path: 'resourceId' }, { label: 'Status', path: 'status' }, { label: 'Correlation ID', path: 'correlationId' }], fields: [], canCreate: false, canEdit: false, canDelete: false }
];

@Component({ selector: 'app-root', imports: [FormsModule, DatePipe, TranslatePipe], templateUrl: './app.component.html', styleUrl: './app.component.css' })
export class AppComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);
  readonly resources = resources;
  readonly statuses = ['Scheduled', 'Confirmed', 'Completed', 'Cancelled'];
  readonly prescriptionStatuses = ['ACTIVE', 'COMPLETED'];
  readonly labStatuses = ['NORMAL', 'ABNORMAL', 'PENDING'];
  readonly genderOptions = ['Female', 'Male', 'Non-binary', 'Prefer not to say'];
  mode: AuthMode = 'login';
  authOpen = false;
  account: Account | null = null;
  loading = false;
  pageLoading = false;
  saving = false;
  notice = '';
  noticeKind: 'error' | 'success' = 'error';
  loginData = { email: '', password: '' };
  signupData: SignupPayload = { role: 'PATIENT', name: '', email: '', password: '' };
  activeKey = 'overview';
  rows: Row[] = [];
  patientDashboard: PatientDashboardData | null = null;
  doctorDashboard: DoctorDashboardData | null = null;
  nurseDashboard: NurseDashboardData | null = null;
  adminDashboard: AdminDashboardData | null = null;
  lookups: Record<string, Row[]> = {};
  search = '';
  showForm = false;
  editingId: number | null = null;
  formData: Row = {};
  assignmentPatient: Row | null = null;
  assignmentNurses: Row[] = [];
  assignedNurses: Row[] = [];
  assignmentLoading = false;
  patientProfile: PatientProfileData | null = null;
  patientProfileLoading = false;
  patientContextId: number | null = null;
  vitalRange: 'week' | 'month' = 'month';
  vitalMetric: 'bloodPressureSystolic' | 'bloodPressureDiastolic' | 'heartRate' | 'temperature' | 'oxygenSaturation' | 'weight' | 'height' = 'heartRate';
  assistantLanguage: 'en' | 'hi' = typeof localStorage === 'undefined' ? 'en' : (localStorage.getItem('careconnect.language') === 'hi' ? 'hi' : 'en');
  assistantMessages: AssistantEntry[] = [];
  assistantText = '';
  assistantSending = false;
  assistantError = '';
  private lastAssistantRequest: { message: string; language: string; history: { role: string; content: string }[] } | null = null;
  noteAppointment: Row | null = null;
  noteLoading = false;
  noteSaving = false;
  consultationNote: Row = { chiefComplaint: '', history: '', examination: '', assessment: '', diagnosis: '', plan: '', followUp: '', followUpDate: null };
  noteNotice = '';
  noteVersions: Row[] = [];
  noteVersionsLoading = false;
  videoAppointment: Row | null = null;
  videoJoinUrl: SafeResourceUrl | null = null;
  videoError = '';
  videoLoading = false;

  get initials(): string { return this.account?.name.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'CC'; }
  get firstName(): string { return this.account?.name.trim().split(/\s+/)[0] || 'there'; }
  get today(): Date { return new Date(); }
  get greeting(): string { const hour = new Date().getHours(); return hour < 12 ? 'morning' : hour < 18 ? 'afternoon' : 'evening'; }
  get greetingTranslationKey(): string { return `dashboard.hello${this.greeting[0].toUpperCase()}${this.greeting.slice(1)}`; }
  resourceTranslationKey(key: string): string {
    const translationKeys: Record<string, string> = { appointments: 'nav.appointments', patients: 'nav.patients', doctors: 'nav.doctors', nurses: 'nav.nurses', 'medical-records': 'nav.records', diagnoses: 'nav.diagnoses', prescriptions: 'nav.prescriptions', 'lab-reports': 'nav.labs', vitals: 'nav.vitals', medications: 'nav.medications', users: 'nav.users', admins: 'nav.admins', 'audit-logs': 'nav.audit' };
    return translationKeys[key] || key;
  }
  get visibleResources(): Resource[] {
    const keys = navigationByRole[this.account?.role.toUpperCase() || ''] || [];
    return keys.map((key) => resources.find((item) => item.key === key)).filter((item): item is Resource => !!item);
  }
  get activeResource(): Resource | undefined { return resources.find((item) => item.key === this.activeKey); }
  get visibleRows(): Row[] {
    const query = this.search.trim().toLowerCase();
    if (!query) return this.rows;
    return this.rows.filter((row) => this.activeResource?.columns.some((column) => this.displayValue(row, column.path).toLowerCase().includes(query)));
  }
  get greetingName(): string { return this.account?.role === 'DOCTOR' ? (this.account.name.startsWith('Dr.') ? this.account.name : `Dr. ${this.account.name}`) : this.firstName; }
  get activePrescriptions(): Row[] { return (this.patientDashboard?.prescriptions || []).filter((row) => String(row['status'] || '').toUpperCase() === 'ACTIVE'); }
  get abnormalReports(): Row[] { return (this.patientDashboard?.labReports || []).filter((row) => String(row['status'] || '').toUpperCase() === 'ABNORMAL'); }
  get doctorPendingLabs(): Row[] { return (this.doctorDashboard?.labReports || []).filter((row) => String(row['status'] || '').toUpperCase() === 'PENDING'); }
  get heartRateTrend(): { value: number; date: Date }[] {
    const now = new Date();
    const cutoff = new Date(now);
    cutoff.setDate(now.getDate() - (this.vitalRange === 'week' ? 7 : 30));
    return (this.patientDashboard?.vitals || []).map((row) => ({ value: Number(row['heartRate']), date: new Date(String(row['recordedAt'])) }))
      .filter((item) => Number.isFinite(item.value) && !Number.isNaN(item.date.getTime()) && item.date >= cutoff)
      .sort((a, b) => a.date.getTime() - b.date.getTime()).slice(-14);
  }
  get vitalTrendLabel(): string {
    return ({ bloodPressureSystolic: 'Blood pressure · systolic mmHg', bloodPressureDiastolic: 'Blood pressure · diastolic mmHg', heartRate: 'Heart rate · bpm', temperature: 'Temperature · °C', oxygenSaturation: 'Oxygen saturation · %', weight: 'Weight · kg', height: 'Height · cm' } as const)[this.vitalMetric];
  }
  private vitalValue(row: Row): number {
    if (this.vitalMetric === 'bloodPressureSystolic' || this.vitalMetric === 'bloodPressureDiastolic') {
      const values = String(row['bloodPressure'] || '').match(/^\s*(\d+(?:\.\d+)?)\s*\/\s*(\d+(?:\.\d+)?)\s*$/);
      return values ? Number(values[this.vitalMetric === 'bloodPressureSystolic' ? 1 : 2]) : Number.NaN;
    }
    return Number(row[this.vitalMetric]);
  }
  get vitalTrend(): { value: number; date: Date }[] {
    const now = new Date();
    const days = this.vitalRange === 'week' ? 7 : 30;
    const cutoff = new Date(now); cutoff.setDate(now.getDate() - days);
    return (this.patientDashboard?.vitals || []).map((row) => ({ value: this.vitalValue(row), date: new Date(String(row['recordedAt'])) }))
      .filter((item) => Number.isFinite(item.value) && !Number.isNaN(item.date.getTime()) && item.date >= cutoff)
      .sort((a, b) => a.date.getTime() - b.date.getTime()).slice(-30);
  }
  get previousVitalTrend(): { value: number; date: Date }[] {
    const now = new Date(); const days = this.vitalRange === 'week' ? 7 : 30;
    const currentStart = new Date(now); currentStart.setDate(now.getDate() - days);
    const previousStart = new Date(currentStart); previousStart.setDate(currentStart.getDate() - days);
    return (this.patientDashboard?.vitals || []).map((row) => ({ value: this.vitalValue(row), date: new Date(String(row['recordedAt'])) }))
      .filter((item) => Number.isFinite(item.value) && !Number.isNaN(item.date.getTime()) && item.date >= previousStart && item.date < currentStart);
  }
  get vitalTrendComparison(): string {
    const current = this.vitalTrend; const previous = this.previousVitalTrend;
    if (!current.length || !previous.length) return 'No prior-period measurements to compare';
    const average = (values: { value: number }[]) => values.reduce((sum, item) => sum + item.value, 0) / values.length;
    const delta = average(current) - average(previous);
    return `${delta > 0 ? '+' : ''}${delta.toFixed(1)} vs previous ${this.vitalRange === 'week' ? 'week' : '30 days'}`;
  }
  get vitalTrendPoints(): string {
    const values = this.vitalTrend;
    if (!values.length) return '';
    const min = Math.min(...values.map((item) => item.value)); const max = Math.max(...values.map((item) => item.value));
    const spread = Math.max(1, max - min);
    return values.map((item, index) => `${values.length === 1 ? 50 : 5 + index * 90 / (values.length - 1)},${90 - (item.value - min) * 75 / spread}`).join(' ');
  }
  get heartRateTrendPoints(): string {
    const values = this.heartRateTrend;
    if (!values.length) return '';
    const min = Math.min(...values.map((item) => item.value));
    const max = Math.max(...values.map((item) => item.value));
    const spread = Math.max(1, max - min);
    return values.map((item, index) => `${values.length === 1 ? 50 : 5 + index * 90 / (values.length - 1)},${90 - (item.value - min) * 75 / spread}`).join(' ');
  }
  get adminAppointmentsToday(): Row[] { return (this.adminDashboard?.appointments || []).filter((row) => this.isToday(row['appointmentDateTime'])); }
  get adminDepartmentStats(): { label: string; value: number }[] {
    const counts = new Map<string, number>();
    for (const doctor of this.adminDashboard?.doctors || []) {
      const department = String(doctor['department'] || 'Unassigned');
      counts.set(department, (counts.get(department) || 0) + 1);
    }
    return [...counts.entries()].map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value);
  }
  get maxDepartmentCount(): number { return this.adminDepartmentStats[0]?.value || 0; }
  get adminAppointmentStatusStats(): { label: string; value: number }[] {
    const counts = new Map<string, number>();
    for (const appointment of this.adminDashboard?.appointments || []) {
      const status = String(appointment['status'] || 'Unspecified');
      counts.set(status, (counts.get(status) || 0) + 1);
    }
    return [...counts.entries()].map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value);
  }
  barPercent(value: number, max: number): number { return max > 0 ? Math.max(6, Math.round(value / max * 100)) : 0; }
  get upcomingAppointments(): Row[] { return this.sortRows(this.appointmentsForRole().filter((row) => this.isUpcoming(row['appointmentDateTime']) && !this.isCancelled(row['status'])), 'appointmentDateTime').slice(0, 4); }
  get todayAppointments(): Row[] { return this.sortRows(this.appointmentsForRole().filter((row) => this.isToday(row['appointmentDateTime'])), 'appointmentDateTime'); }
  get recentRecords(): Row[] { return this.sortRows(this.recordsForRole(), 'recordedAt').reverse().slice(0, 4); }
  get recentReports(): Row[] { return this.sortRows(this.reportsForRole(), 'reportDate').reverse().slice(0, 4); }

  ngOnInit(): void { this.auth.me().subscribe({ next: (account) => { this.account = account; this.loadDashboard(); }, error: () => undefined }); }
  setMode(mode: AuthMode): void { this.mode = mode; this.authOpen = true; this.notice = ''; }
  closeAuth(): void { this.authOpen = false; this.notice = ''; }
  login(): void { this.submit(() => this.auth.login(this.loginData)); }
  signup(): void { const payload = { ...this.signupData }; if (payload.age == null) delete payload.age; if (payload.experience == null) delete payload.experience; this.submit(() => this.auth.signup(payload)); }
  logout(): void { this.loading = true; this.auth.logout().subscribe({ next: () => this.finishLogout(), error: () => this.finishLogout() }); }
  setAssistantLanguage(language: 'en' | 'hi'): void {
    this.assistantLanguage = language;
    if (typeof localStorage !== 'undefined') localStorage.setItem('careconnect.language', language);
  }
  sendAssistant(suggested?: string): void {
    const message = (suggested ?? this.assistantText).trim();
    if (!message || this.assistantSending) return;
    const history = this.assistantMessages.slice(-12).map(({ role, content }) => ({ role, content }));
    this.assistantMessages = [...this.assistantMessages, { role: 'user', content: message }];
    this.assistantText = '';
    this.assistantError = '';
    this.lastAssistantRequest = { message, language: this.assistantLanguage, history };
    this.submitAssistant(this.lastAssistantRequest);
  }
  retryAssistant(): void {
    if (this.lastAssistantRequest && !this.assistantSending) {
      this.assistantError = '';
      this.submitAssistant(this.lastAssistantRequest);
    }
  }
  clearAssistant(): void { this.assistantMessages = []; this.assistantError = ''; this.lastAssistantRequest = null; }
  openConsultationNote(appointment: Row): void {
    const id = Number(appointment['id']);
    if (!id) return;
    this.noteAppointment = appointment; this.noteLoading = true; this.noteNotice = '';
    this.noteVersions = []; this.noteVersionsLoading = true;
    this.consultationNote = { chiefComplaint: '', history: '', examination: '', assessment: '', diagnosis: '', plan: '', followUp: '', followUpDate: null };
    this.auth.consultationNote<Row>(id).subscribe({
      next: (note) => { this.consultationNote = { ...note }; this.noteLoading = false; this.loadConsultationNoteVersions(id); },
      error: (error: HttpErrorResponse) => {
        this.noteLoading = false;
        this.noteVersionsLoading = false;
        if (error.status === 404) return;
        this.noteNotice = this.localizedError(error, 'Could not load this consultation note.');
      }
    });
  }
  saveConsultationNote(): void {
    if (!this.noteAppointment || this.noteSaving) return;
    this.noteSaving = true; this.noteNotice = '';
    this.auth.saveConsultationNote<Row>(Number(this.noteAppointment['id']), this.consultationNote).subscribe({
      next: (note) => { this.consultationNote = { ...note }; this.noteSaving = false; this.noteNotice = 'Consultation note saved.'; this.loadConsultationNoteVersions(Number(this.noteAppointment?.['id'])); },
      error: (error: HttpErrorResponse) => { this.noteSaving = false; this.noteNotice = this.localizedError(error, 'Could not save the consultation note.'); }
    });
  }
  private loadConsultationNoteVersions(appointmentId: number): void {
    if (!appointmentId) { this.noteVersionsLoading = false; return; }
    this.noteVersionsLoading = true;
    this.auth.consultationNoteVersions<Row>(appointmentId).subscribe({
      next: (versions) => { this.noteVersions = versions; this.noteVersionsLoading = false; },
      error: () => { this.noteVersions = []; this.noteVersionsLoading = false; }
    });
  }
  closeConsultationNote(): void { if (!this.noteSaving) this.noteAppointment = null; }
  joinVideoConsultation(appointment: Row): void {
    if (this.videoLoading) return;
    this.videoAppointment = appointment; this.videoJoinUrl = null; this.videoError = ''; this.videoLoading = true;
    this.auth.joinVideoConsultation(Number(appointment['id'])).subscribe({
      next: (result) => { this.videoJoinUrl = this.sanitizer.bypassSecurityTrustResourceUrl(result.joinUrl); this.videoLoading = false; },
      error: (error: HttpErrorResponse) => { this.videoError = this.localizedError(error, 'Could not join this video consultation.'); this.videoLoading = false; }
    });
  }
  closeVideoConsultation(): void { this.videoAppointment = null; this.videoJoinUrl = null; this.videoError = ''; }
  videoEligible(appointment: Row): boolean {
    const time = new Date(String(appointment['appointmentDateTime'])).getTime();
    const now = Date.now();
    return !!appointment['id'] && String(appointment['status'] || '').toUpperCase() !== 'CANCELLED'
      && Number.isFinite(time) && now >= time - 15 * 60_000 && now <= time + 120 * 60_000;
  }
  private submitAssistant(payload: { message: string; language: string; history: { role: string; content: string }[] }): void {
    this.assistantSending = true;
    this.auth.assistantChat(payload).subscribe({
      next: (response) => { this.assistantMessages = [...this.assistantMessages, { role: 'assistant', content: `${response.reply}\n\n${response.safetyNotice}`, urgent: response.urgent }]; this.assistantSending = false; },
      error: (error: HttpErrorResponse) => { this.assistantError = this.localizedError(error, 'The assistant is unavailable right now. Try again.'); this.assistantSending = false; }
    });
  }

  selectPage(key: string, patientId?: number): void {
    this.patientContextId = patientId ?? null;
    this.activeKey = key; this.search = ''; this.notice = ''; this.showForm = false;
    if (key === 'overview') this.loadDashboard(); else this.loadResource();
  }

  canCreate(resource: Resource): boolean {
    if (resource.canCreate === false) return false;
    const role = this.account?.role.toUpperCase();
    if (role === 'ADMIN') return true;
    if (role === 'PATIENT') return resource.key === 'appointments';
    return (role === 'DOCTOR' && ['appointments', 'medical-records', 'prescriptions', 'lab-reports', 'diagnoses', 'vitals'].includes(resource.key)) || (role === 'NURSE' && resource.key === 'vitals');
  }

  canEdit(resource: Resource): boolean {
    if (resource.canEdit === false) return false;
    const role = this.account?.role.toUpperCase();
    if (role === 'ADMIN') return resource.key !== 'doctors' && resource.key !== 'vitals'; // Vitals are append-only clinical observations.
    return role === 'DOCTOR' && ['appointments', 'medical-records', 'prescriptions', 'lab-reports', 'diagnoses'].includes(resource.key);
  }

  canDelete(resource: Resource): boolean { return this.account?.role.toUpperCase() === 'ADMIN' && resource.canDelete !== false && resource.key !== 'vitals'; }

  createOn(key: string, patientId?: number): void { this.selectPage(key, patientId); this.createNew(); }

  openPatientDetails(patient: Row): void {
    const patientId = Number(patient['id']);
    if (!patientId) return;
    this.patientProfile = null;
    this.patientProfileLoading = true;
    const diagnoses = this.auth.list<Row>('diagnoses');
    const prescriptions = this.auth.list<Row>('prescriptions');
    forkJoin({ patient: this.auth.get<Row>('patients', patientId), appointments: this.auth.list<Row>('appointments'), records: this.auth.list<Row>('medical-records'), diagnoses, prescriptions, labReports: this.auth.list<Row>('lab-reports'), vitals: this.auth.list<Row>('vitals') }).subscribe({
      next: (data) => {
        const belongs = (row: Row) => Number(row['patientId'] ?? row['patient']?.['id']) === patientId;
        this.patientProfile = { patient: data.patient, appointments: this.sortRows(data.appointments.filter(belongs), 'appointmentDateTime').reverse(), records: this.sortRows(data.records.filter(belongs), 'recordedAt').reverse(), diagnoses: this.sortRows(data.diagnoses.filter(belongs), 'diagnosedAt').reverse(), prescriptions: this.sortRows(data.prescriptions.filter(belongs), 'prescribedDate').reverse(), labReports: this.sortRows(data.labReports.filter(belongs), 'reportDate').reverse(), vitals: this.sortRows(data.vitals.filter(belongs), 'recordedAt').reverse() };
        this.patientProfileLoading = false;
      },
      error: (error: HttpErrorResponse) => { this.patientProfileLoading = false; this.setError(error, 'Could not load this patient profile.'); }
    });
  }

  private appointmentsForRole(): Row[] {
    return this.patientDashboard?.appointments || this.doctorDashboard?.upcomingAppointments.concat(this.doctorDashboard.todayAppointments) || this.nurseDashboard?.todayAppointments || this.adminDashboard?.appointments || [];
  }
  private recordsForRole(): Row[] { return this.patientDashboard?.records || this.doctorDashboard?.records || this.nurseDashboard?.records || []; }
  private reportsForRole(): Row[] { return this.patientDashboard?.labReports || this.doctorDashboard?.labReports || this.nurseDashboard?.labReports || []; }
  private isToday(value: unknown): boolean { const date = value ? new Date(String(value)) : null; return !!date && !Number.isNaN(date.getTime()) && date.toDateString() === new Date().toDateString(); }
  private isUpcoming(value: unknown): boolean { const date = value ? new Date(String(value)) : null; const today = new Date(); today.setHours(0, 0, 0, 0); return !!date && !Number.isNaN(date.getTime()) && date >= today; }
  private isCancelled(status: unknown): boolean { return String(status || '').toLowerCase() === 'cancelled'; }
  private sortRows(rows: Row[], path: string): Row[] { return [...rows].sort((left, right) => new Date(String(left[path] || 0)).getTime() - new Date(String(right[path] || 0)).getTime()); }

  loadDashboard(): void {
    this.pageLoading = true;
    this.patientDashboard = null; this.doctorDashboard = null; this.nurseDashboard = null; this.adminDashboard = null;
    switch (this.account?.role.toUpperCase()) {
      case 'PATIENT': this.loadPatientDashboard(); break;
      case 'DOCTOR': this.loadDoctorDashboard(); break;
      case 'NURSE': this.loadNurseDashboard(); break;
      case 'ADMIN': this.loadAdminDashboard(); break;
      default: this.pageLoading = false; this.setError(new HttpErrorResponse({ status: 403 }), 'Your role does not have a dashboard.');
    }
  }

  private loadPatientDashboard(): void {
    const profileId = this.account?.profileId;
    if (!profileId) { this.pageLoading = false; this.setError(new HttpErrorResponse({ status: 404 }), 'Your patient profile is not linked to this account.'); return; }
    forkJoin({ patient: this.auth.get<Row>('patients', profileId), appointments: this.auth.list<Row>('appointments'), records: this.auth.list<Row>('medical-records'), prescriptions: this.auth.list<Row>('prescriptions'), labReports: this.auth.list<Row>('lab-reports'), careTeam: this.auth.list<Row>('doctors'), diagnoses: this.auth.list<Row>('diagnoses'), vitals: this.auth.list<Row>('vitals'), notifications: this.auth.list<Row>('notifications') }).subscribe({
      next: (data) => { this.patientDashboard = { ...data, appointments: this.sortRows(data.appointments, 'appointmentDateTime'), vitals: this.sortRows(data.vitals, 'recordedAt').reverse() }; this.pageLoading = false; },
      error: (error: HttpErrorResponse) => this.dashboardError(error)
    });
  }

  private loadDoctorDashboard(): void {
    forkJoin({ appointments: this.auth.list<Row>('appointments'), patients: this.auth.list<Row>('patients'), records: this.auth.list<Row>('medical-records'), labReports: this.auth.list<Row>('lab-reports'), prescriptions: this.auth.list<Row>('prescriptions'), vitals: this.auth.list<Row>('vitals'), notifications: this.auth.list<Row>('notifications') }).subscribe({
      next: (data) => { this.doctorDashboard = { todayAppointments: this.sortRows(data.appointments.filter((row) => this.isToday(row['appointmentDateTime'])), 'appointmentDateTime'), upcomingAppointments: this.sortRows(data.appointments.filter((row) => this.isUpcoming(row['appointmentDateTime'])), 'appointmentDateTime'), patients: data.patients, records: this.sortRows(data.records, 'recordedAt').reverse(), labReports: this.sortRows(data.labReports, 'reportDate').reverse(), prescriptions: data.prescriptions, vitals: data.vitals, notifications: data.notifications }; this.pageLoading = false; },
      error: (error: HttpErrorResponse) => this.dashboardError(error)
    });
  }

  private loadNurseDashboard(): void {
    forkJoin({ appointments: this.auth.list<Row>('appointments'), patients: this.auth.list<Row>('patients'), records: this.auth.list<Row>('medical-records'), labReports: this.auth.list<Row>('lab-reports'), careTeam: this.auth.list<Row>('doctors'), vitals: this.auth.list<Row>('vitals'), notifications: this.auth.list<Row>('notifications') }).subscribe({
      next: (data) => { this.nurseDashboard = { todayAppointments: this.sortRows(data.appointments.filter((row) => this.isToday(row['appointmentDateTime'])), 'appointmentDateTime'), pendingTasks: data.appointments.filter((row) => ['scheduled', 'confirmed'].includes(String(row['status'] || '').toLowerCase()) && this.isUpcoming(row['appointmentDateTime'])), patients: data.patients, records: this.sortRows(data.records, 'recordedAt').reverse(), labReports: this.sortRows(data.labReports, 'reportDate').reverse(), careTeam: data.careTeam, vitals: data.vitals, notifications: data.notifications }; this.pageLoading = false; },
      error: (error: HttpErrorResponse) => this.dashboardError(error)
    });
  }

  private loadAdminDashboard(): void {
    forkJoin({ appointments: this.auth.list<Row>('appointments'), patients: this.auth.list<Row>('patients'), doctors: this.auth.list<Row>('doctors'), nurses: this.auth.list<Row>('nurses'), notifications: this.auth.list<Row>('notifications'), users: this.auth.list<Row>('admins/users') }).subscribe({
      next: (data) => { this.adminDashboard = { ...data, appointments: this.sortRows(data.appointments, 'appointmentDateTime').reverse() }; this.pageLoading = false; },
      error: (error: HttpErrorResponse) => this.dashboardError(error)
    });
  }

  private dashboardError(error: HttpErrorResponse): void { this.pageLoading = false; this.setError(error, 'Could not load your role-specific dashboard. Check the backend and database connection.'); }

  loadResource(): void {
    const resource = this.activeResource;
    if (!resource) return;
    this.pageLoading = true;
    const lookupCalls: Record<string, any> = { rows: this.auth.list<Row>(resource.endpoint) };
    // Read-only roles should not fetch lookup data used only by edit/create forms.
    // For example, patients can read their prescriptions but cannot browse medication catalogs.
    if (this.canCreate(resource) || this.canEdit(resource)) {
      const requiredLookups = new Set(resource.fields.map((field) => field.options).filter((value): value is string => !!value));
      if (requiredLookups.has('patients')) lookupCalls['patients'] = this.auth.list<Row>('patients');
      if (requiredLookups.has('doctors')) lookupCalls['doctors'] = this.auth.list<Row>('doctors/directory');
      if (requiredLookups.has('records')) lookupCalls['records'] = this.auth.list<Row>('medical-records');
      if (requiredLookups.has('medications')) lookupCalls['medications'] = this.auth.list<Row>('medications');
    }
    forkJoin(lookupCalls).subscribe({
      next: (result: any) => { this.rows = result['rows'] as Row[]; this.lookups = { patients: result['patients'] || [], doctors: result['doctors'] || [], records: result['records'] || [], medications: result['medications'] || [] }; this.pageLoading = false; },
      error: (error: HttpErrorResponse) => { this.rows = []; this.pageLoading = false; this.setError(error, `Could not load ${resource.label.toLowerCase()}.`); }
    });
  }

  createNew(): void {
    const resource = this.activeResource;
    if (!resource || !this.canCreate(resource)) return;
    this.editingId = null; this.formData = {};
    for (const field of resource.fields) {
      if (field.key === 'status') this.formData[field.key] = 'Scheduled';
      if (field.type === 'date') this.formData[field.key] = new Date().toISOString().slice(0, 10);
      if (field.type === 'datetime-local') this.formData[field.key] = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16);
    }
    if (this.activeKey === 'appointments' && this.account?.role === 'PATIENT' && this.account.profileId) this.formData['patientId'] = this.account.profileId;
    if (this.patientContextId && ['appointments', 'medical-records', 'vitals'].includes(this.activeKey)) this.formData['patientId'] = this.patientContextId;
    if (this.patientContextId && ['diagnoses', 'prescriptions', 'lab-reports'].includes(this.activeKey)) {
      const profile = this.patientProfile;
      const latestRecord = profile?.records[0];
      if (latestRecord) this.formData['medicalRecordId'] = latestRecord['id'];
    }
    this.showForm = true; this.notice = '';
  }

  editRow(row: Row): void {
    const resource = this.activeResource;
    if (!resource || !this.canEdit(resource)) return;
    this.editingId = Number(row['id']);
    this.formData = { ...row, patientId: row['patientId'] ?? row['patient']?.['id'], doctorId: row['doctorId'] ?? row['doctor']?.['docId'], medicalRecordId: row['medicalRecordId'] ?? row['medicalRecord']?.['id'], medicationId: row['medicationId'] ?? row['medication']?.['id'] };
    this.showForm = true; this.notice = '';
  }

  saveRecord(): void {
    const resource = this.activeResource;
    if (!resource || (this.editingId === null ? !this.canCreate(resource) : !this.canEdit(resource))) return;
    const payload = this.toPayload(resource, this.formData);
    this.saving = true; this.notice = '';
    const request = this.editingId !== null ? this.auth.update(resource.endpoint, this.editingId, payload) : this.auth.create(resource.endpoint, payload);
    request.subscribe({ next: () => { this.saving = false; this.showForm = false; this.noticeKind = 'success'; this.notice = `${resource.singular[0].toUpperCase()}${resource.singular.slice(1)} saved.`; this.loadResource(); if (resource.key === 'appointments') this.loadDashboard(); }, error: (error: HttpErrorResponse) => { this.saving = false; this.setError(error, `Could not save ${resource.singular}.`); } });
  }

  deleteRow(row: Row): void {
    const resource = this.activeResource;
    if (!resource || !this.canDelete(resource) || !window.confirm(`Delete this ${resource.singular}? This cannot be undone.`)) return;
    this.auth.remove(resource.endpoint, Number(row['id'])).subscribe({ next: () => { this.noticeKind = 'success'; this.notice = `${resource.singular[0].toUpperCase()}${resource.singular.slice(1)} deleted.`; this.loadResource(); }, error: (error: HttpErrorResponse) => this.setError(error, `Could not delete ${resource.singular}.`) });
  }

  openNurseAssignments(patient: Row): void {
    this.assignmentPatient = patient;
    this.assignmentNurses = [];
    this.assignedNurses = [];
    this.assignmentLoading = true;
    forkJoin({ nurses: this.auth.list<Row>('nurses'), assigned: this.auth.assignedNurses(Number(patient['id'])) }).subscribe({
      next: (data) => { this.assignmentNurses = data.nurses; this.assignedNurses = data.assigned; this.assignmentLoading = false; },
      error: (error: HttpErrorResponse) => { this.assignmentLoading = false; this.setError(error, 'Could not load nurse assignments.'); }
    });
  }

  isNurseAssigned(nurse: Row): boolean { return this.assignedNurses.some((assigned) => Number(assigned['id']) === Number(nurse['id'])); }

  toggleNurseAssignment(nurse: Row): void {
    if (!this.assignmentPatient) return;
    const patientId = Number(this.assignmentPatient['id']);
    const request = this.isNurseAssigned(nurse) ? this.auth.unassignNurse(patientId, Number(nurse['id'])) : this.auth.assignNurse(patientId, Number(nurse['id']));
    request.subscribe({
      next: () => this.auth.assignedNurses(patientId).subscribe({ next: (nurses) => { this.assignedNurses = nurses; }, error: (error: HttpErrorResponse) => this.setError(error, 'Assignment saved, but the list could not refresh.') }),
      error: (error: HttpErrorResponse) => this.setError(error, 'Could not update the nurse assignment.')
    });
  }

  displayValue(row: Row, path: string): string {
    const value = path.split('.').reduce<unknown>((current, part) => current && typeof current === 'object' ? (current as Row)[part] : undefined, row);
    if (value === null || value === undefined || value === '') return '—';
    if (typeof value === 'string' && (path.toLowerCase().includes('datetime') || path.toLowerCase().endsWith('at'))) return new Date(value).toLocaleString();
    return String(value);
  }

  fieldOptions(field: Field): Row[] | string[] { return field.options === 'appointmentStatus' ? this.statuses : field.options === 'prescriptionStatus' ? this.prescriptionStatuses : field.options === 'labStatus' ? this.labStatuses : field.options === 'gender' ? this.genderOptions : this.lookups[field.options || ''] || []; }
  optionValue(field: Field, option: Row | string): string | number { if (typeof option === 'string') return option; return field.options === 'doctors' ? option['docId'] : option['id']; }
  optionLabel(field: Field, option: Row | string): string { if (typeof option === 'string') return option; return field.options === 'records' ? `Record #${option['id']} · ${option['diagnosis'] || 'No diagnosis'}` : `${option['name'] || option['medicationName'] || option['testName'] || 'Item'}${option['department'] ? ` · ${option['department']}` : ''} · #${this.optionValue(field, option)}`; }
  trackResource(_: number, item: Resource): string { return item.key; }
  trackRow(_: number, row: Row): number { return row['id']; }

  private toPayload(resource: Resource, form: Row): Row {
    const payload: Row = {};
    for (const field of resource.fields) if (form[field.key] !== '' && form[field.key] !== undefined && form[field.key] !== null) payload[field.key] = form[field.key];
    if (resource.key === 'appointments') {
      if (this.canEnterNewPatient()) {
        const patient: Row = {
          name: form['patientName'], disease: form['patientDisease'],
          gender: form['patientGender'], email: form['patientEmail'], phone: form['patientPhone'], bloodGroup: form['patientBloodGroup'],
          address: form['patientAddress'], emergencyContact: form['patientEmergencyContact']
        };
        if (form['patientAge'] !== '' && form['patientAge'] !== undefined && form['patientAge'] !== null) patient['age'] = Number(form['patientAge']);
        payload['patient'] = patient;
      } else payload['patient'] = { id: Number(form['patientId']) };
      payload['doctor'] = { docId: Number(form['doctorId']) };
    }
    if (resource.key === 'vitals') payload['patient'] = { id: Number(form['patientId']) };
    if (resource.key === 'medical-records') { payload['patient'] = { id: Number(form['patientId']) }; if (form['doctorId']) payload['doctor'] = { docId: Number(form['doctorId']) }; }
    if (['prescriptions', 'lab-reports', 'diagnoses'].includes(resource.key)) {
      payload['medicalRecord'] = { id: Number(form['medicalRecordId']) };
      if (form['doctorId']) payload['doctor'] = { docId: Number(form['doctorId']) };
      if (form['medicationId']) payload['medication'] = { id: Number(form['medicationId']) };
    }
    return payload;
  }

  canEnterNewPatient(): boolean { return this.activeKey === 'appointments' && !this.patientContextId && (this.account?.role === 'DOCTOR' || this.account?.role === 'ADMIN') && this.editingId === null; }

  private submit(request: () => ReturnType<AuthService['login']>): void {
    this.notice = ''; this.loading = true;
    request().subscribe({ next: (account) => { this.account = account; this.authOpen = false; this.loading = false; this.loadDashboard(); }, error: (error: HttpErrorResponse) => { this.notice = this.localizedError(error, 'Something went wrong. Please try again.'); this.noticeKind = 'error'; this.loading = false; } });
  }
  private setError(error: HttpErrorResponse, fallback: string): void { this.noticeKind = 'error'; this.notice = this.localizedError(error, fallback); }
  private localizedError(error: HttpErrorResponse, fallback: string): string {
    const message = error.error?.message || error.error?.error || fallback;
    if (this.assistantLanguage !== 'hi') return message;
    const keys: Record<number, string> = { 400: 'errors.invalidInput', 401: 'errors.unauthorized', 403: 'errors.forbidden', 404: 'errors.notFound', 409: 'errors.conflict', 429: 'errors.rateLimited', 503: 'errors.notConfigured' };
    const key = keys[error.status] || (error.status >= 500 ? 'errors.serverUnavailable' : '');
    if (key) return (hi as Record<string, string>)[key] || message;
    return message;
  }
  private finishLogout(): void { this.account = null; this.authOpen = false; this.loginData = { email: '', password: '' }; this.signupData = { role: 'PATIENT', name: '', email: '', password: '' }; this.mode = 'login'; this.loading = false; this.notice = ''; this.activeKey = 'overview'; }
}
