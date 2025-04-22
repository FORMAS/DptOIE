package extrai_clausulas;
//
//import java.io.BufferedOutputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.nio.charset.Charset;

import java.io.IOException;
import java.util.*;
//LEMBRAR DE FAZER O MAPEAMENTO DOS NUMEROS DAS CLAUSULAS USANDO UM VETOR DE INDICES,

public class Extracao1 {

    private Sentence sentence;
    private ArrayList<SujeitoRelacao> sujeitoRelacao;
    private ArrayList<SujeitoRelacaoArgumentos> sujeitoRelacaoArgumentos;

    public Extracao1(Sentence s) {
        this.sentence = s;
        this.sujeitoRelacao = new ArrayList<>(10);
        sujeitoRelacaoArgumentos = new ArrayList<>();

    }

    public void realizaExtracao(boolean CC, boolean SC, int appositive) throws CloneNotSupportedException, IOException {
        /*
         Módulos de extração
         Sujeito
         1 - básico
         2 - pronomes relativos
         3 - transitividade
         Relação
         1 - básico
         2 - conjunção coordenada
         3 - aposto (criação do verbo sintético)
         5 - verbo de ligação
         Argumento 2
         0 e 1 - clausula quebrada / básico
         2 - conjunção coordenada
         3 - aposto
         4 - substituição do aposto por transitividade (ACHO QUE ESSE NÃO USOU)
         6 - decomposição {Identificada no momento da extração}
         */
        buscaSujeito(SC);
//        moduloProcessamentoConjuncoesCoordenativasSujeito();
        buscaRelacao();
        if (CC) {
            moduloProcessamentoConjuncoesCoordenativasRelacao();
        }
        carregaSRA();
        extraiClausulas(SC);
        extraiClausulasQuebradas(SC);
        if (CC) {
            moduloProcessamentoConjuncoesCoordenativasArg2();
        }
        if (appositive == 1 || appositive == 2) {
            moduloExtracaoAposto();
        }
        adicionaArgumentoMarkAntesSujeito();
        if (appositive == 2) {
            moduloSubstituiApostoTransitividade(this.sujeitoRelacaoArgumentos);
        }
        moduloExtracaoVerboLigacao();
        eliminaPreposicaoAntesSujeito();
//        removeHifenFinalRelacaoEAdicionaInicioArgumento(this.sujeitoRelacaoArgumentos);
        excluiSraSujeitoErrado();
        excluiSraRelacaoSemVerbo();
        eliminaTokenPontuacaoErrada();
//        deslocaClausulasNomesPropriosDaRelacaoParaArgumentos();
        excluiExtracoesRepetidas();

        /*OBS: SE INVERTER OS MÓDULOS DA SEQUÊNCIA PODE-SE GERAR OUTROS RESULTADOS*/
    }

