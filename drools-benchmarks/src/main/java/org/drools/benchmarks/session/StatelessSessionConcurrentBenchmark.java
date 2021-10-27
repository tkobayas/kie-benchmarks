/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.drools.benchmarks.session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.common.DRLProvider;
import org.drools.benchmarks.common.providers.SimpleRulesWithConstraintsProvider;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.StatelessKieSession;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks the creation and execution of a stateless ksession
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Threads(1)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
public class StatelessSessionConcurrentBenchmark {

    private int rulesNr = 100;

    private KieBase kieBase;

    @Setup
    public void setupKieBase() {
        final DRLProvider drlProvider = new SimpleRulesWithConstraintsProvider("$i : Integer(this == ${i})");
        kieBase = BuildtimeUtil.createKieBaseFromDrl(drlProvider.getDrl(rulesNr));
    }

    @Benchmark
    public void testStatelessKieSession(final Blackhole eater) throws ExecutionException, InterruptedException {
        KieCommands cmds = KieServices.Factory.get().getCommands();
        StatelessKieSession ksession = kieBase.newStatelessKieSession();
        List<Command<?>> batch = new ArrayList<>();
        batch.add(cmds.newInsert(50));
        batch.add(cmds.newFireAllRules());
        BatchExecutionCommand exec = cmds.newBatchExecution(batch);
        eater.consume(ksession.execute(exec));
    }
}
