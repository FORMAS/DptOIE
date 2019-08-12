/*
 * Copyright 2018 Christian Kohlschütter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pre_processamento;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author Leandro
 */
public class PreProcessamento {

    private ArrayList<String> sentencasSemProcessamento;
    private ArrayList<String[]> tokensSentencasSemProcessamento;

    private String diretorioModelos;
    private String diretorioSaidaTokenizerPosDp;
    private String arquivoTextoEntrada;
    private String arquivoSentencasTokenizadas;
    private String arquivoSentencasEntradaDPConll;
    private String arquivoSaidaDP;

    /*Modelos*/
    private String caminhoModeloPos;
    private String caminhoModeloDP;
    /*------------------*/

    public PreProcessamento() {
        sentencasSemProcessamento = new ArrayList<>();
        tokensSentencasSemProcessamento = new ArrayList<>();

        diretorioModelos = "pt-models\\";
        diretorioSaidaTokenizerPosDp = "saida\\";
        caminhoModeloDP = diretorioModelos + "pt-dep-parser.gz";
        caminhoModeloPos = diretorioModelos + "pt-pos-tagger.model";
        arquivoTextoEntrada = diretorioModelos + "testes.txt";
        arquivoSentencasTokenizadas = diretorioSaidaTokenizerPosDp + "sentencas_tokenizadas.txt";
        arquivoSentencasEntradaDPConll = diretorioSaidaTokenizerPosDp + "entrada_para_DP_testes.txt";
        arquivoSaidaDP = diretorioSaidaTokenizerPosDp + "saidaDP_testes.conll";
    }

    public static void main(String Args[]) throws IOException {
        PreProcessamento preProcessamento = new PreProcessamento();
        /*Gera arquivo com setenças tokenizadas separadas por espaço. Cada linha do arquivo corresponde a uma sentença*/
        preProcessamento.retornaTokensSentencasSeparadoPorEspaco(preProcessamento.arquivoTextoEntrada);
        /*Gera arquivo conll para dar entrada ao Dependency parser*/
        preProcessamento.geraEntradaConllParaDependencyParser(preProcessamento.caminhoModeloPos, preProcessamento.arquivoSentencasTokenizadas);
        /*Executa Dependency Parser*/
        preProcessamento.dependencyParser(preProcessamento.caminhoModeloDP, preProcessamento.arquivoSentencasEntradaDPConll);
//        String sentenca = "Traduzindo em termos simples: nenhum político gosta de colocar azeitona en a empada de seus adversários .";
//        preProcessamento.retornaPos(preProcessamento.caminhoModeloPos, sentenca);

    }


    /*Esta função gera um arquivo com sentenças tokenizadas separadas por espaço e armazena as sentenças brutas em uma
     variável*/
    public void retornaTokensSentencasSeparadoPorEspaco(String caminhoArquivoTexto) throws IOException {
        System.out.println("Criando arquivo de sentenças tokenizadas...");
        /*Ler arquivo de uma vez só*/
//        Path caminhoArquivo = Paths.get(caminhoArquivoTexto);

        ArrayList<String> arraySentencas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(caminhoArquivoTexto), "UTF-8"));
        while (br.ready()) {
            String linha = br.readLine();
            arraySentencas.add(linha);
        }
        br.close();