    //Essa função faz argumentoVerboLigacao busca em profundidade para detectar os elementos do sujeito
    public void buscaSujeito(boolean SC) {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        int indiceArraySujeitoRelacao = 0;
        for (Token tokenSentenca : this.sentence.getSentenca()) {
            if ((tokenSentenca.getDeprel().equals("nsubj") || (tokenSentenca.getDeprel().equals("nsubj:pass")))) {
                SujeitoRelacao sr = new SujeitoRelacao();
                sr.setIdentificadorModuloExtracaoSujeito(1);
                this.sujeitoRelacao.add(sr);/*esse "sujeito relacao" é add null, para quando for concatenar os elementos
                 não dê erro relacionado à posição vazia*/

                pilhaAuxiliar.push(tokenSentenca);//Adiciona na pilha o token
                adicionaPedacoSujeitoRelacao(sr, tokenSentenca, vetorBooleanoTokensVisitados, 0);
                sr.setIndiceNucleoSujeito(tokenSentenca.getId());
                while (!pilhaAuxiliar.empty()) {
                    elementoPilha = pilhaAuxiliar.peek();
                    for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                        tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                        if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                            if (tokenPilha.getDeprel().equals("nummod")
                                    || tokenPilha.getDeprel().equals("advmod")
                                    || (tokenPilha.getDeprel().equals("appos") && tokenPilha.getPostag().equals("NUM"))
                                    || ((tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj"))/* && tokenPilha.getId() > tokenSentenca.getId()*/)
                                    || tokenPilha.getDeprel().contains("det")
                                    || tokenPilha.getDeprel().equals("case")
                                    || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha))
                                    || (tokenPilha.getDeprel().equals("conj") && !tokenPilha.getPostag().equals("VERB"))
                            ) {
                                pilhaAuxiliar.push(tokenPilha);//System.out.println(tokenPilha.getForm());
                                adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 0);
                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                            }
                        }
                    }
                    pilhaAuxiliar.pop();
                }
                Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                sr.setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
                Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
                indiceArraySujeitoRelacao++;
            }
        }
        if (SC) {
            moduloProcessamentoPronomesRelativos(indiceArraySujeitoRelacao);
        }
    }

    public void moduloProcessamentoConjuncoesCoordenativasSujeito() {
        int indiceArraySujeitoRelacao = this.sujeitoRelacao.size();
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        for (Token tokenSentenca : this.sentence.getSentenca()) {
            if ((tokenSentenca.getDeprel().equals("nsubj") || (tokenSentenca.getDeprel().equals("nsubj:pass")))) {
                Token paiSujeito = this.sentence.getSentenca().get(tokenSentenca.getHead());
                if (!(paiSujeito.getDeprel().equals("acl:relcl") && tokenSentenca.getPostag().equals("PRON"))) {
                    SujeitoRelacao sr = new SujeitoRelacao();
                    sr.setIdentificadorModuloExtracaoSujeito(0);
                    this.sujeitoRelacao.add(sr);
                    pilhaAuxiliar.push(tokenSentenca);//Adiciona na pilha o token
                    adicionaPedacoSujeitoRelacao(this.sujeitoRelacao.get(indiceArraySujeitoRelacao), tokenSentenca, vetorBooleanoTokensVisitados, 0);
                    this.sujeitoRelacao.get(indiceArraySujeitoRelacao).setIndiceNucleoSujeito(tokenSentenca.getId());
//                Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                                if (tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || (tokenPilha.getDeprel().equals("appos") && tokenPilha.getPostag().equals("NUM")) || ((tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj"))/* && tokenPilha.getId() > tokenSentenca.getId()*/) || tokenPilha.getDeprel().equals("det") || tokenPilha.getDeprel().equals("case") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha))) {
                                    pilhaAuxiliar.push(tokenPilha);//System.out.println(tokenPilha.getForm());
                                    adicionaPedacoSujeitoRelacao(this.sujeitoRelacao.get(indiceArraySujeitoRelacao), tokenPilha, vetorBooleanoTokensVisitados, 0);
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                } else if (tokenPilha.getDeprel().equals("conj") && !tokenPilha.getPostag().equals("VERB")) {
                                    tokenPilha.setDeprel("conjCC");/*Nessa função argumentoVerboLigacao conjunção coordenada é tratada de forma diferente da buscaSujeito
                                     //por isso foi alterado o Deprel. Assim, na função para concatenar outra função será chamada */

                                    Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                                    System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                                    this.sujeitoRelacao.get(indiceArraySujeitoRelacao).setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
                                    indiceArraySujeitoRelacao = trataCasoEspecialSujeitoComConjuncao(pilhaAuxiliar, tokenPilha, indiceArraySujeitoRelacao, tokenSentenca.getId(), vetorBooleanoTokensVisitados);
                                    tokenPilha.setDeprel("conj");
                                    pilhaAuxiliar.push(tokenPilha);
                                    flagExtracao = true;
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                        pilhaAuxiliar.pop();
                    }
                    if (!flagExtracao) {/*Esta flag é utilizada para verificar se possui alguma conjunção coordenada filha do sujeito. Se
                         não tiver o objeto é removido, pois significa que ele já foi extraído na busca padrão do sujeito*/

                        this.sujeitoRelacao.remove(this.sujeitoRelacao.size() - 1);
                    } else {
                        Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                        System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                        this.sujeitoRelacao.get(indiceArraySujeitoRelacao).setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
                        indiceArraySujeitoRelacao++;
                    }
                    flagExtracao = false;
                    Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
                }
            }
        }
    }

    public int trataCasoEspecialSujeitoComConjuncao(Stack<Token> pilhaAuxiliarTokensPercorridos, Token tokenConj, int indiceArraySujeitoRelacao, int idNucleoSujeito, Boolean[] vetorBooleanoTokensVisitados) {
        SujeitoRelacao sr = new SujeitoRelacao();//Como tem uma conjunção deixa o outro sujeito do jeito que tá e vamos extrair outro
        sr.setIndiceNucleoSujeito(idNucleoSujeito);
        this.sujeitoRelacao.add(sr);
        indiceArraySujeitoRelacao++;
        //Busca em profundidade feita apenas para trás
        Boolean[] vetorBooleanoTokensVisitadosParaTras = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitadosParaTras, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        QuebraLoopNoFolha:
        for (Token tokenSentenca : pilhaAuxiliarTokensPercorridos.get(0).getTokensFilhos()) {
            if ((tokenSentenca.getDeprel().equals("nmod") || tokenSentenca.getDeprel().equals("nummod") || tokenSentenca.getDeprel().equals("advmod") || tokenSentenca.getDeprel().equals("appos") || tokenSentenca.getDeprel().equals("amod") || tokenSentenca.getDeprel().equals("dep") || tokenSentenca.getDeprel().equals("obj")) && tokenSentenca.getId() < tokenSentenca.getHead()) {
                pilhaAuxiliar.push(tokenSentenca);//Adiciona na pilha o token
                adicionaPedacoSujeitoRelacao(this.sujeitoRelacao.get(indiceArraySujeitoRelacao), tokenSentenca, vetorBooleanoTokensVisitadosParaTras, 0);
                flagExtracao = true;
                while (!pilhaAuxiliar.empty()) {
                    elementoPilha = pilhaAuxiliar.peek();
                    for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                        tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                        if (!vetorBooleanoTokensVisitadosParaTras[tokenPilha.getId()]) {
                            if ((tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || tokenPilha.getDeprel().equals("appos") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj")) && tokenPilha.getId() < tokenPilha.getHead()) {
                                pilhaAuxiliar.push(tokenPilha);
                                adicionaPedacoSujeitoRelacao(this.sujeitoRelacao.get(indiceArraySujeitoRelacao), tokenPilha, vetorBooleanoTokensVisitadosParaTras, 0);
                                flagExtracao = true;
                                vetorBooleanoTokensVisitadosParaTras[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                            }
                        }
                    }
                    if (flagExtracao) { //Entrar nesse 'IF' significa que argumentoVerboLigacao busca chegou em algum nó folha
                        break QuebraLoopNoFolha;
                    }
                    pilhaAuxiliar.pop();
                    flagExtracao = false;
                }
            }
        }

        for (int icont = 0; icont < pilhaAuxiliarTokensPercorridos.size() - 1; icont++) {
            adicionaPedacoSujeitoRelacao(this.sujeitoRelacao.get(indiceArraySujeitoRelacao), pilhaAuxiliarTokensPercorridos.get(icont), vetorBooleanoTokensVisitados, 0);
        }
        if (!verificaPreposicaoAntesToken(tokenConj)) {
            Deque<Token> dequePreposicaoParaAdicionarAntesDoTokenConj = retornaApenasConjuncoesAterioresAoToken(this.sentence.getSentenca().get(tokenConj.getHead()));
            for (Token t : dequePreposicaoParaAdicionarAntesDoTokenConj) {
                this.sujeitoRelacao.get(indiceArraySujeitoRelacao).setTokenSujeitoFinalDeque(t);
            }
        }
        adicionaPedacoSujeitoRelacao(this.sujeitoRelacao.get(indiceArraySujeitoRelacao), tokenConj, vetorBooleanoTokensVisitados, 0);//Adiciono o sujeito base
        return indiceArraySujeitoRelacao;
    }

    public void buscaRelacao() throws CloneNotSupportedException {
        /*
         Após argumentoVerboLigacao relação pode-se buscar os argumentos
         acl:part - se tiver no início, sendo o primeiro filho
         expl:pv - elemento argumentoVerboLigacao ser buscado antes ou depois
         */
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
//        System.out.println("Tamanho SujeitoRelação " + this.sujeitoRelacao.size());
        for (int indiceArraySujeitoRelacao = 0; indiceArraySujeitoRelacao < this.sujeitoRelacao.size(); indiceArraySujeitoRelacao++) {
            Token sujeito = this.sentence.getSentenca().get(this.sujeitoRelacao.get(indiceArraySujeitoRelacao).getIndiceNucleoSujeito());
            Token paiSujeito = this.sentence.getSentenca().get(sujeito.getHead());
            SujeitoRelacao sr = this.sujeitoRelacao.get(indiceArraySujeitoRelacao);
            pilhaAuxiliar.push(paiSujeito);//Adiciona na pilha o token
            sr.setIndiceNucleoRelacao(paiSujeito.getId());
            sr.setIdentificadorModuloExtracaoRelacao(1);
            adicionaPedacoSujeitoRelacao(sr, paiSujeito, vetorBooleanoTokensVisitados, 1);

            while (!pilhaAuxiliar.empty()) {
                elementoPilha = pilhaAuxiliar.peek();
                for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                    tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                    if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                        List<String> deprelsValidos1 = Arrays.asList("aux:pass", "obj", "iobj", "advmod", "cop", "aux", "expl:pv", "mark");
                        List<String> deprelsValidos2 = Arrays.asList("flat", "expl:pv");
                        List<String> punctInvalidos = Arrays.asList(",", "--");

                        boolean entreSujeitoERelacao = tokenPilha.getId() < sr.getIndiceNucleoRelacao()
                                && (
                                        (tokenPilha.getId() > sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() < sr.getIndiceNucleoRelacao())
                                                || (tokenPilha.getId() < sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() > sr.getIndiceNucleoRelacao()));

                        boolean isDeprelValido = deprelsValidos1.contains(tokenPilha.getDeprel());
                        boolean isPunctValido = tokenPilha.getDeprel().equals("punct") && !punctInvalidos.contains(tokenPilha.getForm());

                        boolean isDeprelPosSujeito = deprelsValidos2.contains(tokenPilha.getDeprel());
                        boolean isPunctTraco = tokenPilha.getDeprel().equals("punct") && tokenPilha.getForm().equals("-");
                        boolean isAclPartValido = tokenPilha.getDeprel().equals("acl:part") && verificaAclPartPrimeiroFilhoRelacao(tokenPilha);

                        if ((entreSujeitoERelacao && (isDeprelValido || isPunctValido)) || (tokenPilha.getId() > paiSujeito.getId() && (isDeprelPosSujeito || isPunctTraco || isAclPartValido))) {

                            pilhaAuxiliar.push(tokenPilha);
                            adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                            vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;

                            elementoPilha = new Token();
                            elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                            i = -1; // Reinicia o loop

                            // Caso especial para acl:part
                            if (isAclPartValido) {
                                sr.setIndiceNucleoRelacao(tokenPilha.getId());
                            }
                        }
                    }
                }

                pilhaAuxiliar.pop();
            }
            /*Função usada para alterar o núcleo da relação se necessário*/
            setNovoIndiceNucleoRelacao(paiSujeito, sr, vetorBooleanoTokensVisitados);
//            System.out.println("novo ID: " + sr.getIndiceNucleoRelacao());
            /*                                                           */
            Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
            System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
            sr.setVetorBooleanoTokensRelacaoVisitados(vetorBooleanoTokensVisitadosCopia);
            Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        }
        //eliminaArgumentosEntreVirgulasRelacao();
        buscaRelacaoAclPart();
    }

    public void moduloProcessamentoConjuncoesCoordenativasRelacao() {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
//        System.out.println("Tamanho SujeitoRelação " + this.sujeitoRelacao.size());
        int tamanhoInicialSr = this.sujeitoRelacao.size();
        for (int indiceArray = 0; indiceArray < tamanhoInicialSr; indiceArray++) {
            Token sujeito = this.sentence.getSentenca().get(this.sujeitoRelacao.get(indiceArray).getIndiceNucleoSujeito());
            Token paiSujeito = this.sentence.getSentenca().get(sujeito.getHead());
            SujeitoRelacao sr = this.sujeitoRelacao.get(indiceArray).retornaClone();
            sr.setVetorBooleanoTokensRelacaoVisitados(vetorBooleanoTokensVisitados);
//            System.out.println("push: " + paiSujeito.getForm());
            pilhaAuxiliar.push(paiSujeito);//Adiciona na pilha o token
            sr.setIndiceNucleoRelacao(paiSujeito.getId());
            sr.resetaDequeRelacao();
            sr.setIdentificadorModuloExtracaoRelacao(2);
//            adicionaPedacoSujeitoRelacao(sr, paiSujeito, vetorBooleanoTokensVisitados, 1);
//            flagExtracao = true;
            while (!pilhaAuxiliar.empty()) {
                elementoPilha = pilhaAuxiliar.peek();
                for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                    tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                    if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                        if ((tokenPilha.getId() < sr.getIndiceNucleoRelacao() && tokenPilha.getId() > sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() < sr.getIndiceNucleoRelacao()) || (tokenPilha.getId() < sr.getIndiceNucleoRelacao() && tokenPilha.getId() < sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() > sr.getIndiceNucleoRelacao())) {
                            if ((tokenPilha.getDeprel().contains("aux") || (tokenPilha.getDeprel().equals("obj")) || (tokenPilha.getDeprel().equals("iobj")) || (tokenPilha.getDeprel().equals("advmod")) || (tokenPilha.getDeprel().equals("cop")) || tokenPilha.getDeprel().equals("aux") || tokenPilha.getDeprel().equals("expl:pv") || tokenPilha.getDeprel().equals("mark") || (tokenPilha.getDeprel().equals("punct") && (!tokenPilha.getForm().equals(",") && !tokenPilha.getForm().equals("--"))))) {
                                pilhaAuxiliar.push(tokenPilha);
                                if (flagExtracao) {
                                    adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                                }
                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                            }
                        } else if (tokenPilha.getId() > paiSujeito.getId()) {
                            if (tokenPilha.getDeprel().equals("flat") || tokenPilha.getDeprel().equals("expl:pv") || (tokenPilha.getDeprel().equals("punct") && tokenPilha.getForm().equals("-"))) {
                                pilhaAuxiliar.push(tokenPilha);
                                if (flagExtracao) {
                                    adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                                }
                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                            } else if (tokenPilha.getDeprel().equals("acl:part") && verificaAclPartPrimeiroFilhoRelacao(tokenPilha)/*&& tokenPilha.equals(elementoPilha)*/) {
                                pilhaAuxiliar.push(tokenPilha);
                                if (flagExtracao) {
                                    adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                                }
                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                //Coloca um novo núcleo para argumentoVerboLigacao relação, com o objetivo de fazer argumentoVerboLigacao busca argumentoVerboLigacao partir desse token
//                                sr.setIndiceNucleoRelacao(tokenPilha.getId());
                            } else if (tokenPilha.getDeprel().equals("conj")) {
                                if (heuristicasVerificaConjRelacao(tokenPilha)) {
                                    tokenPilha.setDeprel("conjCC");/*Nessa função argumentoVerboLigacao conjunção coordenada é tratada de forma diferente da buscaSujeito
                                     //por isso foi alterado o Deprel. Assim, na função para concatenar outra função será chamada */

                                    sr.setIndiceNucleoRelacao(tokenPilha.getId());
                                    adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                                    tokenPilha.setDeprel("conj");
                                    /*Se necessário coloca outro índice*/
                                    setNovoIndiceNucleoRelacao(tokenPilha, sr, vetorBooleanoTokensVisitados);
                                    sr.setIdentificadorModuloExtracaoRelacao(2);
                                    pilhaAuxiliar.push(tokenPilha);
                                    flagExtracao = true;
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                    }
                }
                if (flagExtracao) { //Entrar nesse 'IF' significa que argumentoVerboLigacao busca chegou em algum nó folha
                    Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                    System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                    System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                    sr.setVetorBooleanoTokensRelacaoVisitados(vetorBooleanoTokensVisitadosCopia);
                    sr.setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
                    this.sujeitoRelacao.add(sr);
//                    System.out.println("IdRelacao: " + sr.getIndiceNucleoRelacao());
                    sr = sr.retornaClone();
                    sr.setIndiceNucleoRelacao(0);
                    sr.resetaDequeRelacao();
                }
                pilhaAuxiliar.pop();
                flagExtracao = false;

            }
        }
        //eliminaArgumentosEntreVirgulasRelacao();
    }

    public void buscaRelacaoAclPart() throws CloneNotSupportedException {
        /*
         Após argumentoVerboLigacao relação pode-se buscar os argumentos
         acl:part - se tiver no início, sendo o primeiro filho
         expl:pv - elemento argumentoVerboLigacao ser buscado antes ou depois
         */
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        int tamanhoInicialSujeitoRelacao = this.sujeitoRelacao.size();
        for (int contSujeitoRelacao = 0; contSujeitoRelacao < tamanhoInicialSujeitoRelacao; contSujeitoRelacao++) {
            SujeitoRelacao sr;
            sr = this.sujeitoRelacao.get(contSujeitoRelacao).retornaClone();
            for (Token tokenFilhoSujeito : this.sentence.getSentenca().get(sr.getIndiceNucleoSujeito()).getTokensFilhos()) {
                if (tokenFilhoSujeito.getDeprel().equals("acl:part")) {
                    Token tokenFilhoSujeitoAclPart = tokenFilhoSujeito.retornaClone();
                    /*Como será criado uma nova relação é necessário clonar o objeto, pois o sujeito é o mesmo,
                     e gerar uma nova relação para ele*/
                    sr.resetaDequeRelacao();
                    sr.setIndiceNucleoRelacao(0);
                    sr.setIdentificadorModuloExtracaoRelacao(1);
                    this.sujeitoRelacao.add(sr);
                    /*--------------------------------------------*/
                    pilhaAuxiliar.push(tokenFilhoSujeitoAclPart);//Adiciona na pilha o token
                    sr.setIndiceNucleoRelacao(tokenFilhoSujeitoAclPart.getId());
                    adicionaPedacoSujeitoRelacao(sr, tokenFilhoSujeitoAclPart, vetorBooleanoTokensVisitados, 1);
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                                if ((tokenPilha.getId() < sr.getIndiceNucleoRelacao() && tokenPilha.getId() > sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() < sr.getIndiceNucleoRelacao()) || (tokenPilha.getId() < sr.getIndiceNucleoRelacao() && tokenPilha.getId() < sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() > sr.getIndiceNucleoRelacao())) {
                                    if ((tokenPilha.getDeprel().equals("aux:pass") || (tokenPilha.getDeprel().equals("obj")) || (tokenPilha.getDeprel().equals("iobj")) || (tokenPilha.getDeprel().equals("advmod")) || (tokenPilha.getDeprel().equals("cop")) || tokenPilha.getDeprel().equals("aux") || tokenPilha.getDeprel().equals("expl:pv") || tokenPilha.getDeprel().equals("mark") || (tokenPilha.getDeprel().equals("punct") && !tokenPilha.getForm().equals(",")))) {
                                        pilhaAuxiliar.push(tokenPilha);
                                        adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                                        vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                        elementoPilha = new Token();
                                        elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                        i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                    }
                                }
                                if (tokenPilha.getId() > tokenFilhoSujeitoAclPart.getId()) {
                                    if (tokenPilha.getDeprel().equals("flat") || tokenPilha.getDeprel().equals("expl:pv") || (tokenPilha.getDeprel().equals("punct") && tokenPilha.getForm().equals("-"))) {
                                        pilhaAuxiliar.push(tokenPilha);
                                        adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 1);
                                        vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                        elementoPilha = new Token();
                                        elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                        i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                    }
                                }
                            }
                        }
                        pilhaAuxiliar.pop();
                    }
                    Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                    System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                    sr.setVetorBooleanoTokensRelacaoVisitados(vetorBooleanoTokensVisitadosCopia);
                    Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
                }
            }
        }
    }

    public int moduloProcessamentoPronomesRelativos(int indiceArraySujeitoRelacao) {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        int tamanhoSujeitoRelacao = this.sujeitoRelacao.size();/*variável necessária para não entrar em loop eterno,
         pois à medida que o sujeito relação ia crescendo argumentoVerboLigacao condição de parada do 'for' também ia crescendo*/

        for (int indiceSujeitoRelacao = 0; indiceSujeitoRelacao < tamanhoSujeitoRelacao; indiceSujeitoRelacao++) {
            Token sujeito = this.sentence.getSentenca().get(this.sujeitoRelacao.get(indiceSujeitoRelacao).getIndiceNucleoSujeito());
            Token paiSujeito = this.sentence.getSentenca().get(sujeito.getHead());
            if (paiSujeito.getDeprel().equals("acl:relcl") && sujeito.getPostag().equals("PRON")) {
                Token novoSujeitoSubstituiPronomeRelativo = this.sentence.getSentenca().get(paiSujeito.getHead());
                SujeitoRelacao sr = new SujeitoRelacao();
                sr.setIdentificadorModuloExtracaoSujeito(2);
                this.sujeitoRelacao.add(sr);/*esse "sujeito relacao" é add null, para quando for concatenar os elementos
                 não dê erro relacionado à posição vazia*/

                pilhaAuxiliar.push(novoSujeitoSubstituiPronomeRelativo);//Adiciona na pilha o token
                adicionaPedacoSujeitoRelacao(sr, novoSujeitoSubstituiPronomeRelativo, vetorBooleanoTokensVisitados, 0);
//                System.out.println("ID SUJEITO ACL RELCL " + sujeito.getId() + " SUJEITO " + novoSujeitoSubstituiPronomeRelativo.getForm());
                this.sujeitoRelacao.get(indiceArraySujeitoRelacao).setIndiceNucleoSujeito(sujeito.getId());
                while (!pilhaAuxiliar.empty()) {
                    elementoPilha = pilhaAuxiliar.peek();
                    for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                        tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                        if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                            //if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || tokenPilha.getDeprel().equals("appos") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj")) {
                            if (tokenPilha.getId() < novoSujeitoSubstituiPronomeRelativo.getId()) {
                                if (tokenPilha.getDeprel().equals("dep")
                                        || tokenPilha.getDeprel().equals("obj")
                                        || tokenPilha.getDeprel().equals("nummod")
                                        || tokenPilha.getDeprel().equals("advmod")
                                        || tokenPilha.getDeprel().equals("det")
                                        || tokenPilha.getDeprel().equals("case")
                                        || (tokenPilha.getDeprel().equals("punct") && !tokenPilha.getForm().equals(","))
                                ) {
                                    pilhaAuxiliar.push(tokenPilha);
                                    adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 0);
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            } else {
                                if (tokenPilha.getDeprel().equals("nummod")
                                        || (tokenPilha.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenPilha))
                                        || tokenPilha.getDeprel().equals("advmod")
                                        || (tokenPilha.getDeprel().equals("appos") && tokenPilha.getPostag().equals("NUM"))
                                        || ((tokenPilha.getDeprel().equals("nmod")
                                        || tokenPilha.getDeprel().equals("amod")
                                        || tokenPilha.getDeprel().equals("dep")
                                        || tokenPilha.getDeprel().equals("obj")) && tokenPilha.getId() > novoSujeitoSubstituiPronomeRelativo.getId())
                                        || tokenPilha.getDeprel().equals("det")
                                        || tokenPilha.getDeprel().equals("case")
                                        || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha))
                                ) {
                                    pilhaAuxiliar.push(tokenPilha);
                                    adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 0);
//                                    System.out.println(tokenPilha.getForm());
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                    }
                    pilhaAuxiliar.pop();
                }
                Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                this.sujeitoRelacao.get(indiceArraySujeitoRelacao).setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
                indiceArraySujeitoRelacao++;
            }
        }

        for (int indiceSujeitoRelacao = 0; indiceSujeitoRelacao < tamanhoSujeitoRelacao; indiceSujeitoRelacao++) {
//            for (Token t : this.sujeitoRelacao.get(indiceSujeitoRelacao).getSujeito()) {
//                System.out.print(t.getForm() + " | ");
//            }
//            System.out.println("SR atual: " + this.sujeitoRelacao.get(indiceSujeitoRelacao).getIndiceNucleoSujeito() );
            Token sujeito = this.sentence.getSentenca().get(this.sujeitoRelacao.get(indiceSujeitoRelacao).getIndiceNucleoSujeito());
            Token paiSujeito = this.sentence.getSentenca().get(sujeito.getHead());
            if (paiSujeito.getDeprel().equals("acl:relcl") && sujeito.getPostag().equals("PRON")) {
                this.sujeitoRelacao.remove(indiceSujeitoRelacao);
                tamanhoSujeitoRelacao--;
                indiceSujeitoRelacao = -1;//Reseta o for e faz a busca do começo dele novamente
            }
//            System.out.println("Tamanho SR: " + this.sujeitoRelacao.size());
        }
        return indiceArraySujeitoRelacao;
    }

    /*Esta função adiciona no deque da classe SujeitoRelacao os pedacos dos argumentos*/
    public void adicionaPedacoSujeitoRelacao(SujeitoRelacao sujeitoRelacaoArgumentos, Token pedacoSujeitoRelacao, Boolean[] vetorBooleanoTokensVisitados, int codigoSujeitoRelacaoArgumentos) { //0 é sujeito, 1 é relacao e 2 é o argumento

        if (codigoSujeitoRelacaoArgumentos == 0) {
            Deque<Token> dequeToken = retornaTokenConcatenadoSujeitoArgumentos(pedacoSujeitoRelacao, vetorBooleanoTokensVisitados);
            if (pedacoSujeitoRelacao.getId() < pedacoSujeitoRelacao.getHead()) {
                while (dequeToken.size() != 0) {
                    sujeitoRelacaoArgumentos.setTokenSujeitoInicioDeque(dequeToken.pollLast());
                }
            } else {
                for (Token t : dequeToken) {
                    sujeitoRelacaoArgumentos.setTokenSujeitoFinalDeque(t);
                }
            }
        } else if (codigoSujeitoRelacaoArgumentos == 1) {
            Deque<Token> dequeToken = retornaTokenConcatenadoRelacao(pedacoSujeitoRelacao, vetorBooleanoTokensVisitados, sujeitoRelacaoArgumentos);
            if (pedacoSujeitoRelacao.getId() < pedacoSujeitoRelacao.getHead()) {
                while (dequeToken.size() != 0) {
                    sujeitoRelacaoArgumentos.setTokenRelacaoInicioDeque(dequeToken.pollLast());
                }
            } else {
                for (Token t : dequeToken) {
                    sujeitoRelacaoArgumentos.setTokenRelacaoFinalDeque(t);
                }
            }
        }
//        else if (codigoSujeitoRelacaoArgumentos == 2) {
//            Deque<Token> dequeToken = retornaTokenConcatenadoSujeitoArgumentos(pedacoSujeitoRelacao, vetorBooleanoTokensVisitados);
//            sujeitoRelacaoArgumentos.setClausulaArrayArgumentos(dequeToken);
//        }
    }

    public void adicionaPedacoArgumento(Argumento argumento, Token pedacoSujeitoRelacao, Boolean[] vetorBooleanoTokensVisitados) {
        Deque<Token> dequeToken = retornaTokenConcatenadoSujeitoArgumentos(pedacoSujeitoRelacao, vetorBooleanoTokensVisitados);
        argumento.setClausulaArrayClausulas(dequeToken);
    }

    /*Procedimento utilizado para carregar todos os SR em SujeitoRelacaoArgumentos para facilitar as extrações.
     Obs: claro que isso já poderia ter sido feito antes, à medida que fosse criado os SR ou nem mesmo o atributo SR
     deveria está nessa classe, visto que já tem em SRA. Em futuras alterações isso pode mudar*/
    public void carregaSRA() {
        for (SujeitoRelacao sr : this.sujeitoRelacao) {
            SujeitoRelacaoArgumentos sra = new SujeitoRelacaoArgumentos(sr.retornaClone());
            this.sujeitoRelacaoArgumentos.add(sra);
        }
    }

    public void extraiClausulas(boolean SC) {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"

        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            Token tokenConj = null;/*Esse token é usado para verificar se ele tem filho 'conj'. Se tiver ele vai ser verificado
             se possui outros filhos 'conj' para extrair tudo em uma só tripla para não ter extrações redundantes*/

            SujeitoRelacao itemSujeitoRelacao = sra.getSujeitoRelacao();
            Argumento argumento = new Argumento();
            //O argumento quebrado vai dar push quando chegar no nó folha
            ArrayList<Argumento> argumentosAntesRelacao = new ArrayList<>();
            Argumento argumentoAntesRelacao = new Argumento();
            /*Essa parte é responsável por separar uma parte da relação e adicionar no argumento quando necessário*/
            SujeitoRelacaoArgumentos sraAux = separaParteTokensRelacaoEmArgumentos(sra);
            sra.setSujeitoRelacao(sraAux.getSujeitoRelacao());
            for (Argumento argumentosSraAux : sraAux.getArgumentos()) {
                for (Deque<Token> dequeToken : argumentosSraAux.getClausulas()) {
                    argumento.setClausulaArrayClausulas(dequeToken);
                }
            }
            if (sra.getArgumentos().size() > 0) {
                argumento = sra.getArgumentos().get(0);
                sra.getArgumentos().clear();
            }
            /*-----------------------------------------------------------*/

            /*Adiciona código a respeito do tipo do módulo*/
            argumento.setIdentificadorModuloExtracao(1);
            argumentoAntesRelacao.setIdentificadorModuloExtracao(1);
            /*-----------------------------------------------------------*/
            Token tokenNucleoRelacao = this.sentence.getSentenca().get(itemSujeitoRelacao.getIndiceNucleoRelacao());
            for (Token tokenFilhoSujeitoRelacao : tokenNucleoRelacao.getTokensFilhos()) {
                if (vetorBooleanoTokensVisitados.length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados().length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados().length > 0) {//Esse if é necessário, pois se argumentoVerboLigacao sentença não tiver sujeito o programa não da erro de vetor null
                    if (!vetorBooleanoTokensVisitados[tokenFilhoSujeitoRelacao.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenFilhoSujeitoRelacao.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados()[tokenFilhoSujeitoRelacao.getId()]) {
                        if (tokenFilhoSujeitoRelacao.getDeprel().equals("nmod") || tokenFilhoSujeitoRelacao.getDeprel().contains("xcomp") || tokenFilhoSujeitoRelacao.getDeprel().equals("dobj") || tokenFilhoSujeitoRelacao.getDeprel().equals("obj") || tokenFilhoSujeitoRelacao.getDeprel().equals("acl:relcl") || tokenFilhoSujeitoRelacao.getDeprel().equals("iobj") || (tokenFilhoSujeitoRelacao.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenFilhoSujeitoRelacao)) || tokenFilhoSujeitoRelacao.getDeprel().equals("acl:part")/* && !tokenPilha.getPostag().equals("VERB")*/ || tokenFilhoSujeitoRelacao.getDeprel().equals("nummod") || tokenFilhoSujeitoRelacao.getDeprel().equals("advmod") || (tokenFilhoSujeitoRelacao.getDeprel().equals("appos") /*&& !tokenFilhoSujeitoRelacao.getCpostag().equals("PROPN")*/) || tokenFilhoSujeitoRelacao.getDeprel().equals("amod") || (tokenFilhoSujeitoRelacao.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)) || (tokenFilhoSujeitoRelacao.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)) || tokenFilhoSujeitoRelacao.getDeprel().equals("dep") || (tokenFilhoSujeitoRelacao.getDeprel().equals("punct") && pontuacaoValida(tokenFilhoSujeitoRelacao)) && (tokenFilhoSujeitoRelacao.getId() > tokenFilhoSujeitoRelacao.getHead())) {
                            vetorBooleanoTokensVisitados[tokenFilhoSujeitoRelacao.getId()] = true;
                            pilhaAuxiliar.push(tokenFilhoSujeitoRelacao);//Adiciona na pilha o token
//                            System.out.println(tokenFilhoSujeitoRelacao.getForm());
                            flagExtracao = true;
                            if (tokenFilhoSujeitoRelacao.getDeprel().equals("conj") && tokenConj == null) {
                                tokenConj = tokenFilhoSujeitoRelacao;
                            }
                            /*Esse if foi utilizado para verificar as cláusulas anteriores à relação*/
                            if (tokenFilhoSujeitoRelacao.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                adicionaPedacoArgumento(argumento, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                            } else {
                                /*Essa verificação é feita em vários locais. Em certos casos, mesmo o token sendo antes da
                                 relação ele deve ser adicionado à frente*/
                                if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                    adicionaPedacoArgumento(argumento, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                                } else {
                                    adicionaPedacoArgumento(argumentoAntesRelacao, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                                }
                            }
                        } else {
                            /*Nesse caso de ccomp e advcl com sujeito extrai direto*/
                            if ((SC == true) && ((tokenFilhoSujeitoRelacao.getDeprel().equals("ccomp") && verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)) || (tokenFilhoSujeitoRelacao.getDeprel().equals("advcl") && verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)))) {
                                realizaApontamentoArgumento(argumento, tokenFilhoSujeitoRelacao);
                                sra.addArg2(argumento);
                                flagExtracao = false;
                                /*Esse break não seria necessário se a árvore de dependência retornada pelo AD fosse sempre correta.
                                 Ex: Importante lembrar que a violencia ocorre fora dos estadios com as torcidas
                                 o DP nao pode retornar torcidas como filho de importante, pois é uma oração subordinada
                                 e torcida deveria depender da oração corretamente que seria a oração de "a violência...", mas as vezes nao acontece*/
                                break;
                            }
                        }
                    }
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados()[tokenPilha.getId()]) {
                                if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().contains("xcomp") || tokenPilha.getDeprel().equals("dobj") || tokenPilha.getDeprel().equals("obj") || tokenPilha.getDeprel().equals("iobj") || tokenPilha.getDeprel().equals("acl:relcl") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || (tokenPilha.getDeprel().equals("appos") /*&& !tokenPilha.getCpostag().equals("PROPN")*/) || (tokenPilha.getDeprel().equals("conj") /*&& !heuristicasVerificaConjRelacao(tokenPilha)*/) || tokenPilha.getDeprel().equals("amod") || (tokenPilha.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenPilha)) || (tokenPilha.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenPilha)) || tokenPilha.getDeprel().equals("acl:part") || tokenPilha.getDeprel().equals("dep") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha)) && (tokenPilha.getId() > tokenPilha.getHead())) /*&& (tokenFilhoRelacaoSujeito.id > tokenFilhoRelacaoSujeito.head*/ {
                                    pilhaAuxiliar.push(tokenPilha);
//                                    System.out.println(tokenPilha.getForm());
                                    if ((tokenPilha.getDeprel().equals("conj")) && (tokenConj == null)) {
                                        tokenConj = tokenPilha;
                                    }
                                    flagExtracao = true;
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                    if (tokenPilha.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                        adicionaPedacoArgumento(argumento, tokenPilha, vetorBooleanoTokensVisitados);
                                    } else {
                                        if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                            adicionaPedacoArgumento(argumento, tokenPilha, vetorBooleanoTokensVisitados);
                                        } else {
                                            adicionaPedacoArgumento(argumentoAntesRelacao, tokenPilha, vetorBooleanoTokensVisitados);
                                        }
                                    }
                                } else {
                                    if ((SC == true) && ((tokenPilha.getDeprel().equals("ccomp") && verificaTokenFilhoSujeito(tokenPilha)) || (tokenPilha.getDeprel().equals("advcl") && verificaTokenFilhoSujeito(tokenPilha)))) {
                                        realizaApontamentoArgumento(argumento, tokenPilha);
                                        sra.addArg2(argumento);
                                        flagExtracao = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (flagExtracao) { //Entrar nesse 'IF' significa que a busca chegou em algum nó folha
                            /*Toda essa comparação teve que ser feita para quando chegar no nó folha e for amod/advcl/conj... extrair de forma completa*/
                            if (pilhaAuxiliar.peek().getDeprel().equals("amod") || pilhaAuxiliar.peek().getDeprel().equals("advmod")) {
                                if (!verificaFilhosTokenPai(pilhaAuxiliar.peek(), vetorBooleanoTokensVisitados)) {
                                    if (argumento.getClausulas().size() > 0) {
                                        sra.addArg2(argumento.retornaClone());
                                    }
                                    if (pilhaAuxiliar.peek().getId() < sra.getSujeitoRelacao().getIndiceNucleoRelacao() && !verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                        argumentosAntesRelacao.add(argumentoAntesRelacao.retornaClone());
                                    }
                                }
                            } else if (tokenConj != null) {
                                if (!verificaFilhosTokenPai(tokenConj, vetorBooleanoTokensVisitados)) {
                                    if (argumento.getClausulas().size() > 0) {
                                        sra.addArg2(argumento.retornaClone());
                                    }
                                    if (pilhaAuxiliar.peek().getId() < sra.getSujeitoRelacao().getIndiceNucleoRelacao() && !verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                        argumentosAntesRelacao.add(argumentoAntesRelacao.retornaClone());
                                    }
                                    tokenConj = null;
                                }
                            } else {
                                if (argumento.getClausulas().size() > 0) {
                                    sra.addArg2(argumento.retornaClone());
                                }
                                if (pilhaAuxiliar.peek().getId() < sra.getSujeitoRelacao().getIndiceNucleoRelacao() && !verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                    argumentosAntesRelacao.add(argumentoAntesRelacao.retornaClone());
                                }
                            }
                            /*Verifica se o Argumento tem clausulas para não adicionar um argumento com cláusulas vazias*/
                        }
                        pilhaAuxiliar.pop();
                        flagExtracao = false;
                    }
                } else {
                    System.out.println("ESTE SR ESTÁ COM ALGUM PROBLEMA RELACIONADO AO TAMANHO DOS VETORES BOOLEANOS");
                }
            }

            Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
            adicionaArgumentosAntesDasRelacoesNasExtracoesEObjCompNaRelacao(sra, argumentosAntesRelacao);
        }
