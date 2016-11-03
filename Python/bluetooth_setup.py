import bluetooth

# The default values are used for TESTING
#def find_device(name = 'Nexus 7', mac = 'D8:50:E6:85:1E:41'):
def find_device(name = 'Samsung ATIV S', mac = '08:37:3D:0E:C4:29'):
    device = bluetooth.lookup_name(mac)
    return name == device

def output_status():
    if find_device:
        print("I found the Android!")
    else:
        print("Where is it?")

###############################################################################
#                                  PROGRAM                                    #
###############################################################################
"""
print("searching for specific device...")
output_status()

counter = 0
while counter < 10:
    if find_device():
        counter += 1
        print("The device is in range!")
    else:
        print("The device can't be detected!")

print("")
print("The device was found " + str(counter) + " times!")
"""

# Program given as example
print("performing inquiry...")

nearby_devices = bluetooth.discover_devices(
        duration=4, lookup_names=True, flush_cache=True, lookup_class=False)

print("found %d devices" % len(nearby_devices))

for addr, name in nearby_devices:
    try:
        print("  %s - %s" % (addr, name))
    except UnicodeEncodeError:
        print("  %s - %s" % (addr, name.encode('utf-8', 'replace')))


