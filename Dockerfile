# Etapa 1: compila el jar con el wrapper de Maven (los tests corren en el CI, no aca)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# primero solo el pom, asi las dependencias quedan en cache mientras no cambie
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Etapa 2: solo el JRE y el jar
FROM eclipse-temurin:25-jre
WORKDIR /app

# no corre como root
RUN useradd --system --no-create-home chispas
USER chispas

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
