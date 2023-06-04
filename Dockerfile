FROM maven:3.8.2-jdk-11-slim AS build
WORKDIR /home/app
COPY src /home/app/src
COPY pom.xml /home/app
RUN rm -rf /home/app/target
RUN mvn -f /home/app/pom.xml clean package

# Download the model
RUN mkdir -p ./pt-models
RUN apt-get update && apt-get install -y wget unar
RUN export FILEID=1N2yJO8y7qodjQOroPVVN0SWSy20CuNzk && \
    export FILENAME=DptOIE.rar && \
    wget --load-cookies /tmp/cookies.txt "https://docs.google.com/uc?export=download&confirm=$(wget --quiet --save-cookies /tmp/cookies.txt --keep-session-cookies --no-check-certificate 'https://docs.google.com/uc?export=download&id=$FILEID' -O- | sed -rn 's/.*confirm=([0-9A-Za-z_]+).*/\1\n/p')&id=$FILEID" -O $FILENAME && rm -rf /tmp/cookies.txt && \
    unar $FILENAME && rm -rf $FILENAME && \
    mv DptOIE/pt-models/pt-dep-parser.gz ./pt-models/pt-dep-parser.gz && \
    rm -rf DptOIE

FROM openjdk:11-jre-slim AS runtime
COPY --from=build /home/app/target/ExtraiClausulas-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/DptOIE.jar
COPY --from=build /home/app/pt-models/pt-dep-parser.gz /home/app/pt-models/pt-dep-parser.gz
WORKDIR /home/app
RUN mkdir -p ./pt-models
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/DptOIE.jar"]