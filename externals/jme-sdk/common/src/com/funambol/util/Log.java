/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2007 Funambol, Inc.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission 
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 * 
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 * 
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.util;

import java.util.Date;
import java.util.Vector;
import java.io.IOException;

/**
 * Generic Log class
 */
public class Log {
    
    //---------------------------------------------------------------- Constants
    /**
     * Log level DISABLED: used to speed up applications using logging features
     */
    public static final int DISABLED = -1;
    
    /**
     * Log level ERROR: used to log error messages.
     */
    public static final int ERROR = 0;
    
    /**
     * Log level INFO: used to log information messages.
     */
    public static final int INFO = 1;
    
    /**
     * Log level DEBUG: used to log debug messages.
     */
    public static final int DEBUG = 2;
    
    /**
     * Log level TRACE: used to trace the program execution.
     */
    public static final int TRACE = 3;
    
    private static final int PROFILING = -2;
    
    //---------------------------------------------------------------- Variables
    /**
     * The default appender is the console
     */
    private static Appender out;
    
    /**
     * The default log level is INFO
     */
    private static int level = INFO;
    
    /**
     * Last time stamp used to dump profiling information
     */
    private static long initialTimeStamp = -1;

    /**
     * Default log cache size
     */
    private static final int CACHE_SIZE = 1024;

    /**
     * This is the log cache size (by default this is CACHE_SIZE)
     */
    private static int cacheSize = CACHE_SIZE;

    /**
     * Log cache
     */
    private static Vector cache;

    /**
     * Tail pointer in the log cache
     */
    private static int next = 0;

    /**
     * Head pointer in the log cache
     */
    private static int first = 0;

    /**
     * Controls the context logging feature
     */
    private static boolean contextLogging = false;

    /**
     * The client max supported log level. This is only needed for more accurate context
     * logging behavior and the client filters log statements.
     */
    private static int clientMaxLogLevel = TRACE;

    private static boolean lockedLogLevel;

    private static Log instance = null;

    //------------------------------------------------------------- Constructors
    /**
     * This class is static and cannot be intantiated
     */
    private Log(){
    }

