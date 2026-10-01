-- CareConnect demo dataset for the existing JPA schema.
-- Run after starting Spring Boot once so Hibernate has created/updated the schema.
-- Intended for a fresh local/demo database: records use stable IDs and duplicate keys update those demo rows.
USE CareConnect;
START TRANSACTION;

-- Demo accounts (PasswordHasher: PBKDF2-HMAC-SHA256, 310000 iterations).
INSERT INTO accounts (id, email, password_hash, role, display_name, profile_id) VALUES
(1, 'patient@careconnect.com', 'pbkdf2-sha256$310000$AQIDBAUGBwgJCgsMDQ4PEA==$Sy0DLPNvW/cFv+8+YwIdLvRsJcvdGyxXfI3vyN3nyzE=', 'PATIENT', 'Rahul Sharma', 1),
(2, 'doctor@careconnect.com', 'pbkdf2-sha256$310000$ERITFBUWFxgZGhscHR4fIA==$zyhxXxXN4AEOPX5tYo1TikjnAg1Oi5qHWxxVc2Cu/sM=', 'DOCTOR', 'Dr. Ankit Verma', 1),
(3, 'nurse@careconnect.com', 'pbkdf2-sha256$310000$ISIjJCUmJygpKissLS4vMA==$CTq47iu/psPnnjkpqtQlcUK5b3nHrXzgAePHHcER4bw=', 'NURSE', 'Nurse Kavita', 1),
(4, 'admin@careconnect.com', 'pbkdf2-sha256$310000$MTIzNDU2Nzg5Ojs8PT4/QA==$kDFXCZ+4oCh0csUs305+xeeMDGwuoY0EarZbO2frjGs=', 'ADMIN', 'CareConnect Admin', 1)
ON DUPLICATE KEY UPDATE email=VALUES(email), password_hash=VALUES(password_hash), role=VALUES(role), display_name=VALUES(display_name), profile_id=VALUES(profile_id);

INSERT INTO doctor (docid, name, experience, department) VALUES
(1, 'Dr. Ankit Verma', 12, 'Cardiology'),
(2, 'Dr. Sneha Kapoor', 9, 'Neurology'),
(3, 'Dr. Rahul Mehta', 15, 'General Medicine'),
(4, 'Dr. Priya Sharma', 8, 'Pulmonology'),
(5, 'Dr. Arjun Malhotra', 11, 'Endocrinology')
ON DUPLICATE KEY UPDATE name=VALUES(name), experience=VALUES(experience), department=VALUES(department);

INSERT INTO nurses (id, name, department, phone_number) VALUES
(1, 'Nurse Kavita', 'Cardiology', NULL),
(2, 'Nurse Pooja', 'General Medicine', NULL),
(3, 'Nurse Riya', 'Neurology', NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name), department=VALUES(department), phone_number=VALUES(phone_number);

INSERT INTO admins (id, name, email) VALUES
(1, 'CareConnect Admin', 'admin@careconnect.com')
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email);

