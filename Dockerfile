# Gradle 公式イメージ（JDK 17 含む）を使用
FROM gradle:8.5-jdk17 AS build

# 作業ディレクトリを設定
WORKDIR /app

# プロジェクトのファイルをコンテナにコピー
COPY --chown=gradle:gradle . .

# Gradle キャッシュを活用しながらビルド（デーモン無効化）
RUN gradle clean build

# JDK 17 のランタイムのみの軽量イメージを使用
FROM openjdk:17-jdk-slim

# 作業ディレクトリを作成
WORKDIR /app

# ビルドされた JAR をコピー
COPY --from=build /app/build/libs/md-tool-0.0.1-SNAPSHOT.jar app.jar

# ポート 8080 を開放
EXPOSE 8080

# Spring Boot アプリを JAR ファイルとして実行
CMD ["java", "-jar", "app.jar"]

