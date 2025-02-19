import chardet

# ファイルのエンコーディングを検出
with open('sales_data.csv', 'rb') as f:
    result = chardet.detect(f.read())

# 検出したエンコーディングを使ってファイルを読み込む
with open('sales_data.csv', 'r', encoding=result['encoding'], errors='ignore') as f_in:
    with open('sales_data_utf8.csv', 'w', encoding='utf-8') as f_out:
        f_out.write(f_in.read())

# UTF-8 に変換したファイルを読み込む
import pandas as pd
sales_data = pd.read_csv('sales_data_utf8.csv')

# 読み込んだデータを確認
print(sales_data.head())

