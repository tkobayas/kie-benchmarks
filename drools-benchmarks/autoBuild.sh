cd /home/tkobayas/usr/work/DROOLS-5647/drools-OneTreeMap/drools/drools-core
mvn clean install -DskipTests
cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks/drools-benchmarks
mvn clean install -DskipTests
cp target/drools-benchmarks.jar ./drools-benchmarks-OneTreeMap.jar
cd /home/tkobayas/usr/work/DROOLS-5647/drools-OneRBTree/drools/drools-core
mvn clean install -DskipTests
cd /home/tkobayas/usr/work/DROOLS-5647/kie-benchmarks/drools-benchmarks
mvn clean install -DskipTests
cp target/drools-benchmarks.jar ./drools-benchmarks-OneRBTree.jar

