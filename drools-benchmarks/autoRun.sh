cd /home/tkobayas/usr/work/DROOLS-5647/drools-TwoRBTrees/drools/drools-core
mvn clean install -DskipTests
cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks-OneTreeMap/drools-benchmarks
mvn clean install -DskipTests
java -jar target/drools-benchmarks.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-TwoRBTrees.csv -f 5 -wi 100000 -i 10000 org.drools.benchmarks.operators.AlphaNodeRangeIndexingBenchmark 
cd /home/tkobayas/usr/work/DROOLS-5647/drools-OneTreeMap/drools/drools-core
mvn clean install -DskipTests
cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks-OneTreeMap/drools-benchmarks
mvn clean install -DskipTests
java -jar target/drools-benchmarks.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneTreeMap.csv -f 5 -wi 100000 -i 10000 org.drools.benchmarks.operators.AlphaNodeRangeIndexingBenchmark 

