"""
app.py — AI Service Entry Point

AI Developer 1: Flask setup, /describe, /recommend, /health
AI Developer 2: GroqClient, /generate-report, flask-limiter, sanitizer, Redis cache, security headers
Port: 5000
"""
from dotenv import load_dotenv
load_dotenv()

import os
import logging
import redis

from flask import Flask, jsonify, request
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s — %(message)s",
)
logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# App factory
# ---------------------------------------------------------------------------
def create_app() -> Flask:
    app = Flask(__name__)

    # ------------------------------------------------------------------
    # Redis client (shared across routes via app.config)
    # ------------------------------------------------------------------
    redis_host = os.getenv("REDIS_HOST", "redis")
    redis_port = int(os.getenv("REDIS_PORT", 6379))
    try:
        redis_client = redis.Redis(
            host=redis_host,
            port=redis_port,
            db=1,                  # DB 1 = AI cache (DB 0 = Java backend)
            decode_responses=True,
            socket_connect_timeout=2,
        )
        redis_client.ping()
        app.config["REDIS_CLIENT"] = redis_client
        logger.info(f"[Redis] Connected at {redis_host}:{redis_port}")
    except Exception as e:
        logger.warning(f"[Redis] Not available — caching disabled: {e}")
        app.config["REDIS_CLIENT"] = None

    # ------------------------------------------------------------------
    # Rate limiter — 30 requests per minute per IP (AI Developer 2)
    # ------------------------------------------------------------------
    limiter = Limiter(
        key_func=get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri=f"redis://{redis_host}:{redis_port}/2" if app.config["REDIS_CLIENT"] else "memory://",
    )

    # ------------------------------------------------------------------
    # Security headers (AI Developer 2 — OWASP ZAP findings fix)
    # ------------------------------------------------------------------
    @app.after_request
    def add_security_headers(response):
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
        response.headers["Content-Security-Policy"] = "default-src 'none'"
        response.headers["Referrer-Policy"] = "no-referrer"
        response.headers["Permissions-Policy"] = "geolocation=(), microphone=(), camera=()"
        # Remove server fingerprint
        response.headers.pop("Server", None)
        return response

    # ------------------------------------------------------------------
    # Rate limit exceeded handler
    # ------------------------------------------------------------------
    @app.errorhandler(429)
    def rate_limit_exceeded(e):
        return jsonify({
            "error": "Rate limit exceeded",
            "message": "Maximum 30 requests per minute allowed. Please slow down.",
        }), 429

    # ------------------------------------------------------------------
    # Register blueprints
    # AI Developer 1 routes
    # ------------------------------------------------------------------
    try:
        from routes.describe import describe_bp
        app.register_blueprint(describe_bp)
        logger.info("[Routes] /describe registered")
    except ImportError:
        logger.warning("[Routes] describe route not found — skipping")

    try:
        from routes.recommend import recommend_bp
        app.register_blueprint(recommend_bp)
        logger.info("[Routes] /recommend registered")
    except ImportError:
        logger.warning("[Routes] recommend route not found — skipping")

    try:
        from routes.health import health_bp
        app.register_blueprint(health_bp)
        logger.info("[Routes] /health registered")
    except ImportError:
        logger.warning("[Routes] health route not found — skipping")

    # AI Developer 2 route
    from routes.generate_report import generate_report_bp
    app.register_blueprint(generate_report_bp)
    logger.info("[Routes] /generate-report registered")

    # ------------------------------------------------------------------
    # Global 404 / 405
    # ------------------------------------------------------------------
    @app.errorhandler(404)
    def not_found(e):
        return jsonify({"error": "Endpoint not found"}), 404

    @app.errorhandler(405)
    def method_not_allowed(e):
        return jsonify({"error": "Method not allowed"}), 405

    return app


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------
app = create_app()

if __name__ == "__main__":
    port = int(os.getenv("AI_PORT", 5000))
    debug = os.getenv("FLASK_DEBUG", "false").lower() == "true"
    logger.info(f"Starting AI service on port {port} (debug={debug})")
    app.run(host="0.0.0.0", port=port, debug=debug)