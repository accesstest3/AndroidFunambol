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
package com.funambol.common.pim.model.converter;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.funambol.util.QuotedPrintable;

import com.funambol.common.pim.model.calendar.Calendar;
import com.funambol.common.pim.model.calendar.CalendarContent;
import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.model.Parameter;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.model.model.VCalendarContent;
import com.funambol.common.pim.model.model.VComponent;
import com.funambol.common.pim.model.model.VTimezone;
import com.funambol.common.pim.model.utility.TimeUtils;

/**
 * This object is a converter from a Calendar object model to a VCalendar string
 *
 * @see Converter
 * @version $Id: VCalendarConverter.java,v 1.13 2008-09-03 09:51:04 mauro Exp $
 */
public class VCalendarConverter extends BaseConverter {

    final private String DATE_OR_DATE_TIME_REGEX =
            "([1-2][0-9]{3}(\\-)?[0-1][0-9](\\-)?[0-3][0-9])(T[0-2][0-9][0-5][0-9][0-5][0-9](Z)?)?";
    final private String DATE_TIME_REGEX =
            "[1-2][0-9]{3}[0-1][0-9][0-3][0-9]T[0-2][0-9][0-5][0-9][0-5][0-9](Z)?";
    
    // Policies for conversion of date/time properties:
    final private int FLOATING_POLICY    = 0;
    final private int UTC_POLICY         = 1;
    final private int PROPERTY_TZ_POLICY = 2;
    final private int CLIENT_TZ_POLICY   = 3;

    // ------------------------------------------------------------- Constructor

    /**
     * This constructor is deprecated because to handle the date is need to know
     * timezone but also if the dates must be converted in local time.
     */
    @Deprecated
    public VCalendarConverter(TimeZone timezone, String charset) {
        super(timezone, charset);
    }

    /**
     *
     * @param timezone the timezone to use in the conversion
     * @param charset the charset
     * @param forceClientLocalTime true if the date must be converted in the 
     *                             client local time, false otherwise.
     */
    public VCalendarConverter(TimeZone timezone, String charset, boolean forceClientLocalTime) {
        super(timezone, charset, forceClientLocalTime);
    }

    // ---------------------------------------------------------- Public Methods

    public String convert(Object obj) throws ConverterException {
        return null;
    }

