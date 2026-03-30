import logging
from flask import Blueprint, request, jsonify
from ..middleware.auth_guard import login_required
from ..models.plant_report import PlantReportModel
from ..models.garden_request import GardenRequestModel
 
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