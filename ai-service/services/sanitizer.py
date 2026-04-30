"""
sanitizer.py — AI Developer 2
Strips HTML tags, detects prompt injection patterns, enforces length limits.
Usage:
    clean, err = sanitize(data, required_fields=["company_name", "section"])
    if err:
        return jsonify({"error": err}), 400
"""

import re
import html

# Prompt injection patterns — common jailbreak phrases
INJECTION_PATTERNS = [
    r"ignore\s+(previous|all|above|prior)\s+instructions?",
    r"you\s+are\s+now\s+(a|an|DAN)",
    r"act\s+as\s+(a|an)",
    r"pretend\s+(you\s+are|to\s+be)",
    r"jailbreak",
    r"disregard\s+(your|all)\s+(rules?|instructions?|guidelines?)",
    r"forget\s+(your|all)\s+(instructions?|training|rules?)",
    r"system\s*prompt",
    r"reveal\s+(your\s+)?(prompt|instructions?|system)",
    r"bypass\s+(your\s+)?(filter|restriction|safety)",
    r"<\s*script",          # XSS
    r"javascript\s*:",      # XSS
    r"on\w+\s*=",           # HTML event handlers
]

_compiled = [re.compile(p, re.IGNORECASE) for p in INJECTION_PATTERNS]

MAX_FIELD_LENGTH = 2000  # characters per field


def _strip_html(value: str) -> str:
    """Remove HTML tags and decode HTML entities."""
    value = html.unescape(value)
    value = re.sub(r"<[^>]+>", "", value)
    return value.strip()


def _contains_injection(value: str) -> bool:
    """Return True if value matches any known injection pattern."""
    for pattern in _compiled:
        if pattern.search(value):
            return True
    return False


def sanitize(data: dict, required_fields: list[str] = None) -> tuple[dict, str | None]:
    """
    Validate and sanitize all string fields in `data`.
    Returns (clean_data, error_message). error_message is None if all safe.
    """
    if required_fields:
        for field in required_fields:
            if not data.get(field):
                return {}, f"Missing required field: '{field}'"

    clean = {}
    for key, value in data.items():
        if not isinstance(value, str):
            clean[key] = value
            continue

        # Strip HTML
        cleaned_value = _strip_html(value)

        # Length check
        if len(cleaned_value) > MAX_FIELD_LENGTH:
            return {}, f"Field '{key}' exceeds maximum length of {MAX_FIELD_LENGTH} characters"

        # Injection check
        if _contains_injection(cleaned_value):
            return {}, f"Invalid input detected in field '{key}'"

        clean[key] = cleaned_value

    return clean, None