//        moduloExtracaoAposto();
//        adicionaArgumentoMarkAntesSujeito();
//        eliminaPreposicaoAntesSujeito();
//        moduloSubstituiApostoTransitividade(this.sujeitoRelacaoArgumentos);
//        moduloExtracaoVerboLigacao();
    }

    public void extraiClausulasQuebradas(boolean SC) {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"

        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            Token tokenConj = null;/*Esse token é usado para verificar se ele tem filho 'conj'. Se tiver ele vai ser verificado
             se possui outros filhos 'conj' para extrair tudo em uma só tripla para não ter extrações redundantes*/

            SujeitoRelacao itemSujeitoRelacao = sra.getSujeitoRelacao();
            //O argumento quebrado vai dar push quando chegar no nó folha
            Argumento argumentoQuebrado = new Argumento();
            /*Essa parte é responsável por separar uma parte da relação e adicionar no argumento quando necessário*/
//            SujeitoRelacaoArgumentos sraAux = separaParteTokensRelacaoEmArgumentos(sra);
//            sra.setSujeitoRelacao(sraAux.getSujeitoRelacao());
//            for (Argumento argumentosSraAux : sraAux.getArgumentos()) {
//                for (Deque<Token> dequeToken : argumentosSraAux.getClausulas()) {
//                    argumentoQuebrado.setClausulaArrayClausulas(dequeToken);
//                }
//            }
//            if (sra.getArgumentos().size() > 0) {
//                argumentoQuebrado = sra.getArgumentos().get(0);
//                sra.getArgumentos().clear();
//            }
            /*-----------------------------------------------------------*/

            /*Adiciona código a respeito do tipo do módulo*/
            argumentoQuebrado.setIdentificadorModuloExtracao(7);
            int qtdFilhosIdMaiorRaizPercorridosQueNaoSejaAdvMod = 0;
            /*-----------------------------------------------------------*/
            Token tokenNucleoRelacao = this.sentence.getSentenca().get(itemSujeitoRelacao.getIndiceNucleoRelacao());
            for (Token tokenFilhoSujeitoRelacao : tokenNucleoRelacao.getTokensFilhos()) {
                argumentoQuebrado = retornaObjetosComplementos(sra);
                if (vetorBooleanoTokensVisitados.length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados().length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados().length > 0) {//Esse if é necessário, pois se argumentoVerboLigacao sentença não tiver sujeito o programa não da erro de vetor null
                    if (!vetorBooleanoTokensVisitados[tokenFilhoSujeitoRelacao.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenFilhoSujeitoRelacao.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados()[tokenFilhoSujeitoRelacao.getId()]) {
                        if (tokenFilhoSujeitoRelacao.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                            if (tokenFilhoSujeitoRelacao.getDeprel().equals("nmod") || tokenFilhoSujeitoRelacao.getDeprel().equals("acl:relcl") || (tokenFilhoSujeitoRelacao.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenFilhoSujeitoRelacao)) || tokenFilhoSujeitoRelacao.getDeprel().equals("acl:part")/* && !tokenPilha.getPostag().equals("VERB")*/ || tokenFilhoSujeitoRelacao.getDeprel().equals("nummod") || tokenFilhoSujeitoRelacao.getDeprel().equals("advmod") || (tokenFilhoSujeitoRelacao.getDeprel().equals("appos") /*&& !tokenFilhoSujeitoRelacao.getCpostag().equals("PROPN")*/) || tokenFilhoSujeitoRelacao.getDeprel().equals("amod") || (tokenFilhoSujeitoRelacao.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)) || (tokenFilhoSujeitoRelacao.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)) || tokenFilhoSujeitoRelacao.getDeprel().equals("dep")) {
                                if (!tokenFilhoSujeitoRelacao.getDeprel().equals("advmod")) {
                                    qtdFilhosIdMaiorRaizPercorridosQueNaoSejaAdvMod++;
                                }
                                vetorBooleanoTokensVisitados[tokenFilhoSujeitoRelacao.getId()] = true;
                                pilhaAuxiliar.push(tokenFilhoSujeitoRelacao);//Adiciona na pilha o token
                                flagExtracao = true;
                                if (tokenFilhoSujeitoRelacao.getDeprel().equals("conj") && tokenConj == null) {
                                    tokenConj = tokenFilhoSujeitoRelacao;
                                }
                                /*Esse if foi utilizado para verificar as cláusulas anteriores à relação*/
                                if (tokenFilhoSujeitoRelacao.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                    adicionaPedacoArgumento(argumentoQuebrado, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                                } else {
                                    /*Essa verificação é feita em vários locais. Em certos casos, mesmo o token sendo antes da
                                     relação ele deve ser adicionado à frente*/
                                    if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                        adicionaPedacoArgumento(argumentoQuebrado, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                                    }
                                }
                            } else {
                                if (tokenFilhoSujeitoRelacao.getDeprel().contains("xcomp") || tokenFilhoSujeitoRelacao.getDeprel().equals("dobj") || tokenFilhoSujeitoRelacao.getDeprel().equals("obj") || tokenFilhoSujeitoRelacao.getDeprel().equals("iobj") && (tokenFilhoSujeitoRelacao.getId() > tokenFilhoSujeitoRelacao.getHead()) && (tokenFilhoSujeitoRelacao.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao())) {
                                    vetorBooleanoTokensVisitados[tokenFilhoSujeitoRelacao.getId()] = true;
                                    flagExtracao = true;
                                    /*Se a quantidade de clausulas for maior que 0 significa que não é o primeiro filho direto da relação
                                     então eu preciso adcionar os argumentos no array*/
                                    if (qtdFilhosIdMaiorRaizPercorridosQueNaoSejaAdvMod != 0) {
                                        pilhaAuxiliar.push(tokenFilhoSujeitoRelacao);
                                        if (tokenFilhoSujeitoRelacao.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                            adicionaPedacoArgumento(argumentoQuebrado, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                                        } else {
                                            /*Essa verificação é feita em vários locais. Em certos casos, mesmo o token sendo antes da
                                             relação ele deve ser adicionado à frente*/
                                            if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                                adicionaPedacoArgumento(argumentoQuebrado, tokenFilhoSujeitoRelacao, vetorBooleanoTokensVisitados);
                                            }
                                        }
                                    }
                                    qtdFilhosIdMaiorRaizPercorridosQueNaoSejaAdvMod++;
                                } else {
                                    /*Nesse caso de ccomp e advcl com sujeito extrai direto*/
                                    if ((SC == true) && ((tokenFilhoSujeitoRelacao.getDeprel().equals("ccomp") && verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)) || (tokenFilhoSujeitoRelacao.getDeprel().equals("advcl") && verificaTokenFilhoSujeito(tokenFilhoSujeitoRelacao)))) {
                                        qtdFilhosIdMaiorRaizPercorridosQueNaoSejaAdvMod++;
                                        realizaApontamentoArgumento(argumentoQuebrado, tokenFilhoSujeitoRelacao);
                                        sra.addArg2(argumentoQuebrado);
                                        flagExtracao = false;
                                        /*Esse break não seria necessário se a árvore de dependência retornada pelo AD fosse sempre correta.
                                         Ex: Importante lembrar que a violencia ocorre fora dos estadios com as torcidas
                                         o DP nao pode retornar torcidas como filho de importante, pois é uma oração subordinada
                                         e torcida deveria depender da oração corretamente que seria a oração de "a violência...", mas as vezes nao acontece*/
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados()[tokenPilha.getId()]) {
                                if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().contains("xcomp") || tokenPilha.getDeprel().equals("dobj") || tokenPilha.getDeprel().equals("obj") || tokenPilha.getDeprel().equals("iobj") || tokenPilha.getDeprel().equals("acl:relcl") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || (tokenPilha.getDeprel().equals("appos") /*&& !tokenPilha.getCpostag().equals("PROPN")*/) || (tokenPilha.getDeprel().equals("conj") /*&& !heuristicasVerificaConjRelacao(tokenPilha)*/) || tokenPilha.getDeprel().equals("amod") || (tokenPilha.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenPilha)) || (tokenPilha.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenPilha)) || tokenPilha.getDeprel().equals("acl:part") || tokenPilha.getDeprel().equals("dep") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha)) && (tokenPilha.getId() > tokenPilha.getHead())) /*&& (tokenFilhoRelacaoSujeito.id > tokenFilhoRelacaoSujeito.head*/ {
                                    pilhaAuxiliar.push(tokenPilha);
//                                    System.out.println(tokenPilha.getForm());
                                    if ((tokenPilha.getDeprel().equals("conj")) && (tokenConj == null)) {
                                        tokenConj = tokenPilha;
                                    }
                                    flagExtracao = true;
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                    if (tokenPilha.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                        adicionaPedacoArgumento(argumentoQuebrado, tokenPilha, vetorBooleanoTokensVisitados);
                                    } else {
                                        if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                            adicionaPedacoArgumento(argumentoQuebrado, tokenPilha, vetorBooleanoTokensVisitados);
                                        }
                                    }
                                } else {
                                    if ((SC == true) && ((tokenPilha.getDeprel().equals("ccomp") && verificaTokenFilhoSujeito(tokenPilha)) || (tokenPilha.getDeprel().equals("advcl") && verificaTokenFilhoSujeito(tokenPilha)))) {
                                        realizaApontamentoArgumento(argumentoQuebrado, tokenPilha);
                                        sra.addArg2(argumentoQuebrado);
                                        flagExtracao = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (flagExtracao) { //Entrar nesse 'IF' significa que a busca chegou em algum nó folha
                            /*Toda essa comparação teve que ser feita para quando chegar no nó folha e for amod/advcl/conj... extrair de forma completa*/
                            if (pilhaAuxiliar.peek().getDeprel().equals("amod") || pilhaAuxiliar.peek().getDeprel().equals("advmod")) {
                                if (!verificaFilhosTokenPai(pilhaAuxiliar.peek(), vetorBooleanoTokensVisitados)) {
                                    if (argumentoQuebrado.getClausulas().size() > 0) {
                                        sra.addArg2(argumentoQuebrado.retornaClone());
                                    }
                                }
                            } else if (tokenConj != null) {
                                if (!verificaFilhosTokenPai(tokenConj, vetorBooleanoTokensVisitados)) {
                                    if (argumentoQuebrado.getClausulas().size() > 0) {
                                        sra.addArg2(argumentoQuebrado.retornaClone());
                                    }
                                    tokenConj = null;
                                }
                            } else {
                                if (argumentoQuebrado.getClausulas().size() > 0) {
                                    sra.addArg2(argumentoQuebrado.retornaClone());
                                }
                            }
                            /*Verifica se o Argumento tem clausulas para não adicionar um argumento com cláusulas vazias*/
                        }
                        /*Esse trecho é utilizado para fazer extração da mesma forma que o antigo DependentIE*/
                        if (pilhaAuxiliar.peek().getDeprel().equals("amod") || pilhaAuxiliar.peek().getDeprel().equals("advmod")) {
                            if (!verificaFilhosTokenPai(pilhaAuxiliar.peek(), vetorBooleanoTokensVisitados)) {
                                while ((argumentoQuebrado.getClausulas().size() != pilhaAuxiliar.size() - 1) && (!argumentoQuebrado.getClausulas().empty())) {
                                    argumentoQuebrado.getClausulas().pop();
                                }
                            }
                        } else if (tokenConj != null) {
                            if (!verificaFilhosTokenPai(tokenConj, vetorBooleanoTokensVisitados)) {
                                while ((argumentoQuebrado.getClausulas().size() != pilhaAuxiliar.size() - 1) && (!argumentoQuebrado.getClausulas().empty())) {
                                    argumentoQuebrado.getClausulas().pop();
                                }
                            }
                        } else {
                            while ((argumentoQuebrado.getClausulas().size() != pilhaAuxiliar.size() - 1) && (!argumentoQuebrado.getClausulas().empty())) {
                                argumentoQuebrado.getClausulas().pop();
                            }
                        }
                        /*---------------------------------------*/
                        pilhaAuxiliar.pop();
                        flagExtracao = false;
                    }
                } else {
                    System.out.println("ESTE SR ESTÁ COM ALGUM PROBLEMA RELACIONADO AO TAMANHO DOS VETORES BOOLEANOS");
                }
            }

            Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        }
    }

    public void moduloProcessamentoConjuncoesCoordenativasArg2() {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Token tokenConj = null;/*Esse token é usado para verificar se ele tem filho 'conj'. Se tiver ele vai ser verificado
         se possui outros filhos 'conj' para extrair tudo em uma só tripla para não ter extrações redundantes*/

        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        /*As próximas 2 variáveis são utilizadas para gravar o caminho percorrido para fazer a busca da conjunção corretamente*/
        Argumento caminhoPercorrido = null;
        Token tokenNucleoPaiConjuncoes = null;
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha = new Token();//recebe os filhos de "elementoPilha"
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            boolean flagIndicaExtracaoAntesQuebrarConjuncao = true;
            SujeitoRelacao itemSujeitoRelacao = sra.getSujeitoRelacao();
            Argumento argumento = new Argumento();
            ArrayList<Argumento> argumentosAntesRelacao = new ArrayList<>();
            Argumento argumentoAntesRelacao = new Argumento();
            /*Adiciona informações a respeito do tipo do módulo*/
            argumento.setIdentificadorModuloExtracao(2);
            argumentoAntesRelacao.setIdentificadorModuloExtracao(2);
            /*-----------------------------------------------------------*/
            Token tokenNucleoRelacao = this.sentence.getSentenca().get(itemSujeitoRelacao.getIndiceNucleoRelacao());
            for (Token tokenFilho : tokenNucleoRelacao.getTokensFilhos()) {
                if (vetorBooleanoTokensVisitados.length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados().length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados().length > 0) {//Esse if é necessário, pois se argumentoVerboLigacao sentença não tiver sujeito o programa não da erro de vetor null
                    if (!vetorBooleanoTokensVisitados[tokenFilho.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenFilho.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados()[tokenFilho.getId()]) {
                        if (tokenFilho.getDeprel().equals("nmod") || tokenFilho.getDeprel().contains("xcomp") || tokenFilho.getDeprel().equals("dobj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("iobj") || tokenFilho.getDeprel().equals("acl:part") || tokenFilho.getDeprel().equals("nummod") || tokenFilho.getDeprel().equals("advmod") || tokenFilho.getDeprel().equals("appos") || tokenFilho.getDeprel().equals("amod") || (tokenFilho.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenFilho)) || (tokenFilho.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenFilho)) || (tokenFilho.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenFilho)) || tokenFilho.getDeprel().equals("dep") || (tokenFilho.getDeprel().equals("punct") && pontuacaoValida(tokenFilho)) && (tokenFilho.getId() > tokenFilho.getHead())) {
//                            System.out.println("1 papapa");
                            vetorBooleanoTokensVisitados[tokenFilho.getId()] = true;
                            pilhaAuxiliar.push(tokenFilho);//Adiciona na pilha o token
//                            System.out.println(tokenFilho.getForm());
//                            flagExtracao = true;
                            if (tokenFilho.getDeprel().equals("conj") && tokenConj == null) {
                                tokenConj = tokenFilho;
                            }
                            if (tokenFilho.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                adicionaPedacoArgumento(argumento, tokenFilho, vetorBooleanoTokensVisitados);
                            } else {
                                if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                    adicionaPedacoArgumento(argumento, tokenFilho, vetorBooleanoTokensVisitados);
                                } else {
                                    adicionaPedacoArgumento(argumentoAntesRelacao, tokenFilho, vetorBooleanoTokensVisitados);
                                }
                            }
                        }
                    }
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensRelacaoVisitados()[tokenPilha.getId()]) {
                                /*Nesse caso é feita a verificação se a conjunção tem um pai que seja conjunção também, pois nesses casos não serão quebrados*/
                                if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().contains("xcomp") || tokenPilha.getDeprel().equals("dobj") || tokenPilha.getDeprel().equals("obj") || tokenPilha.getDeprel().equals("iobj") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || tokenPilha.getDeprel().equals("appos") || (tokenPilha.getDeprel().equals("conj") && this.sentence.getSentenca().get(tokenPilha.getHead()).getDeprel().equals("conj")) || tokenPilha.getDeprel().equals("amod") || (tokenPilha.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenPilha)) || (tokenPilha.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenPilha)) || tokenPilha.getDeprel().equals("acl:part") || tokenPilha.getDeprel().equals("dep") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha)) && (tokenPilha.getId() > tokenPilha.getHead())) /*&& (tokenFilhoRelacaoSujeito.id > tokenFilhoRelacaoSujeito.head*/ {
                                    pilhaAuxiliar.push(tokenPilha);
//                                    System.out.println(tokenPilha.getForm());
                                    if ((tokenPilha.getDeprel().equals("conj")) && (tokenConj == null)) {
                                        tokenConj = tokenPilha;
                                    }
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                    if (tokenPilha.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                                        adicionaPedacoArgumento(argumento, tokenPilha, vetorBooleanoTokensVisitados);
                                    } else {
                                        if (verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                            adicionaPedacoArgumento(argumento, tokenPilha, vetorBooleanoTokensVisitados);
                                        } else {
                                            adicionaPedacoArgumento(argumentoAntesRelacao, tokenPilha, vetorBooleanoTokensVisitados);
                                        }
                                    }
                                } else if (tokenPilha.getDeprel().equals("conj") && !tokenPilha.getPostag().equals("VERB") && !this.sentence.getSentenca().get(tokenPilha.getHead()).getDeprel().equals("conj")) {
                                    if (tokenNucleoPaiConjuncoes == null) {
                                        tokenNucleoPaiConjuncoes = this.sentence.getSentenca().get(tokenPilha.getHead());
                                        caminhoPercorrido = argumento.retornaClone();
                                    } else {
                                        if (tokenNucleoPaiConjuncoes.getId() != tokenPilha.getHead()) {
                                            tokenNucleoPaiConjuncoes = this.sentence.getSentenca().get(tokenPilha.getHead());
                                            caminhoPercorrido = argumento.retornaClone();
                                        }
                                    }
                                    if (flagIndicaExtracaoAntesQuebrarConjuncao) {
                                        flagIndicaExtracaoAntesQuebrarConjuncao = false;
                                        /*Nesse ponto vai ser realizada uma extração antes de tratar a conjunção. Se isso não for feito vai 
                                         ficar faltando tripla ser extraída. Além disso, só vai ser necessário fazer isso UMA ÚNICA VER, por isso
                                         um flag vai ser utilizada.
                                         */
                                        if (argumento.getClausulas().size() > 0) {
                                            sra.addArg2(argumento.retornaClone());
                                            if (argumentoAntesRelacao.getClausulas().size() > 0) {
                                                /*Agora verifico se o último vetor de argumentos antes da relação (pois é o mais completo) tem cláusulas antes da relação para adicionar
                                                 outra tripla*/
                                                Argumento argumentoAuxConcatenado = argumento.retornaClone();
                                                for (Deque<Token> deque : argumentoAntesRelacao.getClausulas()) {
                                                    argumentoAuxConcatenado.setClausulaArrayClausulas(deque);
                                                }
                                                sra.addArg2(argumentoAuxConcatenado.retornaClone());
                                            }
                                        }
                                    }
                                    //
                                    tokenPilha.setDeprel("conjCC");/*Nessa função argumentoVerboLigacao conjunção coordenada é tratada de forma diferente da buscaSujeito
                                     //por isso foi alterado o Deprel. Assim, na função para concatenar outra função será chamada */

                                    argumento = trataCasoEspecialArg2ComConjuncao(caminhoPercorrido, tokenPilha, vetorBooleanoTokensVisitados);
//                                    for(Deque<Token> arrayDeque : argumento.getClausulas()){
//                                        for(Token t : arrayDeque){
//                                            System.out.print(t.getForm() + " ");
//                                        }
//                                    }
//                                    System.out.println();
                                    tokenPilha.setDeprel("conj");
                                    pilhaAuxiliar.push(tokenPilha);
//                                    System.out.println("conj: " + tokenPilha.getForm());
                                    flagExtracao = true;
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                        if (flagExtracao) { //Entrar nesse 'IF' significa que a busca chegou em algum nó folha
                            /*Toda essa comparação teve que ser feita para quando chegar no nó folha e for amod/advcl/conj... extrair de forma completa, pois se chegar em um nó folha e tiver 
                             amod, etc..., e a árvore de dependência ainda tiver mais filhos não deve extrai, senão vai ficar incompleto*/

                            /*Criando flag para verificar se pode criar outro argumento com a parte contextual. Isso está sendo feito
                             para reduzir a quantidade de código redundante. Da pra fazer também com as outras partes da função*/
                            boolean flagIndicaExtracaoParteContextual = false;
                            if (pilhaAuxiliar.peek().getDeprel().equals("amod") || pilhaAuxiliar.peek().getDeprel().equals("advmod")) {
                                if (!verificaFilhosTokenPai(pilhaAuxiliar.peek(), vetorBooleanoTokensVisitados)) {
                                    if (argumento.getClausulas().size() > 0) {
                                        sra.addArg2(argumento.retornaClone());
                                        flagExtracao = false;
                                    }
                                    if (pilhaAuxiliar.peek().getId() < sra.getSujeitoRelacao().getIndiceNucleoRelacao() && !verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                        argumentosAntesRelacao.add(argumentoAntesRelacao.retornaClone());
                                        flagExtracao = false;
                                    }
                                    flagIndicaExtracaoParteContextual = true;
                                }
                            } else if (tokenConj != null) {
                                if (!verificaFilhosTokenPai(tokenConj, vetorBooleanoTokensVisitados)) {
                                    if (argumento.getClausulas().size() > 0) {
                                        sra.addArg2(argumento.retornaClone());
                                        flagExtracao = false;
                                    }
                                    if (pilhaAuxiliar.peek().getId() < sra.getSujeitoRelacao().getIndiceNucleoRelacao() && !verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                        argumentosAntesRelacao.add(argumentoAntesRelacao.retornaClone());
                                        flagExtracao = false;
                                    }
                                    tokenConj = null;
                                    flagIndicaExtracaoParteContextual = true;
                                }
                            } else {
                                if (argumento.getClausulas().size() > 0) {
                                    sra.addArg2(argumento.retornaClone());
                                    flagExtracao = false;
                                }
                                if (pilhaAuxiliar.peek().getId() < sra.getSujeitoRelacao().getIndiceNucleoRelacao() && !verificaTokenPaiAntesDepoisNucleoRelacao(pilhaAuxiliar.peek(), sra)) {
                                    argumentosAntesRelacao.add(argumentoAntesRelacao.retornaClone());
                                    flagExtracao = false;
                                }
                                flagIndicaExtracaoParteContextual = true;
                            }
                            /*Verifica se o Argumento tem clausulas para não adicionar um argumento com cláusulas vazias*/
                            if (flagIndicaExtracaoParteContextual) {
                                /*Adiciona a parte contextual no fim das extrações com conjunção*/
                                if (argumentoAntesRelacao.getClausulas().size() > 0) {
                                    Argumento argAux = argumento.retornaClone();
                                    for (Deque<Token> clausulaAntesRelacao : argumentoAntesRelacao.getClausulas()) {
                                        argAux.setClausulaArrayClausulas(clausulaAntesRelacao);
                                    }
                                    sra.addArg2(argAux);
                                }
                            }
                        }
                        Token t = pilhaAuxiliar.pop();
//                        System.out.println("Pop token e ID do token: " + t.getForm() + " " + t.getId());
                    }
                } else {
                    System.out.println("ESTE SR ESTÁ COM ALGUM PROBLEMA RELACIONADO AO TAMANHO DOS VETORES BOOLEANOS");
                }
            }
            Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
            System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
            sra.getSujeitoRelacao().setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
            Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
            adicionaArgumentosAntesDasRelacoesNasExtracoesEObjCompNaRelacao(sra, argumentosAntesRelacao);
        }
    }

    public Argumento trataCasoEspecialArg2ComConjuncao(Argumento argumentoClausulasPercorridas, Token tokenConj, Boolean[] vetorBooleanoTokensVisitados) {
        //System.out.println("Argumentos percorridos: " + argumentoClausulasPercorridas.toString());

        Argumento argumento = new Argumento();
        argumento.setIdentificadorModuloExtracao(2);
        //Busca em profundidade feita apenas para trás
//        Boolean[] vetorBooleanoTokensVisitadosParaTras = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
//        Arrays.fill(vetorBooleanoTokensVisitadosParaTras, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        QuebraLoopNoFolha:
        for (Token tokenSentenca : this.sentence.getSentenca().get(tokenConj.getHead()).getTokensFilhos()) {
            if (!vetorBooleanoTokensVisitados[tokenSentenca.getId()]) {
                if ((tokenSentenca.getDeprel().equals("nmod") || tokenSentenca.getDeprel().equals("nummod") || tokenSentenca.getDeprel().equals("advmod") || tokenSentenca.getDeprel().equals("appos") || tokenSentenca.getDeprel().equals("amod") || tokenSentenca.getDeprel().equals("dep") || tokenSentenca.getDeprel().equals("obj")) && tokenSentenca.getId() < tokenSentenca.getHead()) {
                    pilhaAuxiliar.push(tokenSentenca);//Adiciona na pilha o token
                    adicionaPedacoArgumento(argumento, tokenSentenca, vetorBooleanoTokensVisitados);
                    flagExtracao = true;
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                                if ((tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || tokenPilha.getDeprel().equals("appos") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj")) && tokenPilha.getId() < tokenPilha.getHead()) {
                                    pilhaAuxiliar.push(tokenPilha);
                                    adicionaPedacoArgumento(argumento, tokenPilha, vetorBooleanoTokensVisitados);
                                    flagExtracao = true;
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                        if (flagExtracao) { //Entrar nesse 'IF' significa que argumentoVerboLigacao busca chegou em algum nó folha
                            break QuebraLoopNoFolha;
                        }
                        pilhaAuxiliar.pop();
                        flagExtracao = false;
                    }
                }
            }
        }
        for (Deque<Token> deque : argumento.getClausulas()) {
            for (Token t : deque) {
                System.out.println(t.getForm());
            }
        }
//        boolean flagIndicaPaiTokenConj = false;
//        System.out.println("Argumentos percorridos: " + argumentoClausulasPercorridas.toString());
//        System.out.println("Tamanho Cláusula: " + argumentoClausulasPercorridas.getClausulas().size());
        for (Deque<Token> dequeToken : argumentoClausulasPercorridas.getClausulas()) {
            /*Esta flag vai indicar se é o pai de tokenconj. Isso é necessário para verificar se vou adicionar essa
             cláusula no Arg2 ou não*/
//            for (Token t : dequeToken) {
                /*Verifico se o head do token conj está na cláusula*/
            if (!(verificaTokenArray(this.sentence.getSentenca().get(tokenConj.getHead()), dequeToken))) {
                argumento.setClausulaArrayClausulas(dequeToken);
            }
//            }
//            if (!flagIndicaPaiTokenConj) {
//                argumento.setClausulaArrayClausulas(dequeToken);
//            }
        }
//        verificaPreposicaoAntesToken
//        retornaApenasConjuncoesAterioresAoToken
        if (!verificaPreposicaoAntesToken(tokenConj)) {
            Deque<Token> dequePreposicaoParaAdicionarAntesDoTokenConj = retornaApenasConjuncoesAterioresAoToken(this.sentence.getSentenca().get(tokenConj.getHead()));
            argumento.setClausulaArrayClausulas(dequePreposicaoParaAdicionarAntesDoTokenConj);
        }
        adicionaPedacoArgumento(argumento, tokenConj, vetorBooleanoTokensVisitados);
        //System.out.println("Argumentos retornados: " + argumentoClausulasPercorridas.toString());
        return argumento;
    }

    //ESSA FUNÇÃO VAI RECEBER UM TOKEN E VAI DEIXAR OS TOKENS FILHOS EM SEQUÊNCIA NO ARRAY: det, case, mark ...
    public Deque<Token> retornaTokenConcatenadoSujeitoArgumentos(Token token, Boolean[] vetorBooleanoTokensVisitados) {
        Deque<Token> argumentoConcatenado = new ArrayDeque<Token>();
//        System.out.println(token.getForm());
        if (token.getDeprel().equals("nmod") || token.getDeprel().equals("acl:part") || token.getDeprel().contains("xcomp") || token.getDeprel().equals("nummod") || token.getDeprel().equals("advmod") || token.getDeprel().equals("amod") || token.getDeprel().equals("obj") || token.getDeprel().equals("iobj") || token.getDeprel().equals("acl:relcl")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
//                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                            argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                            vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
//                            }
                        }
                    }
                    flag = false;
                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                    if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct") && pontuacaoValida(tokenAnterior)) {
                            argumentoConcatenado.addFirst(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
//                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
//                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
//                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/
//
//                    if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
//                        argumentoConcatenado.add(tokenPosterior);
//                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
//                    }
//                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("appos")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
//                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                            argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                            vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
//                            }
                        }
                    }
                    flag = false;
                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                    if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct") && pontuacaoValida(tokenAnterior)) {
                            argumentoConcatenado.addFirst(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
//                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
//                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
//                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/
//
//                    if (tokenPosterior.getDeprel().equals("punct")) {
//                        argumentoConcatenado.add(tokenPosterior);
//                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
//                    }
//                }
            }
            return argumentoConcatenado;
        }

