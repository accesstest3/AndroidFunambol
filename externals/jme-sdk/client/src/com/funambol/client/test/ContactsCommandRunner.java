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

package com.funambol.client.test;

public class ContactsCommandRunner extends CommandRunner implements ContactsUserCommands {

    private static final String TAG_LOG = "ContactsCommandRunner";

    // Contact fields used by SetContactField and EmptyContactField commands
    public static final String CONTACT_FIELD_DISPLAY_NAME  = "DisplayName";
    public static final String CONTACT_FIELD_FIRST_NAME    = "FirstName";
    public static final String CONTACT_FIELD_LAST_NAME     = "LastName";
    public static final String CONTACT_FIELD_MIDDLE_NAME   = "MiddleName";
    public static final String CONTACT_FIELD_PREFIX_NAME   = "PrefixName";
    public static final String CONTACT_FIELD_SUFFIX_NAME   = "SuffixName";
    public static final String CONTACT_FIELD_NICK_NAME     = "NickName";
    public static final String CONTACT_FIELD_TEL_HOME      = "TelHome";
    public static final String CONTACT_FIELD_TEL_WORK      = "TelWork";
    public static final String CONTACT_FIELD_TEL_OTHER     = "TelOther";
    public static final String CONTACT_FIELD_TEL_OTHER2    = "TelOther2";
    public static final String CONTACT_FIELD_TEL_CELL      = "TelCell";
    public static final String CONTACT_FIELD_TEL_PAGER     = "TelPager";
    public static final String CONTACT_FIELD_TEL_FAX_HOME  = "TelFaxHome";
    public static final String CONTACT_FIELD_TEL_FAX_WORK  = "TelFaxWork";
    public static final String CONTACT_FIELD_TEL_COMPANY   = "TelCompany";
    public static final String CONTACT_FIELD_TEL_OTHER_FAX = "TelOtherFax";
    public static final String CONTACT_FIELD_EMAIL_HOME    = "EmailHome";
    public static final String CONTACT_FIELD_EMAIL_WORK    = "EmailWork";
    public static final String CONTACT_FIELD_EMAIL_OTHER   = "EmailOther";
    public static final String CONTACT_FIELD_EMAIL_IM      = "Im";
    public static final String CONTACT_FIELD_ADR_OTHER     = "AddressOther";
    public static final String CONTACT_FIELD_ADR_HOME      = "AddressHome";
    public static final String CONTACT_FIELD_ADR_WORK      = "AddressWork";
    public static final String CONTACT_FIELD_WEB           = "Website";
    public static final String CONTACT_FIELD_WEB_HOME      = "WebsiteHome";
    public static final String CONTACT_FIELD_WEB_WORK      = "WebsiteWork";
    public static final String CONTACT_FIELD_BDAY          = "Birthday";
    public static final String CONTACT_FIELD_ANNIVERSARY   = "Anniversary";
    public static final String CONTACT_FIELD_CHILDREN      = "Children";
    public static final String CONTACT_FIELD_SPOUSE        = "Spouse";
    public static final String CONTACT_FIELD_TITLE         = "Title";
    public static final String CONTACT_FIELD_ORGANIZATION  = "Organization";
    public static final String CONTACT_FIELD_NOTE          = "Note";
    public static final String CONTACT_FIELD_PHOTO         = "Photo";

    
    public ContactsCommandRunner(ContactsRobot robot) {
        super(robot);
    }

    public boolean runCommand(String command, String pars) throws Throwable {

        if (CREATE_EMPTY_CONTACT_COMMAND.equals(command)) {
            createEmptyContact(command, pars);
        } else if (LOAD_CONTACT_COMMAND.equals(command)) {
            loadContact(command, pars);
        } else if (SET_CONTACT_FIELD_COMMAND.equals(command)) {
            setContactField(command, pars);
        } else if (EMPTY_CONTACT_FIELD_COMMAND.equals(command)) {
            emptyContactField(command, pars);
        } else if (SAVE_CONTACT_COMMAND.equals(command)) {
            saveContact(command, pars);
        } else if (DELETE_CONTACT_COMMAND.equals(command)) {
            deleteContact(command, pars);
        } else if (DELETE_ALL_CONTACTS_COMMAND.equals(command)) {
            deleteAllContacts(command, pars);
        } else if (CHECK_NEW_CONTACT_COMMAND.equals(command)) {
            checkNewContact(command, pars);
        } else if (CHECK_UPDATED_CONTACT_COMMAND.equals(command)) {
            checkUpdatedContact(command, pars);
        } else if (CHECK_DELETED_CONTACT_COMMAND.equals(command)) {
            checkDeletedContact(command, pars);
        } else if (CHECK_NEW_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            checkNewContactOnServer(command, pars);
        } else if (CHECK_UPDATED_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            checkUpdatedContactOnServer(command, pars);
        } else if (CHECK_DELETED_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            checkDeletedContactOnServer(command, pars);
        } else if (CREATE_EMPTY_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            createEmptyContactOnServer(command, pars);
        } else if (LOAD_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            loadContactOnServer(command, pars);
        } else if (SAVE_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            saveContactOnServer(command, pars);
        } else if (DELETE_CONTACT_ON_SERVER_COMMAND.equals(command)) {
            deleteContactOnServer(command, pars);
        } else if (DELETE_ALL_CONTACTS_ON_SERVER_COMMAND.equals(command)) {
            deleteAllContactsOnServer(command, pars);
        } else if (SET_CONTACT_AS_VCARD_COMMAND.equals(command)){
            setContactAsVCard(command, pars);
        } else if (IMPORT_CONTACT_ON_SERVER_COMMAND.equals(command)){
            importContactOnServer(command, pars);
        } else if (RESET_CONTACTS_COMMAND.equals(command)) {
            resetContacts(command, pars);
        } else if (CHECK_CONTACT_AS_VCARD.equals(command)) {
            checkContactAsVCard(command, pars);
        } else if (SET_CONTACT_FROM_SERVER.equals(command)) {
            setContactFromServer(command, pars);
        } else {
            return false;
        }
        return true;
    }

