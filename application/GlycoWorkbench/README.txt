   ___ _                __    __           _    _                     _     
  / _ \ |_   _  ___ ___/ / /\ \ \___  _ __| | _| |__   ___ _ __   ___| |__  
 / /_\/ | | | |/ __/ _ \ \/  \/ / _ \| '__| |/ / '_ \ / _ \ '_ \ / __| '_ \ 
/ /_\\| | |_| | (_| (_) \  /\  / (_) | |  |   <| |_) |  __/ | | | (__| | | |
\____/|_|\__, |\___\___/ \/  \/ \___/|_|  |_|\_\_.__/ \___|_| |_|\___|_| |_|
         |___/                                                              
   
=Version=
GWB_MAJOR.GWB_MINOR GWB_STATE (Build: GWB_BUILD)

=Developers=
Alessio Ceroni, Kai Maass and David Damerell

=Contact=
info@glycoworkbench.org

=Web site=
www.glycoworkbench.org

=Installation=
==Windows users==
There are two ways to install this software:
i)Download and run the installer
ii)Download the Windows specific Zip file and extract

==All other users==
Download your platform specific Zip file and extract 

=Run=
==Windows users==
The run options will depend on the installation method chosen (see above)
i)If you installed using the designated installer then you should find both
a Desktop Icon and a Start bar entry for GlycoWorkBench, click on either to
launch the program
ii)If you opted for the Windows specific Zip file then launch by either clicking on the executable or the jar file

==All other useres==
GlycoWorkBench can be launched by double clicking the executable jar file within the archive

=Troubleshooting=
==Run==
If the above methods failed to launch program then attempt to manually launch as follows:
i)Open a terminal or command prompt
ii)Navigate to the directory that contains GWB
iii)Run java -jar -Xmx300M eurocarb-glycoworkbench-1.0rc.jar
If the above method fails to launch then email the given error message to info@glycoworkbench.org

If you can launch the application from the jar file but your shortcuts/executables don't work try the following:
i)Go to Start/Control panel
ii)Select Programs/Uninstall a program
iii)Within the pop-up window search for Java(TM)6(major version)Update 20(minor version)
iv)If your version of Java has listed beside it (64bit) then you must uninstall this version and install the 32bit equivalent
Unfortunately there is no available open source solution to this issue at this time 

=Manual=

The GlycoWorkbench (GWB) manual can be accessed via the Help menu of GWB.  

You can obtain a PDF copy of the latest GWB manual from the web site listed above.

=Bugs/Comments=

If you have discovered a bug or have a suggestion please submit a bug report to the EurocarbDB Google code
project - http://code.google.com/p/eurocarb/issues/entry
  
=Requirements=

GlycoWorkbench is currently developed for JRE 6.X - you can obtain the latest JRE from the Oracle web site: 

http://java.sun.com/javase/downloads/index.jsp

GlycoWorkbench >= 1.3.0, will only run on systems that support the SWT library (includes; Windows, Linux and Mac OS X)