INSERT INTO patients (id, name, age, disease, gender, email, phone, blood_group, address, emergency_contact) VALUES
(1, 'Rahul Sharma', 32, 'Hypertension', 'Male', 'rahul.sharma@example.com', '9876543210', 'B+', 'Ghaziabad, Uttar Pradesh', '9876500011'),
(2, 'Priya Singh', 27, 'Migraine', 'Female', 'priya.singh@example.com', '9876543211', 'O+', 'Delhi', NULL),
(3, 'Arjun Mehta', 45, 'Type 2 Diabetes', 'Male', 'arjun.mehta@example.com', '9876543212', 'A+', 'Noida, Uttar Pradesh', NULL),
(4, 'Neha Verma', 36, 'Asthma', 'Female', 'neha.verma@example.com', '9876543213', 'B+', 'Ghaziabad, Uttar Pradesh', NULL),
(5, 'Rohan Gupta', 51, 'High Cholesterol', 'Male', 'rohan.gupta@example.com', '9876543214', 'AB+', 'Delhi', NULL),
(6, 'Ananya Kapoor', 24, 'Iron Deficiency Anemia', 'Female', 'ananya.kapoor@example.com', '9876543215', 'O-', 'Noida, Uttar Pradesh', NULL),
(7, 'Vikram Sethi', 58, 'Coronary Artery Disease', 'Male', 'vikram.sethi@example.com', '9876543220', 'A+', 'Indirapuram, Uttar Pradesh', '9876500020'),
(8, 'Meera Nair', 43, 'Hypertension', 'Female', 'meera.nair@example.com', '9876543221', 'O+', 'Vaishali, Uttar Pradesh', '9876500021'),
(9, 'Karan Malhotra', 35, 'Palpitations', 'Male', 'karan.malhotra@example.com', '9876543222', 'B-', 'East Delhi', '9876500022')
ON DUPLICATE KEY UPDATE name=VALUES(name), age=VALUES(age), disease=VALUES(disease), gender=VALUES(gender), email=VALUES(email), phone=VALUES(phone), blood_group=VALUES(blood_group), address=VALUES(address), emergency_contact=VALUES(emergency_contact);

INSERT INTO medications (id, name, strength, form) VALUES
(1, 'Amlodipine', '5 mg', 'Tablet'),
(2, 'Metformin', '500 mg', 'Tablet'),
(3, 'Paracetamol', '500 mg', 'Tablet'),
(4, 'Pantoprazole', '40 mg', 'Tablet'),
(5, 'Sumatriptan', '50 mg', 'Tablet'),
(6, 'Salbutamol', '100 mcg', 'Inhaler'),
(7, 'Atorvastatin', '20 mg', 'Tablet'),
(8, 'Ferrous Sulfate', '100 mg', 'Tablet')
ON DUPLICATE KEY UPDATE name=VALUES(name), strength=VALUES(strength), form=VALUES(form);

-- Clinical encounters are parents of diagnoses, prescriptions, and lab reports.
INSERT INTO medical_records (id, patient_id, doctor_id, diagnosis, treatment, notes, recorded_at) VALUES
(1, 1, 1, 'Routine cardiovascular check-up', 'Continue regular blood pressure monitoring.', 'Patient advised to monitor blood pressure regularly.', '2026-07-10 09:30:00'),
(2, 1, 1, 'Hypertension', 'Continue Amlodipine; review blood pressure in 30 days.', 'Patient reported occasional headaches. Blood pressure monitored.', '2026-08-12 10:00:00'),
(3, 2, 2, 'Migraine', 'Sumatriptan as required; hydration and trigger diary advised.', 'Patient reports recurrent headache episodes.', '2026-08-25 11:00:00'),
(4, 3, 5, 'Type 2 Diabetes', 'Continue Metformin and monitor blood glucose.', 'Blood glucose remains above target.', '2026-09-10 09:00:00'),
(5, 4, 4, 'Asthma', 'Continue inhaler as required.', 'Asthma symptoms controlled.', '2026-09-15 14:00:00'),
(6, 6, 3, 'Iron Deficiency Anemia', 'Start oral iron replacement and repeat blood tests.', 'Low hemoglobin and ferritin levels observed.', '2026-09-20 10:30:00'),
(7, 7, 1, 'Coronary Artery Disease', 'Continue cardiac medications and arrange cardiology follow-up.', 'Stable exertional symptoms. Reviewed medication adherence and warning signs.', '2026-09-22 09:00:00'),
(8, 8, 1, 'Hypertension', 'Begin home blood pressure monitoring and review in four weeks.', 'Elevated readings on several recent checks; lifestyle changes discussed.', '2026-09-24 10:00:00'),
(9, 9, 1, 'Palpitations', 'Arrange ECG and limit caffeine pending review.', 'Intermittent palpitations without syncope or chest pain.', '2026-09-26 11:00:00')
ON DUPLICATE KEY UPDATE patient_id=VALUES(patient_id), doctor_id=VALUES(doctor_id), diagnosis=VALUES(diagnosis), treatment=VALUES(treatment), notes=VALUES(notes), recorded_at=VALUES(recorded_at);

