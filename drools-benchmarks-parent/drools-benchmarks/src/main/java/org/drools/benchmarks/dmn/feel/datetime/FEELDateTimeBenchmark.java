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

package org.drools.benchmarks.dmn.feel.datetime;

import org.drools.benchmarks.dmn.feel.AbstractFEELBenchmark;
import org.openjdk.jmh.annotations.Param;

public class FEELDateTimeBenchmark extends AbstractFEELBenchmark {

    @Param({"date and time(\"2016-07-29T05:48:23\")",
            "date and time( 2016, 7, 29, 5, 48, 23 )",
            "date and time(\"2016-07-29T05:48:23-05:00\")",
            "date and time(date(\"2016-07-29\"), time(\"05:48:23-05:00\") )"})
    private String expression;

    @Override
    public String getExpression() {
        return expression;
    }
}
