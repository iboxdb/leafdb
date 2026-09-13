

import os;
os.environ["JAVA_HOME"] = "./target/jlink-image/"


__all__ = ["PyBox","PyBoxConfig", "db_root",  "db_debug_deletefile", 
           "db_tostring", "DBException",

           "Ason", "Long", "Double" , "String", "BigDecimal", "Date", "Boolean",
           "Bytes","Objects","Dict",

           "copy", "datetime", "timezone"] 


import jpype
import jpype.imports

#convertStrings: force Java strings to cast to Python strings
jpype.startJVM("--enable-native-access=ALL-UNNAMED", convertStrings=False)
 
from jpype import JByte,JObject,JException
from java.lang import Long, Double, String, Boolean # type: ignore
from java.math import BigDecimal # type: ignore         
from java.util import HashMap, Date # type: ignore
from iboxdb.localserver import Ason,DB,Local # type: ignore

Bytes = JByte[:]
Objects = JObject[:]
Dict = HashMap
DBException = JException

def db_tostring( net ):
    return DB.toString(net)

def db_root( path:str ):
    return DB.root(path)
    
def db_debug_deletefile( *addr:int ):
    from iboxdb.localserver import BoxSystem # type: ignore
    return BoxSystem.DBDebug.DeleteDBFiles(addr)

from datetime import datetime,timezone
from copy import copy as pycopy

# object is reference, needs copy before modifying.
# but Python is single-threaded, it is fine to do whatever.
def copy(obj):
    if isinstance(obj, (Ason,Local)):
        return obj.clone()
    return pycopy(obj)

# use timestamp(Double) better than using Date()
# datetime.now(timezone.utc).timestamp()
# datetime.fromtimestamp(x.timestamp(),timezone.utc)
def datetime2date(x:datetime):
    return Date(x.year-1900,x.month-1,x.day,x.hour,x.minute,x.second)
def date2datetime(d:Date):
    return datetime(d.getYear()+1900,d.getMonth()+1,d.getDate(),
      d.getHours(), d.getMinutes(), d.getSeconds())

class PyBoxConfig:

    _cfg = 0
    def __init__(self, cfg):
        super().__init__()
        self._cfg = cfg

    def ensure_table(self,proto:Ason,table:str,*key:str):
        self._cfg.ensureTable(proto,table,key)
        return self
 
    def ensure_index(self,proto:Ason,table:str,isUnique:bool=False,*names:str):
        self._cfg.ensureIndex(proto,table,isUnique,names)
        return self

    def ensure_increment(self,proto:Ason,table:str,*names:str):
        self._cfg.ensureIncrement(proto,table,names)
        return self

    def cache_length(self,size:int):
        if size > 0:
            self._cfg.CacheLength = size
        return self._cfg.CacheLength


class PyBox:
    _auto = None
    _db = None
    _addr = 0

    def __init__(self,addr=1):     
        super().__init__()
        self._addr = addr
        self._db = DB(self._addr)

    def get_config(self) -> PyBoxConfig:
        cfg = self._db.getConfig()
        return PyBoxConfig(cfg)
        #  help(cfg)
        
    def open(self):
        self._auto = self._db.open()
        return self

    def close(self):
        if self._auto != None :
            self._auto.getDatabase().close()
            self._auto = None
            self._db = None
    
    # ==== methods ====
    def new_id(self,pos=0,step=1):
        return self._auto.newId(pos,step)
    
    def insert(self,table,ason):
        return self._auto.insert(table,ason)
    
    def update(self,table,ason):
        return self._auto.update(table,ason)
    
    def replace(self,table,ason):
        return self._auto.replace(table,ason)

    def delete(self,table, *key):
        return self._auto.delete(table,key)

    def get(self,table, *key):
        return self._auto.get(table,key)
    
    def select(self,ql, *params):
        return self._auto.select(ql,params)
    
    def count(self,ql, *params):
        return self._auto.count(ql,params)



if __name__ == "__main__" :
    print( Bytes([1,2,3]).__class__ )
    print( Objects([1,2,3]).__class__ )
    print( date2datetime(datetime2date(datetime.now())) )
    print( datetime.fromtimestamp(Double(datetime.now().timestamp())) )
    jmap = Dict()
    jmap["key"] = "name"
    jmap["bb"] = Boolean(True)
    jmap["bs"] = Bytes([4,5,6])
    jmap["os"] = Objects([4,None,6, Dict({"id":11}) ])
    print( db_tostring(jmap) )
    db_root("../TEST_LEAF_NOSQL")
    db_debug_deletefile(17)
    print(DBException)
    pass
