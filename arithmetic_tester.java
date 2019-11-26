// import arithmetic_software.java;
// import org.junit.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
public class arithmetic_tester {
    private static AtomicInteger thread_ID_count = new AtomicInteger(0);
    private static AtomicInteger mutantNumber = new AtomicInteger(0);
    private static AtomicInteger mutantsKilled = new AtomicInteger(0);
    private static int maxThreads = 8;
    public static void main(String[] args) {
        // define input vectors and expected outputs
        String[] inputVectors = {"1 2 3", "-10 58 10", "2 2 2 3 3 3 4"};

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

        // iterate over all the mutant files
        ArrayList<Thread> threads = new ArrayList<Thread>();
        String[] killedByTable = new String[maxMutants];
        for(int i=0; i<maxThreads; i++){
            Thread thread = new Thread(() -> {
                // int threadID = thread_ID_count.getAndIncrement();
                int tempMutantNumber;
                while((tempMutantNumber = mutantNumber.getAndIncrement()) < maxMutants){
                    System.out.println("Running test vectors on mutant " + tempMutantNumber);
                    boolean killed = false;
                    for(int j=0;j<inputVectors.length;j++){
                        if(!runMutant(tempMutantNumber,inputVectors[j],expectedOutputs[j])){
                            killedByTable[tempMutantNumber]=inputVectors[j];
                            killed = true;
                            mutantsKilled.getAndIncrement();
                            break;
                        }
                    }
                    if(!killed){
                        killedByTable[tempMutantNumber] = "passed all tests";
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

        prettyPrintTable(killedByTable);
        System.out.println("MUTANT COVERAGE: " + (mutantsKilled.doubleValue()/(double)maxMutants) + "%");
    
    }

    public static void prettyPrintTable(String[] killedByTable){
        for(int i=0;i<killedByTable.length;i++){
            if(killedByTable[i] != "passed all tests"){
                System.out.println("Mutant file " + i + " killed by vector [" + killedByTable[i] + "]");
            } else {
                System.out.println("Mutant file " + i + " passed all tests");
            }
        }
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