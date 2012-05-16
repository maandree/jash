mkdir bin 2>/dev/null

javac7 -Xlint:all,-serial -cp . -s src -d bin src/se/kth/maandree/jash/{ATProcessor,requires}.java  &&
javac7 -processor se.kth.maandree.jash.ATProcessor -processorpath bin -Xlint:all,-serial -cp . -s src -d bin src/se/kth/maandree/jash/{*,*/*}.java
