/*
 * Copyright 2017 Christian Kohlschütter.
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
package extrai_clausulas;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 *
 * @author Leandro
 */
public class SujeitoRelacao extends Object implements Cloneable {

    private Deque<Token> sujeito;
    private Deque<Token> relacao;
    private int indiceNucleoSujeito;
    private int indiceNucleoRelacao;
    private int identificadorModuloExtracaoSujeito;
    private int identificadorModuloExtracaoRelacao;
    private Boolean[] vetorBooleanoTokensSujeitoVisitados;
    private Boolean[] vetorBooleanoTokensRelacaoVisitados;

    public SujeitoRelacao() {
        this.sujeito = new ArrayDeque<>();
        this.relacao = new ArrayDeque<>();
        identificadorModuloExtracaoSujeito = 0;
        identificadorModuloExtracaoRelacao = 0;
        this.vetorBooleanoTokensSujeitoVisitados = new Boolean[0];
        this.vetorBooleanoTokensRelacaoVisitados = new Boolean[0];
    }

    public Boolean[] getVetorBooleanoTokensSujeitoVisitados() {
        return vetorBooleanoTokensSujeitoVisitados;
    }

    public void setVetorBooleanoTokensSujeitoVisitados(Boolean[] vetorBooleanoTokensSujeitoVisitados) {
        this.vetorBooleanoTokensSujeitoVisitados = vetorBooleanoTokensSujeitoVisitados;
    }

    public Boolean[] getVetorBooleanoTokensRelacaoVisitados() {
        return vetorBooleanoTokensRelacaoVisitados;
    }

    public void setVetorBooleanoTokensRelacaoVisitados(Boolean[] vetorBooleanoTokensRelacaoVisitados) {
        this.vetorBooleanoTokensRelacaoVisitados = vetorBooleanoTokensRelacaoVisitados;
    }

    public Deque<Token> getSujeito() {
        return sujeito;
    }

    public void setSujeito(Deque<Token> sujeito) {
        this.sujeito = sujeito;
    }

    public Deque<Token> getRelacao() {
        return relacao;
    }

    public void setRelacao(Deque<Token> relacao) {
        this.relacao = relacao;
    }

    public int getIndiceNucleoSujeito() {
        return indiceNucleoSujeito;
    }

    public void setIndiceNucleoSujeito(int indiceNucleoSujeito) {
        this.indiceNucleoSujeito = indiceNucleoSujeito;
    }

    public int getIndiceNucleoRelacao() {
        return indiceNucleoRelacao;
    }

    public void setIndiceNucleoRelacao(int indiceNucleoRelacao) {
        this.indiceNucleoRelacao = indiceNucleoRelacao;
    }

    public void setTokenSujeitoInicioDeque(Token t) {
        this.sujeito.addFirst(t);
    }

    public void setTokenSujeitoFinalDeque(Token t) {
        this.sujeito.addLast(t);
    }

    public void setTokenRelacaoInicioDeque(Token t) {
        this.relacao.addFirst(t);
    }

    public void setTokenRelacaoFinalDeque(Token t) {
        this.relacao.addLast(t);
    }

    public int getIdentificadorModuloExtracaoSujeito() {
        return identificadorModuloExtracaoSujeito;
    }

    public void setIdentificadorModuloExtracaoSujeito(int identificadorModuloExtracaoSujeito) {
        this.identificadorModuloExtracaoSujeito = identificadorModuloExtracaoSujeito;
    }

    public int getIdentificadorModuloExtracaoRelacao() {
        return identificadorModuloExtracaoRelacao;
    }

    public void setIdentificadorModuloExtracaoRelacao(int identificadorModuloExtracaoRelacao) {
        this.identificadorModuloExtracaoRelacao = identificadorModuloExtracaoRelacao;
    }

    public void resetaDequeSujeito() {
        this.sujeito.clear();
    }

    public void resetaDequeRelacao() {
        this.relacao.clear();
    }

    /*Essa função é útil quando for imprimir na tela o sujeito com o MARK. Ela é importante quando for utilizar com as
     subextrações*/
    public String getStringSujeitoCompleto() {
        String sujeito = "";
        for (Token t : this.sujeito) {
            sujeito = sujeito + t.getForm() + " ";
        }
        return sujeito;
    }

    /*Essa função é útil quando for imprimir na tela o sujeito sem a necessidade do MARK e seus filhos.*/
    public String getStringSujeitoSemMarkInicio() {
        String sujeito = "";
        Token tokenMark = null;
        boolean flagIndicaMark = false;
        for (Token tokenSujeito : this.sujeito) {
            if (!(tokenSujeito.getDeprel().equals("mark") || (tokenMark != null && tokenMark.getId() == tokenSujeito.getHead()))) {
                flagIndicaMark = true;
                sujeito = sujeito + tokenSujeito.getForm() + " ";
            } else {
                if (flagIndicaMark) {
                    sujeito = sujeito + tokenSujeito.getForm() + " ";
                }else{
                    tokenMark = tokenSujeito;
                }
            }
        }
        return sujeito;
    }

