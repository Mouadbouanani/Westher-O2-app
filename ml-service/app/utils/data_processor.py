import numpy as np
import pandas as pd
from datetime import datetime, timedelta

class DataProcessor:
    """Process weather data for ML models"""

    @staticmethod
    def process_historical_data(historical_data):
        """
        Convert historical data to pandas DataFrame

        Args:
            historical_data: List of weather data from backend

        Returns:
            pd.DataFrame: Processed data
        """
        if not historical_data:
            return pd.DataFrame()

        # Extract relevant fields
        processed = []
        for record in historical_data:
            processed.append({
                'timestamp': record.get('timestamp'),
                'temperature': record.get('temperature'),
                'humidity': record.get('humidity'),
                'pressure': record.get('pressure'),
                'wind_speed': record.get('windSpeed'),
                'precipitation': record.get('precipitation', 0),
                'cloud_coverage': record.get('cloudCoverage', 0)
            })

        df = pd.DataFrame(processed)

        # Convert timestamp to datetime
        df['timestamp'] = pd.to_datetime(df['timestamp'])

        # Sort by timestamp
        df = df.sort_values('timestamp')

        # Fill missing values
        df = df.fillna(method='ffill').fillna(method='bfill')

        return df

    @staticmethod
    def extract_features(df):
        """
        Extract features for ML model

        Args:
            df: DataFrame with weather data

        Returns:
            np.array: Feature matrix
        """
        if df.empty:
            return np.array([])

        features = []

        # Basic statistics
        features.append(df['temperature'].mean())
        features.append(df['temperature'].std())
        features.append(df['humidity'].mean())
        features.append(df['pressure'].mean())
        features.append(df['wind_speed'].mean())

        # Time-based features
        df['hour'] = df['timestamp'].dt.hour
        df['day_of_week'] = df['timestamp'].dt.dayofweek
        df['month'] = df['timestamp'].dt.month

        features.append(df['hour'].mean())
        features.append(df['day_of_week'].mean())
        features.append(df['month'].mean())

        # Trend features
        if len(df) > 1:
            temp_trend = np.polyfit(range(len(df)), df['temperature'], 1)[0]
            features.append(temp_trend)
        else:
            features.append(0)

        return np.array(features)

    @staticmethod
    def calculate_moving_average(series, window=7):
        """Calculate moving average"""
        return series.rolling(window=window, min_periods=1).mean()

    @staticmethod
    def detect_outliers(series, threshold=3):
        """Detect outliers using z-score"""
        z_scores = np.abs((series - series.mean()) / series.std())
        return z_scores > threshold