The MutantGenerator java program will generate a list of mutated lines reading from 
the file the user specifies with keyboard input.
Incorrect file name input will cause an exception and execution will stop.

Any line with a + , - , * , / will be appended to mutantList.txt along with each
instance of the operator swapped out for the other 3 operators.
If mutantList.txt already exists in the source file directory, it will be deleted
before program writes the output of the latest execution.
The program DOES NOT consider mathematical use of operators, it naively replaces
them as removing stillborn mutants was not a specification/it was not specified
to consider arithmetic use.

It replaces operators in the above mentioned order, not left to right order.

Command line:
run "java MutantGenerator.java"
input testfilename.txt when prompted (or whatever your test file is called) and 
hit enter/return
See mutantList.txt in your source file directory for results.


!!!!!! ASSIGNMENT 2 ADDITIONS !!!!!!!
run "java MutantInjector.java"
input mutantgeninput.txt (or whatever your test file is called)
hit enter/return
input mutantList.txt
See generated mutant text files under the "mutants" directory created

I've included mutantgeninput.txt as a sample and the mutantList.txt is the 
output created from MutantGenerator
Note that running MutantGenerator replaces mutantList.txt in the currect directory
with its output, and MutantInjector replaces the contents of a "mutants" 
subdirectory with its output.

---- Veronica Boychuk -----
