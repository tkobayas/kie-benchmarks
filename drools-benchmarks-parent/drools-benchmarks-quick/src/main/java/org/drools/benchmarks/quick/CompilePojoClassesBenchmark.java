/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.benchmarks.quick;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.kie.memorycompiler.CompilationResult;
import org.kie.memorycompiler.JavaCompiler;
import org.kie.memorycompiler.resources.MemoryResourceReader;
import org.kie.memorycompiler.resources.MemoryResourceStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class CompilePojoClassesBenchmark {

    @Param({"NATIVE", "ECLIPSE"})
    private String compilerType;

    @Param({"10", "100", "1000"})
    private int numberOfClasses;

    private JavaCompiler compiler;
    private String[] resourcePaths;
    private MemoryResourceReader reader;

    @Setup(Level.Trial)
    public void setup() {
        if ("ECLIPSE".equals(compilerType)) {
            compiler = JavaCompiler.createEclipseCompiler();
        } else {
            compiler = JavaCompiler.createNativeCompiler();
        }

        reader = new MemoryResourceReader();
        resourcePaths = new String[numberOfClasses];

        for (int i = 0; i < numberOfClasses; i++) {
            String className = "Pojo" + i;
            String path = "src/main/java/org/drools/benchmarks/generated/" + className + ".java";
            String source = generatePojoSource(className, i);
            reader.add(path, source.getBytes(StandardCharsets.UTF_8));
            resourcePaths[i] = path;
        }
    }

    @Benchmark
    public void compile(Blackhole bh) {
        MemoryResourceStore store = new MemoryResourceStore();
        CompilationResult result = compiler.compile(resourcePaths, reader, store, getClass().getClassLoader());
        if (result.getErrors().length > 0) {
            throw new RuntimeException("Compilation failed: " + result.getErrors()[0].getMessage());
        }
        bh.consume(store);
    }

    private static String generatePojoSource(String className, int index) {
        return "package org.drools.benchmarks.generated;\n" +
                "\n" +
                "public class " + className + " {\n" +
                "\n" +
                "    private int id;\n" +
                "    private String name;\n" +
                "    private double value;\n" +
                "\n" +
                "    public " + className + "() {}\n" +
                "\n" +
                "    public " + className + "(int id, String name, double value) {\n" +
                "        this.id = id;\n" +
                "        this.name = name;\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "    public int getId() { return id; }\n" +
                "    public void setId(int id) { this.id = id; }\n" +
                "\n" +
                "    public String getName() { return name; }\n" +
                "    public void setName(String name) { this.name = name; }\n" +
                "\n" +
                "    public double getValue() { return value; }\n" +
                "    public void setValue(double value) { this.value = value; }\n" +
                "\n" +
                "    @Override\n" +
                "    public String toString() {\n" +
                "        return \"" + className + "{id=\" + id + \", name=\" + name + \", value=\" + value + \"}\";\n" +
                "    }\n" +
                "}\n";
    }
}
