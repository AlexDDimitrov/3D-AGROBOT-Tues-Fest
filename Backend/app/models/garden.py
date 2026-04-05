from ..db import get_connection

class GardenModel:

    @staticmethod
    def create(user_id, garden_name, garden_width, garden_height, path_width, number_beds, plant, number_of_plants):
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute(
            """
            INSERT INTO gardens (user_id, garden_name, garden_width, garden_height, path_width, number_beds, plant, number_of_plants)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """,
            (user_id, garden_name, garden_width, garden_height, path_width, number_beds, plant, number_of_plants)
        )
        conn.commit()
        cursor.close()
        conn.close()

    @staticmethod
    def update(garden_id: int, user_id: int, fields: dict):
        conn = get_connection()
        cursor = conn.cursor()

        set_clause = ", ".join([f"{key} = %s" for key in fields.keys()])
        values = list(fields.values()) + [garden_id, user_id]

        cursor.execute(
            f"UPDATE gardens SET {set_clause} WHERE id = %s AND user_id = %s",
            values
        )
        affected = cursor.rowcount
        conn.commit()
        cursor.close()
        conn.close()
        return affected

    @staticmethod
    def get_by_user_id(user_id):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute(
            "SELECT id, garden_name, garden_width, garden_height, path_width, number_beds, plant, number_of_plants FROM gardens WHERE user_id = %s",
            (user_id,)
        )
        gardens = cursor.fetchall()
        cursor.close()
        conn.close()
        return gardens