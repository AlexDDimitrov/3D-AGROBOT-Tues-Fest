# 3D Agrobot — Docking Module

Софтуер за управление на земеделски робот, който автономно обхожда градински лехи, заснема растенията и анализира здравословното им състояние чрез Google Gemini AI.

## Архитектура

```
┌─────────────┐   TCP :8080    ┌───────────┐
│   main.py   │ ──────────────>│   ESP32   │  Мотори (M/L/R)
│             │                └───────────┘
│  Polling    │   WebSocket    ┌───────────┐
│  Navigate   │ <──────────────│ ESP32-CAM │  Камера (CAPTURE)
│  Analyze    │     :8888      └───────────┘
│             │
│             │   HTTP         ┌───────────┐
│             │ <──────────────│ Backend   │  API (заявки, градини)
│             │                │ Railway   │
│             │                └───────────┘
│             │   HTTP         ┌───────────┐
│             │ ──────────────>│  Gemini   │  AI анализ на снимки
│             │                │  Flash    │
└─────────────┘                └───────────┘
```

## Структура на проекта

```
docking/
├── main.py              # Entry point
├── .env                 # Конфигурация (не се комитва)
├── .env.example         # Шаблон за .env
├── .gitignore
├── requirements.txt
├── captures/            # Заснети снимки (автоматично)
└── lib/
    ├── config.py        # Зареждане на .env настройки
    ├── esp32.py         # TCP връзка с ESP32
    ├── camera.py        # WebSocket сървър за ESP32-CAM
    ├── api.py           # Backend API клиент
    ├── gemini.py        # Google Gemini Flash анализ
    └── navigation.py    # Навигация и изпълнение на мисия
```

## Инсталация

```bash
git clone <repo-url>
cd docking
pip install -r requirements.txt
cp .env.example .env
```

Попълни `.env` с твоите стойности:

```env
ESP32_IP=172.20.10.8
ESP32_PORT=8080
CAMERA_PORT=8888
API_URL=https://3d-agrobot-production.up.railway.app
GEMINI_API_KEY=your_key_here
```

Gemini API ключ се взима от [Google AI Studio](https://aistudio.google.com/apikey).

## Стартиране

```bash
python main.py
```

Програмата:
1. Стартира WebSocket сървър за камерата (порт 8888)
2. Логва се в backend-а (имейл + парола)
3. Показва списък с градини и избираш една
4. Свързва се с ESP32 по TCP
5. Започва polling на всеки 5 секунди за нови заявки

## Как работи

### Команди към ESP32

| Команда | Описание                          | Пример |
|---------|-----------------------------------|--------|
| `M<X>`  | Движение напред с X сантиметра    | `M120` |
| `L`     | Завой наляво                      | `L`    |
| `R`     | Завой надясно + заснемане         | `R`    |

### Навигация (Boustrophedon)

Роботът обхожда лехите в змийовиден (boustrophedon) pattern:

```
    Леха 1    Леха 2    Леха 3
  ┌────────┐┌────────┐┌────────┐
  │  ↑     ││     ↑  ││  ↑     │
  │  │     ││     │  ││  │     │
  │  │     ││     │  ││  │     │
  │  START ││     │  ││  │     │
  └──┼─────┘└─────┼──┘└──┼─────┘
     └──→R──→──→R─┘      │
           ←──L──←──L────┘
```

При всеки завой **надясно (R)** роботът:
1. Изпраща `CAPTURE` към ESP32-CAM
2. Получава снимката и я записва в `captures/capture_<id>.jpg`
3. Изпраща снимката към Gemini Flash за анализ на растението

### Жизнен цикъл на заявка

```
Статус 0 (нова)  →  Статус 1 (в движение)  →  Статус 2 (завършена)
   Backend           Робот тръгва              Резултати готови
```

## Модули

### `lib/config.py`
Зарежда настройките от `.env` чрез `python-dotenv`. Всички модули импортират `config` оттук.

### `lib/esp32.py` — `ESP32Connection`
TCP клиент за комуникация с ESP32. Поддържа автоматично reconnect при загуба на връзка. Използва се като context manager:

```python
with ESP32Connection() as esp:
    esp.send("M100")  # движение 100cm
    esp.send("R")     # завой надясно
```

### `lib/camera.py` — `CameraServer`
WebSocket сървър, който работи в background thread. ESP32-CAM се свързва към него. Методът `capture()` е thread-safe и може да се вика от основния поток:

```python
camera = CameraServer()
camera.start()                  # стартира на порт 8888
path = camera.capture()         # -> "captures/capture_a1b2c3d4.jpg"
```

### `lib/api.py` — `Api`
HTTP клиент към backend-а на Railway. Управлява автентикация, списък с градини и статус на заявки:

```python
api = Api()
api.login()
gardens = api.list_gardens()
req = api.get_pending_request()
api.update_request(req["id"], status=2)
```

### `lib/gemini.py` — `analyze_image()`
Изпраща снимка (base64) към Google Gemini 2.0 Flash и връща текстов анализ на растението — здравословно състояние, болести, вредители и препоръки.

### `lib/navigation.py`
- `generate_commands(garden)` — генерира списък от команди (`M`, `L`, `R`) по параметрите на градината
- `execute_mission(esp, camera, garden)` — изпълнява командите, снима при `R` и анализира с Gemini

## Зависимости

| Пакет          | За какво                      |
|----------------|-------------------------------|
| `requests`     | HTTP заявки (API, Gemini)     |
| `websockets`   | WebSocket сървър за камерата  |
| `Pillow`       | Обработка на изображения      |
| `python-dotenv`| Зареждане на `.env` файл      |
