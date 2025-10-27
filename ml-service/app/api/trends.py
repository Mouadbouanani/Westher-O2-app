from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
import logging
from app.services.prediction_service import PredictionService
from app.models.schemas import TrendAnalysisInput, TrendAnalysisResponse

router = APIRouter()
logger = logging.getLogger(__name__)
prediction_service = PredictionService()

@router.post('/trends', response_model=TrendAnalysisResponse)
async def get_trends(data: TrendAnalysisInput):
    """
    POST /api/v1/trends
    Analyze weather trends
    """
    try:
        if data is None:
            raise HTTPException(status_code=400, detail='No data provided')

        latitude = data.latitude
        longitude = data.longitude
        metric = data.metric
        months = data.months
        historical_data = data.historicalData

        logger.info(f"Trend request: lat={latitude}, lon={longitude}, metric={metric}")

        # Analyze trends
        result = prediction_service.analyze_trends(historical_data, metric, months)

        return TrendAnalysisResponse(**result)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Trend analysis error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail={
                'error': 'Internal server error',
                'message': str(e)
            }
        )