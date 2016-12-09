Group 4
78958 - Gonçalo Alfredo dos Santos Rodrigues
79035 - Nuno Cerqueira Afonso
79039 - Diogo Miguel Reis Silva

############
# PLATFORM #
############
* On the computer's side, the project was developed in Ubuntu 16.04 LTS and we used Python 2.7
* On the smartphone's side, we used Android 5.0 and upwards

#########
# SETUP #
#########
* For the computer:
  * Run the install_modules.sh script

  * Activate bluetooth's compatibility mode (it is only needed once):
    * To find the location of bluetooth.service (usually in /lib/systemd/system/bluetooth.service):
      systemctl status bluetooth.service

    * To open the file:
      sudo nano /lib/systemd/system/bluetooth.service

    * To update the line:
      Change: ExecStart=/usr/lib/bluetooth/bluetoothd
      To: ExecStart=/usr/lib/bluetooth/bluetoothd --compat

    * Restart the computer

  * Turn on bluetooth and make and make it visible

  * Run the command: python DroidCipher.py. The terminal location should be: Proj-SIRS16-17-G4/Python.

* For the smartphone:
  * Install DroidCipher_Android_App.apk

  * Open the application
