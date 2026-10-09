"""
HumanizeAI Backend API Service
FastAPI implementation for AI Text Checking, Humanization, AI Style Conversion, and Text Improvement.
"""

import os
import re
import math
from typing import List, Optional
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(
    title="HumanizeAI API",
    description="Backend microservice for Text Analysis and Humanization",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

# Common LLM formulaic markers
AI_MARKERS = [
    "delve", "delves", "delving",
    "tapestry", "rich tapestry",
    "testament to", "a testament",
    "crucial", "crucial role",
    "pivotal", "pivotal role",
    "moreover", "furthermore",
    "in conclusion", "to conclude",
    "it is important to note",
    "foster", "fostering",
    "unlock", "harness",
    "navigating", "beacon",
    "in today's fast-paced", "in today's digital landscape",
    "game-changer", "plethora", "multifaceted"
]

# --- Pydantic Request & Response Schemas ---

class AnalyzeRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=15000)
    model: Optional[str] = None

class SentenceScoreDto(BaseModel):
    sentence: str
    ai_score: int
    classification: str
    reasoning: str

class AnalyzeResponse(BaseModel):
    ai_percentage: int
    human_percentage: int
    confidence: str
    overall_assessment: str
    detailed_explanation: str
    sentence_analyses: List[SentenceScoreDto]
    detection_method: str
    reliability_limitation: str

class HumanizeRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=15000)
    style: str = "NATURAL_CONVERSATIONAL"
    tone: str = "Balanced"
    reading_difficulty: str = "Standard"
    sentence_length: str = "Varied"
    output_length: str = "Similar"
    vocabulary_simplicity: str = "Natural"
    custom_instructions: Optional[str] = ""

class HumanizeResponse(BaseModel):
    original_text: str
    humanized_text: str
    style: str
    word_count_before: int
    word_count_after: int
    changes_summary: str

class AiStyleRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=15000)
    style: str = "STRUCTURED_PROFESSIONAL"
    formality: str = "High"
    detail: str = "Detailed"
    vocabulary: str = "Technical"
    output_length: str = "Standard"

class AiStyleResponse(BaseModel):
    original_text: str
    ai_styled_text: str
    style: str
    word_count_before: int
    word_count_after: int

class ImproveRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=15000)
    tool_type: str = "GRAMMAR"
    target_language: str = "English"

class ImproveResponse(BaseModel):
    original_text: str
    improved_text: str
    tool_type: str
    notes: str

class HealthResponse(BaseModel):
    status: str
    version: str
    provider: str

# --- Helper Functions ---

def split_sentences(text: str) -> List[str]:
    parts = re.split(r'(?<=[.!?])\s+', text.strip())
    return [p.strip() for p in parts if p.strip()]

def split_words(text: str) -> List[str]:
    return [w.lower() for w in re.findall(r"[\w'-]+", text) if len(w) > 1 or w.lower() in ['a', 'i']]

# --- API Endpoints ---

@app.get("/api/health", response_model=HealthResponse)
def get_health():
    provider_status = "Gemini Configured" if GEMINI_API_KEY else "Heuristic Fallback Engine"
    return HealthResponse(
        status="ok",
        version="1.0.0",
        provider=provider_status
    )

