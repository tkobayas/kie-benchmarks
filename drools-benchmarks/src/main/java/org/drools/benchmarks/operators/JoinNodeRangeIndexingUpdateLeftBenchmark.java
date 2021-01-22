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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.benchmarks.common.AbstractBenchmark;
import org.drools.benchmarks.common.util.BuildtimeUtil;
import org.drools.benchmarks.common.util.RuntimeUtil;
import org.drools.benchmarks.model.Account;
import org.drools.benchmarks.model.Transaction;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.util.index.IndexTestUtil;
import org.kie.api.runtime.rule.FactHandle;
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
public class JoinNodeRangeIndexingUpdateLeftBenchmark extends AbstractBenchmark {

    protected static final String ACCOUNT_PREFIX = "Account";
    protected static final String TRANSACTION_PREFIX = "Transaction";

    @Param({"1", "16", "128", "256"})
//    @Param({"1", "8"})
    protected int _accountNum;

    @Param({"1", "16", "128", "256"})
//    @Param({"1", "8"})
    protected int _transactionNum;

    @Param({"false", "true"})
    protected boolean rangeIndexEnabled;

    @Param({"One", "Half", "All"})
//    @Param({"One"})
    protected String match;
    
    
    private List<Account> accounts;
    private List<Transaction> transactions;

//    private List<InternalFactHandle> accountHandles;
    private List<InternalFactHandle> transactionHandles;

    private double maxBalance;
    
    
    @Setup
    public void setupKieBase() {

        String drl = "import org.drools.benchmarks.model.*;\n" +
                     "global java.util.List result;\n" +
                     " rule R1\n" +
                     " when \n " +
                     "     $a : Account()\n " +
                     "     $t : Transaction(amount > $a.balance)\n " +
                     " then\n " +
//                     "     System.out.println(\"Account : \" + $a.getName() + \" [\" + $a.getBalance() + \"], Transaction : \" + $t.getDescription() + \" [\" + $t.getAmount() +\"]\" );\n" +
                     "     result.add($t);\n" +
                     " end\n";

//        System.out.println(drl);

        if (rangeIndexEnabled) {
            IndexTestUtil.enableRangeIndexForJoin();
        } else {
            IndexTestUtil.disableRangeIndexForJoin();
        }
        kieBase = BuildtimeUtil.createKieBaseFromDrl(drl);
    }

    @Setup
    public void generateFacts() {
        accounts = new ArrayList<>();
        for (int i = 1; i <= _accountNum; i++) {
            final Account account = new Account();
            account.setBalance(i * 10000d);
            account.setName(ACCOUNT_PREFIX + i);
            accounts.add(account);
        }
        maxBalance = _accountNum * 10000d;
        

        transactions = new ArrayList<>();
        for (int i = 1; i <= _transactionNum; i++) {
            final Transaction transaction = new Transaction();
            transaction.setAmount(i * 10000d);
            transaction.setDescription(TRANSACTION_PREFIX + i);
            transactions.add(transaction);
        }
    }

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        kieSession = RuntimeUtil.createKieSession(kieBase);
        List<Transaction> result = new ArrayList<>();
        kieSession.setGlobal("result", result);
        
        // Insert everything first
//        accountHandles = new ArrayList<>();
        for (Account account : accounts) {
            InternalFactHandle handle = (InternalFactHandle)kieSession.insert(account);
//            accountHandles.add(handle);
        }
        transactionHandles = new ArrayList<>();
        for (Transaction transaction : transactions) {
            InternalFactHandle handle = (InternalFactHandle)kieSession.insert(transaction);
            transactionHandles.add(handle);
        }
        
        // Make sure fire once
        kieSession.fireAllRules();
//        System.out.println("---> setup done\n");
    }

    @Benchmark
    public int test(final Blackhole eater) {
        for (InternalFactHandle handle : transactionHandles) {
            Transaction transaction = (Transaction)handle.getObject();
            if (match.equals("One")) {
                transaction.setAmount(15000d); // match only one Account
            } else if (match.equals("Half")) {
                transaction.setAmount(maxBalance / 2 + 5000d); // match half of the Accounts
            } else if (match.equals("All")) {
                transaction.setAmount(maxBalance + 5000d); // match all Accounts
            } else {
                throw new RuntimeException("Wrong match value");
            }
            kieSession.update(handle, transaction);
        }
        int fired = kieSession.fireAllRules();
//        System.out.println("fired = " + fired);
        return fired;
    }
}
