### No Transaction Benchmark with MongoDB


#### Run
```
//close all IDE first

mvn clean
mvn install
mvn exec:java
```



#### Result

**2Cores + 8G**

```sql
threadCount= 100,000 batchCount= 10 reinterationSelect= 3 
 
iBoxDB
iBoxDB Insert: 1,000,000 AVG: 10,181 objects/s 
iBoxDB Update: 1,000,000 AVG: 11,243 objects/s 
iBoxDB Delete: 1,000,000 AVG: 9,984 objects/s 

MongoDB
MongoDB Insert: 1,000,000 AVG: 6,763 objects/s 
MongoDB Update: 1,000,000 AVG: 5,721 objects/s 
MongoDB Delete: 1,000,000 AVG: 6,293 objects/s 
```


**Transaction Benchmark see With-MySQL Test**

