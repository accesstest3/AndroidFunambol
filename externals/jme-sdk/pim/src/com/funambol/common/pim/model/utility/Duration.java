/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
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

package com.funambol.common.pim.model.utility;

public class Duration {

    private final long FACTOR_MINUTE =  60;
    private final long FACTOR_HOUR   =  60*FACTOR_MINUTE;
    private final long FACTOR_DAY    =  24*FACTOR_HOUR;
    private final long FACTOR_WEEK   =   7*FACTOR_DAY;
    private final long FACTOR_MONTH  =  30*FACTOR_DAY;
    private final long FACTOR_YEAR   = 365*FACTOR_DAY;

    public int sign;

    public int seconds;
    public int minutes;
    public int hours;
    public int days;
    public int weeks;
    public int months;
    public int years;

    public long millis;

    public Duration() {
        sign = 1;
    }

    /**
     * Parse according to ISO8601
     *
     * @param str
     * @throws IllegalArgumentException
     */
    public void parse(String str) throws IllegalArgumentException {
        
        sign = 1;
        years = months = weeks = days = hours = minutes = seconds = 0;

        int len = str.length();
        int index = 0;
        char c;

        if (len < 1) {
            throw new IllegalArgumentException("Invalid duration: " + str);
        }

        c = str.charAt(0);
        if (c == '-') {
            sign = -1;
            index++;
        }
        else if (c == '+') {
            index++;
        }

        if (len <= index) {
            throw new IllegalArgumentException("Invalid duration: " + str);
        }

        c = str.charAt(index);
        if (c != 'P') {
            throw new IllegalArgumentException (
                    "Duration.parse(str='" + str + "') expected 'P' at index="
                    + index);
        }
        index++;

        int n = 0;
        boolean time = false;
        
        for (; index < len; index++) {
            c = str.charAt(index);
            if (c >= '0' && c <= '9') {
                n *= 10;
                n += ((c-'0'));
            } else if (c == 'Y') {
                years = n;   n = 0;
            } else if (c == 'W') {
                weeks = n;   n = 0;
            } else if (c == 'H') {
                hours = n;   n = 0;
            } else if (c == 'M') {
                if(time) {
                    minutes = n;
                } else {
                    months = n;
                }
                n = 0;
            } else if (c == 'S') {
                seconds = n; n = 0;
            } else if (c == 'D') {
                days = n;    n = 0;
            } else if (c == 'T') {
                time = true;
            } else {
                throw new IllegalArgumentException (
                        "Duration.parse(str='" + str + "') unexpected char '"
                        + c + "' at index=" + index);
            }
        }
        if (n > 0) {
            throw new IllegalArgumentException("Invalid duration: " + str);
        }
    }

    /**
     * Format according to ISO8601
     *
     * @return
     */
    public String format() {
        StringBuffer sb = new StringBuffer(10);
        if(sign == -1) {
            sb.append('-');
        }
        sb.append('P');
        if(years > 0) {
            sb.append(years).append('Y');
        }
        if(months > 0) {
            sb.append(months).append('M');
        }
        if(weeks > 0) {
            sb.append(weeks).append('W');
        }
        if(days > 0) {
            sb.append(days).append('D');
        }
        if((hours + minutes + seconds) > 0) {
            sb.append('T');
        }
        if(hours > 0) {
            sb.append(hours).append('H');
        }
        if(minutes > 0) {
            sb.append(minutes).append('M');
        }
        if(seconds > 0) {
            sb.append(seconds).append('S');
        }
        return sb.toString();
    }

    public long getMillis() {

        long factor = 1000 * sign;

        millis = factor * (FACTOR_YEAR*years
                + (FACTOR_MONTH*months)
                + (FACTOR_WEEK*weeks)
                + (FACTOR_DAY*days)
                + (FACTOR_HOUR*hours)
                + (FACTOR_MINUTE*minutes)
                + seconds);

        return millis;
    }

    public void setMillis(long millis) {
        
        this.millis = millis;

        sign = 1;
        years = months = weeks = days = hours = minutes = seconds = 0;

        if(millis < 0) {
            sign = -1;
        }

        long factor = 1000 * sign;
        millis /= factor;

        years = (int)(millis/FACTOR_YEAR);
        millis -= years*FACTOR_YEAR;
        
        months = (int)(millis/FACTOR_MONTH);
        millis -= months*FACTOR_MONTH;

        days = (int)(millis/FACTOR_DAY);
        millis -= days*FACTOR_DAY;
        
        hours = (int)(millis/FACTOR_HOUR);
        millis -= hours*FACTOR_HOUR;
        
        minutes = (int)(millis/FACTOR_MINUTE);
        millis -= minutes*FACTOR_MINUTE;
        
        seconds = (int)millis%(60);
    }
}
