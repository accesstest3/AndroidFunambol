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

import java.util.Vector;

/**
 * This class allow to process substrings of a bigger string without
 * allocate additional memory, but using the original buffer.
 * The original string is not modified by this class.
 */
public class ChunkedString {
    
    /** The string buffer for the whole content */
    private final String buf;
    /** The start index of the chunk */
    private int start;
    /** The end index of the chunk */
    private int end;
    
    
    // ----------------------------------------------------------- Constructors
    
    /**
     * Creates a ChunkedString from a plain String, with length equal to
     * the whole string.
     *
     * @param s the string
     */
    public ChunkedString(final String s) {
        buf = s;
        start = 0;
        end = s.length();
    }
    
    /**
     * Creates a ChunkedString from a plain String and two indexes
     *
     * @param s the string
     * @param a the start index
     * @param b the end index
     */
    public ChunkedString(final String s, int a, int b)
    throws IndexOutOfBoundsException {
        buf = s;
        start = a;
        end = b;
        
        checkIndexes();
    }
    
    /**
     * Creates a ChunkedString from another ChunkedString
     * and two indexes relative to the chunk
     *
     * @param cs the string
     * @param a the start index
     * @param b the end index
     */
    public ChunkedString(final ChunkedString cs, int a, int b) {
        buf = cs.buf;
        start = cs.start + a;
        end = cs.start + b;
    }
    
    /**
     * Creates a ChunkedString from another ChunkedString
     * and two indexes relative to the chunk
     *
     * @param cs the string
     * @param a the start index
     * @param b the end index
     */
    public ChunkedString(final ChunkedString cs) {
        buf = cs.buf;
        start = cs.start;
        end = cs.end;
    }
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Gets the next chunk, using the given separator, as a plain String.
     * If the separator is not found, return the rest of the chunk.
     * The start position of the object is moved after the end of the
     * returned chunk.
     */
    public String getNextString(String sep) {
        if (isEmpty()) {
            return null;
        }
        String ret = null;
        int idx = buf.indexOf(sep, start);
        if (idx == -1 || idx > end) {
            ret = buf.substring(start, end);
            start = end;
        } else {
            ret = buf.substring(start, idx);
            start = idx+sep.length();
        }
        return ret;
    }

    /**
     * Gets the smallest chunk, using one of the given  separators,
     * as a plain String.
     * If the separator is not found, return the rest of the chunk.
     * The start position of the object is moved after the end of the
     * returned chunk.
     *
     * @param sep an array of string used as separators.
     *            if two or more strings are found at the same index
     *            the first one in the array is used as the separator
     */
    public String getNextString(String[] sep) {
        if (isEmpty()) {
            return null;
        }
        int idx=end;
        int localIdx;
        int sepLen=sep[0].length();
        for (int i=0, l=sep.length; i<l; i++) {
            localIdx =  buf.indexOf(sep[i], start) ;
            if (localIdx != -1 && localIdx < idx) {
                sepLen = sep[i].length();
                idx = localIdx;
            }
            
        }
        String ret = null;
        if (idx == -1 || idx > end) {
            ret = buf.substring(start, end);
            start = end;
        } else {
            ret = buf.substring(start, idx);
            start = idx+sepLen;
        }
        return ret;
    }
    