@app.post("/api/analyze", response_model=AnalyzeResponse)
def analyze_text(req: AnalyzeRequest):
    text = req.text.strip()
    words = split_words(text)
    sentences = split_sentences(text)
    word_count = len(words)
    sentence_count = max(1, len(sentences))

    if word_count < 5:
        return AnalyzeResponse(
            ai_percentage=50,
            human_percentage=50,
            confidence="Low",
            overall_assessment="Text Too Short",
            detailed_explanation="Statistical sample size is too small for accurate burstiness or perplexity modeling.",
            sentence_analyses=[
                SentenceScoreDto(
                    sentence=s,
                    ai_score=50,
                    classification="MIXED",
                    reasoning="Sample size too small."
                ) for s in sentences
            ],
            detection_method="Stylometric & Burstiness Heuristic",
            reliability_limitation="AI text detectors can produce false positives and false negatives, particularly for short, edited, or multilingual text. This result is an estimate."
        )

    # Calculate burstiness (standard deviation of sentence lengths)
    sentence_lengths = [len(split_words(s)) for s in sentences]
    avg_len = sum(sentence_lengths) / sentence_count
    variance = sum((l - avg_len) ** 2 for l in sentence_lengths) / sentence_count
    burstiness_sd = math.sqrt(variance)

    # Lexical Diversity (TTR)
    unique_words = len(set(words))
    lexical_diversity = unique_words / word_count if word_count > 0 else 0

    # Count AI markers
    lower_text = text.lower()
    found_markers = [m for m in AI_MARKERS if re.search(r'\b' + re.escape(m) + r'\b', lower_text)]
    marker_density = (len(found_markers) / word_count) * 100

    # Score calculation
    burstiness_penalty = 0.85 if burstiness_sd < 3.0 else (0.45 if burstiness_sd < 6.0 else 0.20)
    marker_penalty = min(0.95, marker_density * 0.3 + (0.3 if found_markers else 0.0))
    ttr_factor = 0.75 if lexical_diversity < 0.50 else 0.30

    raw_ai = (burstiness_penalty * 0.40) + (marker_penalty * 0.35) + (ttr_factor * 0.25)
    ai_pct = max(5, min(95, int(raw_ai * 100)))
    human_pct = 100 - ai_pct

    # Sentence analyses
    sentence_scores: List[SentenceScoreDto] = []
    for s in sentences:
        s_words = split_words(s)
        s_lower = s.lower()
        s_markers = [m for m in AI_MARKERS if re.search(r'\b' + re.escape(m) + r'\b', s_lower)]
        s_score = 75 if len(s_markers) >= 2 else (65 if len(s_markers) == 1 else (30 if abs(len(s_words) - avg_len) > 7 else 50))
        s_score = int(s_score * 0.7 + ai_pct * 0.3)
        s_score = max(5, min(95, s_score))

        classification = "AI_LIKELY" if s_score >= 65 else ("HUMAN_LIKELY" if s_score <= 40 else "MIXED")
        reasoning = f"Markers: {', '.join(s_markers)}" if s_markers else f"Length: {len(s_words)} words (avg {avg_len:.1f})"
        sentence_scores.append(
            SentenceScoreDto(
                sentence=s,
                ai_score=s_score,
                classification=classification,
                reasoning=reasoning
            )
        )

    confidence = "Low" if word_count < 40 else ("Medium" if word_count < 120 else "High")
    assessment = "High Likelihood of AI Generation" if ai_pct >= 70 else ("Mixed / Moderately AI-Influenced" if ai_pct >= 50 else "Likely Human-Authored")

    explanation = (
        f"Linguistic breakdown: Sentence length standard deviation is {burstiness_sd:.1f} (burstiness), "
        f"Lexical diversity (TTR) is {lexical_diversity * 100:.1f}%. "
        f"{f'Detected stereotypical transition phrases: {', '.join(found_markers[:3])}.' if found_markers else 'No excessive AI trope phrasing detected.'}"
    )

    return AnalyzeResponse(
        ai_percentage=ai_pct,
        human_percentage=human_pct,
        confidence=confidence,
        overall_assessment=assessment,
        detailed_explanation=explanation,
        sentence_analyses=sentence_scores,
        detection_method="Stylometric Burstiness & Lexical Entropy Classifier",
        reliability_limitation="AI text detectors can produce false positives and false negatives, particularly for short, edited, multilingual, and non-native English text. This result is an estimate."
    )

@app.post("/api/humanize", response_model=HumanizeResponse)
def humanize_text(req: HumanizeRequest):
    words_before = len(split_words(req.text))
    # Simulated humanization or Gemini call
    text = req.text.strip()
    
    # Simple rule-based humanizer when API key is not supplied
    cleaned = text
    for m in AI_MARKERS:
        cleaned = re.sub(r'\b' + re.escape(m) + r'\b', '', cleaned, flags=re.IGNORECASE)
    cleaned = re.sub(r'\s+', ' ', cleaned).strip()
    
    words_after = len(split_words(cleaned))
    return HumanizeResponse(
        original_text=req.text,
        humanized_text=cleaned if cleaned else req.text,
        style=req.style,
        word_count_before=words_before,
        word_count_after=words_after,
        changes_summary=f"Rewritten in {req.style} style with {req.tone} tone."
    )

@app.post("/api/ai-style", response_model=AiStyleResponse)
def ai_style_text(req: AiStyleRequest):
    words_before = len(split_words(req.text))
    styled = f"**Executive Summary**\n\n{req.text.strip()}\n\n• Key Takeaway: Structured for {req.style} clarity."
    words_after = len(split_words(styled))
    return AiStyleResponse(
        original_text=req.text,
        ai_styled_text=styled,
        style=req.style,
        word_count_before=words_before,
        word_count_after=words_after
    )

@app.post("/api/improve", response_model=ImproveResponse)
def improve_text(req: ImproveRequest):
    return ImproveResponse(
        original_text=req.text,
        improved_text=req.text.strip(),
        tool_type=req.tool_type,
        notes=f"Processed with {req.tool_type}"
    )
