import logging
from flask import Blueprint, request, jsonify
from ..middleware.auth_guard import login_required
from ..models.garden_request import GardenRequestModel
from ..db import get_connection

garden_request_bp = Blueprint("garden_request", __name__)

@garden_request_bp.post("/start")
@login_required
def start_request():
    data = request.get_json()
    user_id = request.user_id
    garden_id = data.get("garden_id")

    if not garden_id:
        return jsonify({"result": 202}), 400

    if GardenRequestModel.has_active_request(user_id):
        return jsonify({"result": 301}), 400

    GardenRequestModel.create(garden_id, user_id)

    logging.info(f"Garden request created - user_id: {user_id} - garden_id: {garden_id}")
    return jsonify({"result": 0}), 201

@garden_request_bp.get("/status")
@login_required
def get_status():
    user_id = request.user_id

    conn = get_connection()
    cursor = conn.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM garden_request WHERE user_id = %s ORDER BY create_time DESC LIMIT 1",
        (user_id,)
    )
    latest = cursor.fetchone()
    cursor.close()
    conn.close()

    if not latest:
        return jsonify({"result": 0, "request": None}), 200

    return jsonify({"result": 0, "request": {
        "id": latest["id"],
        "garden_id": latest["garden_id"],
        "status": latest["status"],
        "create_time": str(latest["create_time"]),
        "end_time": str(latest["end_time"]) if latest["end_time"] else None
    }}), 200

@garden_request_bp.post("/update")
@login_required
def update_status():
    data = request.get_json()
    request_id = data.get("request_id")
    status = data.get("status")

    if request_id is None or status is None:
        return jsonify({"result": 202}), 400

    GardenRequestModel.update_status(request_id, status)
    return jsonify({"result": 0}), 200