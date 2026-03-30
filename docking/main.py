import time
import logging

from lib import ESP32Connection, CameraServer, Api, execute_mission
from lib.config import config

logging.basicConfig(
	level=logging.INFO,
	format="%(asctime)s [%(levelname)s] %(message)s"
)
log = logging.getLogger(__name__)


def main():
	api = Api()
	if not api.login():
		return

	camera = CameraServer()
	camera.start()

	gardens = api.list_gardens()
	if not gardens:
		return

	garden = api.choose_garden(gardens)
	api.show_garden(garden)

	with ESP32Connection() as esp:
		resp = esp.send("PING")
		log.info(f"PING -> {resp}")

		log.info("Polling за заявки... ")
		while True:
			req = api.get_pending_request()

			if req:
				log.info("Нова заявка! Стартирам робота...")
				api.update_request(req["id"], status=1)

				if not camera.connected:
					log.info("Чакам ESP32-CAM...")
					while not camera.connected:
						time.sleep(1)

				results = execute_mission(esp, camera, garden, api, req_id=req["id"])

				api.update_request(req["id"], status=2)

				log.info(f"Готово! {len(results)} снимки анализирани.")
				for r in results:
					log.info(f"  {r['image']}: {r['analysis']}...")

			time.sleep(config.POLL_INTERVAL)


if __name__ == "__main__":
	main()
