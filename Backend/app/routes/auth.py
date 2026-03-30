import logging
from flask import Blueprint, request, jsonify
from ..services.auth_service import register_user, login_user
from ..utils.jwt_helper import generate_token
from ..models.user import UserModel

auth_bp = Blueprint("auth", __name__)
@auth_bp.post("/register")
def register():
    data = request.get_json()

    first_name = data.get("first_name")
    last_name = data.get("last_name")
    email = data.get("email")
    password = data.get("password")

    result = register_user(first_name, last_name, email, password)

    if result == 0:
        user = UserModel.find_by_email(email)
        token = generate_token(user["id"])
        logging.info(f"Register success: {email}")
        return jsonify({
            "result": 0,
            "token": token,
            "user": {
                "id": user["id"],
                "first_name": user["first_name"],
                "last_name": user["last_name"],
                "email": user["email"]
            }
        }), 201

    logging.warning(f"Register result: {result} - {email}")
    return jsonify({"result": result}), 400

@auth_bp.post("/login")
def login():
    data = request.get_json()

    email = data.get("email")
    password = data.get("password")

    result, user = login_user(email, password)

    if result == 0:
        token = generate_token(user["id"])
        logging.info(f"Login success: {email}")
        return jsonify({
            "result": 0,
            "token": token,
            "user": {
                "id": user["id"],
                "first_name": user["first_name"],
                "last_name": user["last_name"],
                "email": user["email"]
            }
        }), 200
    else:
        logging.warning(f"Login failed: {email}")
        return jsonify({"result": result}), 401