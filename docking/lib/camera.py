import asyncio
import websockets
import threading
import uuid
import os
from PIL import Image
import io
import logging

from .config import config

log = logging.getLogger(__name__)


class CameraServer:
	def __init__(self):
		self.port = config.CAMERA_PORT
		self._ws = None
		self._loop = None
		self._thread = None
		self._incoming = None

	def start(self):
		self._thread = threading.Thread(target=self._run, daemon=True)
		self._thread.start()
		log.info(f"Camera server на порт {self.port}")

	def _run(self):
		self._loop = asyncio.new_event_loop()
		asyncio.set_event_loop(self._loop)
		self._loop.run_until_complete(self._serve())

	async def _serve(self):
		async with websockets.serve(self._handler, "0.0.0.0", self.port):
			await asyncio.Future()

	async def _handler(self, ws):
		log.info(f"ESP32-CAM свързана: {ws.remote_address}")
		self._ws = ws
		self._incoming = asyncio.Queue()
		try:
			async for msg in ws:
				await self._incoming.put(msg)
		except websockets.ConnectionClosed as e:
			log.info(f"ESP32-CAM прекъсна: {e}")
		finally:
			self._ws = None
			self._incoming = None

	async def _do_capture(self) -> str | None:
		ws = self._ws
		q = self._incoming
		if not ws or not q:
			log.warning("ESP32-CAM не е свързана")
			return None

		while not q.empty():
			try:
				q.get_nowait()
			except asyncio.QueueEmpty:
				break

		await ws.send("CAPTURE")
		log.info("CAPTURE изпратен, чакам снимка...")

		try:
			data = await asyncio.wait_for(q.get(), timeout=10.0)
		except asyncio.TimeoutError:
			log.warning("Timeout от ESP32-CAM")
			return None

		if not isinstance(data, bytes):
			log.warning(f"Неочакван тип: {type(data)}")
			return None

		os.makedirs("captures", exist_ok=True)
		filename = f"captures/capture_{uuid.uuid4().hex[:8]}.jpg"
		image = Image.open(io.BytesIO(data))
		image.save(filename)
		log.info(f"Снимка: {filename} ({len(data)} bytes, {image.size})")
		return filename

	def capture(self) -> str | None:
		if not self._loop:
			log.error("Camera server не е стартиран")
			return None
		future = asyncio.run_coroutine_threadsafe(self._do_capture(), self._loop)
		try:
			return future.result(timeout=15.0)
		except Exception as e:
			log.error(f"Capture грешка: {e}")
			return None

	@property
	def connected(self) -> bool:
		return self._ws is not None