package extrai_clausulas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import pre_processamento.PreProcessamento;

//Treebank no formato conll
public class Sentence {
    /*Características de cada sentença*/

    private ArrayList<Token> sentenca;

    public Sentence() {

    }

    public Sentence(ArrayList<Token> sentenca) {
        this.sentenca = sentenca;
    }

    //Esta função faz o mapeamento dos filhos de cada token
    public void mapeamentoSentences() {
        for (Token t : this.sentenca) {
            if (t.getId() != 0) {
                this.sentenca.get(t.getHead()).addTokenFilho(t);
            }
        }
    }

    //Função que carrega os dados no formato conll
    public ArrayList<Sentence> loadData(String pathFile, ArrayList<Sentence> sentences) throws IOException {
        String root = "0	root	root	root	root	root	0	root	root	root";
        String[] rowRoot = root.split("\t");
        ArrayList<Token> arrayTokens = new ArrayList<>(60);
        Token tokenAux;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathFile), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            if (line.contentEquals("")) {
                Sentence sentenceAux = new Sentence(arrayTokens);
                sentences.add(sentenceAux);
                arrayTokens = new ArrayList<>(50);
            } else {
                tokenAux = new Token();
                String[] row = line.split("\t");
                if (row[0].equals("1")) {//Verifica se a linha tem id igual a um para adicionar o root na primeira linha
//                    if (!(row[0].contains("-"))) {
                    tokenAux.setId(Integer.valueOf(rowRoot[0]));
                    tokenAux.setForm(rowRoot[1]);
                    tokenAux.setLemma(rowRoot[2]);
                    tokenAux.setCpostag(rowRoot[3]);
                    tokenAux.setPostag(rowRoot[4]);
                    tokenAux.setFeats(rowRoot[5]);
                    tokenAux.setHead(Integer.valueOf(rowRoot[6]));
                    tokenAux.setDeprel(rowRoot[7]);
                    tokenAux.setPhead(rowRoot[8]);
                    tokenAux.setPdeprel(rowRoot[9]);
                    arrayTokens.add(tokenAux);
                    tokenAux = new Token();
//                    }
                }
//                if (!(row[0].contains("-"))) {
                tokenAux.setId(Integer.valueOf(row[0]));
                tokenAux.setForm(row[1]);
                tokenAux.setLemma(row[2]);
                tokenAux.setCpostag(row[3]);
                tokenAux.setPostag(row[4]);
                tokenAux.setFeats(row[5]);
                tokenAux.setHead(Integer.valueOf(row[6]));
                tokenAux.setDeprel(row[7]);
                tokenAux.setPhead(row[8]);
                tokenAux.setPdeprel(row[9]);
                arrayTokens.add(tokenAux);
//                }
            }
        }
        Sentence sentenceAux = new Sentence(arrayTokens);
        sentences.add(sentenceAux);

        /*No pre-processamento quando o DP é executado o Upos e Xpos ficam iguais de novo. Esse trecho do código é 
         responsável por fazer a alteração necessária*/
        for (Sentence sentence : sentences) {
            for (Token t : sentence.getSentenca()) {
//                System.out.println(t.getForm() + " " + p.mapeamentoTokenPosTagger(t.getCpostag()));
                t.setCpostag(PreProcessamento.mapeamentoTokenPosTagger(t.getCpostag()));
            }
        }

        return sentences;
    }

    public int getTamanhoSentenca() {
        return sentenca.size();
    }

    public ArrayList<Token> getSentenca() {
        return sentenca;
    }

    public void setSentenca(ArrayList<Token> sentenca) {
        this.sentenca = sentenca;
    }

    public void addTokenSinteticoSentenca(Token tokenSintetico) {
        this.sentenca.add(tokenSintetico);
    }

    public String toString() {
        String sentenca = "";
        for (Token t : this.sentenca) {
            if (t.getId() != 0) {
                sentenca = sentenca + t.getForm() + " ";
            }
        }
        return sentenca;
    }

}
