
### Database Benchmark ,  No Transaction Version

Databases Benchmark Version 3 with iBoxDB and MongoDB. 


**with Transaction Benchmark see With-MySQL Test**



### Prepare


**Stop Tracker daemon** 

[Why does Tracker consume resources on my PC?](https://gnome.pages.gitlab.gnome.org/tracker/faq/#why-does-tracker-consume-resources-on-my-pc)

```sh
[user@localhost ~]$ tracker daemon -k
```


[Install Java](https://jdk.java.net/)

[Install Maven](https://maven.apache.org/)



#### Run
```
//close all IDE first

mvn clean
mvn package exec:java
```



#### Result

**VM 2Cores + 8G**

```sql
Benchmark Version 1.3, Java=25
threadCount=100,000 batchCount=10 reinterationSelect=12
iBoxDB
iBoxDB Insert: 1,000,000 AVG: 10,979 objects/s 
iBoxDB Update: 1,000,000 AVG: 19,703 objects/s 
iBoxDB Delete: 1,000,000 AVG: 11,684 objects/s 

MongoDB
MongoDB Insert: 1,000,000 AVG: 1,875 objects/s 
MongoDB Update: 1,000,000 AVG: 1,738 objects/s 
MongoDB Delete: 1,000,000 AVG: 1,835 objects/s 

```


