# Teacher Student & Stream Management System

A dedicated, class-and-stream hierarchy for Cachap teachers to seamlessly discover, browse, and identify their connected students through vibrant pill cards and streamlined navigation.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were aligned during the interactive interview and will guide this implementation:

- **Confirmed Entry Point**: Teachers navigate into student management directly by clicking the existing **Students** stat tile on the Teacher Home Screen, preserving the current bottom navigation and header without disruption.
- **Confirmed Data Scope**: Groups and counts are filtered exclusively to students connected to the teacher's 6-character Teacher ID code (from Supabase `student_teacher_links` and `users`), ensuring teachers only view their authorized students.
- **Confirmed Pill Card Aesthetic**:
  - Class cards (Senior 1 through Senior 6 / S1–S6) styled as wide pill cards (around 4cm / ~150–160dp width) with unique theme colors (teal, coral, purple, navy, deep maroon, mustard).
  - Stream cards dynamically generated from connected student records (e.g. North, Blue, West, White) in distinct contrasting palette colors, displaying real-time student counts.
  - Student item cards matching the high-contrast, rounded card aesthetic established in the Admin Users screen (avatar initial circles, full name, contact/identifier, and stream badge).
  - Clean back-navigation and breadcrumb context ensuring teachers never lose their place when returning from student lists to stream or class views.
- **Dedicated Subjects Screen Integration**: The existing "My Subjects" page is enriched with class and stream group linkages for academic uploads without breaking existing notes or assignments.

---

## 1. Overview & Core Concept

- **What It Does**: Adds a multi-tier student directory to the Teacher section:
  1. **Teacher Classes Page**: Displays secondary classes (S.1 through S.6) as colorful pill cards showing total connected students in each class, with search functionality.
  2. **Class Streams Page**: Clicking a class opens its active streams (e.g., Blue, North, East, White), dynamically created from registered students connected to this teacher. If no students have connected yet, an empty state is shown.
  3. **Stream Student List Page**: Displays the exact students belonging to that specific class and stream using the refined card style from the Admin Users screen, with instant search by name. Clicking a student reveals their profile details.
  4. **Subject & Stream Context**: Reuses existing Supabase endpoints and links smoothly with teaching materials.
- **Target Audience / Persona**: Secondary school teachers managing hundreds of students across multiple classes and streams in the Ugandan curriculum (NCDC / UNEB).
- **Key Value**: Replaces guesswork and flat lists with an intuitive class → stream → student drill-down that updates automatically as new students register and link to the teacher.

---

## 2. User Experience & Visual Design

### Key User Flows

```
Teacher Home Screen
       │
       ▼ (Tap "Students" Stat Card)
Teacher Classes Screen (S1 - S6 Pill Cards + Class Search)
       │
       ▼ (Tap Class, e.g. "S.1")
Class Streams Screen (Dynamically Grouped Stream Pills + Stream Search)
       │
       ▼ (Tap Stream, e.g. "BLUE")
Stream Student List Screen (Student Cards styled like Admin Users + Name Search)
       │
       ▼ (Tap Student Card)
Student Profile Sheet / Dialog (Authorized info: name, class, stream, phone/email, linked date)
```

### Visual Identity & Theme
- **Color Differentiation**:
  - S1–S6 Class cards feature distinct pastel-tinted and solid accent borders/gradients (`#2A9D8F` Teal, `#E76F51` Coral, `#4361EE` Indigo, `#7C4DFF` Purple, `#E67E22` Amber, `#D81B60` Rose).
  - Stream cards utilize secondary high-contrast badges (Ocean Blue, Mint Emerald, Sunset Orange, Lavender).
  - Student cards adopt the polished Admin Users card layout: deep purple gradient background, crisp white circular avatar with uppercase initials, white typography, and subtle stream badge.
