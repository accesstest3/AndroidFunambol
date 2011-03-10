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
package com.funambol.common.pim.model.common;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.TimeZone;

import com.funambol.util.QuotedPrintable;

/**
 * Represent a converter base class. Provides some common methods.
 */
public abstract class BaseFormatter {

    // --------------------------------------------------------------- Constants
    public final static String ENCODING_QP   = "QUOTED-PRINTABLE";
    public final static String CHARSET_UTF8  = "UTF-8"           ;
    public final static String CHARSET_UTF7  = "UTF-7"           ;
    public final static String ENCODING_B64  = "BASE64"          ;
    public static final String PLAIN_CHARSET = "plain"           ;


    // -------------------------------------------------------------- Properties
    protected TimeZone timezone           = null ;
    protected String   charset            = null ;
    protected boolean  forceClientLocalTime = false;

    /** Specifies the list of supported fields */
    protected Vector<String> supportedFields = null;

    // ------------------------------------------------------------- Constructor

    /**
     * This constructor is deprecated because to handle the date is need to know
     * timezone but also if the dates must be converted in local time.
     *
     * @param timezone the timezone to use in the conversion
     * @param charset the charset to use in the conversion
     * @deprecated
     */
    @Deprecated
    public BaseFormatter(TimeZone timezone, String charset) {
        this.timezone = timezone;
        this.charset  = charset;
    }

    /**
     *
     * @param timezone the timezone to use in the conversion
     * @param charset the charset to use in the conversion
     * @param forceDeviceLocalTime true if the date must be converted in the
     *                             device's local time, false otherwise.
     */
    public BaseFormatter(TimeZone timezone, String charset, boolean forceDeviceLocalTime) {
        this.timezone           = timezone;
        this.charset            = charset;
        this.forceClientLocalTime = forceDeviceLocalTime;
    }


    // ---------------------------------------------------------- Public Methods
    /**
     * Converts the given sDate in UTC
     * using the given timezone
     * @param sDate String
     * @param timezone TimeZone
     * @return String
     * @throws FormatterException
     */
    /*
    public static String handleConversionToUTCDate(String sDate     ,
                                                   TimeZone timezone)
    throws FormatterException {
        try {
            sDate = TimeUtils.convertLocalDateToUTC(sDate, timezone);
        } catch (Exception ex) {
            throw new FormatterException("Error converting date " + sDate
                    + " into UTC format.");
        }
        return sDate;
    }
    */

    /**
     * Converts the given sDate it in local time
     * using the given timezone
     * @param sDate String
     * @param timezone TimeZone
     * @return String
     * @throws FormatterException
     */
    /*
    public static String handleConversionToLocalDate(String   sDate   ,
                                                     TimeZone timezone)
    throws FormatterException {

        if (timezone == null) {
            return sDate;
        }
        try {
            sDate = TimeUtils.convertUTCDateToLocal(sDate, timezone);
        } catch (Exception ex) {
            throw new FormatterException("Error converting date " + sDate
                    + " into local timezone format.");
        }
        return sDate;
    }
    */

    /**
     * Converts the given sDate it in the local time of another time zone.
     *
     * @param sDate String
     * @param timezoneIn TimeZone
     * @param timezoneOut TimeZone
     * @return String
     * @throws FormatterException
     */
    /*
    public static String handleConversionAcrossTimeZones(String   sDate      ,
                                                         TimeZone timezoneIn ,
                                                         TimeZone timezoneOut)
    throws FormatterException {

        try {
            sDate = TimeUtils.convertDateFromTo(sDate,
                                                TimeUtils.PATTERN_UTC_WOZ,
                                                timezoneIn,
                                                timezoneOut);
        } catch (Exception ex) {
            throw new FormatterException("Error converting date " + sDate
                    + " across time zones.");
        }
        return sDate;
    }
    */

    /**
     * Converts the given sDate in all-day format
     * using the given timezone
     * @param sDate as a String, in local time format
     * @return String
     * @throws FormatterException
     */
    /*
    public static String handleConversionToAllDayDate(String sDate)
    throws FormatterException {

        try {
            sDate = TimeUtils.convertDateFromTo(sDate,
                                                TimeUtils.PATTERN_YYYY_MM_DD);
        } catch (Exception ex) {
            throw new FormatterException("Error converting date " + sDate
                    + " into all-day format.");
        }
        return sDate;
    }
    */

