/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.benchmarks.session;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.drools.benchmarks.common.util.ReteDumper;
import org.drools.benchmarks.common.util.RuntimeUtil;
import org.drools.benchmarks.model.A;
import org.drools.core.reteoo.AlphaNode;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.AlphaNodeOrderingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Warmup(iterations = 2000)
@Measurement(iterations = 500)
public class AlphaNodeOrderingBenchmark extends AbstractBenchmark {

    @Param({"10", "30"})
//    @Param({"10"})
    private int rulesNr;

    @Param({"1000"})
    private int factsNr;

    @Param({"true", "false"})
    private boolean enableAlphaNodeOdering;

//    @Param({"true", "false"})
    @Param({"false"})
    private boolean useCanonicalModel;

    @Setup
    public void setupKieBase() throws IOException {
        final Resource drlResource = KieServices.get().getResources()
                                                .newReaderResource(new StringReader(getDrl(rulesNr)))
                                                .setResourceType(ResourceType.DRL)
                                                .setSourcePath("drlFile.drl");
        KieContainer kieContainer = BuildtimeUtil.createKieContainerFromResources(useCanonicalModel, drlResource);

        KieBaseConfiguration kieBaseConfiguration = KieServices.get().newKieBaseConfiguration();
        kieBaseConfiguration.setOption(enableAlphaNodeOdering ? AlphaNodeOrderingOption.COUNT : AlphaNodeOrderingOption.NONE);

        kieBase = kieContainer.newKieBase(kieBaseConfiguration);
        
        List<AlphaNode> alphaNodes = MyReteDumper.collectNodes(kieBase)
                .stream()
                .filter(AlphaNode.class::isInstance)
                .map(node -> (AlphaNode) node)
                .collect(Collectors.toList());
        
        System.out.println("alphaNodes.size() = " + alphaNodes.size());
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        kieSession = RuntimeUtil.createKieSession(kieBase);
        for (int i = 0; i < factsNr; i++) {
            kieSession.insert(new A(i % rulesNr));
        }
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        kieSession.dispose();
    }

    @Benchmark
    public int test() {
        int firings = kieSession.fireAllRules();
        return firings;
    }

    private String getDrl(int rulesNr) {
        StringBuilder drlBuilder = new StringBuilder();
        drlBuilder.append("import " + A.class.getCanonicalName() + ";\n");
        for (int i = 0; i < rulesNr; i++) {
            drlBuilder.append("rule R" + i + " when $a : A( ");
            int conditionsNr = rulesNr - i;
            for (int j = (rulesNr - conditionsNr); j < rulesNr; j++) {
                drlBuilder.append("value != " + j);
                if (j != rulesNr - 1) {
                    drlBuilder.append(", ");
                }
            }
            drlBuilder.append(" ) then end\n");
        }
        return drlBuilder.toString();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(AlphaNodeOrderingBenchmark.class.getSimpleName())
                                          .warmupIterations(0)
                                          .measurementIterations(1)
                                          .forks(1)
                                          .build();

        new Runner(opt).run();
    }
}
