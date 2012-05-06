package imuanalyzer.tools.parallel;

/**
 *     Parallel Loop Body Interface
 */
public interface LoopBody <T>
{
    void run(T p);
}