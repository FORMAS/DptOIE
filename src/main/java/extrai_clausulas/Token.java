package extrai_clausulas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Token extends Object implements Cloneable {
    
    private int id;
    private String form;
    private String lemma;
    private String cpostag;
    private String postag;
    private String feats;
    private int head;// índice do token pai
    private String deprel;
    private String phead;
    private String pdeprel;    
    private ArrayList<Token> tokensFilhos = new ArrayList<>(8);

    public void addTokenFilho(Token t){
        this.tokensFilhos.add(t);
    }

    public ArrayList<Token> getTokensFilhos() {
        return tokensFilhos;
    }

    public int getId() {
        return id;
    }

    public String getForm() {
        return form;
    }

    public String getLemma() {
        return lemma;
    }

    public String getCpostag() {
        return cpostag;
    }

    public String getPostag() {
        return postag;
    }

    public String getFeats() {
        return feats;
    }

    public int getHead() {
        return head;
    }

    public String getDeprel() {
        return deprel;
    }

    public String getPhead() {
        return phead;
    }

    public String getPdeprel() {
        return pdeprel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public void setCpostag(String cpostag) {
        this.cpostag = cpostag;
    }

    public void setPostag(String postag) {
        this.postag = postag;
    }

    public void setFeats(String feats) {
        this.feats = feats;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public void setDeprel(String deprel) {
        this.deprel = deprel;
    }

    public void setPhead(String phead) {
        this.phead = phead;
    }

    public void setPdeprel(String pdeprel) {
        this.pdeprel = pdeprel;
    }

    public void setTokensFilhos(ArrayList<Token> tokensFilhos) {
        this.tokensFilhos = tokensFilhos;
    }
    
    public Token retornaClone() {
            try {
                return (Token) this.clone();
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    
}
