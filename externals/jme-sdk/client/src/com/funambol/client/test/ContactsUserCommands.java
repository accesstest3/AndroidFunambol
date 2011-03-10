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

/**
 * This component lists all the contacts-related commands available in the automatic test
 * scripting language.
 */
public interface ContactsUserCommands {

    /**
     * This command can used to simulate a contact addition. It creates an empty
     * contact in memory which will be saved as soon as the SaveContact command 
     * is called.
     * Once this command is called you shall set the contact's FirstName and
     * LastName fields via the SetContactField command before saving it.
     *
     * @example CreateEmptyContact()
     */
    public static final String CREATE_EMPTY_CONTACT_COMMAND = "CreateEmptyContact";

    /**
     * This command can be used to simulate a contact update. It loads an existing
     * contact identified by the given FirstName and LastName fields. If such a
     * contact is not found, then the test fails.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     *
     * @example LoadContact("Foo", "Bar")
     */
    public static final String LOAD_CONTACT_COMMAND = "LoadContact";

    /**
     * This command can used while simulating a contact additon or update. It
     * sets the given field to the given value.
     *
     * @param fieldName is the contact's field name to edit. It can take one of
     * the following values:
     * <ul>
     *  <li>DisplayName</li>
     *  <li>FirstName</li>
     *  <li>LastName</li>
     *  <li>MiddleName</li>
     *  <li>PrefixName</li>
     *  <li>SuffixName</li>
     *  <li>NickName</li>
     *  <li>TelHome</li>
     *  <li>TelWork</li>
     *  <li>TelOther</li>
     *  <li>TelOther2</li>
     *  <li>TelCell</li>
     *  <li>TelPager</li>
     *  <li>TelFaxHome</li>
     *  <li>TelFaxWork</li>
     *  <li>TelCompany</li>
     *  <li>TelOtherFax</li>
     *  <li>EmailHome</li>
     *  <li>EmailWork</li>
     *  <li>EmailOther</li>
     *  <li>Im</li>
     *  <li>AddressOther: formatted as post-office;ext-address;street;city;
     *  state;cap;country</li>
     *  <li>AddressHome: formatted as post-office;ext-address;street;city;
     *  state;cap;country</li>
     *  <li>AddressWork: formatted as post-office;ext-address;street;city;
     *  state;cap;country</li>
     *  <li>Website</li>
     *  <li>WebsiteHome</li>
     *  <li>WebsiteWork</li>
     *  <li>Birthday: formatted as yyyymmdd yyyy-mm-dd or yyyy/mm/dd</li>
     *  <li>Anniversary: formatted as yyyymmdd yyyy-mm-dd or yyyy/mm/dd</li>
     *  <li>Children</li>
     *  <li>Spouse</li>
     *  <li>Title</li>
     *  <li>Organization: formatted as company;department</li>
     *  <li>Note</li>
     *  <li>Photo</li>
     * </ul>
     * @param value is field value to set
     *
     * @example SetContactField("FirstName", "Foo")
     */
    public static final String SET_CONTACT_FIELD_COMMAND = "SetContactField";

    /**
     * This command can be used to simulate a contact update. It empties the value
     * of the specified field. See SetContactField to see the available fields.
     *
     * @param fieldName is the contact's field name to empty.
     *
     * @example EmptyContactField("FirstName")
     */
    public static final String EMPTY_CONTACT_FIELD_COMMAND = "EmptyContactField";

    /**
     * This command can be used to simulate a contact addition or update. It saves
     * the contact actually created or loaded through the CreateEmptyContact
     * and LoadContact respectively.
     *
     * @example SaveContact()
     */
    public static final String SAVE_CONTACT_COMMAND = "SaveContact";

    /**
     * This command can be used to simulate a contact deletion. It removes from the
     * device store the contact identified by the given firstname and lastname.
     * If a contact with the given first name and last name is not found, then
     * the test fails.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     *
     * @example DeleteContact("Foo", "Bar")
     */
    public static final String DELETE_CONTACT_COMMAND = "DeleteContact";

    /**
     * This command can be used to simulate the deletion of all the contacts stored
     * in the device.
     *
     * @example DeleteAllContacts()
     */
    public static final String DELETE_ALL_CONTACTS_COMMAND = "DeleteAllContacts";

    /**
     * This command can be used to check that a new contact created on the server
     * has been correctly received by the client and has the same content of the
     * server's contact as expected.
     *
     * Remember to run RefreshServer before running check commands.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     * @param checkContent set as true if you want to check the item content.
     *
     * @example CheckNewContact("Foo", "Bar", true)
     */
    public static final String CHECK_NEW_CONTACT_COMMAND = "CheckNewContact";

    /**
     * This command can be used to check that an updated contact on the server has
     * been correctly received by the client and has the same content of the
     * server's contact as expected.
     *
     * Remember to run RefreshServer before running check commands.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     * @param checkContent set as true if you want to check the item content.
     *
     * @example CheckUpdatedContact("Foo", "Bar", true)
     */
    public static final String CHECK_UPDATED_CONTACT_COMMAND = "CheckUpdatedContact";

    /**
     * This command can be used to check that a deleted contact on the server has
     * been correctly deleted in the client.
     *
     * Remember to run RefreshServer before running check commands.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     *
     * @example CheckDeletedContact("Foo", "Bar")
     */
    public static final String CHECK_DELETED_CONTACT_COMMAND = "CheckDeletedContact";

    /**
     * This command can be used to check that a new contact sent to the server has
     * been correctly received and has the same content of the device's contact
     * as expected.
     *
     * Remember to run RefreshServer before running check commands.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     * @param checkContent set as true if you want to check the item content.
     *
     * @example CheckNewContactOnServer("Foo", "Bar", true)
     */
    public static final String CHECK_NEW_CONTACT_ON_SERVER_COMMAND = "CheckNewContactOnServer";

