from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from app.models.schemas import HealthCheckResponse
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

def create_app():
    """Application factory"""
    app = FastAPI(
        title="ML Weather Prediction Service",
        description="Machine learning service for weather predictions and analytics",
        version="1.0.0"
    )
    
    # Add CORS middleware
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["http://localhost:8080", "http://localhost:3000"],
        allow_credentials=True,
        allow_methods=["GET", "POST", "OPTIONS"],
        allow_headers=["Content-Type"],
    )
    
    # Include API routes - import inside function to avoid circular imports
    from app.api.forecast import router as forecast_router
    from app.api.trends import router as trends_router
    from app.api.anomaly import router as anomaly_router
    from app.api.correlation import router as correlation_router
    
    app.include_router(forecast_router, prefix='/api/v1', tags=['forecast'])
    app.include_router(trends_router, prefix='/api/v1', tags=['trends'])
    app.include_router(anomaly_router, prefix='/api/v1', tags=['anomaly'])
    app.include_router(correlation_router, prefix='/api/v1', tags=['correlation'])
    
    # Health check endpoint
    @app.get('/health', response_model=HealthCheckResponse)
    async def health_check():
        return HealthCheckResponse(
            status='healthy',
            service='ML Weather Prediction Service',
            version='1.0.0'
        )
    
    # Error handlers
    @app.exception_handler(400)
    async def bad_request_handler(request, exc):
        return JSONResponse(
            status_code=400,
            content={'error': 'Bad Request', 'message': str(exc)}
        )
    
    @app.exception_handler(500)
    async def internal_error_handler(request, exc):
        return JSONResponse(
            status_code=500,
            content={'error': 'Internal Server Error', 'message': str(exc)}
        )
    
    return app