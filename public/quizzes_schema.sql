-- ============================================================================
-- CACHAP QUIZZES & QUIZ QUESTIONS SCHEMA
-- Run this in your Supabase Dashboard -> SQL Editor
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject TEXT NOT NULL,
    title TEXT NOT NULL,
    topic TEXT NOT NULL,
    level TEXT DEFAULT 'S.4',
    time_limit_minutes INTEGER DEFAULT 10,
    published BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quizzes_subject ON public.quizzes(subject);
CREATE INDEX IF NOT EXISTS idx_quizzes_published ON public.quizzes(published);

CREATE TABLE IF NOT EXISTS public.quiz_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES public.quizzes(id) ON DELETE CASCADE,
    position INTEGER NOT NULL DEFAULT 0,
    type TEXT NOT NULL DEFAULT 'multiple_choice' CHECK (type IN ('multiple_choice', 'true_false', 'short_answer')),
    question TEXT NOT NULL,
    options JSONB DEFAULT '[]'::jsonb,
    answer_index INTEGER DEFAULT 0,
    answer_bool BOOLEAN DEFAULT TRUE,
    explanation TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_questions_quiz_id ON public.quiz_questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_quiz_questions_position ON public.quiz_questions(quiz_id, position);

-- Enable RLS & Policies
ALTER TABLE public.quizzes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quiz_questions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow read quizzes" ON public.quizzes;
CREATE POLICY "Allow read quizzes" ON public.quizzes FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow read quiz_questions" ON public.quiz_questions;
CREATE POLICY "Allow read quiz_questions" ON public.quiz_questions FOR SELECT USING (true);

-- Sample initial quizzes & questions for testing
INSERT INTO public.quizzes (id, subject, title, topic, level, time_limit_minutes, published)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Chemistry', 'Periodic Table & Atomic Structure', 'Atomic Theory', 'S.4', 15, true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Math', 'Algebra & Linear Equations Challenge', 'Algebra', 'S.3', 10, true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Physics', 'Newton''s Laws & Force Mechanics', 'Mechanics', 'S.4', 12, true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Biology', 'Cell Structure, Organelles & Functions', 'Cytology', 'S.2', 10, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.quiz_questions (quiz_id, position, type, question, options, answer_index, answer_bool, explanation)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 1, 'multiple_choice', 'What is the atomic number of Carbon?', '["6", "12", "14", "8"]'::jsonb, 0, null, 'Carbon has 6 protons, giving it an atomic number of 6.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 2, 'true_false', 'Isotopes of an element have the same number of neutrons but different number of protons.', '["True", "False"]'::jsonb, null, false, 'Isotopes have the SAME number of protons but a DIFFERENT number of neutrons.'),
    
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 1, 'multiple_choice', 'Solve for x: 3x - 7 = 14', '["x = 5", "x = 7", "x = 8", "x = 21"]'::jsonb, 1, null, 'Adding 7 to both sides gives 3x = 21. Dividing by 3 yields x = 7.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 2, 'true_false', 'The slope of a vertical line is zero.', '["True", "False"]'::jsonb, null, false, 'The slope of a vertical line is undefined, whereas a horizontal line has a slope of zero.'),

    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 1, 'multiple_choice', 'Which law states that F = m * a?', '["Newton''s First Law", "Newton''s Second Law", "Newton''s Third Law", "Law of Gravitation"]'::jsonb, 1, null, 'Newton''s Second Law states that force equals mass times acceleration.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 2, 'true_false', 'Weight is a scalar quantity.', '["True", "False"]'::jsonb, null, false, 'Weight is a force pointing towards the Earth''s center, making it a vector quantity.'),

    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 1, 'multiple_choice', 'Which organelle is known as the powerhouse of the cell?', '["Nucleus", "Ribosome", "Mitochondrion", "Golgi Apparatus"]'::jsonb, 2, null, 'Mitochondria produce ATP through cellular respiration.'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 2, 'true_false', 'Plant cells possess a cell wall made of cellulose.', '["True", "False"]'::jsonb, null, true, 'Plant cell walls are composed primarily of cellulose microfibrils.')
ON CONFLICT DO NOTHING;