    /**
     * This command can used to check that an updated contact sent to the server
     * has been correctly received and has the same content of the device's
     * contact as expected.
     *
     * Remember to run RefreshServer before running check commands.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     * @param checkContent set as true if you want to check the item content.
     *
     * @example CheckUpdatedContactOnServer("Foo", "Bar", true)
     */
    public static final String CHECK_UPDATED_CONTACT_ON_SERVER_COMMAND = "CheckUpdatedContactOnServer";

    /**
     * This command can be used to check that a deleted contact sent to the server
     * has been correctly deleted by the server.
     *
     * Remember to run RefreshServer before running check commands.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     *
     * @example CheckDeletedContactOnServer("Foo", "Bar")
     */
    public static final String CHECK_DELETED_CONTACT_ON_SERVER_COMMAND = "CheckDeletedContactsOnServer";

    /**
     * This command can be used to simulate a contact addition on the server.
     * It creates an empty contact in memory which will be saved as soon as the
     * SaveContactOnServer command is called.
     * Once this command is called you shall set the contact's FirstName and
     * LastName fields via the SetContactField command before saving it.
     *
     * @example CreateEmptyContactOnServer()
     */
    public static final String CREATE_EMPTY_CONTACT_ON_SERVER_COMMAND = "CreateEmptyContactOnServer";

    /**
     * This command can be used to simulate a contact update on the server. It
     * loads an existing contact identified by the given FirstName and LastName
     * fields. If such a contact does not exist on server, then the test fails.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     *
     * @example LoadContactOnServer("Foo", "Bar")
     */
    public static final String LOAD_CONTACT_ON_SERVER_COMMAND = "LoadContactOnServer";

    /**
     * This command can be used to simulate a contact addition or update on the 
     * server. It saves the contact actually created or loaded through the
     * CreateEmptyContactOnServer and LoadContactOnServer respectively.
     *
     * @example SaveContactOnServer()
     */
    public static final String SAVE_CONTACT_ON_SERVER_COMMAND = "SaveContactOnServer";

    /**
     * This command can be used to simulate a contact deletion on the server. It
     * removes from the server the contact identified by the given firstname and
     * lastname. If such a contact does not exist on server, then the test
     * fails.
     *
     * @param firstName is the contact firstname
     * @param lastName is the contact lastname
     *
     * @example DeleteContactOnServer("Foo", "Bar")
     */
    public static final String DELETE_CONTACT_ON_SERVER_COMMAND = "DeleteContactOnServer";

    /**
     * This command can used to simulate the deletion of all the contacts stored
     * in the server.
     *
     * @example DeleteAllContactsOnServer()
     */
    public static final String DELETE_ALL_CONTACTS_ON_SERVER_COMMAND = "DeleteAllContactsOnServer";

    /**
     * This command allows to fill a contact with a VCard. This is useful when
     * the user needs to generate contacts on the server. It is possible to
     * create an empty contact, fill it with this command and save it.
     *
     * @param vcard is the contact representation as vcard
     *
     * @example CreateEmptyContactOnServer() <br>
     *          SetContactAsVCard("BEGIN:VCARD\r\nVERSION:2.1\r\nFN:Luca\r\nN:Bianchi;Luca;middle;prefix;suffix\r\nADR;HOME:post;via 1;street;Milano;Italy;24356;country\r\nEND:VCARD") <br>
     *          SaveContactOnServer()
     */
    public static final String SET_CONTACT_AS_VCARD_COMMAND = "SetContactAsVCard";

    /**
     * This command allows to import a vCard content form a file on the server.
     * Useful to generate a test set on the server for a particular test or to
     * automatically reproduce the server vCard test set for a bug. The file
     * must be compliant with the "\r\n" policy (can be edited from a standard
     * text editor using multiple lines ("return" key) in place of "\r\n" chars).
     * @param vCard is the file that contains the vCard representation of the contact
     *
     * @example CreateEmptyContactOnServer() <br>
     *          ImportContactOnServer(vCardStub.txt) <br>
     *          SaveContactOnServer()
     */
    public static final String IMPORT_CONTACT_ON_SERVER_COMMAND = "ImportContactOnServer";

    /**
     * This command resets contacts client/server side and guarantees client and
     * server are in sync.
     *
     * @example ResetContacts()
     */
    public static final String RESET_CONTACTS_COMMAND = "ResetContacts";

    /**
     * This command formats the current contact (current in the robot) and
     * compares it against the given VCard.
     *
     * @example CreateEmptyContactOnClient();<br>
     *          SetContactField("FirstName", "Mario");<br>
     *          SaveContactOnClient();<br>
     *          CheckContactAsVCard("BEGIN:VCARD\r\nNAME:Mario;Bianchi;;;;\r\nEND:VCARD\r\n");
     */
    public static final String CHECK_CONTACT_AS_VCARD = "CheckContactAsVCard";

    /**
     * This command simulates an item received from the server. The use of this
     * command is for local tests, where the behavior of the source must be
     * tested for incoming items. After this command is executed, there shall be
     * a new contact on the device. It is possible to load such a contact and
     * check for its correctness.
     *
     * @example SetContactFromServer("BEGIN:VCARD\r\n......END:VCARD\r\n");
     *          LoadContact("Foo", "Bar");
     *          CheckContactAsVCard("BEGIN:VCARD\r\n.....END:VCARD\r\n");
     *
     */
    public static final String SET_CONTACT_FROM_SERVER = "SetContactFromServer";


}
    

