

import os;
os.environ["JAVA_HOME"] = "./target/jlink-image/"

import jpype
import jpype.imports

#convertStrings: force Java strings to cast to Python strings
jpype.startJVM("--enable-native-access=ALL-UNNAMED", convertStrings=False)

#if using jlink, doesn't need to load jar.
#jpype.addClassPath("iboxdb-4.1.2.jar")

from java.lang import Long, Double, String # type: ignore
from iboxdb.localserver import Ason,DB # type: ignore

# Prototype
proto = Ason("id:",Long(0), "value:",String("_"))

class PyBox:
    auto = None
    n = 0
    def __init__(self,n=1,root="../DBRoot"):     
        DB.root(root)
        self.n = n
    
    def create_db(self):
        db = DB(self.n)
        cfg = db.getConfig()
        cfg.ensureTable(proto,"table","id")
        self.auto = db.open()

    def close_db(self):
        if self.auto != None :
            self.auto.getDatabase().close()
            self.auto = None

    def debug_clear(self):
        if self.auto != None :
            return False
        from iboxdb.localserver import BoxSystem # type: ignore
        return BoxSystem.DBDebug.DeleteDBFiles(self.n)
    
    def new_id(self):
        return self.auto.newId()
    
    def insert(self,table,obj):
        return self.auto.insert(table,obj)
    
    def update(self,table,obj):
        return self.auto.update(table,obj)
    
    def replace(self,table,obj):
        return self.auto.replace(table,obj)

    def delete(self,table,key):
        return self.auto.delete(table,key)

    def get(self,table,key):
        return self.auto.get(table,key)
    
    def select(self,ql, *params):
        return self.auto.select(ql,params)
    
    def count(self,ql, *params):
        return self.auto.count(ql,params)
    
        
import datetime as dt    
print("iBoxDB Python Single Thread Testing : ")

py = PyBox(1)
py.debug_clear()
py.create_db()

re_select = 5
total = 100_000
#total = 10

begin = dt.datetime.now()
for i in range(1,total+1):
    v = proto.clone()
    v.s("id",py.new_id())
    v.s("value",str(i))
    if not py.insert("table",v):
        print("Check Insert")
    pass
watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Insert AVG: {total // watch :,}") 

begin = dt.datetime.now()
for i in range(1,total+1):
    v = py.get("table",i)
    v = v.clone()
    v.s("value", "UP:" + str(i))
    if not py.update("table",v):
        print("Check Update")
    pass
watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Update AVG: {total // watch :,}")

fc = py.count("from table")
print(f"Count     : {fc :,}")

begin = dt.datetime.now()
for i in range(1,total+1):
    if not py.delete("table",i):
        print("Check Delete")
    pass
watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Delete AVG: {total // watch :,}")

fc = py.count("from table")
print(f"Count     : {fc:,}")


begin = dt.datetime.now()
for i in range(1,total+1):
    v = proto.clone()
    v.s("id",i)
    v.s("value",str(i))
    if not py.replace("table",v):
        print("Check Replace 1")
    pass
watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Replace-1 AVG: {total // watch :,}") 
fc = py.count("from table")
print(f"Count        : {fc:,}")


begin = dt.datetime.now()
for i in range(1,total+1):
    v = proto.clone()
    v.s("id",i)
    v.s("value", "up" + str(i))
    if not py.replace("table",v):
        print("Check Replace 2")
    pass
watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Replace-2 AVG: {total // watch :,}") 
fc = py.count("from table")
print(f"Count        : {fc:,}")

for i in range(1,total+1):
    v = py.get("table",i)
    if not str(v["value"]).startswith("up") : 
        print("Check Replace 3")

get_count = 0
begin = dt.datetime.now()
for t in range(re_select):
    for i in range(1,total+1):
        v = py.get("table",i)
        #it is not None
        if v["id"] != i :
            print("Check Get")
        else:
            get_count = get_count + 1

watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Get()  AVG: {get_count // watch :,}/s") 
get_count = 0

#Re-Open
py.close_db()
del py

py = PyBox(1)
py.create_db()

sel_count = 0
begin = dt.datetime.now()
for t in range(re_select):
    print(f"Time:{t+1} / {re_select}")
    for i in range(1,total+1):
        st = py.select("from table where id>=? & id<=?", i, i+64)
        for s in st:
            sel_count = sel_count + 1
        del st
        
watch = (dt.datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Select AVG: {sel_count // watch :,}/s") 

py.close_db()

print("End.")
