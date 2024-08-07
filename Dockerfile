FROM ubuntu:latest AS build

RUN sudo apt-get update
RUN sudo apt-get install openjdk-17-jdk -y
COPY . .

RUN sudo apt-get install maven -y
RUN sudo apt-get install git -y --fix-missing

RUN mkdir /goodbuy-security
RUN git clone https://github.com/Fiap-Pos-Tech-Arquitetura-Java/HackaPay-Security /goodbuy-security
WORKDIR /goodbuy-security
RUN mvn clean install

WORKDIR /
RUN mvn clean install

FROM openjdk:17-jdk-slim

EXPOSE 8082

COPY --from=build /target/HackaPay-Cartao-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]