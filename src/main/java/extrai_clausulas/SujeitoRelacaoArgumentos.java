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

import java.util.ArrayList;
import java.util.Deque;

/**
 *
 * @author Leandro
 */
public class SujeitoRelacaoArgumentos {

    private SujeitoRelacao sujeitoRelacao;
    private ArrayList<Argumento> argumentos;

    public SujeitoRelacaoArgumentos(SujeitoRelacao sujeitoRelacao) {
        this.sujeitoRelacao = sujeitoRelacao;
        this.argumentos = new ArrayList<>();
    }

    public SujeitoRelacaoArgumentos() {
        this.sujeitoRelacao = new SujeitoRelacao();
        this.argumentos = new ArrayList<>();
    }

    public SujeitoRelacao getSujeitoRelacao() {
        return sujeitoRelacao;
    }

    public void setSujeitoRelacao(SujeitoRelacao sujeitoRelacao) {
        this.sujeitoRelacao = sujeitoRelacao;
    }

    public ArrayList<Argumento> getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(ArrayList<Argumento> argumentos) {
        this.argumentos = argumentos;
    }

    public void addArg2(Argumento argumento) {
        this.argumentos.add(argumento);
    }

    public void setCopiaClausulaArgumentos(ArrayList<Argumento> argumentos) {
        this.argumentos = argumentos;
    }

    public SujeitoRelacaoArgumentos retornaClone() {
        SujeitoRelacaoArgumentos sra = new SujeitoRelacaoArgumentos();
        sra.setSujeitoRelacao(this.sujeitoRelacao.retornaClone());
        for (Argumento arg : this.argumentos) {
            sra.argumentos.add(arg.retornaClone());
        }
        return sra;
    }

    public String getStringArgumento(int indiceArgumentoDesejado) {
        String argumento = "";
            for (Deque<Token> dequeToken : this.getArgumentos().get(indiceArgumentoDesejado).getClausulas()) {
                if (dequeToken != null) {
                    for (Token t : dequeToken) {
                        argumento = argumento + " " + t.getForm();
                    }
                }
            }
        return argumento;
    }

}
