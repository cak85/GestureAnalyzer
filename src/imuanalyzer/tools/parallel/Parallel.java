package imuanalyzer.tools.parallel;


/**
 *     Java Parallel.For
 *             Parallel.ForEach
 *             Parallel.Tasks
 */
import java.util.ArrayList;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
/**
 *    A Java Parallel for SMP
 */
public class Parallel
{
static int iCPU = Runtime.getRuntime().availableProcessors();
/**
 * Parallel.Tasks
 */
public static void Tasks(final Task [] tasks)
{
    ExecutorService executor = Executors.newFixedThreadPool(iCPU);
    ArrayList<Future<?>> futures  = new ArrayList<Future<?>>();

    for(final Task task : tasks)
    {
        Future<?> future = executor.submit(new Runnable()
        {
            public void run() { task.run(); }
        });        
        futures.add(future);        
    }
    
    for (Future<?> f : futures)
    {
        try   { f.get(); }
        catch (InterruptedException e) { } 
        catch (ExecutionException   e) { }         
    }
    
    executor.shutdown();     
}
/**
 * Parallel.ForEach
 */
public static <T> void
           ForEach(Iterable <T> parameters, final LoopBody<T> loopBody)
{
    ExecutorService executor = Executors.newFixedThreadPool(iCPU);
    ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
    
    for (final T param : parameters)
    {
        Future<?> future = executor.submit(new Runnable()
        {
            public void run() { loopBody.run(param); }
        });        
        futures.add(future);
    }
    
    for (Future<?> f : futures)
    {
        try   { f.get(); }
        catch (InterruptedException e) { } 
        catch (ExecutionException   e) { }         
    }
    
    executor.shutdown();     
}
/**
 * Parallel.For
 */
public static void
           For(int start, int end, final LoopBody<Integer> loopBody)
{
    ExecutorService executor = Executors.newFixedThreadPool(iCPU);
    ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
    ArrayList<Partition> partitions = create(start, end, iCPU);
    
    for(final Partition p : partitions)
    {
        Future<?> future = executor.submit(new Runnable()
        {
            public void run()
            {                
                for(int i=p.start; i<p.end; i++) loopBody.run(i);
            }
        });
        futures.add(future);
    }

    for (Future<?> f : futures)
    {
        try   { f.get(); }
        catch (InterruptedException e) { } 
        catch (ExecutionException   e) { }         
    }
    
    executor.shutdown();     
}
/**
 * Create Partitions To Turn Parallel.For To Parallel.ForEach
 */
public static ArrayList<Partition>
           create(int inclusiveStart, int exclusiveEnd)
{
    return create(inclusiveStart, exclusiveEnd, iCPU);    
}
public static ArrayList<Partition>
           create(int inclusiveStart, int exclusiveEnd, int cores)
{
    //increment
    int       total = exclusiveEnd - inclusiveStart;
    double dc   = (double)total / cores;
    int        ic    = (int)dc;

    if (ic <= 0) ic = 1;
    if (dc > ic) ic++;

    //partitions
    ArrayList<Partition> partitions = new ArrayList<Partition>();
    if (total <= cores)
    {
        for (int i = 0; i < total; i++)
        {
            Partition p = new Partition();
            p.start = i;
            p.end   = i + 1;
            partitions.add(p);
        }
        return partitions;
    }

    int count = inclusiveStart;
    while (count < exclusiveEnd)
    {
        Partition p = new Partition();
        p.start = count;
        p.end   = count + ic;

        partitions.add(p);
        count += ic;

        //boundary check
        if (p.end >= exclusiveEnd)
        {
            p.end = exclusiveEnd;
            break;
        }
    }

    return partitions;    
}
/**
 * Unit Test
 */
public static void main(String [] argv)
{
    //sample data
     final ArrayList<String> ss = new ArrayList<String>();
    
     String [] s = {"a", "b", "c", "d", "e", "f", "g"};
     for (String z : s) ss.add(z);
     int m = ss.size();
    
    //parallel-for loop
    System.out.println("Parallel.For loop:");
    Parallel.For(0, m, new LoopBody<Integer>()
    {
        public void run(Integer i)
        {
            System.out.println(i +"\t"+ ss.get(i));    
        }        
    });    

    //parallel for-each loop
    System.out.println("Parallel.ForEach loop:");
    Parallel.ForEach(ss, new LoopBody<String>()
    {
        public void run(String p)
        {
            System.out.println(p);                
        }        
    });

    //partitioned parallel loop
    System.out.println("Partitioned Parallel loop:");
    Parallel.ForEach(Parallel.create(0, m), new LoopBody<Partition>()
    {
        public void run(Partition p)
        {
            for(int i=p.start; i<p.end; i++)
                System.out.println(i +"\t"+ ss.get(i));
        }
    });
    
    //parallel tasks
    System.out.println("Parallel Tasks:");
    Parallel.Tasks(new Task []
    {
        //task-1
        new Task() {public void run()
        {
            for(int i=0; i<3; i++)
                System.out.println(i +"\t"+ ss.get(i));
        }},
        
        //task-2
        new Task() {public void run()
        {
            for (int i=3; i<6; i++)
                System.out.println(i +"\t"+ ss.get(i));
        }}    
    });
}
/** End of Parallel class */
}
