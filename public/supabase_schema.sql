-- ============================================================================
-- CACHAP PLATFORM — COMPLETE SUPABASE DATABASE & STORAGE SCHEMA (WITH TEACHER
-- SUBJECT QUERIES, LIVE UPLOADS TRIGGERS & ADMIN PORTAL REAL-TIME SYNC)
-- Paste this entire script into your Supabase Dashboard -> SQL Editor -> Run
-- Safe to run on an existing project (uses IF NOT EXISTS & CREATE OR REPLACE)
-- ============================================================================

-- Enable UUID generation extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. USERS TABLE (Students, Teachers, and Admins)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number TEXT UNIQUE NOT NULL,
    email TEXT DEFAULT '',
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'student' CHECK (role IN ('student', 'teacher', 'admin')),
    password_hash TEXT NOT NULL DEFAULT '',
    school_name TEXT DEFAULT 'Secondary School',
    academic_class TEXT DEFAULT '',          -- e.g., 'S.1', 'S.2', 'S.3', 'S.4', 'S.5', 'S.6'
    stream TEXT DEFAULT '',                  -- e.g., 'Green', 'Blue', 'North'
    teacher_id_code VARCHAR(6) UNIQUE,       -- 6-char uppercase alphanumeric ID (A-Z, 0-9), e.g., 'K7M4P9'
    subjects_taught TEXT DEFAULT '',         -- Comma-separated subjects selected by teacher on signup
    is_plan_active BOOLEAN DEFAULT FALSE,
    plan_duration TEXT DEFAULT '',           -- e.g., '1 Month'
    plan_expiry_date TEXT DEFAULT '',        -- e.g., 'Valid for 30 days' or ISO date
    referral_code TEXT UNIQUE,               -- e.g., 'CACHAP-SARAH'
    referred_by_code TEXT DEFAULT '',
    affiliate_balance_ugx BIGINT DEFAULT 0,
    total_earned_ugx BIGINT DEFAULT 0,
    referral_count INTEGER DEFAULT 0,
    affiliate_status TEXT DEFAULT 'ACTIVE' CHECK (affiliate_status IN ('ACTIVE', 'CANCELLED')),
    fcm_token TEXT DEFAULT '',
    account_status TEXT DEFAULT 'Active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Ensure columns and email index exist if table was created before email & password auth migration
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS email TEXT DEFAULT '';
ALTER TABLE public.users ALTER COLUMN phone_number SET DEFAULT '';
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS subjects_taught TEXT DEFAULT '';
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS account_status TEXT DEFAULT 'Active';

-- Backfill email for existing accounts where email was blank
UPDATE public.users
SET email = lower(trim(phone_number))
WHERE (email IS NULL OR trim(email) = '')
  AND phone_number IS NOT NULL
  AND trim(phone_number) <> '';

CREATE INDEX IF NOT EXISTS idx_users_phone ON public.users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(lower(email));
CREATE INDEX IF NOT EXISTS idx_users_role ON public.users(role);
CREATE INDEX IF NOT EXISTS idx_users_teacher_id_code ON public.users(teacher_id_code);

-- ============================================================================
-- 1B. CUSTOM AUTH SESSIONS TABLE (Zero-SMS Email & Password Table-Based Auth)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.auth_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    phone_number TEXT NOT NULL DEFAULT '',
    email TEXT DEFAULT '',
    role TEXT NOT NULL DEFAULT 'student',
    session_token TEXT UNIQUE NOT NULL DEFAULT encode(gen_random_bytes(32), 'hex'),
    device_info TEXT DEFAULT 'Cachap Android',
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '90 days')
);

ALTER TABLE public.auth_sessions ADD COLUMN IF NOT EXISTS email TEXT DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_auth_sessions_token ON public.auth_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_phone ON public.auth_sessions(phone_number);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_email ON public.auth_sessions(lower(email));

-- ============================================================================
-- 2. CURRICULUM SUBJECTS TABLE (25 NCDC / UNEB Ugandan Secondary Subjects)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.curriculum_subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT UNIQUE NOT NULL,
    category TEXT NOT NULL DEFAULT 'Core',
    color_hex TEXT NOT NULL,
    icon_name TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO public.curriculum_subjects (name, category, color_hex, icon_name, display_order)
