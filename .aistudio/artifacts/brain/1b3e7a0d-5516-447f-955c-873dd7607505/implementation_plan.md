# Realtime Notification System for Cachap

A comprehensive dual-channel notification architecture combining **in-app real-time alerts** (notification bell with unread badge, animated floating top banner, and dedicated notification center) with **system push notifications** (Firebase Cloud Messaging + Supabase Realtime).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The requirements confirmed during interactive planning are reflected below.

- **Confirmed Delivery Channel**: Dual delivery — both an interactive in-app notification center (with live bell badge and floating popup banner) and Android system push notifications via Firebase Cloud Messaging (FCM).
- **Confirmed Notification Categories**:
  1. *New Study Notes*: Instant alerts when teachers share notes/assignments for the student's specific class (e.g. S.4 Geography).
  2. *Live Audio Classes*: High-priority notices when a live class is scheduled or started by a teacher.
  3. *Subscription & Payments*: Payment confirmations, receipt generation, and expiry reminder notices.
  4. *School & Admin Announcements*: Broadcast messages from administrators to all users or specific secondary school levels.
- **Confirmed In-App Presentation**: Modern header notification bell with dynamic unread badge count, paired with a spring-animated floating banner that drops down from the status bar when new events arrive in real-time.

---

## 1. Overview & Core Concept

### What It Does
The Realtime Notification System connects students, teachers, and administrators into an immediate notification loop. When teachers publish notes or start audio classes, or when mobile money payments complete, the system:
1. Pushes the record into a central `public.notifications` table in Supabase.
2. Emits real-time WebSocket events via Supabase Realtime to active apps.
3. Displays a sleek floating in-app banner for active foreground users and bumps the unread bell badge.
4. Triggers background Android system notifications via FCM with distinct notification channels and deep links directly to the relevant content (PDF viewer, live audio session, or subscription account).

### Target Audience & Persona
- **Secondary School Students (S.1 - S.6)**: Get alerted immediately when their teachers upload term notes or go live, and receive timely reminders before their UGX 2,000 monthly access expires.
- **Teachers**: Receive notifications when students submit questions, enroll, or when their classes start.
- **School Administrators**: Broadcast urgent school notices and verify payment/system audit events in real time.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                       Foreground Experience                            │
│                                                                        │
│   Event Occurs (e.g. Note Uploaded)                                   │
│           │                                                            │
│           ▼                                                            │
│   Floating Top Banner Slides Down (5s auto-dismiss or tap to open)     │
│           │                                                            │
│           ▼                                                            │
│   Bell Icon in Header increments unread count (e.g. "3")               │
│           │                                                            │
│           ▼ (Tap Bell)                                                 │
│   Notification Center opens: Filter by Notes / Classes / Payments      │
│           │                                                            │
│           ▼ (Tap Notification Card)                                    │
│   Marks as Read + Direct Deep-Link to PDF Viewer / Class / Account     │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                       Background Experience                            │
│                                                                        │
│   Event Occurs -> FCM High-Priority Push -> Android Notification Bar   │
│           │                                                            │
│           ▼ (Tap System Notification)                                  │
│   App Launches -> MainActivity routes Intent to target screen          │
└────────────────────────────────────────────────────────────────────────┘
```

### Visual Identity & Theme
- **Aesthetic Direction**: Clean educational minimalism with energetic accents and clear visual hierarchy adhering to the established Cachap brand.
- **Color Coding by Category**:
  - **Study Notes**: Indigo Purple (`#4B3F8F`) with soft lavender container (`#EDE9FE`).
  - **Live Classes**: Vibrant Coral / Crimson (`#E63946`) with soft rose container (`#FFE4E6`).
  - **Payments & Plans**: Emerald Teal (`#2A9D8F`) with mint container (`#D1FAE5`).
  - **Announcements**: Warm Amber (`#F39C12`) with soft golden container (`#FEF3C7`).
- **Typography & Layout**:
  - Bold, legible headlines with relative timestamp labels (*"Just now"*, *"12m ago"*, *"Yesterday"*).
  - Unread indicators: Pulsing purple badge on the header bell and solid accent dot on unread cards.
  - Generous 48dp touch targets on all interactive items, filter chips, and action buttons.

### Interactive Components & Motion
1. **Header Notification Bell**:
   - Subtle scale pulse when unread count increases.
   - Badge pill: Deep maroon background with white bold number (displays `9+` if count exceeds 9).
