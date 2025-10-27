from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
import logging
from app.services.prediction_service import PredictionService
from app.models.schemas import AnomalyDetectionInput, AnomalyDetectionResponse

router = APIRouter()
logger = logging.getLogger(__name__)
prediction_service = PredictionService()

@router.post('/anomaly', response_model=AnomalyDetectionResponse)
async def detect_anomalies(data: AnomalyDetectionInput):
    """
    POST /api/v1/anomaly
    Detect weather anomalies
    """
    try:
        if data is None:
            raise HTTPException(status_code=400, detail='No data provided')

        latitude = data.latitude
        longitude = data.longitude
        recent_data = data.recentData

        logger.info(f"Anomaly detection request: lat={latitude}, lon={longitude}")

        # Detect anomalies
        result = prediction_service.detect_anomalies(recent_data)

        return AnomalyDetectionResponse(**result)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Anomaly detection error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail={
                'error': 'Internal server error',
                'message': str(e)
            }
        )