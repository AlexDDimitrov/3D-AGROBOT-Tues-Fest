import base64
import json
import logging
import requests

from .config import config

log = logging.getLogger(__name__)

GEMINI_URL = (
	"https://generativelanguage.googleapis.com/v1beta"
	"/models/gemini-2.5-flash:generateContent"
)

PROMPT = """Анализирай изображението. Отговори САМО с валиден JSON в следния формат:
{
  "has_plant": true/false,
  "plant_type": "вид на растението или null",
  "health": "healthy" | "sick" | "dead" | "unknown",
  "issues": ["проблем 1", "проблем 2"],
  "recommendations": ["препоръка 1", "препоръка 2"]
}

Очакваното растение е: %s
Ако няма растение на снимката, върни has_plant: false и health: "unknown".
Отговори САМО с JSON, без markdown, без обяснения."""


def analyze_image(image_path: str, plant: str) -> dict:
	if not config.GEMINI_API_KEY:
		log.warning("GEMINI_API_KEY не е зададен!")
		return {"error": "Няма API ключ за Gemini"}

	with open(image_path, "rb") as f:
		data = base64.b64encode(f.read()).decode()

	resp = requests.post(
		f"{GEMINI_URL}?key={config.GEMINI_API_KEY}",
		json={
			"contents": [{
				"parts": [
					{"text": PROMPT % plant},
					{
						"inline_data": {
							"mime_type": "image/jpeg",
							"data": data
						}
					}
				]
			}]
		},
		timeout=30
	)

	try:
		text = resp.json()["candidates"][0]["content"]["parts"][0]["text"]
		text = text.strip().removeprefix("```json").removesuffix("```").strip()
		return json.loads(text)
	except (KeyError, IndexError):
		log.error(f"Gemini грешка: {resp.text}")
		return {"error": "Грешка при заявка към Gemini"}
	except json.JSONDecodeError:
		log.error(f"Невалиден JSON от Gemini: {text}")
		return {"error": "Невалиден отговор", "raw": text}
