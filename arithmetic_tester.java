import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.print.attribute.ResolutionSyntax;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;


public class arithmetic_tester {
    private static AtomicInteger mutantNumber = new AtomicInteger(0);
    private static int maxThreads = 12;
    public static void main(String[] args) {
        // define input vectors and expected outputs
        String[] inputVectors = {"1 2 3", "-10 58 10", "2 2 2 3 3 3 4", "2 5 -9 50", "0 0 0"};

        // Get the number of mutant files that we will have to iterate over
        // Create a FileFilter 
        FileFilter filter = new FileFilter() { 
            public boolean accept(File f) 
            { 
                return f.getName().endsWith("java"); 
            } 
        }; 
        int maxMutants = new File("mutants").listFiles(filter).length;

        // Get the expected outputs from the original software
        String[] expectedOutputs = new String[inputVectors.length];
        for(int i=0;i<inputVectors.length;i++){
            expectedOutputs[i] = getExpectedOutput(inputVectors[i]);
        }


        int[][] inputVectorsResults = new int[maxMutants][inputVectors.length];

        // iterate over all the mutant files
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for(int i=0; i<maxThreads; i++){
            Thread thread = new Thread(() -> {
                // int threadID = thread_ID_count.getAndIncrement();
                int tempMutantNumber;
                while((tempMutantNumber = mutantNumber.getAndIncrement()) < maxMutants){
                    System.out.println("Running test vectors on mutant " + tempMutantNumber);
                    for(int j=0;j<inputVectors.length;j++){
                        boolean compileFailed = false;
                        int result = runMutant(tempMutantNumber,inputVectors[j],expectedOutputs[j]);
                        if(result == -2){
                            for(int k=0;k<inputVectors.length;k++){
                                inputVectorsResults[tempMutantNumber][k] = result;
                            }
                            compileFailed = true;
                        }
                        if(compileFailed){
                            compileFailed = false;
                            break;
                        } else {
                            inputVectorsResults[tempMutantNumber][j] = result;
                        }
                    }
                }
            });
            // add thread to list
            threads.add(thread);
            thread.start();
        }
        		// Iterate over all the threads
		for(int i = 0; i < maxThreads; i++) {
			try {
				// Wait for every thread to end
				threads.get(i).join();
			} catch (InterruptedException e) {
				System.out.println("Error during the thread JOIN.");
			}
		}

        prettyPrintTable(inputVectorsResults);
        greedyInputVectorSelect(inputVectors,inputVectorsResults);
        System.out.println("MUTANT COVERAGE: " + getCoverage(inputVectorsResults) + "%");
    }

    public static void prettyPrintTable(int[][] resultMatrix){
        System.out.print("Mutant number\t");
        for(int i=0;i<resultMatrix[0].length;i++){
            System.out.print("Vector "+i+"\t");
        }
        System.out.print("Compile\n");
        for(int i=0;i<resultMatrix.length;i++){
            System.out.print(i+"\t");
            for(int j=0;j<resultMatrix[0].length;j++){
                int vectorI = resultMatrix[i][j];
                System.out.print("\t\t\t");
                if(vectorI == -2){
                    // no compile
                    System.out.print("NA");
                    if(j == resultMatrix[0].length-1){
                        System.out.print("\t\t\tERROR");
                    }
                } else{
                    if(vectorI == -1){
                        System.out.print("E");
                    } else if(vectorI == 1){
                        System.out.print("P");
                    } else if(vectorI == 0){
                        System.out.print("F");
                    }
                    if(j == resultMatrix[0].length-1){
                        System.out.print("\t\t\tCOMPILED");
                    }
                }

            }
            System.out.print("\n");
        }
    }

    public static double getCoverage(int[][] resultMatrix){
        int mutantsKilled = 0;
        for(int i=0; i<resultMatrix.length;i++){
            boolean killed = false;
            for(int j=0;j<resultMatrix[0].length;j++){
                if(resultMatrix[i][j] != 1){
                    killed = true;
                }
            }
            if(killed){
                mutantsKilled++;
                killed = false;
            }
        }
        return ((double)mutantsKilled/(double)resultMatrix.length)*100;
    }