//        if (token.getDeprel().equals("root")) {
//            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
//            for (Token tokensFilhos : token.getTokensFilhos()) {
//                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
//                    if (!((tokensFilhos.getPostag().equals("VERB") || tokensFilhos.getDeprel().equals("nmod") || tokensFilhos.getDeprel().equals("nsubj") || tokensFilhos.getDeprel().equals("acl:part") || tokensFilhos.getDeprel().contains("xcomp") || tokensFilhos.getDeprel().equals("advmod") || tokensFilhos.getDeprel().equals("amod") || tokensFilhos.getDeprel().equals("obj")))) {
//                        for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
//                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
//                                argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
//                                vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
//                            }
//                        }
//                        break;
//                    }
//                    flag = false;
//                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
//                    if (tokensFilhos.getDeprel().equals("flat") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
//                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
//                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/
//
//                        if (tokenAnterior.getDeprel().equals("punct")) {
//                            argumentoConcatenado.addFirst(tokenAnterior);
//                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
//                        }
//                        argumentoConcatenado.addLast(tokensFilhos);
//                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
//                    }
//                }
//            }
//            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
//                argumentoConcatenado.addFirst(token);
//                vetorBooleanoTokensVisitados[token.getId()] = true;
//                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
//                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
//                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/
//
//                    if (tokenPosterior.getDeprel().equals("punct")) {
//                        argumentoConcatenado.add(tokenPosterior);
//                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
//                    }
//                }
//            }
//
//            return argumentoConcatenado;
//        }
        if (token.getDeprel().equals("dep")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() > token.getHead()) {
                    if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                        for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                            if (flag) {
                                if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                                    argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                                    vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
                                }
                            }
                        }
                        flag = false;
                    } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                        if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                            Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                             pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                            if (tokenAnterior.getDeprel().equals("punct") && pontuacaoValida(tokenAnterior)) {
                                argumentoConcatenado.addFirst(tokenAnterior);
                                vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                            }
                            argumentoConcatenado.addLast(tokensFilhos);
                            vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                        }
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

