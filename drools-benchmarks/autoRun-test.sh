cd /home/tkobayas/usr/work/DROOLS-5647/drools-OneTreeMap/drools/drools-core
mvn clean install -DskipTests
cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks/drools-benchmarks
mvn clean install -DskipTests
java -jar target/drools-benchmarks.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneTreeMap.csv -f 1 -wi 1 -i 1 org.drools.benchmarks.operators.AlphaNodeRangeIndexingBenchmark 
cd /home/tkobayas/usr/work/DROOLS-5647/drools-OneRBTree/drools/drools-core
mvn clean install -DskipTests
cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks/drools-benchmarks
mvn clean install -DskipTests
java -jar target/drools-benchmarks.jar -jvmArgs "-Xms4g -Xmx4g" -foe true -rf csv -rff results-OneRBTree.csv -f 1 -wi 1 -i 1 org.drools.benchmarks.operators.AlphaNodeRangeIndexingBenchmark 

