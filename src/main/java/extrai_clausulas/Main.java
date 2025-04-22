package extrai_clausulas;
//
//import edu.stanford.nlp.parser.nndep.DependencyParser;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import pre_processamento.PreProcessamento;

public class Main {

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        String sentencesIn = "",
                dependencTreeIN = "";
        boolean CC = false,
                SC = false;
        int appositive = 0; /* 0 = não usa aposto
         1 = somente aposto
         2 = aposto + transitividade
         */

        for (int argIndex = 0; argIndex < args.length;) {
            switch (args[argIndex]) {
                case "-sentencesIN":
                    sentencesIn = args[argIndex + 1];
                    argIndex += 2;
                    break;
                case "-dependencyTreeIN":
                    dependencTreeIN = args[argIndex + 1];
                    argIndex += 2;
                    break;
                case "-CC":
                    if (args[argIndex + 1].equals("true")) {
                        CC = true;
                    }
                    argIndex += 2;
                    break;
                case "-SC":
                    if (args[argIndex + 1].equals("true")) {
                        SC = true;
                    }
                    argIndex += 2;
                    break;
                case "-appositive":
                    if (args[argIndex + 1].equals("0")) {
                        appositive = 0;
                    } else if (args[argIndex + 1].equals("1")) {
                        appositive = 1;
                    } else if (args[argIndex + 1].equals("2")) {
                        appositive = 2;
                    }
                    argIndex += 2;
                    break;
                default:
                    throw new RuntimeException("Unknown argument " + args[argIndex]);
            }
        }
        
        if(sentencesIn.equals("") && dependencTreeIN.equals("")){
            throw new RuntimeException("Could not find file with sentences");
        }

        int index = 0;
        Sentence carregaSentencasFormatoConll = new Sentence();
        ArrayList<Sentence> sentences = new ArrayList<>();
//        String caminhoTreebankFileIn = "C:\\Users\\Leandro\\Documents\\boilerpipe-master\\ExtraiClausulas\\saida\\saidaDP_testes.conll";
        String caminhoTreebankFileIn;

        //iniciar o contador do tempo total
        long startTime = System.nanoTime();

        if (dependencTreeIN.equals("")) {
            PreProcessamento preProcessamento = new PreProcessamento();

            // reinicia o contador do tempo total para desconsiderar o tempo de carregamento do modelo na memória
            startTime = System.nanoTime();

            /*Gera arquivo com setenças tokenizadas separadas por espaço. Cada linha do arquivo corresponde a uma sentença*/
            preProcessamento.retornaTokensSentencasSeparadoPorEspaco(sentencesIn);
            /*Gera arquivo conll para dar entrada ao Dependency parser*/
            preProcessamento.geraEntradaConllParaDependencyParser(preProcessamento.getCaminhoModeloPos(), preProcessamento.getArquivoSentencasTokenizadas());
            /*Executa Dependency Parser*/
            preProcessamento.dependencyParser(preProcessamento.getCaminhoModeloDP(), preProcessamento.getArquivoSentencasEntradaDPConll());
            caminhoTreebankFileIn = preProcessamento.getArquivoSaidaDP();
        } else {
            caminhoTreebankFileIn = dependencTreeIN;
        }

        carregaSentencasFormatoConll.loadData(caminhoTreebankFileIn, sentences);
        for (Sentence s : sentences) {
            s.mapeamentoSentences();
            sentences.get(index).setSentenca(s.getSentenca());
            index++;
        }

        extrairFatosParaCSV(sentences, CC, SC, appositive);

        extrairFatosParaJSON(sentences, CC, SC, appositive);