//        String dados = new String(Files.readAllBytes(caminhoArquivo), "UTF-8");
        String TokensSentencaSepadadoPorEspaco = "";
        /*-------------------------------------------------*/
        // cria pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", "tokenize,ssplit",
                        "ssplit.isOneSentence", "false", //O parametro false é usado para o sentenceSplit não ler tudo como se fosse uma sentença em uma linha
                        "tokenize.language", "es",
                        "tokenize.options", "ptb3Escaping=false"
                ));
        for(String dados : arraySentencas){
            // read some text in the text variable
            Annotation document = new Annotation(dados);
            // Executa todos os anotadores passados na função pipeline
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                int index = 0;
                /*Adiciona a sentença bruta no array*/
                this.sentencasSemProcessamento.add(sentence.toString());
                String[] tokensSemProcessamento = new String[sentence.get(TokensAnnotation.class).size()];
                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                    // Retorna texto do token
                    String wordToken = token.get(TextAnnotation.class);
                    String[] tokensContracaoQuebrado = retornaTokenQuebrado(wordToken);
                    String[] tokenComHifen = retornaPalavraComHifenQuebrada(wordToken);
                    tokensSemProcessamento[index] = wordToken;
                    if (tokensContracaoQuebrado != null) {
                        for (String tokenContracaoQuebrado : tokensContracaoQuebrado) {
                            TokensSentencaSepadadoPorEspaco = TokensSentencaSepadadoPorEspaco + tokenContracaoQuebrado + " ";
                        }
                    } else if (tokenComHifen != null) {
                        int qtdHifenToken = tokenComHifen.length - 1;
                        for (String tokenHifen : tokenComHifen) {
                            TokensSentencaSepadadoPorEspaco = TokensSentencaSepadadoPorEspaco + tokenHifen + " ";
                            if (qtdHifenToken > 0) {
                                TokensSentencaSepadadoPorEspaco = TokensSentencaSepadadoPorEspaco + "-" + " ";
                                qtdHifenToken--;
                            }
                        }
                    } else {
                        TokensSentencaSepadadoPorEspaco = TokensSentencaSepadadoPorEspaco + wordToken + " ";
                    }
                    index++;
                }
                //Adiciona quebra de linha
                TokensSentencaSepadadoPorEspaco = TokensSentencaSepadadoPorEspaco + System.lineSeparator();
                this.tokensSentencasSemProcessamento.add(tokensSemProcessamento);
            }
        }

        File arquivo = new File(arquivoSentencasTokenizadas);
        try (FileWriter fw = new FileWriter(arquivo)) {
            fw.write(TokensSentencaSepadadoPorEspaco);
            fw.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Arquivo de sentenças tokenizadas criado");
    }

    /*Função que retorna POS de uma sentença*/
    public ArrayList<String> retornaPos(String caminhoModelo, String sentenca) {
        Properties props = new Properties();
        String separador = "___";
        props.setProperty("tagSeparator", separador);
        MaxentTagger tagger = new MaxentTagger(caminhoModelo, props);
        String TokenEposJuntosComOSeparador = tagger.tagTokenizedString(sentenca);
//        System.out.println(TokenEposJuntosComOSeparador);
        String[] posSeparadoPorTokens = TokenEposJuntosComOSeparador.split(" ");
        ArrayList<String> pos = new ArrayList<>();
        for (String token : posSeparadoPorTokens) {
            pos.add(token.split(separador)[1]);
        }
        return pos;
    }

    public void dependencyParser(String modelo, String arquivoEntradaDP) throws FileNotFoundException {
        System.out.println("Executando Dependency Parser...");
        Properties props = new Properties();
        String saidaDP = arquivoSaidaDP;
        DependencyParser dp = new DependencyParser(props);
        //Carrega modelo treinado
        dp.loadModelFile(modelo);
        //Gera saída no formato conll e mostra resultados LAS se for um arquivo de teste
        /*Foi utilizada a função de teste do DP porque ainda não encontrei uma forma de colocar a saída em
         conll de outra forma.*/
        dp.testCoNLL(arquivoEntradaDP, saidaDP);
        System.out.println("Execução do Dependency Parser finalizada.");
    }

    /*Função que recebe as sentenças(separadas por quebra de linha) tokenizadas(separadas por vírgula)*/
    public void geraEntradaConllParaDependencyParser(String caminhoModeloPos, String sentencasTokenizadas) throws FileNotFoundException, IOException {
        System.out.println("Criando arquivo de entrada para o DependencyParser...");
        String linhaPadrãoConll = "";
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(arquivoSentencasEntradaDPConll),
                Charset.forName("UTF-8").newEncoder()
        );
        FileReader fr = new FileReader(arquivoSentencasTokenizadas);
        BufferedReader br = new BufferedReader(fr);
        String sentenca;
        int index = 0;
        while ((sentenca = br.readLine()) != null) {
            String[] tokensSentenca = sentenca.split(" ");
            ArrayList<String> posSentenca = retornaPos(caminhoModeloPos, sentenca);
//            Boolean[] tokensSemEspacoFinal = retornaVetorBooleanoDosTokensSemEspacoFinal(this.tokensSentencasSemProcessamento.get(index), this.sentencasSemProcessamento.get(index));
            for (int i = 0; i < tokensSentenca.length; i++) {
                linhaPadrãoConll = retornaLinhaArquivoConll(i + 1, tokensSentenca, posSentenca/*, tokensSemEspacoFinal[i]*/);
                writer.write(linhaPadrãoConll);
            }
            writer.write(System.lineSeparator());
            index++;
        }
        br.close();
        writer.flush();
        writer.close();
        System.out.println("Arquivo de entrada para o DependencyParser criado");
    }

    public String retornaLinhaArquivoConll(int idToken, String[] tokens, ArrayList<String> pos/*, boolean flagIndicaCaractereAposTokenEspacoOuNao*/) {
        int index = idToken - 1;
        String form = tokens[index];
        String lemma = "_";
        String upos = mapeamentoTokenPosTagger(pos.get(index));
        String xpos = pos.get(index);
        String feats = "_";
        String head = "0";
        String deprel = "null";
        String deps = "_";
        String misc = "_";
        String linhaConll;
        /*Em alguns casos o POS não retorna nada. Então foi atribuido 'X' a ele*/
        if (xpos.equals("_")) {
            upos = "X";
            xpos = "X";
        }

        String featDetSingMasc = "Definite=Def|Gender=Masc|Number=Sing|PronType=Art";
        String featDetPluMasc = "Definite=Def|Gender=Masc|Number=Plur|PronType=Art";
        String featDetSingFem = "Definite=Def|Gender=Fem|Number=Sing|PronType=Art";
        String featDetPluFem = "Definite=Def|Gender=Fem|Number=Plur|PronType=Art";
//        String noSpaceAfter = "SpaceAfter=No";

        /*Verifica se é um artigo e se antes é uma preposição(a, de, en ou por) com contração para gerar as feats corretamente*/
        if (!(index == 0 || index == tokens.length - 1)) {
            if (xpos.equals("DET") && pos.get(index - 1).equals("ADP") && (tokens[index - 1].toLowerCase().equals("por") || tokens[index - 1].toLowerCase().equals("a") || tokens[index - 1].toLowerCase().equals("de") || tokens[index - 1].toLowerCase().equals("en"))) {
                lemma = "o";
                if (form.equals("o")) {
                    feats = featDetSingMasc;
                } else if (form.equals("os")) {
                    feats = featDetPluMasc;
                } else if (form.equals("a")) {
                    feats = featDetSingFem;
                } else if (form.equals("as")) {
                    feats = featDetPluFem;
                    /*Verifica se é uma preposição(a, de, en ou por) com contração para gerar as feats corretamente*/
                }
            } else if (xpos.equals("ADP") && pos.get(index + 1).equals("DET") && (form.toLowerCase().equals("por") || form.toLowerCase().equals("a") || form.toLowerCase().equals("de") || form.toLowerCase().equals("en"))) {
                lemma = form;
            }
        }

        if (xpos.equals("ADV") && form.toLowerCase().equals("não")) {
            feats = "Polarity=Neg";
        } else if (xpos.equals("NUM")) {
            feats = "NumType=Card";
        }/*else if(flagIndicaCaractereAposTokenEspacoOuNao){
         misc = noSpaceAfter;
         }*/

        linhaConll = idToken + "\t" + form + "\t" + lemma + "\t" + upos + "\t" + xpos + "\t" + feats + "\t" + head + "\t" + deprel + "\t" + deps + "\t" + misc + System.lineSeparator();

        return linhaConll;
    }

    public String mapeamentoTokenPosTagger(String posToken) {
        Map<String, String> map = new HashMap<>();
        map.put(".", "PUNCT");
        map.put("PNOUN", "PROPN");
        map.put("PRT", "PART");
        map.put("ADPPRON", "X");
        map.put("ADJ", "ADJ");
        map.put("ADP", "ADP");
        map.put("ADV", "ADV");
        map.put("AUX", "AUX");
        map.put("CONJ", "CONJ");
        map.put("DET", "DET");
        map.put("NOUN", "NOUN");
        map.put("PRON", "PRON");
        map.put("VERB", "VERB");
        map.put("NUM", "NUM");
        map.put("X", "X");
        map.put("root", "root");
        return map.get(posToken);
    }

    /*Função que vai quebrar o token. Isso foi feito para seguir o mesmo padrão do treebank*/
    public String[] retornaTokenQuebrado(String token) {
        String tokenQuebrado[] = new String[2];
        switch (token.toLowerCase()) {
            case "do":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "o";
                break;

            case "da":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "a";
                break;

            case "no":
                tokenQuebrado[0] = "en";
                tokenQuebrado[1] = "o";
                break;

            case "na":
                tokenQuebrado[0] = "en";
                tokenQuebrado[1] = "a";
                break;

            case "dos":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "os";
                break;

            case "ao":
                tokenQuebrado[0] = "a";
                tokenQuebrado[1] = "o";
                break;

            case "das":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "as";
                break;

            case "à":
                tokenQuebrado[0] = "a";
                tokenQuebrado[1] = "a";
                break;

            case "pelo":
                tokenQuebrado[0] = "por";
                tokenQuebrado[1] = "o";
                break;

            case "pela":
                tokenQuebrado[0] = "por";
                tokenQuebrado[1] = "a";
                break;

            case "nos":
                tokenQuebrado[0] = "en";
                tokenQuebrado[1] = "os";
                break;

            case "aos":
                tokenQuebrado[0] = "a";
                tokenQuebrado[1] = "os";
                break;

            case "nas":
                tokenQuebrado[0] = "en";
                tokenQuebrado[1] = "as";
                break;

            case "às":
                tokenQuebrado[0] = "a";
                tokenQuebrado[1] = "as";
                break;

            case "dum":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "um";
                break;

            case "duma":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "umas";
                break;

            case "pelos":
                tokenQuebrado[0] = "por";
                tokenQuebrado[1] = "os";
                break;

            case "num":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "um";
                break;

            case "numa":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "uma";
                break;

            case "pelas":
                tokenQuebrado[0] = "por";
                tokenQuebrado[1] = "as";
                break;

            case "doutros":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "outros";
                break;

            case "nalguns":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "alguns";
                break;

            case "dalguns":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "alguns";
                break;

            case "noutras":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "outras";
                break;

            case "dalgumas":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "algumas";
                break;

            case "doutra":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "outra";
                break;

            case "noutros":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "outros";
                break;

            case "nalgumas":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "algumas";
                break;

            case "doutras":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "outras";
                break;

            case "noutro":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "outro";
                break;

            case "donde":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "onde";
                break;

            case "doutro":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "outro";
                break;

            case "noutra":
                tokenQuebrado[0] = "em";
                tokenQuebrado[1] = "outra";
                break;

            case "dalguma":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "alguma";
                break;

            case "dalgum":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "algum";
                break;

            case "dalguém":
                tokenQuebrado[0] = "de";
                tokenQuebrado[1] = "alguém";
                break;

//            case "dali":
//                tokenQuebrado[0] = "";
//                tokenQuebrado[1] = "";
//                break;
//            case "dele":
//                tokenQuebrado[0] = "";
//                tokenQuebrado[1] = "";
//                break;
            default:
                return null;
        }
        return tokenQuebrado;
    }

    public String[] retornaPalavraComHifenQuebrada(String token) {
        String[] parts = token.split("-");
        if (parts.length > 1) {
            return parts;
        } else {
            return null;
        }

    }

    /*Esta função comentada ensina a usar o string args, DP e POS*/
