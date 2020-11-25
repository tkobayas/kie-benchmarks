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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.drools.benchmarks.common.util.RuntimeUtil;
import org.drools.benchmarks.model.Account;
import org.drools.core.base.ClassObjectType;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectTypeNode;
import org.kie.internal.builder.conf.AlphaNetworkCompilerOption;
import org.kie.internal.builder.conf.KnowledgeBuilderOption;
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
public class AlphaNodeRangeIndexingBenchmark extends AbstractBenchmark {

    protected static final String RULENAME_PREFIX = "AccountBalance";

        @Param({"12", "24", "48"})
//    @Param({"12"})
    protected int rulesAndFactsNumber;

        @Param({"false", "true"})
//    @Param({"false"})
    protected boolean rangeIndexingEnabled;

    @Param({"false", "true"})
    protected boolean alphanetworkCompilerEnabled;

    private Set<Account> accounts;

    @Setup
    public void setupKieBase() {
        KnowledgeBuilderOption[] knowledgeBuilderOptions;
        if (alphanetworkCompilerEnabled) {
            knowledgeBuilderOptions = new KnowledgeBuilderOption[]{AlphaNetworkCompilerOption.INMEMORY};
        } else {
            knowledgeBuilderOptions = new KnowledgeBuilderOption[]{AlphaNetworkCompilerOption.DISABLED};
        }

        StringBuilder sb = new StringBuilder();
        sb.append("import org.drools.benchmarks.model.*;\n");
        for (int i = 1; i <= rulesAndFactsNumber; i++) {
            sb.append(" rule " + RULENAME_PREFIX + i + "\n" +
                      " when \n " +
                      "     $account : Account(balance >= " + (i * 10000) + ")\n " +
                      " then\n " +
                      " end\n");
        }

        if (rangeIndexingEnabled) {
            kieBase = BuildtimeUtil.createKieBaseFromDrl(sb.toString(), BuildtimeUtil.getKieBaseConfiguration(AlphaRangeIndexThresholdOption.get(AlphaRangeIndexThresholdOption.DEFAULT_VALUE)), knowledgeBuilderOptions); // AlphaRangeIndexThresholdOption.DEFAULT_VALUE = 9
        } else {
            kieBase = BuildtimeUtil.createKieBaseFromDrl(sb.toString(), BuildtimeUtil.getKieBaseConfiguration(AlphaRangeIndexThresholdOption.get(0)), knowledgeBuilderOptions);
        }

        final List<ObjectTypeNode> nodes = ((KnowledgeBaseImpl) kieBase).getRete().getObjectTypeNodes();
        for (final ObjectTypeNode n : nodes) {
            if (((ClassObjectType) n.getObjectType()).getClassType() == Account.class) {
                ObjectSinkPropagator objectSinkPropagator = n.getObjectSinkPropagator();
                System.out.println("objectSinkPropagator : " + objectSinkPropagator);
            }
        }
    }

    @Setup
    public void generateFacts() {
        accounts = new HashSet<>();
        for (int i = 1; i <= rulesAndFactsNumber; i++) {
            final Account account = new Account();
            account.setBalance(i * 10000 + 5000d); // not boundary value
            account.setName(RULENAME_PREFIX + i);
            accounts.add(account);
        }
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        kieSession = RuntimeUtil.createKieSession(kieBase);
    }

    @Benchmark
    public int test(final Blackhole eater) {
        for (Account account : accounts) {
            kieSession.insert(account);
        }
        int fired = kieSession.fireAllRules();
//        System.out.println("fired : " + fired);
        return fired;
    }
}
