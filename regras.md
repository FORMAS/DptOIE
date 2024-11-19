métodos que faltam documentar
- adicionaArgumentoMarkAntesSujeito();
- moduloSubstituiApostoTransitividade(this.sujeitoRelacaoArgumentos);
- moduloExtracaoVerboLigacao();
- eliminaPreposicaoAntesSujeito();
- excluiSraSujeitoErrado();
- excluiSraRelacaoSemVerbo();
- eliminaTokenPontuacaoErrada();
- excluiExtracoesRepetidas();


# Sistema de Extração de Sujeito e Relação

## Visão Geral
O sistema realiza a análise sintática de sentenças, identificando e extraindo sujeitos e suas relações através de análise de dependências gramaticais. O processamento é feito através de busca em profundidade nos tokens (palavras) da sentença.

## Estruturas Principais

### Classe SujeitoRelacao
Representa a unidade básica de armazenamento para sujeitos e relações extraídos.

#### Atributos Principais
- `sujeito`: `Deque<Token>` - Armazena os tokens que compõem o sujeito
- `relacao`: `Deque<Token>` - Armazena os tokens que compõem a relação
- `indiceNucleoSujeito`: `int` - Índice do token principal do sujeito
- `indiceNucleoRelacao`: `int` - Índice do token principal da relação
- `vetorBooleanoTokensSujeitoVisitados`: `Boolean[]` - Controle de tokens visitados durante extração do sujeito
- `vetorBooleanoTokensRelacaoVisitados`: `Boolean[]` - Controle de tokens visitados durante extração da relação

### Processo de Extração

#### 1. Busca de Sujeito
```java
public void buscaSujeito(boolean SC)
```

##### Funcionamento
1. Inicializa vetor de controle para tokens visitados
2. Para cada token da sentença:
    - Verifica se é sujeito (`deprel` = "nsubj" ou "nsubj:pass")
    - Realiza busca em profundidade nos tokens filhos
    - Marca tokens relacionados ao sujeito principal

##### Critérios de Inclusão no Sujeito
Token filho é incluído se sua relação de dependência (`deprel`) for:
- `nummod`: Modificador numérico
- `advmod`: Modificador adverbial
- `appos`: Aposição (quando `postag` é "NUM")
- `nmod`, `amod`, `dep`, `obj`: Modificadores nominais e objetos
- `det`: Determinantes
- `case`: Marcadores de caso
- `punct`: Pontuações específicas
- `conj`: Conjunções (exceto quando `postag` é "VERB")

Vou ajustar a formatação Markdown para melhor organização e legibilidade:

#### 2. Processamento de Relações

```java
public void buscaRelacao() throws CloneNotSupportedException
```

##### Funcionamento
- Inicializa um vetor de controle para tokens visitados (`vetorBooleanoTokensVisitados`)
- Para cada sujeito identificado na sentença:
   - Obtém o token pai do sujeito
   - Adiciona o token pai na pilha para iniciar a busca em profundidade
   - Define o token pai como o núcleo da relação
   - Marca o token pai e seus filhos que atendem aos critérios de inclusão

##### Critérios de Inclusão na Relação
Um token filho é incluído na relação se sua relação de dependência (deprel) for uma das seguintes:

- `aux:pass`: Auxiliar passivo
- `obj`: Objeto
- `iobj`: Objeto indireto
- `advmod`: Modificador adverbial
- `cop`: Verbo copulativo
- `aux`: Auxiliar
- `expl:pv`: Explícito
- `mark`: Marcador
- `punct`: Pontuação (exceto `,` e `--`)
- `flat`: Flat
- `acl:part`: Cláusula participial (se for o primeiro filho)

##### Detalhamento do Processo

**Inicialização:**
- Cria um vetor booleano `vetorBooleanoTokensVisitados` para controlar quais tokens foram visitados durante a extração da relação
- Inicializa o vetor com `false` para todos os tokens

**Iteração sobre Sujeitos:**
- Itera sobre cada sujeito identificado na sentença
- Obtém o token pai do sujeito
- Adiciona o token pai na pilha para iniciar a busca em profundidade
- Define o token pai como o núcleo da relação
- Marca o token pai e seus filhos que atendem aos critérios de inclusão

