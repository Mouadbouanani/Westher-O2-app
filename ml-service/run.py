import uvicorn
from app.main import create_app
from app.config import Config

if __name__ == '__main__':
    app = create_app()

    print(f"""
    ============================================================
    ML Weather Prediction Service (FastAPI)                          
    Running on: http://{Config.HOST}:{Config.PORT}                   
    Health check: http://{Config.HOST}:{Config.PORT}/health          
    Docs: http://{Config.HOST}:{Config.PORT}/docs                    
    ============================================================
    """)

    uvicorn.run(
        "run:app",
        host=Config.HOST,
        port=Config.PORT,
        reload=Config.DEBUG
    )