VALUES
    ('Math', 'Core Sciences & Mathematics', '#2A9D8F', 'Architecture', 1),
    ('English', 'Core Sciences & Mathematics', '#D81B60', 'MenuBook', 2),
    ('Physics', 'Core Sciences & Mathematics', '#7C4DFF', 'Hub', 3),
    ('Chemistry', 'Core Sciences & Mathematics', '#E67E22', 'Science', 4),
    ('Biology', 'Core Sciences & Mathematics', '#2A4D8F', 'Eco', 5),
    ('Geography', 'Core Sciences & Mathematics', '#E76F51', 'Public', 6),
    ('History', 'Core Sciences & Mathematics', '#4361EE', 'AccountBalance', 7),
    ('Agriculture', 'Vocational, Applied & Technical', '#2E7D32', 'Agriculture', 8),
    ('ICT', 'Vocational, Applied & Technical', '#00838F', 'Computer', 9),
    ('Entrepreneurship', 'Vocational, Applied & Technical', '#F39C12', 'TrendingUp', 10),
    ('Commerce', 'Vocational, Applied & Technical', '#3F51B5', 'PointOfSale', 11),
    ('Economics', 'Vocational, Applied & Technical', '#1B5E20', 'ShowChart', 12),
    ('Technology & Design', 'Vocational, Applied & Technical', '#5D4037', 'DesignServices', 13),
    ('Nutrition & Food Tech', 'Vocational, Applied & Technical', '#E65100', 'Restaurant', 14),
    ('C.R.E', 'Religious & Moral Education', '#8E24AA', 'Church', 15),
    ('I.R.E', 'Religious & Moral Education', '#00796B', 'Mosque', 16),
    ('Literature', 'Arts, Languages & Humanities', '#C2185B', 'AutoStories', 17),
    ('Fine Art', 'Arts, Languages & Humanities', '#FF5722', 'Palette', 18),
    ('Performing Arts', 'Arts, Languages & Humanities', '#9C27B0', 'MusicNote', 19),
    ('Physical Education', 'Arts, Languages & Humanities', '#C0392B', 'SportsSoccer', 20),
    ('Kiswahili', 'Arts, Languages & Humanities', '#D35400', 'RecordVoiceOver', 21),
    ('Luganda', 'Arts, Languages & Humanities', '#16A085', 'Translate', 22),
    ('French', 'Arts, Languages & Humanities', '#455A64', 'Language', 23),
    ('German', 'Arts, Languages & Humanities', '#37474F', 'Forum', 24),
    ('Arabic', 'Arts, Languages & Humanities', '#00695C', 'Spellcheck', 25)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- 3. TEACHER SUBJECTS TABLE (Subjects & Classes Assigned to Each Teacher)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.teacher_subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    teacher_id_code VARCHAR(6) NOT NULL,
    subject_name TEXT NOT NULL,
    assigned_classes TEXT NOT NULL DEFAULT 'S.1, S.2, S.3, S.4',
    note_count INTEGER DEFAULT 0,
    assignment_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(teacher_id_code, subject_name)
);

CREATE INDEX IF NOT EXISTS idx_teacher_subjects_code ON public.teacher_subjects(teacher_id_code);
CREATE INDEX IF NOT EXISTS idx_teacher_subjects_teacher_id ON public.teacher_subjects(teacher_id);

-- ============================================================================
-- 4. STUDENT <-> TEACHER SUBJECT LINKS TABLE
-- Created when a student enters a Teacher's 6-digit ID on Subjects / Quick Access
-- Powers both the Student's linked subjects and the Teacher's "Students" section
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.student_teacher_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    student_phone TEXT DEFAULT '',
    student_name TEXT NOT NULL,
    student_class TEXT NOT NULL DEFAULT 'S.4',
    subject_name TEXT NOT NULL,
    teacher_id_code VARCHAR(6) NOT NULL CHECK (teacher_id_code ~ '^[A-Z0-9]{6}$'),
    teacher_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    connected_date TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(student_name, subject_name, teacher_id_code)
);

CREATE INDEX IF NOT EXISTS idx_links_teacher_code ON public.student_teacher_links(teacher_id_code);
CREATE INDEX IF NOT EXISTS idx_links_student_phone ON public.student_teacher_links(student_phone);
CREATE INDEX IF NOT EXISTS idx_links_subject ON public.student_teacher_links(subject_name);

-- ============================================================================
-- 5. NOTES & ASSIGNMENTS UPLOADS TABLE
-- Teachers upload PDFs/documents; linked students view & download them;
-- Admin Portal monitors all uploaded notes & assignments in real time.
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.notes_uploads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    subject_name TEXT NOT NULL,
    target_class TEXT NOT NULL,              -- e.g., 'S.4'
    content_type TEXT NOT NULL DEFAULT 'Notes' CHECK (content_type IN ('Notes', 'Assignment')),
    teacher_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    teacher_id_code VARCHAR(6) NOT NULL,
    author_name TEXT NOT NULL DEFAULT 'Teacher',
    file_url TEXT DEFAULT '',                -- Public/Signed URL from 'study-materials' bucket
    file_size TEXT DEFAULT '2.0 MB',
    file_type TEXT DEFAULT 'PDF',
    status TEXT DEFAULT 'Published',         -- 'Published', 'Draft'
    date_uploaded TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notes_subject ON public.notes_uploads(subject_name);
