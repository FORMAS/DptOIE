FROM openjdk:8-jdk-slim AS setup_models

WORKDIR /home/app/

RUN apt-get update && apt-get install -y wget curl zip unzip
RUN wget http://nlp.stanford.edu/software/stanford-corenlp-full-2018-02-27.zip
RUN unzip stanford-corenlp-full-2018-02-27.zip
RUN rm stanford-corenlp-full-2018-02-27.zip
RUN mv stanford-corenlp-full-2018-02-27 stanford-corenlp
RUN curl --remote-name-all https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/1-2515{/ud-treebanks-v2.1.tgz,/ud-documentation-v2.1.tgz,/ud-tools-v2.1.tgz}
RUN tar -xvzf ud-treebanks-v2.1.tgz
RUN rm ud-treebanks-v2.1.tgz
RUN mv ud-treebanks-v2.1 ud-treebanks
RUN mkdir -p ./pt-models

FROM openjdk:8-jdk-slim AS train_model_dep_parser

WORKDIR /home/app/

COPY --from=setup_models /home/app/ud-treebanks /home/app/ud-treebanks
COPY --from=setup_models /home/app/stanford-corenlp /home/app/stanford-corenlp

RUN mkdir -p ./pt-models
RUN java -mx6g -cp "stanford-corenlp/*" edu.stanford.nlp.parser.nndep.DependencyParser \
    -trainFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-train.conllu \
    -devFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-dev.conllu \
    -testFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-test.conllu \
    -model pt-models/pt-dep-parser.gz \
    -maxIter 10000 \
    -trainingThreads 6

RUN rm -rf ud-treebanks
RUN rm -rf stanford-corenlp

FROM openjdk:8-jdk-slim AS train_model_pos_tagger

WORKDIR /home/app/

COPY --from=setup_models /home/app/ud-treebanks /home/app/ud-treebanks
COPY --from=setup_models /home/app/stanford-corenlp /home/app/stanford-corenlp

RUN mkdir -p ./pt-models
RUN java -mx6g -cp "stanford-corenlp/*" edu.stanford.nlp.tagger.maxent.MaxentTagger \
    -trainFile format=TREES,ud-treebanks/UD_Portuguese-BR/pt_br-ud-train.conllu \
    -devFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-dev.conllu \
    -testFile ud-treebanks/UD_Portuguese-BR/pt_br-ud-test.conllu \
    -model pt-models/pt-pos-tagger.model \
    -arch generic \
    -nthreads 6

RUN rm -rf ud-treebanks
RUN rm -rf stanford-corenlp

FROM maven:3.8.2-jdk-8-slim AS build

WORKDIR /home/app

COPY pom.xml .
RUN mvn clean package -Dmaven.main.skip -Dmaven.test.skip && rm -r target

# To package the application
COPY src ./src
RUN mvn clean package -Dmaven.test.skip

FROM openjdk:8-jre-slim AS runtime
COPY --from=build /home/app/target/ExtraiClausulas-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/DptOIE.jar

ARG GITHUB_REPOSITORY=FORMAS/DptOIE

COPY . /home/app

WORKDIR /home/app

RUN if [ ! -f ./pt-models/pt-dep-parser.gz ] || [ ! -f pt-models/pt-pos-tagger.model ]; then \
        mkdir -p ./pt-models && \
        apt-get update && apt-get install -y wget zip unzip && \
        wget https://github.com/$GITHUB_REPOSITORY/releases/download/v1.0.0/DptOIE.zip && \
        unzip DptOIE.zip -d v1.0.0 && \
        mv v1.0.0/pt-models/pt-dep-parser.gz ./pt-models/pt-dep-parser.gz && \
        mv v1.0.0/pt-models/pt-pos-tagger.model ./pt-models/pt-pos-tagger.model && \
        rm DptOIE.zip && \
        rm -rf v1.0.0; \
    fi

EXPOSE 8080
