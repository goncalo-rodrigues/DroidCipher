class colors:
    BLACK   =   '\033[30m'
    RED     =   '\033[31m'
    BRED    = '\x1b[31;4m'
    BREDRESET = '\x1b[5;39;49m'

    GREEN   =   '\033[32m'
    BGREEN  = '\x1b[1;30;42m'

    YELLOW  =   '\033[33m'
    BYELLOW =   '\033[43m'

    BLUE    =   '\033[34m'
    BYELLOWRED = '\x1b[1;31;43m'

    BLUE2   =   '\033[36m'

    RESET   =   '\033[39m'
    BRESET = '\x1b[5;39;49m'

    BLINK = '\033[4m'

    CURSORFOWARD ='\033[3C'

"""
import Tkinter
from Tkinter import Tk
from tkFileDialog import askopenfilename
import os
import subprocess

Tk().withdraw() # we don't want a full GUI, so keep the root window from appearing
old = askopenfilename() # show an "Open" dialog box and return the path to the selected file
print(old)

filename = os.path.basename(old)
print(filename)
p = subprocess.Popen(["xdg-open", old])
p.wait()
print ("after startfile")
"""