    public static void greedyInputVectorSelect(String[] inputs, int[][] resultMatrix){
        // reminder for resultMatrix values
        // 1 -> passed test
        // 0 -> failed test
        // -1 -> exception during test
        // -2 -> did not compile
        ArrayList<Integer> remainingMutants = getEmptyList(resultMatrix.length);
        // remove the mutants that cant compile 

        ArrayList<Integer> selectedVectorsIndices = new ArrayList<Integer>();
        for(int k=0;k<inputs.length;k++){
            int killsMostVectorIndex = 0;
            ArrayList<Integer> killedMutants = new ArrayList<Integer>();
            for(int i=0;i<inputs.length;i++){
                ArrayList<Integer> temp = killWhichMutants(i,resultMatrix,remainingMutants);
                if(temp.size() > killedMutants.size()){
                    killedMutants = temp;
                    killsMostVectorIndex = i;
                }
            }
            for(int j=0;j<remainingMutants.size();j++){
                for(int p=0;p<killedMutants.size();p++){
                    if(remainingMutants.get(j) == killedMutants.get(p)){
                        remainingMutants.remove(j);
                    }
                }
            }
            if(killedMutants.size() > 0){
                selectedVectorsIndices.add(killsMostVectorIndex);
            }
        }
        System.out.println("====================================");
        System.out.println("Printing the selected vectors");
        for(int i=0;i<selectedVectorsIndices.size();i++){
            System.out.println(inputs[selectedVectorsIndices.get(i)]);
        }
        System.out.println("Printing the mutants that can't be killed");
        System.out.println(remainingMutants);
        // System.out.println("compiled -> " + (resultMatrix.length-noCompile));
    }

    public static ArrayList<Integer> getEmptyList(int n) {
        ArrayList<Integer> a = new ArrayList<Integer>();
        for (int i = 0; i < n; ++i) {
            a.add(i);
        }
        return a;
    }

    public static ArrayList<Integer> killWhichMutants(int inputVectorIndex, int[][] resultMatrix, ArrayList<Integer> mutantsRemaining){
        ArrayList<Integer> mutantsKilledIndexList = new ArrayList<Integer>();
        for(int i=0;i<mutantsRemaining.size();i++){
            if(resultMatrix[mutantsRemaining.get(i)][inputVectorIndex] != 1){
                mutantsKilledIndexList.add(mutantsRemaining.get(i));
            }
        }
        return mutantsKilledIndexList;
    }


    public static int runMutant(int mutantNumber,String inputVector,String expectedOutput){
        // function running test on mutant, outputs false when tests fail, true when it succeeds
        try {
            // FIRST TRY TO COMPILE THE PROGRAM
            Process compileProcess = Runtime.getRuntime().exec("javac mutants/mutant"+Integer.toString(mutantNumber)+".java");
    
            StringBuilder output = new StringBuilder();
    
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(compileProcess.getInputStream()));
    
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
    
            int exitVal = compileProcess.waitFor();
            if (exitVal == 0) {
                // THE COMPILE WORKED, NOW TRY TO RUN
                try {

                    Process process = Runtime.getRuntime().exec("java -cp ./mutants mutant"+Integer.toString(mutantNumber)+" "+inputVector);
            
                    StringBuilder output2 = new StringBuilder();
            
                    BufferedReader reader2 = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
            
                    String line2;
                    while ((line2 = reader2.readLine()) != null) {
                        output2.append(line2);
                    }
            
                    int exitVal2 = process.waitFor();
                    if (exitVal2 == 0) {
                        if(output2.toString().compareTo(expectedOutput) == 0){
                            return 1;
                        } else {
                            return 0;
                        }
                    } else {
                        return -1;
                    }
            
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1;
                }
            } else {
                // THE COMPILE FAILED, EXIT
                return -2;
            }
    
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getExpectedOutput(String inputVector){
        // function running test on mutant, outputs false when tests fail, true when it succeeds
        try {
            // FIRST TRY TO COMPILE THE PROGRAM
            Process compileProcess = Runtime.getRuntime().exec("javac arithmetic_software.java");
    
            StringBuilder output = new StringBuilder();
    
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(compileProcess.getInputStream()));
    
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
    
            int exitVal = compileProcess.waitFor();
            if (exitVal == 0) {
                // THE COMPILE WORKED, NOW TRY TO RUN
                try {

                    Process process = Runtime.getRuntime().exec("java arithmetic_software "+inputVector);
            
                    StringBuilder output2 = new StringBuilder();
            
                    BufferedReader reader2 = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
            
                    String line2;
                    while ((line2 = reader2.readLine()) != null) {
                        output2.append(line2);
                    }
            
                    int exitVal2 = process.waitFor();
                    if (exitVal2 == 0) {
                        return output2.toString();

                    } else {
                        return "";
                    }
            
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "";
                }
            } else {
                // THE COMPILE FAILED, EXIT
                return "";
            }
    
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }
    
}