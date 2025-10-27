from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
import logging
import numpy as np
from app.services.prediction_service import PredictionService
from app.utils.data_processor import DataProcessor
from app.models.schemas import CorrelationInput, CorrelationResponse

router = APIRouter()
logger = logging.getLogger(__name__)
prediction_service = PredictionService()
data_processor = DataProcessor()

@router.post('/correlation', response_model=CorrelationResponse)
async def calculate_correlation(data: CorrelationInput):
    """
    POST /api/v1/correlation
    Calculate correlation between metrics
    """
    try:
        if data is None:
            raise HTTPException(status_code=400, detail='No data provided')

        latitude = data.latitude
        longitude = data.longitude
        metric1 = data.metric1
        metric2 = data.metric2

        if not all([latitude, longitude, metric1, metric2]):
            raise HTTPException(status_code=400, detail='All parameters are required')

        logger.info(f"Correlation request: {metric1} vs {metric2}")

        # For simplicity, generate sample data
        # In production, you'd fetch actual historical data
        data1 = np.random.randn(100) * 10 + 20
        data2 = data1 * 0.5 + np.random.randn(100) * 2  # Correlated data

        result = prediction_service.calculate_correlation(data1, data2, metric1, metric2)

        return CorrelationResponse(**result)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Correlation calculation error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail={
                'error': 'Internal server error',
                'message': str(e)
            }
        )