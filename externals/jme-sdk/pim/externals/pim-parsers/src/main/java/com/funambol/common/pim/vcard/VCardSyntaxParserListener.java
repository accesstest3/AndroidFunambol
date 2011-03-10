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

package com.funambol.common.pim.vcard;

import com.funambol.common.pim.ParamList;

public interface VCardSyntaxParserListener {

    public void start();

    public void end();

    public void setCategories(String content, ParamList plist,
                              Token group) throws ParseException;
    public void addExtension(String tagName, String content, ParamList plist,
                             Token group) throws ParseException;
    public void setVersion(String ver, ParamList plist,
                           Token group) throws ParseException;
    public void setTitle(String content, ParamList plist,
                         Token group) throws ParseException;
    public void setMail(String content, ParamList plist,
                        Token group) throws ParseException;
    public void setUrl(String content, ParamList plist,
                       Token group) throws ParseException;
    public void setTelephone(String content, ParamList plist,
                             Token group) throws ParseException;
    public void setFName(String content, ParamList plist,
                         Token group) throws ParseException;
    public void setRole(String content, ParamList plist,
                        Token group) throws ParseException;
    public void setRevision(String content, ParamList plist,
                            Token group) throws ParseException;
    public void setNickname(String content, ParamList plist,
                            Token group) throws ParseException;
    public void setOrganization(String content, ParamList plist,
                                Token group) throws ParseException;
    public void setAddress(String content, ParamList plist,
                           Token group)throws ParseException;
    public void setBirthday(String content, ParamList plist,
                            Token group) throws ParseException;
    public void setLabel(String content, ParamList plist,
                         Token group) throws ParseException;
    public void setTimezone(String content, ParamList plist,
                            Token group) throws ParseException;
    public void setLogo(String content, ParamList plist,
                        Token group) throws ParseException;
    public void setNote(String content, ParamList plist,
                        Token group) throws ParseException;
    public void setUid(String content, ParamList plist,
                       Token group) throws ParseException;
    public void setPhoto(String content, ParamList plist,
                         Token group) throws ParseException;
    public void setName(String content, ParamList plist,
                        Token group) throws ParseException;
    public void setFolder(String content, ParamList plist,
                          Token group) throws ParseException;
    public void setFreebusy(String content, ParamList plist,
                            Token group) throws ParseException;
    public void setAnniversary(String content, ParamList plist,
                               Token group) throws ParseException;
    public void setChildren(String content, ParamList plist,
                            Token group) throws ParseException;
    public void setCompanies(String content, ParamList plist,
                             Token group) throws ParseException;
    public void setLanguages(String content, ParamList plist,
                             Token group) throws ParseException;
    public void setManager(String content, ParamList plist,
                           Token group) throws ParseException;
    public void setMileage(String content, ParamList plist,
                           Token group) throws ParseException;
    public void setSpouse(String content, ParamList plist,
                          Token group) throws ParseException;
    public void setSubject(String content, ParamList plist,
                           Token group) throws ParseException;
    public void setAccessClass(String content, ParamList plist,
                               Token group) throws ParseException;
    public void setGeo(String content, ParamList plist,
                       Token group) throws ParseException;
    public void setMailer(String content, ParamList plist,
                          Token group) throws ParseException;
    public void setIMPP(String content, ParamList plist,
                          Token group) throws ParseException;
    public void setProductID(String content, ParamList plist,
                       Token group) throws ParseException;
    public void setAgent(String content, ParamList plist,
                          Token group) throws ParseException;
    public void setKey(String content, ParamList plist,
                          Token group) throws ParseException;
}
