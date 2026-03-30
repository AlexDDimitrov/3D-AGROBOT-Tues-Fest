from ..db import get_connection

class GardenRequestModel:

    @staticmethod
    def create(garden_id: int, user_id: int):
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO garden_request (garden_id, user_id) VALUES (%s, %s)",
            (garden_id, user_id)
        )
        conn.commit()
        cursor.close()
        conn.close()

    @staticmethod
    def has_active_request(user_id: int) -> bool:
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute(
            "SELECT id FROM garden_request WHERE user_id = %s AND status IN (0, 1)",
            (user_id,)
        )
        result = cursor.fetchone()
        cursor.close()
        conn.close()
        return result is not None

    @staticmethod
    def get_pending_request():
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute(
            "SELECT * FROM garden_request WHERE status = 0 ORDER BY create_time ASC LIMIT 1"
        )
        request = cursor.fetchone()
        cursor.close()
        conn.close()
        return request

    @staticmethod
    def update_status(request_id: int, status: int):
        conn = get_connection()
        cursor = conn.cursor()
        if status == 2:
            cursor.execute(
                "UPDATE garden_request SET status = %s, end_time = NOW() WHERE id = %s",
                (status, request_id)
            )
        else:
            cursor.execute(
                "UPDATE garden_request SET status = %s WHERE id = %s",
                (status, request_id)
            )
        conn.commit()
        cursor.close()
        conn.close()