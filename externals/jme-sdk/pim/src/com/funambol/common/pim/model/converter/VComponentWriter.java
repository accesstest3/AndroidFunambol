/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2007 Funambol, Inc.
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
package com.funambol.common.pim.model.converter;

import com.funambol.common.pim.model.model.Parameter;
import com.funambol.common.pim.model.model.Property;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.model.model.VComponent;
import java.util.Iterator;

/**
 * This class is used to convert a VCalendar object to a string in the
 * vCalendar (1.0) or iCalendar (vCalendar 2.0) format.
 *
 * @version $Id: VComponentWriter.java,v 1.2 2007-11-28 11:14:04 nichele Exp $
 */
public class VComponentWriter {
    
    private static final String CRLF = "\r\n";
    
    public static final int NO_FOLDING = -1;
    public static final int STANDARD_FOLDING = 75;
    
    private int maxLineWidth;
    
    /**
     * Creates a new instance of VComponentWriter, setting the max line width at
     * the default value (presently, NO_FOLDING ie no line width control is 
     * done).
     */
    public VComponentWriter() {
        this.maxLineWidth = NO_FOLDING; // default value
    }
    
    /**
     * Creates a new instance of VComponentWriter, setting the max line width at
     * a given value.
     *
     * @param maxLineWidth an integer that should not be too small (some space
     *                     is needed to write at least the property's name and
     *                     parameters in the first line), usually 
     *                     STANDARD_FOLDING (75) or NO_FOLDING (-1, that means
     *                     that no line width control is expected) 
     */
    public VComponentWriter(int maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
    }
    
    /**
     * Converts a Parameter object into a String, following the vCalendar/
     * iCalendar style.
     *
     * @param parameter the Parameter to be converted
     * @return the result of the conversion, as a String object
     */
    private String toString(Parameter parameter) {
        return (parameter.name + "=" + parameter.value);
    }

    /**
     * Converts a Property object into a String, following the vCalendar/
     * iCalendar style.
     *
     * @param property the Property to be converted
     * @return the result of the conversion, as a String object
     */    
    private String toString(Property property) {
        StringBuffer out = new StringBuffer();
        
        out.append(property.getName());
        
        // Parameters
        Iterator pIter = property.getParameters().iterator();
        while (pIter.hasNext()) {
            out.append(";");
            out.append(toString((Parameter) pIter.next()));
        }
        
        out.append(":");
        
        // Value
        int column = out.length(); // column variable is now properly initialized
        boolean qp = false;

        String unfolded = property.getValue();

        String folded;
        if (maxLineWidth != NO_FOLDING &&
                column + unfolded.length() > maxLineWidth) { // needs be folded
            folded = "";
            int truce = 0;
            boolean escaped = false;
            Parameter encoding = property.getParameter("ENCODING");
            if (encoding != null && encoding.value != null && 
                    encoding.value.equalsIgnoreCase("QUOTED-PRINTABLE")) {
                
                for (int i = 0; i < unfolded.length(); column++, i++) {
                    if ((truce == 0) && (column >= maxLineWidth - 2)) {
                        folded += "=" + CRLF; // QP soft line break
                        column = 0;
                    }
                    folded += unfolded.charAt(i);
                    if (unfolded.charAt(i) == '=') {
                        truce = 2; // because it could be a QP-encoded char
                    } else if (unfolded.charAt(i) == '\\') { // one backslash
                        if (!escaped) { 
                            truce = 1; // becase it could be a \-escaped char
                            escaped = true; 
                        } else { // It's the second backslash of a "\\" sequence
                            truce = 0;
                            escaped = false;
                        }
                    } else if (truce > 0) {
                        truce--;
                    }
                }
                
            } else { // non-Quoted-Printable
                
                for (int i = 0; i < unfolded.length(); column++, i++) {
                    if ((truce == 0) && (column >= maxLineWidth - 1)) {
                        folded += CRLF + " "; // non-QP soft line break
                        column = 1; // because there's already a space in the
                    }                                                // new line
                    folded += unfolded.charAt(i);
                    if (unfolded.charAt(i) == '\\') { // one backslash
                        if (!escaped) { 
                            truce = 1; // becase it could be a \-escaped char
                            escaped = true; 
                        } else { // It's the second backslash of a "\\" sequence
                            truce = 0;
                            escaped = false;
                        }
                    } else if (truce > 0) {
                        truce--;
                    }
                }
            }
            
        } else { // folding's not needed
            
            folded = unfolded;
        }
        
        out.append(folded);
        
        out.append(CRLF);
        
        return out.toString();
        
    }
   
    /**
     * Converts any VComponent object into a String, following the vCalendar/
     * iCalendar style.
     *
     * @param vComponent the VComponent to be converted
     * @return the result of the conversion, as a String object
     */ 
    public String toString(VComponent vComponent) {
        
        StringBuffer out = open(vComponent);
        
        Iterator pIter = vComponent.getAllProperties().iterator ();
        while (pIter.hasNext()) {
            Property property = (Property) pIter.next();
            out.append(toString(property));
        }
        
        Iterator cIter = vComponent.getAllComponents ().iterator ();
        while (cIter.hasNext()) {
            VComponent subcomponent = (VComponent) cIter.next ();
            out.append(toString(subcomponent));
        }
        
        return close(out, vComponent);
    }
    
    /**
     * Performs the operations needed to have a StringBuffer containing the
     * opening line of a VComponent converted into a vCalendar/iCalendar-style
     * text string.
     *
     * @param vComponent the VComponent to be converted
     * @return "BEGIN:" + name of the VComponent + CR + LF
     */   
    private StringBuffer open(VComponent vComponent) {
        
        StringBuffer out = new StringBuffer();
        
        out.append("BEGIN:").append(vComponent.getVComponentName()).append(CRLF);
        
        return out;
    }
    
    /**
     * Performs the operations needed to append to a given StringBuffer the
     * closing line of a VComponent converted into a vCalendar/iCalendar-style
     * text string.
     *
     * @param out the StringBuffer already filled with the VComponent's data in
     *            text format
     * @param vComponent the VComponent to be converted
     * @return out + "END:" + name of the VComponent + CR + LF
     */ 
    private String close(StringBuffer out, VComponent vComponent) {
        
        out.append("END:").append(vComponent.getVComponentName()).append(CRLF);
        
        return out.toString();
    }
    
}