    /**
     * Performs the VCalendar-to-Calendar conversion, finding out automatically
     * which type of calendar (Event or Todo) it is.
     *
     * @param vcal the VCalendar object to be converted
     * @return a Calendar containing the converted representation of this
     *         VCalendar
     * @throws com.funambol.common.pim.converter.ConverterException
     */
    public Calendar vcalendar2calendar(VCalendar vcal)
     throws ConverterException {

        VCalendarContentConverter vccc =
                new VCalendarContentConverter(timezone, charset, false);
        Calendar cal= new Calendar();
        setCommonProperties(cal, vcal);
        boolean xv = false;
        if (vcal.getProperty("VERSION")            != null &&
            vcal.getProperty("VERSION").getValue() != null &&
            "1.0".equals(vcal.getProperty("VERSION").getValue())) {

            xv = true;
        }
        VCalendarContent vcc = vcal.getVCalendarContent();

        // Creates the list of available timezones (often, just one)
        Map<String, TimeZoneHelper> timeZones =
                new Hashtable<String, TimeZoneHelper>(3);
        if (xv) {
            com.funambol.common.pim.model.model.Property tz = vcal.getProperty("TZ");
            List<com.funambol.common.pim.model.model.Property> daylightList =
                    vcal.getProperties("DAYLIGHT");
            try {
                TimeZoneHelper vctz = new CachedTimeZoneHelper(tz          ,
                                                               daylightList);
                timeZones.put(vctz.getName(), vctz);
            } catch (Exception e) {
                timeZones.clear();
            }
        } else {
            long[] interval = vccc.extractInterval(vcal.getVCalendarContent());
            List<VComponent> vTimezones =
                    vcal.getComponents(VTimezone.COMPONENT_NAME);
            for (VComponent vTimezone : vTimezones) {
                try {
                    TimeZoneHelper vctz =
                            new CachedTimeZoneHelper((VTimezone) vTimezone,
                                                        interval[0]          ,
                                                        interval[1]          );
                    timeZones.put(vctz.getName(), vctz);
                } catch (Exception e) {
                    // Skips this one
                }
            }
        }

        // Fill the three time-zone "slots"
        TimeZone dtStartTimeZone, dtEndTimeZone, reminderTimeZone;
        if (timeZones.isEmpty()) {
            dtStartTimeZone = dtEndTimeZone = reminderTimeZone = timezone;
        } else if (xv) {
            String allTimeZones = timeZones.get("").toID(timezone);
            if (allTimeZones == null) {
                dtStartTimeZone = dtEndTimeZone = reminderTimeZone = timezone;
            } else {
                dtStartTimeZone = dtEndTimeZone = reminderTimeZone =
                        TimeZone.getTimeZone(allTimeZones);
            }
        } else { // only iCalendar (vCalendar 2.0) items can reach this point
            List<com.funambol.common.pim.model.model.Property> dtStart =
                    new ArrayList<com.funambol.common.pim.model.model.Property>(1);
            dtStart.add(vcc.getProperty("DTSTART"));
            dtStartTimeZone = matchTimeZone(dtStart, timeZones);
            List<com.funambol.common.pim.model.model.Property> dtEnd =
                    new ArrayList<com.funambol.common.pim.model.model.Property>(2);
            dtEnd.add(vcc.getProperty("DTEND"));
            dtEnd.add(vcc.getProperty("DUE"));
            dtEndTimeZone = matchTimeZone(dtEnd, timeZones);
            /*
            List<com.funambol.common.pim.model.Property> reminders =
                    new ArrayList<com.funambol.common.pim.model.Property>(3);
            // @todo fill reminders...
            reminderTimeZone = matchTimeZone(reminders, timeZones);
             */
            reminderTimeZone = null; // Provisional "solution"
        }

        CalendarContent cc = vccc.vcc2cc(vcc             ,
                                         xv              ,
                                         dtStartTimeZone ,
                                         dtEndTimeZone   ,
                                         reminderTimeZone);
        cal.setCalendarContent(cc);
        return cal;
    }

    /**
     * Performs the Calendar-to-VCalendar conversion.
     *
     * @param cal the Calendar object to be converted
     * @param xv true if the text/x-vcalendar format must be used while
     *           generating some properties of the VCalendar output object
     * @return a VCalendar containing the converted representation of this
     *         Calendar
     * @throws com.funambol.common.pim.converter.ConverterException
     */
    public VCalendar calendar2vcalendar(Calendar cal, boolean xv)
     throws ConverterException {
        VCalendar vcal= new VCalendar();
        CalendarContent cc = cal.getCalendarContent();
        setCommonProperties(vcal, cal, xv);
        String version = (xv ? "1.0" : "2.0");
        vcal.setProperty(new com.funambol.common.pim.model.model.Property(
                "VERSION", false, new ArrayList(), version));
        VCalendarContentConverter vccc =
                new VCalendarContentConverter(timezone, charset, forceClientLocalTime);
        VCalendarContent vcc = vccc.cc2vcc(cc, xv);

        if (xv && !forceClientLocalTime) {
            String id = cc.getDtStart().getTimeZone();
            if ((id != null) && (id.length() != 0)) {
                long[] interval = cc.extractInterval();
                TimeZoneHelper tz = new CachedTimeZoneHelper(id         ,
                                                             interval[0],
                                                             interval[1]);
                for (com.funambol.common.pim.model.model.Property xvCalendarProperty :
                     tz.getXVCalendarProperties()){
                    vcal.addProperty(xvCalendarProperty);
                }
            }
        } else if (!xv) {
            List<String> timeZoneIDs = new ArrayList<String>(3);

            addIfNeeded(cc.getDtStart() , timeZoneIDs);
            addIfNeeded(cc.getDtEnd()   , timeZoneIDs);
            addIfNeeded(cc.getReminder(), timeZoneIDs);
            if (forceClientLocalTime) {
                if (hasNoTimeZone(cc.getDtStart()) ||
                    hasNoTimeZone(cc.getDtEnd())   ||
                    hasNoTimeZone(cc.getReminder()) ) {
                    // In this case, the device TZ might need to be added to the
                    // list
                    String clientTimeZoneID = timezone.getID();
                    boolean addClientTimeZoneID = true;
                    for (String existingID : timeZoneIDs) {
                        if (existingID.equals(clientTimeZoneID)) {
                            addClientTimeZoneID = false;
                            break;
                        }
                    }
                    if (addClientTimeZoneID) {
                                    timeZoneIDs.add(timezone.getID());
                    }
                }
            }
            if (!timeZoneIDs.isEmpty()) {
                long[] interval = cc.extractInterval();
                for (String id : timeZoneIDs) {
                    TimeZoneHelper tz = new CachedTimeZoneHelper(id         ,
                                                                   interval[0],
                                                                   interval[1]);
                    VTimezone vtz = tz.getVTimezone();
                    vcal.addComponent(vtz);
                }
            }
        }

        vcal.addComponent(vcc);

        return vcal;
    }

