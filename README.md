# Iliya AI — Native Android Assistant

**Iliya AI** is a native Kotlin Android app for the Redmi Note 12 4G and other Android 8.0+ devices. It is not a website, PWA, or WebView wrapper.

This repository contains the Android application, a small Cloudflare Workers AI backend, JVM unit tests, and a GitHub Actions workflow that builds an installable debug APK.

## What is included

- Persian-first interface with a Persian / English toggle and RTL/LTR layout.
- Online AI chat through a configurable HTTPS backend. No model-provider API key is embedded in the APK.
- Conversation history saved locally on the phone.
- A user-editable personal memory list, local notes, and task list.
- Saved memory, notes, and open tasks are sent to the AI backend with each chat request so answers can use that context. The app explains this in the Memory screen.
- Voice input through Android SpeechRecognizer, and spoken replies through Android TextToSpeech. Available languages and offline recognition depend on the speech engine installed on the phone.
- Local reminders using AlarmManager and Android notifications. Reminders are restored after reboot. Android battery management can still delay inexact alarms.
- Direct phone calls and carrier SMS from explicit Persian or English chat commands, including from recognized voice commands. Examples: `به مامان زنگ بزن` or `به مامان پیام بده سلام، خوبی؟`. Contact names are resolved through the phone's contacts when Contacts permission is granted; numeric phone numbers can be used instead.
- Safe shortcuts for web search, maps, calendar event creation, and sharing text.
- A data-export action for notes, memories, tasks, and recent conversations.
- HTTPS endpoint configuration and optional app access token. The token is encrypted at rest with Android Keystore and is not part of source code.
- GitHub Actions workflow that runs JVM tests, builds the debug APK, runs Android lint, and uploads the APK as a workflow artifact.

## What this version does not claim to do

This is a native personal-assistant foundation, not a fully autonomous Jarvis replacement. Direct calls and SMS are triggered only by explicit imperative commands entered or dictated by the user; the app does not initiate communications on its own. Android's one-time runtime permission dialogs for Contacts, Phone, and SMS still apply. SMS delivery depends on the SIM, carrier, plan, and device support. WhatsApp and Telegram remain app-opening shortcuts; they are not silently automated. The app does not read notification contents, access banking accounts, or transfer money. It does not include the JARVIS project's 45-tool executor, local llama.cpp runtime, or Accessibility-driven screen automation.

## Build an APK with GitHub Actions

1. Extract this ZIP. The files inside `iliya-ai-android/` are the repository root; upload the contents of that folder, not the ZIP as a single file.
2. Create a new repository on GitHub. A private repository is fine; do not upload passwords, API keys, tokens, signing keys, or private user data.
3. Upload the extracted project files and commit them to the `main` branch. Keep `.github/workflows/android.yml` in that exact path.
4. Open the repository's **Actions** tab. The workflow named **Build Iliya AI Android APK** starts on push to `main`.
5. When the workflow finishes successfully, open the run and download the artifact named `iliya-ai-debug-apk` near the bottom of the page.
6. Extract the downloaded artifact ZIP. It contains `app-debug.apk`, which can be installed on the phone. Android may ask you to permit installation from the browser or file manager you used.

You can also start a build manually from **Actions → Build Iliya AI Android APK → Run workflow**.

The workflow installs JDK 17, Android SDK Platform 35, and Gradle 8.9 in GitHub's hosted runner. No PC or Android SDK installation is required on your phone. GitHub Actions usage is subject to the limits of your GitHub plan; public repositories generally provide the most generous free usage. Check GitHub's current plan limits before changing repository visibility.

## Build locally

Requirements: JDK 17, Android SDK Platform 35, Android Build Tools 35.0.0, and Gradle 8.9.

