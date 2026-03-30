import json
from ..db import get_connection


class PlantReportModel:

    @staticmethod
    def create(garden_request_id: int, garden_id: int, user_id: int, data: dict):
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute(
            """
            INSERT INTO plant_reports 
            (garden_request_id, garden_id, user_id, has_plant, plant_type, health, issues, recommendations)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """,
            (
                garden_request_id,
                garden_id,
                user_id,
                data["has_plant"],
                data.get("plant_type"),
                data.get("health"),
                json.dumps(data.get("issues", [])),
                json.dumps(data.get("recommendations", []))
            )
        )
        conn.commit()
        cursor.close()
        conn.close()

    @staticmethod
    def get_by_user_id(user_id: int):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute(
            """
            SELECT * FROM plant_reports 
            WHERE user_id = %s 
            ORDER BY received_at DESC
            """,
            (user_id,)
        )
        reports = cursor.fetchall()
        cursor.close()
        conn.close()

        for report in reports:
            report["issues"] = json.loads(report["issues"])
            report["recommendations"] = json.loads(report["recommendations"])
            report["received_at"] = str(report["received_at"])

        return reports
    @staticmethod
    def get_unread_ill(user_id: int):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute(
            """
            SELECT * FROM plant_reports 
            WHERE user_id = %s AND health != 'healthy' AND is_read = FALSE
            ORDER BY received_at DESC
            """,
            (user_id,)
        )
        reports = cursor.fetchall()

        cursor.execute(
            """
            UPDATE plant_reports 
            SET is_read = TRUE
            WHERE user_id = %s AND health != 'healthy' AND is_read = FALSE
            """,
            (user_id,)
        )
        conn.commit()
        cursor.close()
        conn.close()

        for report in reports:
            report["issues"] = json.loads(report["issues"])
            report["recommendations"] = json.loads(report["recommendations"])
            report["received_at"] = str(report["received_at"])

        return reports