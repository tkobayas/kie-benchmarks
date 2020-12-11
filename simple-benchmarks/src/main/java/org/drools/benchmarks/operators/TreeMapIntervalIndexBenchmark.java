/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.drools.benchmarks.operators;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.model.Account;
import org.drools.benchmarks.model.LongInterval;
import org.drools.core.util.index.IntervalIndex;
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
public class TreeMapIntervalIndexBenchmark {
    
    @Param({"TreeMap", "IntervalIndex"})
    protected String treeImpl;

    private boolean isTreeMap = true;

    @Param({"4", "8", "32", "64", "128", "256", "512"})
//    @Param({"4", "8"})
    //@Param({"256", "512"})
    protected int numOfInterval; // e.g. Account(balance >= 100 && balance < 200)

    private final TreeMap<Comparable, String> map = new TreeMap<>();

    private IntervalIndex<Long, LongInterval> index;

    //private Set<Account> accounts;
    private Account account; // test only one fact per iteration

    @Setup
    public void setupTree() {
        if (treeImpl.equals("TreeMap")) {
            System.out.println("### TreeMap");
            isTreeMap = true;
        } else {
            System.out.println("### IntervalIndex");
            isTreeMap = false;
            index = new IntervalIndex<>();
        }

        if (isTreeMap) {
            // TreeMap
            for (int i = 1; i <= numOfInterval; i++) {
                String intervalString = "Interval [" + (i * 100) + ", " + (i + 1) * 100 + ")"; // dummy now
                map.put(Long.valueOf(i * 100), intervalString);
            }
//            System.out.println(map);
        } else {
            // IntervalTree
            for (int i = 1; i <= numOfInterval; i++) {
                index.addInterval(new LongInterval("Interval" + i , Long.valueOf(i * 100), true, Long.valueOf((i + 1) * 100), false));
            }
//            System.out.println(index);
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
//            System.out.println("key = " + key);
            Entry<Comparable, String> floorEntry = map.floorEntry(key); // floorEntry is enough because we can put a marker object in the map to judge (Start of interval / End of interval / Border of 2 intervals)
            interval = floorEntry.getValue();
//            System.out.println("  hit -> " + interval);
        } else {
            // IntervalIndex
            Comparable key = account.getBalance();
//            System.out.println("key = " + key);
            Collection<LongInterval> intervals = index.getIntervals((Long)key);
            Iterator it = intervals.iterator();
            interval = intervals.iterator().next();
//            System.out.println("  hit -> " + interval);
        }
        return interval;
    }
}
