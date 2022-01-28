import java.lang.Math;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.concurrent.Callable;
public class primes{
    static long startTime = System.currentTimeMillis();// start our timer

    // Variables given to us by the problem, and modifications of them
    final static int NUM_THREADS = 8; // Total number of threads
    final static int N = (int)Math.pow(10,8);
    final static int sqrtN = (int) Math.sqrt(N);
    final static int numOdds =  (N % 2 == 0) ? N/2 - 1 : N/2; // # of odds in [0,10^8]

    // int that will be utilized throughout to keep track of how much work each thread is doing
    static int interval = 0;

    // Variables that keep track of primes
    // We only care about crossing out odd numbers because we know all evens are not prime. As a result,
    // this array will start with 3 as the first odd (since 1 is not prime).
    // To find a number's value in the array: (num - 1)/2 - 1
    static boolean[] oddPrimes = new boolean[numOdds]; 
    static ArrayList<Integer> primeList = new ArrayList<Integer>(); // Holds primes found by original sieve 


    // We will utilize a segmented sieve to solve this problem. 
    // To do so, we must first use an ordinary sieve to find the set of prime numbers < sqrt(N)
    // Once this is done, we can create intervals of approximately equal length and run a sieve through those
    // intervals using the original set of prime numbers we found.
    // Each thread will be responsible of running a sieve through each interval
    // In order to save time, we will also be skipping all even numbers; we know they are not prime.
    public static void main(String[] args) {
        long[] primeVars = new long[2]; // pos 0 holds sum of primes, pos 1 holds num of primes

        // First, we must initialize each element in our array to true
        // We will do so in parallel to save time

        // Calculate interval and account for remainder
        interval = numOdds / NUM_THREADS;
        int remainder = numOdds % NUM_THREADS;

        // Create executor that will be in charge of initializing the numOdds array
        ExecutorService exec = Executors.newFixedThreadPool(NUM_THREADS);

        // First N-1 tasks will allocate the same amount of space
        for (int i = 0; i < NUM_THREADS - 1; i++) {
            InitArrTask task = new InitArrTask(interval * i, interval * (i + 1)); // Each thread will initialize a part of the array
            exec.submit(task);
        }

        // Last task will allocate the final amount of space, accounting for remainder
        exec.submit(new InitArrTask(interval * (NUM_THREADS-1), interval * (NUM_THREADS) + remainder));

        // Shutdown thread and await results
        exec.shutdown();
        try{
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (Exception e){System.out.println("Error awaiting termination of first executor.");};

        // Run simple sieve and store results
        primeVars = simpleSieve(sqrtN);        

        // Calculate interval and account for remainder 
        interval = (N - sqrtN) / NUM_THREADS;
        remainder = (N - sqrtN) % NUM_THREADS;
        
        // Run segmented sieves in parallel
        ExecutorService exec2 = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS - 1; i++){
            SegmentedSieveTask task = new SegmentedSieveTask(sqrtN + interval * i, sqrtN + interval * (i + 1));
            exec2.submit(task);
        }
        exec2.submit(new SegmentedSieveTask(sqrtN + interval*(NUM_THREADS-1), sqrtN + interval * NUM_THREADS + remainder));

        exec2.shutdown();
        try{
            exec2.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (Exception e){System.out.println("Error awaiting termination of second executor.");}

        // Tally up primes discovered through segmented sieve
        for (int  i = (sqrtN - 1) / 2 - 1; i < numOdds; i ++){
            if (oddPrimes[i]){
                primeVars[0]++;
                primeVars[1] += 2 * (i + 1) + 1;
                
            }
        }

        // Find the x largest primes
        final int x = 10;
        int[] tenLargest = new int[x];
        int count = 0;

        // Start from the end, and stop once you found ten primes or reached the end
        for (int i = numOdds - 1; i > 0 && count!= 10; i--){
            if (oddPrimes[i]){
                tenLargest[x-count-1] = 2 * (i + 1) + 1;
                count++;
            }
        }

        // Print out our results
        System.out.println("Num primes: " + primeVars[0] + ", sum of primes: " + primeVars[1]);
        System.out.println("Time: " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Ten largest:");
        for (int i = 0; i < x; i++)
            System.out.println(tenLargest[i]);
    }

    public static long[] simpleSieve(int n) {
        long[] arr = new long[2];
        
        // Cross off multiples of prime numbers
        // To find all primes less than or equal to n, sieve only has to go up to sqrt(n)
        int intVal = 3;
        for (int i = 0; intVal * intVal < n; i += 1) {
            
            if (oddPrimes[i]) {
                // Increment by intVal * 2 so as to skip even multiples
                for (int j = intVal * intVal; j < n; j += intVal * 2) {
                    oddPrimes[(j - 1) / 2 - 1] = false; 
                }
            }
            // Increase by 2 so as to go to the next odd number
            intVal += 2;
        }

        // Get n's position in array
        int oddPos = (n - 1) / 2 - 1; 
        for (int i = 0; i < oddPos; i++) {
            if (oddPrimes[i]) {
                primeList.add(2 * (i + 1) + 1);
                arr[0]++;
                arr[1] += (2 * (i + 1) + 1);
            }
        }

        // Account for only even prime
        if (n > 1){
            arr[0]++;
            arr[1] += 2;
        }

        return arr;
    }

    // Task responsible for running independent intervals of array initialization
    public static class InitArrTask implements Callable<Integer>{
        int loBound, upBound;

        public InitArrTask(int lo, int hi){
            loBound = (lo == 0) ? lo : lo + 1; 
            upBound = (hi == numOdds) ? hi - 1 : hi;
        }
        public Integer call(){
            for (int i = loBound; i <= upBound; i++){
                oddPrimes[i] = true;
            }
            return 1;
        }
    }

    // Task responsible for running independent intervals of segmented sieve
    public static class SegmentedSieveTask implements Callable<Integer>{
        int loBound, upBound;
        public SegmentedSieveTask(int lo, int hi){
            loBound = (lo == sqrtN) ? lo : lo + 1; 
            upBound = (hi == numOdds) ? hi - 1 : hi;
        }

        public Integer call(){
            int len = primeList.size();
            // Loops through list of primes found in simple sieve
            for (int i = 0; i < len; i++){ 
                // Finds highest odd multiple of prime that is >= loBound
                int lo = (int)((int)(loBound/primeList.get(i)) * primeList.get(i));
                if (lo < loBound)
                    lo += primeList.get(i);
                if (lo % 2 == 0) lo += primeList.get(i);

                // Declare that each multiple of prime is not prime
                for (int j = lo; j < upBound; j += primeList.get(i) * 2) {
                    oddPrimes[(j - 1) / 2 - 1] = false; 
                }
            }
            return 1;
        }
    }

}