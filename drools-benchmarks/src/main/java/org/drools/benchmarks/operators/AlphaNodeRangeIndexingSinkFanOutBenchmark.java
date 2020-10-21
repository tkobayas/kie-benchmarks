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
 */

package org.drools.benchmarks.operators;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.drools.benchmarks.common.util.RuntimeUtil;
import org.drools.benchmarks.model.Account;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 100000)
@Measurement(iterations = 10000)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class AlphaNodeRangeIndexingSinkFanOutBenchmark extends AbstractBenchmark {

    @Param({"false", "true"})
    protected boolean rangeIndexingEnabled;

    protected static final String RULENAME_PREFIX = "AccountBalance";

    @Param({"1", "5", "10", "50", "100", "500", "1000", "2000", "5000", "10000"})
    //@Param({"1", "5"})
    protected int sinkNum;

    private Set<Account> accounts;

    @Setup
    public void generateFacts() {
        // Just one fact which matches 1 rule
        accounts = new HashSet<>();
        final Account account = new Account();
        account.setBalance(15000);
        account.setName(RULENAME_PREFIX + 1);
        accounts.add(account);
    }

    @Setup
    public void setupKieBase() {
        
        if (!rangeIndexingEnabled) {
            System.setProperty("drools.alphaNodeRangeIndex.enabled", "false"); // Default true
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append( "import org.drools.benchmarks.model.*;\n" );
        for (int i = 1; i <= sinkNum; i++) {
            sb.append(" rule " + RULENAME_PREFIX + i + "\n" +
                    " when \n " +
                    "     $account : Account(balance >= " + (i * 10000) + ")\n " +
                    " then\n " +
                    " end\n" );
        }

        //System.out.println(sb.toString());
        
        kieBase = BuildtimeUtil.createKieBaseFromDrl(sb.toString());
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        kieSession = RuntimeUtil.createKieSession(kieBase);
    }

    @Benchmark
    public int test(final Blackhole eater) {
        for (Account account : accounts) {
            eater.consume(kieSession.insert(account));
        }
        int fired = kieSession.fireAllRules();
        //System.out.println("fired = " + fired);
        return fired;
    }
}
