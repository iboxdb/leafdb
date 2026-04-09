
### Database Benchmark ,  No Transaction Version

Databases Benchmark Version (3 & 4) with iBoxDB and MongoDB. 


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
$ cd mavenproject
$ mvn clean
$ mvn package exec:java
```



#### Result

**VM 2Cores + 8G**

```sql

Benchmark Version 1.4, Java=25
threadCount=100,000 batchCount=10 reinterationSelect=12 
-Xmx 4096 MB

iBoxDB
iBoxDB Insert: 1,000,000 AVG: 10,664 objects/s 
iBoxDB Update: 1,000,000 AVG: 20,243 objects/s 
iBoxDB Delete: 1,000,000 AVG: 8,495 objects/s 

MongoDB
MongoDB Insert: 1,000,000 AVG: 3,070 objects/s 
MongoDB Update: 1,000,000 AVG: 2,988 objects/s 
MongoDB Delete: 1,000,000 AVG: 3,165 objects/s

```


