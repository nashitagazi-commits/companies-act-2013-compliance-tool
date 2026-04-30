"""
groq_client.py — AI Developer 2
Wraps Groq API: 3-retry with exponential backoff, JSON parse, error logging.
"""

import os
import time
import json
import hashlib
import logging

from groq import Groq

logger = logging.getLogger(__name__)

_client = None


def get_client() -> Groq:
    global _client
    if _client is None:
        api_key = os.getenv("GROQ_API_KEY")
        if not api_key:
            raise EnvironmentError("GROQ_API_KEY is not set in environment")
        _client = Groq(api_key=api_key)
    return _client


def call_groq(
    messages: list,
    model: str = "llama-3.3-70b-versatile",
    temperature: float = 0.3,
    max_tokens: int = 2000,
    retries: int = 3,
    base_delay: float = 1.0,
) -> dict | None:
    """
    Call Groq API. Returns parsed JSON dict, or None on total failure.
    Retries 3 times with exponential backoff: 1s -> 2s -> 4s.
    """
    client = get_client()
    last_error = None

    for attempt in range(1, retries + 1):
        try:
            logger.info(f"[GroqClient] Attempt {attempt}/{retries}")
            response = client.chat.completions.create(
                model=model,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
            )
            raw = response.choices[0].message.content.strip()

            # Strip markdown fences if model wraps output in ```json ... ```
            if raw.startswith("```"):
                parts = raw.split("```")
                raw = parts[1]
                if raw.startswith("json"):
                    raw = raw[4:]
                raw = raw.strip()

            parsed = json.loads(raw)
            logger.info(f"[GroqClient] Success on attempt {attempt}")
            return parsed

        except json.JSONDecodeError as e:
            logger.error(f"[GroqClient] JSON parse failed: {e}")
            last_error = e
            break  # Same input => same bad output, no point retrying

        except Exception as e:
            last_error = e
            delay = base_delay * (2 ** (attempt - 1))  # 1s, 2s, 4s
            logger.warning(
                f"[GroqClient] Attempt {attempt} failed — {type(e).__name__}: {e}. "
                f"Retrying in {delay:.0f}s..."
            )
            if attempt < retries:
                time.sleep(delay)

    logger.error(f"[GroqClient] All {retries} attempts failed. Last error: {last_error}")
    return None


def make_cache_key(prefix: str, payload: dict) -> str:
    """Generate SHA256 cache key — same input always maps to same key."""
    raw = json.dumps(payload, sort_keys=True)
    digest = hashlib.sha256(raw.encode()).hexdigest()
    return f"{prefix}:{digest}"