        long endTime   = System.nanoTime();
        double totalTime = (double) (endTime - startTime) / 1_000_000_000;
        System.out.println("Tempo de execução: " + totalTime + " segundos");
    }

    public void increment(AtomicInteger i) {
        i.set(i.get() + 1);
    }

    private static void extrairFatosParaJSON(ArrayList<Sentence> sentences, boolean CC, boolean SC, int appositive) throws IOException, CloneNotSupportedException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("output/extractedFactsByDpOIE.json"));
        JsonOutput jsonOutput = new JsonOutput();
        jsonOutput.appositive = appositive;
        jsonOutput.coordinatedConjunctions = CC;
        jsonOutput.subordinateClause = SC;

        for (int indiceSentenca = 0; indiceSentenca < sentences.size(); indiceSentenca++) {
            System.out.println("Índice sentença " + indiceSentenca);
            Extracao1 ex = new Extracao1(sentences.get(indiceSentenca));
            ex.realizaExtracao(CC, SC, appositive);
            ex.imprimeExtracoes();

            String sentenca = sentences.get(indiceSentenca).toString();
            String sujeito;
            String rel;
            String argumento2;
            int moduloSujeito;
            int moduloRelacao;
            int moduloArg2 = 0;
            int indiceExtracao = 1;

            ExtractedSentence extractedSentence = new ExtractedSentence();
            extractedSentence.sentenceId = indiceSentenca + 1;
            extractedSentence.sentence = sentenca;

            for (SujeitoRelacaoArgumentos sra : ex.getSujeitoRelacaoArgumentos()) {
                sujeito = sra.getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                rel = sra.getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
                moduloSujeito = sra.getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                moduloRelacao = sra.getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                for (int iCont = 0; iCont < sra.getArgumentos().size(); iCont++) {
                    Argumento arg2 = sra.getArgumentos().get(iCont);
                    argumento2 = arg2.toString().replace("\"", "\"\"");
                    moduloArg2 = arg2.getIdentificadorModuloExtracao();
                    if (arg2.getSraApontamentoCcompAdvcl() == null) {
                        Fact fact = new Fact();
                        fact.extractionId = indiceExtracao + ".0";
                        fact.arg1 = sujeito;
                        fact.rel = rel;
                        fact.arg2 = argumento2;
                        fact.coherence = "";
                        fact.minimalism = "";
                        fact.subjectModule = moduloSujeito;
                        fact.relationModule = moduloRelacao;
                        fact.arg2Module = moduloArg2;
                        extractedSentence.facts.add(fact);
                    }
                }
                int indiceSubExtracao = 0;

                for (Argumento arg2 : sra.getArgumentos()) {
                    if (arg2.getSraApontamentoCcompAdvcl() != null) {
                        Fact fact = new Fact();
                        fact.extractionId = indiceExtracao + "." + indiceSubExtracao++;
                        fact.arg1 = sujeito;
                        fact.rel = rel;
                        fact.arg2 = arg2.toString() + ex.retornaStringMarkSubExtracao(arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getSujeito());
                        fact.coherence = "";
                        fact.minimalism = "";
                        fact.subjectModule = moduloSujeito;
                        fact.relationModule = moduloRelacao;
                        fact.arg2Module = moduloArg2;
                        extractedSentence.facts.add(fact);
                        if (arg2.getSraApontamentoCcompAdvcl().getArgumentos().isEmpty()) {
                            Fact fact2 = new Fact();
                            fact2.extractionId = indiceExtracao + "." + indiceSubExtracao++;
                            fact2.arg1 = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                            fact2.rel = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
                            fact2.arg2 = "";
                            fact2.coherence = "x";
                            fact2.minimalism = "x";
                            fact2.subjectModule = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                            fact2.relationModule = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                            fact2.arg2Module = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                            extractedSentence.facts.add(fact2);
                        }
                    }
                }

                if (!sra.getArgumentos().isEmpty()) {
                    indiceExtracao++;
                }

            }

            jsonOutput.sentences.add(extractedSentence);

        }

        jsonOutput.exportToJson("output/extractedFactsByDpOIE.json");
    }

    private static void extrairFatosParaCSV(ArrayList<Sentence> sentences, boolean CC, boolean SC, int appositive) throws IOException, CloneNotSupportedException {

        Main classeMain = new Main();

        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream("output/extractedFactsByDpOIE.csv")/*,
         Charset.forName("UTF-8").newEncoder()*/
        );
        writer.append(" \"ID SENTENÇA\" ; \"SENTENÇA\" ; \"ID EXTRAÇÃO\" ; \"ARG1\" ; \"REL\" ; \"ARG2\" ; \"COERÊNCIA\" ; \"MINIMALIDADE\" ; \"MÓDULO SUJEITO\" ; \"MÓDULO RELAÇÃO\" ; \"MÓDULO ARG2\";" + System.lineSeparator());

        for (int indiceSentenca = 0; indiceSentenca < sentences.size(); indiceSentenca++) {
            System.out.println("Índice sentença " + indiceSentenca);
            Extracao1 ex = new Extracao1(sentences.get(indiceSentenca));
            ex.realizaExtracao(CC, SC, appositive);
            ex.imprimeExtracoes();

            String sentenca = sentences.get(indiceSentenca).toString();
            String sujeito;
            String rel;
            String argumento2;
            int moduloSujeito;
            int moduloRelacao;
            int moduloArg2;
            boolean flagIndicaSeImpressaoArquivoTemSentecaOuNao = false;
            int indiceExtracao = 1;

            for (SujeitoRelacaoArgumentos sra : ex.getSujeitoRelacaoArgumentos()) {
                sujeito = sra.getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                rel = sra.getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
                moduloSujeito = sra.getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                moduloRelacao = sra.getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                for (int iCont = 0; iCont < sra.getArgumentos().size(); iCont++) {
                    Argumento arg2 = sra.getArgumentos().get(iCont);
                    argumento2 = arg2.toString().replace("\"", "\"\"");
                    moduloArg2 = arg2.getIdentificadorModuloExtracao();
                    if (arg2.getSraApontamentoCcompAdvcl() == null) {
                        if (!flagIndicaSeImpressaoArquivoTemSentecaOuNao) {
                            writer.append("\"" + ((int) indiceSentenca + 1) + "\"" + ";" + "\"" + sentenca + "\"" + ";" + "\"" + indiceExtracao++ + ".0" + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + moduloSujeito + "\"" + ";" + "\"" + moduloRelacao + "\"" + ";" + "\"" + moduloArg2 + "\";" + System.lineSeparator());
                            //writer.append("" + (int) indiceSentenca + 1); writer.append(","); writer.append(sentenca); writer.append(","); writer.append("" + indiceExtracao++  + ".0"); writer.append(","); writer.append(sujeito); writer.append(""); writer.append(sentenca);
                            flagIndicaSeImpressaoArquivoTemSentecaOuNao = true;
                        } else {
                            writer.append("\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + indiceExtracao++ + ".0" + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + moduloSujeito + "\"" + ";" + "\"" + moduloRelacao + "\"" + ";" + "\"" + moduloArg2 + "\";" + System.lineSeparator());
                        }
                    }
                }
            }
            if (!flagIndicaSeImpressaoArquivoTemSentecaOuNao) {
                writer.append("\"" + ((int) indiceSentenca + 1) + "\"" + ";" + "\"" + sentenca + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + "" + "\";" + System.lineSeparator());
            }

            boolean flagIndicaIncremento = false;
            ArrayList<SraEArgumento> arrayExtracoes = new ArrayList<>();
            ArrayList<AtomicInteger> arrayIndices = new ArrayList<>();
            String indiceExtracoesQuebradas = "";
            //Essa string só serve pra adicionar o ".0" se for o elemento raiz
            String auxColocaPontoZero = "";
            String auxMarcaXSubExtracao = "";
            for (SujeitoRelacaoArgumentos sra : ex.getSujeitoRelacaoArgumentos()) {
                for (Argumento arg2 : sra.getArgumentos()) {
                    arrayExtracoes.clear();
                    arrayIndices.clear();
                    if (arg2.getSraApontamentoCcompAdvcl() != null) {
                        /*Array que armazena as extrações e subextrações para imprimir posteriormente. O primeiro elemento sempre
                         será a extração principal*/
                        SraEArgumento dadoArmazenado = new SraEArgumento(sra, arg2);
                        arrayExtracoes.add(dadoArmazenado);
                        if (arrayIndices.isEmpty()) {
                            arrayIndices.add(new AtomicInteger(indiceExtracao));
                        } else {
                            arrayIndices.add(new AtomicInteger(1));
                        }
                        for (Argumento argApontado : arg2.getSraApontamentoCcompAdvcl().getArgumentos()) {
                            int idSubExtracao = 1;
                            for (SraEArgumento elementoArrayExtracoes : arrayExtracoes) {
                                sujeito = elementoArrayExtracoes.getSra().getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                                rel = elementoArrayExtracoes.getSra().getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
//                                moduloSujeito = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
//                                moduloRelacao = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                                argumento2 = elementoArrayExtracoes.getArg().toString().replace("\"", "\"\"") + ex.retornaStringMarkSubExtracao(arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getSujeito());
//                                moduloArg2 = elementoArrayExtracoes.getArg().getIdentificadorModuloExtracao();
                                indiceExtracoesQuebradas = "" + arrayIndices.get(0);
                                for (int i = 1; i < arrayIndices.size(); i++) {
                                    int indice = arrayIndices.get(i).get();
                                    indiceExtracoesQuebradas = indiceExtracoesQuebradas + "." + indice;
                                }

                                /*Se o array só tem um elemento significa que é a extração principal/'raiz'*/
                                if (arrayIndices.size() == 1) {
                                    auxColocaPontoZero = ".0";
                                    moduloArg2 = 6;
                                    moduloSujeito = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                                    moduloRelacao = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                                    auxMarcaXSubExtracao = "";
                                } else {
                                    auxColocaPontoZero = "";
                                    moduloArg2 = 0;
                                    moduloSujeito = 0;
                                    moduloRelacao = 0;
                                    auxMarcaXSubExtracao = "x";
                                }

                                if (!flagIndicaSeImpressaoArquivoTemSentecaOuNao) {
                                    writer.append("\"" + ((int) indiceSentenca + 1) + "\"" + ";" + "\"" + sentenca + "\"" + ";" + "\"" + indiceExtracoesQuebradas + auxColocaPontoZero + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + moduloSujeito + "\"" + ";" + "\"" + moduloRelacao + "\"" + ";" + "\"" + moduloArg2 + "\";" + System.lineSeparator());
                                    flagIndicaSeImpressaoArquivoTemSentecaOuNao = true;
                                    flagIndicaIncremento = true;
                                } else {
                                    writer.append("\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + indiceExtracoesQuebradas + auxColocaPontoZero + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + moduloSujeito + "\"" + ";" + "\"" + moduloRelacao + "\"" + ";" + "\"" + moduloArg2 + "\";" + System.lineSeparator());
                                    flagIndicaIncremento = true;
                                }
                                if ((arrayIndices.size() - 1) == 0 && (flagIndicaIncremento)) {
                                    indiceExtracao++;
                                }
                                classeMain.increment(arrayIndices.get(arrayIndices.size() - 1));
                            }
//                            writer.append("SEPARA" + System.lineSeparator());
                            sujeito = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                            rel = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
                            moduloSujeito = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                            moduloRelacao = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                            argumento2 = argApontado.toString().replace("\"", "\"\"");
                            moduloArg2 = argApontado.getIdentificadorModuloExtracao();
                            writer.append("\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + indiceExtracoesQuebradas + "." + idSubExtracao++ + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\";" + System.lineSeparator());
                            flagIndicaIncremento = true;
                            if (argApontado.getSraApontamentoCcompAdvcl() != null) {
                                sra = arg2.getSraApontamentoCcompAdvcl();
                            }
                        }
                        //indiceExtracao++;

                        if (arg2.getSraApontamentoCcompAdvcl().getArgumentos().isEmpty()) {
                            int idSubExtracao = 1;
                            for (SraEArgumento elementoArrayExtracoes : arrayExtracoes) {
                                sujeito = elementoArrayExtracoes.getSra().getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                                rel = elementoArrayExtracoes.getSra().getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
                                moduloSujeito = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                                moduloRelacao = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                                argumento2 = elementoArrayExtracoes.getArg().toString().replace("\"", "\"\"") + ex.retornaStringMarkSubExtracao(arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getSujeito());
                                moduloArg2 = elementoArrayExtracoes.getArg().getIdentificadorModuloExtracao();
                                indiceExtracoesQuebradas = "" + arrayIndices.get(0);
                                for (int i = 1; i < arrayIndices.size(); i++) {
                                    int indice = arrayIndices.get(i).get();
                                    indiceExtracoesQuebradas = indiceExtracoesQuebradas + "." + indice;
                                }
                                /*Se o array só tem um elemento significa que é a extração principal/'raiz'*/
                                if (arrayIndices.size() == 1) {
                                    auxColocaPontoZero = ".0";
                                    moduloArg2 = 6;
                                    moduloSujeito = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                                    moduloRelacao = elementoArrayExtracoes.getSra().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                                    auxMarcaXSubExtracao = "";
                                } else {
                                    auxColocaPontoZero = "";
                                    moduloArg2 = 0;
                                    moduloSujeito = 0;
                                    moduloRelacao = 0;
                                    auxMarcaXSubExtracao = "x";
                                }
                                if (!flagIndicaSeImpressaoArquivoTemSentecaOuNao) {
                                    writer.append("\"" + ((int) indiceSentenca + 1) + "\"" + ";" + "\"" + sentenca + "\"" + ";" + "\"" + indiceExtracoesQuebradas + auxColocaPontoZero + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + moduloSujeito + "\"" + ";" + "\"" + moduloRelacao + "\"" + ";" + "\"" + moduloArg2 + "\";" + System.lineSeparator());
                                    flagIndicaSeImpressaoArquivoTemSentecaOuNao = true;
                                    flagIndicaIncremento = true;
                                } else {
                                    writer.append("\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + indiceExtracoesQuebradas + auxColocaPontoZero + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + auxMarcaXSubExtracao + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\";" + System.lineSeparator());
                                    flagIndicaIncremento = true;
                                }
                            }
//                            writer.append("SEPARA" + System.lineSeparator());
                            Argumento argVazio = new Argumento();
                            sujeito = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getStringSujeitoSemMarkInicio().replace("\"", "\"\"");
                            rel = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getStringRelacao().replace("\"", "\"\"");
                            moduloSujeito = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoSujeito();
                            moduloRelacao = arg2.getSraApontamentoCcompAdvcl().getSujeitoRelacao().getIdentificadorModuloExtracaoRelacao();
                            argumento2 = argVazio.toString().replace("\"", "\"\"");
                            moduloArg2 = argVazio.getIdentificadorModuloExtracao();
                            writer.append("\"" + "" + "\"" + ";" + "\"" + "" + "\"" + ";" + "\"" + indiceExtracao++ + "." + idSubExtracao++ + "\"" + ";" + "\"" + sujeito + "\"" + ";" + "\"" + rel + "\"" + ";" + "\"" + argumento2 + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + "x" + "\"" + ";" + "\"" + moduloSujeito + "\"" + ";" + "\"" + moduloRelacao + "\"" + ";" + "\"" + moduloArg2 + "\";" + System.lineSeparator());
                            flagIndicaIncremento = true;
                        }
//                        if (flagIndicaIncremento) {
//                            indiceExtracao++;
//                            flagIndicaIncremento = false;
//                        }
                    }
                }
                //arrayIndices.clear();
            }
            flagIndicaSeImpressaoArquivoTemSentecaOuNao = false;
        }

        writer.flush();
        writer.close();
    }

}

