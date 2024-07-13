
## TODO:
- [ ] Módulos
    - [x] oração subordinada
    - [ ] conjunções coordenadas
    - [ ] aposto
    - [ ] transitividade
- [ ] Pré-processamento
- [ ] Extração
- [ ] Estrutura interna
    - [ ] SujeitoRelacao
    - [ ] ArgumentoConcatenado
## Dicionário

1. **HEAD**: token no qual o token atual tem dependência.
2. **deprel**: Relação de dependência com o HEAD.
3. **postag**: categoria gramatical da palavra.
4. **ID**: identificador do token que segue ordem crescente começando do '1'

### Testes
1. **heuristicasVerificaConjRelacao**:
## Extração
Os seguintes módulos estão disponíveis:
1. oração subordinada
2. conjunções coordenadas
3. aposto
4. transitividade
   A extração dos fatos na acontece por meio de um script seguindo a ordem abaixo:
1. [Encontrar Sujeito](#encontrar-sujeito)
2. [Encontrar Relação](#encontrar-relacao)
### Encontrar Sujeito

tokens marcados como por 'nsubj' ou 'nsubj:pass'.

para cada sujeito na frase vai fazer a busca em profundidade em cada um dos tokens filhos  marcando se é um sujeito relacionado com o sujeito principal se cumprirem com algum dos parâmetros abaixo:
1. deprel ''nummod"
2. deprel "advmod"
3. deprel "appos" e postag "NUM"
4. deprel "nmod"
5. deprel "amod"
6. deprel "dep"
7. deprel "obj"
8. deprel "det"
9. deprel "case"
10. deprel "punct" e é uma das pontuações:  "(", ")", "{", "}", """, "'", "\[", "]", '","
11. deprel "conj" e postag "conj"

#### oração subordinada
Se o módulo de oração subordinada estiver habilitado, vai buscar os pronomes relativos nesse momento também

Para cada um dos tokens de sujeitos encontrados, verifica se ele tem o postag "PRON" e tem dependência de outro token cujo deprel é "acl:relcl"

Então irá fazer a mesma busca em profundidade em todos os tokens filhos desses sujeitos para marcar como relacionado se algum dos critérios abaixo for atendido:
1. ID do token filho é menor que o da raiz e compre algum critério abaixo:
    1. deprel "obj"
    2. deprel "dep"
    3. deprel "nummod"
    4. deprel "advmod"
    5. deprel "det"
    6. deprel "case"
    7. deprel "punct" e não é uma virgula (",")
2. Ou o ID do filho é maior e atende algum dos critérios abaixo:
    1. deprel "nummod"
    2. deprel "conj" e se o token é o núcleo de uma relação usando regras abaixo:
        1. postag "VERB"
        2. se algum token filho tiver deprel "cc" não pode ser nenhum dos valores "e", "ou", e ",".
        3. Não pode ter algum token filho com ID menor que o token verificado com deprel diferente de "punct" todos os tokens com ID entre eles deve respeitar os critérios:
            1. não ter nenhum dos postag "ADP" e "DET"
            2. não nenhum dos deprel "punct", "mark", "advmod", "aux", "cop" e "expl:pv"
    3. deprel "advmod"
    4. deprel "appos" e postag "NUM"
    5. deprel "nmod"
    6. deprel "amod"
    7. deprel "dep"
    8. deprel "obj" e ID do filho deve se maior que a token raiz da busca em profundidade
    9. deprel "det"
    10. deprel "case"
    11. deprel "punct" e é uma das pontuações:  "(", ")", "{", "}", """, "'", "\[", "]", '","
### Encontrar Relação

Para cada Sujeito encontrado adiciona o HEAD dele como relação. Agora irá fazer uma busca em profundidade em todos os filhos dos tokens filhos de cada relação e adicionar como um pedaço da relação se cumprir com **QUALQUER** requisito abaixo:

1. Todas as condições:
    1. Qualquer uma dessas condições:
        1. ID do token nó é menor que o ID da relação raiz e ID do token nó é maior que o ID do sujeito e o ID do sujeito é menor que o ID da relação
        2. ID do token nó pe  menor que o ID da relação e ID do token nó é menor que o ID do sujeito e ID do sujeito é maior que o ID da relação
    2. Qualquer uma das condições
        1. deprel do token "aux:pas"
        2. deprel do token "obj"
        3. deprel do token "iobj"
        4. deprel do token "advmod"
        5. deprel do token "cop"
        6. deprel do token "aux"
        7. deprel do token "expl:p"
        8. deprel do token "mark"
        9. deprel do token "punct" e tem o valor ',' ou '--'
2. Todas as condições:
    1. ID do token é maior que o ID do token usado inicialmente como relação
    2. Qualquer uma das condições:
        1. deprel do token "flat"
        2. deprel do token "expl:pv"
        3. deprel do token "punct" e o valor é '-'
    3. deprel do token "acl:part" e algum token filho do token pai desse nó tem que ter id maior que o pai e ter deprel "acl:part"
        1. Nesse caso o núcleo da relação será modificado para o nó atual, substituindo a raiz oda busca

Finalizada a busca em profundidade, será verificado se é necessário modificar o núcleo da relação que acontece em casos de dois verbos na relação, onde o núcleo é um verbo e os tokens filhos estão associados ao outro.
começa verificando se o token de relação atual tem algum filho que é um sujeito. Se tiver, o token de teste será o token de relação atual. Se não, o token de teste será o token que é o "head" do token de relação atual.  Em seguida, o código percorre os filhos do token de teste. Se algum desses filhos for uma conjunção e passar no teste heuristicasVerificaConjRelacao, ele é adicionado a uma lista de tokens de conjunção e pai sujeito.
Depois, o código percorre os tokens de relação encontrados na lista de tokens de conjunção e pai sujeito. Para cada token de relação que não seja o token de relação atual, o código percorre seus filhos. Se algum desses filhos não foi visitado e não é um filho sujeito, o código verifica se o filho é um dos tipos de dependência especificados:
1. ID to token maior que o ID do head
2. alguma condição verdadeira:
1. deprel "nmod"
2. deprel contendo "xcomp"
3. deprel "dobj"  
4. deprel "obj"  
5. deprel "iobj"
6. deprel "acl:relcl"  
7. deprel "nummod"  
8. deprel "advmod"
9. deprel "appos" e CPOSTAG diferente de "PROPN"
10. deprel "amod"
11. deprel "ccomp"  e algum token filho contendo deprel "subj"
12. deprel "advcl"  e algum token filho contendo deprel "subj"
13. deprel "acl:part"
14. deprel "dep"
15. deprel "punct" e é uma das pontuações:  "(", ")", "{", "}", """, "'", "\[", "]", '","

