Zutubi Pulse(TM) @VERSION@ release
---------------------------------------

Thank you for downloading Zutubi Pulse!.

Pulse is an automated build or continuous integration server. 
Pulse regularly checks out your project's source code from your 
SCM, builds the project and reports on the results. 


Requirements
------------

    Before you can install pulse, you will need to ensure that you have the 
    following installed:
    
    1. A Java Runtime Environment (JRE), version 1.5 or higher, available at 
       http://java.sun.com/j2se/1.5.0/download.jsp.  

    2. (Perforce users only) The Perforce command-line client p4.
    3. (Git users only) The git command line client.

Installation
------------

1. A pulse package may be installed anywhere on the host system by unpacking
   the archive. The archive unpacks into a directory of the same name.
   
2. The directory the archive is unpacked into is known as the pulse home
   directory. To be able to run pulse scripts from outside of this directory,
   you must set the value of PULSE_HOME to the absolute path of the directory.

3. Once the archive is installed, you can start your pulse server by running 
   the pulse script (pulse.bat for Windows users) in the bin directory:

     $ ./bin/pulse start
   
   
For an extended description of the installation process, please see the Getting
Started Guide http://confluence.zutubi.com/display/pulse0200/Getting+Started+Guide
   
Problems?
---------

If you run into any problems during the installation, you can find help in the 
following places:

The Pulse Documentation:
    
    http://confluence.zutubi.com/display/pulse0200/Getting+Started+Guide

The Pulse Support Forums:
    
    http://forums.zutubi.com

Send us an email:
    
    mailto:support@zutubi.com
    

Thank you for using Pulse!

The Zutubi Team

