# OpenJDK 17 の公式イメージを使用
FROM openjdk:17

# 作業ディレクトリを設定
WORKDIR /app

# プロジェクトのファイルをコピー
COPY . .

# Gradle でビルド
RUN ./gradlew build

# アプリを実行
CMD ["java", "-jar", "build/libs/md-tool.jar"]

