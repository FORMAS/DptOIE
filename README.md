[![Open In Colab](https://colab.research.google.com/assets/colab-badge.svg)](https://colab.research.google.com/github/FORMAS/DptOIE/blob/main/notebook/dptoie-colab.ipynb)

# DptOIE and Models
DptOIE method uses the Dependence Parser and Part of Speech Tagger models trained with Stanford CoreNLP.

This work is described in the paper **"DptOIE: A Portuguese Open Information Extraction system based on Dependency Analysis"** under submission at Computer Speech and Language Journal.

## This project contains:
- DptOIE's source code: An Open Information Extraction for Portuguese language.
- Dependenct Parser and Part of Speech Tagger models trained with Stanford CoreNLP.

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

## Cite
* GLAUBER, R. ; CLARO, D. B. ; OLIVEIRA, L. S. . Dependency Parser on Open Information Extraction for Portuguese Texts - DptOIE and DependentIE on IberLEF. In: Iberian Languages Evaluation Forum (IberLEF 2019), 2019, Bilbao, Spain. Proceedings of the Iberian Languages Evaluation Forum (IberLEF 2019) co-located with 35th Conference of the Spanish Society for Natural Language Processing (SEPLN 2019). Bilbao: CEUR, 2019. v. 2421. p. 442-448.

* Oliveira L., Claro D. B., Souza M., DptOIE: A Portuguese Open Information Extraction based on Dependency Analysis. Artificial Intelligence Review (**under review**)
