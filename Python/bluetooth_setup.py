import bluetooth

# The default values are used for TESTING
def find_device(name = 'Nexus 7', mac = 'D8:50:E6:85:1E:41'):
    device = bluetooth.lookup_name(mac)
    if name == device:
        print("I found the Android!")
    else:
        print("Where is it?")

print("searching for specific device...")
find_device();

# Program given as example
"""print("performing inquiry...")

nearby_devices = bluetooth.discover_devices(
        duration=4, lookup_names=True, flush_cache=True, lookup_class=False)

print("found %d devices" % len(nearby_devices))

for addr, name in nearby_devices:
    try:
        print("  %s - %s" % (addr, name))
    except UnicodeEncodeError:
        print("  %s - %s" % (addr, name.encode('utf-8', 'replace')))
"""

