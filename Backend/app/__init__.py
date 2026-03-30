from flask import Flask
from dotenv import load_dotenv
from .config import Config

load_dotenv()

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    from .routes.auth import auth_bp
    from .routes.garden import garden_bp
    from .routes.garden_request import garden_request_bp
    from .routes.plant_report import plant_report_bp

    app.register_blueprint(auth_bp, url_prefix="/auth")
    app.register_blueprint(garden_bp, url_prefix="/garden")
    app.register_blueprint(garden_request_bp, url_prefix="/request")
    app.register_blueprint(plant_report_bp, url_prefix="/report")

    return app