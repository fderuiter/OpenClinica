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
RUN groupadd -r tomcat && useradd -r -g tomcat -m -d /opt/tomcat tomcat
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.83/bin/apache-tomcat-9.0.83.tar.gz && \
    tar xzf apache-tomcat-9.0.83.tar.gz && \
    mv apache-tomcat-9.0.83 /usr/local/tomcat && \
    rm apache-tomcat-9.0.83.tar.gz
RUN rm -rf /usr/local/tomcat/webapps/* && \
    chown -R tomcat:tomcat /usr/local/tomcat && \
    mkdir -p /opt/clinica/data && \
    chown -R tomcat:tomcat /opt/clinica
COPY --from=build --chown=tomcat:tomcat /app/web/target/OpenClinica-web-*.war /usr/local/tomcat/webapps/ROOT.war
USER tomcat
EXPOSE 8080
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