INSERT INTO appointments (id, patient_id, doctor_id, appointment_date_time, status, reason) VALUES
(1, 1, 1, '2026-08-12 10:00:00', 'COMPLETED', 'Hypertension follow-up'),
(2, 1, 1, '2026-10-05 10:00:00', 'CONFIRMED', 'Blood pressure review'),
(3, 2, 2, '2026-08-25 11:00:00', 'COMPLETED', 'Migraine consultation'),
(4, 2, 2, '2026-10-08 11:30:00', 'SCHEDULED', 'Follow-up consultation'),
(5, 3, 5, '2026-09-10 09:00:00', 'COMPLETED', 'Diabetes review'),
(6, 3, 5, '2026-10-12 09:00:00', 'CONFIRMED', 'Diabetes and HbA1c review'),
(7, 4, 4, '2026-09-15 14:00:00', 'COMPLETED', 'Asthma check-up'),
(8, 6, 3, '2026-09-20 10:30:00', 'COMPLETED', 'Anemia consultation'),
(9, 7, 1, '2026-09-22 09:00:00', 'COMPLETED', 'Cardiology review'),
(10, 8, 1, '2026-09-24 10:00:00', 'COMPLETED', 'Blood pressure consultation'),
(11, 9, 1, '2026-09-26 11:00:00', 'COMPLETED', 'Palpitations assessment'),
(12, 7, 1, '2026-10-20 09:30:00', 'CONFIRMED', 'Cardiac follow-up'),
(13, 8, 1, '2026-10-22 10:00:00', 'SCHEDULED', 'Blood pressure review'),
(14, 9, 1, '2026-10-24 11:00:00', 'SCHEDULED', 'ECG and symptom review')
ON DUPLICATE KEY UPDATE patient_id=VALUES(patient_id), doctor_id=VALUES(doctor_id), appointment_date_time=VALUES(appointment_date_time), status=VALUES(status), reason=VALUES(reason);

INSERT INTO diagnoses (id, medical_record_id, patient_id, doctor_id, diagnosis, notes, diagnosed_at) VALUES
(1, 2, 1, 1, 'Essential Hypertension', 'Blood pressure consistently elevated. Lifestyle modification advised.', '2026-08-12 10:00:00'),
(2, 2, 1, 1, 'Vitamin D Deficiency', 'Vitamin D supplementation recommended.', '2026-08-12 10:05:00'),
(3, 3, 2, 2, 'Migraine without aura', 'Recurring headache episodes approximately 2-3 times per month.', '2026-08-25 11:00:00'),
(4, 4, 3, 5, 'Type 2 Diabetes Mellitus', 'Blood glucose requires continued monitoring.', '2026-09-10 09:00:00'),
(5, 4, 3, 5, 'Hyperlipidemia', 'Elevated LDL cholesterol.', '2026-09-10 09:10:00'),
(6, 5, 4, 4, 'Bronchial Asthma', 'Mild intermittent asthma.', '2026-09-15 14:00:00'),
(7, 6, 6, 3, 'Iron Deficiency Anemia', 'Low hemoglobin and ferritin levels observed.', '2026-09-20 10:30:00'),
(8, 7, 7, 1, 'Coronary Artery Disease', 'Known stable disease; continue risk-factor management.', '2026-09-22 09:00:00'),
(9, 8, 8, 1, 'Essential Hypertension', 'Home readings requested to guide treatment adjustment.', '2026-09-24 10:00:00'),
(10, 9, 9, 1, 'Intermittent Palpitations', 'ECG requested; seek urgent care if chest pain or fainting occurs.', '2026-09-26 11:00:00')
ON DUPLICATE KEY UPDATE medical_record_id=VALUES(medical_record_id), patient_id=VALUES(patient_id), doctor_id=VALUES(doctor_id), diagnosis=VALUES(diagnosis), notes=VALUES(notes), diagnosed_at=VALUES(diagnosed_at);

