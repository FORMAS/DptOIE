FROM maven:3.8.2-jdk-11-slim AS build

ARG GITHUB_REPOSITORY=FORMAS/DptOIE

WORKDIR /home/app
COPY src /home/app/src
COPY pom.xml /home/app
RUN rm -rf /home/app/target
RUN mvn -f /home/app/pom.xml clean package

# Download 1.0.0 models
RUN mkdir -p ./pt-models && \
    apt-get update && apt-get install -y wget zip unzip && \
    wget https://github.com/$GITHUB_REPOSITORY/releases/download/v1.0.0/DptOIE.zip && \
    unzip DptOIE.zip -d v1.0.0 && \
    mv v1.0.0/pt-models/pt-dep-parser.gz ./pt-models/pt-dep-parser.gz && \
    mv v1.0.0/pt-models/pt-pos-tagger.model ./pt-models/pt-pos-tagger.model && \
    rm DptOIE.zip && \
    rm -rf v1.0.0

FROM openjdk:11-jre-slim AS runtime
COPY --from=build /home/app/target/ExtraiClausulas-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/DptOIE.jar
COPY --from=build /home/app/pt-models/pt-dep-parser.gz /home/app/pt-models/pt-dep-parser.gz
WORKDIR /home/app
RUN mkdir -p ./pt-models
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/DptOIE.jar"]