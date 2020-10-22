package org.drools.benchmarks.operators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.model.Account;
import org.drools.core.util.Entry;
import org.drools.core.util.FastIterator;
import org.drools.core.util.RBTree;
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
public class TreeMapRBTreeTraversalBenchmark {
    
    @Param({"TreeMap", "RBTree"})
    protected String treeImpl;

    private boolean isTreeMap = true;

    @Param({"10000"})
    protected int sinkNum;

    @Param({"1", "10", "25", "50", "75", "100"})
//    @Param({"1"})
    protected int traversalPercentage;

    private final TreeMap<IndexKey<Comparable>, String> map = new TreeMap<>();

    private final RBTree<IndexKey<Comparable>, String> tree = new RBTree<>();

    private Set<Account> accounts;

    @Setup
    public void setupTree() {
        if (treeImpl.equals("TreeMap")) {
            //System.out.println("### TreeMap");
            isTreeMap = true;
        } else {
            //System.out.println("### RBTree");
            isTreeMap = false;
        }

        if (isTreeMap) {
            // TreeMap
            for (int i = 1; i <= sinkNum; i++) {
                map.put(new IndexKey<>(IndexType.GE, Double.valueOf(i * 100)), "valueGE" + (i * 100));
            }
        } else {
            // RBTree
            for (int i = 1; i <= sinkNum; i++) {
                tree.insert(new IndexKey<>(IndexType.GE, Double.valueOf(i * 100)), "valueGE" + (i * 100));
            }
        }
    }

    @Setup
    public void generateFacts() {
        // Just one key which matches traversalPercentage of nodes
        accounts = new HashSet<>();
        final Account account = new Account();
        double balance = (sinkNum * 100) / 100 * traversalPercentage;
        //System.out.println("\n  balance = " + balance);
        account.setBalance(balance);
        account.setName("Account" + 1);
        accounts.add(account);
    }

    @Benchmark
    public void test(final Blackhole eater) {
        if (isTreeMap) {
            // TreeMap
            for (Account account : accounts) {
                Comparable key = account.getBalance();
                //System.out.println(key);
                Collection<String> results = map.subMap(new IndexKey<>(IndexType.LT, key), false, new IndexKey<>(IndexType.GT, key), false).values();
                for (String string : results) {
                    eater.consume(string); // do some work
                    //System.out.println("  hit -> " + string);
                }
                //System.out.println("  results.size() -> " + results.size());
                //System.out.println("    ratio = " + ((double)(results.size()) / sinkNum));
            }
        } else {
            // RBTree
            for (Account account : accounts) {
                Comparable key = account.getBalance();
                //System.out.println(key);
                //int count = 0;
                FastIterator it = tree.range(new IndexKey<>(IndexType.LT, key), false, new IndexKey<>(IndexType.GT, key), false);
                for (Entry entry = it.next(null); entry != null; entry = it.next(entry)) {
                    RBTree.Node<Comparable<Comparable>, String> node = (RBTree.Node<Comparable<Comparable>, String>) entry;
                    String string = node.value;
                    eater.consume(string); // do some work
                    //System.out.println("  hit -> " + string);
                    //count++;
                }
                //System.out.println("  count -> " + count);
                //System.out.println("    ratio = " + ((double)count / sinkNum));
            }
        }
    }

    public enum IndexType {
        LT(0), LE(0), GE(1), GT(1);

        private int direction;

        IndexType(int direction) {
            this.direction = direction;
        }
    }

    private static class IndexKey<K extends Comparable> implements Comparable<IndexKey<K>> {

        private final IndexType indexType;
        private final K key;

        public IndexKey(IndexType indexType, K key) {
            this.indexType = indexType;
            this.key = key;
        }

        @Override
        public int compareTo(IndexKey<K> o) {
            int directionDiff = indexType.direction - o.indexType.direction;
            if (directionDiff != 0) {
                return directionDiff;
            }
            int orderDiff = key.compareTo(o.key);
            return orderDiff != 0 ? orderDiff : indexType.compareTo(o.indexType);
        }
    }
}