**Busca em Profundidade:**
- Utiliza uma pilha para realizar a busca em profundidade
- Adiciona o token inicial (pai do sujeito) na pilha
- Enquanto a pilha não estiver vazia:
   1. Remove o token do topo da pilha
   2. Marca o token como visitado no vetor `vetorBooleanoTokensVisitados`
   3. Adiciona o token ao conjunto de tokens da relação
   4. Itera sobre os filhos do token atual
   5. Se o filho atender aos critérios de inclusão (baseado no deprel), adiciona o filho na pilha para continuar a busca

Vou ajustar a formatação Markdown para melhor organização e legibilidade:

#### 3. Processamento de Conjunções Coordenadas

```java
public void moduloProcessamentoConjuncoesCoordenativasRelacao()
```

##### Funcionamento
- Inicializa um vetor booleano para controle de tokens visitados (`vetorBooleanoTokensVisitados`)
- Para cada sujeito identificado na sentença:
   - Obtém o token pai do sujeito
   - Define o token pai como núcleo inicial da relação e o adiciona à pilha
   - Realiza uma busca em profundidade para identificar tokens filhos que atendem aos critérios de inclusão
   - Detecta conjunções coordenadas (`conj`) e aplica um tratamento especial para expandir a relação

##### Critérios de Inclusão na Relação
Um token filho é incluído na relação se:
- Está posicionado adequadamente em relação ao núcleo do sujeito e da relação
- Possui um rótulo de dependência linguística (deprel) relevante, como:
   - `aux`: Auxiliar
   - `obj`: Objeto
   - `iobj`: Objeto indireto
   - `advmod`: Modificador adverbial
   - `cop`: Verbo copulativo
   - `expl:pv`: Explícito
   - `mark`: Marcador
   - `punct`: Pontuação (exceto `,` e `--`)
   - `flat`: Flat
   - `acl:part`: Cláusula participial (se for validada pela heurística)

##### Tratamento de Conjunções Coordenadas
- Quando um token com o rótulo `conj` é encontrado:
   - Uma heurística verifica se ele deve ser processado
   - O rótulo do token é temporariamente alterado para `conjCC` para diferenciar conjunções coordenadas
   - Um novo núcleo de relação é definido com o índice do token `conj`
   - O token é adicionado à relação e a busca continua

##### Detalhamento do Processo

**Inicialização:**
- Cria o vetor `vetorBooleanoTokensVisitados` e o preenche com `false` para indicar que nenhum token foi visitado
- Prepara uma pilha (`pilhaAuxiliar`) para realizar a busca em profundidade

**Iteração sobre Sujeitos:**
- Para cada sujeito identificado:
   1. Obtém o token núcleo do sujeito e seu token pai
   2. Cria uma cópia da estrutura de sujeito-relação (`SujeitoRelacao`) para evitar alterações no original
   3. Define o token pai como o núcleo inicial da relação e o empilha

**Busca em Profundidade:**
- Enquanto a pilha não estiver vazia:
   1. Remove o token do topo da pilha (`elementoPilha`)
   2. Itera sobre os filhos do token atual:
      - Se um filho atende aos critérios de inclusão, ele é adicionado à pilha e marcado como visitado
      - Quando um token `conj` é encontrado, aplica-se o tratamento especial descrito acima
   3. Se um nó folha é alcançado:
      - O vetor de tokens visitados é copiado
      - A estrutura de sujeito-relação atualizada é armazenada na lista principal

#### 4. Extração de Cláusulas

```java
public void extraiClausulas(boolean SC)
```

**Funcionamento**

- Inicializa um vetor booleano para marcar tokens visitados (`vetorBooleanoTokensVisitados`) e uma pilha auxiliar para realizar uma busca em profundidade.
- Itera sobre os sujeitos e relações extraídos previamente.
- Realiza uma busca em profundidade para identificar e extrair argumentos das cláusulas, verificando rótulos de dependência (`deprel`) e aplicando heurísticas específicas.
- Trata especialmente conjunções (`conj`) e cláusulas subordinadas (`ccomp`, `advcl`).

