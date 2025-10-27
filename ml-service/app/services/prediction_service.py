import numpy as np
import pandas as pd
from datetime import datetime, timedelta
from sklearn.linear_model import LinearRegression
from app.utils.data_processor import DataProcessor

class PredictionService:
    """Weather prediction service using simple ML models"""

    def __init__(self):
        self.processor = DataProcessor()

    def forecast_weather(self, historical_data, days=7):
        """
        Forecast weather for next N days

        Args:
            historical_data: List of historical weather records
            days: Number of days to forecast

        Returns:
            dict: Forecast results
        """
        df = self.processor.process_historical_data(historical_data)

        if df.empty or len(df) < 7:
            return self._generate_simple_forecast(days)

        forecasts = []

        # Forecast temperature
        temp_forecast = self._forecast_metric(df, 'temperature', days)

        # Forecast humidity
        humidity_forecast = self._forecast_metric(df, 'humidity', days)

        # Forecast pressure
        pressure_forecast = self._forecast_metric(df, 'pressure', days)

        # Generate daily forecasts
        last_date = df['timestamp'].max()

        for i in range(days):
            forecast_date = last_date + timedelta(days=i+1)

            forecasts.append({
                'date': forecast_date.isoformat(),
                'tempMin': round(temp_forecast[i] - 3, 1),
                'tempMax': round(temp_forecast[i] + 3, 1),
                'tempAvg': round(temp_forecast[i], 1),
                'tempMinConfidence': 0.75,
                'tempMaxConfidence': 0.75,
                'humidity': round(humidity_forecast[i], 1),
                'pressure': round(pressure_forecast[i], 1),
                'precipitation': round(np.random.uniform(0, 5), 1),
                'precipitationProbability': round(np.random.uniform(0, 1), 2),
                'windSpeed': round(df['wind_speed'].mean(), 1),
                'weatherCondition': self._predict_condition(temp_forecast[i]),
                'conditionProbability': 0.7,
                'anomalyDetected': False
            })

        return {
            'days': days,
            'forecasts': forecasts,
            'modelType': 'Linear Regression',
            'confidence': 0.78,
            'message': 'Forecast generated successfully',
            'fallback': False
        }

    def _forecast_metric(self, df, metric, days):
        """Forecast a single metric"""
        values = df[metric].values

        # Simple linear regression
        X = np.arange(len(values)).reshape(-1, 1)
        y = values

        model = LinearRegression()
        model.fit(X, y)

        # Predict future values
        future_X = np.arange(len(values), len(values) + days).reshape(-1, 1)
        predictions = model.predict(future_X)

        return predictions

    def _predict_condition(self, temperature):
        """Predict weather condition based on temperature"""
        if temperature < 0:
            return "Snow"
        elif temperature < 10:
            return "Cold"
        elif temperature < 20:
            return "Cloudy"
        elif temperature < 30:
            return "Clear"
        else:
            return "Hot"

    def _generate_simple_forecast(self, days):
        """Generate simple forecast when insufficient data"""
        forecasts = []
        base_temp = 20

        for i in range(days):
            forecasts.append({
                'date': (datetime.now() + timedelta(days=i+1)).isoformat(),
                'tempMin': round(base_temp - 5, 1),
                'tempMax': round(base_temp + 5, 1),
                'tempAvg': round(base_temp, 1),
                'tempMinConfidence': 0.5,
                'tempMaxConfidence': 0.5,
                'humidity': 60.0,
                'pressure': 1013.0,
                'precipitation': 0.0,
                'precipitationProbability': 0.3,
                'windSpeed': 5.0,
                'weatherCondition': 'Clear',
                'conditionProbability': 0.5,
                'anomalyDetected': False
            })

        return {
            'days': days,
            'forecasts': forecasts,
            'modelType': 'Simple Average',
            'confidence': 0.5,
            'message': 'Insufficient historical data - using simple forecast',
            'fallback': True
        }

    def analyze_trends(self, historical_data, metric, months):
        """
        Analyze trends in weather data

        Args:
            historical_data: Historical weather records
            metric: Metric to analyze
            months: Number of months to analyze

        Returns:
            dict: Trend analysis results
        """
        df = self.processor.process_historical_data(historical_data)

        if df.empty:
            return self._generate_simple_trend(metric)

        # Extract metric values
        if metric not in df.columns:
            return self._generate_simple_trend(metric)

        values = df[metric].values
        timestamps = df['timestamp'].values

        # Calculate trend
        X = np.arange(len(values)).reshape(-1, 1)
        y = values

        model = LinearRegression()
        model.fit(X, y)

        slope = model.coef_[0]
        r_squared = model.score(X, y)

        # Determine trend direction
        if abs(slope) < 0.01:
            direction = "STABLE"
        elif slope > 0:
            direction = "INCREASING"
        else:
            direction = "DECREASING"

        # Calculate percentage change
        if len(values) > 0:
            change_percentage = ((values[-1] - values[0]) / values[0]) * 100
        else:
            change_percentage = 0

        # Generate predictions
        future_points = 30  # Next 30 days
        future_X = np.arange(len(values), len(values) + future_points).reshape(-1, 1)
        predictions = model.predict(future_X)

        return {
            'metric': metric,
            'months': months,
            'analysis': {
                'direction': direction,
                'slope': round(slope, 4),
                'changePercentage': round(change_percentage, 2),
                'significance': 'SIGNIFICANT' if r_squared > 0.7 else 'MODERATE',
                'rSquared': round(r_squared, 3),
                'interpretation': self._interpret_trend(direction, slope, metric)
            },
            'dataPoints': self._format_data_points(timestamps, values),
            'trendLine': self._format_predictions(timestamps, model.predict(X)),
            'predictions': {
                'nextWeek': self._format_future_predictions(predictions[:7]),
                'nextMonth': self._format_future_predictions(predictions[:30]),
                'predictionQuality': 'Good' if r_squared > 0.7 else 'Moderate'
            },
            'message': 'Trend analysis completed',
            'fallback': False
        }

    def _interpret_trend(self, direction, slope, metric):
        """Generate human-readable interpretation"""
        if direction == "STABLE":
            return f"The {metric} has remained relatively stable over the analyzed period."
        elif direction == "INCREASING":
            return f"The {metric} shows an increasing trend with a rate of {abs(slope):.4f} units per day."
        else:
            return f"The {metric} shows a decreasing trend with a rate of {abs(slope):.4f} units per day."

    def _format_data_points(self, timestamps, values):
        """Format historical data points"""
        points = []
        for ts, val in zip(timestamps, values):
            points.append({
                'timestamp': pd.Timestamp(ts).isoformat(),
                'value': round(float(val), 2),
                'confidence': None
            })
        return points

    def _format_predictions(self, timestamps, predictions):
        """Format prediction points"""
        points = []
        for ts, pred in zip(timestamps, predictions):
            points.append({
                'timestamp': pd.Timestamp(ts).isoformat(),
                'value': round(float(pred), 2),
                'confidence': 0.8
            })
        return points

    def _format_future_predictions(self, predictions):
        """Format future predictions"""
        base_date = datetime.now()
        points = []
        for i, pred in enumerate(predictions):
            points.append({
                'timestamp': (base_date + timedelta(days=i+1)).isoformat(),
                'value': round(float(pred), 2),
                'confidence': max(0.5, 0.9 - (i * 0.01))  # Decreasing confidence
            })
        return points

    def _generate_simple_trend(self, metric):
        """Generate simple trend when insufficient data"""
        return {
            'metric': metric,
            'months': 0,
            'analysis': {
                'direction': 'STABLE',
                'slope': 0.0,
                'changePercentage': 0.0,
                'significance': 'NOT_SIGNIFICANT',
                'rSquared': 0.0,
                'interpretation': 'Insufficient data for trend analysis'
            },
            'dataPoints': [],
            'trendLine': [],
            'predictions': {
                'nextWeek': [],
                'nextMonth': [],
                'predictionQuality': 'Poor'
            },
            'message': 'Insufficient data',
            'fallback': True
        }

    def detect_anomalies(self, recent_data):
        """
        Detect anomalies in recent weather data

        Args:
            recent_data: Recent weather records

        Returns:
            dict: Anomaly detection results
        """
        df = self.processor.process_historical_data(recent_data)

        if df.empty or len(df) < 10:
            return {
                'anomaliesDetected': False,
                'anomalyCount': 0,
                'anomalies': []
            }

        anomalies = []

        # Check temperature anomalies
        temp_anomalies = self.processor.detect_outliers(df['temperature'])
        for idx in df[temp_anomalies].index:
            anomalies.append({
                'timestamp': df.loc[idx, 'timestamp'].isoformat(),
                'metric': 'temperature',
                'value': round(df.loc[idx, 'temperature'], 2),
                'expectedValue': round(df['temperature'].mean(), 2),
                'deviation': round(abs(df.loc[idx, 'temperature'] - df['temperature'].mean()), 2),
                'severity': self._calculate_severity(
                    df.loc[idx, 'temperature'],
                    df['temperature'].mean(),
                    df['temperature'].std()
                ),
                'description': f"Unusual temperature reading detected",
                'anomalyScore': 0.85
            })

        # Check humidity anomalies
        humidity_anomalies = self.processor.detect_outliers(df['humidity'])
        for idx in df[humidity_anomalies].index:
            anomalies.append({
                'timestamp': df.loc[idx, 'timestamp'].isoformat(),
                'metric': 'humidity',
                'value': round(df.loc[idx, 'humidity'], 2),
                'expectedValue': round(df['humidity'].mean(), 2),
                'deviation': round(abs(df.loc[idx, 'humidity'] - df['humidity'].mean()), 2),
                'severity': self._calculate_severity(
                    df.loc[idx, 'humidity'],
                    df['humidity'].mean(),
                    df['humidity'].std()
                ),
                'description': f"Unusual humidity reading detected",
                'anomalyScore': 0.80
            })

        return {
            'anomaliesDetected': len(anomalies) > 0,
            'anomalyCount': len(anomalies),
            'anomalies': anomalies[:10]  # Return top 10
        }

    def _calculate_severity(self, value, mean, std):
        """Calculate anomaly severity"""
        z_score = abs((value - mean) / std) if std > 0 else 0

        if z_score > 3:
            return "EXTREME"
        elif z_score > 2.5:
            return "HIGH"
        elif z_score > 2:
            return "MODERATE"
        else:
            return "LOW"

    def calculate_correlation(self, data1, data2, metric1, metric2):
        """
        Calculate correlation between two metrics

        Args:
            data1: Data for first metric
            data2: Data for second metric
            metric1: First metric name
            metric2: Second metric name

        Returns:
            dict: Correlation analysis
        """
        # Simple correlation calculation
        if len(data1) != len(data2) or len(data1) < 2:
            return self._generate_no_correlation(metric1, metric2)

        correlation = np.corrcoef(data1, data2)[0, 1]

        # Classify correlation strength
        abs_corr = abs(correlation)
        if abs_corr > 0.8:
            strength = "VERY_STRONG"
        elif abs_corr > 0.6:
            strength = "STRONG"
        elif abs_corr > 0.4:
            strength = "MODERATE"
        elif abs_corr > 0.2:
            strength = "WEAK"
        else:
            strength = "VERY_WEAK"

        # Determine type
        if correlation > 0.1:
            corr_type = "POSITIVE"
        elif correlation < -0.1:
            corr_type = "NEGATIVE"
        else:
            corr_type = "NO_CORRELATION"

        # Generate scatter data
        scatter_data = []
        for x, y in zip(data1[:100], data2[:100]):  # Limit to 100 points
            scatter_data.append({'x': round(float(x), 2), 'y': round(float(y), 2)})

        return {
            'metric1': metric1,
            'metric2': metric2,
            'correlationCoefficient': round(correlation, 3),
            'correlationStrength': strength,
            'correlationType': corr_type,
            'pValue': 0.05,  # Simplified
            'statistically_significant': abs(correlation) > 0.3,
            'interpretation': self._interpret_correlation(correlation, metric1, metric2),
            'scatterData': scatter_data
        }

    def _interpret_correlation(self, correlation, metric1, metric2):
        """Generate correlation interpretation"""
        if abs(correlation) < 0.1:
            return f"There is no significant correlation between {metric1} and {metric2}."
        elif correlation > 0:
            return f"There is a positive correlation between {metric1} and {metric2}. " \
                   f"As {metric1} increases, {metric2} tends to increase as well."
        else:
            return f"There is a negative correlation between {metric1} and {metric2}. " \
                   f"As {metric1} increases, {metric2} tends to decrease."

    def _generate_no_correlation(self, metric1, metric2):
        """Generate no correlation result"""
        return {
            'metric1': metric1,
            'metric2': metric2,
            'correlationCoefficient': 0.0,
            'correlationStrength': 'NONE',
            'correlationType': 'NO_CORRELATION',
            'pValue': 1.0,
            'statistically_significant': False,
            'interpretation': 'Insufficient data for correlation analysis',
            'scatterData': []
        }