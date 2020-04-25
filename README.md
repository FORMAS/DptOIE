# DptOIE and Models
DptOIE method uses the Dependence Parser and Part of Speech Tagger models trained with Stanford CoreNLP.

This work is described in the paper **"DptOIE: A Portuguese Open Information Extraction system based on Dependency Analysis"** under submission at Computer Speech and Language Journal.

## This project contains:
- DptOIE's source code: An Open Information Extraction for Portuguese language.
- Dependenct Parser and Part of Speech Tagger models trained with Stanford CoreNLP.

## Prerequisites to run from source code
- [Dataset CETEN200 e WIKI200](https://drive.google.com/open?id=18o4vvQOCZyfhA31yJQ0RxRx7KifDeL-9)
- Insert the file **pt-dep-parser.gz** in **pt-models** directory

## How to use
To run the DptOIE.jar
```
 java -jar DptOIE.jar -sentencesIN **sentences_file_path**
```
To use the module that handles subordinate clause
```
-SC true
```
To use the module that handles coordinated conjunctions
```
-CC true
```
To use the module that handles appositive
```
-appositive 1
```
To apply transitivity
```
-appositive 2
```
DptOIE is independent of dependency parser, so it can receive annotated sentences with other dependency parsers, as long as they are in ConLL-U format with the same tagsets of [the Google treebank Treebanks Universal V2,1](https://lindat.mff.cuni.cz/repository/xmlui/handle/11234/1-2515#show-files).

To run DptOIE from a dependency tree in ConLL-U format
```
java -jar DptOIE.jar -sentencesIN 'sentences_file_path' -dependencyTreeIN 'dependency_Tree_conllu_format'
```
## Contributing
[Please use the GitHub issue tracker](https://github.com/FORMAS/DptOIE/issues)

## Authors
* Leandro Souza de Oliveira
* [Daniela Barreiro Claro](http://formas.ufba.br/dclaro/)