**Etapas do Processo**

1. **Inicialização:**
   - Cria o vetor `vetorBooleanoTokensVisitados` e o preenche com `false` para indicar que nenhum token foi visitado.
   - Prepara uma pilha (`pilhaAuxiliar`) para realizar a busca em profundidade.
   - Define uma variável de controle `flagExtracao` para gerenciar quando um pedaço de cláusula deve ser extraído.

2. **Iteração sobre Sujeitos e Relações:**
   - Para cada `SujeitoRelacaoArgumentos`:
      - Obtém o núcleo da relação e seus tokens filhos.
      - Usa a função `separaParteTokensRelacaoEmArgumentos` para isolar partes da relação que serão tratadas como argumentos.
      - Marca tokens filhos relevantes para extração com base em critérios específicos de dependência (`deprel`), como:
         - `nmod`, `xcomp`, `dobj`, `obj`, `acl:relcl`, `iobj`
         - `conj` (aplicando heurísticas para evitar redundâncias)
         - `acl:part`, `nummod`, `advmod`, `appos`, `amod`, `ccomp`, `advcl`, `dep`
         - `punct` (validações específicas para pontuações relevantes).

3. **Busca em Profundidade:**
   - Utiliza a pilha para iterar recursivamente sobre os tokens filhos:
      - Adiciona tokens filhos que atendem aos critérios na pilha e marca como visitados.
      - Quando encontra um nó folha:
         - Adiciona pedaços de argumentos ao objeto `Argumento`.
         - Verifica se o token é uma conjunção ou cláusula subordinada para aplicar tratamentos diferenciados.
         - Trata tokens filhos antes ou depois do núcleo da relação usando a função `verificaTokenPaiAntesDepoisNucleoRelacao`.

4. **Tratamento de Conjunções e Cláusulas Subordinadas:**
   - Quando encontra tokens `conj`, verifica se outros filhos `conj` estão presentes para evitar extrações redundantes.
   - Para cláusulas subordinadas (`ccomp`, `advcl`), usa a flag `SC` para decidir se devem ser processadas diretamente.

5. **Finalização:**
   - Remove tokens processados da pilha e reseta a flag `flagExtracao`.
   - Adiciona argumentos extraídos à estrutura `SujeitoRelacaoArgumentos`.
   - Verifica e incorpora pedaços de argumentos antes da relação para completar a extração.

#### 5. Extração de Clausulas Quebradas

```java
public void extraiClausulasQuebradas()
```

1. **Inicialização:**

   - Cria um vetor `vetorBooleanoTokensVisitados` para marcar quais tokens já foram processados, inicializando todos os valores como `false`.
   - Prepara uma pilha (`pilhaAuxiliar`) para realizar a busca em profundidade nos tokens.
   - Define uma flag `flagExtracao` para indicar quando uma cláusula ou parte dela deve ser extraída.

2. **Iteração sobre Sujeitos e Relações:**

   - Para cada `SujeitoRelacaoArgumentos`:
      - Obtém o núcleo da relação e seus tokens filhos.
      - Inicializa um objeto `Argumento` para armazenar partes de cláusulas que serão extraídas.
      - Verifica tokens filhos do núcleo da relação, aplicando critérios de dependência para decidir se devem ser processados:
         - Considera dependências como `nmod`, `acl:relcl`, `conj`, `acl:part`, `nummod`, `advmod`, `appos`, `amod`, `ccomp`, `advcl`, `dep`.
         - Usa heurísticas para evitar redundâncias, especialmente com `conj`.

3. **Busca em Profundidade:**

   - Utiliza a pilha para realizar uma busca recursiva nos tokens filhos:
      - Empilha tokens que atendem aos critérios e marca-os como visitados.
      - Ao alcançar um nó folha:
         - Adiciona pedaços de argumentos ao objeto `Argumento`.
         - Verifica se o token é uma conjunção ou pertence a uma cláusula subordinada para aplicar tratamentos diferenciados.
           - Usa a função `verificaTokenPaiAntesDepoisNucleoRelacao` para decidir se tokens devem ser adicionados antes ou depois do núcleo da relação.        

