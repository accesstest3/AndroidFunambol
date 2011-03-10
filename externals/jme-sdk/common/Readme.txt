Funambol J2ME Common API

Version: 6.0.5

OVERVIEW
--------
This library is a collection of basic utilities for: object serialization
and persistence on the RMS, Logging and string manipulation.

See the javadoc for more information on the structure of the library.


BUILD
-----
To build the package, you need ANT, ANTENNA (http://antenna.sourceforge.org) 
and the Sun WTK. You need to setup the build.properties file to meet your
paths.

The package contains unit tests, based on JMUnit (under the folder test). To
build them you need antenna and JMUnit (http://sourceforge.net/projects/jmunit).

You should put the antenna jar in the ant lib directory, and the JMUnit in the
bin directory of the WTK.
Follow the packages instructions for more details.

If all is configured correctly, 'ant run' will start the tests on the default
emulator. Run 'ant usage' in the build directory to get all the available
targets.

If you are using Eclipse or Netbeans, just import the source in a new project
as you are used to.

-----------

Funambol offers commercial support for this software.
See http://www.funambol.com/support.

You can also get support from the open source community.
See http://www.funambol.com/opensource/support.

Copyright (c) 2007 Funambol. All rights reserved. 

