package timingtest;

import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        SLList<Integer> list = new SLList<>();
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        int M = 10000;
        int stoppoint = 1000;
        for (int i = 1; i <= 128000; i++) {
            list.addLast(i);
            if (i == stoppoint) {
                Ns.addLast(stoppoint);
                Stopwatch sw = new Stopwatch();
                for (int j = 0; j < M; j++) {
                    list.getLast();
                }
                times.addLast(sw.elapsedTime());
                opCounts.addLast(M);
                stoppoint *= 2;
            }
        }
        printTimingTable(Ns, times, opCounts);
    }

}