    //-------------------------------------------------------- Protected Methods

    /**
     * Creates an HashMap with the X-Param set extracted from a
     * com.funambol.common.pim.model.Property object. The encoding and charset
     * parameters are ignored.
     *
     * @param property the Property object (containing the X-Param ArrayList)
     * @return a list of X-Param's
     */
    protected Map<String, String> getParameters(
            com.funambol.common.pim.model.model.Property property) {

        Map<String, String> parameters
                = new HashMap<String, String>(property.getParameters().size());
        Iterator it = property.getParameters().iterator();
        while(it.hasNext()) {
            Parameter parameter = (Parameter) it.next();
            parameters.put(parameter.name, parameter.value);
        }
        return parameters;
    }

    /**
     * Creates an ArrayList with the X-Param set extracted from a
     * com.funambol.common.pim.common.Property object.
     *
     * @param property the Property object (containing the X-Param HashMap)
     * @return a list of X-Param's
     */
    protected List<Parameter> getXParams(Property property) {

        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        if (property.getXParams() != null && property.getXParams().size() > 0) {
            Map<String, String> h = property.getXParams();
            Iterator<String> it = h.keySet().iterator();
            String tag   = null;
            String value = null;
            Parameter parameter = null;
            while(it.hasNext()) {
                tag   = new String(it.next());
                value = new String(h.get(tag));
                parameter = new Parameter(tag, value);
                parameters.add(parameter);
            }
        }
        return parameters;
    }


    /**
     * Added X-Param to the input list of the property parameters
     * The buffer iterates throguh the parameters and adds the
     * start parameter char ';' and then the parameter.
     * Avoids the add the starting ';' by the caller and delete
     * the trailing ';' here.
     *
     * @param paramList the list of standard param
     * @param prop the property object
     *
     */
    protected void addXParams(StringBuffer paramList, Property prop) {
        if (prop.getXParams() != null && prop.getXParams().size() > 0) {
            Map<String, String> h = prop.getXParams();
            Iterator<String> it = h.keySet().iterator();
            String tag   = null;
            String value = null;
            while(it.hasNext()) {
                tag   = it.next();
                value = h.get(tag);
                //
                // If tag is the same as value then this tag is handle as
                // param without value
                //
                if (tag.equals(value)) {
                    paramList.append(';').append(tag);
                } else {
                    paramList.append(';')
                             .append(tag)
                             .append("=\"")
                             .append(value)
                             .append('\"');
                }
            }
        }
    }

