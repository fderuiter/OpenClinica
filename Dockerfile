# Stage 1: Build the application
FROM ubuntu:22.04 AS build
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y maven openjdk-17-jdk
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests \
    -DdbHost=db \
    -DdbType=postgres \
    -DdbUser=clinica \
    -DdbPass=clinica \
    -Ddb=clinica \
    -DdbPort=5432

# Stage 2: Run Tomcat
FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y openjdk-17-jre wget tar
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.83/bin/apache-tomcat-9.0.83.tar.gz && \
    tar xzf apache-tomcat-9.0.83.tar.gz && \
    mv apache-tomcat-9.0.83 /usr/local/tomcat && \
    rm apache-tomcat-9.0.83.tar.gz
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/web/target/OpenClinica-web-*.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir -p /opt/clinica && chown -R 1001:1001 /opt/clinica
COPY --from=build /app/core/src/main/resources/properties/CRF_Design_Template_v3.10.xls /opt/clinica/fallback_template.xls
EXPOSE 8080
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
# Stage 3: Run Modern
FROM ubuntu:22.04 AS modern
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y openjdk-17-jre
WORKDIR /app
COPY --from=build /app/modern/target/OpenClinica-modern-*.jar /app/modern.jar
EXPOSE 8080
CMD ["java", "-jar", "modern.jar"]
