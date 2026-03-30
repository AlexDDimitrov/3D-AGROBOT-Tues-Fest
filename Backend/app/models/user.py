from ..db import get_connection

class UserModel:

    @staticmethod
    def create(first_name: str, last_name: str, email: str, hashed_password: str):
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO users (first_name, last_name, email, password) VALUES (%s, %s, %s, %s)",
            (first_name, last_name, email, hashed_password)
        )
        conn.commit()
        cursor.close()
        conn.close()

    @staticmethod
    def find_by_email(email: str):
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute(
            "SELECT id, first_name, last_name, email, password, register_time FROM users WHERE email = %s",
            (email,)
        )
        user = cursor.fetchone()
        cursor.close()
        conn.close()
        return user