    /**
     * Gets the smallest chunk, using one of the given  separators,
     * as a ChunkedString.
     * If the separator is not found, return the rest of the chunk.
     * The start position of the object is moved after the end of the
     * returned chunk.
     *
     * @param sep an array of string used as separators.
     *            if two or more strings are found at the same index
     *            the first one in the array is used as the separator
     */
    public ChunkedString getNextChunk(String[] sep) {
        if (isEmpty()) {
            return null;
        }
        int idx=end;
        int localIdx;
        int sepLen=sep[0].length();
        for (int i=0, l=sep.length; i<l; i++) {
            localIdx =  buf.indexOf(sep[i], start) ;
            if (localIdx != -1 && localIdx < idx) {
                sepLen = sep[i].length();
                idx = localIdx;
            }
        }
        
        ChunkedString ret = null;
        if (idx == -1 || idx > end) {
            ret = new ChunkedString(buf, start, end);
            start = end;
        } else {
            ret = new ChunkedString(buf, start, idx);
            start = idx+sepLen;
        }
        
        return ret;
    }
    
    
    /**
     * Gets the next chunk, using the given separator, as another
     * ChunkedString, without allocating memory.
     * If the separator is not found, return the rest of the chunk.
     * The start position of the object is moved after the end of the
     * returned chunk.
     */
    public ChunkedString getNextChunk(String sep) {
        if (isEmpty()) {
            return this;
        }
        int idx = buf.indexOf(sep, start);
        
        ChunkedString ret = null;
        if (idx == -1 || idx > end) {
            ret = new ChunkedString(buf, start, end);
            start = end;
        } else {
            ret = new ChunkedString(buf, start, idx);
            start = idx+sep.length();
        }
        
        return ret;
    }
    
    /**
     * Returns the character at the specified position, relative
     * to the chunk.
     *
     * @param index the index of the character relatoive to the chunk.
     * @return the character at the specified index of this chunk.
     *         The first character is at index 0.
     *
     * @throws IndexOutOfBoundException
     *                If the given index is out of the chunk boundaries.
     */
    public char charAt(int index) throws IndexOutOfBoundsException {
        if(index < 0 ||start+index > end) {
            throw new IndexOutOfBoundsException("charAt("+index+")");
        }
        return buf.charAt(start+index);
    }
    
        /**
     * Split the string into an array of strings using one of the separator
     * in 'sep'.
     *
     * @param s the string to tokenize
     * @param sep a list of separator to use
     *
     * @return the array of tokens (an array of size 1 with the original
     *         string if no separator found)
     */
    public ChunkedString[] split(String sep) {
        Vector tokenIndex = new Vector(10);
        int len = length();
        int i;
        
        // Find all characters in string matching one of the separators in 'sep'
        for (i = 0; i < len; i++) {
            if (sep.indexOf(charAt(i)) != -1 ){
                tokenIndex.addElement(new Integer(i));
            }
        }

        int size = tokenIndex.size();
        ChunkedString[] elements = new ChunkedString[size+1];

        // No separators: return the string as the first element
        if(size == 0) {
            elements[0] = this;
        }
        else {
            // Init indexes
            int newStart = 0;
            int end = ((Integer)tokenIndex.elementAt(0)).intValue();
            // Get the first token
            elements[0] = substring(newStart, end);

            // Get the mid tokens
            for (i=1; i<size; i++) {
                // update indexes
                newStart = ((Integer)tokenIndex.elementAt(i-1)).intValue()+1;
                end = ((Integer)tokenIndex.elementAt(i)).intValue();
                elements[i] = substring(newStart, end);
            }
            // Get last token
            newStart = ((Integer)tokenIndex.elementAt(i-1)).intValue()+1;
            //elements[i] = (newStart < length()) ? substring(newStart, end) : new ChunkedString("");
            elements[i] = (newStart < length()) ? substring(newStart, length()) : new ChunkedString("");
        }

        return elements;
    }

    /**
     * Returns true if s is equals to the current chunk
     */
    public boolean equals(String s) {
        return equals(s, false);
    }
    
    /**
     * Returns true if s is equals to the current chunk, ignoring case.
     */
    public boolean equalsIgnoreCase(String s) {
        return equals(s, true);
    }
    
    private boolean equals(String s, boolean ignoreCase) {
        int len = s.length();
        if (len != length()) {
            return false;
        }
        return buf.regionMatches(ignoreCase, start, s, 0, len);
    }
    
    /**
     * Returns true if the chunk is empty (i.e. start&gt;=end)
     */
    public boolean isEmpty() {
        return (start >= end);
    }
    