    public String getStringRelacao() {
        String relacao = "";
        for (Token tokenRelacao : this.relacao) {
            relacao = relacao + " " + tokenRelacao.getForm();
        }
        return relacao;
    }
    
    public boolean comparaConteudoSujetoRelacao(SujeitoRelacao sr){
        String sujeitoThis = this.getStringSujeitoCompleto();
        String sujeitoASerComparado = sr.getStringSujeitoCompleto();
        String relacaoThis = this.getStringRelacao();
        String relacaoASerComparada = sr.getStringRelacao();
        if( sujeitoThis.equals(sujeitoASerComparado) && relacaoThis.equals(relacaoASerComparada) ){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.sujeito);
        hash = 47 * hash + Objects.hashCode(this.relacao);
        return hash;
    }

    @Override
    public boolean equals(Object sr) {
        Token[] tokensSujeitoThis = this.sujeito.toArray(new Token[0]);
        Token[] tokensSujeitoObject = ((SujeitoRelacao) sr).getSujeito().toArray(new Token[0]);
        Token[] tokensRelacaoThis = this.relacao.toArray(new Token[0]);
        Token[] tokensRelacaoObject = ((SujeitoRelacao) sr).getRelacao().toArray(new Token[0]);

        if (sr == null) {
            return false;
        }
        if (getClass() != sr.getClass()) {
            return false;
        }

        if (sr == null) {
            return false;
        }

        if ((tokensSujeitoThis != null && tokensSujeitoObject != null) && (tokensSujeitoThis.length == tokensSujeitoObject.length) /*&& (tokensSujeitoThis.length > 0)*/) {
            for (int i = 0; i < tokensSujeitoThis.length; i++) {
                if (!(tokensSujeitoThis[i].getForm().equals(tokensSujeitoObject[i].getForm()))) {
//                    System.out.println("RETORNOU FALSE NO SUJEITO1 - form1 " + tokensSujeitoThis[i].getForm() + " form2 " + tokensRelacaoObject[i].getForm());
                    return false;
                }
            }
        } else {
            return false;
        }

        if ((tokensRelacaoThis != null && tokensRelacaoObject != null) && (tokensRelacaoThis.length == tokensRelacaoObject.length) /*&& (tokensRelacaoThis.length > 0)*/) {
            for (int i = 0; i < tokensRelacaoThis.length; i++) {
                if (!(tokensRelacaoThis[i].getForm().equals(tokensRelacaoObject[i].getForm()))) {
//                    System.out.println("RETORNOU FALSE NO SUJEITO1 - form1 " + tokensRelacaoThis[i].getForm() + " form2 " + tokensRelacaoObject[i].getForm());
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    //Esta função clone não faz o clone dos arrays
//    @Override
//    public SujeitoRelacao clone() throws CloneNotSupportedException {
//        return (SujeitoRelacao) super.clone();
//    }
    public SujeitoRelacao retornaClone() {
        SujeitoRelacao sr = new SujeitoRelacao();
        Deque<Token> sujeito = new ArrayDeque<>();
        Deque<Token> relacao = new ArrayDeque<>();
        Boolean[] vetorBooleanoTokensSujeitoVisitadosCopia = new Boolean[this.vetorBooleanoTokensSujeitoVisitados.length];
        Boolean[] vetorBooleanoTokensRelacaoVisitadosCopia = new Boolean[this.vetorBooleanoTokensRelacaoVisitados.length];
        System.arraycopy(this.vetorBooleanoTokensSujeitoVisitados, 0, vetorBooleanoTokensSujeitoVisitadosCopia, 0, this.vetorBooleanoTokensSujeitoVisitados.length);
        System.arraycopy(this.vetorBooleanoTokensRelacaoVisitados, 0, vetorBooleanoTokensRelacaoVisitadosCopia, 0, this.vetorBooleanoTokensRelacaoVisitados.length);

        for (Token t : this.sujeito) {
            sujeito.add(t.retornaClone());
        }
        for (Token t : this.relacao) {
            relacao.add(t.retornaClone());
        }

        sr.setSujeito(sujeito);
        sr.setRelacao(relacao);
        sr.setIndiceNucleoSujeito(this.indiceNucleoSujeito);
        sr.setIndiceNucleoRelacao(this.indiceNucleoRelacao);
        sr.setIdentificadorModuloExtracaoSujeito(this.identificadorModuloExtracaoSujeito);
        sr.setIdentificadorModuloExtracaoRelacao(this.identificadorModuloExtracaoRelacao);
        sr.setVetorBooleanoTokensSujeitoVisitados(vetorBooleanoTokensSujeitoVisitadosCopia);
        sr.setVetorBooleanoTokensRelacaoVisitados(vetorBooleanoTokensRelacaoVisitadosCopia);

        return sr;
    }

}
