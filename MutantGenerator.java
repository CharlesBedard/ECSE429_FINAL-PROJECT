import java.io.FileNotFoundException;
import java.util.*;
import java.io.*;

public class MutantGenerator {
    private static int plusMutants =0;
    private static int minusMutants =0;
    private static int multMutants =0;
    private static int divMutants =0;
    //vars to track number of mutants of each type generated


    public static void main(String[] args){
        String filename = "";
        Scanner sc;
        System.out.println("*****Starting mutant generator********");
	    System.out.println("~ please see README.txt for instructions ~");
        System.out.println("Enter file name of source code (must be same project directory): ");

        //Keep trying filenames gotten from scanner until you find a valid filename
        while(filename == "") {
            sc = new Scanner(System.in);
            filename = sc.nextLine();
            try {
                sc = new Scanner(new File(filename));
            } catch(FileNotFoundException fnfe){
                System.out.println("! ! ! Invalid File Name: \""+filename+"\" ! ! !");
                filename = ""; //Clear incorrect entry
                sc.close();
                System.out.println("Please try another file name: ");
            }finally{
                sc.close();//close scanner
            }
        }
        File mutantList = new File("mutantList.txt");
        if(mutantList.exists()){
            mutantList.delete();
        }
        System.out.println(">Now generating mutants for file: \""+filename+"\"");
        runMutantMaker(filename);
        System.out.println("****** Mutations complete! Please see mutantList.txt ******");
    }

    public static void runMutantMaker(String filename){
        //Go line by line in file
        //Check number of "?" operators in line, generate mutants for it
        //repeat for each "?" operator (in this case, ?: +,-,*,/)
        //once you reach EOF, write summary line to mutantList
        int codeLine = -1;
        String original = "";
        int opInstances = 0;

        try {
            Scanner sc = new Scanner(new File(filename));
            while(sc.hasNextLine()){
                codeLine++;
                original=sc.nextLine();
                //mutate +
                opInstances = original.length() - original.replace("+","").length();
                generateMutants(codeLine, original, "+", opInstances);
                //mutate -
                opInstances = original.length() - original.replace("-","").length();
                generateMutants(codeLine, original, "-", opInstances);
                //mutate *
                opInstances = original.length() - original.replace("*","").length();
                generateMutants(codeLine, original, "*", opInstances);
                //mutate /
                opInstances = original.length() - original.replace("/","").length();
                generateMutants(codeLine, original, "/", opInstances);
            }
        }catch(IOException runMM){
            System.out.println("Bad filename: runMM");
        }

        String summary = "Total \"+\" mutants created: "+plusMutants+"\n"
                +"Total \"-\" mutants created: "+minusMutants+"\n"
                +"Total \"*\" mutants created: "+multMutants+"\n"
                +"Total \"/\" mutants created: "+divMutants;
        try(FileWriter fw = new FileWriter("mutantList.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(summary);
        }catch(IOException io){
            System.out.println("writeToMutantList Error");
        }
    }

    public static void generateMutants(int codeLine, String original, String originalOp, int instances){
        //Given a line, the operator we want to replace, and how many instances of the operator
        // in the original line we want to replace,
        // generate the other 3 mutated lines (which also calls writeToMutantList to add it.)
        //Increases count of each mutant type generated
        String mutatedLine = "";
        if(instances == 0){
            return;
        }
        for(int i = 0; i<instances;i++) {//for every i<instances, replace the ith instance and add to list
            mutatedLine = "!!!Error - mutant generation failed to write to mutatedLine for original: \""+original+"\"\n" +
                    "Operator was: "+originalOp+" at instance "+i;
            if (originalOp.equals("+")) {
                makeMutant(codeLine, original, originalOp, i, "-");
                minusMutants++;
                makeMutant(codeLine, original, originalOp,i,"*");
                multMutants++;
                makeMutant(codeLine, original, originalOp,i,"/");
                divMutants++;

            } else if (originalOp.equals("-")) {
                makeMutant(codeLine, original, originalOp, i, "+");
                plusMutants++;
                makeMutant(codeLine, original, originalOp, i, "*");
                multMutants++;
                makeMutant(codeLine, original, originalOp, i, "/");
                divMutants++;
            } else if (originalOp.equals("*")) {
                makeMutant(codeLine, original, originalOp, i, "+");
                plusMutants++;
                makeMutant(codeLine, original, originalOp, i, "-");
                minusMutants++;
                makeMutant(codeLine, original, originalOp, i, "/");
                divMutants++;

            } else if (originalOp.equals("/")) {
                makeMutant(codeLine, original, originalOp, i, "+");
                plusMutants++;
                makeMutant(codeLine, original, originalOp, i, "-");
                minusMutants++;
                makeMutant(codeLine, original, originalOp, i, "*");
                multMutants++;

            } else {
                throw new IllegalArgumentException("Bad originalOp input");
            }
        }


    }//end of generate mutants

    public static String makeMutant(int codeLine, String original, String originalOp, int instance, String mutantOp){
        String mutant="";
        //String REGEX = "((?:[^"+originalOp+"]+\\+){"+instance+"})([^"+originalOp+"]+)\\+(.*)";
        String REGEX = "((?:[^"+originalOp+"]*\\"+originalOp+"){"+instance+"})([^"+originalOp+"]*)\\"+originalOp+"(.*)";
        mutant= original.replaceAll(REGEX,"$1$2"+mutantOp+"$3");
        writeToMutantList(codeLine, original, mutant, mutantOp);
        return mutant;
    }//end of MakeMutant

    public static void writeToMutantList(int codeLine, String original, String mutant, String mutantOp){
        String toAdd =
                "Original line of code:\n"+(codeLine+1)+"\n"+original+"\n"
                +"Mutated code:\n"+mutant+"\n"
                +"Mutant type: "+mutantOp+"\n";
        //File mutantFile = new File("mutantList.txt");
        try(FileWriter fw = new FileWriter("mutantList.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(toAdd);
        }catch(IOException io){
            System.out.println("writeToMutantList Error");
        }
    }//end of writeToMutantList



}