2. **Floating In-App Banner**:
   - Drops smoothly from under the top app bar using Compose `spring(dampingRatio = Spring.DampingRatioMediumBouncy)`.
   - Displays icon, category badge, title, preview text, and dismiss cross.
   - Tap opens direct destination; swipe-up gesture dismisses early.
3. **Notification Center (`NotificationScreen`)**:
   - Top Bar with back navigation, screen title, and "Mark all read" button.
   - Horizontally scrolling filter chips: *All*, *Unread*, *Notes*, *Live Classes*, *Payments*, *Announcements*.
   - Dismissible cards with swipe-to-delete or "Clear read" button.
   - Welcoming empty state illustration: *"You're all caught up! No new notifications."*

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Dual-Layer Notification Pipeline (In-App Flow + FCM Push)
- **Chosen Approach**: Combine Supabase Realtime (WebSocket `postgres_changes` / live Flow) for foreground in-app alerts and Firebase Cloud Messaging for background notifications.
- **Why**: WebSocket connections provide instantaneous (<150ms) in-app updates and allow the floating banner and bell badge to react immediately without waiting on cellular push relays. FCM ensures students never miss a note or live class even when Cachap is closed.
- **Alternative Considered**: FCM-only was considered, but FCM messages while the app is in foreground require custom local notification posting and can have variable relay latency over low-bandwidth mobile connections.

### Decision 2: Broadcast vs. Per-User Notification Storage Model
- **Chosen Approach**: Hybrid notification schema with `recipient_user_id` (nullable for targeted notifications) and `recipient_role` + `target_class` for broadcast notices, paired with a lightweight `notification_reads` tracking table.
- **Why**: Class-wide notes (e.g. S.4 Biology) and school-wide announcements should not require inserting thousands of duplicate rows into the database for each student. Instead, one broadcast record is written, and student read state is tracked individually.

### Decision 3: Local Persistence & Offline Resiliency
- **Chosen Approach**: Persist notification state and unread IDs in local cache (`SharedPreferences` / local store) so the notification badge and cached messages remain accessible immediately during offline study or network drops.
- **Why**: Ugandan secondary students often study offline or experience intermittent connectivity; the notification center should never show a blank spinner when offline.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                           SUPABASE BACKEND                             │
│                                                                        │
│   ┌─────────────────────┐    ┌──────────────────────────────────────┐  │
│   │ public.notes_uploads│───▶│ Triggers:                            │  │
│   ├─────────────────────┤    │ - on_note_upload_inserted()          │  │
│   │ payments_transactions───▶│ - on_payment_transaction_inserted()  │  │
│   ├─────────────────────┤    │ - on_live_class_created()            │  │
│   │ public.live_classes ├───▶│                                      │  │
│   └─────────────────────┘    └──────────────────┬───────────────────┘  │
│                                                 │                      │
│                                                 ▼                      │
│                                      ┌──────────────────────┐          │
│                                      │ public.notifications │          │
│                                      │ (Realtime Enabled)   │          │
│                                      └──────────┬───────────┘          │
└─────────────────────────────────────────────────┼──────────────────────┘
                                                  │
                      ┌───────────────────────────┴────────────────────────────┐
                      │                                                        │
                      ▼ (Supabase Realtime WS / API)                           ▼ (FCM Push Service)
