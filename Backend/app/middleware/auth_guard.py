from functools import wraps
from flask import request, jsonify
from ..utils.jwt_helper import verify_token

def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get("Authorization")

        if not token or not token.startswith("Bearer "):
            return jsonify({"result": 201}), 401

        token = token.split(" ")[1]

        payload = verify_token(token)
        if not payload:
            return jsonify({"result": 201}), 401

        request.user_id = payload["id"]
        return f(*args, **kwargs)
    return decorated