    /**
     * @param label
     * @param property
     * @param xvCalendar true only if this property is part of a vCalendar (1.0)
     *                   item, false if it is part of an iCalendar (2.0)
     * @return a representation of the event field
     */
    protected com.funambol.common.pim.model.model.Property composeField(String label,
                                                            Property property ,
                                                            boolean xvCalendar) {

        if (property == null || property.getPropertyValueAsString() == null) {
            return null;
        }

        com.funambol.common.pim.model.model.Property out = null;
        String name            = label;
        boolean xtag           = false;
        List<Parameter> params = getXParams(property);
        String  value          = new String(property.getPropertyValueAsString());

        value = addCRBeforeEachLF(value);

        boolean qpNeeded = false;

        // Check if QP-encoding is needed (iCalendar is never QP-encoded)
        if (xvCalendar && isQPProperty(name)) {
            try {
                qpNeeded = true;
                value = QuotedPrintable.encode(value, charset);
                if (value.indexOf('=') == -1) {
                    qpNeeded = false; // The encoding was useless
                }
            } catch (UnsupportedEncodingException uee) { // It shouldn't happen
                // The value won't be encoded
                value = new String(property.getPropertyValueAsString());
                qpNeeded = false;
            }
        }

        if (qpNeeded) { // QP codec has been used to encode the value
            // Escapes only commas, semi-colons and backslashes
            value = vCalEscapeButKeepNewlines(value);

            params.add(new Parameter("ENCODING", "QUOTED-PRINTABLE"));
            params.add(new Parameter("CHARSET", charset));

        } else if (!isComplexProperty(name)) { // Checks if escaping is needed
            // Escapes also line breaks
            if (xvCalendar) {
                value = vCalEscape(value);
            } else {
                value = iCalEscape(value);
            }
        }

        if (value != null){
            out = new com.funambol.common.pim.model.model.Property(
                    name, xtag, params, value);
        }

        return out;

    }

    // --------------------------------------------------------- Private Methods

    /**
     * Sets on a Calendar object those properties which are shared among
     * calendars of both types. The properties are set according to the content
     * of a given VCalendar object.
     *
     * @param cal
     * @param vcal
     * @throws ConverterException
     */
    private void setCommonProperties(Calendar cal, VCalendar vcal)
            throws ConverterException {

        cal.setProdId(decodeField(vcal.getProperty("PRODID")));
        cal.setVersion(decodeField(vcal.getProperty("VERSION")));
        cal.setCalScale(decodeField(vcal.getProperty("CALSCALE")));
        cal.setMethod(decodeField(vcal.getProperty("METHOD")));
    }

    /**
     * Sets on a VCalendar object those properties which are shared among
     * calendars of both types. The properties are set according to the content
     * of a given Calendar object.
     *
     * @param vcal
     * @param cal
     * @param xvCalendar true only if this property is part of a vCalendar (1.0)
     *                   item, false if it is part of an iCalendar (2.0)
     * @throws ConverterException
     */
    private void setCommonProperties(VCalendar vcal, Calendar cal, boolean xvCalendar)
            throws ConverterException {

        if (cal.getProdId() != null){
            com.funambol.common.pim.model.model.Property prodId =
                    composeField("PRODID", cal.getProdId(), xvCalendar);
            if (prodId != null){
                vcal.addProperty(prodId);
            }
        }

        if (cal.getVersion() != null){
            com.funambol.common.pim.model.model.Property version =
                    composeField("VERSION", cal.getVersion(), xvCalendar);
            if (version != null){
                vcal.addProperty(version);
            }
        }

        if (cal.getCalScale() != null){
            com.funambol.common.pim.model.model.Property calscale =
                    composeField("CALSCALE", cal.getCalScale(), xvCalendar);
            if (calscale != null){
                vcal.addProperty(calscale);
            }
        }

        if (cal.getMethod() != null){
            com.funambol.common.pim.model.model.Property method =
                    composeField("METHOD", cal.getMethod(), xvCalendar);
            if (method != null){
                vcal.addProperty(method);
            }
        }
    }

    protected Short decodeShortField(
            com.funambol.common.pim.model.model.Property property) {

        if (property == null) {
            return null;
        }

        String value = property.getValue();

        if (value == null) {
            return null;
        }

        return new Short(Short.parseShort(value));
    }

    /**
     *
     * @param label
     * @param property
     * @param allDay
     * @param xvCalendar
     * @return
     */
    protected com.funambol.common.pim.model.model.Property composeDateTimeField(
        String               label            ,
        PropertyWithTimeZone property         ,
        boolean              allDay           ,
        boolean              xvCalendar       ) {

        return composeDateTimeField(label, property, allDay, xvCalendar, false);
    }