- **Layout Rhythm & Density**:
  - Search bar placed at the top of each view (`AdminSearchBar` style with rounded corners and magnifying glass icon).
  - Class and stream pills rendered in a responsive grid or flexible staggered layout respecting the ~4cm visual pill width.
  - Generous 16dp edge padding and 12dp spacing conforming to Material 3 touch targets (48dp minimum).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dynamic Stream Aggregation from Live Records**
  - *Chosen Approach*: Stream pill cards are dynamically derived from `connectedStudents` matching the selected class. If a student signs up with stream "WHITE", the system instantly reflects the "WHITE" stream card with its count without requiring manual admin configuration.
  - *Why*: Eliminates rigid static configurations and allows schools with arbitrary stream naming schemes (colors, directions, houses) to work out-of-the-box.
  - *Alternatives Considered*: Hardcoding a fixed list of streams was rejected because Ugandan schools use varied stream nomenclatures (East/West/North, Red/Blue/Green, Crane/Dove, etc.).

- **Decision 2: Stateful Navigation via BackHandler & Screen State Stack**
  - *Chosen Approach*: Maintain back-stack state in `MainActivity` (`AppScreen.TEACHER_CLASSES`, `AppScreen.TEACHER_CLASS_STREAMS`, `AppScreen.TEACHER_STREAM_STUDENTS`) or encapsulated multi-pane state with Android `BackHandler` integration.
  - *Why*: Guarantees that pressing the device back button or top-bar arrow navigates strictly: Student List → Streams → Classes → Teacher Home, preserving selected class and stream context.
  - *Alternatives Considered*: Replacing the bottom navigation tab would violate the prompt constraint not to restructure existing navigation.

- **Decision 3: Zero Disruption to Live Classes and Note Uploads**
  - *Chosen Approach*: Keep `TeacherLiveClassesScreen`, `TeacherLiveSessionScreen`, and `TeacherUploadNotesScreen` fully intact, while passing class and stream helpers to allow targeted uploads where applicable.
  - *Why*: Strictly satisfies the non-disruption instructions.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Component & Navigation Architecture

```
┌────────────────────────────────────────────────────────┐
│                   MainActivity.kt                      │
│   (Manages AppScreen state, connectedStudents list)    │
└──────────────────────────┬─────────────────────────────┘
                           │
       ┌───────────────────┼───────────────────┐
       ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌─────────────────┐
│ TeacherHome  │   │TeacherClasses│   │ TeacherSubjects │
│    Screen    │   │    Screen    │   │     Screen      │
└──────┬───────┘   └───────┬──────┘   └─────────────────┘
       │ (Tap Stat Card)   │
       └──────────────────►│
                           ▼ (Select S1..S6)
               ┌────────────────────────┐
               │TeacherClassStreamsScrn │
               └───────────┬────────────┘
                           ▼ (Select Stream)
               ┌────────────────────────┐
               │TeacherStreamStudentsScrn│
               └───────────┬────────────┘
                           ▼ (Select Student)
               ┌────────────────────────┐
               │ StudentProfileDialog   │
               └────────────────────────┘
```

### Data Layer & State Flow
- **Source of Truth**: Supabase `student_teacher_links` and `users` tables, synced through `SupabaseRepository.fetchConnectedStudentsForTeacher(teacherIdCode)`.
- **Filtering Logic**:
  - Class student count: `connectedStudents.count { normalizeSecondaryClass(it.studentClass) == classId }`
  - Streams for Class: `connectedStudents.filter { normalizeSecondaryClass(it.studentClass) == classId }.map { normalizeStream(it.stream) }.distinct()`
  - Students in Stream: `connectedStudents.filter { normalizeSecondaryClass(it.studentClass) == classId && normalizeStream(it.stream) == streamName }`
- **Search Support**: Immediate reactive filtering by class label, stream name, or student full name in memory.

---

## Verification & Validation Plan

1. **Compilation Check**: Run `compile_applet` to confirm zero compilation errors.
2. **Navigation Flow Verification**: Verify clicking the Students stat card on Teacher Home opens Teacher Classes, selecting S1 opens streams, selecting a stream opens student pills, and back buttons traverse back step-by-step.
3. **Data Integrity Verification**: Verify that registered students connected to the teacher code are accurately grouped and counted, and that empty states appear when a class or stream has 0 connected students.
