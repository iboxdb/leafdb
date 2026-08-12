

# https://github.com/moditect/moditect
# [user@localhost site-packages]$ jdeps -s  org.jpype.jar 

echo "Install Java, Maven, Python. and "
echo "pip install jpype1"

mvn clean package exec:java

./target/jlink-image/bin/java --list-modules

./target/jlink-image/bin/java -cp ./target/jlink-image/jars/linkjdk-1.0.jar benchmark.BenchmarkDBTest

python jb.py




