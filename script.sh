# to install pip
sudo apt-get install python-pip python-dev build-essential
sudo pip install --upgrade pip

# to install other packages
sudo apt-get install bluetooth libbluetooth-dev
sudo pip install pycrypto
sudo pip install qrcode
sudo apt-get install python-tk
sudo pip install pillow
cd ExtraModules/pybluez-master
sudo python setup.py install

# Bluetooth: should be on and visible

# To run bluetooth as a service:

# To find the location of bluetooth.service (usually in /lib/systemd/system/bluetooth.service)
# systemctl status bluetooth.service

# To open the file
# sudo nano /lib/systemd/system/bluetooth.service

# Update the line
# ExecStart=/usr/lib/bluetooth/bluetoothd
# to
# ExecStart=/usr/lib/bluetooth/bluetoothd --compat
# Restart the computer and it should work
