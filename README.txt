Funambol Sync Client for Android
=================================

 1. Overview
 2. Download
 3. Build Requirements
 4. Preparing the Build Environment
    4.1 Windows
    4.2 Linux
    4.3 Mac OSX
 5. Build and Install
 6. Test
 7. Known Issues

1. Overview
-----------

 Funambol Sync Client for Android is a client to synchronize PIM Data of Android
 devices with any SyncML aware server.

 For the moment, it is an experimental project to explore the possibility of the
 new platform.

 Currently you can synchronize contacts with the internal database of the
 Funambol server so you can exchange them with other devices or Thunderbird
 adressbook for example.

 If you want to test without risk try the Android emulator shiped with the
 Android SDK.

 For comments, bugreports or any other help you are very welcome. See
 https://android-client.forge.funambol.org/servlets/ProjectProcess?pageID=vSJyYt


2. Download
-----------

 Prebuild binaries and source archives are located here:
 https://android-client.forge.funambol.org/servlets/ProjectDocumentList

 You can obtain the source code from subversion here:
 https://android-client.forge.funambol.org/svn/android-client/trunk/android-client

 To access the subversion repository you need an account. You can create one by
 registering on this page:
 https://www.forge.funambol.org/servlets/Join


3. Build requirements
---------------------

 - Sun Java 1.6 JDK
   http://java.sun.com/javase/downloads/index.jsp
 - Apache Ant
   http://ant.apache.org/bindownload.cgi
 - Ant-Contrib Tasks: http://ant-contrib.sourceforge.net/ (version 0.6)
 - JavaCC 4.0 (note that 4.1 does not work with J2ME)
   https://javacc.dev.java.net/files/documents/17/26777/javacc-4.0.zip
 - Android SDK 1.0 / 1.1 / 1.5
   http://developer.android.com/sdk
 - Subversion client
   http://subversion.tigris.org


4. Preparing the build environment
----------------------------------

 Funambol Sync Client for Android builds on Windows, Linux and Mac OSX with any
 Android SDK from 1.0 to 1.5. You may prefer to build with the 1.5 SDK because
 of the newer Android GUI in the emulator but it needs some steps to get the
 emulator running whilst the emulator of the 1.1 SDK starts with one click.

  4.1 Windows
  -----------

   - Download and install Sun Java 1.6 JDK.
     Add the bin directory of the JDK to your path and create a new system
     variable JAVA_HOME pointing to the base directory of the JDK.
     For example:
     Add "C:\Program Files\Java\jdk_1.6.0_14\bin" to the path.
     Set JAVA_HOME to "C:\Program Files\Java\jdk_1.6.0_14".

   - Download Apache Ant and unzip it to a directory of your choice.
     Add Ant's bin directory to your path and create a new system variable
     ANT_HOME.
     For example:
     Add "C:\Apps\ant\bin" to your path.
     Set ANT_HOME to "C:\Apps\ant".

     You can now check your installation by opening a comand prompt and typing
     ant in any directory. You should get a message like:
          C:\> ant
     	  Buildfile: build.xml does not exist!
	  Build failed
     Now check for the java compiler
          C:\> javac -version
	  javac 1.6.0_14
     If you got anything else, check your path!

   - Download and install Ant-Contrib Tasks: http://ant-contrib.sourceforge.net/ (version 0.6)
     Copy your ant-contrib-0.6.jar to your $ANT_HOME/lib

   - Download JavaCC 4.0 and unzip it to a directory of your choice.
     For example:
     c:\src\lib\javacc-4.0

   - Download Android SDK and unzip it to a directory of your choice.
     For example:
     C:\src\lib\android-sdk-windows-1.5_r2
     You can add the tools directory of the SDK to your path. This is not
     necessary for the build but it's easier to play with the emulator and
     related stuff.
     For example:
     Add "C:\src\lib\android-sdk-windows-1.5_r2\tools" to your path.

   - If you want to compile a zipped source archive, you don't need subversion
     but you certainly want the newest stuff so...
     Download CollabNet Subversion Command-Line Client v1.6.2 from
     http://www.collab.net/downloads/subversion/
     If you don't like to sign in at CollabNet you can take the Silk SVN build from
     http://www.sliksvn.com/en/download
     Just run setup and your are done.
     If you like an Explorer integration you can additionally install
     TortuiseSVN from http://tortoisesvn.net/downloads
     Among other things it provides a command to apply patches from the context
     menu.

   - Open a commandprompt and change to the directory where you want to store
     the sources and unzip the source archive or do a checkout from the
     repository.
     For example:
     C:\src>svn co https://android-client.forge.funambol.org/svn/android-client/trunk/android-client
     Change to the new directory, copy the build.properties.example to
     build.properties and edit the file to match your installation.
     For example:
     Change the line which contains sdk-folder to
     sdk-folder=C:/src/lib/android-sdk-windows-1.5_r2

     Now you are ready to build.

  4.2 Linux
  ---------

   Most Linux distributios provide a Java JDK, Subersion and Ant via it's own
   setup and software repository.
   On Ubuntu just type
   # apt-get install sun-java6-jdk ant ant-optional javacc subversion unzip
   to get the basic things.

   - Download Android SDK and unzip it to a directory of your choice. If you
     have root access you can use /opt if not put it elsewhere in your homedir.
     You may want to add the tools directory to your path.
     For example:
     Add the following line to your .bashrc:
     export PATH=$PATH:/opt/android-sdk-linux_x86-1.5_r2/tools

   - Open a terminal and change to the directory where you want to store
     the sources and unzip the source archive or do a checkout from the
     repository.
     For example:
     myhost:~/src/$ svn co https://android-client.forge.funambol.org/svn/android-client/trunk/android-client
     Change to the new directory and copy the build.properties.example to
     build.properties and edit the file to match your installation.
     For example:
     Change the line which contains sdk-folder to
     sdk-folder=/opt/android-sdk-linux_x86-1.5_r2

     Now you are ready to build.

  4.3 Mac OSX
  -----------

   Mac OSX Leopard ships with Java already installed It also has Ant
   and Subversion if you have XCode installed. If you miss subversion
   and don't like to install the big XCode Package, you can obtain
   subversion from
   http://www.open.collab.net/downloads/apple/download.html
   or install it via http://www.macports.org/ or
   http://finkproject.org/.
   This also applies to Ant.

   - Download JavaCC 4.0 and unzip it to a directory of your choice.
     For example:
     ~\lib\javacc-4.0

   The rest of the installation is the same as for Linux. Just press
   COMMAND-SPACE to start Spotlight and type Terminal to get a console.
   If you want root access type
   # sudo -i
   it's just unix...


