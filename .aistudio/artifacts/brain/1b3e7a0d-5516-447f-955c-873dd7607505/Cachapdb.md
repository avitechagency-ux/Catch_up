# Cachap Database Schema (Supabase)

This document outlines the database schema used in the Cachap application, managed via Supabase Postgrest.

## Table Definitions

### 1. `users`
Stores all registered users (Students, Teachers, Admins).

| Column | Type | Description |
| :--- | :--- | :--- |
| id | UUID | Primary key |
| phone_number | Text | Unique identifier |
| email | Text | Optional email |
| full_name | Text | User display name |
| role | Text | student, teacher, admin |
| password_hash | Text | Encrypted password |
| school_name | Text | User's institution |
| academic_class | Text | (Student) S.1 - S.6 |
| stream | Text | (Student) Class stream |
| teacher_id_code | Text | (Teacher) Unique ID |
| subjects_taught | Text | (Teacher) CSV list |
| is_plan_active | Boolean | Subscription status |
| plan_duration | Text | Duration e.g. "1 Month" |
| plan_expiry_date | Text | Expiry date |
| streak_days | Int | Current streak count |
| streak_millis_today | Long | Session time today |
| created_at | Timestamp | Join date |

### 2. `notes_uploads`
Stores educational material uploaded by teachers.

| Column | Type | Description |
| :--- | :--- | :--- |
| id | UUID | Primary key |
| title | Text | Note/Assignment title |
| subject_name | Text | Associated subject |
| teacher_id_code | Text | Authoring teacher |
| file_url | Text | URL in storage |
| status | Text | e.g. "Published" |
| date_uploaded | Text | ISO date |

### 3. `student_teacher_links`
Manages connections between students and their teachers.

| Column | Type | Description |
| :--- | :--- | :--- |
| student_phone | Text | Student identifier |
| teacher_id_code | Text | Connected teacher ID |
| subject_name | Text | Subject of connection |
| connected_date | Text | Date connected |

### 4. `subscription_plans`
Defines available learning subscription tiers.

| Column | Type | Description |
| :--- | :--- | :--- |
| id | Text | Plan ID |
| duration | Text | e.g. "1 Month" |
| price_ugx | Int | Price in UGX |
| is_active | Boolean | Visibility |

### 5. `payments_transactions`
Tracks student subscription payments.

| Column | Type | Description |
| :--- | :--- | :--- |
| id | UUID | Primary key |
| reference_number | Text | Transaction reference |
| user_name | Text | Subscriber name |
| phone_number | Text | Subscriber phone |
| amount_ugx | Long | Amount paid |
| status | Text | e.g. "COMPLETED" |
| transaction_date | Text | ISO date |

---
*Note: Additional tables exist for `live_classes`, `affiliates`, `glossary`, and platform settings consistent with the application's DTO structure.*
