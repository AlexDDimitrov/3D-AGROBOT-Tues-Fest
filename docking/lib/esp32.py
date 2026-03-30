import socket
import time
import logging

from .config import config

log = logging.getLogger(__name__)


class ESP32Connection:
	def __init__(self):
		self.ip = config.ESP32_IP
		self.port = config.ESP32_PORT
		self.sock = None

	def connect(self):
		while True:
			try:
				self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
				self.sock.settimeout(config.TIMEOUT)
				self.sock.connect((self.ip, self.port))
				log.info(f"Свързан с ESP32 @ {self.ip}:{self.port}")
				return
			except (ConnectionRefusedError, socket.timeout, OSError) as e:
				log.warning(f"Неуспешна връзка ({e}), опит след {config.RECONNECT_DELAY}s...")
				time.sleep(config.RECONNECT_DELAY)

	def send(self, cmd: str):
		try:
			self.sock.sendall((cmd + "\n").encode("utf-8"))
			return True
		except (socket.timeout, OSError) as e:
			log.error(f"Грешка при комуникация: {e}")
			self.reconnect()
			return False
		
	def wait_for(self, expected: str, timeout: int = 30):
		self.sock.settimeout(timeout)
		buffer = ""
		try:
			while True:
				data = self.sock.recv(1024).decode("utf-8")
				if not data:
					break
				buffer += data
				if expected in buffer:
					log.info(f"Получено '{expected}' от ESP32")
					self.sock.settimeout(config.TIMEOUT)
					return True
		except socket.timeout:
			log.warning(f"Timeout - не получих '{expected}' след {timeout}s")
			self.sock.settimeout(config.TIMEOUT)
			return False

	def reconnect(self):
		log.info("Преповтаряне на връзката...")
		self.close()
		self.connect()

	def close(self):
		if self.sock:
			try:
				self.sock.close()
			except OSError:
				pass
			self.sock = None

	def __enter__(self):
		self.connect()
		return self

	def __exit__(self, *args):
		self.close()
