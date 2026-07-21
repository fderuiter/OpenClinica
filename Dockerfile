# Stage 1: Build the application
FROM ubuntu:22.04 AS build
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y maven openjdk-17-jdk
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run Tomcat
FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y openjdk-17-jre wget tar curl

RUN groupadd -r clinica && useradd -r -g clinica -m -d /home/clinica clinica

RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.83/bin/apache-tomcat-9.0.83.tar.gz && \
    tar xzf apache-tomcat-9.0.83.tar.gz && \
    mv apache-tomcat-9.0.83 /usr/local/tomcat && \
    rm apache-tomcat-9.0.83.tar.gz

RUN mkdir -p /opt/clinica/data && \
    chown -R clinica:clinica /usr/local/tomcat /opt/clinica

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build --chown=clinica:clinica /app/web/target/OpenClinica-web-*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080

USER clinica
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
