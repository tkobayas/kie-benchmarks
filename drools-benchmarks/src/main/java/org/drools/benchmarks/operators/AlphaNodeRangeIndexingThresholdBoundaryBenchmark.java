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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.drools.benchmarks.common.util.RuntimeUtil;
import org.drools.benchmarks.model.Account;
import org.drools.core.base.ClassObjectType;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.CompositeObjectSinkAdapter;
import org.drools.core.reteoo.CompositeObjectSinkAdapter.FieldIndex;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.util.index.AlphaRangeIndex;
import org.kie.internal.conf.AlphaRangeIndexThresholdOption;
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
public class AlphaNodeRangeIndexingThresholdBoundaryBenchmark extends AbstractBenchmark {

    protected static final String RULENAME_PREFIX = "AccountBalance";

    @Param({"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"})
    protected int _sinkNum;
    
    @Param({"false", "true"})
    protected boolean rangeIndexingEnabled;

    private Set<Account> accounts;

    @Setup
    public void generateFacts() {
        // Just two facts which matches 1 rule
        accounts = new HashSet<>();
        // final Account account1 = new Account();
        // account1.setBalance(15000);
        // account1.setName(RULENAME_PREFIX + 1);
        // accounts.add(account1);

        final Account account2 = new Account();
        account2.setBalance(10000);
        account2.setName(RULENAME_PREFIX + 1 + "boundary");
        accounts.add(account2);
    }

    @Setup
    public void setupKieBase() {

        StringBuilder sb = new StringBuilder();
        sb.append( "import org.drools.benchmarks.model.*;\n" );
        for (int i = 1; i <= _sinkNum; i++) {
            sb.append(" rule " + RULENAME_PREFIX + i + "\n" +
                    " when \n " +
                    "     $account : Account(balance >= " + (i * 10000) + ")\n " +
                    " then\n " +
                    " end\n" );
        }

        //System.out.println(sb.toString());

        if (rangeIndexingEnabled) {
            // use 1 because wanted to compare from minimal value (but actually, sink = 1 means SingleObjectSinkAdapter so there is no difference)
            kieBase = BuildtimeUtil.createKieBaseFromDrl(sb.toString(), AlphaRangeIndexThresholdOption.get(1));
        } else {
            kieBase = BuildtimeUtil.createKieBaseFromDrl(sb.toString(), AlphaRangeIndexThresholdOption.get(0));
        }

//        if (_sinkNum >= 2) {
//            ObjectTypeNode otn = null;
//            final List<ObjectTypeNode> nodes = ((KnowledgeBaseImpl) kieBase).getRete().getObjectTypeNodes();
//            for (final ObjectTypeNode n : nodes) {
//                if (((ClassObjectType) n.getObjectType()).getClassType() == Account.class) {
//                    otn = n;
//                }
//            }
//            final CompositeObjectSinkAdapter sinkAdapter = (CompositeObjectSinkAdapter) otn.getObjectSinkPropagator();
//            Map<FieldIndex, AlphaRangeIndex> rangeIndexMap = sinkAdapter.getRangeIndexMap();
//            if (rangeIndexMap == null) {
//                System.out.println("rangeIndexMap is null");
//            } else {
//                System.out.println("rangeIndexMap size = " + rangeIndexMap.entrySet().iterator().next().getValue().size());
//            }
//        } else {
//            System.out.println("SingleObjectSinkAdapter");
//        }
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
