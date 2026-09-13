
### Run project

```sh
python test_iboxdb.py
```

### Packing iBoxDB with JDK and running on Python.

Using **moditect jlink** to build a custom **JDK** with **iBoxDB** inside [pom.xml](pom.xml) , 
and load this JDK to Python by using **jpype1** [test_iboxdb.py](test_iboxdb.py)



#### Linux

```sh
echo "Online..."
echo "1. Install Java, Maven"
echo "2. sudo dnf install python3.14"
echo "3. pip install jpype1"

mvn clean package exec:java

./target/jlink-image/bin/java --list-modules

./target/jlink-image/bin/java -cp ./target/jlink-image/jars/linkjdk-1.0.jar benchmark.BenchmarkDBTest

python --version
python test_iboxdb.py

read -p "exit?"
```


#### Windows

```bat
echo "Online..."
echo "1. Install Java, Maven"
echo "2. sudo dnf install python3.14"
echo "3. pip install jpype1"

call mvn clean package exec:java

.\target\jlink-image\bin\java --list-modules

.\target\jlink-image\bin\java -cp .\target\jlink-image\jars\linkjdk-1.0.jar benchmark.BenchmarkDBTest

python --version
python test_iboxdb.py

pause
```


[more Doc](doc.txt)

