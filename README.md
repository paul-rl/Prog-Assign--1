How to Run:

In order to run this code, please do the following:
  1. Download the primes.java or transfer it to the cmd line you will be utilizing
  2. Change directories to the directory containing primes.java in the terminal using the cd command 
  3. Once in this directory, run the command: javac primes.java
  4. Then, run the command: java primes
 
 The program will then output its results onto the command line. This method of installation only works for command lines such as Eustis.
 
 Summary of Aproach:
 
 I approached this problem using a Segmented Sieve of Eratosthenes. A segmented sieve to find all primes less than or equal to N works as follows:
  1. Run a normal sieve in order to find all primes less than or equal to the square root of N.
  2. Then, create approximately equivalent intervals out of the space of primes that hasn't been sieved.
