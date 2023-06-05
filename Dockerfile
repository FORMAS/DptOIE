FROM openjdk:8-jdk-slim AS models

WORKDIR /home/app/

RUN apt-get update && apt-get install -y wget zip unzip
RUN wget http://nlp.stanford.edu/software/stanford-corenlp-full-2018-02-27.zip
RUN unzip stanford-corenlp-full-2018-02-27.zip
RUN rm stanford-corenlp-full-2018-02-27.zip
RUN mv stanford-corenlp-full-2018-02-27 stanford-corenlp
RUN curl --remote-name-all https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/1-2515{/ud-treebanks-v2.1.tgz,/ud-documentation-v2.1.tgz,/ud-tools-v2.1.tgz}
RUN tar -xvzf ud-treebanks-v2.1.tgz
RUN rm ud-treebanks-v2.1.tgz
RUN mv ud-treebanks-v2.1 ud-treebanks
RUN mkdir -p ./pt-models

# pt-dep-parser.gz
RUN java -mx6g -cp "stanford-corenlp/*" edu.stanford.nlp.parser.nndep.DependencyParser \
    -trainFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-train.conllu \
    -devFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-dev.conllu \
    -testFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-test.conllu \
    -model pt-models/pt-dep-parser.gz \

# pt-pos-tagger.model
RUN java -mx6g -cp "stanford-corenlp/*" edu.stanford.nlp.tagger.maxent.MaxentTagger \
    -trainFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-train.conllu \
    -devFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-dev.conllu \
    -testFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-test.conllu \
    -model pt-models/pt-pos-tagger.model

FROM maven:3.8.2-jdk-8-slim AS build

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

FROM openjdk:8-jre-slim AS runtime
COPY --from=build /home/app/target/ExtraiClausulas-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/DptOIE.jar
COPY --from=build /home/app/pt-models/pt-dep-parser.gz /home/app/pt-models/pt-dep-parser.gz
WORKDIR /home/app
RUN mkdir -p ./pt-models
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/DptOIE.jar"]