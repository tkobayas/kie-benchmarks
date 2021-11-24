/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.drools.benchmarks.buildtime;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.common.DRLProvider;
import org.drools.benchmarks.common.providers.SimpleRulesWithConstraintsAndFunctionProvider;
import org.drools.benchmarks.common.providers.SimpleRulesWithConstraintsAndStaticMethodProvider;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.drools.mvel.builder.MVELDialect;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 20, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(4)
public class BuildKJarFromResourceWithFunctionsBenchmark {

//    @Param({"true", "false"})
//    @Param({"false"})
    private boolean useCanonicalModel = false;

    @Param({"drl", "java"})
//    @Param({"drl"})
    private String functionType;

    @Param({"true", "false"})
    private boolean pkgCLfirst;

    @Param({"1000"})
//    @Param({"10"})
    private int numberOfRules;
    
    @Param({"50"})
//    @Param({"5"})
    private int numberOfFunctions;

    private Resource drlResource;


    @Setup
    public void createResource() {
        
        MVELDialect.packageClassLoaderFirst = pkgCLfirst;

        DRLProvider drlProvider;
        
        if (functionType.equals("drl")) {
            drlProvider = new SimpleRulesWithConstraintsAndFunctionProvider("Integer(this == ${i})", "int result = myFunction${i}();", numberOfFunctions, "return ${i};");
        } else if (functionType.equals("java")) {
            drlProvider = new SimpleRulesWithConstraintsAndStaticMethodProvider("Integer(this == ${i})", "int result = myFunction${i}();", numberOfFunctions);
        } else {
            throw new IllegalArgumentException("functionType " + functionType + " is not supported");
        }

        drlResource = KieServices.get().getResources()
                .newReaderResource(new StringReader(drlProvider.getDrl(numberOfRules)))
                .setResourceType(ResourceType.DRL)
                .setSourcePath("drlFile.drl");
    }

    @Benchmark
    public ReleaseId createKJarFromResource() throws IOException {
        return BuildtimeUtil.createKJarFromResources(useCanonicalModel, drlResource);
    }

}
