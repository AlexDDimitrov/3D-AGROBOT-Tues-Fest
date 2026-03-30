import os
from dotenv import load_dotenv

load_dotenv()


class Config:
	ESP32_IP = os.getenv("ESP32_IP", "172.20.10.8")
	ESP32_PORT = int(os.getenv("ESP32_PORT", "8080"))
	CAMERA_PORT = int(os.getenv("CAMERA_PORT", "8888"))
	API_URL = os.getenv("API_URL", "https://3d-agrobot-production.up.railway.app")
	GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

	TIMEOUT = 120.0
	RECONNECT_DELAY = 3.0
	POLL_INTERVAL = 5


config = Config()