//                    if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
//                        argumentoConcatenado.add(tokenPosterior);
//                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
//                    }
                }
            }

            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("conj")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
//                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                            argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                            vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
//                            }
                        }
                    }
                    flag = false;
                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                    if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct") && pontuacaoValida(tokenAnterior)) {
                            argumentoConcatenado.addFirst(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
//                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
//                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
//                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/
//
//                    if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
//                        argumentoConcatenado.add(tokenPosterior);
//                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
//                    }
//                }
            }

            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("conjCC")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
                            if (!(this.sentence.getSentenca().get(i).getForm().equals(",") || this.sentence.getSentenca().get(i).getDeprel().equals("cc"))) {
                                argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                                vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
                            }
                        }
                    }
                    flag = false;
                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                    if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct") && pontuacaoValida(tokenAnterior)) {
                            argumentoConcatenado.addFirst(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
//                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
//                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
//                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/
//
//                    if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
//                        argumentoConcatenado.add(tokenPosterior);
//                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
//                    }
//                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("mark")) {
            argumentoConcatenado.addLast(token);
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (!(tokensFilhos.getDeprel().equals("nmod") || tokensFilhos.getDeprel().equals("nummod") || tokensFilhos.getDeprel().equals("advmod") || tokensFilhos.getDeprel().equals("appos") || tokensFilhos.getDeprel().equals("amod") || tokensFilhos.getDeprel().equals("obj") || tokensFilhos.getDeprel().equals("dep"))) {
                    if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                        argumentoConcatenado.addFirst(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    } else {
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().contains("det") || token.getDeprel().equals("case")) {
            for (int i = token.getId(); i < token.getHead(); i++) {
                argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("punct")) {
            if (token.getId() < token.getHead()) {
                for (int i = token.getId(); i < token.getHead(); i++) {
                    argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                    vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
                }
                return argumentoConcatenado;
            } else {
                argumentoConcatenado.add(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
                return argumentoConcatenado;
            }

        }

        if (token.getDeprel().equals("nsubj") || token.getDeprel().equals("nsubj:pass")) {
            argumentoConcatenado.addLast(token);
            vetorBooleanoTokensVisitados[token.getId()] = true;
            for (Token tokensFilhos : token.getTokensFilhos()) {
//                if (tokensFilhos.getDeprel().equals("det") || tokensFilhos.getDeprel().equals("case")) {
//                    argumentoConcatenado.addFirst(tokensFilhos);
//                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
//                }
                if (tokensFilhos.getDeprel().equals("flat") /*|| t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                    Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1);

                    if (tokenAnterior.getDeprel().equals("punct") && !tokenAnterior.getForm().equals(",") && (vetorBooleanoTokensVisitados[tokenAnterior.getId()] == false)) {
                        argumentoConcatenado.addLast(tokenAnterior);
                        vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                    }
                    argumentoConcatenado.addLast(tokensFilhos);
                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                }
            }
            return argumentoConcatenado;
        }

//        if (token.getDeprel().equals("conjCC")) {
//            argumentoConcatenado.addLast(token);
//            vetorBooleanoTokensVisitados[token.getId()] = true;
//            for (Token tokensFilhos : token.getTokensFilhos()) {
//                if (tokensFilhos.getDeprel().equals("flat") /*|| t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
//                    argumentoConcatenado.addLast(tokensFilhos);
//                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
//                }
//            }
//            return argumentoConcatenado;
//        }
        /*Este pedaço do código foi adicionado pois o aclrelcl pode ter diversas variações e colocar tudo no 'if' seria ruim*/
        for (Token t : token.getTokensFilhos()) {
            if (t.getDeprel().equals("acl:relcl")) {
                argumentoConcatenado.addLast(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
                for (Token tokensFilhos : token.getTokensFilhos()) {
                    if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-")) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct") && !tokenAnterior.getForm().equals(",") && (vetorBooleanoTokensVisitados[tokenAnterior.getId()] == false) && pontuacaoValida(tokenAnterior)) {
                            argumentoConcatenado.addLast(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
//                System.out.println("acl:relcl " + argumentoConcatenado.getFirst().getForm());
                return argumentoConcatenado;
            }
        }

        /*Esse caso sem if foi colocado pra evitar situações que retorne null e travar o programa*/
        boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
        for (Token tokensFilhos : token.getTokensFilhos()) {
            if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                if (!(tokensFilhos.getDeprel().equals("nmod") || tokensFilhos.getDeprel().contains("xcomp") || tokensFilhos.getDeprel().equals("dobj") || tokensFilhos.getDeprel().equals("obj") || tokensFilhos.getDeprel().equals("acl:relcl") || tokensFilhos.getDeprel().equals("iobj") || tokensFilhos.getDeprel().equals("conj") || tokensFilhos.getDeprel().equals("acl:part") || tokensFilhos.getDeprel().equals("advmod") || tokensFilhos.getDeprel().equals("appos") || tokensFilhos.getDeprel().equals("amod") || tokensFilhos.getDeprel().equals("ccomp") || tokensFilhos.getDeprel().equals("advcl") || tokensFilhos.getDeprel().equals("dep") || tokensFilhos.getDeprel().equals("nsubj") || tokensFilhos.getPostag().equals("VERB"))) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                                argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                                vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
                            }
                        }
                    }
                    flag = false;
                }
            } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                if (tokensFilhos.getDeprel().equals("flat") || tokensFilhos.getForm().equals("-") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                    Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                    if (tokenAnterior.getDeprel().equals("punct") && pontuacaoValida(tokenAnterior)) {
                        argumentoConcatenado.addFirst(tokenAnterior);
                        vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                    }
                    argumentoConcatenado.addLast(tokensFilhos);
                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                }
            }
        }
        if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
            argumentoConcatenado.addFirst(token);
            vetorBooleanoTokensVisitados[token.getId()] = true;
            if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
                Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
                 pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
                    argumentoConcatenado.add(tokenPosterior);
                    vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
                }
            }
        }
        return argumentoConcatenado;