    /**
     * Converts the given sDate in all-day format
     * <br>In the conversion the following rules are applied:
     * <ul>
     *     <li>if the given timezoneIn is not null, it is applied on the stringDate
     *         conversion
     *     </li>
     *     <li>if the given timezoneOut is not null, it is applied on the output date
     *     </li>
     * </ul>
     * @param sDate the date to convert
     * @param timezoneIn the timezone to apply to the given date
     * @param timezoneOut the timezone to apply on the output date
     * @return String the date into proper format
     * @throws FormatterException if an error occurs
     */
    /*
    public static String handleConversionToAllDayDate(String   sDate,
                                                      TimeZone timezoneIn,
                                                      TimeZone timezoneOut)
    throws FormatterException {

        try {
            sDate = TimeUtils.convertDateFromTo(sDate,
                                                TimeUtils.PATTERN_YYYY_MM_DD,
                                                timezoneIn,
                                                timezoneOut);
        } catch (Exception ex) {
            throw new FormatterException("Error converting date " + sDate
                    + " into all-day format.");
        }
        return sDate;
    }
    */

    /**
     * Replace values with proper encoding
     *
     * @param value the value to apply the replacement
     * @param encoding the encoding to use (default is QUOTED-PRINTABLE)
     * @param charset the charset to use (default is UTF-8)
     * @return String the value replaced
     */
    public String encode(String value, String encoding, String charset)
    throws FormatterException {
        try {
            if (value == null) {
                return value;
            }

            //
            // If input charset is null then set it with default value as UTF-8
            //
            if (charset == null) {
                charset = "UTF-8";
            }

            if (ENCODING_B64.equals(encoding)) {
                //
                // truncate the b64 encoded text into lines of no more that 76 chars
                //
                StringBuffer sb = new StringBuffer();
                sb.append("\r\n ");
                while(value.length() > 75) {
                    sb.append(value.substring(0,75));
                    sb.append("\r\n");
                    sb.append(" ");

                    value = value.substring(75);
                }
                sb.append(value);
                sb.append("\r\n");

                value = sb.toString();

            } else if (ENCODING_QP.equals(encoding)) {
                byte b[] = value.getBytes(charset);
                b = QuotedPrintable.encode(b);
                value = new String(b, charset);
            }
        } catch(UnsupportedEncodingException e) {
            String msg = "The Character Encoding (" + charset + ") is not supported";
            throw new FormatterException(msg);
        }
        return value;
    }

    /**
     * This method compose the single component of vCard and in particular:
     * 1) encode value with the proper encoding
     * 2) handle the params
     * 3) create a representation of the specificated vCard field
     *
     * @param propertyValue the value of vCard field
     * @param properties a vector of params
     * @param field the filed name
     *
     * @return String the representation of the specificated vCard field
     * @throws com.funambol.common.pim.converter.FormatterException if an error occurs
     */
    public StringBuffer composeVCardComponent(String propertyValue,
                                              ArrayList properties,
                                              String field        )
    throws FormatterException {
        return composeVCardComponent(propertyValue,
                                     properties,
                                     field,
                                     false);
    }

