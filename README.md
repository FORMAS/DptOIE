# DptOIE and Models
DptOIE method uses the Dependence Parser and Part of Speech Tagger models trained with Stanford CoreNLP.


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

## Reference

If you find this repo helpful, please consider citing:

```@Article{Oliveira2022,
author={Oliveira, Leandro
and Claro, Daniela Barreiro
and Souza, Marlo},
title={DptOIE: a Portuguese open information extraction based on dependency analysis},
journal={Artificial Intelligence Review},
year={2022},
month={Dec},
day={05},
issn={1573-7462},
doi={10.1007/s10462-022-10349-4},
url={https://doi.org/10.1007/s10462-022-10349-4}
}´´´

* GLAUBER, R. ; CLARO, D. B. ; OLIVEIRA, L. S. . Dependency Parser on Open Information Extraction for Portuguese Texts - DptOIE and DependentIE on IberLEF. In: Iberian Languages Evaluation Forum (IberLEF 2019), 2019, Bilbao, Spain. Proceedings of the Iberian Languages Evaluation Forum (IberLEF 2019) co-located with 35th Conference of the Spanish Society for Natural Language Processing (SEPLN 2019). Bilbao: CEUR, 2019. v. 2421. p. 442-448.