4. **Tratamento de Conjunções e Cláusulas Subordinadas:**

   - Para tokens `conj`, verifica a presença de outros filhos `conj` para evitar extrações redundantes.
   - Para cláusulas subordinadas (`ccomp`, `advcl`), a flag `SC` determina se devem ser processadas diretamente.

5. **Finalização:**

   - Desempilha tokens processados e reseta a flag `flagExtracao`.
   - Adiciona argumentos extraídos à estrutura `SujeitoRelacaoArgumentos`.
   - Garante que pedaços de argumentos antes da relação sejam incorporados para completar a extração.


#### 6. Extração de Conjunções Coordenadas argumento 2

```java
public void moduloProcessamentoConjuncoesCoordenativasArg2()
```

O método `moduloProcessamentoConjuncoesCoordenativasArg2` realiza a extração de conjunções coordenadas (arg2) de uma sentença. Aqui está uma descrição detalhada de como ele funciona:

1. **Inicialização**:
   - Cria um vetor booleano `vetorBooleanoTokensVisitados` para marcar tokens já visitados.
   - Inicializa variáveis auxiliares como `tokenConj`, `pilhaAuxiliar`, `caminhoPercorrido`, `tokenNucleoPaiConjuncoes`, `elementoPilha`, `tokenPilha`, e `flagExtracao`.

2. **Iteração sobre `sujeitoRelacaoArgumentos`**:
   - Para cada `SujeitoRelacaoArgumentos` (`sra`), inicializa variáveis como `flagIndicaExtracaoAntesQuebrarConjuncao`, `itemSujeitoRelacao`, `argumento`, `argumentosAntesRelacao`, e `argumentoAntesRelacao`.
   - Define o `tokenNucleoRelacao` a partir do índice do núcleo da relação.

3. **Processamento dos filhos do núcleo da relação**:
   - Itera sobre os filhos do `tokenNucleoRelacao`.
   - Verifica se o token filho não foi visitado e se não está nos vetores booleanos de tokens do sujeito e da relação.
   - Se o token filho atende a certos critérios de dependência, ele é marcado como visitado e adicionado à `pilhaAuxiliar`.
   - Dependendo da posição do token filho em relação ao núcleo da relação, ele é adicionado ao `argumento` ou `argumentoAntesRelacao`.

4. **Processamento da pilha auxiliar**:
   - Enquanto a `pilhaAuxiliar` não estiver vazia, processa o topo da pilha (`elementoPilha`).
   - Itera sobre os filhos do `elementoPilha`.
   - Se um filho (`tokenPilha`) não foi visitado e atende a certos critérios de dependência, ele é processado:
      - Adicionado à `pilhaAuxiliar`.
      - Dependendo da posição do `tokenPilha` em relação ao núcleo da relação, ele é adicionado ao `argumento` ou `argumentoAntesRelacao`.
      - Se o `tokenPilha` é uma conjunção, ele é tratado de forma especial, incluindo a alteração temporária de sua dependência para `conjCC` e a chamada de `trataCasoEspecialArg2ComConjuncao`.

5. **Extração de argumentos**:
   - Se `flagExtracao` está ativa, verifica se chegou a um nó folha e processa a extração de argumentos.
   - Dependendo da dependência do topo da pilha (`pilhaAuxiliar.peek()`), adiciona o `argumento` ou `argumentoAntesRelacao` ao `sra`.

6. **Finalização**:
   - Copia o vetor booleano de tokens visitados para o `sra`.
   - Reseta o vetor booleano de tokens visitados.
   - Chama `adicionaArgumentosAntesDasRelacoesNasExtracoesEObjCompNaRelacao` para adicionar argumentos antes das relações nas extrações e objetos complementares na relação.

Este método é responsável por garantir que todas as conjunções coordenadas sejam corretamente extraídas e processadas, evitando extrações redundantes e garantindo a completude das triplas extraídas.


### 7. Algoritmo de Extração de Aposto

