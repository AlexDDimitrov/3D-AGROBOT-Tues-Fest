import requests
import logging

from .config import config

log = logging.getLogger(__name__)


class Api:
	def __init__(self):
		self.base_url = config.API_URL
		self.token = None
		self.user = None

	@property
	def headers(self):
		return {"Authorization": f"Bearer {self.token}"}

	def login(self):
		email = input("Имейл: ")
		password = input("Парола: ")

		resp = requests.post(f"{self.base_url}/auth/login", json={
			"email": email,
			"password": password
		})
		data = resp.json()

		if data["result"] != 0:
			log.error(f"Грешка при логин: {data['result']}")
			return False

		self.token = data["token"]
		self.user = data["user"]
		log.info(f"Добре дошъл, {self.user['first_name']} {self.user['last_name']}!")
		return True

	def list_gardens(self):
		resp = requests.get(f"{self.base_url}/garden/list", headers=self.headers)
		gardens = resp.json()["gardens"]

		print("\nТвоите градини:")
		for i, g in enumerate(gardens):
			print(f"  {i + 1}. {g['garden_name']}")

		return gardens

	def choose_garden(self, gardens):
		while True:
			try:
				choice = int(input("\nИзбери градина (номер): "))
				if 1 <= choice <= len(gardens):
					return gardens[choice - 1]
			except ValueError:
				print("Въведи валиден номер!")

	def show_garden(self, garden):
		print("\n--- Параметри на градината ---")
		print(f"Име: {garden['garden_name']}")
		print(f"Ширина: {garden['garden_width']} см")
		print(f"Височина: {garden['garden_height']} см")
		print(f"Ширина пътека: {garden['path_width']} см")
		print(f"Брой лехи: {garden['number_beds']}")
		print(f"Растение: {garden['plant']}")
		print("------------------------------")

	def get_pending_request(self):
		try:
			resp = requests.get(f"{self.base_url}/request/status", headers=self.headers)
			req = resp.json().get("request")
			if req and req["status"] == 0:
				return req
		except Exception as e:
			log.error(f"API грешка: {e}")
		return None

	def update_request(self, request_id, status):
		requests.post(
			f"{self.base_url}/request/update",
			headers=self.headers,
			json={"request_id": request_id, "status": status}
		)

	def submit_report(self, report: dict, garden_request_id: int, garden_id: int):
		try:
			resp = requests.post(
				f"{self.base_url}/report/submit",
				headers=self.headers,
				json={
					"garden_request_id": garden_request_id,
					"garden_id": garden_id,
					"report": report
				}
			)
			log.info(f"Report submit: {resp.status_code}")
			return resp.json()
		except Exception as e:
			log.error(f"Report грешка: {e}")
			return None