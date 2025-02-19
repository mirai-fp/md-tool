import requests
import pandas as pd
from datetime import datetime, timedelta

# 過去1年分の日付を生成
def generate_date_range():
    end_date = datetime.now()
    start_date = end_date - timedelta(days=365)
    date_range = pd.date_range(start=start_date, end=end_date, freq="D")
    return date_range

# APIから気温データを取得
def fetch_weather_data(lat, lon):
    base_url = "https://archive-api.open-meteo.com/v1/archive"
    date_range = generate_date_range()
    all_data = []

    for date in date_range:
        params = {
            "latitude": lat,            # 緯度
            "longitude": lon,           # 経度
            "start_date": date.strftime("%Y-%m-%d"),
            "end_date": date.strftime("%Y-%m-%d"),
            "daily": "temperature_2m_max,temperature_2m_min,temperature_2m_mean",
            "timezone": "auto",
        }

        response = requests.get(base_url, params=params)
        if response.status_code == 200:
            data = response.json()
            daily_data = data["daily"]
            all_data.append({
                "date": daily_data["time"][0],
                "temperature_mean": daily_data["temperature_2m_mean"][0],
                "temperature_max": daily_data["temperature_2m_max"][0],
                "temperature_min": daily_data["temperature_2m_min"][0],
            })
        else:
            print(f"Failed to fetch data for {date.strftime('%Y-%m-%d')}. Error: {response.status_code}")

    return pd.DataFrame(all_data)

# 緯度・経度を指定（例：東京）
latitude = 35.6895
longitude = 139.6917

# 気温データを取得
weather_data = fetch_weather_data(latitude, longitude)

# データフレームを確認
print(weather_data)

# 必要に応じてCSVとして保存
weather_data.to_csv("past_year_weather.csv", index=False)

