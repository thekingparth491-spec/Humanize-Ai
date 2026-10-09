# HumanizeAI Backend Microservice

A production-ready FastAPI service providing endpoints for AI Text Detection, Humanization, AI Style Conversion, and Text Improvement.

## Requirements

- Python 3.9+
- pip

## Quickstart

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Configure environment:
```bash
cp .env.example .env
# Edit .env and supply your GEMINI_API_KEY
```

3. Run the development server:
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

## Available Endpoints

- `GET  /api/health` - Service health check
- `POST /api/analyze` - Linguistic burstiness & perplexity text analysis
- `POST /api/humanize` - Transform rigid text into natural human prose
- `POST /api/ai-style` - Convert notes into structured AI formats
- `POST /api/improve` - Grammar, rephrase, summarize, expand, or translate

## Connecting with Android App

1. Ensure the backend server is running.
2. In the Android app, go to **Settings**.
3. Under **Custom Backend REST Server**, enter:
   - For Android Emulator: `http://10.0.2.2:8000`
   - For Physical Device: `http://<your-local-ip>:8000`
4. Tap **Test Backend Health**.
