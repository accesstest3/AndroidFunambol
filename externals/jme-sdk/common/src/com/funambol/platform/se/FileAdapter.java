/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import java.util.Enumeration;
import java.util.Collections;
import java.util.Arrays;

/**
 * This class is a proxy to javax.io.File to provide a common access to a file
 * resource on all mobile platforms.
 *
 * A portable code must use this class only to access files, and must take care
 * of closing the FileAdapter when not used anymore, even this makes no action on 
 * Standard Edition.
 * <pre>
 * Example:
 * 
 *   void fileAccessExample(String path) {
 *      FileAdapter fa = new FileAdapter(path); // opens the File
 *      InputStream is = fa.openInputStream();  // opens the InputStream
 *      while( (char c = is.read()) != -1) {    // read till the end of the file
 *         System.out.print(c);
 *      }
 *      is.close();                             // closes the InputStream
 *      fa.close();     // * MUST DO, even if it does nothing on JavaSE *
 * </pre>
 */
public class FileAdapter {

    /** The underlying File object */
    private File file;
    
    /* Filters only files, excluding the directories. */
    private class DirFilter implements FilenameFilter {
        public boolean accept(File f, String n) {
            return !f.isDirectory();
        }
    }

    static DirFilter dirFilter;

    //------------------------------------------------------------- Constructors

    /**
     * Construct a new FileAdapter, opening the underlying File.
     */
    public FileAdapter(String path) throws IOException {
        this(path, false);
    }

    /**
     * Construct a new FileAdapter, opening the underlying File.
     * @param readonly open the file in readonly mode. This is currently ignored
     * and the file is always opened in read/write
     */
    public FileAdapter(String path, boolean readonly) throws IOException {
        file = new File(path);
    }

    /**
     * Open and return an input stream for this FileHandler.
     * If the file does not exist, en IOException is thrown.
     */
    public InputStream openInputStream() throws IOException {
        if (!exists()) {
            throw new IOException("File not found: " + file.getName());
        }
        return new FileInputStream(file);
    }

    /**
     * Open and return an output stream for this FileHandler.
     * If the file does not exist, it is created by this call, otherwise
     * it is truncated to the beginning.
     */
    public OutputStream openOutputStream() throws IOException {
        return openOutputStream(false);
    }

    /**
     * Open and return an output stream for this FileHandler.
     * If the file does not exist, it is created by this call.
     * The parameter append  determines whether or not the file is opened and
     * appended to or just opened empty. 
     *
     * @param append a boolean indicating whether or not to append to an
     *               existing file. 
     *
     * @return the new OutputStream
     */
    public OutputStream openOutputStream(boolean append) throws IOException {
        if (!exists()) {
            create();  // create the file if it doesn't exist
        }

        return new FileOutputStream(file, append);
    }

    /**
     * Close this FileAdapter
     */
    public void close() throws IOException {
        file = null;
    }

    /** 
     * Create a file with the name of this FileAdapter.
     */
    public void create() throws IOException {
        file.createNewFile();
    }

    /** 
     * Delete the file with the name of this FileAdapter.
     */
    public void delete() throws IOException {
        file.delete();
    }

    /** 
     * Renames this File to the name represented by the File dest. This works
     * for both normal files and directories.
     *
     * @param newName - the File containing the new name. 
     * @return true if the File was renamed, false otherwise.
     */
    public void rename(String newName) throws IOException {
        file.renameTo(new File(newName));
    }

    /** 
     * Check if the file with the name of this FileAdapter exists.
     */
    public boolean exists() throws IOException {
        return file.exists();
    }

    /** 
     * Check if the file with the name of this FileAdapter exists.
     */
    public long getSize() throws IOException {
        return file.length();
    }

    /**
     * Returns if this FileAdapter represents a directory on the underlying
     * file system. 
     */
    public boolean isDirectory() throws IOException {
        return file.isDirectory();
    }

    /** 
     * Gets a list of all visible files and directories contained in a
     * directory. The directory is the connection's target as specified in
     * Connector.open().
     *
     * @return An Enumeration of strings, denoting the files and directories in
     *         the directory. The string returned contain only the file or
     *         directory name and does not contain any path prefix (to get a
     *         complete path for each file or directory, prepend getPath()).
     *         Directories are denoted with a trailing slash "/" in their
     *         returned name. The Enumeration has zero length if the directory
     *         is empty. Any hidden files and directories in the directory are
     *         not included in the returned list. Any current directory
     *         indication (".") and any parent directory indication ("..") is
     *         not included in the list of files and directories returned. 
     *
     * @throw java.io.IOException - if invoked on a file, the directory does
     * not exist, the directory is not accessible, or an I/O error occurs. 
     */
    public Enumeration list(boolean includeSubdirs) throws IOException {
        if(!isDirectory()) {
            throw new IOException("FileAdapter.list: " + file.getName()
                                  + " is not a directory");
        }
        try {
            String[] list;
            if(includeSubdirs) {
                list = file.list();
            }
            else {
                list = file.list(dirFilter);
            }
            return Collections.enumeration(Arrays.asList(list));
        }
        catch(Exception e) {
            throw new IOException("FileAdapter.list failed: " + e.toString());
        }
    }

    /**
     * Returns the name of a file or directory excluding the URL schema and all paths.
     */
    public String getName() {
        return file.getName();
    }

    /**
     * Creates a directory corresponding to the directory string provided in the
     * constructor.
     *
     * @throws IOException if the directory cannot be created.
     */
    public void mkdir() throws IOException {
        boolean created = file.mkdir();
        if (!created) {
            throw new IOException("Directory creation failed " + file.getAbsolutePath());
        }
    }

    /**
     * Returns the timestamp of the last modification to the file
     */
    public long lastModified() {
        return file.lastModified();
    }

    /**
     * Returns true if the underlying platform supports the setLastModified
     * method.
     */
    public boolean isSetLastModifiedSupported() {
        return true;
    }

    /**
     * Sets the last modification time for this file
     * Users shall check if the operation is supported by invoking
     * @see isSetLastModifiedSupported.
     *
     * @param date the modification time expressed as UTC
     *
     * @throws IOException if the operation fails
     */
    public void setLastModified(long date) throws IOException {
        file.setLastModified(date);
    }
}

