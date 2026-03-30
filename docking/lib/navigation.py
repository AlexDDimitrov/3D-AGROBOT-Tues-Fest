import time
import logging

from .esp32 import ESP32Connection
from .camera import CameraServer
from .gemini import analyze_image
from .api import Api

log = logging.getLogger(__name__)


"""def generate_commands(garden: dict) -> list[str]:
    bed_length = garden["garden_height"]
    path_width = garden["path_width"]
    num_beds = garden["number_beds"]

    commands = []
    for bed in range(num_beds):
        commands.append(f"M{bed_length}")
        
        commands.append("C") 
        
        if bed < num_beds - 1:
            if bed % 2 == 0:
                commands.extend(["R", f"M{path_width}", "R"])
            else:
                commands.extend(["L", f"M{path_width}", "L"])
                
    return commands"""

def generate_commands(garden: dict) -> list[str]:
    bed_length = garden["garden_height"]
    path_width = garden["path_width"]
    num_beds = garden["number_beds"]

    commands = []
    
    for bed in range(num_beds):
        commands.append(f"M {bed_length}")
        
        commands.append("CAPTURE")
        
        if bed < num_beds - 1:
            commands.append(f"M {path_width}")
                
    return commands

def execute_mission(esp: ESP32Connection, camera: CameraServer, garden: dict, api: Api = None, req_id: int = None) -> list[dict]:
    commands = generate_commands(garden)
    plant = garden["plant"]
    garden_id = garden["id"]
    results = []

    log.info(f"Команди: {commands}")
    log.info(f"Мисия: {len(commands)} команди за {garden['number_beds']} лехи")

    for cmd in commands:
        log.info(f"-> {cmd}")

        if cmd == "CAPTURE":
            log.info("Чакам 'R' от Arduino...")
            response = esp.wait_for("R")
            log.info(f"Получено: {response}")

            log.info("Снимам...")
            image_path = camera.capture()
            if image_path:
                analysis = analyze_image(image_path, plant)
                results.append({"image": image_path, "analysis": analysis})
                log.info(f"Анализ: {analysis}")
                if api:
                    api.submit_report(analysis, req_id, garden_id)
            else:
                log.warning("camera.capture() върна None!")
        else:
            resp = esp.send(cmd)
            log.info(f"<- {resp}")

    return results

"""def execute_mission(esp: ESP32Connection, camera: CameraServer, garden: dict, api: Api = None) -> list[dict]:
    commands = generate_commands(garden)
    plant = garden["plant"]
    results = []

    log.info(f"Команди: {commands}")
    log.info(f"Мисия: {len(commands)} команди за {garden['number_beds']} лехи")

    for cmd in commands:
        log.info(f"-> {cmd}")

        if cmd == "CAPTURE":
            log.info("Чакам 'R' от Arduino...")
            response = esp.wait_for("R")
            log.info(f"Получено: {response}")

            log.info("Снимам...")
            image_path = camera.capture()
            if image_path:
                analysis = analyze_image(image_path, plant)
                results.append({"image": image_path, "analysis": analysis})
                log.info(f"Анализ: {analysis}")
                if api:
                    api.submit_report(analysis)
            else:
                log.warning("camera.capture() върна None!")
        else:
            resp = esp.send(cmd)
            log.info(f"<- {resp}")

    return results"""

"""def execute_mission(esp: ESP32Connection, camera: CameraServer, garden: dict, api: Api = None) -> list[dict]:
	commands = generate_commands(garden)
	plant = garden["plant"]
	results = []

	log.info(f"Мисия: {len(commands)} команди за {garden['number_beds']} лехи")

	for cmd in commands:
		log.info(f"-> {cmd}")
		resp = esp.send(cmd)
		log.info(f"<- {resp}")
		time.sleep(1)

		if cmd == "R":
			image_path = camera.capture()
			if image_path:
				analysis = analyze_image(image_path, plant)
				results.append({"image": image_path, "analysis": analysis})
				log.info(f"Анализ: {analysis}")

				if api:
					api.submit_report(analysis)
	return results"""
