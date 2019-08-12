package extrai_clausulas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Leandro
 */
public class SimplificaTriplasArg {

    public static void main(String[] args) throws IOException {
        String s = "[TRIPLA -> 4] A primeira edição da Olimpíada&Gay	ocorreu em	1982";
        String[] a = s.split("\t");
        for (String a1 : a) {
            System.out.println(a1);
        }
        SimplificaTriplasArg sh = new SimplificaTriplasArg();
        sh.consertaTriplasArg();
    }

    public void consertaTriplasArg() throws IOException {
        String pathFileOut = "C:\\Users\\Leandro\\Desktop\\ArgOE\\saida_wikipedia_ArgOE_formatado.csv";
        String pathFileIn = "C:\\Users\\Leandro\\Desktop\\ArgOE\\saida_wikipedia_ArgOE_formatado.txt";
        //String pathFile = "C:\\Users\\Leandro\\Desktop\\ArgOE\\saida_wikipedia_ArgOE.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathFileIn), "UTF-8"));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(pathFileOut));
        try (FileWriter writer = new FileWriter(pathFileOut)) {
            //        BufferedWriter writer = new BufferedWriter(new FileWriter(pathFileOut));
            String line = "";
            boolean flag = true;
//        int contador = 1;
//        while ((line = br.readLine()) != null) {
//            if ((line.contains("sentence\t"))) {
//                writer.write("\n");
//                writer.write(line+"\n");
//            }
//            if (line.contains("t-extraction")) {
//                str1 = line.replaceAll("\t[0-9]+\t", "[TRIPLA -> " + contador + "] ");
//                str2 = str1.replace("t-extraction", "");
//                writer.write(str2+"\n");
//                contador++;
//                str2.ma
//            }
//        }
            while ((line = br.readLine()) != null) {
                if ((line.contains("sentence\t"))) {
//                    String str1 = line.replaceAll("sentence\t[0-9]+\t", "");
                    writer.append("\n\"" + line + "\";");
                    flag = true;
                }
                if (line.contains("[TRIPLA ->")) {
//                    String str1 = line.replaceAll("[TRIPLA -> [0-9]+]", "");
//                    System.out.println(str1);
                    if (flag) {
                        String tripla[] = line.split("\t");
                        writer.append("\"" + tripla[0] + "\";");
                        writer.append("\"" + tripla[1] + "\";");
                        writer.append("\"" + tripla[2] + "\";");
                        writer.append("\"\";");//accurate
                        writer.append("\"\";\n");//minimal
                        flag = false;
                    } else {
                        String tripla[] = line.split("\t");
                        writer.append("\"\";");
                        writer.append("\"" + tripla[0] + "\";");
                        writer.append("\"" + tripla[1] + "\";");
                        writer.append("\"" + tripla[2] + "\";");
                        writer.append("\"\";");//accurate
                        writer.append("\"\";\n");//minimal
                    }
                }
            }
            writer.flush();
            writer.close();
        }
    }

    public void padronizarSentencasArg() throws IOException {
        String pathFileOut = "C:\\Users\\Leandro\\Desktop\\ArgOE\\arquivo_padrao_ArgOE_centenfolha.txt";
        String pathFileIn = "C:\\Users\\Leandro\\Desktop\\ArgOE\\arquivo_tokenizado_centenfolha.txt";
        //String pathFile = "C:\\Users\\Leandro\\Desktop\\ArgOE\\saida_wikipedia_ArgOE.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathFileIn), "UTF-8"));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(pathFileOut));
        try (FileWriter writer = new FileWriter(pathFileOut)) {
            //        BufferedWriter writer = new BufferedWriter(new FileWriter(pathFileOut));
            String line = "";
//        int contador = 1;
//        while ((line = br.readLine()) != null) {
//            if ((line.contains("sentence\t"))) {
//                writer.write("\n");
//                writer.write(line+"\n");
//            }
//            if (line.contains("t-extraction")) {
//                str1 = line.replaceAll("\t[0-9]+\t", "[TRIPLA -> " + contador + "] ");
//                str2 = str1.replace("t-extraction", "");
//                writer.write(str2+"\n");
//                contador++;
//                str2.ma
//            }
//        }
            int cont = 1;
            while ((line = br.readLine()) != null) {
                writer.append(cont + "\t" + line + "\n");
                cont++;
            }
            writer.flush();
            writer.close();
        }
    }
}