    protected com.funambol.common.pim.model.model.Property composeDateTimeField(
        String               label            ,
        PropertyWithTimeZone property         ,
        boolean              allDay           ,
        boolean              xvCalendar       ,
        boolean              isRecurrence     ) {

        com.funambol.common.pim.model.model.Property p =
            composeField(label, property, xvCalendar);

        if (p != null) {            
            try {
                String propertyTimeZoneID = property.getTimeZone();
                boolean hasTimeZone = (propertyTimeZoneID != null);
                
                int policy = decidePolicy(allDay      ,
                                          xvCalendar  , 
                                          isRecurrence, 
                                          hasTimeZone );

                if (policy == FLOATING_POLICY) {
                    if (!xvCalendar) { // iCalendar
                        removeTime(p);
                    }
                }
                if ((policy == UTC_POLICY) && hasTimeZone) {
                    replaceInDateTime(p                                       ,
                                      TimeZone.getTimeZone(propertyTimeZoneID), 
                                      null                                    );
                }
                
                if (policy == PROPERTY_TZ_POLICY) {
                    replaceInDateTime(p                                       ,
                                      null                                    ,
                                      TimeZone.getTimeZone(propertyTimeZoneID));
                    if (!xvCalendar) { // iCalendar
                        p.setParameter(new Parameter("TZID", propertyTimeZoneID)); 
                    }
                }
                
                if (policy == CLIENT_TZ_POLICY) {
                    if (hasTimeZone) {
                        replaceInDateTime(p                                       ,
                                          TimeZone.getTimeZone(propertyTimeZoneID),
                                          timezone                                );
                    }
                    replaceInDateTime(p               ,
                                      null            ,
                                      timezone        );
                    if (!xvCalendar) {
                        p.setParameter(new Parameter("TZID", timezone.getID())); 
                    }
                }
                
            } catch (ConverterException e) {
                p.setValue(null);
            }
        }
        return p;
    }

    protected Property decodeField(
            com.funambol.common.pim.model.model.Property property) {

        if (property == null) {
            return null;
        }

        String value = property.getValue();

        if (value == null) {
            return null;
        }

        Property encoded = null;

        String fieldCharset;
        Parameter fieldCharsetParameter = property.getParameter("CHARSET");
        if (fieldCharsetParameter == null) {
            fieldCharset = charset; // uses the default charset for the device
        } else {
            fieldCharset = fieldCharsetParameter.value; // uses the charset
        }                                            // specified for this field

        // Check if decoding is needed
        Parameter encodingParameter = property.getParameter("ENCODING");
        if ((encodingParameter != null) &&
                (ENCODING_QP.equalsIgnoreCase(encodingParameter.value))) {
            value = QuotedPrintable.decode(value.getBytes(), fieldCharset);
        }

        // Check if unescaping is needed
        if (!isComplexProperty(property.getName())) {
            value = vCalUnescape(value);
        }

        // @todo Implement other encoding types

        encoded = new Property(value);

        Map<String, String> parameters = getParameters(property);
        parameters.remove("ENCODING"); // No, thanks
        parameters.remove("CHARSET"); // No, thanks

        encoded.setAltrep((String) parameters.remove("ALTREP"));
        encoded.setCn((String) parameters.remove("CN"));
        encoded.setCutype((String) parameters.remove("CUTYPE"));
        encoded.setDelegatedFrom((String) parameters.remove("DELEGATED-FROM"));
        encoded.setDelegatedTo((String) parameters.remove("DELEGATED-TO"));
        encoded.setDir((String) parameters.remove("DIR"));
        encoded.setGroup((String) parameters.remove("GROUP"));
        encoded.setLanguage((String) parameters.remove("LANGUAGE"));
        encoded.setMember((String) parameters.remove("MEMBER"));
        encoded.setPartstat((String) parameters.remove("PARTSTAT"));
        encoded.setRelated((String) parameters.remove("RELATED"));
        encoded.setSentby((String) parameters.remove("SENT-BY"));
        encoded.setTag((String) parameters.remove("TAG"));
        encoded.setType((String) parameters.remove("TYPE"));
        encoded.setValue((String) parameters.remove("VALUE"));

        encoded.setXParams(parameters); // All we left behind

        return encoded;
    }

