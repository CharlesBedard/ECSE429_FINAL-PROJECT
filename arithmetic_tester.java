import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;

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


        boolean[][] inputVectorsResults = new boolean[maxMutants][inputVectors.length];

        // iterate over all the mutant files
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for(int i=0; i<maxThreads; i++){
            Thread thread = new Thread(() -> {
                // int threadID = thread_ID_count.getAndIncrement();
                int tempMutantNumber;
                while((tempMutantNumber = mutantNumber.getAndIncrement()) < maxMutants){
                    System.out.println("Running test vectors on mutant " + tempMutantNumber);
                    for(int j=0;j<inputVectors.length;j++){
                        inputVectorsResults[tempMutantNumber][j] = runMutant(tempMutantNumber,inputVectors[j],expectedOutputs[j]);
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

        prettyPrintBigTable(inputVectorsResults);
        System.out.println("MUTANT COVERAGE: " + getCoverage(inputVectorsResults) + "%");
    }

    public static void prettyPrintBigTable(boolean[][] resultMatrix){
        System.out.print("Mutant number\t");
        for(int i=0;i<resultMatrix[0].length;i++){
            System.out.print("Vector "+i+"\t");
        }
        System.out.print("\n");
        for(int i=0;i<resultMatrix.length;i++){
            System.out.print(i+"\t\t\t\t"+resultMatrix[i][0]);
            for(int j=1;j<resultMatrix[0].length;j++){
                System.out.print("\t\t"+resultMatrix[i][j]);
            }
            System.out.print("\n");
        }
    }

    public static double getCoverage(boolean[][] resultMatrix){
        int mutantsKilled = 0;
        for(int i=0; i<resultMatrix.length;i++){
            boolean temp = true;
            for(int j=0;j<resultMatrix[0].length;j++){
                temp = (temp&resultMatrix[i][j]);
            }
            if(!temp){
                mutantsKilled++;
            }
        }
        return ((double)mutantsKilled/(double)resultMatrix.length)*100;
    }

    public static boolean runMutant(int mutantNumber,String inputVector,String expectedOutput){
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
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
            
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                // THE COMPILE FAILED, EXIT
                return false;
            }
    
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
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