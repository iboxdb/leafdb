print("iBoxDB Java Python!")

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

#if using jlink, doesn't need to load this.
#jpype.addClassPath("iboxdb-4.1.2.jar")

from java.lang import Long, Double, String
from java.math import BigDecimal          
from iboxdb.localserver import Ason,DB

print("",Ason.class_,DB.class_)

# Prototype
proto = Ason(["id:",JLong(0), "name:",JString("_"), "val:",JDouble(0.0)])
print( proto.typeName("id"), proto.typeName("name"), proto.typeName("val") )

proto = Ason("id:",Long(0), "name:",String("_"), "val:",Double(0.0), "star", BigDecimal('0'))
print( proto.typeName("id"), proto.typeName("name"), proto.typeName("val") )

DB.root("../DBRoot")
db = DB(1)
cfg = db.getConfig()
cfg.ensureTable(proto,"table","id")
isUnique = False
cfg.ensureIndex(proto,"table",isUnique,"name")
auto = db.open()

print(auto.select("from table limit 0, 10"))
print(dir(auto))
print(dir(proto))
print(proto.clone.__doc__)

auto.getDatabase().close()

from iboxdb.localserver import BoxSystem
BoxSystem.DBDebug.DeleteDBFiles(1)
jpype.shutdownJVM()
del jpype
