package org.drools.benchmarks.operators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder.IntervalType;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import org.drools.benchmarks.model.Account;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Thread)
@Warmup(iterations = 100000)
@Measurement(iterations = 10000)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class TreeMapIntervalTreeBenchmark {
    
    @Param({"TreeMap", "IntervalTree"})
    protected String treeImpl;

    private boolean isTreeMap = true;

    @Param({"4", "8", "32", "64", "128", "256", "512"})
    //@Param({"4", "8"})
    //@Param({"256", "512"})
    protected int numOfInterval; // e.g. Account(balance >= 100 && balance < 200)

    private final TreeMap<Comparable, String> map = new TreeMap<>();

    private IntervalTree tree;

    //private Set<Account> accounts;
    private Account account; // test only one fact per iteration

    @Setup
    public void setupTree() {
        if (treeImpl.equals("TreeMap")) {
            System.out.println("### TreeMap");
            isTreeMap = true;
        } else {
            System.out.println("### IntervalTree");
            isTreeMap = false;
            tree = IntervalTreeBuilder.newBuilder()
                    .usePredefinedType(IntervalType.LONG)
                    .collectIntervals(interval -> new ListIntervalCollection())
                    .build();
        }

        if (isTreeMap) {
            // TreeMap
            for (int i = 1; i <= numOfInterval; i++) {
                String intervalString = "Interval [" + (i * 100) + ", " + (i + 1) * 100 + ")"; // dummy now
                map.put(Long.valueOf(i * 100), intervalString);
            }
            //System.out.println(map);
        } else {
            // IntervalTree
            for (int i = 1; i <= numOfInterval; i++) {
                tree.add(new LongInterval(Long.valueOf(i * 100), Long.valueOf((i + 1) * 100), false, true));
            }
            //System.out.println(tree);
        }
    }

    @Setup
    public void generateFacts() {
        account = new Account();
        account.setBalance(150);
        account.setName("Account");
    }

    @Benchmark
    public Object test(final Blackhole eater) {
        Object interval;
        if (isTreeMap) {
            // TreeMap
            Comparable key = account.getBalance();
            //System.out.println("key = " + key);
            Entry<Comparable, String> floorEntry = map.floorEntry(key); // floorEntry is enough because we can put a marker object in the map to judge (Start of interval / End of interval / Border of 2 intervals)
            interval = floorEntry.getValue();
            //System.out.println("  hit -> " + interval);
        } else {
            // IntervalTree
            Comparable key = account.getBalance();
            //System.out.println("key = " + key);
            Collection<IInterval> intervals = tree.overlap(new LongInterval((Long) key, (Long) key));
            Iterator it = intervals.iterator();
            interval = intervals.iterator().next();
            //System.out.println("  hit -> " + interval);
        }
        return interval;
    }
}
