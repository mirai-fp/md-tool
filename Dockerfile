# OpenJDK 17 の公式イメージを使用
FROM gradle:8.5-jdk17

# 作業ディレクトリを設定
WORKDIR /app

# プロジェクトのファイルをコピー
COPY . .


# アプリを実行
CMD ["gradle", "-p", "/app", "bootRun"]

