FROM eclipse-temurin:17.0.15_6-jdk AS builder
WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

FROM eclipse-temurin:17.0.15_6-jre
WORKDIR /app
COPY --from=builder /app/target/latency-regression-1.0.0.jar app.jar
EXPOSE 8080
# dontinline keeps every application method a real frame, so profiles and
# probes see CheckoutService -> PricingService -> DiscountRules instead of one
# JIT-merged blob.
CMD ["java", "-XX:CompileCommand=quiet", "-XX:CompileCommand=dontinline,com.example.checkout.*::*", "-jar", "app.jar"]
