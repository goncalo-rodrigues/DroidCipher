#! /bin/bash

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
