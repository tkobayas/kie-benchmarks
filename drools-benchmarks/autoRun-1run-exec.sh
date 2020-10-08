cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks/drools-benchmarks
java -jar ./drools-benchmarks-OneTreeMap.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneTreeMap.csv -f 1 -wi 1 -i 1 org.drools.benchmarks.operators.AlphaNodeRangeIndexingExecModelBenchmark 
java -jar ./drools-benchmarks-OneRBTree.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneRBTree.csv -f 1 -wi 1 -i 1 org.drools.benchmarks.operators.AlphaNodeRangeIndexingExecModelBenchmark 

