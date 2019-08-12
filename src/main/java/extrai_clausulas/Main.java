package extrai_clausulas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        int index = 0;
        Main classeMain = new Main();
        Sentence carregaSentencasFormatoConll = new Sentence();
        ArrayList<Sentence> sentences = new ArrayList<>(1000);
//        String caminhoTreebankFileIn = "C:\\Users\\Leandro\\Desktop\\arquivos_treino_maltparser_datasets\\sentencas_teste_dependentIE2.0";
        //String caminhoTreebankFileIn = "C:\\Users\\Leandro\\Desktop\\arquivos_treino_maltparser_datasets\\Treebank 2.0 formatado para dependency viewer.conllu.conllu";
//        String caminhoTreebankFileIn = "C:\\Users\\Leandro\\Documents\\boilerpipe-master\\ExtraiClausulas\\saida\\ceten-200.conll";
        String caminhoTreebankFileIn = "C:\\Users\\Leandro\\Documents\\boilerpipe-master\\ExtraiClausulas\\saida\\saidaDP_testes.conll";
        carregaSentencasFormatoConll.loadData(caminhoTreebankFileIn, sentences);
        for (Sentence s : sentences) {
            s.mapeamentoSentences();
            sentences.get(index).setSentenca(s.getSentenca());
            index++;
        }
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream("extrações_triplas_DependentIE++_arte_todos_modulos.csv")/*,
         Charset.forName("UTF-8").newEncoder()*/
        );
        writer.append(" \"ID SENTENÇA\" ; \"SENTENÇA\" ; \"ID EXTRAÇÃO\" ; \"ARG1\" ; \"REL\" ; \"ARG2\" ; \"COERÊNCIA\" ; \"MINIMALIDADE\" ; \"MÓDULO SUJEITO\" ; \"MÓDULO RELAÇÃO\" ; \"MÓDULO ARG2\";" + System.lineSeparator());

        for (int indiceSentenca = 0; indiceSentenca <= 10508; indiceSentenca++) {
            System.out.println("Índice sentença " + indiceSentenca);
            Extracao1 ex = new Extracao1(sentences.get(indiceSentenca));
            ex.realizaExtracao();
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
                                if((arrayIndices.size() - 1) == 0 && (flagIndicaIncremento)){
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

    public void increment(AtomicInteger i) {
        i.set(i.get() + 1);
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
