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
package extrai_clausulas;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

/**
 *
 * @author Leandro
 */
public class Argumento extends Object implements Cloneable {

    private Stack<Deque<Token>> clausulas;
    private int idExtracao;
    private SujeitoRelacaoArgumentos sraApontamentoCcompAdvcl;
    private int identificadorModuloExtracao;

    public Argumento() {
        this.clausulas = new Stack<>();
        this.idExtracao = 0;
        this.sraApontamentoCcompAdvcl = null;
        this.identificadorModuloExtracao = 0;
    }

    public void setClausulaArrayClausulas(Deque<Token> clausula) {
        this.clausulas.push(clausula);
    }

    public Stack<Deque<Token>> getClausulas() {
        return clausulas;
    }

    public void removeClausulaPilha() {
        this.clausulas.pop();
    }

    public void setClausulas(Stack<Deque<Token>> clausulas) {
        this.clausulas = clausulas;
    }

    public int getIdExtracao() {
        return idExtracao;
    }

    public void setIdExtracao(int idExtracao) {
        this.idExtracao = idExtracao;
    }

    public SujeitoRelacaoArgumentos getSraApontamentoCcompAdvcl() {
        return sraApontamentoCcompAdvcl;
    }

    public void setSraApontamentoCcompAdvcl(SujeitoRelacaoArgumentos sraApontamentoCcompAdvcl) {
        this.sraApontamentoCcompAdvcl = sraApontamentoCcompAdvcl;
    }

    public int getIdentificadorModuloExtracao() {
        return identificadorModuloExtracao;
    }

    public void setIdentificadorModuloExtracao(int identificadorModuloExtracao) {
        this.identificadorModuloExtracao = identificadorModuloExtracao;
    }

    public boolean comparaConteudoArgumentos(Argumento arg) {
        String argumentoThis = this.toString();
        String argumentoASerComparado = arg.toString();
        if (argumentoThis.equals(argumentoASerComparado)) {
            if ((this.getSraApontamentoCcompAdvcl() == null) && (arg.getSraApontamentoCcompAdvcl() == null)) {
                return true;
            } else if ((this.getSraApontamentoCcompAdvcl() != null) && (arg.getSraApontamentoCcompAdvcl() != null)) {
                if (this.getSraApontamentoCcompAdvcl().equals(arg.getSraApontamentoCcompAdvcl())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        String argumento = "";
        for (Deque<Token> dequeToken : this.clausulas) {
            if (dequeToken != null) {
                for (Token t : dequeToken) {
                    argumento = argumento + t.getForm() + " ";
                }
            }
        }
        return argumento;
    }

    public Argumento retornaClone() {
        Argumento argumentoClonado = new Argumento();
        for (Deque<Token> dequeClausula : this.clausulas) {
            Deque<Token> dequeAux = new ArrayDeque<>();
            if (dequeClausula != null) {
                for (Token tokenClausula : dequeClausula) {
                    dequeAux.add(tokenClausula.retornaClone());
                }
            }
            argumentoClonado.clausulas.push(dequeAux);
        }
        if (this.sraApontamentoCcompAdvcl != null) {
            argumentoClonado.sraApontamentoCcompAdvcl = this.sraApontamentoCcompAdvcl.retornaClone();
        } else {
            argumentoClonado.sraApontamentoCcompAdvcl = null;
        }
        argumentoClonado.idExtracao = 0;
        argumentoClonado.setIdentificadorModuloExtracao(this.identificadorModuloExtracao);
        return argumentoClonado;
    }
}
