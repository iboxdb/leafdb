print("iBoxDB Java Python!")

# pip install jpype1
# Download openjdk

import os;
print(os.environ.get("JAVA_HOME"))
os.environ["JAVA_HOME"] = "./target/jlink-image/"
print(os.environ.get("JAVA_HOME"))


import jpype
# ModuleNotFoundError if not imported
import jpype.imports
 
 
print("startJVM...");
jpype.startJVM("--enable-native-access=ALL-UNNAMED")

print("JVM: ", jpype.getDefaultJVMPath())
print("Ver: ", jpype.getJVMVersion())

#jlink jdk don't need to load this.
#jpype.addClassPath("iboxdb-4.1.2.jar")

from java.lang import Long, Double, String
from java.math import BigDecimal          
from iboxdb.localserver import Ason,DB


print("",Ason.class_,DB.class_)

# Prototype
proto = Ason("id:",Long(0), "name:",String("_"), "val:",Double(0.0), "star", BigDecimal('0'))

DB.root("../DBRoot")
db = DB(1)
cfg = db.getConfig()
cfg.ensureTable(proto,"table","id")
isUnique = False
cfg.ensureIndex(proto,"table",isUnique,"name")
auto = db.open()

print(dir(auto))
print(dir(proto))
print(proto.clone.__doc__)






#------------------------#
auto.getDatabase().close()
jpype.shutdownJVM()

del jpype
