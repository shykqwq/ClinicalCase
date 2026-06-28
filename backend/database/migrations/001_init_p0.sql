CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE departments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name varchar(100) NOT NULL,
  hospital_name varchar(150),
  status varchar(20) NOT NULL DEFAULT 'active',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  username varchar(80) NOT NULL,
  password_hash varchar(255) NOT NULL,
  display_name varchar(80) NOT NULL,
  phone varchar(30),
  email varchar(120),
  role varchar(30) NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'active',
  last_login_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  CONSTRAINT uk_users_department_username UNIQUE (department_id, username)
);

CREATE TABLE patients (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  name varchar(80) NOT NULL,
  gender varchar(20) NOT NULL,
  birth_date date,
  age_at_first_visit int,
  phone varchar(30),
  identity_no varchar(30),
  outpatient_no varchar(80),
  inpatient_no varchar(80),
  first_visit_date date NOT NULL,
  attending_doctor_id uuid REFERENCES users(id),
  remark text,
  created_by uuid NOT NULL REFERENCES users(id),
  updated_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

CREATE TABLE clinical_cases (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  patient_id uuid NOT NULL REFERENCES patients(id),
  title varchar(150) NOT NULL,
  visit_type varchar(30) NOT NULL,
  laterality varchar(20) NOT NULL,
  disease_type varchar(50) NOT NULL,
  preliminary_diagnosis text,
  confirmed_diagnosis text,
  pathology_type varchar(120),
  er_status varchar(20),
  pr_status varchar(20),
  her2_status varchar(30),
  ki67_percent numeric(5,2),
  molecular_subtype varchar(40),
  clinical_t_stage varchar(20),
  clinical_n_stage varchar(20),
  clinical_m_stage varchar(20),
  pathological_t_stage varchar(20),
  pathological_n_stage varchar(20),
  pathological_m_stage varchar(20),
  clinical_stage varchar(30),
  pathological_stage varchar(30),
  has_neoadjuvant_therapy boolean,
  neoadjuvant_therapy_plan text,
  surgery_date date,
  surgery_type varchar(120),
  axillary_management varchar(120),
  chemotherapy_plan text,
  has_radiotherapy boolean,
  has_targeted_therapy boolean,
  targeted_therapy_plan text,
  has_endocrine_therapy boolean,
  endocrine_therapy_plan text,
  recurrence_status varchar(40),
  current_status varchar(40) NOT NULL,
  summary text,
  case_status varchar(20) NOT NULL DEFAULT 'draft',
  archived_at timestamptz,
  created_by uuid NOT NULL REFERENCES users(id),
  updated_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

CREATE TABLE tags (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  name varchar(80) NOT NULL,
  color varchar(20),
  created_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  CONSTRAINT uk_tags_department_name UNIQUE (department_id, name)
);

CREATE TABLE case_tag_relations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  case_id uuid NOT NULL REFERENCES clinical_cases(id),
  tag_id uuid NOT NULL REFERENCES tags(id),
  created_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT uk_case_tag UNIQUE (case_id, tag_id)
);

CREATE TABLE attachments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  patient_id uuid NOT NULL REFERENCES patients(id),
  case_id uuid REFERENCES clinical_cases(id),
  category varchar(40) NOT NULL,
  original_filename varchar(255) NOT NULL,
  content_type varchar(120) NOT NULL,
  file_size bigint NOT NULL,
  object_key varchar(500) NOT NULL,
  checksum varchar(128),
  uploaded_by uuid NOT NULL REFERENCES users(id),
  uploaded_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

CREATE TABLE follow_up_plans (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  patient_id uuid NOT NULL REFERENCES patients(id),
  case_id uuid NOT NULL REFERENCES clinical_cases(id),
  follow_up_type varchar(50) NOT NULL,
  planned_date date NOT NULL,
  assignee_id uuid REFERENCES users(id),
  status varchar(30) NOT NULL DEFAULT 'pending',
  created_by uuid NOT NULL REFERENCES users(id),
  updated_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

CREATE TABLE follow_up_records (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid NOT NULL REFERENCES departments(id),
  follow_up_plan_id uuid NOT NULL REFERENCES follow_up_plans(id),
  patient_id uuid NOT NULL REFERENCES patients(id),
  case_id uuid NOT NULL REFERENCES clinical_cases(id),
  actual_date date NOT NULL,
  method varchar(40) NOT NULL,
  survival_status varchar(40) NOT NULL,
  has_recurrence boolean,
  recurrence_site varchar(255),
  has_metastasis boolean,
  metastasis_site varchar(255),
  imaging_summary text,
  tumor_marker_summary text,
  current_treatment text,
  adverse_reactions text,
  remark text,
  summary text,
  next_follow_up_date date,
  recorded_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

CREATE TABLE audit_logs (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id uuid REFERENCES departments(id),
  actor_id uuid REFERENCES users(id),
  action varchar(80) NOT NULL,
  object_type varchar(80),
  object_id uuid,
  before_snapshot jsonb,
  after_snapshot jsonb,
  ip_address varchar(80),
  user_agent text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_departments_status ON departments(status);
CREATE INDEX idx_users_department_role ON users(department_id, role);
CREATE INDEX idx_users_department_status ON users(department_id, status);
CREATE INDEX idx_patients_department_name ON patients(department_id, name);
CREATE INDEX idx_patients_department_phone ON patients(department_id, phone);
CREATE INDEX idx_patients_department_outpatient_no ON patients(department_id, outpatient_no);
CREATE INDEX idx_patients_department_inpatient_no ON patients(department_id, inpatient_no);
CREATE INDEX idx_patients_department_first_visit ON patients(department_id, first_visit_date);
CREATE INDEX idx_cases_department_patient ON clinical_cases(department_id, patient_id);
CREATE INDEX idx_cases_department_status ON clinical_cases(department_id, case_status);
CREATE INDEX idx_cases_department_disease ON clinical_cases(department_id, disease_type);
CREATE INDEX idx_cases_department_subtype ON clinical_cases(department_id, molecular_subtype);
CREATE INDEX idx_cases_department_stage ON clinical_cases(department_id, clinical_stage, pathological_stage);
CREATE INDEX idx_cases_department_current_status ON clinical_cases(department_id, current_status);
CREATE INDEX idx_cases_surgery_date ON clinical_cases(surgery_date);
CREATE INDEX idx_case_tag_department_tag ON case_tag_relations(department_id, tag_id);
CREATE INDEX idx_attachments_department_patient ON attachments(department_id, patient_id);
CREATE INDEX idx_attachments_department_case ON attachments(department_id, case_id);
CREATE INDEX idx_attachments_category ON attachments(category);
CREATE INDEX idx_follow_up_plans_department_status_date ON follow_up_plans(department_id, status, planned_date);
CREATE INDEX idx_follow_up_plans_assignee ON follow_up_plans(assignee_id, planned_date);
CREATE INDEX idx_follow_up_plans_case ON follow_up_plans(case_id);
CREATE INDEX idx_follow_up_records_plan ON follow_up_records(follow_up_plan_id);
CREATE INDEX idx_follow_up_records_case ON follow_up_records(case_id, actual_date);
CREATE INDEX idx_follow_up_records_patient ON follow_up_records(patient_id, actual_date);
CREATE INDEX idx_audit_logs_department_time ON audit_logs(department_id, created_at DESC);
CREATE INDEX idx_audit_logs_actor_time ON audit_logs(actor_id, created_at DESC);
CREATE INDEX idx_audit_logs_object ON audit_logs(object_type, object_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
