/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2008 Funambol, Inc.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import com.funambol.platform.FileAdapter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * This appender logs messages to a file using JSR75 (FileConnection)
 * The appender tracks the file size and if it exceeds a given maximum size
 * (customizable by clients) then the current log file is renamed appending a
 * .old to the log name and a new one is created. Therefore the maximum size
 * on this is about 2 times the maxFileSize (this is not accurate as there is
 * no limit on the size of the single message printed).
 */
public class FileAppender implements Appender {

    private String allLogFileName ="allsynclog.txt";
    private String path = "file:///root1/";
    private String contentPath = path;
    private String fileUrl   = "file:///root1/synclog.txt";
    private String fileName  = "synclog.txt";
    private String oldSuffix = ".sav.txt";

    private FileAdapter file;
    private OutputStream os;
    
    private long maxFileSize = 512 * 1024;

    private boolean generateContentInMemory = false;

    private Object lock = new Object();

    
    /**
     * Default constructor
     */
    public FileAppender(String path, String fileName) {
        if (path != null && fileName != null) {
            if (path.endsWith("/")) {
                this.fileUrl = path + fileName;
                this.path = path;
            } else {
                this.fileUrl = path + "/" + fileName;
                this.path = path + "/";
            }
            this.fileName = fileName;
            // By default the contentPath is the same as the path
            contentPath = this.path;
        }
        os = null;
    }

    //----------------------------------------------------------- Public Methods
    /**
     * Sets the maximum file size. Once this is size is reached, the current log
     * file is renamed and a new one is created. This way we have at most 2 log
     * files whose size is (roughly) bound to maxFileSize.
     * The minimum file size is 1024 as smaller size does not really make sense.
     * If a client needs smaller files it should probably the usage of other
     * Appenders or modify the behavior of this one by deriving it.
     *
     * @param maxFileSize the max size in bytes
     */
    public void setMaxFileSize(long maxFileSize) {
        if (maxFileSize > 1024) {
            this.maxFileSize = maxFileSize;
        }
    }

    /**
     * Sets the content path. This path is the directory where the combined log
     * is placed so that the LogContent is accessible. By default this directory
     * is the same as the log directory, but it is possible to specify a
     * different one if needed.
     */
    public void setContentPath(String path) {
        contentPath = path;
    }

    /**
     * Sets the content type of the log when it is retrieved via getLogContent.
     * By default the FileAppender returns a content in a file, but if the
     * client prefers an inlined value, then this method allows to force this
     * behavior.
     * Note that regardless of this setting, the log is always written to a
     * file.
     */
    public void setLogContentType(boolean memory) {
        generateContentInMemory = memory;
    }

    /**
     * FileAppender writes one message to the output file
     */
    public void writeLogMessage(String level, String msg) {
        synchronized(lock) {
            try {
                if (os != null) {
                    Date now = new Date();
                    StringBuffer logMsg = new StringBuffer(now.toString());
                    logMsg.append(" [").append(level).append("] ");
                    logMsg.append(msg);
                    logMsg.append("\r\n");
                    os.write(logMsg.toString().getBytes());
                    os.flush();

                    // If the file grows beyond the limit, we rename it and create a new
                    // one
                    if (file.getSize() > maxFileSize) {
                        try {
                            String oldFileName = fileUrl + oldSuffix;
                            FileAdapter oldFile = new FileAdapter(oldFileName);
                            if (oldFile.exists()) {
                                oldFile.delete();
                            }
                            file.rename(oldFileName);
                            file.close();
                            // Reopen the file
                            initLogFile();
                        } catch (Exception ioe) {
                            System.out.println("Exception while renaming " + ioe);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception while logging. " + e);
                e.printStackTrace();
                // We try to close and reopen the log file. The message being logged
                // is lost. We don't try to reopen it and get into an infinite
                // recursion.
                try {
                    file.close();
                } catch (Exception e1) {
                    // We cannot even close the file, too bad. Logging maybe disabled
                    // at this point. Nevertheless we try to reopen the file
                } finally {
                    initLogFile();
                }
            }
        }
    }

    /**
     * Init the logger
     */
    public void initLogFile() {
        synchronized(lock) {
            try {
                file = new FileAdapter(fileUrl);
                os = file.openOutputStream(true);
            } catch (Exception e) {
                System.out.println("Cannot open or create file at: " + fileUrl);
                e.printStackTrace();
            }
        }
    }

    /**
     * FileAppender doesn't implement this method
     */
    public void openLogFile() {
    }

    /**
     * Close connection and streams
     */
    public void closeLogFile() {

        synchronized(lock) {
            try {
                if (os != null) {
                    os.close();
                }
                if (file != null) {
                    file.close();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Perform additional actions needed when setting a new level.
     * FileAppender doesn't implement this method
     */
    public void setLogLevel(int level) {
    }

    /**
     * Delete the log file
     */
    public void deleteLogFile() {
        synchronized(lock) {
            try {
                FileAdapter file = new FileAdapter(fileUrl);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // We cannot log here, so just print to stdout
                System.out.println("Cannot open or create file at: " + fileUrl);
                e.printStackTrace();
            }
        }
    }

    public LogContent getLogContent() throws IOException {
        synchronized (lock) {
            String inlinedContent = null;
            try {
                //Merge txt sav log file
                FileAdapter txtSavFa = null;
                InputStream txtSavIs = null;
                try {
                    txtSavFa = new FileAdapter(fileUrl + oldSuffix);
                    txtSavIs = txtSavFa.openInputStream();
                } catch (Exception e) {
                    // We cannot log here, so just print to stdout
                    System.out.println("Sav file not found or not accessible");
                }

                // Prepare the output stream
                FileAdapter allLogFa = null;
                OutputStream allOs;
                if (generateContentInMemory) {
                    allOs = new ByteArrayOutputStream();
                } else {
                    allLogFa = new FileAdapter(contentPath + allLogFileName);
                    // Open in truncate mode
                    allOs = allLogFa.openOutputStream();
                }

                if (txtSavIs != null) {
                    merge(txtSavIs, allOs);
                    txtSavIs.close();
                    txtSavFa.close();
                }

                //Merge current log file. We need to close the file to be able
                //to read it. Note that this method is synchronized on so so that we
                //can safely do it
                os.close();
                file.close();

                FileAdapter file = new FileAdapter(fileUrl);
                InputStream txtIs = file.openInputStream();
                merge(txtIs, allOs);
                txtIs.close();
                file.close();

                // Now close the combined content file
                if (generateContentInMemory) {
                    inlinedContent = allOs.toString();
                } else {
                    allOs.close();
                    allLogFa.close();
                }
            } catch (Exception e) {
                // We cannot log here, so just print to stdout
                System.out.println("Cannot prepare log content:" + e.toString());
                throw new IOException("Cannot prepare log content");
            } finally {
                initLogFile();
            }
            if (generateContentInMemory) {
                return new LogContent(LogContent.STRING_CONTENT, inlinedContent);
            } else {
                return new LogContent(LogContent.FILE_CONTENT, path + allLogFileName);
            }
        }
    }

    private void merge(InputStream is, OutputStream os) throws IOException {

        byte[] buffer = new byte[4096];
        int length = 0;
        do {
            length = is.read(buffer);
            if (length > 0) {
                os.write(buffer, 0, length);
                os.flush();
            }
        } while(length > 0);
    }

}

