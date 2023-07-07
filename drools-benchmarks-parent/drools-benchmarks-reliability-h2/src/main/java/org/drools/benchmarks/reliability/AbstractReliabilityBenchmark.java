/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.drools.benchmarks.reliability;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.util.RuntimeUtil;
import org.drools.reliability.h2mvstore.H2MVStoreStorageManager;
import org.kie.api.runtime.conf.PersistedSessionOption;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import static org.drools.benchmarks.reliability.AbstractReliabilityBenchmark.Mode.NONE;

public abstract class AbstractReliabilityBenchmark extends AbstractBenchmark {

    public enum Mode {
        NONE,
        H2
    }

    @Param({"NONE", "H2"})
    //@Param({"H2"})
    private Mode mode;

    @Param({"true", "false"})
    //@Param({"false"})
    private boolean useSafepoints;


    @Setup
    public void setupEnvironment() {
        H2MVStoreStorageManager.cleanUpDatabase();
    }

    @TearDown
    public void tearDownEnvironment() {
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        if (mode != NONE) {
            PersistedSessionOption option = PersistedSessionOption.newSession().withPersistenceStrategy(PersistedSessionOption.PersistenceStrategy.STORES_ONLY);
            if (useSafepoints) {
                option = option.withSafepointStrategy(PersistedSessionOption.SafepointStrategy.AFTER_FIRE);
            }
            kieSession = RuntimeUtil.createKieSession(kieBase, option);
        } else {
            kieSession = RuntimeUtil.createKieSession(kieBase);
        }

        populateKieSessionPerIteration();
    }

    protected abstract void populateKieSessionPerIteration();
}