    private ContactsRobot getContactsRobot() {
        return (ContactsRobot)robot;
    }

    private void importContactOnServer(String command, String args) throws Throwable {
        getContactsRobot().importContactOnServer(getParameter(args, 0));
    }

    private void createEmptyContact(String command, String args) throws Throwable {
        getContactsRobot().createEmptyContact();
    }

    private void createEmptyContactOnServer(String command, String args) throws Throwable {
        getContactsRobot().createEmptyContact();
    }

    private void setContactField(String command, String args) throws Throwable {

        String field = getParameter(args, 0);
        String value = getParameter(args, 1);

        checkArgument(field, "Missing field name in " + command);
        checkArgument(value, "Missing value in " + command);

        getContactsRobot().setContactField(field, value);
    }

    private void setContactAsVCard (String command, String args) throws Throwable {

        String VCard = getParameter(args, 0);

        checkArgument(VCard, "Missing field name in " + command);

        getContactsRobot().setContactAsVCard(VCard);
    }

    private void emptyContactField(String command, String args) throws Throwable {

        String field = getParameter(args, 0);

        checkArgument(field, "Missing field in " + command);

        String empty = "";
        if(field.startsWith("Address")) {
            empty = ";;;;;;";
        } else if(field.equals(CONTACT_FIELD_ORGANIZATION)) {
            empty = ";";
        }
        getContactsRobot().setContactField(field, empty);
    }

    private void loadContact(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);

        checkArgument(lastname, "Missing lastname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);

        getContactsRobot().loadContact(firstname, lastname);
    }

    private void saveContact(String command, String args) throws Throwable {
        getContactsRobot().saveContact();
    }

    private void deleteContact(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);

        getContactsRobot().deleteContact(firstname, lastname);
    }

    private void deleteAllContacts(String command, String args) throws Throwable {
        getContactsRobot().deleteAllContacts();
    }


    private void checkNewContact(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);
        String checkContent  = getParameter(args, 2);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().checkNewContact(firstname, lastname, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkUpdatedContact(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);
        String checkContent  = getParameter(args, 2);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().checkUpdatedContact(firstname, lastname, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkDeletedContact(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().checkDeletedContact(firstname, lastname, checkSyncClient);
    }


    private void checkNewContactOnServer(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);
        String checkContent  = getParameter(args, 2);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().checkNewContactOnServer(firstname, lastname, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkUpdatedContactOnServer(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);
        String checkContent  = getParameter(args, 2);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().checkUpdatedContactOnServer(firstname, lastname, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkDeletedContactOnServer(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().checkDeletedContactOnServer(firstname, lastname, checkSyncClient);
    }

    private void checkContactAsVCard(String command, String args) throws Throwable {

        String vcard     = getParameter(args, 0);

        checkArgument(vcard, "Missing vcard in " + command);

        getContactsRobot().checkContactAsVCard(vcard);
    }

    private void setContactFromServer(String command, String args) throws Throwable {

        String vcard     = getParameter(args, 0);

        checkArgument(vcard, "Missing vcard in " + command);

        getContactsRobot().setContactFromServer(vcard);
    }


    public void loadContactOnServer(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().loadContactOnServer(firstname, lastname, checkSyncClient);
    }

    public void saveContactOnServer(String command, String args) throws Throwable {

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().saveContactOnServer(checkSyncClient);
    }

    public void deleteContactOnServer(String command, String args) throws Throwable {

        String firstname = getParameter(args, 0);
        String lastname  = getParameter(args, 1);

        checkArgument(firstname, "Missing firstname in " + command);
        checkArgument(lastname, "Missing lastname in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().deleteContactOnServer(firstname, lastname, checkSyncClient);
    }

    public void deleteAllContactsOnServer(String command, String args) throws Throwable {
        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getContactsRobot().deleteAllContactsOnServer(checkSyncClient);
    }

    private void resetContacts(String command, String args) throws Throwable {
        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);
        getContactsRobot().resetContacts(checkSyncClient);
    }
}