    protected PropertyWithTimeZone decodeDateTimeField(
            com.funambol.common.pim.model.model.Property property,
            TimeZone fieldTimezone) {
        Property decodedField = decodeField(property);
        if (decodedField == null) {
            return new PropertyWithTimeZone();
        }
        if (fieldTimezone == null) {
            return new PropertyWithTimeZone(decodeField(property), null);
        }
        return new PropertyWithTimeZone(decodeField(property), fieldTimezone.getID());
    }

    //----------------------------------------------------------- Public methods

    /**
     * This method is used to recognize those properties that could have a
     * complex (ie, made of different semicolon-separated and comma-separated
     * parts) content. For such properties, the content saved into the DB is not
     * unescaped, in order to keep the difference between "true" (ie, escaped)
     * commas and semicolons and those used as separators (ie, unescaped).
     *
     * @param name the name of the property, as a String
     * @return true if the property can have, according to the specification, a
     *              complex content and therefore mustn't be (un)escaped
     */
    public static boolean isComplexProperty(String name) {
        //
        // Returns true for all properties that could have a complex content
        //
        if (("RRULE"     ).equals(name) ||
            ("EXRULE"    ).equals(name) ||
            ("RDATE"     ).equals(name) ||
            ("EXDATE"    ).equals(name) ||
            ("AALARM"    ).equals(name) ||
            ("PALARM"    ).equals(name) ||
            ("DALARM"    ).equals(name) ||
            ("MALARM"    ).equals(name) ||
            ("CATEGORIES").equals(name) ||
            ("GEO"       ).equals(name)  )
        {
            return true;
        }
        return false;
    }

    /**
     * This method is used to recognize those properties that have been decided
     * to be encoded with Quoted-Printable. This information is to be used only
     * in the Calendar-to-VCalendar side of the converter.
     *
     * @param name the name of the property, as a String
     * @return true if the property must be QP-encoded
     */
    public static boolean isQPProperty(String name) {

        //
        // Returns false for all properties that are never encoded
        //
        if (("DTSTART"      ).equals(name) ||
            ("DTEND"        ).equals(name) ||
            ("DUE"          ).equals(name) ||
            ("DTSTAMP"      ).equals(name) ||
            ("CREATED"      ).equals(name) ||
            ("DCREATED"     ).equals(name) ||
            ("LAST-MODIFIED").equals(name) ||
            ("COMPLETED"    ).equals(name) ||
            ("RRULE"        ).equals(name) ||
            ("RDATE"        ).equals(name) ||
            ("EXDATE"       ).equals(name) ||
            ("EXRULE"       ).equals(name)  )
        {
            return false;
        }
        return true;
    }

    public static String vCalEscapeButKeepNewlines(String raw) {
        return (raw
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll(";", "\\\\;"));
    }

    public static String vCalEscape(String raw) {
        return (raw
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\r\\n", "\\\\N")
                .replaceAll("\\n", "\\\\N")
                .replaceAll(";", "\\\\;"));
    }

    public static String iCalEscape(String raw) {
        return (raw
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\r\\n", "\\\\N")
                .replaceAll("\\n", "\\\\N")
                .replaceAll(",", "\\\\,")
                .replaceAll(";", "\\\\;"));
    }

