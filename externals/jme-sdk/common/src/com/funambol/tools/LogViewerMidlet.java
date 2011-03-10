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

package com.funambol.tools;

import com.funambol.util.*;
import com.funambol.util.LogViewer;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

public class LogViewerMidlet extends MIDlet implements CommandListener {
    //------------------------------------------------------------- Private Data
    private Form logRecordList;
    private Command exit;
    private String[] result;
    
    //------------------------------------------------------------- Constructors
    /** Creates a new instance of VisualMidlet */
    public LogViewerMidlet() {
        getDisplay().setCurrent(getLogRecordList());
    }
    
    //----------------------------------------------------------- Public Methods
    /**
     * This method should return an instance of the display.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }
    
    /**
     * This method exit the midlet.
     */
    public void exitMIDlet() {
        getDisplay().setCurrent(null);
        destroyApp(true);
        notifyDestroyed();
    }
    
    /**
     * This method returns instance for logRecordList and must
     * be called instead of accessing logRecordList field directly.
     *
     * @return Instance for logRecordList component
     */
    public Form getLogRecordList() {
        LogViewer lv = new LogViewer();
        result = lv.getLogEntries(lv.RMSLOG);
        
        if (logRecordList == null) {
            logRecordList = new Form("LOG");
            logRecordList.addCommand(getExitCommand());
            logRecordList.setCommandListener(this);
            //logRecordList.setSelectedFlags(new boolean[0]);
        }
        
        for (int i=0; i<result.length; i++) {
            logRecordList.append(new String(result[i]));
        }
        return logRecordList;
    }
    
    /**
     * This method returns instance for exit component and must be called 
     * instead of accessing exit field directly.
     *
     * @return Instance for exit component
     */
    public Command getExitCommand() {
        if (exit == null) {
            exit = new Command("Exit", Command.EXIT, 1);
        }
        return exit;
    }
    
    /** Called by the system to indicate that a command has been invoked
     * on a particular displayable.
     * @param command the Command that ws invoked
     * @param displayable the Displayable on which the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command == exit) {
            exitMIDlet();
        }
    }
    
    /**
     * Life Cicle Related Midlet's method.
     */
    public void startApp() {
    }
    
    /**
     * Life Cicle Related Midlet's method.
     */
    public void pauseApp() {
    }
    
    /**
     * Life Cicle Related Midlet's method.
     */
    public void destroyApp(boolean unconditional) {
    }
}
