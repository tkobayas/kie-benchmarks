/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.benchmarks.session;

import java.util.concurrent.TimeUnit;
import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.DrlProvider;
import org.drools.benchmarks.common.providers.RulesWithJoinsProvider;
import org.drools.benchmarks.domain.A;
import org.drools.benchmarks.domain.B;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Inserts facts and fires at each insertion causing the activation of all rules.
 */
@Warmup(iterations = 2000)
@Measurement(iterations = 1000)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class InsertFireLoopBenchmark extends AbstractBenchmark {

    @Param({"1", "4", "16"})
    private int rulesNr;

    @Param({"1", "4", "16"})
    private int factsNr;

    @Setup
    public void setupKieBase() {
        final DrlProvider drlProvider = new RulesWithJoinsProvider(1, false, true);
        createKieBaseFromDrl(drlProvider.getDrl(rulesNr));
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        createKieSession();
    }

    @Benchmark
    public void test(final Blackhole eater) {
        A a = new A( rulesNr + 1 );
        eater.consume(kieSession.insert( a ));
        for ( int i = 0; i < factsNr; i++ ) {
            eater.consume(kieSession.insert( new B( rulesNr + 3 ) ));
            eater.consume(kieSession.fireAllRules());
        }
    }
}