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

import java.io.IOException;
import java.io.StringReader;

import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class AlphaNodeRangeIndexingExecModelBenchmark extends AbstractOperatorsBenchmark {

    //@Param({ "4", "8", "32", "64", "128" })
    @Param({"4", "8", "32", "64"})
    protected int rulesAndFactsNumber; // actually, the number of rules is "rulesAndFactsNumber * 2"

    @Param({"false", "true"})
    //@Param({ "true" })
    protected boolean rangeIndexingEnabled;

    @Param({ "true", "false" })
    private boolean useCanonicalModel;

    @Setup
    public void setupKieBase() throws IOException {
        
        if (!rangeIndexingEnabled) {
            System.setProperty("drools.alphaNodeRangeIndex.enabled", "false"); // Default true
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append( "import org.drools.benchmarks.model.*;\n" );
        for (int i = 1; i <= rulesAndFactsNumber; i++) {
            sb.append(" rule " + RULENAME_PREFIX + "_asc" + i + "\n" +
                    " when \n " +
                    "     $account : Account(balance >= " + (i * 10000) + ")\n " +
                    " then\n " +
                    " end\n" );
            sb.append(" rule " + RULENAME_PREFIX + "_desc" + i + "\n" +
                    " when \n " +
                    "     $account : Account(balance < " + (i * 10000) + ")\n " +
                    " then\n " +
                    " end\n" );
        }

        //System.out.println(sb.toString());
        
        Resource drlResource = KieServices.get().getResources()
                .newReaderResource(new StringReader(sb.toString()))
                .setResourceType(ResourceType.DRL)
                .setSourcePath("drlFile.drl");
        
        ReleaseId releaseId = BuildtimeUtil.createKJarFromResources(useCanonicalModel, drlResource);
        KieContainer kieContainer = KieServices.get().newKieContainer(releaseId);

        kieBase = kieContainer.getKieBase();
    }

    @Benchmark
    public int test(final Blackhole eater) {
        int fired = runBenchmark(eater);
        //System.out.println("fired = " + fired);
        return fired;
    }
}