    /**
     * The log can be used via its static methods or as a singleton in case
     * static access is not allowed (this is the case for example on the
     * BlackBerry listeners when invoked outside of the running process)
     */
    public static Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }
    
    //----------------------------------------------------------- Public methods
    /**
     * Initialize log file with a specific appender and log level. Contextual
     * errors handling is disabled after this call.
     *
     * @param object the appender object that write log file
     * @param level the log level
     */
    public static void initLog(Appender object, int level){
        out = object;
        out.initLogFile();
        // Init the caching part
        cache = new Vector(cacheSize);
        first = 0;
        next  = 0;
        contextLogging = false;
        lockedLogLevel = false;
        setLogLevel(level);
        if (level > Log.DISABLED) {
            writeLogMessage(level, "INITLOG","---------");
        }
    }

    /**
     * Initialize log file with a specific appender and log level.
     * Contextual errors handling is enabled after this call.
     *
     * @param object the appender object that write log file
     * @param level the log level
     * @param cacheSize the max number of log messages cached before an error is
     *                  dumped
     */
    public static void initLog(Appender object, int level, int cacheSize) {
        Log.cacheSize = cacheSize;
        initLog(object, level);
        contextLogging = true;
    }
    
    /**
     * Ititialize log file
     * @param object the appender object that write log file
     */
    public static void initLog(Appender object){
        initLog(object, INFO);
    }

    /**
     * Return a reference to the current appender
     */
    public static Appender getAppender() {
        return out;
    }

    /**
     * Enabled/disable the context logging feature. When this feature is on, any
     * call to Log.error will trigger the dump of the error context.
     */
    public static void enableContextLogging(boolean contextLogging) {
        Log.contextLogging = contextLogging;
    }

    /**
     * Allow clients to specify their maximum log level. By default this value
     * is set to TRACE.
     */
    public static void setClientMaxLogLevel(int clientMaxLogLevel) {
        Log.clientMaxLogLevel = clientMaxLogLevel;
    }
    
    /**
     * Delete log file
     *
     */
    public static void deleteLog() {
        out.deleteLogFile();
    }
    
    /**
     * Accessor method to define log level
     * the method will be ignorated in Log level is locked
     * @param newlevel log level to be set
     */
    public static void setLogLevel(int newlevel) {
        if(!lockedLogLevel){
            level = newlevel;
            if (out != null) {
                out.setLogLevel(level);
            }
        }
    }

    /**
     * Accessor method to lock defined log level
     * @param level log level to be lock
     */
    public static void lockLogLevel(int levelToLock) {
        level = levelToLock;
        lockedLogLevel = true;
        if (out != null) {
            out.setLogLevel(level);
        }
    }
    
    /**
     * Accessor method to lock defined log level
     * 
     */
    public static void unlockLogLevel() {
        lockedLogLevel = false;
    }

    
    /**
     * Accessor method to retrieve log level:
     * @return actual log level
     */
    public static int getLogLevel() {
        return level;
    }
    
    /**
     * ERROR: Error message
     * @param msg the message to be logged
     */
    public static void error(String msg) {
        writeLogMessage(ERROR, "ERROR", msg);
    }
    
    /**
     * ERROR: Error message
     * @param msg the message to be logged
     * @param obj the object that send error message
     */
    public static void error(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName())
               .append("] ").append(msg);
        writeLogMessage(ERROR, "ERROR", message.toString());
    }

    /**
     * ERROR: Error message
     * @param msg the message to be logged
     * @param tag the tag characterizing the log message initiator
     */
    public static void error(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(ERROR, "ERROR", message.toString());
    }

    /**
     * ERROR: Error message
     * @param msg the message to be logged
     * @param tag the tag characterizing the log message initiator
     * @param e the exception that caused the error
     */
    public static void error(String tag, String msg, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg).append("(").append(e.toString()).append(")");
        writeLogMessage(ERROR, "ERROR", message.toString());
        writeLogMessage(ERROR, "ERROR", StackTracePrinter.getStackTrace(e));
    }

    
    /**
     * INFO: Information message
     * @param msg the message to be logged
     */
    public static void info(String msg) {
        writeLogMessage(INFO, "INFO", msg);
    }
    
    /**
     * INFO: Information message
     * @param msg the message to be logged
     * @param obj the object that send log message
     */
    public static void info(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName())
               .append("] ").append(msg);
        writeLogMessage(INFO, "INFO", message.toString());
    }

    /**
     * INFO: Information message
     * @param msg the message to be logged
     * @param tag the tag characterizing the log message initiator
     */
    public static void info(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(INFO, "INFO", message.toString());
    }

    
    /**
     * DEBUG: Debug message
     * @param msg the message to be logged
     */
    public static void debug(String msg) {
        writeLogMessage(DEBUG, "DEBUG", msg);
    }
    
    /**
     * DEBUG: Information message
     * @param msg the message to be logged
     * @param tag the tag characterizing the log message initiator
     */
    public static void debug(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(DEBUG, "DEBUG", message.toString());
    }

    /**
     * DEBUG: Information message
     * @param msg the message to be logged
     * @param obj the object that send log message
     */
    public static void debug(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName())
               .append("] ").append(msg);
        writeLogMessage(DEBUG, "DEBUG", message.toString());
    }

    
    /**
     * TRACE: Debugger mode
     */
    public static void trace(String msg) {
        writeLogMessage(TRACE, "TRACE", msg);
    }
    
    /**
     * TRACE: Information message
     * @param msg the message to be logged
     * @param obj the object that send log message
     */
    public static void trace(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName())
               .append("] ").append(msg);
        writeLogMessage(TRACE, "TRACE", message.toString());
    }

    /**
     * TRACE: Information message
     * @param msg the message to be logged
     * @param tag the tag characterizing the log message initiator
     */
    public static void trace(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(TRACE, "TRACE", message.toString());
    }

    
    /**
     * Dump memory statistics at this point. Dump if level >= DEBUG.
     *
     * @param msg message to be logged
     */
    public static void memoryStats(String msg) {
        // Try to force a garbage collection, so we get the real amount of
        // available memory
        long available = Runtime.getRuntime().freeMemory();
        Runtime.getRuntime().gc();
        writeLogMessage(PROFILING, "PROFILING-MEMORY", msg + ":" + available
                + " [bytes]");
    }
    
    /**
     * Dump memory statistics at this point.
     *
     * @param obj caller object
     * @param msg message to be logged
     */
    public static void memoryStats(Object obj, String msg) {
        // Try to force a garbage collection, so we get the real amount of
        // available memory
        Runtime.getRuntime().gc();
        long available = Runtime.getRuntime().freeMemory();
        writeLogMessage(PROFILING, "PROFILING-MEMORY", obj.getClass().getName()
        + "::" + msg + ":" + available + " [bytes]");
    }
    
    /**
     * Dump time statistics at this point.
     *
     * @param msg message to be logged
     */
    public static void timeStats(String msg) {
        long time = System.currentTimeMillis();
        if (initialTimeStamp == -1) {
            writeLogMessage(PROFILING, "PROFILING-TIME", msg + ": 0 [msec]");
            initialTimeStamp = time;
        } else {
            long currentTime = time - initialTimeStamp;
            writeLogMessage(PROFILING, "PROFILING-TIME", msg + ": "
                    + currentTime + "[msec]");
        }
    }
    
    /**
     * Dump time statistics at this point.
     *
     * @param obj caller object
     * @param msg message to be logged
     */
    public static void timeStats(Object obj, String msg) {
        // Try to force a garbage collection, so we get the real amount of
        // available memory
        long time = System.currentTimeMillis();
        if (initialTimeStamp == -1) {
            writeLogMessage(PROFILING, "PROFILING-TIME", obj.getClass().getName()
            + "::" + msg + ": 0 [msec]");
            initialTimeStamp = time;
        } else {
            long currentTime = time - initialTimeStamp;
            writeLogMessage(PROFILING, "PROFILING-TIME", obj.getClass().getName()
            + "::" + msg + ":" + currentTime + " [msec]");
        }
    }
    
    /**
     * Dump time statistics at this point.
     *
     * @param msg message to be logged
     */
    public static void stats(String msg) {
        memoryStats(msg);
        timeStats(msg);
    }
    
    /**
     * Dump time statistics at this point.
     *
     * @param obj caller object
     * @param msg message to be logged
     */
    public static void stats(Object obj, String msg) {
        memoryStats(obj, msg);
        timeStats(obj, msg);
    }

    /**
     * Return the current log appender LogContent container object
     */         
    public static LogContent getCurrentLogContent() throws IOException {
        return out.getLogContent();
    }
    
    private static synchronized void writeLogMessage(int msgLevel, String levelMsg, String msg) {
        if (contextLogging) {
            try {
                cacheMessage(msgLevel, levelMsg, msg);
            } catch (Exception e) {
                // Cannot cache log message, just ignore the error
            }
        }

        try {
            writeLogMessageNoCache(msgLevel, levelMsg, msg);
        } catch (Exception e) {
            // Cannot write log message, just ignore the error
        }
    }

    private static void writeLogMessageNoCache(int msgLevel, String levelMsg, String msg) {
        if (level >= msgLevel) {
            try {
                if (out != null) {
                    out.writeLogMessage(levelMsg, msg);
                } else {
                    Date now = new Date();
                    System.out.print(now.toString());
                    System.out.print(" [" + levelMsg + "] " );
                    System.out.println(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void cacheMessage(int msgLevel, String levelMsg, String msg) throws IOException {

        // If we are already dumping at DEBUG, then the context is already
        // available
        if (cache == null || level >= clientMaxLogLevel) {
            return;
        }

        if (msgLevel == ERROR) {
            dumpAndFlushCache();
        } else {

            // Store at next
            if (next >= cache.size()) {
                cache.addElement(msg);
            } else {
                cache.setElementAt(msg, next);
            }
            // Move next
            next++;
            if (next == cacheSize) {
                next = 0;
            }

            if (next == first) {
                // Make room for the next entry
                first++;
            }
            if (first == cacheSize) {
                first = 0;
            }
        }
    }

    private static void dumpAndFlushCache() throws IOException {

        int i = first;
        if (first != next) {
            writeLogMessageNoCache(ERROR, "[Error Context]", "==================================================");
        }
        while (i != next) {
            if (i == cacheSize) {
                i = 0;
            }
            writeLogMessageNoCache(ERROR, "[Error Context]", (String) cache.elementAt(i));
            ++i;
        }

        if (first != next) {
            writeLogMessageNoCache(ERROR, "[Error Context]", "==================================================");
        }
        first = 0;
        next = 0;
    }
}
