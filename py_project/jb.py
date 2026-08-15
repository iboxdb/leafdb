print("Testing...")

import os;
print(os.environ.get("JAVA_HOME"))
os.environ["JAVA_HOME"] = "./target/jlink-image/"
print(os.environ.get("JAVA_HOME"))

# pip install jpype1
import jpype
import jpype.imports
from jpype.types import JLong, JDouble, JString 
 
print("startJVM...");
jpype.startJVM("--enable-native-access=ALL-UNNAMED")
print("JVM: ", jpype.getDefaultJVMPath())
print("Ver: ", jpype.getJVMVersion())

#if using jlink, doesn't need to load jar.
#jpype.addClassPath("iboxdb-4.1.2.jar")

from java.lang import Long, Double, String # type: ignore
from java.math import BigDecimal # type: ignore         
from iboxdb.localserver import Ason,DB # type: ignore

print("",Ason.class_,DB.class_)

# Prototype
proto = Ason(["id:",JLong(0), "name:",JString("_"), "val:",JDouble(0.0)])
print( proto.typeName("id"), proto.typeName("name"), proto.typeName("val") )

proto = Ason("id:",Long(0), "name:",String("_"), "val:",Double(0.0), "star", BigDecimal('0'))
print( proto.typeName("id"), proto.typeName("name"), proto.typeName("val") )

from iboxdb.localserver import BoxSystem # type: ignore
DB.root("../DBRoot")

BoxSystem.DBDebug.DeleteDBFiles(1)
db = DB(1)
cfg = db.getConfig()
cfg.ensureTable(proto,"table","id")
isUnique = False
cfg.ensureIndex(proto,"table",isUnique,"name")
auto = db.open()
v = proto.clone()
v.set("id",auto.newId())
# s() == set()
v.s("name","testing")

print(auto.replace("table",v))
print(auto.select("from table limit 0, 10"))
print(dir(auto))
print(dir(proto))
print(proto.clone.__doc__)

auto.getDatabase().close()
BoxSystem.DBDebug.DeleteDBFiles(1)

jpype.shutdownJVM()
del jpype
