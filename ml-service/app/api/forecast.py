from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
import logging
from app.services.prediction_service import PredictionService
from app.models.schemas import WeatherDataInput, ForecastResponse

router = APIRouter()
logger = logging.getLogger(__name__)
prediction_service = PredictionService()

@router.post('/forecast', response_model=ForecastResponse)
async def get_forecast(data: WeatherDataInput):
    """
    POST /api/v1/forecast
    Get weather forecast
    """
    try:
        # Validate request
        if data is None:
            raise HTTPException(status_code=400, detail='No data provided')

        latitude = data.latitude
        longitude = data.longitude
        days = data.days
        historical_data = data.historicalData

        logger.info(f"Forecast request: lat={latitude}, lon={longitude}, days={days}")

        # Generate forecast
        result = prediction_service.forecast_weather(historical_data, days)
        result['latitude'] = latitude
        result['longitude'] = longitude

        return ForecastResponse(**result)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Forecast error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail={
                'error': 'Internal server error',
                'message': str(e)
            }
        )