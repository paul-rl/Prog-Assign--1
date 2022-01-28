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
  3. For each interval, find the multiples of the primes found in the first sieve and mark them off as not prime.
 
Since we are finding all multiples less than or equal to sqrt(N), we know that all primes not found in the first sieve are greater than the   sqrt(N). As a result, we also know that any multiple greater than 1 of each prime found in the first sieve is NOT less than or equal to N.
Assuming that our simple sieve finds all necessary primes, each segmented sieve finds all primes in their predetermined interval.
 
This algorithm results in us having a list of all prime numbers less than N.
My algorithm took advantage of parallelization in two main ways:
  1. Initializing the boolean array responsible for keeping track of which odd numbers are prime.
  2. Finding the primes in each interval using the segmented sieve.
In both these cases, each thread is responsible for one interval. As a result, this work can be done simultaneously. 

Another optimization that I utilized throughout is accounting for the fact that 2 is the only even prime number. This lead to half the amount of space being used and never checking any even number throughout any of the sieves.
