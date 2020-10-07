cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks/drools-benchmarks
java -jar ./drools-benchmarks-OneTreeMap.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneTreeMap.csv -f 5 -wi 100000 -i 10000 org.drools.benchmarks.operators.AlphaNodeRangeIndexingBenchmark 
java -jar ./drools-benchmarks-OneRBTree.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneRBTree.csv -f 5 -wi 100000 -i 10000 org.drools.benchmarks.operators.AlphaNodeRangeIndexingBenchmark 