//    public static void tagger() {
//    String modelPath = DependencyParser.DEFAULT_MODEL;
//    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
//
//    for (int argIndex = 0; argIndex < args.length; ) {
//      switch (args[argIndex]) {
//        case "-tagger":
//          taggerPath = args[argIndex + 1];
//          argIndex += 2;
//          break;
//        case "-model":
//          modelPath = args[argIndex + 1];
//          argIndex += 2;
//          break;
//        default:
//          throw new RuntimeException("Unknown argument " + args[argIndex]);
//      }
//    }
//
//    String text = "I can almost always tell when movies use fake dinosaurs.";
//
//    MaxentTagger tagger = new MaxentTagger(taggerPath);
//    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
//
//    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
//    for (List<HasWord> sentence : tokenizer) {
//      List<TaggedWord> tagged = tagger.tagSentence(sentence);
//      GrammaticalStructure gs = parser.predict(tagged);
//
//      // Print typed dependencies
//      log.info(gs);
//    }
//  }
//    public Boolean[] retornaVetorBooleanoDosTokensSemEspacoFinal(String[] tokens, String sentenca) {
//        Boolean[] tokensSemEspacoFinal = new Boolean[tokens.length];
//        Arrays.fill(tokensSemEspacoFinal, Boolean.FALSE);
//        /*Removendo espaços em branco em excesso por segurança*/
//        sentenca = sentenca.replaceAll("\\s+", " ");
//        char[] sentencaBrutaQuebradaPorEspaco = sentenca.toCharArray();
//        int indiceVetorCharVerificarEspacoBranco = 0;
//        for (int i = 0; i < tokens.length - 1; i++) {
//            indiceVetorCharVerificarEspacoBranco = indiceVetorCharVerificarEspacoBranco + tokens[i].length();
//            if(sentencaBrutaQuebradaPorEspaco[indiceVetorCharVerificarEspacoBranco] == ' '){
//                indiceVetorCharVerificarEspacoBranco = indiceVetorCharVerificarEspacoBranco + 1;
//            }else{
//                tokensSemEspacoFinal[i] = true;
//            }
//        }
//        return tokensSemEspacoFinal;
//    }
}
