/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.benchmarks.quick;

import java.util.concurrent.TimeUnit;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.util.maven.support.ReleaseIdImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 15, time = 3)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class InstantiateKieContainerFromKJarBenchmark {

    private static final String MODULE_GROUPID ="org.drools";
    private static final String MODULE_ARTIFACTID ="drools-benchmarks-module";
    private static final String MODULE_VERSION ="1.0-SNAPSHOT";

    private static final String MODULE_KIEBASE ="benchmarks-module";

    private static final ReleaseId MODULE_RELEASEID = new  ReleaseIdImpl(MODULE_GROUPID, MODULE_ARTIFACTID, MODULE_VERSION);

    @Benchmark
    public KieBase createKieContainerFromKjar()  {
        return KieServices.get().newKieContainer(MODULE_RELEASEID).getKieBase(MODULE_KIEBASE);
    }


}