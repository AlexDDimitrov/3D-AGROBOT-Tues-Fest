import logging
import tempfile
import base64 as b64
from flask import Blueprint, request, jsonify
from ..middleware.auth_guard import login_required
from ..models.plant_report import PlantReportModel
from ..models.garden_request import GardenRequestModel
from .gemini import analyze_image
 
plant_report_bp = Blueprint("plant_report", __name__)
 
 
@plant_report_bp.post("/submit")
@login_required
def submit_report():
    data = request.get_json()
 
    garden_request_id = data.get("garden_request_id")
    garden_id = data.get("garden_id")
    user_id = request.user_id
    report = data.get("report")
 
    if not all([garden_request_id, garden_id, report]):
        return jsonify({"result": 202}), 400
 
    PlantReportModel.create(garden_request_id, garden_id, user_id, report)
 
    logging.info(f"Plant report saved - garden_id: {garden_id} - health: {report.get('health')}")
    return jsonify({"result": 0}), 201

@plant_report_bp.post("/analyze")
@login_required
def analyze_from_app():
    data = request.get_json()
    image_b64 = data.get("image")
    garden_id = data.get("garden_id")
    garden_request_id = data.get("garden_request_id")
    plant = data.get("plant")

    if not all([image_b64, garden_id, garden_request_id, plant]):
        return jsonify({"result": 202}), 400

    try:
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=True) as f:
            f.write(b64.b64decode(image_b64))
            f.flush()
            analysis = analyze_image(f.name, plant)

        PlantReportModel.create(
            garden_request_id=garden_request_id, 
            garden_id=garden_id, 
            user_id=request.user_id, 
            report=analysis
        )

        logging.info(f"Analysis saved for garden_id: {garden_id}")
        return jsonify({"result": 0, "analysis": analysis}), 201

    except Exception as e:
        logging.error(f"Analysis error: {e}")
        return jsonify({"result": 500, "error": "Internal server error during analysis"}), 500
 
@plant_report_bp.get("/list")
@login_required
def list_reports():
    user_id = request.user_id
    reports = PlantReportModel.get_by_user_id(user_id)
    return jsonify({"result": 0, "reports": reports}), 200
 
 
@plant_report_bp.get("/notifications")
@login_required
def notifications():
    user_id = request.user_id
    ill_reports = PlantReportModel.get_unread_ill(user_id)
 
    return jsonify({
        "result": 0,
        "has_notifications": len(ill_reports) > 0,
        "notifications": ill_reports
    }), 200