//        return null;
    }

    public Deque<Token> retornaTokenConcatenadoRelacao(Token token, Boolean[] vetorBooleanoTokensVisitados, SujeitoRelacao sujeitoRelacao) {
        Deque<Token> argumentoConcatenado = new ArrayDeque<Token>();

        if (sujeitoRelacao.getIndiceNucleoRelacao() == token.getId()) {//Verifica se o token recebido é o núcleo do head
            argumentoConcatenado.addLast(token);
            vetorBooleanoTokensVisitados[token.getId()] = true;
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("obj") || token.getDeprel().equals("aux:pass") || token.getDeprel().equals("iobj") || token.getDeprel().equals("advmod") || token.getDeprel().equals("cop") || token.getDeprel().equals("aux") || token.getDeprel().equals("dep") || token.getDeprel().equals("mark")) {
            for (int i = token.getId(); i < token.getHead(); i++) {
                argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("flat") || token.getDeprel().equals("acl:part") || token.getDeprel().equals("punct") || token.getDeprel().equals("expl:pv")) {
            argumentoConcatenado.addLast(token);
            vetorBooleanoTokensVisitados[token.getId()] = true;
            return argumentoConcatenado;
        }

//        if (token.getDeprel().equals("conj")) {
//            argumentoConcatenado.addLast(token);
//            vetorBooleanoTokensVisitados[token.getId()] = true;
//            for (Token tokensFilhos : token.getTokensFilhos()) {
//                if (tokensFilhos.getDeprel().equals("flat") /*|| t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
//                    argumentoConcatenado.addLast(tokensFilhos);
//                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
//                }
//            }
//            return argumentoConcatenado;
//        }
        return null;
    }

    /*Esta função verifica se argumentoVerboLigacao pontuação do token pode ser usada na busca do sujeito. Por exemplo, se for uma vírgula ou
     hífen, não entraria na busca, mas se for aspas ou parêntese já pode entrar*/
    public boolean pontuacaoValida(Token token) {
        if (token.getForm().equals("(") || token.getForm().equals(")") || token.getForm().equals("{") || token.getForm().equals("}") || token.getForm().equals("\"") || token.getForm().equals("'") || token.getForm().equals("[") || token.getForm().equals("]") || token.getForm().equals(",")) {
            return true;
        }
        return false;
    }

    /*Esta função é necessária pois nos casos que tem acl:relcl alguns sujeitos vêm com preposição*/
    public void eliminaPreposicaoAntesSujeito() {
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            SujeitoRelacao sr = sra.getSujeitoRelacao();
            for (Token t : sr.getSujeito()) {
                if ((t.getPostag().equals("ADP") && !(t.getDeprel().equals("mark") && t.getHead() == sr.getIndiceNucleoRelacao())) || t.getDeprel().equals("cc")) {
                    sr.getSujeito().pollFirst();
                } else {
                    break;
                }
            }
        }
    }

    /*Esta função é necessária pois tem casos em que existe um token antes do sujeito que não é filho dele e que é necessário para complementar o sentido*/
    public void adicionaArgumentoMarkAntesSujeito() {
        Deque<Token> argumentoConcatenado = new ArrayDeque<Token>();
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            SujeitoRelacao sr = sra.getSujeitoRelacao();
            Token tokenNucleoRelacao = this.sentence.getSentenca().get(sr.getIndiceNucleoRelacao());
            for (Token tokenFilho : tokenNucleoRelacao.getTokensFilhos()) {
//                System.out.println(tokenNucleoRelacao.getForm());
                if ((tokenFilho.getDeprel().equals("aux:pass") || (tokenFilho.getDeprel().equals("obj")) || (tokenFilho.getDeprel().equals("iobj")) || (tokenFilho.getDeprel().equals("advmod")) || (tokenFilho.getDeprel().equals("cop")) || tokenFilho.getDeprel().equals("aux") || tokenFilho.getDeprel().equals("expl:pv"))) {
                    break;
                }
                if (tokenFilho.getDeprel().equals("mark") && tokenFilho.getId() < sr.getIndiceNucleoSujeito() && sr.getIndiceNucleoSujeito() < sr.getIndiceNucleoRelacao() /*&& !tokenFilho.getForm().equals("que")*/) {
                    Boolean[] vetorBooleanoTokensVisitadosParaTras = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
                    Arrays.fill(vetorBooleanoTokensVisitadosParaTras, Boolean.FALSE);
                    Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
                    Token elementoPilha;//Vai ser o topo da pilha
                    Token tokenPilha;//recebe os filhos de "elementoPilha"
                    pilhaAuxiliar.push(tokenFilho);//Adiciona na pilha o token
                    argumentoConcatenado = retornaTokenConcatenadoSujeitoArgumentos(tokenFilho, vetorBooleanoTokensVisitadosParaTras);
                    while (argumentoConcatenado.size() != 0) {
                        sr.setTokenSujeitoInicioDeque(argumentoConcatenado.pollLast());
                    }
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitadosParaTras[tokenPilha.getId()]) {
                                if ((tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || tokenPilha.getDeprel().equals("appos") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj")) && tokenPilha.getId() < tokenPilha.getHead()) {
                                    pilhaAuxiliar.push(tokenPilha);
                                    argumentoConcatenado = retornaTokenConcatenadoSujeitoArgumentos(tokenPilha, vetorBooleanoTokensVisitadosParaTras);
                                    while (argumentoConcatenado.size() != 0) {
                                        sr.setTokenSujeitoInicioDeque(argumentoConcatenado.pollLast());
                                    }
                                    vetorBooleanoTokensVisitadosParaTras[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                        pilhaAuxiliar.pop();
                    }
                    break;
                }
            }
        }
    }

    /*Em alguns casos pode ser que argumentoVerboLigacao relação esteja dividida com alguns tokens entre vírgulas, então esta função serve para
     eliminar esses tokens entre vírgulas e ficar apenas argumentoVerboLigacao relação necessária.*/
    public void eliminaArgumentosEntreVirgulasRelacao() {
        int idTokenPrimeiraVirgula = 0;
        int idTokenSegundaVirgula = 0;
        boolean flag = true;
        for (SujeitoRelacao sr : this.sujeitoRelacao) {
            for (Token tokenFilho : sr.getRelacao()) {
                if (tokenFilho.getForm().equals(",")) {
                    if (flag) {
                        idTokenPrimeiraVirgula = tokenFilho.getId();
                        flag = false;
                    } else {
                        idTokenSegundaVirgula = tokenFilho.getId();
                        break;
                    }
                }
            }
            if (!flag && idTokenPrimeiraVirgula != 0 && idTokenSegundaVirgula != 0) {
                /*Este 'for' vai ser executado de novo por segurança, para garantir que não haverá apenas uma vírgula
                 e acabar removendo parte da relação que não devia*/
                for (Token t : sr.getRelacao()) {
                    if (t.getId() >= idTokenPrimeiraVirgula && t.getId() <= idTokenSegundaVirgula) {
                        sr.getRelacao().remove(t);
                    }
                }
            }
            flag = true;
        }
    }

    /*Esta função foi criada para verificar se o tokenPai possui outro token, além do que está sendo visitado,
     para ser visitado. Isso é necessário para que não tenha extrações faltando partes nos casos em que o nó folha
     é um amod ou advmod*/
    public boolean verificaFilhosTokenPai(Token tokenPai, Boolean[] vetorBooleanoTokensVisitados) {
        Token tokenPaiTokenTopoPilha = this.sentence.getSentenca().get(tokenPai.getHead());
        for (Token tokenFilho : tokenPaiTokenTopoPilha.getTokensFilhos()) {
            if (tokenFilho.getId() > tokenPai.getId() && tokenPai.getId() > tokenPaiTokenTopoPilha.getId()) {
                if (tokenFilho.getDeprel().equals("nmod") || tokenFilho.getDeprel().contains("xcomp") || tokenFilho.getDeprel().equals("dobj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("iobj") || tokenFilho.getDeprel().equals("conj") || tokenFilho.getDeprel().equals("acl:part") || tokenFilho.getDeprel().equals("nummod") || tokenFilho.getDeprel().equals("advmod") || tokenFilho.getDeprel().equals("appos") || tokenFilho.getDeprel().equals("amod") || tokenFilho.getDeprel().equals("ccomp") || tokenFilho.getDeprel().equals("advcl") || tokenFilho.getDeprel().equals("dep") || (tokenFilho.getDeprel().equals("punct") && pontuacaoValida(tokenFilho)) && (tokenFilho.getId() > tokenFilho.getHead())) {
                    if (!vetorBooleanoTokensVisitados[tokenFilho.getId()]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void adicionaArgumentosAntesDasRelacoesNasExtracoesEObjCompNaRelacao(SujeitoRelacaoArgumentos sra, ArrayList<Argumento> clausulasAntesRelacao) {
        if (clausulasAntesRelacao.size() > 0) {
            Argumento argumentoAux = retornaObjetosComplementos(sra);
            for (Argumento x : clausulasAntesRelacao) {
                Argumento a = argumentoAux.retornaClone();
                a.setIdentificadorModuloExtracao(1);
                for (Deque<Token> d : x.getClausulas()) {
                    a.setClausulaArrayClausulas(d);
                }
                sra.addArg2(a.retornaClone());
            }
        }
    }

    /*Esta função serve para retornar o conjunto de cláusulas que fazem parte do complemento e objetos*/
    public Argumento retornaObjetosComplementos(SujeitoRelacaoArgumentos sra) {
        Argumento argumentoObjetosComplementos = new Argumento();
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        SujeitoRelacao sr = sra.getSujeitoRelacao();
        boolean flag = true;
        ArrayList<Token> tokenNucleoRelacao = this.sentence.getSentenca().get(sr.getIndiceNucleoRelacao()).getTokensFilhos();
        for (Token tokenFilho : tokenNucleoRelacao) {
            if ((tokenFilho.getId() > sr.getIndiceNucleoRelacao())) {
                if ((tokenFilho.getDeprel().contains("xcomp") || tokenFilho.getDeprel().equals("dobj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("iobj")) && flag) {
                    flag = false;
                    pilhaAuxiliar.push(tokenFilho);//Adiciona na pilha o token
                    argumentoObjetosComplementos.setClausulaArrayClausulas(retornaTokenConcatenadoSujeitoArgumentos(tokenFilho, vetorBooleanoTokensVisitados));
                    while (!pilhaAuxiliar.empty()) {
                        elementoPilha = pilhaAuxiliar.peek();
                        for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                            tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                            if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                                if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().contains("xcomp") || tokenPilha.getDeprel().equals("dobj") || tokenPilha.getDeprel().equals("obj") || tokenPilha.getDeprel().equals("iobj") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || (tokenPilha.getDeprel().equals("appos") && tokenPilha.getPostag().equals("NUM")) || (tokenPilha.getDeprel().equals("conj")/* && !tokenPilha.getPostag().equals("VERB")*/) || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("acl:part") || tokenPilha.getDeprel().equals("dep") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha)) && (tokenPilha.getId() > tokenPilha.getHead())) {
                                    pilhaAuxiliar.push(tokenPilha);
                                    argumentoObjetosComplementos.setClausulaArrayClausulas(retornaTokenConcatenadoSujeitoArgumentos(tokenPilha, vetorBooleanoTokensVisitados));
                                    vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                    elementoPilha = new Token();
                                    elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                    i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                                }
                            }
                        }
                        pilhaAuxiliar.pop();
                    }
                } else if (tokenFilho.getDeprel().equals("nmod") || tokenFilho.getDeprel().equals("acl:relcl") || (tokenFilho.getDeprel().equals("conj")) || tokenFilho.getDeprel().equals("acl:part") || tokenFilho.getDeprel().equals("nummod") || (tokenFilho.getDeprel().equals("appos")) || tokenFilho.getDeprel().equals("amod") || tokenFilho.getDeprel().equals("ccomp") || tokenFilho.getDeprel().equals("advcl") || tokenFilho.getDeprel().equals("dep")) {
                    return argumentoObjetosComplementos;
                }
            }
        }
        return argumentoObjetosComplementos;
    }

    public void moduloExtracaoVerboLigacao() {
        SujeitoRelacaoArgumentos sraAux = new SujeitoRelacaoArgumentos();
        Argumento argumentoAux = new Argumento();
        boolean flag = false;
        Deque<Token> dequeTokens = new ArrayDeque<>();
        int tamanhoSrInicial = this.sujeitoRelacao.size();
        for (int i = 0; i < tamanhoSrInicial; i++) {
            /*OBS IMPORTANTE: Quando eu adiciono o SR no SRA na função extrai cláusula eu estou adicionando o clene. Então, quando
             eu faço o tratamento la e tiro uma parte da relação paa colocar no argumento não vai intererir no SR achado inicialmente.
             Logo, para extrair o verbo de ligação eu só preciso recorrer aos SRs armazenados e tratá-los, ja que estão intactos, ou seja,
             na forma original quando foi extraído pela função de extrai sujeito e relação.*/

            SujeitoRelacao sr = this.sujeitoRelacao.get(i).retornaClone();
            sr.setIdentificadorModuloExtracaoRelacao(5);
            SujeitoRelacaoArgumentos sra = new SujeitoRelacaoArgumentos();
            sra.setSujeitoRelacao(sr);
            sraAux = sra.retornaClone();
            sraAux.getSujeitoRelacao().getRelacao().clear();
            //Essa parte é responsável por separar os tokens da relação para os argumentos
            for (Token tokenRelacao : sra.getSujeitoRelacao().getRelacao()) {
                if (!tokenRelacao.getDeprel().equals("cop") && !flag) {
                    sraAux.getSujeitoRelacao().getRelacao().add(tokenRelacao);
                } else if (tokenRelacao.getDeprel().equals("cop") && !flag) {
                    flag = true;
                    sraAux.getSujeitoRelacao().getRelacao().add(tokenRelacao);
                    sraAux.getSujeitoRelacao().setIndiceNucleoRelacao(tokenRelacao.getHead());
                } else if (flag) {
                    if (!tokenRelacao.getPostag().equals("VERB")) {
                        dequeTokens.add(tokenRelacao);
                    }
                }
            }
            if (flag) {
                boolean flagIndicaPrimeiroArgumentoTemTokenConj = false;
                Argumento primeiroArgumento = new Argumento();
                argumentoAux.getClausulas().push(dequeTokens);
                ArrayList<Argumento> arrayArgumento = retornaArgumentoVerboLigacao(sraAux);
                if (arrayArgumento.size() > 0) {
                    primeiroArgumento = arrayArgumento.get(0);
                    /*Verifica se no argumento tem algum token conj*/
                    for (Deque<Token> dequeToken : primeiroArgumento.getClausulas()) {
                        for (Token token : dequeToken) {
                            if (token.getDeprel().equals("conj")) {
                                flagIndicaPrimeiroArgumentoTemTokenConj = true;
                                break;
                            }
                        }
                    }
                    if (!flagIndicaPrimeiroArgumentoTemTokenConj) {
                        Stack<Deque<Token>> clausulas = primeiroArgumento.getClausulas();
                        for (Deque<Token> deque : clausulas) {
                            argumentoAux.getClausulas().push(deque);
                        }
                        argumentoAux.setIdentificadorModuloExtracao(1);
                        sraAux.addArg2(argumentoAux.retornaClone());
                        arrayArgumento.remove(0);
                    } else {
                        argumentoAux.setIdentificadorModuloExtracao(1);
                        sraAux.addArg2(argumentoAux.retornaClone());
                    }
                } else {
                    argumentoAux.setIdentificadorModuloExtracao(1);
                    sraAux.addArg2(argumentoAux.retornaClone());
                }

                for (Argumento argumento : arrayArgumento) {
                    argumento.setIdentificadorModuloExtracao(1);
                    sraAux.addArg2(argumento.retornaClone());
                }

                if (argumentoAux.getClausulas().get(0).size() > 0) {/*Verifica se a primeira clausula não está vazia,
                     pois se ela tiver as outras também estarão, logo não haverá arg2*/

                    this.sujeitoRelacaoArgumentos.add(sraAux);
                    argumentoAux = new Argumento();
                }
            }
            dequeTokens = new ArrayDeque<>();
            flag = false;
        }
    }

    /*Esta função é importante para verificar os argumentos que vão ser acrescidos, caso necessáro, no arg2 nos casos de verbos de ligação*/
    public ArrayList<Argumento> retornaArgumentoVerboLigacao(SujeitoRelacaoArgumentos sra) {
        Argumento argumentoVerboLigacao = new Argumento();
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        SujeitoRelacao sr = sra.getSujeitoRelacao();
        ArrayList<Token> tokenNucleoRelacao = this.sentence.getSentenca().get(sr.getIndiceNucleoRelacao()).getTokensFilhos();
        boolean flagIndicaPrimeiroFilho = false;/*Se achar o primeiro filho do nó raiz a busca não precisa continuar*/

        ArrayList<Argumento> argumentosExtraidos = new ArrayList<>();
        for (Token tokenFilho : tokenNucleoRelacao) {
            if (!sra.getSujeitoRelacao().getVetorBooleanoTokensRelacaoVisitados()[tokenFilho.getId()]) {
                if ((tokenFilho.getDeprel().equals("nmod") || tokenFilho.getDeprel().contains("xcomp") || tokenFilho.getDeprel().equals("dobj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("iobj") || tokenFilho.getDeprel().equals("nummod") || tokenFilho.getDeprel().equals("advmod") || (tokenFilho.getDeprel().equals("appos")) || tokenFilho.getDeprel().equals("amod") || tokenFilho.getDeprel().equals("dep") && (tokenFilho.getId() > tokenFilho.getHead())) && !flagIndicaPrimeiroFilho) {
                    flagIndicaPrimeiroFilho = true;
                    pilhaAuxiliar.push(tokenFilho);//Adiciona na pilha o token
                    vetorBooleanoTokensVisitados[tokenFilho.getId()] = true;
//                    System.out.println(tokenFilho.getForm());
                    argumentoVerboLigacao.setClausulaArrayClausulas(retornaTokenConcatenadoSujeitoArgumentos(tokenFilho, vetorBooleanoTokensVisitados));
                } else {
                    if ((tokenFilho.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenFilho))) {
                        pilhaAuxiliar.push(tokenFilho);//Adiciona na pilha o token
                        vetorBooleanoTokensVisitados[tokenFilho.getId()] = true;
                        tokenFilho.setDeprel("conjCC");
                        argumentoVerboLigacao.setClausulaArrayClausulas(retornaTokenConcatenadoSujeitoArgumentos(tokenFilho, vetorBooleanoTokensVisitados));
                        tokenFilho.setDeprel("conj");
                    }
                }
            }
            while (!pilhaAuxiliar.empty()) {
                elementoPilha = pilhaAuxiliar.peek();
                for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                    tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                    if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                        if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().contains("xcomp") || tokenPilha.getDeprel().equals("dobj") || (tokenPilha.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenPilha)) || tokenPilha.getDeprel().equals("obj") || tokenPilha.getDeprel().equals("iobj") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || (tokenPilha.getDeprel().equals("appos")) || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") && (tokenPilha.getId() > tokenPilha.getHead())) {
                            if (!sra.getSujeitoRelacao().getVetorBooleanoTokensRelacaoVisitados()[tokenPilha.getId()]) {
                                pilhaAuxiliar.push(tokenPilha);
                                argumentoVerboLigacao.setClausulaArrayClausulas(retornaTokenConcatenadoSujeitoArgumentos(tokenPilha, vetorBooleanoTokensVisitados));
                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                            }
                        }
                    }
                }
                if (flagIndicaPrimeiroFilho) {
                    break;
                }
                pilhaAuxiliar.pop();
            }
            if (argumentoVerboLigacao.getClausulas().size() > 0) {
//                System.out.println("+: " + argumentoVerboLigacao.toString());
                argumentosExtraidos.add(argumentoVerboLigacao);
                argumentoVerboLigacao = new Argumento();
            }
        }
//        for (Argumento argumentosExtraido : argumentosExtraidos) {
//            System.out.println(argumentosExtraido.toString());
//        }
//        System.out.println("Tamanho do vetor: " + argumentosExtraidos.size());
//        System.out.println("Argumento: " + argumentoVerboLigacao.toString());
        return argumentosExtraidos;
    }

    public SujeitoRelacaoArgumentos separaParteTokensRelacaoEmArgumentos(SujeitoRelacaoArgumentos sra) {
        if (verificaEnumeracao(this.sentence.getSentenca().get(sra.getSujeitoRelacao().getIndiceNucleoRelacao()))) {
            boolean flag = false;
            Deque<Token> dequeTokens = new ArrayDeque<>();
            SujeitoRelacaoArgumentos sraAux = sra.retornaClone();
            sraAux.getArgumentos().clear();
            sraAux.getSujeitoRelacao().getRelacao().clear();
            for (Token tokenRelacao : sra.getSujeitoRelacao().getRelacao()) {
                if (!tokenRelacao.getDeprel().equals("cop") && !flag) {
                    sraAux.getSujeitoRelacao().getRelacao().add(tokenRelacao);
                } else if (tokenRelacao.getDeprel().equals("cop") && !flag) {
                    flag = true;
                    sraAux.getSujeitoRelacao().getRelacao().add(tokenRelacao);
//                sraAux.getSujeitoRelacao().setIndiceNucleoRelacao(tokenRelacao.getHead());
                } else if (flag) {
                    if (!tokenRelacao.getPostag().equals("VERB")) {
                        dequeTokens.add(tokenRelacao);
                    } else {
                        if (tokenRelacao.getPostag().equals("VERB") && tokenRelacao.getDeprel().equals("root")) {
                            dequeTokens.add(tokenRelacao);
                        }
                    }
                }
            }
            Argumento argumento = new Argumento();
            argumento.setClausulaArrayClausulas(dequeTokens);
            sraAux.addArg2(argumento);
            if (flag) {
                return sraAux;
            } else {
                return sra;
            }
        } else {
            return sra;
        }
//        if (dequeTokens.size() > 0) {
//            System.out.println("1 papapa");
//            System.out.println(sraAux.getSujeitoRelacao().getStringRelacao());
//            return dequeTokens;
//        }else{
//            return null;
//        }
    }

    /*Este módulo encontra o sujeito relação e chama outra função para extrair as cláusulas do argumento 2*/
    public void moduloExtracaoAposto() {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        Token verboSintetico = new Token();
        verboSintetico.setDeprel("cop");
        verboSintetico.setForm("é");
        verboSintetico.setPostag("VERB");
        verboSintetico.setCpostag("VERB");
        verboSintetico.setHead(0);
        for (Token tokenSentenca : this.sentence.getSentenca()) {
            if (tokenSentenca.getDeprel().equals("appos") && tokenSentenca.getCpostag().equals("PROPN") && !verificaFilhoConj(tokenSentenca)) {
                SujeitoRelacao sr = new SujeitoRelacao();
                this.sujeitoRelacao.add(sr);/*esse "sujeito relacao" é add null, para quando for concatenar os elementos
                 não dê erro relacionado à posição vazia*/

                Token tokenNucleoSujeitoAposto = this.sentence.getSentenca().get(tokenSentenca.getHead()).retornaClone();
                verboSintetico.setId(tokenNucleoSujeitoAposto.getId());//Por ser um token sintético estou adicionando o mesmo ID do núcleo do sujeito
                sr.setTokenRelacaoFinalDeque(verboSintetico);
                verboSintetico.addTokenFilho(tokenSentenca);
                sr.setIndiceNucleoSujeito(tokenNucleoSujeitoAposto.getId());
                sr.setIndiceNucleoRelacao(tokenNucleoSujeitoAposto.getId());
                pilhaAuxiliar.push(tokenNucleoSujeitoAposto);//Adiciona na pilha o token
                adicionaPedacoSujeitoRelacao(sr, tokenNucleoSujeitoAposto, vetorBooleanoTokensVisitados, 0);
                while (!pilhaAuxiliar.empty()) {
                    elementoPilha = pilhaAuxiliar.peek();
                    for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                        tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                        if (!vetorBooleanoTokensVisitados[tokenPilha.getId()]) {
                            if ((tokenPilha.getDeprel().equals("nummod") /*|| (tokenPilha.getDeprel().equals("appos") && !tokenPilha.getCpostag().equals("PROPN"))*/ || (((tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || tokenPilha.getDeprel().equals("obj"))) && (tokenPilha.getId() > tokenNucleoSujeitoAposto.getId())) || tokenPilha.getDeprel().equals("det") || tokenPilha.getDeprel().equals("case") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha))) && (tokenPilha.getId() < tokenSentenca.getId())) {
                                pilhaAuxiliar.push(tokenPilha);
                                adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 0);
                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                                elementoPilha = new Token();
                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                            }
//                            else if (tokenPilha.getDeprel().equals("conj") && !tokenPilha.getPostag().equals("VERB")) {
////                                indiceArraySujeitoRelacaoArgumentos = trataCasoEspecialSujeitoComConjuncao(pilhaAuxiliar, tokenPilha, indiceArraySujeitoRelacaoArgumentos, tokenSentenca.getId(), vetorBooleanoTokensVisitados);
//                                pilhaAuxiliar.push(tokenPilha);//System.out.println(tokenPilha.getForm());
//                                adicionaPedacoSujeitoRelacao(sr, tokenPilha, vetorBooleanoTokensVisitados, 0);
//                                vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
//                                elementoPilha = new Token();
//                                elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
//                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
//                            }
                        }
                    }
                    pilhaAuxiliar.pop();
                }
                Boolean[] vetorBooleanoTokensVisitadosCopia = new Boolean[sentence.getTamanhoSentenca()];
                System.arraycopy(vetorBooleanoTokensVisitados, 0, vetorBooleanoTokensVisitadosCopia, 0, vetorBooleanoTokensVisitados.length);
                sr.setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensVisitadosCopia);
                sr.setVetorBooleanoTokensRelacaoVisitados(vetorBooleanoTokensVisitadosCopia);
                sr.setIdentificadorModuloExtracaoSujeito(1);
                sr.setIdentificadorModuloExtracaoRelacao(3);
                extraiClausulasAposto(sr, tokenSentenca);
                Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
            }
        }
    }

    public void extraiClausulasAposto(SujeitoRelacao sr, Token nucleoAposto) {
        SujeitoRelacaoArgumentos sra = new SujeitoRelacaoArgumentos(sr);
        this.sujeitoRelacaoArgumentos.add(sra);
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<>();//Pilha usada para fazer argumentoVerboLigacao busca em profundidade
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        SujeitoRelacao itemSujeitoRelacao = sra.getSujeitoRelacao();
        Argumento argumento = new Argumento();
        /*Adiciona informações a respeito do tipo do módulo*/
        argumento.setIdentificadorModuloExtracao(3);
        /*-----------------------------------------------------------*/
        if (vetorBooleanoTokensVisitados.length > 0 && itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados().length > 0) {//Esse if é necessário, pois se argumentoVerboLigacao sentença não tiver sujeito o programa não da erro de vetor null
            vetorBooleanoTokensVisitados[nucleoAposto.getId()] = true;
            pilhaAuxiliar.push(nucleoAposto);//Adiciona na pilha o token
//            System.out.println("Push: " + nucleoAposto.getForm());
            adicionaPedacoArgumento(argumento, nucleoAposto, vetorBooleanoTokensVisitados);
            while (!pilhaAuxiliar.empty()) {
                elementoPilha = pilhaAuxiliar.peek();
                for (int i = 0; i < elementoPilha.getTokensFilhos().size(); i++) {
                    tokenPilha = elementoPilha.getTokensFilhos().get(i);//filho de elementoPilha
                    if (!vetorBooleanoTokensVisitados[tokenPilha.getId()] && !itemSujeitoRelacao.getVetorBooleanoTokensSujeitoVisitados()[tokenPilha.getId()]) {
                        if (tokenPilha.getDeprel().equals("nmod") || tokenPilha.getDeprel().contains("xcomp") || tokenPilha.getDeprel().equals("dobj") || tokenPilha.getDeprel().equals("obj") || tokenPilha.getDeprel().equals("iobj") || tokenPilha.getDeprel().equals("nummod") || tokenPilha.getDeprel().equals("advmod") || (tokenPilha.getDeprel().equals("appos") && tokenPilha.getPostag().equals("NUM")) || tokenPilha.getDeprel().equals("amod") || tokenPilha.getDeprel().equals("dep") || (tokenPilha.getDeprel().equals("punct") && pontuacaoValida(tokenPilha)) && (tokenPilha.getId() > tokenPilha.getHead())) {
                            pilhaAuxiliar.push(tokenPilha);
                            adicionaPedacoArgumento(argumento, tokenPilha, vetorBooleanoTokensVisitados);
//                            System.out.println("Push: " + tokenPilha.getForm());
                            vetorBooleanoTokensVisitados[tokenPilha.getId()] = true;
                            elementoPilha = new Token();
                            elementoPilha.setTokensFilhos(tokenPilha.getTokensFilhos());
                            i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar argumentoVerboLigacao variável, fazendo com que o valor dela fique zero (i = i+1)
                        }
                    }
                }
                pilhaAuxiliar.pop();
            }
            sra.addArg2(argumento);
            Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        }
    }

    /*Esta função é necessária para verificar se o pai/avo/bisavo... do token que está antes do sujeito é proveniente
     do núcleo da relação ou de um token após a relação. Se for de um token após a relação a extração (em outra função) é
     feita de forma normal. Caso contrário, o token será adicionado aos argumentos anteriores ao sujeito para posterior
     extração*/
    public boolean verificaTokenPaiAntesDepoisNucleoRelacao(Token tokenTopoPilha, SujeitoRelacaoArgumentos sra) {
        Token tokenPai = tokenTopoPilha;
        tokenPai = this.sentence.getSentenca().get(tokenPai.getHead());
        while ((tokenPai.getId() != sra.getSujeitoRelacao().getIndiceNucleoRelacao()) && (tokenPai.getId() != 0)) {
            if (tokenPai.getId() > sra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                return true;
            }
            tokenPai = this.sentence.getSentenca().get(tokenPai.getHead());
        }
        return false;
    }
    /*Função que verifica se token tem filho 'conj'. Ela vai ser útil para o aposto*/

    public boolean verificaFilhoConj(Token token) {
        for (Token t : token.getTokensFilhos()) {
            if (t.getDeprel().equals("conj")) {
                return true;
            }
        }
        return false;
    }

    /*Esta função verifica se o token é o primeiro filho (com id maior que seu pai)*/
    public boolean verificaAclPartPrimeiroFilhoRelacao(Token token) {
        Token tokenPai = this.sentence.getSentenca().get(token.getHead());
        for (Token tokenFilho : tokenPai.getTokensFilhos()) {
            if (tokenFilho.getId() > tokenPai.getId()) {
                if (tokenFilho.getDeprel().equals("nmod") || tokenFilho.getDeprel().contains("xcomp") || tokenFilho.getDeprel().equals("dobj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("iobj") || tokenFilho.getDeprel().equals("nummod") || tokenFilho.getDeprel().equals("advmod") || (tokenFilho.getDeprel().equals("appos")) || (tokenFilho.getDeprel().equals("conj")) || tokenFilho.getDeprel().equals("amod") || tokenFilho.getDeprel().equals("dep")) {
                    return false;
                } else if (tokenFilho.getDeprel().equals("acl:part")) {
                    return true;
                }
            }
        }
        return false;
    }
    /*Esta função substitui o aposto com nome próprio se tiver alguma correferência com o sujeito ou argumento*/

    public void moduloSubstituiApostoTransitividade(ArrayList<SujeitoRelacaoArgumentos> sra) {
        SujeitoRelacaoArgumentos sraSubstituindoAposto;
        ArrayList<SujeitoRelacaoArgumentos> sraComAposto = new ArrayList<>();
        /*Verifica as extrações com aposto*/
        for (SujeitoRelacaoArgumentos termosSra : sra) {
            if (termosSra.getSujeitoRelacao().getIndiceNucleoSujeito() == termosSra.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                sraComAposto.add(termosSra);
            }
        }
//        System.out.println("Tamanho array aposto: " + sraComAposto.size());
        for (SujeitoRelacaoArgumentos termosSra : sraComAposto) {
            /*Obtém o sujeito completo(com mark no começo se tiver) */
            String sujeitoAposto = termosSra.getSujeitoRelacao().getStringSujeitoCompleto();
            int tamanhoInicialSra = sra.size();
            for (int i = 0; i < tamanhoInicialSra; i++) {
                SujeitoRelacaoArgumentos termosSraSubstituicao = sra.get(i);
                /*Nas extrações com aposto é adicionada a cláusula sintética "é" que foi atribuido o mesmo núcleo da relação
                 no sujeito*/
                if (termosSraSubstituicao.getSujeitoRelacao().getIndiceNucleoSujeito() != termosSraSubstituicao.getSujeitoRelacao().getIndiceNucleoRelacao()) {
                    String sujeito = termosSraSubstituicao.getSujeitoRelacao().getStringSujeitoCompleto();
                    /*Verifica se o aposto é uma substring*/
                    boolean indiceSubstringAposto = sujeito.contains(sujeitoAposto);
                    /*Se tiver a substring então será feita a substituição do termo pelo aposto*/
                    if (indiceSubstringAposto) {
                        Deque<Token> arrayDequeSujeitoSubstituido = new ArrayDeque<>();
                        boolean flagIndicaSubstituicao = false;
                        /*Novo SRA que vai ser*/
                        sraSubstituindoAposto = termosSraSubstituicao.retornaClone();
                        for (Token tokensDeque : termosSraSubstituicao.getSujeitoRelacao().getSujeito()) {
                            if (verificaTokenArray(tokensDeque, termosSra.getSujeitoRelacao().getSujeito())) {
                                if (!flagIndicaSubstituicao) {
                                    for (Argumento arg2 : termosSra.getArgumentos()) {
                                        for (Deque<Token> dequeToken : arg2.getClausulas()) {
                                            if (dequeToken != null) {
                                                for (Token t : dequeToken) {
                                                    arrayDequeSujeitoSubstituido.add(t);
                                                }
                                            }
                                        }
                                    }
                                    flagIndicaSubstituicao = true;
                                }
                            } else {
                                arrayDequeSujeitoSubstituido.add(tokensDeque);
                            }
                        }
                        sraSubstituindoAposto.getSujeitoRelacao().setSujeito(arrayDequeSujeitoSubstituido);
                        sraSubstituindoAposto.getSujeitoRelacao().setIdentificadorModuloExtracaoSujeito(3);
                        this.sujeitoRelacaoArgumentos.add(sraSubstituindoAposto);
                    }
                }
            }
        }

    }

    /*Função que pode ser útil para verificar se antes de um token tem uma preposicao. Se tiver o algoritmo que quebras as
     conjunções verifica se pode ou não adicionar esse deque de conjunções na cláusula para o Arg não ficar sem uma conexão.
     Ex: A doação [dessas] roupas. Se não verificar que tem o "dessas" para adicionar no token núcleo da outra frase
     coordenada a extração fica estranha.*/
    public Deque<Token> retornaApenasConjuncoesAterioresAoToken(Token token) {
        Deque<Token> dequeConjuncoes = new ArrayDeque<>();
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Deque<Token> clausula = retornaTokenConcatenadoSujeitoArgumentos(token, vetorBooleanoTokensVisitados);
        for (Token t : clausula) {
            if (t.getCpostag().equals("ADP")) {
                dequeConjuncoes.add(t);
            } else {
                break;
            }
        }
        return dequeConjuncoes;
    }
    /*Esta função é util para verificar se tem uma preposição antes de um token. Se tiver o algoritmo decide se
     vai adicionar ou não outra preposição antes desse token. Esta função é utilizada em conjunto com o módulo
     de extração de conjunções coordenadas*/

    public boolean verificaPreposicaoAntesToken(Token token) {
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.getTamanhoSentenca()];
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Deque<Token> clausula = retornaTokenConcatenadoSujeitoArgumentos(token, vetorBooleanoTokensVisitados);
        for (Token t : clausula) {
            if (t.getCpostag().equals("ADP")) {
                return true;
            }
        }
        return false;
    }

    /*Esta função serve para verificar se um determinado token se encontra em um array de token*/
    public boolean verificaTokenArray(Token tokenASerVerificado, Deque<Token> dequeTokens) {
        for (Token t : dequeTokens) {
            if (t.getId() == tokenASerVerificado.getId()) {
                return true;
            }
        }
        return false;
    }

    /*Esta função verifica se o token passado no parâmetro tem um filho que seja sujeito. Essa função vai ser
     muito útil quando tiver 'ccomp' ou 'advcl' na sentença*/
    public boolean verificaTokenFilhoSujeito(Token token) {
        for (Token tokenFilho : token.getTokensFilhos()) {
            if (tokenFilho.getDeprel().contains("subj")) {
                return true;
            }
        }
        return false;
    }

    /*essa função faz o apontamento dos termos que tem filho ccomp e advcl com sujeito. Deve-se ter cuidado para
     não excluir o sra*/
    public void realizaApontamentoArgumento(Argumento argumentoSraPai, Token tokenCcompAdvcl) {
        int indiceTokenNucleoSubSujeito = 0;
        int indiceTokenNucleoSubRelacao = tokenCcompAdvcl.getId();
        for (Token t : tokenCcompAdvcl.getTokensFilhos()) {
            if (t.getDeprel().contains("nsubj")) {
                indiceTokenNucleoSubSujeito = t.getId();
                break;
            }
        }
        for (SujeitoRelacaoArgumentos subSra : this.sujeitoRelacaoArgumentos) {
            if ((subSra.getSujeitoRelacao().getIndiceNucleoRelacao() == indiceTokenNucleoSubRelacao) && (subSra.getSujeitoRelacao().getIndiceNucleoSujeito() == indiceTokenNucleoSubSujeito)) {
                argumentoSraPai.setSraApontamentoCcompAdvcl(subSra);
                break;
            }
        }
    }

    /*Função que utiliza algumas heurísticas para veriricar se um token deve ser o núcleo de uma relação ou não*/
    public boolean heuristicasVerificaConjRelacao(Token tokenConj) {
        int idPrimeiroTokenFilhoConjIdMenorTokenConj = 0;
        if (tokenConj.getPostag().equals("VERB")) {
            for (Token tokenFilhoConj : tokenConj.getTokensFilhos()) {
                if ((idPrimeiroTokenFilhoConjIdMenorTokenConj == 0) && (tokenFilhoConj.getId() < tokenConj.getId()) && (!tokenFilhoConj.getDeprel().equals("punct"))) {
                    idPrimeiroTokenFilhoConjIdMenorTokenConj = tokenFilhoConj.getId();
                }
                if (tokenFilhoConj.getDeprel().equals("cc")) {
                    if (!(tokenFilhoConj.getForm().equals("e") || tokenFilhoConj.getForm().equals("ou") || tokenFilhoConj.getForm().equals(","))) {
                        return false;
                    }
                }
            }
            if (idPrimeiroTokenFilhoConjIdMenorTokenConj != 0) {
                for (int i = idPrimeiroTokenFilhoConjIdMenorTokenConj + 1; i < tokenConj.getId(); i++) {
                    Token tokenEntrePrimeiroFilhoConjEConj = this.sentence.getSentenca().get(i);
                    if (!(tokenEntrePrimeiroFilhoConjEConj.getPostag().contains("ADP") || tokenEntrePrimeiroFilhoConjEConj.getPostag().equals("DET") || tokenEntrePrimeiroFilhoConjEConj.getDeprel().equals("punct") || tokenEntrePrimeiroFilhoConjEConj.getDeprel().equals("mark") || tokenEntrePrimeiroFilhoConjEConj.getDeprel().equals("advmod") || tokenEntrePrimeiroFilhoConjEConj.getDeprel().contains("aux") || tokenEntrePrimeiroFilhoConjEConj.getDeprel().equals("cop") || tokenEntrePrimeiroFilhoConjEConj.getDeprel().equals("expl:pv"))) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /*Nesse treebank relações com conjunções do tipo "faz e distribui" o primeiro token "faz" não tem filhos dependentes, pois
     os filhos que deveria ser dele estão ligados somente no token "distribui". Então essa função é utilizada para verificar
     se o primeiro token tem outros filhos. Se tiver em outro lugar o id do núcleo será alterado.*/
    public boolean setNovoIndiceNucleoRelacao(Token tokenRelacao, SujeitoRelacao sr, Boolean[] vetorBooleanoTokensVisitados) {
//        System.out.println("Token: " + tokenRelacao.getForm());
        boolean flagIndicaPaiSujeito = false;
        ArrayList<Token> arrayTokensConjEPaiSujeito = new ArrayList<>();
        Token tokenTeste;
        for (Token tokenFilho : tokenRelacao.getTokensFilhos()) {
            if (tokenFilho.getDeprel().contains("nsubj")) {
                flagIndicaPaiSujeito = true;
            }
            if (tokenFilho.getDeprel().equals("nmod") || tokenFilho.getDeprel().contains("xcomp") || tokenFilho.getDeprel().equals("dobj") || tokenFilho.getDeprel().equals("obj") || tokenFilho.getDeprel().equals("iobj") || tokenFilho.getDeprel().equals("acl:relcl") || tokenFilho.getDeprel().equals("nummod") || tokenFilho.getDeprel().equals("advmod") || (tokenFilho.getDeprel().equals("appos") && !tokenFilho.getCpostag().equals("PROPN")) || tokenFilho.getDeprel().equals("amod") || (tokenFilho.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenFilho)) || (tokenFilho.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenFilho)) || tokenFilho.getDeprel().equals("acl:part") || tokenFilho.getDeprel().equals("dep") || (tokenFilho.getDeprel().equals("punct") && pontuacaoValida(tokenFilho)) && (tokenFilho.getId() > tokenFilho.getHead())) {
                return false;
            }
        }
//        System.out.println("------");
        if (flagIndicaPaiSujeito) {
            tokenTeste = tokenRelacao;
        } else {
            tokenTeste = this.sentence.getSentenca().get(tokenRelacao.getHead());
        }

        for (Token tokenFilho : tokenTeste.getTokensFilhos()) {
            if (tokenFilho.getId() != tokenRelacao.getId()) {
                if (tokenFilho.getDeprel().equals("conj") && heuristicasVerificaConjRelacao(tokenFilho)) {
                    arrayTokensConjEPaiSujeito.add(tokenFilho);
                }
            }
        }
        for (Token tokenRelacaoEncontrado : arrayTokensConjEPaiSujeito) {
            if (tokenRelacaoEncontrado.getId() != tokenRelacao.getId()) {
                for (Token tokenFilho : tokenRelacaoEncontrado.getTokensFilhos()) {
                    if (!vetorBooleanoTokensVisitados[tokenFilho.getId()] && !sr.getVetorBooleanoTokensSujeitoVisitados()[tokenFilho.getId()]) {
                        if (tokenFilho.getDeprel().equals("nmod")
                                || tokenFilho.getDeprel().contains("xcomp")
                                || tokenFilho.getDeprel().equals("dobj")
                                || tokenFilho.getDeprel().equals("obj")
                                || tokenFilho.getDeprel().equals("iobj")
                                || tokenFilho.getDeprel().equals("acl:relcl")
                                || tokenFilho.getDeprel().equals("nummod")
                                || tokenFilho.getDeprel().equals("advmod")
                                || (tokenFilho.getDeprel().equals("appos") && !tokenFilho.getCpostag().equals("PROPN"))
                                || tokenFilho.getDeprel().equals("amod")
                                    || (tokenFilho.getDeprel().equals("ccomp") && !verificaTokenFilhoSujeito(tokenFilho))
                                || (tokenFilho.getDeprel().equals("advcl") && !verificaTokenFilhoSujeito(tokenFilho))
                                || tokenFilho.getDeprel().equals("acl:part")
                                || tokenFilho.getDeprel().equals("dep")
                                || (tokenFilho.getDeprel().equals("punct") && pontuacaoValida(tokenFilho))
                                && (tokenFilho.getId() > tokenFilho.getHead())
                        ) {
                            sr.setIndiceNucleoRelacao(tokenRelacaoEncontrado.getId());
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public String retornaExtracaoComArgumentoEspecifico(SujeitoRelacaoArgumentos sra, Argumento arg, int codigo) {
        /*codigo = 0 sujeito com mark
         codigo = 1 sujeito sem mark*/
        String extracao = "";
        if (codigo == 1) {
            extracao = sra.getSujeitoRelacao().getStringSujeitoSemMarkInicio();
            extracao = extracao + " ||| ";
            extracao = extracao + sra.getSujeitoRelacao().getStringRelacao();
            extracao = extracao + " ||| ";
            extracao = extracao + arg.toString();
        } else {
            extracao = sra.getSujeitoRelacao().getStringSujeitoCompleto();
            extracao = extracao + " ||| ";
            extracao = extracao + sra.getSujeitoRelacao().getStringRelacao();
            extracao = extracao + " ||| ";
            extracao = extracao + arg.toString();
        }
        return extracao;
    }

    /*Essa função serve para coletar o 'mark' no início do sujeito. Ela vai ser interessante no momento que
     tiver subextrações*/
    public String retornaStringMarkSubExtracao(Deque<Token> sujeito) {
        String mark = "";
        Token tokenMark = new Token();
        for (Token t : sujeito) {
            if (t.getDeprel().equals("mark")) {
                mark = mark + t.getForm() + " ";
                tokenMark = t;
            } else if (t.getDeprel().equals("fixed") && t.getHead() == tokenMark.getId()) {
                mark = mark + t.getForm() + " ";
            } else {
                return mark;
            }
        }
        return mark;
    }

    public void removeHifenFinalRelacaoEAdicionaInicioArgumento(ArrayList<SujeitoRelacaoArgumentos> arraySra) {
        for (SujeitoRelacaoArgumentos sra : arraySra) {
            Token ultimoTokenRelacao = sra.getSujeitoRelacao().getRelacao().getLast();
            if (ultimoTokenRelacao.getForm().equals("-")) {
                sra.getSujeitoRelacao().getRelacao().pollLast();
                for (Argumento argumento : sra.getArgumentos()) {
                    if (!argumento.getClausulas().isEmpty()) {
                        Token primeiroTokenArgumento = argumento.getClausulas().get(0).getFirst();
                        if (ultimoTokenRelacao.getId() == (primeiroTokenArgumento.getId() - 1)) {
                            argumento.getClausulas().get(0).addFirst(ultimoTokenRelacao);//Adiciona o hífen no argumento
                        }
                    }
                }
            }
        }
    }

    public boolean verificaEnumeracao(Token tokenRaizBuscaEnumeracao) {
        for (Token tokenFilho : tokenRaizBuscaEnumeracao.getTokensFilhos()) {
            if (tokenFilho.getId() > tokenFilho.getHead()) {
                if ((tokenFilho.getDeprel().equals("conj") && !heuristicasVerificaConjRelacao(tokenFilho))) {
                    /*verifica apenas o primeiro filho com ID maior.*/
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /*Essa função exclui os sujeitos que os núcleos são pronomes "que" ou artigos*/
    public void excluiSraSujeitoErrado() {
        int tamanhoArraySra = this.sujeitoRelacaoArgumentos.size();
        for (int i = 0; i < tamanhoArraySra; i++) {
            SujeitoRelacaoArgumentos sra = this.sujeitoRelacaoArgumentos.get(i);
            int indiceSujeito = sra.getSujeitoRelacao().getIndiceNucleoSujeito();
            /*Essa parte foi necessária para verificar os casos de acl:relcl que o núcleo continua sendo 'que', embora ele seja trocado*/
            Token sujeito = this.sentence.getSentenca().get(sra.getSujeitoRelacao().getIndiceNucleoSujeito());
            Token paiSujeito = this.sentence.getSentenca().get(sujeito.getHead());
            if (paiSujeito.getDeprel().equals("acl:relcl") && sujeito.getPostag().equals("PRON")) {
                indiceSujeito = paiSujeito.getHead();
            }
            /*----------------------------------------------*/
            if (this.sentence.getSentenca().get(indiceSujeito).getForm().equals("que") || this.sentence.getSentenca().get(indiceSujeito).getForm().equals("a") || this.sentence.getSentenca().get(indiceSujeito).getForm().equals("o")) {
                this.sujeitoRelacaoArgumentos.remove(i);
                tamanhoArraySra--;
                i = -1;
            }
        }
    }

    public void excluiSraRelacaoSemVerbo() {
        int tamanhoArraySra = this.sujeitoRelacaoArgumentos.size();
        boolean flagIndicaRelacaoSemVerbo = false;
        for (int i = 0; i < tamanhoArraySra; i++) {
            SujeitoRelacaoArgumentos sra = this.sujeitoRelacaoArgumentos.get(i);
            for (Token tokenRelacao : sra.getSujeitoRelacao().getRelacao()) {
                if (tokenRelacao.getCpostag() != null && tokenRelacao.getCpostag().equals("VERB")) {
                    flagIndicaRelacaoSemVerbo = true;
                }
            }
            if (!flagIndicaRelacaoSemVerbo) {
                this.sujeitoRelacaoArgumentos.remove(i);
                tamanhoArraySra--;
                i = -1;
            }
            flagIndicaRelacaoSemVerbo = false;
        }
    }

    public void eliminaTokenPontuacaoErrada() {
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            Deque<Token> sujeito = sra.getSujeitoRelacao().getSujeito();
            Deque<Token> relacao = sra.getSujeitoRelacao().getRelacao();

            if (sujeito.getFirst().getForm().equals(",") || sujeito.getFirst().getForm().equals(")") || sujeito.getFirst().getForm().equals("]") || sujeito.getFirst().getForm().equals("}")) {
                sujeito.pollFirst();
            }
            if (sujeito.getLast().getForm().equals(",") || sujeito.getLast().getForm().equals("(") || sujeito.getLast().getForm().equals("[") || sujeito.getLast().getForm().equals("{")) {
                sujeito.pollLast();
            }
            if (relacao.getFirst().getForm().equals(",") || relacao.getFirst().getForm().equals(")") || relacao.getFirst().getForm().equals("]") || relacao.getFirst().getForm().equals("}")) {
                relacao.pollFirst();
            }
            if (relacao.getLast().getForm().equals(",") || relacao.getLast().getForm().equals("(") || relacao.getLast().getForm().equals("[") || relacao.getLast().getForm().equals("{")) {
                relacao.pollLast();
            }

            for (Argumento arg : sra.getArgumentos()) {
                int qtdClausulas = arg.getClausulas().size();
                if (qtdClausulas > 0) {
                    if (!arg.getClausulas().get(0).isEmpty()) {
                        if (arg.getClausulas().get(0).getFirst().getForm().equals(",") || arg.getClausulas().get(0).getFirst().getForm().equals(")") || arg.getClausulas().get(0).getFirst().getForm().equals("]") || arg.getClausulas().get(0).getFirst().getForm().equals("}")) {
                            arg.getClausulas().get(0).pollFirst();
                        }
                    }
                    if (!arg.getClausulas().get(qtdClausulas - 1).isEmpty()) {
                        if (arg.getClausulas().get(qtdClausulas - 1).getLast().getForm().equals(",") || arg.getClausulas().get(qtdClausulas - 1).getLast().getForm().equals("(") || arg.getClausulas().get(qtdClausulas - 1).getLast().getForm().equals("[") || arg.getClausulas().get(qtdClausulas - 1).getLast().getForm().equals("{")) {
                            arg.getClausulas().get(qtdClausulas - 1).pollLast();
                        }
                    }
                }
            }
        }
    }

    /*Na relação não pode ter nomes próprios, então uso essa função para tirar os nomes próprios da relação e colocar no argumento*/
    public void deslocaClausulasNomesPropriosDaRelacaoParaArgumentos() {
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            boolean flagIndicaNomeProprio = false;
            Deque<Token> dequeNomesProprios = new ArrayDeque<>();
            Deque<Token> relacao = sra.getSujeitoRelacao().getRelacao();
            int nucleoRelacao = sra.getSujeitoRelacao().getIndiceNucleoRelacao();
            if (relacao.size() > 0) {
                for (Token tokenRelacao : relacao) {
                    if ((tokenRelacao.getCpostag().equals("PROPN") && tokenRelacao.getId() == nucleoRelacao) || flagIndicaNomeProprio) {
                        dequeNomesProprios.addLast(tokenRelacao);
                        relacao.removeFirstOccurrence(tokenRelacao);
                        flagIndicaNomeProprio = true;
                    }
                }
                if (dequeNomesProprios.size() > 0) {
                    int indiceLoop = 0;
                    for (Argumento arg : sra.getArgumentos()) {
                        Argumento novoArgumento = new Argumento();
                        novoArgumento.setClausulaArrayClausulas(dequeNomesProprios);
                        for (Deque<Token> clausulaArgumentoSra : arg.getClausulas()) {
                            novoArgumento.setClausulaArrayClausulas(clausulaArgumentoSra);
                        }
                        novoArgumento.setSraApontamentoCcompAdvcl(sra.getArgumentos().get(indiceLoop).getSraApontamentoCcompAdvcl());
                        novoArgumento.setIdentificadorModuloExtracao(sra.getArgumentos().get(indiceLoop).getIdentificadorModuloExtracao());
                        sra.getArgumentos().get(indiceLoop).setClausulas(novoArgumento.retornaClone().getClausulas());
                        indiceLoop++;
                    }
                }
            }
        }
    }

    public void excluiExtracoesRepetidas() {
        for (SujeitoRelacaoArgumentos sra1 : this.sujeitoRelacaoArgumentos) {
            for (SujeitoRelacaoArgumentos sra2 : this.sujeitoRelacaoArgumentos) {
                if (sra1.getSujeitoRelacao().comparaConteudoSujetoRelacao(sra2.getSujeitoRelacao())) {
                    int tamanhoArrayArg1 = sra1.getArgumentos().size();
                    int tamanhoArrayArg2 = sra2.getArgumentos().size();
                    for (int j = 0; j < tamanhoArrayArg1; j++) {
                        Argumento arg1 = sra1.getArgumentos().get(j);
                        for (int i = 0; i < tamanhoArrayArg2; i++) {
                            Argumento arg2 = sra2.getArgumentos().get(i);
                            if (!arg1.equals(arg2)) {
                                if (!arg1.equals(arg2)) {
                                    if (arg1.comparaConteudoArgumentos(arg2)) {
                                        sra2.getArgumentos().remove(i);
                                        i = -1;
                                        tamanhoArrayArg2--;
                                        if (sra1.equals(sra2)) {
                                            j = -1;
                                            tamanhoArrayArg1--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Deque<Token> retornaTokenConcatenadoArgumentos(Token token, Boolean[] vetorBooleanoTokensVisitados) {
        Deque<Token> argumentoConcatenado = new ArrayDeque<Token>();
//        System.out.println(token.getForm());
        if (token.getDeprel().equals("nmod") || token.getDeprel().equals("nummod") || token.getDeprel().equals("advmod") || token.getDeprel().equals("appos") || token.getDeprel().equals("amod") || token.getDeprel().equals("obj") || token.getDeprel().equals("dep")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                                argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                                vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
                            }
                        }
                    }
                    flag = false;
                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                    if (tokensFilhos.getDeprel().equals("flat") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct") && !tokenAnterior.getForm().equals(",")) {
                            argumentoConcatenado.addFirst(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                    if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
                        argumentoConcatenado.add(tokenPosterior);
                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
                    }
                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("conj")) {
            boolean flag = true; //flag que vai indica se o argumento do deprel tem filho ou nao
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                    for (int i = tokensFilhos.getId(); i <= tokensFilhos.getHead(); i++) {
                        if (flag) {
//                            if (!(this.sentence.getSentenca().get(i).getForm().equals(","))) {
                            argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                            vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
//                            }
                        }
                    }
                    flag = false;
                } else if (tokensFilhos.getId() > tokensFilhos.getHead()) {
                    if (tokensFilhos.getDeprel().equals("flat") /*|| (tokensFilhos.getDeprel().equals("punct") && !(tokensFilhos.getForm().equals(",")) || t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                        Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1); /*esse token vai ser usado para verificar se é uma pontuação,
                         pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                        if (tokenAnterior.getDeprel().equals("punct")) {
                            argumentoConcatenado.addFirst(tokenAnterior);
                            vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                        }
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            if (flag) {//se o argumento do deprel nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                argumentoConcatenado.addFirst(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
                if (token.getId() != this.sentence.getSentenca().size() - 1) {//Verifica se é o último token para não dar 'exception' de null
                    Token tokenPosterior = this.sentence.getSentenca().get(token.getId() + 1); /*esse token vai ser usado para verificar se é uma pontuação,
                     pois quando tem um token sozinho argumentoVerboLigacao função não verifica se depois do token tem algum ponto*/

                    if (tokenPosterior.getDeprel().equals("punct") && !tokenPosterior.getForm().equals(",")) {
                        argumentoConcatenado.add(tokenPosterior);
                        vetorBooleanoTokensVisitados[tokenPosterior.getId()] = true;
                    }
                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("conjCC")) {
            argumentoConcatenado.addLast(token);
            vetorBooleanoTokensVisitados[token.getId()] = true;
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (tokensFilhos.getDeprel().equals("flat") /*|| t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                    argumentoConcatenado.addLast(tokensFilhos);
                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("mark")) {
            argumentoConcatenado.addLast(token);
            for (Token tokensFilhos : token.getTokensFilhos()) {
                if (!(tokensFilhos.getDeprel().equals("nmod") || tokensFilhos.getDeprel().equals("nummod") || tokensFilhos.getDeprel().equals("advmod") || tokensFilhos.getDeprel().equals("appos") || tokensFilhos.getDeprel().equals("amod") || tokensFilhos.getDeprel().equals("obj") || tokensFilhos.getDeprel().equals("dep"))) {
                    if (tokensFilhos.getId() < tokensFilhos.getHead()) {
                        argumentoConcatenado.addFirst(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    } else {
                        argumentoConcatenado.addLast(tokensFilhos);
                        vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                    }
                }
            }
            return argumentoConcatenado;
        }

        if (token.getDeprel().equals("punct")) {
            if (token.getId() < token.getHead()) {
                for (int i = token.getId(); i < token.getHead(); i++) {
                    argumentoConcatenado.addLast(this.sentence.getSentenca().get(i));
                    vetorBooleanoTokensVisitados[this.sentence.getSentenca().get(i).getId()] = true;
                }
                return argumentoConcatenado;
            } else {
                argumentoConcatenado.add(token);
                vetorBooleanoTokensVisitados[token.getId()] = true;
                return argumentoConcatenado;
            }

        }

        if (token.getDeprel().equals("nsubj") || token.getDeprel().equals("nsubj:pass")) {
            argumentoConcatenado.addLast(token);
            vetorBooleanoTokensVisitados[token.getId()] = true;
            for (Token tokensFilhos : token.getTokensFilhos()) {
//                if (tokensFilhos.getDeprel().equals("det") || tokensFilhos.getDeprel().equals("case")) {
//                    argumentoConcatenado.addFirst(tokensFilhos);
//                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
//                }
                if (tokensFilhos.getDeprel().equals("flat") /*|| t.getDeprel().equals("amod") || t.getDeprel().equals("appos")*/) {
                    Token tokenAnterior = this.sentence.getSentenca().get(tokensFilhos.getId() - 1);

                    if (tokenAnterior.getDeprel().equals("punct") && !tokenAnterior.getForm().equals(",") && (vetorBooleanoTokensVisitados[tokenAnterior.getId()] == false)) {
                        argumentoConcatenado.addLast(tokenAnterior);
                        vetorBooleanoTokensVisitados[tokenAnterior.getId()] = true;
                    }
                    argumentoConcatenado.addLast(tokensFilhos);
                    vetorBooleanoTokensVisitados[tokensFilhos.getId()] = true;
                }
            }
            return argumentoConcatenado;
        }

        return null;
    }

    public void imprimeNucleoRelacao() {
        System.out.println("--------------------------------------------------");
        for (SujeitoRelacao x : this.sujeitoRelacao) {
            System.out.println(x.getIndiceNucleoRelacao());
        }
        System.out.println("--------------------------------------------------");
    }

    public void imprimeSujeito() {
        System.out.println("--------------------------------------------------");
        for (SujeitoRelacaoArgumentos x : this.sujeitoRelacaoArgumentos) {
            System.out.println("O sujeito é: " + x.getSujeitoRelacao().getStringSujeitoCompleto() + " A quantidade de argumentos é:" + x.getArgumentos().size());
        }
        System.out.println("--------------------------------------------------");
    }

    public void imprimeExtracoes() {
        System.out.println("--------------------------------------------------");
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
            for (Argumento arg2 : sra.getArgumentos()) {
                if (arg2.getSraApontamentoCcompAdvcl() == null) {
                    System.out.println(retornaExtracaoComArgumentoEspecifico(sra, arg2, 1));
                }
            }
        }
        ArrayList<String> arrayStringExtracoes = new ArrayList<>();
        for (SujeitoRelacaoArgumentos sra : this.sujeitoRelacaoArgumentos) {
//            sra = this.sujeitoRelacaoArgumentos.get(i);
            for (Argumento arg2 : sra.getArgumentos()) {
                arrayStringExtracoes.clear();
                if (arg2.getSraApontamentoCcompAdvcl() != null) {
                    /*Array que armazena as extrações e subextrações para imprimir posteriormente. O primeiro elemento sempre
                     será a extração principal*/
                    arrayStringExtracoes.add(retornaExtracaoComArgumentoEspecifico(sra, arg2, 1) + retornaStringMarkSubExtracao(arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getSujeito()));

                    for (Argumento argApontado : arg2.getSraApontamentoCcompAdvcl().getArgumentos()) {
                        for (String elementoArrayString : arrayStringExtracoes) {
                            System.out.println(elementoArrayString);
                        }
                        System.out.println("|");
                        System.out.println("v");
                        System.out.println(retornaExtracaoComArgumentoEspecifico(arg2.getSraApontamentoCcompAdvcl(), argApontado, 1));
                        if (argApontado.getSraApontamentoCcompAdvcl() != null) {
                            sra = arg2.getSraApontamentoCcompAdvcl();
                        }
                    }

                    if (arg2.getSraApontamentoCcompAdvcl().getArgumentos().isEmpty()) {
                        for (String elementoArrayString : arrayStringExtracoes) {
                            System.out.println(elementoArrayString);
                        }
                        System.out.println("|");
                        System.out.println("v");
                        /*
                         Tem casos que podem haver extração e o array de argumentos da subextração do SRA está vazio e a 
                         extração não ocorre. Então foi adicionado um array vazio nesses casos para que a extração seja realizada.
                         Ex: a sentença: Leo disse que ele ia fazer
                         a relação da subextração é "ia fazer" e ela não tem o arg2. Com esse trecho do código a extração
                         é possível.
                         */
                        Argumento argVazio = new Argumento();
                        System.out.println(retornaExtracaoComArgumentoEspecifico(arg2.getSraApontamentoCcompAdvcl(), argVazio, 1));
                    }
                }
            }
        }
        System.out.println("--------------------------------------------------");
    }

    public Sentence getSentence() {
        return sentence;
    }

    public ArrayList<SujeitoRelacao> getSujeitoRelacao() {
        return sujeitoRelacao;
    }

    public ArrayList<SujeitoRelacaoArgumentos> getSujeitoRelacaoArgumentos() {
        return sujeitoRelacaoArgumentos;
    }

}
