package imuanalyzer.utils.parallel;

/**
 *     Parallel Loop Body Interface
 */
public interface LoopBody <T>
{
    void run(T p);
}