    /**
     * This method compose the single component of vCard and in particular:
     * 1) encode value with the proper encoding
     * 2) handle the params
     * 3) create a representation of the specificated vCard field
     *
     * @param propertyValue the value of vCard field
     * @param properties a vector of params
     * @param field the filed name
     *
     * @param excludeCharset must the charset be not set ?
     * @return String the representation of the given vCard field
     * @throws com.funambol.common.pim.converter.FormatterException if an error occurs
     */
    public StringBuffer composeVCardComponent(String propertyValue,
                                              ArrayList properties,
                                              String field        ,
                                              boolean excludeCharset)
    throws FormatterException {

        StringBuffer result = new StringBuffer(120);

        try {
            String group = getGrouping(properties);
            if (group != null && !group.equals("")) {
                result.append(group).append(".");
            }
            result.append(field);

            String encodingParam = getEncoding(properties);
            if (encodingParam == null) {
                encodingParam = ENCODING_QP;
            }

            String charsetParam  = getCharset(properties);
            if (charsetParam == null) {
                if (charset == null) {
                    charsetParam = CHARSET_UTF8;
                } else {
                    charsetParam = charset;
                }
            }

            if (propertyValue == null) {
                propertyValue = "";
            } else {
                if (!PLAIN_CHARSET.equalsIgnoreCase(charsetParam)) {
                    //
                    // We encode the value only if the charset isn't PLAIN_CHARSET
                    //
                    // At this level we have always an ENCODING (at least QP)
                    //
                    propertyValue = encode(propertyValue,
                                           encodingParam,
                                           charsetParam);

                    //
                    // We set the ENCODING and the CHARSET only if:
                    // 1. we are using the QP and the result doesn't contain any '='
                    //    (the value doesn't contain chars to encode)
                    // or
                    // 2. we have a different encoding from QP
                    //    (in this way we preserve the original property encoding)
                    //
                    if (ENCODING_QP.equalsIgnoreCase(encodingParam) &&
                        propertyValue.indexOf("=") != -1) {
                        result.append(";ENCODING=").append(encodingParam);
                        if (!excludeCharset) {
                            result.append(";CHARSET=").append(charsetParam);
                        }
                    } else if (!ENCODING_QP.equalsIgnoreCase(encodingParam)) {
                        result.append(";ENCODING=").append(encodingParam);
                        if (!excludeCharset) {
                            result.append(";CHARSET=").append(charsetParam);
                        }
                    }
                }
            }
            String languageParam = getLanguage(properties);
            if (languageParam != null) {
                result.append(";LANGUAGE=").append(languageParam);
            }
            String valueParam = getValue(properties);
            if (valueParam != null) {
                result.append(";VALUE=").append(valueParam);
            }
            String typeParam  = getType(properties);
            if (typeParam != null) {
                result.append(";TYPE=").append(typeParam);
            }
            result.append(":").append(propertyValue).append("\r\n");

        } catch(Exception e) {
            throw new FormatterException("Error composing VCard component ");
        }

        // Once formatted remove the property from the supported list
        if(supportedFields != null) {
            supportedFields.remove(field);
        }

        return result;
    }

    /**
     * This method compose the single component of iCalendar and in particular:
     * 1) encode value with the proper encoding
     * 2) handle the params
     * 3) create a representation of the specificated iCalendar field
     *
     * @param value the value of iCalendar field
     * @param properties a vector of params
     * @param field the field name
     * @return the representation of the specificated iCalendar field as a
     *         StringBuffer object
     *
     * @deprecated Unused since version 6.5, replaced by methods in
     *             {@link #com.funambol.common.pim.converter.CalendarContentConverter}
     */
    public StringBuffer composeICalTextComponent(Property property, String field)
    throws FormatterException {

        StringBuffer result = new StringBuffer(240); // Estimate 240 is needed
        result.append(field);
        String propertyValue = escapeSeparator((String)property.getPropertyValue());

        try {
            //
            // Encode value as QUOTED-PRINTABLE and set encodingParam at the
            // default value (at the moment we handle only ENCODING=QUOTED-PRINTABLE)
            //
            String encodingParam = ENCODING_QP;
            String charsetParam  = property.getCharset();
            if (charsetParam == null) {
                if (charset == null) {
                charsetParam = CHARSET_UTF8;
                } else {
                    charsetParam = charset;
                }
            }

            if (propertyValue == null) {
                propertyValue = "";
            } else {

                if (!PLAIN_CHARSET.equalsIgnoreCase(charsetParam)) {
                    //
                    // We encode the value only if the charset isn't PLAIN_CHARSET
                    //
                    propertyValue = encode(propertyValue, encodingParam,
                                           charsetParam);

                    if (propertyValue.indexOf("=") != -1) {
                        result.append(";ENCODING=").append(encodingParam);
                        result.append(";CHARSET=").append(charsetParam);
                    }
                }
            }
            String altrepParam = property.getAltrep();
            if (altrepParam != null) {
                result.append(";ALTREP=").append(altrepParam);
            }
            String languageParam = property.getLanguage();
            if (languageParam != null) {
                result.append(";LANGUAGE=").append(languageParam);
            }
            String cnParam = property.getCn();
            if (cnParam != null) {
                result.append(";CN=").append(cnParam);
            }
            String cuttypeParam = property.getCutype();
            if (cuttypeParam != null) {
                result.append(";CUTYPE=").append(cuttypeParam);
            }
            String delegatedFromParam = property.getDelegatedFrom();
            if (delegatedFromParam != null) {
                result.append(";DELEGATED-FROM=").append(delegatedFromParam);
            }
            String delegatedToParam = property.getDelegatedTo();
            if (delegatedToParam != null) {
                result.append(";DELEGATED-TO=").append(delegatedToParam);
            }
            String dirParam = property.getDir();
            if (dirParam != null) {
                result.append(";DIR=").append(dirParam);
            }
            String memberParam = property.getMember();
            if (memberParam != null) {
                result.append(";MEMBER=").append(memberParam);
            }
            String partstatParam = property.getPartstat();
            if (partstatParam != null) {
                result.append(";PARTSTAT=").append(partstatParam);
            }
            String relatedParam = property.getRelated();
            if (relatedParam != null) {
                result.append(";RELATED=").append(relatedParam);
            }
            String sentbyParam = property.getSentby();
            if (sentbyParam != null) {
                result.append(";SENT-BY=\"").append(sentbyParam).append("\"");
            }
            String valueParam = property.getValue();
            if (valueParam != null) {
                result.append(";VALUE=").append(valueParam);
            }

            result.append(getXParams(property));

            result.append(":").append(propertyValue).append("\r\n");

        } catch(Exception e) {
            throw new FormatterException("Error composing iCalendar component ");
        }
        return result;
    }