5. Build and install
--------------------

 To build the software change to the source directory you checked out
 before. Then type:
 ant
 It runs the default target which is the same as typing
 ant debug
 It will build the Android application signed whith a debug key into
 bin/funambol-sync-client.apk

 For an overview of availible targets type
 ant -p

 You can build and upload to a running emulator or usb connected device with
 one command:
 ant install

 If you have already installed the application before use
 ant reinstall

 If you want to build from an IDE you have to take into account that there are
 two libraries the project depends on. The source is located under funambol_sdk
 and the compiled libraries are located in the external-libs directory. They can
 also build separately whith:
 ant funambol-sdk

 To get logs from your connected device while running the software use the adb
 command from the tools directory of the sdk:
 adb logcat debug

 Have Fun!

6. Test
-------

 To build the application with the test runners enabled type:
 ant test

 To run Unit Test cases:
 ant run-unit-test

 To run Integration Test cases:
 ant run-integration-test

 To run both Unit and Integration Test cases:
 ant run-test

 To specify the test script url add the following definition to the command line:
 -Dtest.script.url=http://url.to.script.com


7. Known Issues
---------------

 Using Funambol Sync Client for Android with any other server than Funambol
 Server may not work at current state and there is always a possibility that
 your data get accidentally modified or lost.

 Issue: SC96:
 Can't choose sync source ID for contacts, calendar, etc
 https://android-client.forge.funambol.org/servlets/tracking?id=SC96

 Issue: SC138
 first sync after an recover duplicates all contacts with groupdav connector
 https://android-client.forge.funambol.org/servlets/tracking?id=SC138

 Issue: SC128
 Calendar has an issue with timezones so your appointments get moved by hours
 depending on the timezone offset between server and client.
 https://android-client.forge.funambol.org/servlets/tracking?id=SC128

 This is not a complete list. There are other issues too!
 Check https://android-client.forge.funambol.org/servlets/tracking
 for details.
