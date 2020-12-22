package benchmark;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import java.io.*;

import iBoxDB.LocalServer.*;
import iBoxDB.LocalServer.IO.*;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/* iBoxDB.java Zero Setup */
 /*
mongodb-linux-x86_64-rhel70-4.2.3, mongodb-driver-sync
mkdir /tmp/mdata
./mongod --dbpath /tmp/mdata  
 */
public class BenchmarkDBTest {

    static int threadCount = 100_000;
    static int batchCount = 10;
    static int reinterationSelect = 6;

    public static void main(String[] args) {
        try {
            System.out.println("Benchmark ver= 1.1");
            System.out.format("threadCount= %,d batchCount= %,d reinterationSelect= %,d %n %n",
                    threadCount, batchCount, reinterationSelect);

            String root = "../"; //"/tmp"

            root = System.getProperty("user.home");
            root += File.separator;
            root += "TEST_LEAF_NOSQL";
            new File(root).mkdirs();

            System.out.format("PATH= %s %n", root);
            DB.root(root);

            System.out.println("iBoxDB");
            TestiBoxDB();
            System.out.println();

            System.gc();
            System.runFinalization();

            System.out.println("MongoDB");
            try {
                TestMongoDB();
            } catch (com.mongodb.MongoTimeoutException ex) {
                System.out.println("No MongoDB Server");
            }
            System.out.println("Test End.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void TestiBoxDB() {
        BoxSystem.DBDebug.DeleteDBFiles(1);

        try (AppServer server = new AppServer()) {
            final Database db = server.getInstance();
            final AutoBox auto = db.get();

            long watch = System.currentTimeMillis();
            final AtomicInteger count = new AtomicInteger(0);
            ExecutorService pool = CreatePool();
            for (int i = 0; i < threadCount; i++) {
                final int p = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < batchCount; i++) {
                            int id = (p * batchCount) + i;
                            auto.insert("T1", new T1(id, Integer.toString(id)));
                            count.incrementAndGet();
                        }

                        for (int t = 0; t < reinterationSelect; t++) {
                            int minId = p * batchCount + 0;
                            int maxId = p * batchCount + batchCount;
                            Iterator<T1> reader = auto
                                    .select(T1.class, "from T1 where Id>=? & Id<? order by Id",
                                            minId, maxId).iterator();
                            int ti = minId;
                            while (reader.hasNext()) {
                                T1 t1 = reader.next();
                                int iv = t1.getId();
                                if (ti != iv) {
                                    System.out.println("e");
                                    throw new RuntimeException(ti + "  " + iv);
                                }
                                ti++;
                            }
                            if (ti != maxId) {
                                System.out.println("e");
                                throw new RuntimeException();
                            }
                        }
                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            watch = System.currentTimeMillis() - watch;
            if (count.get() != (batchCount * threadCount)) {
                throw new RuntimeException(count + "  "
                        + (batchCount * threadCount));
            }
            int avg = (int) (count.get() / (watch / 1000.0));
            System.out.format("iBoxDB Insert: %,d AVG: %,d objects/s %n", count.get(), avg);

            // ----------------------Update------------------
            watch = System.currentTimeMillis();
            count.set(0);
            pool = CreatePool();
            for (int i = 0; i < threadCount; i++) {
                final int p = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < batchCount; i++) {
                            int id = (p * batchCount) + i;

                            T1 t1 = new T1();
                            t1.setId(id);
                            t1.setValue("S" + id);
                            if (auto.update("T1", t1)) {
                                count.incrementAndGet();
                            }
                        }

                        for (int t = 0; t < reinterationSelect; t++) {
                            int minId = p * batchCount + 0;
                            int maxId = p * batchCount + batchCount;
                            Iterator<T1> reader = auto
                                    .select(T1.class, "from T1 where Id>=? & Id<? order by Id",
                                            minId, maxId).iterator();
                            int ti = minId;
                            while (reader.hasNext()) {
                                T1 t1 = reader.next();
                                int iv = t1.getId();
                                if (ti != iv) {
                                    System.out.println("e");
                                    throw new RuntimeException(ti + "  " + iv);
                                }
                                if (!("S" + ti).equals(t1.getValue())) {
                                    System.out.println("e2");
                                    throw new RuntimeException(ti + "  " + iv);
                                }
                                ti++;
                            }
                            if (ti != maxId) {
                                System.out.println("e");
                                throw new RuntimeException();
                            }
                        }

                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            watch = System.currentTimeMillis() - watch;
            if (count.get() != (batchCount * threadCount)) {
                throw new RuntimeException(count + "  "
                        + (batchCount * threadCount));
            }
            avg = (int) (count.get() / (watch / 1000.0));
            System.out.format("iBoxDB Update: %,d AVG: %,d objects/s %n", count.get(), avg);

            // ------------------------Delete------------------
            watch = System.currentTimeMillis();
            count.set(0);
            pool = CreatePool();
            for (int i = 0; i < threadCount; i++) {
                final int p = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int t = 0; t < reinterationSelect; t++) {
                            int minId = p * batchCount + 0;
                            int maxId = p * batchCount + batchCount;
                            Iterator<T1> reader = auto
                                    .select(T1.class, "from T1 where Id>=? & Id<? order by Id",
                                            minId, maxId).iterator();
                            int ti = minId;
                            while (reader.hasNext()) {
                                T1 t1 = reader.next();
                                int iv = t1.getId();
                                if (ti != iv) {
                                    System.out.println("e");
                                    throw new RuntimeException(ti + "  " + iv);
                                }
                                if (!("S" + ti).equals(t1.getValue())) {
                                    System.out.println("e2");
                                    throw new RuntimeException(ti + "  " + iv);
                                }
                                ti++;
                            }
                            if (ti != maxId) {
                                System.out.println("e");
                                throw new RuntimeException();
                            }
                        }

                        for (int i = 0; i < batchCount; i++) {
                            int id = (p * batchCount) + i;
                            if (auto.delete("T1", id)) {
                                count.incrementAndGet();
                            }
                        }

                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            watch = System.currentTimeMillis() - watch;
            if (count.get() != (batchCount * threadCount)) {
                throw new RuntimeException(count + "  "
                        + (batchCount * threadCount));
            }
            avg = (int) (count.get() / (watch / 1000.0));
            System.out.format("iBoxDB Delete: %,d AVG: %,d objects/s %n", count.get(), avg);

            if (auto.selectCount("from T1") != 0) {
                throw new RuntimeException("SC");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void TestMongoDB() throws Exception {

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);

        MongoClient mongoClient = MongoClients.create();
        try {
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            MongoDatabase database = mongoClient.getDatabase("test").withCodecRegistry(pojoCodecRegistry);
            MongoCollection<T1> coll = database.getCollection("T1", T1.class);
            coll.drop();

            long watch = System.currentTimeMillis();
            final AtomicInteger count = new AtomicInteger(0);
            ExecutorService pool = CreatePool();
            for (int i = 0; i < threadCount; i++) {
                final int p = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < batchCount; i++) {
                            int id = (p * batchCount) + i;
                            coll.insertOne(new T1(id, Integer.toString(id)));
                            count.incrementAndGet();

                        }

                        for (int t = 0; t < reinterationSelect; t++) {
                            int minId = p * batchCount + 0;
                            int maxId = p * batchCount + batchCount;
                            Bson q = and(gte("_id", minId), lt("_id", maxId));

                            try (MongoCursor<T1> reader = coll.find(q).iterator()) {
                                int ti = minId;
                                while (reader.hasNext()) {
                                    T1 t1 = reader.next();
                                    int iv = t1.getId();
                                    if (ti != iv) {
                                        System.out.println("e");
                                        throw new RuntimeException(ti + "  " + iv);
                                    }
                                    ti++;
                                }
                                if (ti != maxId) {
                                    System.out.println("e");
                                    throw new RuntimeException();
                                }
                            }
                        }

                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            watch = System.currentTimeMillis() - watch;
            if (count.get() != (batchCount * threadCount)) {
                throw new Exception(count + "  " + (batchCount * threadCount));
            }
            int avg = (int) (count.get() / (watch / 1000.0));

            System.out.format("MongoDB Insert: %,d AVG: %,d objects/s %n", count.get(), avg);

            // ---------------Update-----------------------------
            watch = System.currentTimeMillis();
            count.set(0);
            pool = CreatePool();
            for (int i = 0; i < threadCount; i++) {
                final int p = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < batchCount; i++) {
                            int id = (p * batchCount) + i;
                            if (coll.updateOne(eq("_id", id), set("value", "S" + id)).getModifiedCount() == 1) {
                                count.incrementAndGet();
                            }
                        }

                        for (int t = 0; t < reinterationSelect; t++) {
                            int minId = p * batchCount + 0;
                            int maxId = p * batchCount + batchCount;
                            Bson q = and(gte("_id", minId), lt("_id", maxId));

                            try (MongoCursor<T1> reader = coll.find(q).iterator()) {
                                int ti = minId;
                                while (reader.hasNext()) {
                                    T1 t1 = reader.next();
                                    int iv = t1.getId();
                                    if (ti != iv) {
                                        System.out.println("e");
                                        throw new RuntimeException(ti + "  " + iv);
                                    }
                                    if (!("S" + ti).equals(t1.getValue())) {
                                        System.out.println("e2");
                                        throw new RuntimeException(ti + "  " + iv);
                                    }
                                    ti++;
                                }
                                if (ti != maxId) {
                                    System.out.println("e");
                                    throw new RuntimeException();
                                }
                            }
                        }
                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            watch = System.currentTimeMillis() - watch;
            if (count.get() != (batchCount * threadCount)) {
                throw new Exception(count + "  " + (batchCount * threadCount));
            }
            avg = (int) (count.get() / (watch / 1000.0));
            System.out.format("MongoDB Update: %,d AVG: %,d objects/s %n", count.get(), avg);

            //--------------- Delete --------------------
            watch = System.currentTimeMillis();
            count.set(0);
            pool = CreatePool();
            for (int i = 0; i < threadCount; i++) {
                final int p = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int t = 0; t < reinterationSelect; t++) {
                            int minId = p * batchCount + 0;
                            int maxId = p * batchCount + batchCount;
                            Bson q = and(gte("_id", minId), lt("_id", maxId));

                            try (MongoCursor<T1> reader = coll.find(q).iterator()) {
                                int ti = minId;
                                while (reader.hasNext()) {
                                    T1 t1 = reader.next();
                                    int iv = t1.getId();
                                    if (ti != iv) {
                                        System.out.println("e");
                                        throw new RuntimeException(ti + "  " + iv);
                                    }
                                    if (!("S" + ti).equals(t1.getValue())) {
                                        System.out.println("e2");
                                        throw new RuntimeException(ti + "  " + iv);
                                    }
                                    ti++;
                                }
                                if (ti != maxId) {
                                    System.out.println("e");
                                    throw new RuntimeException();
                                }
                            }
                        }

                        for (int i = 0; i < batchCount; i++) {
                            int id = (p * batchCount) + i;
                            if (coll.deleteOne(eq("_id", id)).getDeletedCount() == 1) {
                                count.incrementAndGet();
                            }
                        }
                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            watch = System.currentTimeMillis() - watch;
            if (count.get() != (batchCount * threadCount)) {
                throw new Exception(count + "  " + (batchCount * threadCount));
            }
            avg = (int) (count.get() / (watch / 1000.0));
            System.out.format("MongoDB Delete: %,d AVG: %,d objects/s %n", count.get(), avg);
            //------------------End------------- 

            if (coll.countDocuments() != 0) {
                throw new RuntimeException("SC");
            }
        } finally {
            mongoClient.close();
        }
    }

    private static ExecutorService CreatePool() {
        return Executors.newFixedThreadPool(8);
    }

    public static class T1 {

        public T1() {
        }

        public T1(int _id, String _value) {
            this.id = _id;
            this.value = _value;
        }
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int _id) {
            id = _id;
        }

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String _value) {
            value = _value;
        }

        public static <T> List<T> toArray(Iterable<T> it) {
            ArrayList<T> list = new ArrayList<>();
            for (T t : it) {
                list.add(t);
            }
            return list;
        }
    }

    public static class AppServer extends LocalDatabaseServer {

        @Override
        protected DatabaseConfig BuildDatabaseConfig(long address) {

            return new FileConfig();

        }

        public static class FileConfig extends BoxFileStreamConfig {

            public FileConfig() {
                CacheLength = mb(300);
                ensureTable(T1.class, "T1", "Id");
            }
        }

    }

}
