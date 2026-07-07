# ===== Etapa 1: build =====
# Usa uma imagem com Maven + JDK 17 só para compilar e empacotar o projeto.
# Essa etapa não vai para a imagem final (fica só o resultado do "mvn package").
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /build

# Copia primeiro o pom.xml e baixa as dependências (aproveita o cache do Docker:
# se o pom.xml não mudar, essa camada não é reconstruída a cada build).
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Agora copia o código-fonte e empacota o jar (pula os testes para acelerar o build da imagem;
# os testes já rodam via CI/CD ou localmente antes do build).
COPY src ./src
RUN mvn -B package -DskipTests

# ===== Etapa 2: runtime =====
# Imagem final enxuta, só com o JRE (não precisa de Maven/JDK completo para rodar).
FROM amazoncorretto:17-alpine
WORKDIR /app

# Copia apenas o jar gerado na etapa de build anterior.
COPY --from=build /build/target/tds_app_exemplo-1.0.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