CREATE INDEX IF NOT EXISTS idx_notes_teacher_code ON public.notes_uploads(teacher_id_code);
CREATE INDEX IF NOT EXISTS idx_notes_content_type ON public.notes_uploads(content_type);

-- ============================================================================
-- 6. STUDENT DOWNLOADS & RECENTLY OPENED TOPICS TABLES
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.student_downloads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_phone TEXT NOT NULL,
    note_id UUID REFERENCES public.notes_uploads(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    subject_name TEXT NOT NULL,
    file_size TEXT DEFAULT '2.0 MB',
    saved_date TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(student_phone, title, subject_name)
);

CREATE TABLE IF NOT EXISTS public.student_recent_topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_phone TEXT NOT NULL,
    title TEXT NOT NULL,
    subject_name TEXT NOT NULL,
    opened_date TEXT NOT NULL,
    opened_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_downloads_student ON public.student_downloads(student_phone);
CREATE INDEX IF NOT EXISTS idx_recent_student ON public.student_recent_topics(student_phone);

-- ============================================================================
-- 7. SUBSCRIPTION PLANS TABLE (Single UGX 1,000 / Month Plan)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.subscription_plans (
    id TEXT PRIMARY KEY,
    duration TEXT NOT NULL,
    caption TEXT NOT NULL,
    price_formatted TEXT NOT NULL,
    price_ugx BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO public.subscription_plans (id, duration, caption, price_formatted, price_ugx, is_active)
VALUES ('plan_1_month', '1 Month', 'Billed monthly', 'UGX 1,000', 1000, TRUE)
ON CONFLICT (id) DO UPDATE SET
    duration = EXCLUDED.duration,
    caption = EXCLUDED.caption,
    price_formatted = EXCLUDED.price_formatted,
    price_ugx = EXCLUDED.price_ugx,
    is_active = EXCLUDED.is_active;

-- ============================================================================
-- 8. PAYMENTS & PLATFORM TRANSACTIONS TABLE
-- Records Student Mobile Money payments (UGX 1,000), Affiliate Withdrawals,
-- Teacher Royalties, and Admin Platform Transactions
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.payments_transactions (
    id TEXT PRIMARY KEY DEFAULT ('TX-' || upper(substr(md5(random()::text), 1, 6))),
    user_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    user_name TEXT NOT NULL,
    user_role TEXT NOT NULL DEFAULT 'Student',
    phone_number TEXT NOT NULL,
    provider TEXT NOT NULL DEFAULT 'MTN MoMo', -- 'MTN MoMo', 'Airtel Money', 'Wallet'
    category TEXT NOT NULL DEFAULT 'SUBSCRIPTION' CHECK (
        category IN ('SUBSCRIPTION', 'AFFILIATE_WITHDRAWAL', 'TEACHER_ROYALTY', 'TAX_OPS', 'REFUND')
    ),
    plan_duration TEXT DEFAULT '1 Month',
    amount_ugx BIGINT NOT NULL,               -- Positive for inflow (+1000), negative for payout
    fee_ugx BIGINT DEFAULT 0,
    net_amount_ugx BIGINT NOT NULL,
    status TEXT NOT NULL DEFAULT 'COMPLETED' CHECK (
        status IN ('COMPLETED', 'PENDING', 'FAILED')
    ),
    reference_code TEXT DEFAULT '',
    description TEXT DEFAULT '',
    transaction_date TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tx_phone ON public.payments_transactions(phone_number);
CREATE INDEX IF NOT EXISTS idx_tx_category ON public.payments_transactions(category);

-- ============================================================================
-- 9. AFFILIATE TRANSACTIONS TABLE
-- Tracks +100 UGX referral earnings, wallet withdrawals, and plan purchases
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.affiliate_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_phone TEXT NOT NULL,
    title TEXT NOT NULL,
    subtitle TEXT NOT NULL,
    tx_type TEXT NOT NULL CHECK (
        tx_type IN ('REFERRAL_EARNING', 'MOBILE_MONEY_WITHDRAWAL', 'PLAN_PAYMENT')
    ),
    amount_ugx INTEGER NOT NULL,
    is_credit BOOLEAN NOT NULL DEFAULT TRUE,
    tx_date TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_affiliate_tx_phone ON public.affiliate_transactions(student_phone);

-- ============================================================================
-- 10. STUDENT STREAKS TABLE
-- Tracks daily study goal progress (60 mins/day) and streak milestones
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.student_streaks (
    student_phone TEXT PRIMARY KEY,
    streak_days INTEGER DEFAULT 0,
    is_today_completed BOOLEAN DEFAULT FALSE,
    next_milestone_days INTEGER DEFAULT 7,
    accumulated_minutes INTEGER DEFAULT 0,
    accumulated_millis_today BIGINT DEFAULT 0,
    last_active_date TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================================
-- 11. ADMIN AUDIT LOGS & PLATFORM SETTINGS TABLES
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.admin_audit_logs (
    id TEXT PRIMARY KEY DEFAULT ('log-' || substr(md5(random()::text), 1, 6)),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'CONTENT', -- 'CONTENT', 'PAYMENT', 'SECURITY', 'DATABASE', 'AFFILIATE', 'STORAGE'
    level TEXT NOT NULL DEFAULT 'INFO',       -- 'INFO', 'PAYOUT', 'SECURITY', 'DATABASE', 'WARN', 'ERROR'
    actor TEXT NOT NULL DEFAULT 'System',
    timestamp_label TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.platform_settings (
    setting_key TEXT PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO public.platform_settings (setting_key, setting_value)
VALUES
    ('platform_name', 'Cachap'),
    ('monthly_plan_price_ugx', '1000'),
    ('referral_reward_ugx', '100'),
    ('mtn_momo_enabled', 'true'),
    ('airtel_money_enabled', 'true'),
    ('maintenance_mode', 'false')
ON CONFLICT (setting_key) DO NOTHING;

-- ============================================================================
-- 11B. RPC FUNCTIONS & AUTOMATIC TRIGGERS FOR AUTH, TEACHER SUBJECTS & ADMIN
-- ============================================================================

-- Drop existing function signatures if column return types changed
DROP FUNCTION IF EXISTS public.register_user_account(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT);
DROP FUNCTION IF EXISTS public.register_user_account(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT);
DROP FUNCTION IF EXISTS public.authenticate_user_account(TEXT, TEXT, TEXT);

-- RPC 1: Register a user by Email & Password in public.users, sync teacher_subjects, log to admin_audit_logs, and issue session_token
CREATE OR REPLACE FUNCTION public.register_user_account(
    p_phone_number TEXT,
    p_full_name TEXT,
    p_role TEXT,
    p_password_hash TEXT,
    p_academic_class TEXT DEFAULT '',
    p_stream TEXT DEFAULT '',
    p_teacher_id_code TEXT DEFAULT NULL,
    p_subjects_taught TEXT DEFAULT '',
    p_referral_code TEXT DEFAULT NULL
)
RETURNS TABLE (
    user_id UUID,
    phone_number TEXT,
    full_name TEXT,
    role TEXT,
    academic_class TEXT,
    stream TEXT,
    teacher_id_code VARCHAR(6),
    subjects_taught TEXT,
    is_plan_active BOOLEAN,
    plan_duration TEXT,
    session_token TEXT
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user public.users%ROWTYPE;
    v_identifier TEXT;
    v_token TEXT;
    v_subj TEXT;
BEGIN
    v_identifier := lower(trim(p_phone_number));

    -- Check if an account already exists with this email or phone_number
    SELECT * INTO v_user
    FROM public.users u
    WHERE (u.email IS NOT NULL AND u.email <> '' AND lower(trim(u.email)) = v_identifier)
       OR lower(trim(u.phone_number)) = v_identifier
    LIMIT 1;

    IF FOUND THEN
        UPDATE public.users
        SET email = v_identifier,
            full_name = trim(p_full_name),
            role = lower(trim(p_role)),
            password_hash = p_password_hash,
            academic_class = COALESCE(p_academic_class, ''),
            stream = COALESCE(p_stream, ''),
            teacher_id_code = COALESCE(public.users.teacher_id_code, p_teacher_id_code),
            subjects_taught = CASE WHEN COALESCE(p_subjects_taught, '') <> '' THEN p_subjects_taught ELSE public.users.subjects_taught END,
            referral_code = COALESCE(public.users.referral_code, p_referral_code),
            updated_at = NOW()
        WHERE id = v_user.id
        RETURNING * INTO v_user;
    ELSE
        INSERT INTO public.users (
            phone_number, email, full_name, role, password_hash,
            academic_class, stream, teacher_id_code, subjects_taught, referral_code, updated_at
        )
        VALUES (
            v_identifier, v_identifier, trim(p_full_name), lower(trim(p_role)), p_password_hash,
            COALESCE(p_academic_class, ''), COALESCE(p_stream, ''), p_teacher_id_code, COALESCE(p_subjects_taught, ''), p_referral_code, NOW()
        )
        ON CONFLICT ON CONSTRAINT users_phone_number_key DO UPDATE SET
            email = EXCLUDED.email,
            full_name = EXCLUDED.full_name,
            role = EXCLUDED.role,
            password_hash = EXCLUDED.password_hash,
            academic_class = EXCLUDED.academic_class,
            stream = EXCLUDED.stream,
            teacher_id_code = COALESCE(public.users.teacher_id_code, EXCLUDED.teacher_id_code),
            subjects_taught = CASE WHEN EXCLUDED.subjects_taught <> '' THEN EXCLUDED.subjects_taught ELSE public.users.subjects_taught END,
            updated_at = NOW()
        RETURNING * INTO v_user;
    END IF;

    -- If teacher selected subjects during signup, populate public.teacher_subjects
    IF v_user.role = 'teacher' AND v_user.teacher_id_code IS NOT NULL AND COALESCE(p_subjects_taught, '') <> '' THEN
        FOREACH v_subj IN ARRAY string_to_array(p_subjects_taught, ',')
        LOOP
            IF trim(v_subj) <> '' THEN
                INSERT INTO public.teacher_subjects (teacher_id, teacher_id_code, subject_name)
                VALUES (v_user.id, v_user.teacher_id_code, trim(v_subj))
                ON CONFLICT (teacher_id_code, subject_name) DO UPDATE SET
                    teacher_id = EXCLUDED.teacher_id;
            END IF;
        END LOOP;
    END IF;

    -- Record audit log so Admin Portal -> Logs immediately shows new user signup
    INSERT INTO public.admin_audit_logs (title, description, category, level, actor, timestamp_label)
    VALUES (
        CASE WHEN v_user.role = 'teacher' THEN 'New Teacher Registered' ELSE 'New Student Registered' END,
        CASE
            WHEN v_user.role = 'teacher' THEN
                'Teacher ''' || v_user.full_name || ''' (ID: ' || COALESCE(v_user.teacher_id_code, 'N/A') || ') signed up with email ' || COALESCE(NULLIF(v_user.email, ''), v_user.phone_number) || ' for subjects: ' || COALESCE(NULLIF(v_user.subjects_taught, ''), 'General')
            ELSE
                'Student ''' || v_user.full_name || ''' (' || trim(v_user.academic_class || ' ' || v_user.stream) || ') created an account with email ' || COALESCE(NULLIF(v_user.email, ''), v_user.phone_number)
        END,
        'SECURITY',
        'INFO',
        v_user.full_name || ' (' || initcap(v_user.role) || ')',
        to_char(NOW(), 'DD Mon YYYY, HH24:MI:SS')
    );

    v_token := encode(gen_random_bytes(32), 'hex');

    INSERT INTO public.auth_sessions (user_id, phone_number, email, role, session_token)
    VALUES (v_user.id, COALESCE(NULLIF(v_user.email, ''), v_user.phone_number), COALESCE(NULLIF(v_user.email, ''), v_user.phone_number), v_user.role, v_token);

    RETURN QUERY SELECT
        v_user.id,
        COALESCE(NULLIF(v_user.email, ''), v_user.phone_number),
        v_user.full_name,
        v_user.role,
        v_user.academic_class,
        v_user.stream,
        v_user.teacher_id_code,
        v_user.subjects_taught,
        v_user.is_plan_active,
        v_user.plan_duration,
        v_token;
END;
$$;

-- RPC 2: Authenticate a user by Email (or legacy phone_number) + SHA-256 password_hash and query teacher_subjects if needed
CREATE OR REPLACE FUNCTION public.authenticate_user_account(
    p_phone_number TEXT,
    p_password_hash TEXT,
    p_device_info TEXT DEFAULT 'Cachap Android'
)
RETURNS TABLE (
    user_id UUID,
    phone_number TEXT,
    full_name TEXT,
    role TEXT,
    academic_class TEXT,
    stream TEXT,
    teacher_id_code VARCHAR(6),
    subjects_taught TEXT,
    is_plan_active BOOLEAN,
    plan_duration TEXT,
    session_token TEXT
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user public.users%ROWTYPE;
    v_identifier TEXT;
    v_token TEXT;
    v_queried_subjects TEXT;
BEGIN
    v_identifier := lower(trim(p_phone_number));

    SELECT * INTO v_user
    FROM public.users u
    WHERE (
        (u.email IS NOT NULL AND u.email <> '' AND lower(trim(u.email)) = v_identifier)
        OR lower(trim(u.phone_number)) = v_identifier
    )
      AND (u.password_hash = p_password_hash OR u.password_hash = '')
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN;
    END IF;

    -- Backfill password_hash or email if previously empty
    IF v_user.password_hash = '' OR COALESCE(v_user.email, '') = '' THEN
        UPDATE public.users
        SET password_hash = CASE WHEN password_hash = '' THEN p_password_hash ELSE password_hash END,
            email = CASE WHEN COALESCE(email, '') = '' THEN v_identifier ELSE email END,
            updated_at = NOW()
        WHERE id = v_user.id
        RETURNING * INTO v_user;
    END IF;

    -- Query public.teacher_subjects for teachers so subjects_taught is always up to date from the DB
    IF v_user.role = 'teacher' THEN
        SELECT string_agg(ts.subject_name, ', ' ORDER BY ts.created_at ASC, ts.subject_name ASC)
        INTO v_queried_subjects
        FROM public.teacher_subjects ts
        WHERE ts.teacher_id = v_user.id
           OR (v_user.teacher_id_code IS NOT NULL AND ts.teacher_id_code = v_user.teacher_id_code);

        IF v_queried_subjects IS NOT NULL AND v_queried_subjects <> '' THEN
            v_user.subjects_taught := v_queried_subjects;
            UPDATE public.users
            SET subjects_taught = v_queried_subjects, updated_at = NOW()
            WHERE id = v_user.id;
        ELSE
            UPDATE public.users SET updated_at = NOW() WHERE id = v_user.id;
        END IF;
    ELSE
        UPDATE public.users SET updated_at = NOW() WHERE id = v_user.id;
    END IF;

    v_token := encode(gen_random_bytes(32), 'hex');

    INSERT INTO public.auth_sessions (user_id, phone_number, email, role, session_token, device_info)
    VALUES (
        v_user.id,
        COALESCE(NULLIF(v_user.email, ''), v_user.phone_number),
        COALESCE(NULLIF(v_user.email, ''), v_user.phone_number),
        v_user.role,
        v_token,
        COALESCE(p_device_info, 'Cachap Android')
    );

    RETURN QUERY SELECT
        v_user.id,
        COALESCE(NULLIF(v_user.email, ''), v_user.phone_number),
        v_user.full_name,
        v_user.role,
        v_user.academic_class,
        v_user.stream,
        v_user.teacher_id_code,
        v_user.subjects_taught,
        v_user.is_plan_active,
        v_user.plan_duration,
        v_token;
END;
$$;

-- RPC 3: Revoke a custom session token on Logout
CREATE OR REPLACE FUNCTION public.revoke_auth_session(
    p_session_token TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.auth_sessions
    SET is_revoked = TRUE
    WHERE session_token = trim(p_session_token);
    RETURN FOUND;
END;
$$;

-- RPC 4: Query a teacher's assigned subjects from public.teacher_subjects + live note/assignment counts
CREATE OR REPLACE FUNCTION public.get_teacher_subjects(
    p_teacher_id_code TEXT,
    p_phone_number TEXT DEFAULT ''
)
RETURNS TABLE (
    teacher_id_code VARCHAR(6),
    subject_name TEXT,
    assigned_classes TEXT,
    note_count BIGINT,
    assignment_count BIGINT
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user public.users%ROWTYPE;
    v_subj TEXT;
    v_code VARCHAR(6);
BEGIN
    v_code := upper(trim(COALESCE(p_teacher_id_code, '')));

    -- If phone_number is provided, ensure any subjects in public.users.subjects_taught are synced into public.teacher_subjects
    IF trim(COALESCE(p_phone_number, '')) <> '' THEN
        SELECT * INTO v_user FROM public.users u WHERE u.phone_number = trim(p_phone_number) LIMIT 1;
    ELSIF v_code <> '' THEN
        SELECT * INTO v_user FROM public.users u WHERE u.teacher_id_code = v_code LIMIT 1;
    END IF;

    IF v_user.id IS NOT NULL THEN
        v_code := COALESCE(v_user.teacher_id_code, v_code);
        IF COALESCE(v_user.subjects_taught, '') <> '' AND v_code <> '' THEN
            FOREACH v_subj IN ARRAY string_to_array(v_user.subjects_taught, ',')
            LOOP
                IF trim(v_subj) <> '' THEN
                    INSERT INTO public.teacher_subjects (teacher_id, teacher_id_code, subject_name)
                    VALUES (v_user.id, v_code, trim(v_subj))
                    ON CONFLICT ON CONSTRAINT teacher_subjects_teacher_id_code_subject_name_key DO NOTHING;
                END IF;
            END LOOP;
        END IF;
    END IF;

    RETURN QUERY
    SELECT
        ts.teacher_id_code,
        ts.subject_name,
        ts.assigned_classes,
        COALESCE((
            SELECT COUNT(*)
            FROM public.notes_uploads nu
            WHERE nu.teacher_id_code = ts.teacher_id_code
              AND lower(nu.subject_name) = lower(ts.subject_name)
              AND lower(nu.content_type) = 'notes'
        ), ts.note_count::BIGINT) AS note_count,
        COALESCE((
            SELECT COUNT(*)
            FROM public.notes_uploads nu
            WHERE nu.teacher_id_code = ts.teacher_id_code
              AND lower(nu.subject_name) = lower(ts.subject_name)
              AND lower(nu.content_type) = 'assignment'
        ), ts.assignment_count::BIGINT) AS assignment_count
    FROM public.teacher_subjects ts
    WHERE ts.teacher_id_code = v_code
    ORDER BY ts.created_at ASC, ts.subject_name ASC;
END;
$$;

-- RPC 5: Update a teacher's subjects in both public.users and public.teacher_subjects
CREATE OR REPLACE FUNCTION public.sync_teacher_subjects_account(
    p_phone_number TEXT,
    p_teacher_id_code TEXT,
    p_subjects_taught TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user_id UUID;
    v_code VARCHAR(6);
    v_subj TEXT;
    v_clean_subjects TEXT[];
BEGIN
    v_code := upper(trim(COALESCE(p_teacher_id_code, '')));

    SELECT id INTO v_user_id
    FROM public.users
    WHERE (trim(p_phone_number) <> '' AND phone_number = trim(p_phone_number))
       OR (v_code <> '' AND teacher_id_code = v_code)
    LIMIT 1;

    IF v_user_id IS NOT NULL THEN
        UPDATE public.users
        SET subjects_taught = trim(p_subjects_taught),
            teacher_id_code = COALESCE(teacher_id_code, NULLIF(v_code, '')),
            updated_at = NOW()
        WHERE id = v_user_id;
    END IF;

    IF v_code <> '' AND trim(COALESCE(p_subjects_taught, '')) <> '' THEN
        v_clean_subjects := ARRAY(
            SELECT trim(x)
            FROM unnest(string_to_array(p_subjects_taught, ',')) AS x
            WHERE trim(x) <> ''
        );

        -- Remove deselected subjects for this teacher_id_code
        DELETE FROM public.teacher_subjects
        WHERE teacher_id_code = v_code
          AND NOT (subject_name = ANY(v_clean_subjects));

        -- Insert/keep selected subjects
        FOREACH v_subj IN ARRAY v_clean_subjects
        LOOP
            INSERT INTO public.teacher_subjects (teacher_id, teacher_id_code, subject_name)
            VALUES (v_user_id, v_code, v_subj)
            ON CONFLICT (teacher_id_code, subject_name) DO UPDATE SET
                teacher_id = COALESCE(public.teacher_subjects.teacher_id, EXCLUDED.teacher_id);
        END LOOP;
    END IF;

    RETURN TRUE;
END;
$$;

-- Trigger Function: When a teacher uploads a note/assignment into public.notes_uploads,
-- automatically update public.teacher_subjects counts AND add an audit log for Admin Portal
CREATE OR REPLACE FUNCTION public.on_note_upload_inserted()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Ensure teacher_subjects entry exists and update note/assignment counts
    INSERT INTO public.teacher_subjects (teacher_id, teacher_id_code, subject_name, note_count, assignment_count)
    VALUES (
        NEW.teacher_id,
        NEW.teacher_id_code,
        NEW.subject_name,
        CASE WHEN lower(NEW.content_type) = 'notes' THEN 1 ELSE 0 END,
        CASE WHEN lower(NEW.content_type) = 'assignment' THEN 1 ELSE 0 END
    )
    ON CONFLICT (teacher_id_code, subject_name) DO UPDATE SET
        note_count = (
            SELECT COUNT(*) FROM public.notes_uploads nu
            WHERE nu.teacher_id_code = NEW.teacher_id_code
              AND lower(nu.subject_name) = lower(NEW.subject_name)
              AND lower(nu.content_type) = 'notes'
        ),
        assignment_count = (
            SELECT COUNT(*) FROM public.notes_uploads nu
            WHERE nu.teacher_id_code = NEW.teacher_id_code
              AND lower(nu.subject_name) = lower(NEW.subject_name)
              AND lower(nu.content_type) = 'assignment'
        );

    -- Insert into admin_audit_logs so Admin Portal immediately logs the upload
    INSERT INTO public.admin_audit_logs (title, description, category, level, actor, timestamp_label)
    VALUES (
        CASE WHEN lower(NEW.content_type) = 'assignment' THEN 'Assessment Published' ELSE 'Study Material Published' END,
        'Teacher ''' || NEW.author_name || ''' (ID: ' || NEW.teacher_id_code || ') published ''' || NEW.title || ''' (' || NEW.file_size || ') for ' || NEW.target_class || ' ' || NEW.subject_name || '.',
        'CONTENT',
        'INFO',
        NEW.author_name || ' (Teacher)',
        to_char(NOW(), 'DD Mon YYYY, HH24:MI:SS')
    );

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_after_note_upload_insert ON public.notes_uploads;
CREATE TRIGGER trg_after_note_upload_insert
AFTER INSERT ON public.notes_uploads
FOR EACH ROW
EXECUTE FUNCTION public.on_note_upload_inserted();

-- Trigger Function: When a payment/transaction is inserted into public.payments_transactions,
-- automatically add an audit log in public.admin_audit_logs and mark student plan active
CREATE OR REPLACE FUNCTION public.on_payment_transaction_inserted()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    IF NEW.category = 'SUBSCRIPTION' AND NEW.status = 'COMPLETED' THEN
        UPDATE public.users
        SET is_plan_active = TRUE,
            plan_duration = COALESCE(NULLIF(NEW.plan_duration, ''), '1 Month'),
            plan_expiry_date = 'Valid for 30 days',
            updated_at = NOW()
        WHERE phone_number = NEW.phone_number
           OR lower(email) = lower(NEW.phone_number)
           OR lower(full_name) = lower(NEW.user_name);
    END IF;

    INSERT INTO public.admin_audit_logs (title, description, category, level, actor, timestamp_label)
    VALUES (
        NEW.provider || ' ' || initcap(NEW.category) || ' Verified',
        NEW.user_role || ' ''' || NEW.user_name || ''' completed ' || COALESCE(NEW.description, NEW.category) || ': UGX ' || NEW.amount_ugx::TEXT || '.',
        'PAYMENT',
        'PAYOUT',
        NEW.user_name || ' (' || NEW.user_role || ')',
        to_char(NOW(), 'DD Mon YYYY, HH24:MI:SS')
    );

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_after_payment_insert ON public.payments_transactions;
CREATE TRIGGER trg_after_payment_insert
AFTER INSERT ON public.payments_transactions
FOR EACH ROW
EXECUTE FUNCTION public.on_payment_transaction_inserted();

-- ============================================================================
-- 12. SUPABASE STORAGE BUCKET FOR PDF NOTES & ASSIGNMENTS
-- ============================================================================
INSERT INTO storage.buckets (id, name, public)
VALUES ('study-materials', 'study-materials', true)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 13. ROW LEVEL SECURITY (RLS), CLIENT HEADER VERIFICATION & POLICIES
-- ============================================================================
CREATE OR REPLACE FUNCTION public.is_valid_cachap_client()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
  SELECT COALESCE(
    (current_setting('request.headers', true)::json ->> 'x-cachap-client') = 'cachap-android-v2.3'
    OR (current_setting('request.headers', true) IS NULL),
    true
  );
$$;

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.auth_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.curriculum_subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teacher_subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.student_teacher_links ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notes_uploads ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.student_downloads ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.student_recent_topics ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscription_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.affiliate_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.student_streaks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.platform_settings ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if re-running script
DROP POLICY IF EXISTS "Allow full access to users" ON public.users;
DROP POLICY IF EXISTS "Allow full access to auth_sessions" ON public.auth_sessions;
DROP POLICY IF EXISTS "Allow read curriculum_subjects" ON public.curriculum_subjects;
DROP POLICY IF EXISTS "Allow full access to teacher_subjects" ON public.teacher_subjects;
DROP POLICY IF EXISTS "Allow full access to student_teacher_links" ON public.student_teacher_links;
DROP POLICY IF EXISTS "Allow full access to notes_uploads" ON public.notes_uploads;
DROP POLICY IF EXISTS "Allow full access to student_downloads" ON public.student_downloads;
DROP POLICY IF EXISTS "Allow full access to student_recent_topics" ON public.student_recent_topics;
DROP POLICY IF EXISTS "Allow full access to subscription_plans" ON public.subscription_plans;
DROP POLICY IF EXISTS "Allow full access to payments_transactions" ON public.payments_transactions;
DROP POLICY IF EXISTS "Allow full access to affiliate_transactions" ON public.affiliate_transactions;
DROP POLICY IF EXISTS "Allow full access to student_streaks" ON public.student_streaks;
DROP POLICY IF EXISTS "Allow full access to admin_audit_logs" ON public.admin_audit_logs;
DROP POLICY IF EXISTS "Allow full access to platform_settings" ON public.platform_settings;

-- Create policies allowing the mobile app (using anon/authenticated role) to read & write
CREATE POLICY "Allow full access to users" ON public.users FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to auth_sessions" ON public.auth_sessions FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow read curriculum_subjects" ON public.curriculum_subjects FOR SELECT USING (true);
CREATE POLICY "Allow full access to teacher_subjects" ON public.teacher_subjects FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to student_teacher_links" ON public.student_teacher_links FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to notes_uploads" ON public.notes_uploads FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to student_downloads" ON public.student_downloads FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to student_recent_topics" ON public.student_recent_topics FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to subscription_plans" ON public.subscription_plans FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to payments_transactions" ON public.payments_transactions FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to affiliate_transactions" ON public.affiliate_transactions FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to student_streaks" ON public.student_streaks FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to admin_audit_logs" ON public.admin_audit_logs FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow full access to platform_settings" ON public.platform_settings FOR ALL USING (true) WITH CHECK (true);

-- Storage bucket policies for 'study-materials'
DROP POLICY IF EXISTS "Public read access for study-materials" ON storage.objects;
DROP POLICY IF EXISTS "Allow uploads to study-materials" ON storage.objects;
DROP POLICY IF EXISTS "Allow deletes from study-materials" ON storage.objects;

CREATE POLICY "Public read access for study-materials"
ON storage.objects FOR SELECT
USING (bucket_id = 'study-materials');

CREATE POLICY "Allow uploads to study-materials"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'study-materials');

CREATE POLICY "Allow deletes from study-materials"
ON storage.objects FOR DELETE
USING (bucket_id = 'study-materials');
