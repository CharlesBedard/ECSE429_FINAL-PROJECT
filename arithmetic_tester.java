// import arithmetic_software.java;
// import org.junit.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
public class arithmetic_tester {

    public static void main(String[] args) {
        // define input vectors and expected outputs
        String[] inputVectors = {"1 2 3", "-10 58 10", "2 2 2 3 3 3 4"};
        String[] expectedOutputs = {"2.0,2.0,0.6666666666666666", "19.0,10.0,25.666666666666668", "2.0,3.0,0.7142857142857143"};

        int totalMutants = 0;
        int killedMutants = 0;

        // iterate over all the mutant files
        // TODO, find a way to replace 72 by counting how many files were generated
        for(int i=0;i<72;i++){
            totalMutants++;
            boolean killed = false;
            for(int j=0;j<inputVectors.length;j++){
                if(!runMutant(i,inputVectors[j],expectedOutputs[j])){
                    System.out.println("Mutant file " + i + " killed by vector " + inputVectors[j]);
                    killed = true;
                    killedMutants++;
                    break;
                }
            }
            if(!killed){
                System.out.println("Mutant file " + i + " passed all tests");
            } else {
                killed = false;
            }

        }

        System.out.println("MUTANT COVERAGE: " + ((double)killedMutants/(double)totalMutants) + "%");
    
    }

    public static boolean runMutant(int mutantNumber,String inputVector,String expectedOutput){
        // function running test on mutant, outputs false when tests fail, true when it succeeds
        try {
            // FIRST TRY TO COMPILE THE PROGRAM
            Process compileProcess = Runtime.getRuntime().exec("javac mutant"+Integer.toString(mutantNumber)+".java", null, null);
    
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

                    Process process = Runtime.getRuntime().exec("java mutant"+Integer.toString(mutantNumber)+" "+inputVector, null, null);
            
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
    
}