    public static String vCalUnescape(String raw) {

        StringBuffer unescaped = new StringBuffer();

        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '\\') { // a backslash
                char nextChar = raw.charAt(++i); // i is being increased
                if ((nextChar == 'N') | (nextChar == 'n')) {
                    unescaped.append("\r\n");
                    continue;
                } // In the other case, we just use the escaped character
            }
            unescaped.append(raw.charAt(i));
        }

        return unescaped.toString();
    }

    public static String addCRBeforeEachLF(String raw) {
        return (raw
                .replaceAll("(\\r\\n|\\n|\\r)", "\r\n"));
    }

    //---------------------------------------------------------- Private methods

    private TimeZone matchTimeZone(
            List<com.funambol.common.pim.model.model.Property> properties,
            Map<String, TimeZoneHelper> timeZones) {
        for (com.funambol.common.pim.model.model.Property property : properties) {
            if (property != null) {
                Parameter tzIDparameter = property.getParameter("TZID");
                if ((tzIDparameter != null) && (tzIDparameter.value != null)) {
                    TimeZoneHelper timezoneHelper =
                            timeZones.get(tzIDparameter.value);
                    if (timezoneHelper != null) {
                        return TimeZone.getTimeZone(timezoneHelper.toID(timezone));
                    }
                }
            }
        }
        return null;
    }

    /**
     * Adds the time-zone ID to the list only if it exists and is not yet there.
     */
    private boolean addIfNeeded(PropertyWithTimeZone property, List<String> list) {

        if (property == null) {
            return false;
        }
        String timeZoneID = property.getTimeZone();
        if ((timeZoneID != null) && (timeZoneID.length() != 0)) {
            for (String otherString : list) {
                if (otherString.equals(timeZoneID)) {
                    return false;
                }
            }
            list.add(timeZoneID);
            return true;
        }
        return false;
    }
    
    /**
     * Determines the policy to follow in the format conversion of a date/time
     * property on the basis of different criteria.
     */
    int decidePolicy(boolean allDay, boolean xvCalendar, boolean isRecurrence, boolean hasTimeZone) {
        
        if (allDay) {
            return FLOATING_POLICY;
        }
        
        if (xvCalendar) { // vCalendar 1.0
            
            if (forceClientLocalTime) {
                return (timezone != null) ? CLIENT_TZ_POLICY : UTC_POLICY;
            }
            if (isRecurrence && hasTimeZone) {
                return PROPERTY_TZ_POLICY;
            }
            return UTC_POLICY;
            
        } else {          // iCalendar 2.0
         
            if (hasTimeZone) {
                return PROPERTY_TZ_POLICY;
            }
            if (forceClientLocalTime) {
                return (timezone != null) ? CLIENT_TZ_POLICY : UTC_POLICY;
            }
            return UTC_POLICY;
            
        }
    }
    
    private void replaceInDateTime(com.funambol.common.pim.model.model.Property p,
                                   TimeZone timeZoneIn, TimeZone timeZoneOut) 
    throws ConverterException {
        
        Pattern pattern = Pattern.compile(DATE_TIME_REGEX);
        Matcher matcher = pattern.matcher(p.getValue());
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            boolean lookForUTC         = (timeZoneIn == null);
            boolean foundUTC           = matcher.group().endsWith("Z");
            if (lookForUTC == foundUTC) {
                String replacement;
                if (lookForUTC && (timeZoneOut != null)) { // UTC -> local
                    replacement = handleConversionToLocalDate(matcher.group(),
                                                              timeZoneOut    );
                    
                } else if (!lookForUTC && (timeZoneOut == null)) { // local -> UTC
                    replacement = handleConversionToUTCDate(matcher.group(),
                                                            timeZoneIn     );
                    
                } else if (!lookForUTC && (timeZoneOut != null)) { // local -> local
                    replacement = handleConversionAcrossTimeZones(matcher.group(),
                                                                  timeZoneIn     ,
                                                                  timeZoneOut    );
                    
                } else { // UTC -> floating
                    replacement = matcher.group().replaceFirst("Z", "");
                }
                
                matcher.appendReplacement(buffer, replacement);
            }
        }
        matcher.appendTail(buffer);
        p.setValue(buffer.toString());
    }
    
    private void removeTime(com.funambol.common.pim.model.model.Property p)
    throws ConverterException {
        
        Pattern pattern = Pattern.compile(DATE_OR_DATE_TIME_REGEX);
        Matcher matcher = pattern.matcher(p.getValue());
        StringBuffer buffer = new StringBuffer();
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String replacement = matcher.group(1) // only the date
                                        .replaceAll("-", ""); // no dashes
            if ((matcher.group(4) != null) && ("DTEND".equals(p.getName()))) {
                replacement = TimeUtils.rollOneDay(replacement, 
                                                   true       ); // roll on
            }
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        
        p.setValue(buffer.toString());
        
        // iCalendar all-day properties must specify VALUE=DATE:
        if (found) {
            p.setParameter(new Parameter("VALUE", "DATE"));
        }
    }
    
    private boolean hasNoTimeZone(PropertyWithTimeZone property) {
        if (property == null) {
            return true;
        }
        if (property.getTimeZone() == null) {
            return true;
        }
        return false;
    }
}
