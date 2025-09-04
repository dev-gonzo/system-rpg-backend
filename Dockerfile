# Multi-stage build para otimizar o tamanho da imagem
FROM eclipse-temurin:21-jdk AS builder

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos do Maven wrapper e pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permissão de execução ao mvnw
RUN chmod +x ./mvnw

# Baixar dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src src

# Compilar aplicação
RUN ./mvnw clean package -DskipTests

# Segunda etapa - runtime
FROM eclipse-temurin:21-jre

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Criar diretório de logs
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Mudar para usuário não-root
USER appuser

# Expor porta da aplicação
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/auth/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]