    /**
     * Returns the index within this chunk of the first occurrence
     * of the specified substring.
     */
    public int indexOf(String s) {
        int ret = buf.indexOf(s, start);
        if (ret != -1 && ret < end) {
            return ret - start;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the index within this chunk of the first occurrence
     * of the specified substring.
     */
    public int indexOf(String s, int newStartPoint) {
        // Find the substring
        int ret = buf.indexOf(s, start+newStartPoint);
        // Check the result
        if (ret != -1 && ret < end) {
            return ret - start;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the index within this chunk of the first occurrence
     * of the specified substring.
     */
    public int indexOf(ChunkedString s) {
        int ret = buf.indexOf(s.toString(), start);
        if (ret != -1 && ret < end) {
            return ret - start;
        } else {
            return -1;
        }
    }
    
    /**
     * Creates a new ChunkedString from the specified indexes.
     */
    public ChunkedString substring(int newStart, int newEnd) {
        return new ChunkedString(buf, start+newStart, start+newEnd);
    }
    
    /**
     * Creates a new ChunkedString from the specified indexes.
     */
    public ChunkedString substring(int newStart) {
        return new ChunkedString(buf, start+newStart, end);
    }

    /*
     * Returns the index within this chunk of the last occurrence
     * of the specified substring.
     *
    public int lastIndexOf(String s) {
        int ret = buf.lastIndexOf(s, end) - start;
        return (ret > 0) ? ret : -1;
    }*/
    
    /**
     * Returns the length of the substring.
     */
    public int length() {
        return end-start;
    }
    
    /**
     * Returns true if the chunk starts with the specified string.
     */
    public boolean startsWith(String s) {
        return buf.regionMatches(false, start, s, 0, s.length());
    }

    /**
     * Returns true if the chunk ends with the specified string.
     */
    public boolean endsWith(String s) {
        int endIdx = end - s.length();
        if (endIdx < 0) {
            return false;
        }

        return buf.regionMatches(false, endIdx, s, 0, s.length());
    }
    
    /**
     * Reset the chunk indexes to the lenght of the whole buffer.
     */
    public void reset() {
        start = 0;
        end = buf.length();
    }
    
    /**
     * Move the forward start index of 'n' characters.
     *
     * @param n the number of characters to move the index. If n is negative
     *        start is moved backward.
     * @return <code>true<code/> if the start index is still valid after the move
     *         <code>false<code/> if a boundary has been reached
     *         (start was &lt; 0 if for n negative, or greater than end
     *         for n positive). In the this case, start is made equal
     *         to the boundary.
     */
    public boolean moveStart(int n) {
        start += n;
        
        if(start < 0){
            start = 0;
            return false;
        }
        
        if(start > end) {
            start = end;
            return false;
        }
        
        return true;
    }
    
    /**
     * Move the <b>backward</b> the end index of 'n' characters. If n
     * is negative, end is moved <b>forward</b>.
     *
     * @param n the number of characters to move the index
     * @return <code>true<code/> if the end index is still valid after the move
     *         <code>false<code/> if a boundary has been reached
     *         (end was less than start if for n positive, or greater than
     *         buffer length for n positive). In this case, end is made equal
     *         to the boundary.
     */
    public boolean moveEnd(int n) {
        end -= n;
        
        if(end > buf.length()){
            end = buf.length();
            return false;
        }
        
        if(end < start) {
            end = start;
            return false;
        }
        
        return true;
    }
    
    /**
     * Return a copy of the string limited by the chunk indexes.
     */
    public String toString() {
        return buf.substring(start, end);
    }
    
    // -------------------------------------------------------- Private Methods
    
    private void checkIndexes() throws IndexOutOfBoundsException {
        int l = buf.length();
        if ( start < 0 || end > l || start > end) {
            throw new
                    IndexOutOfBoundsException("ChunckedString("+start+","+end+")");
        }
    }
}


