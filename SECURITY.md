# Security notes

- Never commit API keys, Cloudflare secrets, access tokens, or signing keystores.
- The backend requires the `APP_TOKEN` Worker secret. Set it with `wrangler secret put APP_TOKEN` after deployment.
- Enter the matching app token only in the installed app's Tools settings. It is encrypted at rest using an AES-GCM key held by Android Keystore.
- The model-provider API key, if a provider requires one, belongs only in server-side secret storage. This app should never send a provider key from the device.
- The app communicates only with HTTPS endpoints.
- Saved memories, notes, tasks, and recent conversation history are sent to the configured backend when chatting. Do not store passwords, one-time codes, or banking details in the app.
- Calls and text messages require the user's interaction in Android's own app. The app does not request direct calling or SMS-send permission.
- No remote-control Accessibility service is included in this release.
