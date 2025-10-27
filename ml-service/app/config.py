import os

class Config:
    """Application configuration"""
    
    # Flask config
    DEBUG = os.getenv('DEBUG', 'True') == 'True'
    HOST = os.getenv('HOST', '0.0.0.0')
    PORT = int(os.getenv('PORT', 8000))
    
    # Model config
    MODEL_PATH = os.getenv('MODEL_PATH', './trained_models')
    
    # Backend API
    BACKEND_URL = os.getenv('BACKEND_URL', 'http://localhost:8080')
    
    # Logging
    LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')