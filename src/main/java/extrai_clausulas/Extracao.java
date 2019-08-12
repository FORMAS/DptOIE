package extrai_clausulas;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
//LEMBRAR DE FAZER O MAPEAMENTO DOS NUMEROS DAS CLAUSULAS USANDO UM VETOR DE INDICES,
public class Extracao {
    protected Sentence sentence;
    protected ArrayList<Sujeito_Head> sv;
    protected ArrayList<ClausulaExtraida> clausulasExtraidas;
    protected ArrayList<Tripla> triplas;
    public Extracao(Sentence s) {
        this.sentence = s;
        sv = new ArrayList<>(10);
        clausulasExtraidas = new ArrayList<>(10);
        triplas = new ArrayList<>(5);
    }
    public void detectaSujeitoVerbo() {
        Sujeito_Head sujeitoHeadAux;
        int indiceHeadSujeito;
        int indiceSujeito;
        Stack<Token> pilhaAuxiliar = new Stack<Token>();
        Token elementoPilha = new Token();
        String auxPreSujeito = "";//Palavras que podem vir antes do sujeito
        String sujeitoBruto = "";//sujeito sem seus complementos
        String auxPosSujeito = "";//Palavras que podem vir depois do sujeito
        String auxPreHead = "";
        String auxHead = "";
        for (Token tokenSentenca : this.sentence.sentenca) {
            if ((tokenSentenca.deprel.equals("nsubj") || (tokenSentenca.deprel.equals("nsubjpass")))) {
                indiceHeadSujeito = tokenSentenca.head;
                indiceSujeito = tokenSentenca.id;
                sujeitoBruto = "";
                sujeitoBruto = tokenSentenca.form;
                loopSujeito:
                for (Token tokenFilhoSujeito : tokenSentenca.tokensFilhos) {
                    if (tokenFilhoSujeito.id < tokenSentenca.id) {
                        auxPreSujeito = auxPreSujeito + " " + tokenFilhoSujeito.form;
                    } else {
                        if (tokenFilhoSujeito.deprel.equals("name") || tokenFilhoSujeito.deprel.equals("amod")) {
                            auxPosSujeito = auxPosSujeito + " " + tokenFilhoSujeito.form;
                        }
                        if (tokenFilhoSujeito.deprel.equals("nmod")) {
//                            System.out.println(tokenFilhoSujeito.form);
                            pilhaAuxiliar.push(tokenFilhoSujeito);//Adiciona na pilha o token nmod
//                            flagSujeito = true;
                        }
                        while (!pilhaAuxiliar.empty()) {
                            elementoPilha = pilhaAuxiliar.pop();
                            for (Token tokenPilha : elementoPilha.tokensFilhos) { //TokenPilha serão os filhos de nmod, que estão sendo iterados
                                if (tokenPilha.deprel.equals("case")) {
                                    for (int i = tokenPilha.id; i <= elementoPilha.id; i++) {
                                        auxPosSujeito = auxPosSujeito + " " + this.sentence.sentenca.get(i).form;
                                    }
                                }
                                if (tokenPilha.id > elementoPilha.id) {
                                    if (tokenPilha.deprel.equals("name") || tokenPilha.deprel.equals("amod")) {
                                        auxPosSujeito = auxPosSujeito + " " + tokenPilha.form;
                                    }
                                    if (tokenPilha.deprel.equals("nmod") || tokenPilha.deprel.equals("dep") || tokenPilha.deprel.equals("dobj")) {
                                        pilhaAuxiliar.push(tokenPilha);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!(sujeitoBruto.equals(""))) {//Verifica se o sujeito não esta vazio
                    boolean flagPronomeRelativo = true;//Essa flag esta sendo usada para verificar se o pronome relativo foi substituido, se nao for substituido a tripla não será extraída
                    if (sujeitoBruto.equals("que") || sujeitoBruto.equals("quem")) {
                        flagPronomeRelativo = false;
                    }
                    sujeitoHeadAux = new Sujeito_Head();
                    sujeitoHeadAux.indiceHeadSujeito = indiceHeadSujeito;
                    sujeitoHeadAux.indiceSujeito = indiceSujeito;
                    sujeitoHeadAux.sujeito = auxPreSujeito + " " + sujeitoBruto + "" + auxPosSujeito;//concatena partes do sujeito
                    auxPreSujeito = "";
                    sujeitoBruto = "";
                    auxPosSujeito = "";
                    auxPreHead = "";
                    auxHead = sentence.sentenca.get(indiceHeadSujeito).form;
                    //VERIFICAR SE NÃO VAI SER NECESSÁRIO FAZER ESSA TROCA SOMENTE SE O FILHO DESSE VERBO FOR SUJEITO COM PRONOME RELATIO
                    if (this.sentence.sentenca.get(indiceHeadSujeito).deprel.equals("acl:relcl")) {
                        //foi passado como parâmetro o token que aponta para o sujeito que vai substituir o pronome relativo
                        sujeitoHeadAux.sujeito = detectaSujeitoAclRelc(this.sentence.sentenca.get(this.sentence.sentenca.get(indiceHeadSujeito).head));
                        flagPronomeRelativo = true;// Se o sujeito for um "que" a flag fica falso, mas se houver a substituiçao ela fica true de novo avisando que pode fazer a extraçao
                    }
                    loopHeadSujeito:
                    for (Token tokenFilhoHeadSujeito : sentence.sentenca.get(indiceHeadSujeito).tokensFilhos) {//Laço usado para detectar o head (normalmente verbo) do sujeito e seus auxiliares
                        if (tokenFilhoHeadSujeito.id > indiceHeadSujeito) {
                            break loopHeadSujeito;
                        }
                        //VERIFICAR SE O ID É MAIOR QUE O SUJEITO
                        if (((tokenFilhoHeadSujeito.deprel.equals("auxpass")) || (tokenFilhoHeadSujeito.deprel.equals("dobj")) || (tokenFilhoHeadSujeito.deprel.equals("cop")) || (tokenFilhoHeadSujeito.deprel.equals("aux")) || /*(tokenFilhoHeadSujeito.deprel.equals("advmod")) ||*/ (tokenFilhoHeadSujeito.deprel.equals("neg"))) && (tokenFilhoHeadSujeito.id > indiceSujeito)) {//VERIFICAR DEPOIS SE O COMENTARIO FEITO NO advmod VAI INTERFERIR EM ALGO
//                            System.out.println(tokenFilhoHeadSujeito.id + " " + indiceSujeito);
                            for (int i = tokenFilhoHeadSujeito.id; i < indiceHeadSujeito; i++) {
                                auxPreHead = auxPreHead + " " + this.sentence.sentenca.get(i).form;
                            }
                            break loopHeadSujeito;
                        }
                    }
                    detectaComplementosObjetosPosHeadSujeito(sujeitoHeadAux);//acha os dependentes do head
//                    for (ParteClausula pedacoSentencaDependente : sujeitoHeadAux.pedacosClausulaDependentesHeadSujeito) {
//                        System.out.print(pedacoSentencaDependente.parteClausula + " ");
//                    }
//                    System.out.println();
                    sujeitoHeadAux.headSujeito = auxPreHead + " " + auxHead;
//                    System.out.println(sujeitoHeadAux.headSujeito);
                    if (flagPronomeRelativo) {
                        this.sv.add(sujeitoHeadAux);
                    }
                }
            }
        }
    }
    public void extraiClausulas() {//Essa função faz a busca em profundidade dos elementos importantes
        Boolean[] vetorBooleanoTokensVisitados = new Boolean[sentence.sentenca.size()];//Vetor que será usado para verifica se token já foi visitado
        Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
        Stack<Token> pilhaAuxiliar = new Stack<Token>();//Pilha usada para fazer a busca em profundidade
        Stack<ParteClausula> pilhaParteClausula = new Stack<ParteClausula>();
        Token elementoPilha;//Vai ser o topo da pilha
        Token tokenPilha;//recebe os filhos de "elementoPilha"
        ParteClausula pedacoClausulaAux = new ParteClausula();
        ParteClausula topoPilhaParteClausula;
        Sujeito_Head sujeito_Head;
//        ParteClausula pedacoClausulaComplementar = new ParteClausula();
        boolean flagExtracao = false; //Esta flag vai indicar quando devo extrair algum pedaço de cláusula
        for (int indice = 0; indice < this.sv.size(); indice++) {
            sujeito_Head = this.sv.get(indice);
            Arrays.fill(vetorBooleanoTokensVisitados, Boolean.FALSE);
            for (Token tokenFilhoHeadSujeito : this.sentence.sentenca.get(sujeito_Head.indiceHeadSujeito).tokensFilhos) {
//                System.out.println(tokenFilhoHeadSujeito.deprel);
//                System.out.println(tokenFilhoHeadSujeito.form);
                if ((tokenFilhoHeadSujeito.deprel.equals("nmod") || tokenFilhoHeadSujeito.deprel.equals("ccomp") || tokenFilhoHeadSujeito.deprel.equals("advcl")) /*&& (tokenFilhoHeadSujeito.id > sujeito_Head.indiceHeadSujeito)*/) {//FICAR ATENTO SE A PARTE COMENTADA NÃO VAI GERAR MUITAS TRIPLAS ERRADAS
                    pilhaAuxiliar.push(tokenFilhoHeadSujeito);//Adiciona na pilha o token nmod
//                    System.out.println("Push1: " + tokenFilhoHeadSujeito.form);
//                    System.out.println("Id Pai " + this.sentence.sentenca.get(sujeito_Head.indiceHeadSujeito).form + ":" + this.sentence.sentenca.get(sujeito_Head.indiceHeadSujeito).id);
//                    System.out.println("Id Filho: " + tokenFilhoHeadSujeito.id + "\n");
                    vetorBooleanoTokensVisitados[tokenFilhoHeadSujeito.id] = true;//Avisa que o token já foi visitado
                    pedacoClausulaAux.parteClausula = "";
                    pedacoClausulaAux.parteClausula = pedacoClausulaAux.parteClausula + " " + retornaTokenConcatenado(tokenFilhoHeadSujeito);
                    pedacoClausulaAux.idNucleoParte = tokenFilhoHeadSujeito.id;
                    pilhaParteClausula.push(pedacoClausulaAux);
                    pedacoClausulaAux = new ParteClausula();
                    flagExtracao = true;
                } else {
                    //SO PODERA ENTRAR NA PILHA ESSAS PALAVRAS SE O ID DELAS FOREM MAIOR QUE O HEAD -> IMPORTANTISSIMO
                    if ((tokenFilhoHeadSujeito.deprel.contains("xcomp") || tokenFilhoHeadSujeito.deprel.equals("dobj") || tokenFilhoHeadSujeito.deprel.equals("iobj")) && (tokenFilhoHeadSujeito.id > tokenFilhoHeadSujeito.head)) {
                        pilhaAuxiliar.push(tokenFilhoHeadSujeito);//Adiciona na pilha o token
//                        System.out.println("Push nas palavras especiais1: " + tokenFilhoHeadSujeito.form);
//                        System.out.println("Id Pai " + this.sentence.sentenca.get(sujeito_Head.indiceHeadSujeito).form + ":" + this.sentence.sentenca.get(sujeito_Head.indiceHeadSujeito).id);
//                        System.out.println("Id Filho: " + tokenFilhoHeadSujeito.id + "\n");
                        vetorBooleanoTokensVisitados[tokenFilhoHeadSujeito.id] = true;//Avisa que o token já foi visitado
                        pedacoClausulaAux.parteClausula = "";
//                        sujeito_Head.pedacosClausulaDependentesHeadSujeito.add(pedacoClausulaAux);
//                        System.out.println(tokenFilhoHeadSujeito.form);
                        //clausulas aida esta vazia e eu sei que o pai das palavras que estao no if é o head
                        //entao eu adiciono esse dependente diretamente em sujeito_verbo
//                        pilhaParteClausula.push(topoPilhaParteClausula);
                        pedacoClausulaAux = new ParteClausula();
                        pedacoClausulaAux.parteClausula = "";/*tem que adicionar algo na pilha para não dar problema
                         de pilha vazia depois*/
                        pilhaParteClausula.push(pedacoClausulaAux);
                        pedacoClausulaAux = new ParteClausula();
                        flagExtracao = true;
                    }
                }
                while (!pilhaAuxiliar.empty()) {
                    elementoPilha = pilhaAuxiliar.peek();
                    for (int i = 0; i < elementoPilha.tokensFilhos.size(); i++) {
                        //tokenPilha = new Token();
                        tokenPilha = elementoPilha.tokensFilhos.get(i);//filho de elementoPilha
//                        System.out.println(tokenPilha.deprel);
                        if (!vetorBooleanoTokensVisitados[tokenPilha.id]) {
                            if ((tokenPilha.deprel.equals("nmod") || tokenPilha.deprel.equals("ccomp") || tokenPilha.deprel.equals("advcl") || tokenPilha.deprel.equals("advmod")) && (tokenPilha.id > tokenPilha.head)) {
//                                System.out.println("Push2: " + tokenPilha.form);
//                                System.out.println("Id Pai " + elementoPilha.form + ":" + elementoPilha.id);
//                                System.out.println("Id Filho: " + tokenPilha.id + "\n");
                                pilhaAuxiliar.push(tokenPilha);
                                pedacoClausulaAux.parteClausula = "";
                                pedacoClausulaAux.idNucleoParte = tokenPilha.id;
                                pedacoClausulaAux.parteClausula = pedacoClausulaAux.parteClausula + " " + retornaTokenConcatenado(tokenPilha);
                                pilhaParteClausula.push(pedacoClausulaAux);
                                pedacoClausulaAux = new ParteClausula();
                                flagExtracao = true;
                                vetorBooleanoTokensVisitados[tokenPilha.id] = true;
                                elementoPilha = new Token();
                                elementoPilha.tokensFilhos = tokenPilha.tokensFilhos;
                                i = -1;//deve ser -1, pois quando entrar no for ele vai incrementar a variável, fazendo com que o valor dela fique zero (i = i+1)
//                                System.out.println("valor do primeiro filho: " + elementoPilha.tokensFilhos.get(0).deprel);
                            } else {
                                if ((tokenPilha.deprel.contains("xcomp") || tokenPilha.deprel.equals("dobj") || tokenPilha.deprel.equals("iobj")) && (tokenPilha.id > tokenPilha.head)) {
                                    if (tokenPilha.id > tokenPilha.head) {//Isso tem que ser verificado para nao dar
                                        //problema nas extrações nem entrar em loop eterno na função "retornaTokenConcatenado"
                                        pilhaAuxiliar.push(tokenPilha);//
//                                        System.out.println("Push nas palavras especiais2: " + tokenPilha.form);
//                                        System.out.println("Id Pai " + elementoPilha.form + ":" + elementoPilha.id);
//                                        System.out.println("Id Filho: " + tokenPilha.id + "\n");
                                        topoPilhaParteClausula = pilhaParteClausula.pop();//obtenho este ultimo token da pilha para
                                        //adicionar o dependente dele, depois eu vou colocar de novo na pilha
                                        pedacoClausulaAux.parteClausula = "";
                                        pedacoClausulaAux.parteClausula = pedacoClausulaAux.parteClausula + " " + retornaTokenConcatenado(tokenPilha);
                                        pedacoClausulaAux.idNucleoParte = tokenPilha.id;
                                        topoPilhaParteClausula.pedacosDependentes.add(pedacoClausulaAux);
//                                        System.out.println(tokenPilha.form);
//                                        System.out.println(topoPilhaParteClausula.pedacosDependentes.get(0).parteClausula);
                                        pilhaParteClausula.push(topoPilhaParteClausula);//adicionando o token na pilha novamente
                                        pedacoClausulaAux = new ParteClausula();
                                        pedacoClausulaAux.parteClausula = "";
                                        if (verificaTokenPrincipalComplementosObjetos(sujeito_Head.indiceHeadSujeito, tokenPilha)) {
//                                            System.out.println(pedacoClausulaAux.parteClausula);
                                            pilhaParteClausula.push(pedacoClausulaAux);
                                        } else {
                                            pedacoClausulaAux.parteClausula = "";
                                            pilhaParteClausula.push(pedacoClausulaAux);
                                        }
                                        pedacoClausulaAux = new ParteClausula();
                                        topoPilhaParteClausula = new ParteClausula();//Essa variável vai ser usada para
                                        //adicionar o token dependente dela, se for vai ter que tirar do topo pra depois colocar
                                        //novamente ja com o dependente, dessa forma, quando entrar na funçao que extrai a tripla nao
                                        //vai precisar concatenar quando aparecer esses tokens, porque ele ja vai ter sido concatenado antes
                                        flagExtracao = true;
                                        vetorBooleanoTokensVisitados[tokenPilha.id] = true;//Avisa que o token já foi visitado
                                        elementoPilha = new Token();
                                        elementoPilha.tokensFilhos = tokenPilha.tokensFilhos;
                                        i = -1;
//                                        System.out.println("valor do primeiro filho: " + elementoPilha.tokensFilhos.get(0).deprel);
//                                        System.out.println("tamanho do vetor tokens filhos: " + elementoPilha.tokensFilhos.size());
//                                        System.out.println("valor de i: " + i);
                                    }
                                }
                            }
                        }
                    }
                    if (flagExtracao) {
//                        System.out.println(sujeito_Head.pedacosClausulaDependentesHeadSujeito.get(0).parteClausula);
                        organizaClausulaEtriplaEadiciona(sujeito_Head, pilhaParteClausula);
                    }
//                    System.out.println(sujeito_Head.pedacosClausulaDependentesHeadSujeito.get(0).parteClausula);
                    pilhaParteClausula.pop();
//                    System.out.println("Pop: " + pilhaAuxiliar.pop().form);
                    pilhaAuxiliar.pop();
                    flagExtracao = false;
                }
            }
        }
    }
    //ESSA FUNÇÃO VAI RECEBER UM TOKEN E VAI CONCATENAR AO MESMO AS SUAS PARTES COMPLEMENTARES COMO: det, case, mark ...
    public String retornaTokenConcatenado(Token token) {
        String tokenConcatenadoComDependentes = "";
        if (token.deprel.equals("nmod")) {
            boolean flagNmod = true; //flag que vai indica se o xcomp tem filho ou nao
            for (Token t : token.tokensFilhos) {
                if (t.id < token.id) {
                    for (int i = t.id; i <= token.id; i++) {
                        if (flagNmod) {
                            if (!(this.sentence.sentenca.get(i).deprel.equals("punct"))) {
                                tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + this.sentence.sentenca.get(i).form;
                            }
                        }
                    }
                    flagNmod = false;
                } else if (t.id > token.id) {
                    if (t.deprel.equals("name") || t.deprel.equals("amod") || t.deprel.equals("nummod") || t.deprel.equals("appos")) {
                        tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + t.form;
                    }
                }
            }
            if (flagNmod) {//se o nmod nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                tokenConcatenadoComDependentes = token.form + " " + tokenConcatenadoComDependentes;
            }
//            System.out.println(token.form);
            return tokenConcatenadoComDependentes;
        }
        if (token.deprel.equals("ccomp")) {
            for (Token t : token.tokensFilhos) {
                if (t.deprel.equals("mark")) {
                    for (int i = t.id; i <= token.id; i++) {
                        tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + this.sentence.sentenca.get(i).form;
                    }
                }
                if (t.deprel.equals("name") || t.deprel.equals("amod")) {
                    tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + t.form;
                }
            }
            return tokenConcatenadoComDependentes;
        }
        if (token.deprel.equals("advcl") || token.deprel.equals("advmod")) {
            boolean flagAdv = true; //flag que vai indica se o xcomp tem filho ou nao
            for (Token t : token.tokensFilhos) {
                if (t.id < token.id) {
                    for (int i = t.id; i <= token.id; i++) {
                        if (flagAdv) {
                            if (!(this.sentence.sentenca.get(i).deprel.equals("punct"))) {
                                tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + this.sentence.sentenca.get(i).form;
                            }
                        }
                    }
                    flagAdv = false;
                } else if (t.id > token.id) {
                    if (t.deprel.equals("name") || t.deprel.equals("amod") || t.deprel.equals("nummod") || t.deprel.equals("appos")) {
                        tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + t.form;
                    }
                }
            }
            if (flagAdv) {//se o nmod nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                tokenConcatenadoComDependentes = token.form + " " + tokenConcatenadoComDependentes;
            }
//            System.out.println(token.form);
            return tokenConcatenadoComDependentes;
        }
        if (token.deprel.equals("dobj")) {
            boolean flagDobj = true; //flag que vai indica se o xcomp tem filho ou nao
            for (Token t : token.tokensFilhos) {
                if (t.id < token.id) {
                    for (int i = t.id; i <= token.id; i++) {
                        if (flagDobj) {
                            if (!(this.sentence.sentenca.get(i).deprel.equals("punct"))) {
                                tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + this.sentence.sentenca.get(i).form;
                            }
                        }
                    }
                    flagDobj = false;
                } else if (t.id > token.id) {
                    if (t.deprel.equals("name") || t.deprel.equals("amod") || t.deprel.equals("nummod") || t.deprel.equals("appos")) {
                        tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + t.form;
                    }
                }
            }
            if (flagDobj) {//se o nmod nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                tokenConcatenadoComDependentes = token.form + " " + tokenConcatenadoComDependentes;
            }
//            System.out.println(token.form);
            return tokenConcatenadoComDependentes;
        }
        if (token.deprel.equals("iobj")) {
            boolean flagIobj = true; //flag que vai indica se o xcomp tem filho ou nao
            for (Token t : token.tokensFilhos) {
                if (t.id < token.id) {
                    for (int i = t.id; i <= token.id; i++) {
                        if (flagIobj) {
                            if (!(this.sentence.sentenca.get(i).deprel.equals("punct"))) {
                                tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + this.sentence.sentenca.get(i).form;
                            }
                        }
                    }
                    flagIobj = false;
                } else if (t.id > token.id) {
                    if (t.deprel.equals("name") || t.deprel.equals("amod") || t.deprel.equals("nummod") || t.deprel.equals("appos")) {
                        tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + t.form;
                    }
                }
            }
            if (flagIobj) {//se o nmod nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                tokenConcatenadoComDependentes = token.form + " " + tokenConcatenadoComDependentes;
            }
//            System.out.println(token.form);
            return tokenConcatenadoComDependentes;
        }
        if (token.deprel.contains("xcomp")) {
            boolean flagXcomp = true; //flag que vai indica se o xcomp tem filho ou nao
            for (Token t : token.tokensFilhos) {
                if (!(t.deprel.equals("punct"))) {
                    if (t.id < token.id) {
                        for (int i = t.id; i <= token.id; i++) {
                            if (flagXcomp) {
                                if (!(this.sentence.sentenca.get(i).deprel.equals("punct"))) {
                                    tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + this.sentence.sentenca.get(i).form;
                                }
                            }
                        }
                        flagXcomp = false;
                    } else if (t.id > token.id) {
                        if (t.deprel.equals("name") || t.deprel.equals("amod") || t.deprel.equals("nummod") || t.deprel.equals("appos")) {
                            tokenConcatenadoComDependentes = tokenConcatenadoComDependentes + " " + t.form;
                        }
                    }
                }
            }
            if (flagXcomp) {//se o nmod nao tiver case ou mark vai ser concatenado apenas o nmod com o name e amod (se tiver esses dois ultimos)
                tokenConcatenadoComDependentes = token.form + " " + tokenConcatenadoComDependentes;
            }
//            System.out.println(token.form);
            return tokenConcatenadoComDependentes;
        }
        return "";
    }
    public void organizaClausulaEtriplaEadiciona(Sujeito_Head sujeito_head, Stack<ParteClausula> pilhaParteClausula) {
//        for (ParteClausula pedacoSentencaDependente : pilhaParteClausula) {
//            System.out.print(pedacoSentencaDependente.parteClausula + " ");
//        }
//        System.out.println();
        boolean flag = false;//flag que vai verificar se existe clausulas a serem adicionadas no arg2 que nao seja O ou C
        Tripla tripla = new Tripla();
        ClausulaExtraida clausulaExtraida = new ClausulaExtraida();
        ArrayList<ParteClausula> ordemPartes = new ArrayList<>(10);
        ParteClausula parteClausula = new ParteClausula();
        String pedacosClausula, arg1, arg2, rel;
        pedacosClausula = arg1 = arg2 = "";
        arg1 = sujeito_head.sujeito;
        parteClausula.idNucleoParte = sujeito_head.indiceSujeito;
        parteClausula.parteClausula = sujeito_head.sujeito;
        ordemPartes.add(parteClausula);
        parteClausula = new ParteClausula();
        parteClausula.idNucleoParte = sujeito_head.indiceHeadSujeito;
        parteClausula.parteClausula = sujeito_head.headSujeito;
        ordemPartes.add(parteClausula);
        parteClausula = new ParteClausula();
        rel = sujeito_head.headSujeito;
        for (ParteClausula pedacoSentenca : pilhaParteClausula) {
            if (!(pedacoSentenca.parteClausula.equals(""))) {
                flag = true;
                pedacosClausula = pedacosClausula + " " + pedacoSentenca.parteClausula;
                for (ParteClausula pedacoSentencaDependente : pedacoSentenca.pedacosDependentes) {
                    pedacosClausula = pedacosClausula + pedacoSentencaDependente.parteClausula + " ";
                }
                parteClausula.idNucleoParte = pedacoSentenca.idNucleoParte;
                parteClausula.parteClausula = pedacoSentenca.parteClausula;
                ordemPartes.add(parteClausula);
                parteClausula = new ParteClausula();
            }
        }
        if (flag) {
            for (ParteClausula pedacoSentencaDependente : sujeito_head.pedacosClausulaDependentesHeadSujeito) {
                rel = rel + " " + pedacoSentencaDependente.parteClausula + " ";
            }
        } else {
            for (ParteClausula pedacoSentencaDependente : sujeito_head.pedacosClausulaDependentesHeadSujeito) {
                arg2 = arg2 + pedacoSentencaDependente.parteClausula + " ";
            }
//            arg2 = arg2 + pedacosClausula;
        }
//        int[] arrayPedacosClausula = {sujeito_head.indiceSujeito, sujeito_head.indiceHeadSujeito, pilhaParteClausula.peek().idNucleoParte};
//        Arrays.sort(arrayPedacosClausula);
//        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//        map.put(sujeito_head.indiceSujeito, 1);//codigo a tribuido ao sujeito
//        map.put(sujeito_head.indiceHeadSujeito, 2);//codigo a tribuido ao head do sujeito
//        map.put(pilhaParteClausula.peek().idNucleoParte, 3);//codigo a tribuido à parte secundaria da clausula
//        for (int indiceTipoClausula : arrayPedacosClausula) {
//            if (map.get(indiceTipoClausula) == 1) {
//                parteClausula.idNucleoParte = sujeito_head.indiceSujeito;
//                parteClausula.parteClausula = sujeito_head.sujeito;
//                ordemPartes.add(parteClausula);
//                parteClausula = new ParteClausula();
//            }
//            if (map.get(indiceTipoClausula) == 2) {
//                for (ParteClausula pedacoSentencaDependente : sujeito_head.pedacosClausulaDependentesHeadSujeito) {
//                    pedacosClausula = pedacosClausula + pedacoSentencaDependente.parteClausula + " ";
//
////                    System.out.print(pedacoSentencaDependente.parteClausula + " ");
//                }
//
////                System.out.println("");
//                parteClausula.idNucleoParte = sujeito_head.indiceHeadSujeito;
//                parteClausula.parteClausula = sujeito_head.headSujeito;
//                ordemPartes.add(parteClausula);
//                parteClausula = new ParteClausula();
//            }
//            if (map.get(indiceTipoClausula) == 3) {
//                for (ParteClausula pedacoSentenca : pilhaParteClausula) {
//                    pedacosClausula = pedacosClausula + pedacoSentenca.parteClausula + " ";
//                    parteClausula.idNucleoParte = pedacoSentenca.idNucleoParte;
//                    parteClausula.parteClausula = pedacoSentenca.parteClausula;
//                    for (ParteClausula pedacoSentencaDependente : pedacoSentenca.pedacosDependentes) {
//                        pedacosClausula = pedacosClausula + pedacoSentencaDependente.parteClausula + " ";
//                    }
//                    ordemPartes.add(parteClausula);
//                    parteClausula = new ParteClausula();
//                }
//            }
//
//        }
        tripla.arg1 = arg1;
        tripla.rel = rel;
        tripla.arg2 = arg2 + pedacosClausula;
//        if (sujeito_head.indiceSujeito < pilhaParteClausula.peek().idNucleoParte) {
//            tripla.arg1 = sujeito_head.sujeito;
//            tripla.rel = sujeito_head.headSujeito;
//            tripla.arg2 = pedacosClausula;
//        } else {
//            tripla.arg2 = sujeito_head.sujeito;
//            tripla.rel = sujeito_head.headSujeito;
//            tripla.arg1 = pedacosClausula;
//        }
        //System.out.println(sujeito_head.sujeito);
        this.triplas.add(tripla);
        clausulaExtraida.ordemPartes = ordemPartes;
        this.clausulasExtraidas.add(clausulaExtraida);
    }
    public String detectaSujeitoAclRelc(Token tokenSujeitoAclRelc) { //Função que vai detectar o sujeito, que nesse caso provavelmente vai ser "que" ou algum pronome do tipo, relacionado ao verbo e vai substituir o atual sujeito pelo encontrado
        Stack<Token> pilhaAuxiliar = new Stack<Token>();
        Token elementoPilha = new Token();
        String auxPreSujeito = "";//Palavras que podem vir antes do sujeito
        String sujeitoBruto = "";//sujeito sem seus complementos
        String auxPosSujeito = "";//Palavras que podem vir depois do sujeito
        sujeitoBruto = "";
        sujeitoBruto = tokenSujeitoAclRelc.form;
        loopSujeito:
        for (Token tokenFilhoSujeito : tokenSujeitoAclRelc.tokensFilhos) {
            if (tokenFilhoSujeito.id < tokenSujeitoAclRelc.id) {
                auxPreSujeito = auxPreSujeito + " " + tokenFilhoSujeito.form;
            } else {
                if (tokenFilhoSujeito.deprel.equals("name") || tokenFilhoSujeito.deprel.equals("amod")) {
                    auxPosSujeito = auxPosSujeito + " " + tokenFilhoSujeito.form;
                }
                if (tokenFilhoSujeito.deprel.equals("nmod")) {
                    pilhaAuxiliar.push(tokenFilhoSujeito);//Adiciona na pilha o token nmod
                }
                while (!pilhaAuxiliar.empty()) {
                    elementoPilha = pilhaAuxiliar.pop();
                    for (Token tokenPilha : elementoPilha.tokensFilhos) { //TokenPilha serão os filhos de nmod, que estão sendo iterados
                        if (tokenPilha.deprel.equals("case")) {
                            for (int i = tokenPilha.id; i <= elementoPilha.id; i++) {
                                auxPosSujeito = auxPosSujeito + " " + this.sentence.sentenca.get(i).form;
                            }
                        }
                        if (tokenPilha.id > elementoPilha.id) {
                            if (tokenPilha.deprel.equals("name") || tokenPilha.deprel.equals("amod")) {
                                auxPosSujeito = auxPosSujeito + " " + tokenPilha.form;
                            }
                            if (tokenPilha.deprel.equals("nmod") || tokenPilha.deprel.equals("dep") || tokenPilha.deprel.equals("dobj")) {
                                pilhaAuxiliar.push(tokenPilha);
                            }
                        }
                    }
                }
            }
        }
        return auxPreSujeito + " " + sujeitoBruto + " " + auxPosSujeito;//concatena partes do sujeito e retorna;
    }
    public void detectaComplementosObjetosPosHeadSujeito(Sujeito_Head sujeito_head) { //Função que vai detectar os complementos do head (normalmente verbo) que podem ser: xcomp e objetos. A função so vai detectar os pedaços que estao depois do head e adicionando como item dependente. A saida é passada por referencia
        Token TokenHead = this.sentence.sentenca.get(sujeito_head.indiceHeadSujeito);
        Token elementoFilho;
        ParteClausula pedacoClausulaAux = new ParteClausula();
        String tokenConcatenado = "";
        for (int i = 0; i < TokenHead.tokensFilhos.size(); i++) {
            elementoFilho = new Token();
            elementoFilho = TokenHead.tokensFilhos.get(i);
            if ((elementoFilho.deprel.contains("xcomp") || elementoFilho.deprel.equals("dobj") || elementoFilho.deprel.equals("iobj")) && (elementoFilho.id > TokenHead.id)) {
                tokenConcatenado = retornaTokenConcatenado(elementoFilho);
//                System.out.println("pedaço: " + tokenConcatenado);
                pedacoClausulaAux = new ParteClausula();
                pedacoClausulaAux.parteClausula = "";
                pedacoClausulaAux.parteClausula = tokenConcatenado;
                pedacoClausulaAux.idNucleoParte = elementoFilho.id;
//                System.out.println("pedaço: " + elementoFilho.form);
//                System.out.println("id: " + elementoFilho.id + "\n");
                sujeito_head.pedacosClausulaDependentesHeadSujeito.add(pedacoClausulaAux);
                tokenConcatenado = "";
                TokenHead = new Token();
                TokenHead = elementoFilho;
                i = -1;
            }
        }
    }
    /*Esta função vai verificar se o objeto ou complemento tem como pai/avo/bisavo o head do sujeito para não repetir
     o elemento na hora da extraçao. Ex: imagine uma frase assim: S + V + C + O, o O mesmo tem como pai C e avo V
     Dessa forma eu preciso verificar que o O também depende de V para quando chegar na funçao de extraçao eu nao add O
     como se fosse dependente de C*/
    public boolean verificaTokenPrincipalComplementosObjetos(int indiceHeadSujeito, Token token) {
        Stack<Token> pilhaAuxiliar = new Stack<Token>();
        Token elementoPilha = new Token();
        Token tokenAux = this.sentence.sentenca.get(token.head);
        if (tokenAux.id == indiceHeadSujeito) {
            return false;
        }
        if (tokenAux.deprel.contains("xcomp") || tokenAux.deprel.equals("dobj") || tokenAux.deprel.equals("iobj")) {
            pilhaAuxiliar.push(tokenAux);
        }
        while (!pilhaAuxiliar.empty()) {
            elementoPilha = pilhaAuxiliar.pop();
            tokenAux = new Token();
            tokenAux = this.sentence.sentenca.get(elementoPilha.head);
            if (tokenAux.id == indiceHeadSujeito) {
                return false;
            }
            if (tokenAux.deprel.contains("xcomp") || tokenAux.deprel.equals("dobj") || tokenAux.deprel.equals("iobj")) {
                pilhaAuxiliar.push(tokenAux);
            }
        }
        return true;//retorna true se não tiver parentesco com o head do sujeito por meio de complementos e objetos apenas
    }
    public class Sujeito_Head {
        protected String sujeito;
        protected String headSujeito;
        protected int indiceSujeito;
        protected int indiceHeadSujeito;
        protected ArrayList<ParteClausula> pedacosClausulaDependentesHeadSujeito;
        public Sujeito_Head() {
            pedacosClausulaDependentesHeadSujeito = new ArrayList<>(3);
        }
    }
    public class ParteClausula {
        protected String parteClausula;
        protected int idNucleoParte;
        protected ArrayList<ParteClausula> pedacosDependentes;
        public ParteClausula() {
            pedacosDependentes = new ArrayList<>(3);
        }
    }
    public class ClausulaExtraida {
        protected ArrayList<ParteClausula> ordemPartes;
    }
    public class ClasseAuxiliar {
        protected String string;
        protected int id;
    }
    public class Tripla {
        protected String arg1;
        protected String rel;
        protected String arg2;
    }
}