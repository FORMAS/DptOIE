# DptOIE 
DPToie is an Open Information Extraction method for Brazilian Portuguese language. It extracts triples based on the Universal Dependencies. 

## To run
- [Colab](https://colab.research.google.com/drive/1-vFNmw9lx2cilpTwDVAqTF_m9yeCKG8h?usp=sharing)
- [Models](https://drive.google.com/drive/folders/1U7p3o2dvWMN0xecocCcsHh7uPmaW1Zmh?usp=drive_link)

## How to use the single Java
To run the DptOIE.jar
- Insert the file **DptOIE.jar** and **pt-models** in your directory

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
DptOIE is can receive CONLL-U format to annotate the sentences with the dependency parser. 

To run DptOIE from a dependency tree in ConLL-U format
```
java -jar DptOIE.jar -sentencesIN 'sentences_file_path' -dependencyTreeIN 'dependency_Tree_conllu_format'
```
## Contributing
[Please use the GitHub issue tracker](https://github.com/FORMAS/DptOIE/issues)

## Authors
* Leandro Souza de Oliveira
* [Daniela Barreiro Claro](http://formas.ufba.br/dclaro/)
* [Oliveira, L. and Claro, D. B. and Souza, M. DptOIE: a Portuguese open information extraction based on dependency analysis. ARTIFICIAL INTELLIGENCE REVIEW, v. 56, p. 7015-7046, 2023](https://dl.acm.org/doi/abs/10.1007/s10462-022-10349-4)

## How to cite
If you find this repo helpful, please consider citing:
* 	Leandro Oliveira, Daniela Barreiro Claro, Marlo Souza: DptOIE: a Portuguese open information extraction based on dependency analysis. Artif. Intell. Rev. 56(7): 7015-7046 (2023).
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