Este algoritmo tem como objetivo identificar e processar tokens que representam apostos em uma sentença, extraindo suas relações e associando-as a uma estrutura específica. A seguir, descreve-se cada etapa do processo.

#### 1. Inicialização

**Estruturas de Dados:**
- Criação de um vetor booleano `vetorBooleanoTokensVisitados`, usado para marcar tokens já processados na sentença. Todos os valores são inicializados como `false`.
- Inicialização de uma pilha (`pilhaAuxiliar`), que será usada para realizar busca em profundidade nos tokens relacionados.

**Token Sintético:**
- Criação de um token sintético `verboSintetico`, configurado como um verbo ("VERB") com a relação de dependência "cop" e a forma "é".
- Esse token é usado para estabelecer a relação entre o núcleo do sujeito e o aposto.

#### 2. Identificação de Apostos

**Para cada token na sentença:**

**Critérios de Seleção:**
- O token deve ter:
   - Relação de dependência (`deprel`) igual a "appos".
   - Tag gramatical de palavra-base (`cpostag`) igual a "PROPN" (nome próprio).
   - O token não deve possuir filhos com relação `conj`.

**Processamento de Apostos:**
- Cria-se uma nova instância de `SujeitoRelacao` e associa-se a estrutura à lista global.
- O núcleo do sujeito relacionado ao aposto é identificado (token pai do aposto).
- O token sintético `verboSintetico` é atualizado para usar o mesmo ID do núcleo identificado.

#### 3. Busca em Profundidade nos Tokens Relacionados

**Inicialização da Pilha:**
- O núcleo do sujeito é empilhado na `pilhaAuxiliar` para iniciar a busca.

**Processamento Recursivo:**
- Enquanto a pilha não estiver vazia:
   - Retira-se o elemento do topo (`elementoPilha`) e verifica seus filhos (`tokenPilha`).
   - Se o filho não foi visitado e atende aos critérios:
      - Empilha o token filho e marca-o como visitado.
      - Adiciona o token à estrutura de argumentos (`SujeitoRelacao`) utilizando a função `adicionaPedacoSujeitoRelacao`.

**Critérios para Processamento de Tokens Filhos:**
- As relações de dependência consideradas incluem:
   - `nummod`, `nmod`, `amod`, `dep`, `obj`, `det`, `case`, e `punct`.
- Valida-se se o token está localizado antes do aposto na sentença.

**Finalização:**
- Após processar todos os filhos de um token, remove-se o elemento do topo da pilha.

#### 4. Extração de Cláusulas

- Cria-se uma cópia do vetor `vetorBooleanoTokensVisitados` para armazenar os tokens processados para o sujeito e para a relação.
- Define-se identificadores do módulo de extração para indicar:
   - Sujeito: 1.
   - Relação: 3.
- Chama-se a função `extraiClausulasAposto` para processar as cláusulas do argumento associadas ao aposto.

#### 5. Reset para Próximo Aposto

- Após concluir o processamento de um aposto:
   - Reseta-se o vetor `vetorBooleanoTokensVisitados` para `false` para preparar a análise do próximo aposto.

## Dicionário de Termos Técnicos

- **HEAD**: Token hierarquicamente superior ao atual
- **deprel**: Relação de dependência com o HEAD
- **postag**: Categoria gramatical do token
- **ID**: Identificador único do token (ordem crescente)

## Notas de Implementação

### Pontuações Válidas
As seguintes pontuações são consideradas válidas durante o processamento:
- Parênteses: `(` `)`
- Chaves: `{` `}`
- Colchetes: `[` `]`
- Apóstrofo: `'`
- Vírgula: `,`

### Tratamento de Casos Especiais

1. **Múltiplos Núcleos**
    - Sistema identifica e ajusta quando existem dois verbos na relação
    - Define hierarquia baseada em sujeitos e relações adicionais

2. **Substituição de Núcleo**
    - Permite ajuste dinâmico do núcleo quando token mais relevante é encontrado

### Considerações de Performance
- Uso de `ArrayDeque` para armazenamento eficiente
- Vetores booleanos para controle de visitação
- Sistema de clonagem otimizado para cópias profundas