INSERT INTO prescriptions (id, medical_record_id, patient_id, doctor_id, medication_id, medication_name, dosage, frequency, duration, instructions, prescribed_date, status) VALUES
(1, 2, 1, 1, 1, 'Amlodipine', '5 mg', 'Once daily', '30 days', 'Take at the same time each day.', '2026-09-12', 'ACTIVE'),
(2, 2, 1, 1, 4, 'Pantoprazole', '40 mg', 'Once daily before breakfast', '14 days', 'Take before breakfast.', '2026-09-25', 'ACTIVE'),
(3, 3, 2, 2, 5, 'Sumatriptan', '50 mg', 'When migraine occurs', 'As required', 'Use as directed by your clinician.', '2026-08-25', 'ACTIVE'),
(4, 3, 2, 2, 3, 'Paracetamol', '500 mg', 'Twice daily after meals', '5 days', 'Take after food.', '2026-08-25', 'COMPLETED'),
(5, 4, 3, 5, 2, 'Metformin', '500 mg', 'Twice daily after meals', '90 days', 'Take after meals.', '2026-09-10', 'ACTIVE'),
(6, 4, 3, 5, 7, 'Atorvastatin', '20 mg', 'Once daily at night', '90 days', 'Take at night.', '2026-09-10', 'ACTIVE'),
(7, 5, 4, 4, 6, 'Salbutamol', '2 puffs', 'When required', '30 days', 'Use inhaler as directed.', '2026-09-15', 'ACTIVE'),
(8, 6, 6, 3, 8, 'Ferrous Sulfate', '100 mg', 'Once daily', '60 days', 'Take as prescribed.', '2026-09-20', 'ACTIVE'),
(9, 7, 7, 1, 1, 'Amlodipine', '5 mg', 'Once daily', '30 days', 'Continue current regimen; review blood pressure at follow-up.', '2026-09-22', 'ACTIVE'),
(10, 8, 8, 1, 1, 'Amlodipine', '5 mg', 'Once daily', '30 days', 'Take daily and record home blood pressure readings.', '2026-09-24', 'ACTIVE')
ON DUPLICATE KEY UPDATE medical_record_id=VALUES(medical_record_id), patient_id=VALUES(patient_id), doctor_id=VALUES(doctor_id), medication_id=VALUES(medication_id), medication_name=VALUES(medication_name), dosage=VALUES(dosage), frequency=VALUES(frequency), duration=VALUES(duration), instructions=VALUES(instructions), prescribed_date=VALUES(prescribed_date), status=VALUES(status);