    /**
     * Compose the remaining fields from the supportedFields Vector.
     * @return
     */
    protected StringBuffer composeRemainingFields() {
        StringBuffer result = new StringBuffer();
        if(supportedFields != null && supportedFields.size() > 0) {
            for(int i=0; i<supportedFields.size(); i++) {
                String field = supportedFields.elementAt(i);
                String value = "";
                if(field.equals("ORG")) {
                    value = ";";
                } else if(field.startsWith("ADR")) {
                    value = ";;;;;;";
                }
                result.append(field).append(":").append(value).append("\r\n");
            }
        }
        return result;
    }

    /**
     * A SEMI-COLON in a property value MUST be escaped with a '\' character.
     * A BACKSLASH in a property value MUST be escaped with a '\' character.
     *
     * @param value the value in which replaced the ; and the \
     */
    public String escapeSeparator(String value) {
        String tmp = value.replaceAll("\\\\", "\\\\\\\\");
        tmp = tmp.replaceAll(";", "\\\\;");
        return tmp;
    }

    // --------------------------------------------------------- Private Methods
    private String getEncoding(ArrayList properties) {
        for (int i=0;i<properties.size();i++) {
            if (((Property)properties.get(i)).getEncoding()!=null) {
               return ((Property)properties.get(i)).getEncoding();
            }
        }
        return null;
    }

    private String getCharset(ArrayList properties) {
        for (int i=0;i<properties.size();i++) {
            if (((Property)properties.get(i)).getCharset()!=null) {
               return ((Property)properties.get(i)).getCharset();
            }
        }
        return null;
    }
    private String getGrouping(ArrayList properties) {
        for (int i=0;i<properties.size();i++) {
            if (((Property)properties.get(i)).getGroup()!=null)
                return ((Property)properties.get(i)).getGroup();
        }
        return null;
    }
    private String getLanguage(ArrayList properties) {
        for (int i=0;i<properties.size();i++) {
            if (((Property)properties.get(i)).getLanguage()!=null) {
                return ((Property)properties.get(i)).getLanguage();
            }
        }
        return null;
    }

    private String getValue(ArrayList properties) {
        for (int i=0;i<properties.size();i++) {
            if (((Property)properties.get(i)).getValue()!=null)
                return ((Property)properties.get(i)).getValue();
        }
        return null;
    }

    private String getType(ArrayList properties) {
        for (int i=0;i<properties.size();i++) {
            if (((Property)properties.get(i)).getType()!=null) {
                return ((Property)properties.get(i)).getType();
            }
        }
        return null;
    }

    private StringBuffer getXParams(Property property) {
        ArrayList properties = new ArrayList();
        properties.add(property);
        return getXParams(properties);
    }

    private StringBuffer getXParams(ArrayList properties) {
        HashMap  hm       = null;
        Iterator it       = null;
        Property xtagProp = null;
        String tag        = null;
        String value      = null;
        StringBuffer params = new StringBuffer();

        for (int i=0;i<properties.size();i++) {
            xtagProp = (Property)properties.get(i);
            hm = xtagProp.getXParams();
            it = hm.keySet().iterator();
            while(it.hasNext()) {
                tag   = (String)it.next();
                value = (String)hm.get(tag);
                //
                // If tag is the same as value then this tag is handle as
                // param without value
                //
                if (tag.equals(value) || value == null) {
                    params.append(";").append(tag);
                } else {
                    params.append(";").append(tag).append("=").append(value);
                }
            }
        }
        return params;
    }
}
