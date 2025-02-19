# OpenJDK 17 の公式イメージを使用
FROM gradle:8.5-jdk17

# 作業ディレクトリを設定
WORKDIR /app

# プロジェクトのファイルをコピー
COPY . .

RUN gradle build

COPY --from=build /app/build/md-tool-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar

EXPOSE 8080

# アプリを実行
CMD ["java", "-jar", "/usr/local/lib/demo.jar"]