INSERT INTO lab_reports (id, medical_record_id, patient_id, doctor_id, test_name, result, reference_range, report_date, status) VALUES
(1, 2, 1, 1, 'Complete Blood Count', 'Hemoglobin 14.2 g/dL', '13.0 - 17.0 g/dL', '2026-08-12', 'NORMAL'),
(2, 2, 1, 1, 'Lipid Profile', 'LDL 142 mg/dL', 'Less than 100 mg/dL', '2026-08-12', 'ABNORMAL'),
(3, 3, 2, 2, 'Complete Blood Count', 'Hemoglobin 12.8 g/dL', '12.0 - 15.5 g/dL', '2026-08-25', 'NORMAL'),
(4, 4, 3, 5, 'Fasting Blood Sugar', '138 mg/dL', '70 - 100 mg/dL', '2026-09-10', 'ABNORMAL'),
(5, 4, 3, 5, 'HbA1c', '7.2%', 'Less than 5.7%', '2026-09-10', 'ABNORMAL'),
(6, 4, 3, 5, 'Lipid Profile', 'LDL 155 mg/dL', 'Less than 100 mg/dL', '2026-09-10', 'ABNORMAL'),
(7, 5, 4, 4, 'Pulmonary Function Test', 'FEV1 82%', 'Greater than 80%', '2026-09-15', 'NORMAL'),
(8, 6, 6, 3, 'Hemoglobin', '9.8 g/dL', '12.0 - 16.0 g/dL', '2026-09-20', 'ABNORMAL'),
(9, 6, 6, 3, 'Serum Ferritin', '11 ng/mL', '15 - 150 ng/mL', '2026-09-20', 'ABNORMAL'),
(10, 7, 7, 1, 'Lipid Profile', 'LDL 96 mg/dL', 'Less than 100 mg/dL', '2026-09-22', 'NORMAL'),
(11, 7, 7, 1, 'ECG', 'Sinus rhythm; no acute changes', 'Normal sinus rhythm', '2026-09-22', 'NORMAL'),
(12, 8, 8, 1, 'Basic Metabolic Panel', 'Creatinine 0.9 mg/dL', '0.7 - 1.3 mg/dL', '2026-09-24', 'NORMAL'),
(13, 9, 9, 1, 'ECG', 'Sinus rhythm at rest', 'Normal sinus rhythm', '2026-09-26', 'NORMAL')
ON DUPLICATE KEY UPDATE medical_record_id=VALUES(medical_record_id), patient_id=VALUES(patient_id), doctor_id=VALUES(doctor_id), test_name=VALUES(test_name), result=VALUES(result), reference_range=VALUES(reference_range), report_date=VALUES(report_date), status=VALUES(status);

INSERT INTO vitals (id, patient_id, blood_pressure, heart_rate, temperature, oxygen_saturation, weight, height, recorded_at) VALUES
(1, 1, '138/88', 78, 36.7, 98, 74, 175, '2026-09-20 09:00:00'),
(2, 3, '142/90', 82, 36.8, 97, 82, 172, '2026-09-20 09:15:00'),
(3, 4, '124/80', 76, 36.6, 97, 62, 165, '2026-09-20 09:30:00'),
(4, 7, '132/82', 72, 36.7, 98, 80, 178, '2026-09-22 08:50:00'),
(5, 8, '148/92', 84, 36.8, 98, 68, 162, '2026-09-24 09:50:00'),
(6, 9, '126/78', 88, 36.7, 99, 76, 174, '2026-09-26 10:50:00')
ON DUPLICATE KEY UPDATE patient_id=VALUES(patient_id), blood_pressure=VALUES(blood_pressure), heart_rate=VALUES(heart_rate), temperature=VALUES(temperature), oxygen_saturation=VALUES(oxygen_saturation), weight=VALUES(weight), height=VALUES(height), recorded_at=VALUES(recorded_at);

INSERT INTO patient_nurse_assignments (patient_id, nurse_id) VALUES
(1, 1), (2, 3), (3, 2), (4, 1), (6, 2), (7, 1), (8, 1), (9, 1)
ON DUPLICATE KEY UPDATE patient_id=VALUES(patient_id), nurse_id=VALUES(nurse_id);

INSERT INTO notifications (id, account_id, title, message, created_at, read_at) VALUES
(1, 1, 'Appointment confirmed', 'Your blood pressure review with Dr. Ankit Verma is confirmed for October 5, 2026.', '2026-09-25 09:00:00', NULL),
(2, 1, 'Lab result available', 'Your lipid profile report is available in your lab reports.', '2026-08-12 12:00:00', NULL),
(3, 2, 'Upcoming patient visit', 'Rahul Sharma is scheduled for a blood pressure review on October 5, 2026.', '2026-09-25 09:05:00', NULL),
(4, 3, 'Patient assignment', 'Rahul Sharma has been assigned to your care list.', '2026-09-20 08:00:00', NULL),
(5, 4, 'Demo environment ready', 'Use the CareConnect admin workspace to review demo accounts and records.', '2026-09-30 08:00:00', NULL)
ON DUPLICATE KEY UPDATE account_id=VALUES(account_id), title=VALUES(title), message=VALUES(message), created_at=VALUES(created_at), read_at=VALUES(read_at);

COMMIT;
