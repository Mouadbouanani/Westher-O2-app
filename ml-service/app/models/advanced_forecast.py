import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import StandardScaler
import joblib
import os

class AdvancedForecastModel:
    """Advanced weather forecasting using Random Forest"""

    def __init__(self, model_path='./trained_models'):
        self.model_path = model_path
        self.model = None
        self.scaler = StandardScaler()
        self._load_or_create_model()

    def _load_or_create_model(self):
        """Load existing model or create new one"""
        model_file = os.path.join(self.model_path, 'weather_rf_model.pkl')

        if os.path.exists(model_file):
            self.model = joblib.load(model_file)
            print(f"Loaded model from {model_file}")
        else:
            # Create new model
            self.model = RandomForestRegressor(
                n_estimators=100,
                max_depth=10,
                random_state=42
            )
            print("Created new Random Forest model")

    def prepare_features(self, df):
        """
        Prepare features for ML model

        Args:
            df: DataFrame with historical weather data

        Returns:
            np.array: Feature matrix
        """
        features = []

        # Time-based features
        df['hour'] = df['timestamp'].dt.hour
        df['day'] = df['timestamp'].dt.day
        df['month'] = df['timestamp'].dt.month
        df['day_of_week'] = df['timestamp'].dt.dayofweek
        df['day_of_year'] = df['timestamp'].dt.dayofyear

        # Lag features (previous values)
        for col in ['temperature', 'humidity', 'pressure']:
            if col in df.columns:
                df[f'{col}_lag1'] = df[col].shift(1)
                df[f'{col}_lag2'] = df[col].shift(2)
                df[f'{col}_lag7'] = df[col].shift(7)

        # Rolling statistics
        for col in ['temperature', 'humidity', 'pressure']:
            if col in df.columns:
                df[f'{col}_rolling_mean_7'] = df[col].rolling(window=7, min_periods=1).mean()
                df[f'{col}_rolling_std_7'] = df[col].rolling(window=7, min_periods=1).std()

        # Fill NaN values
        df = df.fillna(method='bfill').fillna(method='ffill')

        # Select feature columns
        feature_cols = [
            'hour', 'day', 'month', 'day_of_week', 'day_of_year',
            'humidity', 'pressure', 'wind_speed'
        ]

        # Add lag and rolling features
        for col in df.columns:
            if 'lag' in col or 'rolling' in col:
                feature_cols.append(col)

        # Filter only existing columns
        feature_cols = [col for col in feature_cols if col in df.columns]

        return df[feature_cols].values

    def train(self, df, target='temperature'):
        """
        Train the model

        Args:
            df: Training data
            target: Target variable to predict
        """
        X = self.prepare_features(df)
        y = df[target].values

        # Remove rows with NaN
        mask = ~np.isnan(X).any(axis=1) & ~np.isnan(y)
        X = X[mask]
        y = y[mask]

        if len(X) < 10:
            raise ValueError("Insufficient training data")

        # Scale features
        X_scaled = self.scaler.fit_transform(X)

        # Train model
        self.model.fit(X_scaled, y)

        # Save model
        os.makedirs(self.model_path, exist_ok=True)
        model_file = os.path.join(self.model_path, 'weather_rf_model.pkl')
        joblib.dump(self.model, model_file)

        print(f"Model trained and saved to {model_file}")
        print(f"Training score: {self.model.score(X_scaled, y):.3f}")

    def predict(self, df, days=7):
        """
        Make predictions

        Args:
            df: Historical data
            days: Number of days to forecast

        Returns:
            np.array: Predictions
        """
        if self.model is None:
            raise ValueError("Model not trained")

        predictions = []
        current_df = df.copy()

        for _ in range(days):
            # Prepare features
            X = self.prepare_features(current_df)
            X_scaled = self.scaler.transform(X[-1:])

            # Make prediction
            pred = self.model.predict(X_scaled)[0]
            predictions.append(pred)

            # Update dataframe for next prediction
            last_date = current_df['timestamp'].max()
            new_row = {
                'timestamp': last_date + pd.Timedelta(days=1),
                'temperature': pred,
                'humidity': current_df['humidity'].iloc[-1],
                'pressure': current_df['pressure'].iloc[-1],
                'wind_speed': current_df['wind_speed'].iloc[-1]
            }
            current_df = pd.concat([current_df, pd.DataFrame([new_row])], ignore_index=True)

        return np.array(predictions)