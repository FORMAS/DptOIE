# DptOIE and Models
DptOIE method uses the Dependence Parser and Part of Speech Tagger models trained with Stanford CoreNLP.


## This project contains:
- DptOIE's source code: An Open Information Extraction for Portuguese language.
- Dependence Parser and Part of Speech Tagger models trained with Stanford CoreNLP.

## Prerequisites to run from source code
- [Dataset CETEN200, WIKI200 and models](https://drive.google.com/file/d/11ktTybvwMBAVWch4ZKaGSkO22q_iTBKK/view?usp=sharing)
- Insert the file **pt-dep-parser.gz** in **pt-models** directory
- Import as Maven Project in Eclipse IDE

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

## How to cite
If you find this repo helpful, please consider citing:
```
@article{DBLP:journals/air/OliveiraCS23,
  author       = {Leandro Oliveira and
                  Daniela Barreiro Claro and
                  Marlo Souza},
  title        = {DptOIE: a Portuguese open information extraction based on dependency
                  analysis},
  journal      = {Artif. Intell. Rev.},
  volume       = {56},
  number       = {7},
  pages        = {7015--7046},
  year         = {2023},
  url          = {https://doi.org/10.1007/s10462-022-10349-4},
  doi          = {10.1007/s10462-022-10349-4},
  timestamp    = {Thu, 15 Jun 2023 21:57:36 +0200},
  biburl       = {https://dblp.org/rec/journals/air/OliveiraCS23.bib},
  bibsource    = {dblp computer science bibliography, https://dblp.org}
}
```

* 	Leandro Oliveira, Daniela Barreiro Claro, Marlo Souza: DptOIE: a Portuguese open information extraction based on dependency analysis. Artif. Intell. Rev. 56(7): 7015-7046 (2023).
