# HumanizeAI - AI Text Checker & Humanizer Android Application

**HumanizeAI** is a production-quality Android application built with Kotlin and Jetpack Compose (Material 3). It features real text analysis, burstiness & perplexity heuristics, style conversion, and authentic human text rewriting.

---

## Architecture & Tech Stack

- **Platform:** Android 8.0+ (API 26+)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3 (Dark Navy, Violet, and Cyan glowing design system)
- **Architecture:** MVVM with Repository Pattern
- **Persistence:** Local Room Database for session history and preferences
- **Networking:** Retrofit 2 + Moshi for REST Backend communication; OkHttp for direct Google Gemini AI API (`gemini-3.5-flash`)
- **Linguistic Engine:** Mathematical burstiness (sentence length variance), lexical diversity (Type-Token Ratio), formulaic transition trope density, and sentence attribution scoring.

---

## Main Features

### 1. AI Text Checker
- Evaluates text authorship using stylometric variance, sentence burstiness, and lexical entropy.
- **Dual Likelihood Metrics:** Estimated AI-written percentage and Estimated human-written percentage with circular progress gauge.
- **Detailed Statistics:** Total word count, character count, sentence count, paragraph count, and burstiness standard deviation.
- **Sentence-Level Highlighting:** Interactive colored cards indicating AI-likely (red), mixed (amber), and human-likely (green) sentences with expandable analytical rationale.
- **Honest Limitations Disclosure:** Clarifies that scores are experimental estimates and highlights real-world false positive considerations.

### 2. Humanize Text
- Transforms rigid or AI-generated writing into natural human prose while strictly preserving facts, names, numbers, dates, and technical terminology.
- **9 Rewriting Styles:** Natural & Conversational, Simple English, Student-Friendly, Professional but Natural, Friendly & Casual, Academic & Readable, Hindi (हिन्दी), Hinglish, and Custom Instructions.
- **Fine-Tuning Controls:** Tone, Reading Difficulty, Sentence Rhythm, and Vocabulary Simplicity.
- Side-by-side original and rewritten comparison with Copy, Share, Rewrite Again, and Replace Input functions.

### 3. AI Style Converter
- Transforms rough notes into structured AI prose:
  - Formal AI Assistant
  - Structured Professional (executive summary & bullets)
  - Technical Explanatory
  - Academic-Style Prose
  - Concise Informational
  - Detailed Structured AI
- Adjustable formality, detail level, and vocabulary.

### 4. Text Improvement Tool
- Grammar correction and syntax fixing.
- Spelling and typo rectification.
- Sentence rephrasing and flow enhancement.
- Concise text summarization.
- Short text expansion.
- Multilingual translation (Spanish, French, German, Hindi, Japanese, Chinese, Italian, Portuguese, etc.).
- Paragraph restructuring.

### 5. Local History & Privacy
- On-device Room database stores past analyses and rewritten drafts.
- Search query filtering and tool type filter chips.
- Reopen, copy, individual delete, and clear-all actions.
- Local storage toggle to disable history anytime.
- Ephemeral processing disclosure ensuring text is never permanently stored on remote servers.

---

## Backend Integration

The app supports a dual-engine architecture:
1. **Direct AI Integration:** Automatically connects to Gemini API using `BuildConfig.GEMINI_API_KEY` (configured in the AI Studio Secrets panel or `.env`).
2. **On-Device Linguistic Fallback:** Built-in stylometric burstiness engine operates offline with zero external network dependencies.
3. **Dedicated FastAPI Microservice:** A complete Python backend is provided in the `/backend` folder:
   - `POST /api/analyze`
   - `POST /api/humanize`
   - `POST /api/ai-style`
   - `POST /api/improve`
   - `GET  /api/health`

### Running the Python Backend
```bash
cd backend
pip install -r requirements.txt
cp .env.example .env
# Set your GEMINI_API_KEY in .env
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

In the Android app **Settings** screen, configure the Backend URL to `http://10.0.2.2:8000` (for Android emulator) or `http://<your-local-ip>:8000` (for physical device) and tap **Test Backend Health**.

---

## Build & Testing Instructions

### Compiling and Testing
Run tests using Gradle:
```bash
gradle :app:testDebugUnitTest
```

### Generating Debug APK
```bash
gradle :app:assembleDebug
```
The debug APK is generated at: `app/build/outputs/apk/debug/app-debug.apk`.

### Generating Release APK
```bash
gradle :app:assembleRelease
```
The release APK is generated at: `app/build/outputs/apk/release/app-release.apk`.
