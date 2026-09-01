print("jpype Testing...")

import os;
print(os.environ.get("JAVA_HOME"))
os.environ["JAVA_HOME"] = "./target/jlink-image/"
print(os.environ.get("JAVA_HOME"))

# pip install jpype1
import jpype
import jpype.imports

#from jpype.types import JLong, JDouble, JString 

from jpype import JImplements # ,JOverride
from jpype.types import JOverride

print("startJVM...");
jpype.startJVM("--enable-native-access=ALL-UNNAMED")
print("JVM: ", jpype.getDefaultJVMPath())
print("Ver: ", jpype.getJVMVersion())

#if using jlink, doesn't need to load jar.
#jpype.addClassPath("iboxdb-4.1.2.jar")

from java.lang import Long, Double, String # type: ignore
from java.math import BigDecimal # type: ignore         
from java.util import Date # type: ignore
from iboxdb.localserver import Ason,DB # type: ignore


from iboxdb.localserver.replication import IBoxRecycler # type: ignore
from iboxdb.localserver import IFunction # type: ignore

print( Ason.class_ )
print( DB.class_ )
print( IBoxRecycler.class_ )
print( IFunction.IFun.class_ )


#proto = Ason(["id:",JLong(0), "name:",JString("_"), "val:",JDouble(0.0)])
#print( proto.typeName("id"), proto.typeName("name"), proto.typeName("val") )
# Prototype
proto = Ason("id:",Long(0), "name:",String("_"), 
            "val:",Double(0.0), "star:", BigDecimal('0'),
            "ver:",Long(1),"wt:", Date())
print( proto.typeName("id"), proto.typeName("name"), proto.typeName("val") )

from iboxdb.localserver import BoxSystem # type: ignore
DB.root("../DBRoot")

BoxSystem.DBDebug.DeleteDBFiles(1)
db = DB(1)
cfg = db.getConfig()
cfg.ensureTable(proto,"table","id")
isUnique = False
cfg.ensureIndex(proto,"table",isUnique,"name(32)")
cfg.ensureIncrement(proto,"table","ver")

auto = db.open()

v = proto.clone()
v.set("id",auto.newId())
# s() == set()
v.s("name","testing")
print(auto.replace("table",v))
print(auto.select("from table limit 0, 10"))

#print(dir(auto))
#print(dir(proto))
#print(dir(IFunction.IFun))
@JImplements(IFunction.IFun)
class MyImpl(object):
    _str = ""
    def __init__(self,str):
        self._str = str
        pass
    @JOverride
    def execute(self, arg):
        print(self._str, arg, arg[0], arg[0][0:3])
        pass

DB.lock( IFunction(MyImpl("OneImpl")),  "OneNone" )

print("")
print(proto.clone.__doc__)


for x in range(5):
    v = proto.clone()
    v.set("id",auto.newId())
    v.set("name","Inc_" + str(x))
    v.set("val", x / 10)
    v.set("wt", Date(x*100000 + 88880000))
    auto.insert("table",v)

print(auto.select("from table limit 0, 10"))

auto.getDatabase().close()
BoxSystem.DBDebug.DeleteDBFiles(1)

jpype.shutdownJVM()
del jpype
