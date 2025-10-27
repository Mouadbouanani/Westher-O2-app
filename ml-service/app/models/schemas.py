from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
import numpy as np


class HealthCheckResponse(BaseModel):
    status: str
    service: str
    version: str


class WeatherDataInput(BaseModel):
    latitude: float = Field(..., ge=-90, le=90, description="Latitude")
    longitude: float = Field(..., ge=-180, le=180, description="Longitude")
    historicalData: Optional[List[dict]] = []
    days: Optional[int] = 7


class WeatherForecast(BaseModel):
    date: str
    tempMin: float
    tempMax: float
    tempAvg: float
    tempMinConfidence: float
    tempMaxConfidence: float
    humidity: float
    pressure: float
    precipitation: float
    precipitationProbability: float
    windSpeed: float
    weatherCondition: str
    conditionProbability: float
    anomalyDetected: bool


class ForecastResponse(BaseModel):
    days: int
    forecasts: List[WeatherForecast]
    modelType: str
    confidence: float
    message: str
    fallback: bool
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class TrendAnalysisResponse(BaseModel):
    metric: str
    months: int
    analysis: dict
    dataPoints: List[dict]
    trendLine: List[dict]
    predictions: dict
    message: str
    fallback: bool


class AnomalyDetectionResponse(BaseModel):
    anomaliesDetected: bool
    anomalyCount: int
    anomalies: List[dict]


class CorrelationResponse(BaseModel):
    metric1: str
    metric2: str
    correlationCoefficient: float
    correlationStrength: str
    correlationType: str
    pValue: float
    statistically_significant: bool
    interpretation: str
    scatterData: List[dict]


class TrendAnalysisInput(BaseModel):
    latitude: float = Field(..., ge=-90, le=90, description="Latitude")
    longitude: float = Field(..., ge=-180, le=180, description="Longitude")
    metric: str = "temperature"
    months: Optional[int] = 6
    historicalData: Optional[List[dict]] = []


class AnomalyDetectionInput(BaseModel):
    latitude: float = Field(..., ge=-90, le=90, description="Latitude")
    longitude: float = Field(..., ge=-180, le=180, description="Longitude")
    recentData: Optional[List[dict]] = []


class CorrelationInput(BaseModel):
    latitude: float = Field(..., ge=-90, le=90, description="Latitude")
    longitude: float = Field(..., ge=-180, le=180, description="Longitude")
    metric1: str
    metric2: str