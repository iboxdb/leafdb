

import os;
os.environ["JAVA_HOME"] = "./target/jlink-image/"

import jpype
import jpype.imports
jpype.startJVM("--enable-native-access=ALL-UNNAMED")

from java.lang import Long, Double, String        
from iboxdb.localserver import Ason,DB

# Prototype
proto = Ason("id:",Long(0), "value:",String("_"))

class PyBox:
    auto = None
    n = 0
    def __init__(self,n=1):     
        DB.root("../DBRoot")
        self.n = n
        db = DB(n)
        cfg = db.getConfig()
        cfg.ensureTable(proto,"table","id")
        self.auto = db.open()
        pass
    def debug_clear(self):
        from iboxdb.localserver import BoxSystem
        BoxSystem.DBDebug.DeleteDBFiles(self.n)
    
    def new_id(self,p=0):
        return self.auto.newId(p)
    
    def insert(self,table,obj):
        return self.auto.insert(table,obj)
    
    def update(self,table,obj):
        return self.auto.update(table,obj)
    
    def delete(self,table,key):
        return self.auto.delete(table,key)
    
    def get(self,table,key):
        return self.auto.get(table,key)
    
    def select(self,ql, *params):
        return self.auto.select(ql,params)
    
    
auto = PyBox(1)
print(auto.auto.select.__doc__)
a = proto.clone()
a.s("id",10)
auto.insert("table",a)

b = auto.get("table",10)
print(b)

print(auto.select("from table id=? | id=? | id=?", 9,10,11))

auto.delete("table",10)
