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

import java.io.File;
import java.nio.file.Files;

/**
 *
 * @author Leandro
 */
public class Teste {
    public static void main(String [] Args) throws CloneNotSupportedException{
//        Token t1,t2;
//        t1 = new Token();
//        t2 = new Token();
//        t1.setForm("t1");
//        t2.setForm("t2");
//        SujeitoRelacao sr1, sr2;
//        sr1 = new SujeitoRelacao();
//        sr1.setTokenSujeitoFinalDeque(t1);
//        sr1.setTokenSujeitoFinalDeque(t1);
//        sr1.setTokenSujeitoFinalDeque(t1);
//        sr1.setTokenSujeitoFinalDeque(t1);
//        sr1.setTokenSujeitoFinalDeque(t1);
//        sr1.setIndiceNucleoSujeito(1);
//        sr2 = sr1.clone();
//        System.out.println("Tamanho array sujeito1: " + sr1.getSujeito().size());
//        System.out.println("Tamanho array sujeito2: " + sr2.getSujeito().size());
//        System.out.println("Índice sujeito1: " + sr1.getIndiceNucleoSujeito());
//        System.out.println("Índice sujeito2: " + sr2.getIndiceNucleoSujeito());
//        sr2.setIndiceNucleoSujeito(2);
//        sr2.setTokenSujeitoFinalDeque(t2);
//        sr2.setTokenSujeitoFinalDeque(t2);
//        sr2.setTokenSujeitoFinalDeque(t2);
//        sr2.setTokenSujeitoFinalDeque(t2);
//        sr2.setTokenSujeitoFinalDeque(t2);
//        System.out.println("Tamanho array sujeito1: " + sr1.getSujeito().size());
//        System.out.println("Tamanho array sujeito2: " + sr2.getSujeito().size());        
//        System.out.println("Índice sujeito1: " + sr1.getIndiceNucleoSujeito());
//        System.out.println("Índice sujeito2: " + sr2.getIndiceNucleoSujeito());
        String sentenca = "jljbkh jlbkjbk  jbkjbkhbk    kbhkhbkjbk";
        System.out.println(sentenca);
        sentenca = sentenca.replaceAll("\\s+", " ");
        System.out.println(sentenca);
    }
    public void t(Integer i){
        i = 10;
    }
}
