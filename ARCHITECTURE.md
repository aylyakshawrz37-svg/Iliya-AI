# Architecture

## Android app

`MainActivity` owns the native UI and page navigation. `SharedPreferences` stores user-created tasks, notes, memories, language choice, endpoint configuration, and a bounded conversation history. `AssistantCore` contains platform-independent validation/sanitization helpers that are unit-tested. `ReminderStore` persists reminder records; `ReminderScheduler` schedules inexact idle-friendly alarms; `BootReceiver` restores them after system restart; `ReminderReceiver` posts notifications.

Network requests run on a single-thread executor. Each request receives immutable snapshots of chat history, saved memories, notes, and open tasks so UI edits cannot mutate the context while the request is running. The AI endpoint must use HTTPS and return a JSON object containing a non-empty `reply` string.

## Backend

`backend/worker.js` validates the request, requires a server-side `APP_TOKEN`, adds trusted assistant instructions, treats user-saved memory as data rather than instructions, and calls a Cloudflare Workers AI model through the `AI` binding. The token is supplied by the user after installation and encrypted on the Android device with Android Keystore. It is never committed to the repository or embedded into the APK.

## Permissions and user confirmation

The app does not implement an Accessibility service. Call actions open `ACTION_DIAL`; SMS actions open `ACTION_SENDTO`; calendar creation opens the system calendar insert flow; sharing uses Android's chooser. The user completes or cancels those actions in the destination app.


## Direct call and SMS commands

The chat input recognizes only explicit imperative commands in Persian and English. A phone number may be supplied directly, or a contact name can be resolved using Android Contacts Provider after `READ_CONTACTS` is granted. Calls use `ACTION_CALL` after `CALL_PHONE`; carrier SMS uses `SmsManager` after `SEND_SMS`. Android's runtime permission prompts are unavoidable on first use, but there is no additional in-app confirmation dialog after the user submits the command. WhatsApp and Telegram remain external app shortcuts and are not automated.
