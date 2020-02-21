package benchmark;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import iBoxDB.LocalServer.*;
import iBoxDB.LocalServer.IO.*;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.conversions.Bson;

//  iBoxDB.java v2.15
/*
mongodb-linux-x86_64-rhel70-4.2.3, mongodb-driver-sync
mkdir /tmp/mdata
./mongod --dbpath /tmp/mdata  
 */
public class BenchmarkDBTest {

    static int threadCount = 10000; //100_000;
    static int batchCount = 10;

    public static void main(String[] args) {
        try {

            System.out.println("threadCount=" + threadCount + " , batchCount="
                    + batchCount);

            iBoxDB.LocalServer.DB.root("/tmp");
            System.out.println("iBoxDB");
            TestiBoxDB();
            System.out.println();

            System.gc();
            System.runFinalization();

            System.out.println("MongoDB");
            TestMongoDB();

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
                        }

                        {
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
                                count.incrementAndGet();
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
            System.out.println("iBoxDB Insert:" + count.get()
                    + "  AVG:" + avg + " objects/s");

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
            System.out.println("iBoxDB Update:" + count.get() + "  AVG:" + avg
                    + " objects/s");

            // ------------------------Delete------------------
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
            System.out.println("iBoxDB Delete:" + count.get() + "  AVG:" + avg
                    + " objects/s");

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
                    }

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
                            count.incrementAndGet();
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
            throw new Exception(count + "  " + (batchCount * threadCount));
        }
        int avg = (int) (count.get() / (watch / 1000.0));
        System.out.println("MongoDB Insert:"
                + Integer.toString(count.get()) + "  AVG:"
                + Integer.toString(avg) + " objects/s");

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
        System.out.println("MongoDB Update:" + Integer.toString(count.get())
                + "  AVG:" + Integer.toString(avg) + " objects/s");

        //--------------- Delete --------------------
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
        System.out.println("MongoDB Delete:" + Integer.toString(count.get())
                + "  AVG:" + Integer.toString(avg) + " objects/s");
        //------------------End------------- 

        if (coll.countDocuments() != 0) {
            throw new RuntimeException("SC");
        }
        mongoClient.close();
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
                CacheLength = mb(512);
                EnsureTable(T1.class, "T1", "Id");
            }
        }

    }

}