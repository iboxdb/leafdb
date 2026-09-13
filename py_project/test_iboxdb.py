


print("=========================")
print("iBoxDB.Java & Python")
print("=========================")

print("Single Thread Testing")

from _load_iboxdb import *

Address = 17        
db_root("../DBRoot")
db_debug_deletefile(Address)

# Prototype
proto = Ason("id:",Long(0), "value:",String("_"), "ver:",Long(0) )

py = PyBox(Address)  
cfg = py.get_config()
cfg.ensure_table(proto,"table","id")
cfg.ensure_index(proto,"table", False, "value")
cfg.ensure_increment(proto,"table","ver")
cfg.cache_length( 1024*1024*512 )
py.open()

re_select = 2
total = 100_000
#total = 10

begin = datetime.now()
for i in range(1,total+1):
    # clone to modify, object is reference
    v = copy(proto)
    v.set("id",py.new_id())
    v.set("value",str(i))
    if not py.insert("table",v):
        print("Check Insert")
    pass
watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Insert AVG: {total // watch :,}") 


begin = datetime.now()
for i in range(1,total+1):
    v = py.get("table",i)
    v = copy(v)
    v.set("value", "UP:" + str(i))
    if not py.update("table",v):
        print("Check Update")
    pass
watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Update AVG: {total // watch :,}")


fc = py.count("from table")
print(f"Count     : {fc :,}")

begin = datetime.now()
for i in range(1,total+1):
    if not py.delete("table",i):
        print("Check Delete")
    pass
watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Delete AVG: {total // watch :,}")

fc = py.count("from table")
print(f"Count     : {fc:,}")


begin = datetime.now()
for i in range(1,total+1):
    v = copy(proto)
    v.set("id",i)
    v.set("value",str(i))
    if not py.replace("table",v):
        print("Check Replace 1")
    pass
watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Replace-1 AVG: {total // watch :,}") 
fc = py.count("from table")
print(f"Count        : {fc:,}")


begin = datetime.now()
for i in range(1,total+1):
    v = copy(proto)
    v.set("id",i)
    v.set("value", "up" + str(i))
    if not py.replace("table",v):
        print("Check Replace 2")
    pass
watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Replace-2 AVG: {total // watch :,}") 
fc = py.count("from table")
print(f"Count        : {fc:,}")

for i in range(1,total+1):
    if i % 2 == 0:
        v = py.get("table",i)
    else:
        v = py.select("from table where id==?", Long(i))[0]
    if not str(v["value"]).startswith("up") : 
        print("Check Replace 3")


get_count = 0
begin = datetime.now()
for t in range(re_select):
    for i in range(1,total+1):
        v = py.get("table",i)
        #it is not None
        if v["id"] != i :
            print("Check Get")
        else:
            get_count = get_count + 1

watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Get()  AVG: {get_count // watch :,}/s") 
get_count = 0

#Re-Open
py.close()
del py

py = PyBox(Address)
py.open()


sel_count = 0
begin = datetime.now()
for t in range(re_select):
    print(f"Time:{t+1} / {re_select}")
    for i in range(1,total+1): 
        st = py.select("from table where id>=? & id<=?", Long(i), Long(i+64))
        for s in st:
            sel_count = sel_count + 1
        del st
        
watch = (datetime.now()  - begin).seconds
watch = max(watch,1)
print(f"Select AVG: {sel_count // watch :,}/s") 

py.close()
del py

print(input("End."))