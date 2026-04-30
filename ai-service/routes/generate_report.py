"""
generate_report.py — AI Developer 2
POST /generate-report
Accepts compliance record data, returns structured AI compliance report.
"""

import os
import json
import logging
from datetime import datetime, timezone

from flask import Blueprint, request, jsonify, current_app

from services.groq_client import call_groq, make_cache_key
from services.sanitizer import sanitize

logger = logging.getLogger(__name__)

generate_report_bp = Blueprint("generate_report", __name__)

# Load prompt template once at import time
_PROMPT_PATH = os.path.join(os.path.dirname(__file__), "..", "prompts", "generate_report_prompt.txt")
with open(_PROMPT_PATH, "r") as f:
    _PROMPT_TEMPLATE = f.read()

# Fallback report returned when Groq is unavailable
FALLBACK_REPORT = {
    "is_fallback": True,
    "title": "Compliance Report — AI Unavailable",
    "executive_summary": "AI service is temporarily unavailable. Please retry shortly.",
    "compliance_overview": {
        "current_status": "UNKNOWN",
        "risk_level": "UNKNOWN",
        "section_reference": "N/A",
        "assessment": "Unable to assess at this time.",
    },
    "key_findings": [],
    "recommendations": [],
    "legal_references": [],
    "conclusion": "Report could not be generated. Please try again later.",
}


@generate_report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "Request body must be valid JSON"}), 400

    # Sanitize + validate required fields
    required = ["company_name", "section_number", "status", "description"]
    clean, err = sanitize(data, required_fields=required)
    if err:
        return jsonify({"error": err}), 400

    # Set defaults for optional fields
    clean.setdefault("section_title", "N/A")
    clean.setdefault("due_date", "Not specified")
    clean.setdefault("ai_description", "Not available")

    # Check Redis cache
    redis_client = current_app.config.get("REDIS_CLIENT")
    cache_key = make_cache_key("report", clean)

    if redis_client:
        try:
            cached = redis_client.get(cache_key)
            if cached:
                logger.info(f"[generate-report] Cache HIT for key {cache_key[:16]}...")
                result = json.loads(cached)
                result["cache_hit"] = True
                return jsonify(result), 200
        except Exception as e:
            logger.warning(f"[generate-report] Redis read error: {e}")

    # Build prompt using replace (avoids Python format() conflicts with JSON braces)
    prompt = (
        _PROMPT_TEMPLATE
        .replace("{company_name}", clean.get("company_name", ""))
        .replace("{section_number}", clean.get("section_number", ""))
        .replace("{section_title}", clean.get("section_title", "N/A"))
        .replace("{status}", clean.get("status", ""))
        .replace("{due_date}", clean.get("due_date", "Not specified"))
        .replace("{description}", clean.get("description", ""))
        .replace("{ai_description}", clean.get("ai_description", "Not available"))
    )

    messages = [
        {
            "role": "system",
            "content": (
                "You are a senior Companies Act 2013 compliance analyst. "
                "Always respond with valid JSON only. No markdown, no extra text. "
                "No trailing commas. No comments inside JSON."
            ),
        },
        {"role": "user", "content": prompt},
    ]

    # Call Groq
    result = call_groq(messages, temperature=0.3, max_tokens=2000)

    if result is None:
        logger.error("[generate-report] Groq call failed — returning fallback")
        fallback = dict(FALLBACK_REPORT)
        fallback["generated_at"] = datetime.now(timezone.utc).isoformat()
        return jsonify(fallback), 200

    result["generated_at"] = datetime.now(timezone.utc).isoformat()
    result["is_fallback"] = False
    result["cache_hit"] = False

    # Store in Redis (15 min TTL)
    if redis_client:
        try:
            redis_client.setex(cache_key, 900, json.dumps(result))
        except Exception as e:
            logger.warning(f"[generate-report] Redis write error: {e}")

    return jsonify(result), 200