┌──────────────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
│            ANDROID FOREGROUND UI             │       │            ANDROID BACKGROUND (FCM)          │
│                                              │       │                                              │
│  ┌────────────────────────────────────────┐  │       │  CatchUpFirebaseMessagingService             │
│  │ NotificationRepository / StateFlow     │  │       │  - onMessageReceived()                       │
│  └──────────────────┬─────────────────────┘  │       │  - NotificationChannels (Notes, Live, etc.)  │
│                     │                        │       │  - NotificationCompat.Builder                │
│         ┌───────────┴───────────┐            │       │  - Deep Link Intent -> MainActivity          │
│         ▼                       ▼            │       └──────────────────────────────────────────────┘
│  FloatingTopBanner     NotificationCenter    │
│  & Bell Badge Icon     Screen (Inbox)        │
└──────────────────────────────────────────────┘
```

### Database Schema Updates (`public/supabase_schema.sql`)
1. **`public.notifications` Table**:
   - `id`: UUID (Primary Key)
   - `recipient_user_id`: UUID (Nullable; if NULL, broadcast to target role/class)
   - `recipient_role`: TEXT ('all', 'student', 'teacher', 'admin')
   - `target_class`: TEXT ('S.1', 'S.2', 'S.3', 'S.4', 'S.5', 'S.6', 'all')
   - `type`: TEXT ('new_note', 'live_class', 'payment', 'announcement')
   - `title`: TEXT NOT NULL
   - `message`: TEXT NOT NULL
   - `reference_id`: TEXT (e.g. note ID, class ID, or transaction ID)
   - `metadata`: JSONB (contains subject name, author, deep-link extras)
   - `is_read`: BOOLEAN DEFAULT FALSE
   - `created_at`: TIMESTAMPTZ DEFAULT NOW()

2. **`public.notification_reads` Table**:
   - `id`: UUID PRIMARY KEY
   - `notification_id`: UUID REFERENCES public.notifications(id) ON DELETE CASCADE
   - `user_id`: UUID REFERENCES public.users(id) ON DELETE CASCADE
   - `read_at`: TIMESTAMPTZ DEFAULT NOW()
   - `UNIQUE(notification_id, user_id)`

3. **Supabase Realtime Publication**:
   - `ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;`

4. **Automated Notification Triggers**:
   - `trg_after_note_upload_insert`: Creates notification for `target_class` whenever a teacher publishes a note.
   - `trg_after_payment_insert`: Creates a personal payment receipt/confirmation notification for the paying user.
   - `trg_after_live_class_insert`: Creates live class alert for enrolled class.

### Kotlin Client Implementation Components
1. **`com.example.data.NotificationModels.kt`**:
   - `NotificationItem`: id, type, title, message, timestamp, isRead, referenceId, subjectName, deepLinkAction.
   - `NotificationFilter`: ALL, UNREAD, NOTES, LIVE_CLASSES, PAYMENTS, ANNOUNCEMENTS.
2. **`com.example.data.NotificationRepository.kt`**:
   - Singleton managing local cache, Supabase fetch, Realtime subscription, and FCM token sync.
   - Exposes `unreadCount: StateFlow<Int>` and `bannerEvent: SharedFlow<NotificationItem?>`.
   - `markAsRead(id: String)`, `markAllAsRead()`, `deleteNotification(id: String)`.
3. **`com.example.ui.notifications.NotificationComponents.kt`**:
   - `NotificationBellButton(unreadCount: Int, onClick: () -> Unit)`
   - `FloatingNotificationBanner(item: NotificationItem, onDismiss: () -> Unit, onOpen: (NotificationItem) -> Unit)`
4. **`com.example.ui.notifications.NotificationCenterScreen.kt`**:
   - Full-featured inbox with filtering, read status, direct deep-link actions, and swipe to dismiss.
5. **`MainActivity.kt` & Top Bars**:
   - Embed `NotificationBellButton` into `AppHeader` across Student, Teacher, and Admin home screens.
   - Overlay `FloatingNotificationBanner` at the top of the root `Scaffold`.
   - Wire `AppScreen.NOTIFICATIONS` into the main navigation flow.
   - Connect deep links from system notifications and banner clicks to `PdfViewerScreen`, `StudentLiveSessionScreen`, and `AccountScreen`.
6. **Admin Broadcast Tool (`AdminBroadcastDialog` / Screen)**:
   - Form in Admin Portal allowing admins to push instant announcements to all students or specific classes.

---

## Verification Plan

### Automated & Build Verification
1. Run `compile_applet` to ensure all new Composables, repositories, models, and deep-link handlers compile cleanly without syntax or version catalog issues.
2. Verify Kotlin serialization and Supabase JSON parsing for `NotificationItem` and metadata payload.

### Functional Verification
1. **In-App Realtime Notification Simulation & Display**:
   - Send test alerts across all four categories (New Note, Live Class, Payment, Announcement).
   - Verify that the floating banner drops down smoothly from the top, stays visible for 5 seconds, and responds to click or dismiss.
   - Verify the bell icon reflects the correct unread badge count (and transitions to 0 when "Mark all as read" is tapped).
2. **Deep Link Traversal**:
   - Tapping a "New Note" card opens the PDF Viewer for that subject/topic.
   - Tapping a "Live Class" card navigates directly to the live session or student live classes.
   - Tapping a "Payment" card navigates to the Student Account / Plan tier screen.
3. **FCM Token Synchronization**:
   - Verify `saveFcmToken` updates `public.users.fcm_token` so backend services can target the student device.