class SraEArgumento {

    SujeitoRelacaoArgumentos sra;
    Argumento arg;

    public SraEArgumento(SujeitoRelacaoArgumentos sra, Argumento arg) {
        this.sra = sra;
        this.arg = arg;
    }

    public SujeitoRelacaoArgumentos getSra() {
        return sra;
    }

    public void setSra(SujeitoRelacaoArgumentos sra) {
        this.sra = sra;
    }

    public Argumento getArg() {
        return arg;
    }

    public void setArg(Argumento arg) {
        this.arg = arg;
    }
}


class JsonOutput{

    boolean coordinatedConjunctions = false;
    boolean subordinateClause = false;
    int appositive = 0;
    ArrayList<ExtractedSentence> sentences = new ArrayList<>();

    public void exportToJson(String path) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path));
        writer.write("{\n");
        writer.write("  \"coordinatedConjunctions\": " + coordinatedConjunctions + ",\n");
        writer.write("  \"subordinateClause\": " + subordinateClause + ",\n");
        writer.write("  \"appositive\": " + appositive + ",\n");
        writer.write("  \"extractions\": [\n");
        for (int i = 0; i < sentences.size(); i++) {
            ExtractedSentence sentence = sentences.get(i);
            writer.write("    {\n");
            writer.write("      \"sentenceId\": " + sentence.sentenceId + ",\n");
            writer.write("      \"sentence\": \"" + sentence.sentence + "\",\n");
            writer.write("      \"facts\": [\n");
            for (int j = 0; j < sentence.facts.size(); j++) {
                Fact fact = sentence.facts.get(j);
                writer.write("        {\n");
                writer.write("          \"extractionId\": " + fact.extractionId + ",\n");
                writer.write("          \"arg1\": \"" + fact.arg1 + "\",\n");
                writer.write("          \"rel\": \"" + fact.rel + "\",\n");
                writer.write("          \"arg2\": \"" + fact.arg2 + "\",\n");
                writer.write("          \"coherence\": \"" + fact.coherence + "\",\n");
                writer.write("          \"minimalism\": \"" + fact.minimalism + "\",\n");
                writer.write("          \"subjectModule\": " + fact.subjectModule + ",\n");
                writer.write("          \"relationModule\": " + fact.relationModule + ",\n");
                writer.write("          \"arg2Module\": " + fact.arg2Module + "\n");
                writer.write("        }");
                if (j < sentence.facts.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("      ]\n");
            writer.write("    }");
            if (i < sentences.size() - 1) {
                writer.write(",");
            }
            writer.write("\n");
        }
        writer.write("  ]\n");
        writer.write("}\n");
        writer.flush();
        writer.close();
    }
}

class ExtractedSentence {
    int sentenceId;
    String sentence;
    ArrayList<Fact> facts = new ArrayList<>();
}

class Fact {
    String extractionId;
    String arg1;
    String rel;
    String arg2;
    String coherence;
    String minimalism;
    int subjectModule;
    int relationModule;
    int arg2Module;
}