```bash
gradle --no-daemon :app:testDebugUnitTest
gradle --no-daemon :app:assembleDebug
gradle --no-daemon :app:lintDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

This project currently does not include a Gradle Wrapper JAR. The GitHub Actions workflow installs the pinned Gradle distribution directly. Android Studio can import the project once a local Gradle 8.9 installation is available.

## AI backend

The Android app's default endpoint is the Cloudflare Worker previously configured for this project:

```text
https://iliya-assistant.nova-b75a23.workers.dev/api/chat
```

The endpoint can be changed from **Tools → AI connection settings** inside the app.

A matching backend source is included in `backend/`. To deploy your own Worker:

1. Install the Cloudflare Wrangler CLI and sign in to your own Cloudflare account.
2. From the `backend/` directory, deploy the Worker:

   ```bash
   npx wrangler deploy
   ```

3. Create a private high-entropy app token and store it as a Worker secret (not in GitHub):

   ```bash
   npx wrangler secret put APP_TOKEN
   ```

4. Set the same token in the installed Iliya AI app from **Tools → AI connection settings**. The app encrypts it using Android Keystore. Never paste a model API key into the Android app.
5. If Wrangler deployed the Worker at a different URL, replace the endpoint in the same settings screen.

The backend expects a Workers AI binding named `AI`. The included `wrangler.toml` declares that binding and uses `@cf/meta/llama-3.1-8b-instruct-fast`, which Cloudflare documents as its fast multilingual Llama 3.1 variant ([model documentation](https://developers.cloudflare.com/workers-ai/models/llama-3.1-8b-instruct-fast/)). The separate `fp8` model family has a planned deprecation on November 20, 2026, so this project deliberately does not use that `fp8` ID ([Cloudflare changelog](https://developers.cloudflare.com/changelog/post/2026-11-05-planned-model-deprecations/)). Model availability can change; check Cloudflare's model catalog before future upgrades.

Workers AI inference is usage-based and may incur Cloudflare charges depending on your account's included quota and usage. This source package and GitHub build workflow do not buy anything or activate a paid subscription. Check your Cloudflare usage limits before using the hosted AI backend; the app's local notes, tasks, memories and reminders remain usable without sending a chat request.

**Important:** the older Worker URL configured by earlier project work may not have the token check from the new `backend/worker.js`. The new backend source requires `APP_TOKEN`; deploying it and setting the token is the safer setup. Do not publish a shared backend secret in the repository.

## Privacy and permissions

The app requests only:

- `INTERNET` for AI chat and web services.
- `RECORD_AUDIO` when the user starts voice input.
- `POST_NOTIFICATIONS` for reminder notifications on Android 13+.
- `RECEIVE_BOOT_COMPLETED` to restore saved reminders after device restart.
- `READ_CONTACTS` only when a contact name needs to be resolved to a phone number.
- `CALL_PHONE` only when placing a direct call from an explicit command.
- `SEND_SMS` only when sending a direct carrier SMS from an explicit command.

Android displays its runtime permission dialog the first time each protected action is needed. Once granted, a clearly dictated or typed command such as `به مامان زنگ بزن` or `به مامان پیام بده سلام` can place the call or submit the SMS without an extra in-app confirmation screen. The assistant does not continuously listen in the background. Calls and SMS should be used only for intended recipients; the app does not send messages automatically without a user command. Notes, tasks, memories, and conversation history are stored in app-private preferences with Android backup disabled. Memories, notes, open tasks, recent chat history, and the current message are sent to the configured AI backend when a chat request is made.

## Technical details

- Package: `com.iliya.assistant` (debug build application ID: `com.iliya.assistant.debug`)
- Minimum Android: 8.0 (API 26)
- Compile / target SDK: 35
- Language: Kotlin
- UI: native Android Views; no WebView
- Backend: Cloudflare Workers AI (optional to redeploy)
- Tests: JUnit 4 JVM tests for endpoint validation and input sanitization

## JARVIS reference

The open-source project [ansaribilal14/JARVIS](https://github.com/ansaribilal14/jarvis) was reviewed as an architectural reference for local-first storage, voice interaction, risk-aware user confirmation, and honest reporting. This package does not copy JARVIS source files or claim its complete agent runtime. The MIT-licensed project can be integrated further after a separate dependency, permission, and device-compatibility review; its source and license remain separate.
