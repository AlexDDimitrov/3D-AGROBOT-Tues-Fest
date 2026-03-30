import re
import bcrypt
from ..models.user import UserModel

def is_valid_email(email: str) -> bool:
    return bool(re.match(r"[^@]+@[^@]+\.[^@]+", email)) #To do

def is_valid_password(password: str) -> bool:
    return len(password) >= 6

def is_valid_name(name: str) -> bool:
    return bool(name) and len(name) >= 2

def register_user(first_name: str, last_name: str, email: str, password: str):
    if not is_valid_name(first_name) or not is_valid_name(last_name):
        return 103
    
    if not email or not is_valid_email(email):
        return 101

    if not is_valid_password(password):
        return 102

    existing = UserModel.find_by_email(email)
    if existing:
        return 104

    hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
    UserModel.create(first_name, last_name, email, hashed)
    return 0

def login_user(email: str, password: str):
    if not email or not password:
        return 105, None

    user = UserModel.find_by_email(email)
    if not user:
        return 105, None

    if not bcrypt.checkpw(password.encode(), user["password"].encode()):
        return 105, None

    return 0, user