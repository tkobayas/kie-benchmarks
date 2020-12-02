package org.drools.benchmarks.operators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder.IntervalType;
import com.brein.time.timeintervals.intervals.DoubleInterval;
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
public class TreeMapIntervalTreeBenchmark2 {
    
    @Param({"TreeMap", "IntervalTree"})
    protected String treeImpl;

    private boolean isTreeMap = true;

    //@Param({"4", "8", "32", "64", "128"})
    @Param({"4", "8"})
    //@Param({"128"})
    protected int numOfInterval; // e.g. Account(balance >= 10000 && balance < 20000)

    private final TreeMap<Comparable, IntervalMarker> map = new TreeMap<>();

    private IntervalTree tree;

    private Set<Account> accounts;

    @Setup
    public void setupTree() {
        if (treeImpl.equals("TreeMap")) {
            System.out.println("### TreeMap");
            isTreeMap = true;
        } else {
            System.out.println("### IntervalTree");
            isTreeMap = false;
            tree = IntervalTreeBuilder.newBuilder()
                    .usePredefinedType(IntervalType.DOUBLE)
                    .collectIntervals(interval -> new ListIntervalCollection())
                    .build();
        }

        if (isTreeMap) {
            // TreeMap
            for (int i = 1; i <= numOfInterval; i++) {
                IntervalMarker marker;
                String intervalString = "Interval [" + (i * 10000) + ", " + (i * 20000) + ")"; // dummy now
                if (i == 1) {
                    marker = new IntervalMarker(intervalString, IntervalMarker.Type.START);
                } else if (i == numOfInterval) {
                    marker = new IntervalMarker(intervalString, IntervalMarker.Type.END);
                } else {
                    marker = new IntervalMarker(intervalString, IntervalMarker.Type.BORDER);
                }
                map.put(Double.valueOf(i * 10000), marker);
            }
        } else {
            // IntervalTree
            for (int i = 1; i <= numOfInterval; i++) {
                tree.add(new DoubleInterval(Double.valueOf(i * 10000), Double.valueOf(i * 20000), false, true));
            }
        }
    }

    @Setup
    public void generateFacts() {
        accounts = new HashSet<>();
        for (int i = 1; i <= numOfInterval; i++) {
            final Account account = new Account();
            account.setBalance(i * 15000);
            account.setName("Account" + i);
            accounts.add(account);
        }
    }

    @Benchmark
    public void test(final Blackhole eater) {
        if (isTreeMap) {
            // TreeMap
            for (Account account : accounts) {
                Comparable key = account.getBalance();
                //System.out.println(key);
                Entry<Comparable, String> floorEntry = map.floorEntry(key);
                String value = floorEntry.getValue();
                eater.consume(value); // do some work
                System.out.println("  hit -> " + value);
            }
        } else {
            // IntervalTree
            for (Account account : accounts) {
                Comparable key = account.getBalance();
                //System.out.println(key);
                FastIterator it = tree.range(new IndexKey<>(IndexType.LT, key), false, new IndexKey<>(IndexType.GT, key), false);
                for (Entry entry = it.next(null); entry != null; entry = it.next(entry)) {
                    RBTree.Node<Comparable<Comparable>, String> node = (RBTree.Node<Comparable<Comparable>, String>) entry;
                    String string = node.value;
                    eater.consume(string); // do some work
                    //System.out.println("  hit -> " + string);
                }
            }
        }
    }

    // Just to emulate Interval-AlphaNode usage
    private static class IntervalMarker {

        public enum Type {
            START, END, BORDER;
        }
        
        private Object intervalAlphaNodeObject; // dummy now
        
        private Type type;

        public IntervalMarker(Object intervalAlphaNodeObject, Type type) {
            this.intervalAlphaNodeObject = intervalAlphaNodeObject;
            this.type = type;
        }
    }
}
