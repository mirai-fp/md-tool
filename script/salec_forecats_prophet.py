import pandas as pd
from prophet import Prophet
import matplotlib.pyplot as plt

# Step 1: データの読み込み
# 気温データの読み込み
weather_data = pd.read_csv(
    "past_year_weather.csv",  # 気温データファイル名
    names=["date", "temperature_mean", "temperature_max", "temperature_min"],  # 列名を指定
    header=0,  # ヘッダーあり
    parse_dates=["date"]  # 日付列をdatetime形式に変換
)

# 売上データの読み込み（Shift-JIS対応）
sales_data = pd.read_csv(
    "sales_data_utf8.csv",  # 売上データファイル名
    encoding="utf-8",  # Shift-JISエンコーディングで読み込み
    parse_dates=["注文日"]  # 日付列をdatetime形式に変換
)

# Step 2: 子商品タイプごとに予測
results = []  # 各子商品タイプの予測結果を格納するリスト

# 気温データの日付を売上データの日付とマージ
sales_data = sales_data.merge(weather_data, left_on="注文日", right_on="date", how="left")

# 子商品タイプごとにグループ化
for child_product_type, group_data in sales_data.groupby("子商品タイプ"):
    print(f"子商品タイプ: {child_product_type}")
    
    # 日次売上データを作成
    daily_sales = group_data.groupby("注文日").agg({"注文数": "sum"}).reset_index()
    daily_sales = daily_sales.rename(columns={"注文日": "ds", "注文数": "y"})
    
    # 気温データをマージ
    daily_sales = pd.merge(daily_sales, weather_data, left_on="ds", right_on="date", how="left")
    
    # Prophetモデルの構築
    model = Prophet()
    model.add_regressor("temperature_mean")
    model.add_regressor("temperature_max")
    model.add_regressor("temperature_min")
    
    # モデルの学習
    model.fit(daily_sales)
    
    # 未来データ（週間天気予報）の準備
    future_weather = pd.DataFrame({
        "ds": pd.date_range(start="2025-01-21", periods=7, freq="D"),  # 週間天気予報の日付
        "temperature_mean": [8.4, 7.5, 6.0, 5.5, 6.2, 8.1, 9.0],      # 平均気温（例）
        "temperature_max": [13.3, 12.0, 10.5, 9.8, 10.2, 14.1, 15.0], # 最高気温（例）
        "temperature_min": [4.9, 3.5, 2.8, 2.2, 3.0, 4.5, 5.0]        # 最低気温（例）
    })
    
    # 予測
    forecast = model.predict(future_weather)
    forecast["子商品タイプ"] = child_product_type  # 子商品タイプを予測結果に追加
    
    # 予測結果を保存
    results.append(forecast[["ds", "yhat", "yhat_lower", "yhat_upper", "子商品タイプ"]])
    
    # 予測結果の可視化（オプション）
    fig = model.plot(forecast)
    plt.title(f"売上予測: {child_product_type}")
    plt.show()

# すべての予測結果を結合
final_forecast = pd.concat(results)

# 予測結果の出力
print(final_forecast)
final_forecast.to_csv("sales_forecast_by_child_product_type.csv", index=False, encoding="utf-8-sig")

