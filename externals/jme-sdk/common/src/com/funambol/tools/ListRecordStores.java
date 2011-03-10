/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.funambol.tools;

import com.funambol.storage.AbstractRecordStore;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;




public class ListRecordStores extends MIDlet implements CommandListener {

    private RecordStoreList list;
    
    private StoreContentList scl;
    
    class RecordStoreList extends List {
        
        Command exitCommand;
        Command viewCommand;
         
        public RecordStoreList() {
            super("Stores", List.IMPLICIT);
            String[] store = getStoreNames();
            for (int i =0; i< store.length; i++) {
                this.append(store[i], null);
            }
            
            this.addCommand(getExitCommand());
            this.addCommand(getViewCommand());
        }
        
        public Command getExitCommand() {
            if (exitCommand == null) {
                exitCommand = new Command("Exit", Command.EXIT, 0);
            }
            return exitCommand;
        }
        
        private String[] getStoreNames() {
            return AbstractRecordStore.listRecordStores();
        }

        public Command getViewCommand() {
            if (viewCommand==null) {
                viewCommand = new Command("View", Command.ITEM, 0);
            }
            return viewCommand;
        }
    }
 
   protected void startApp() throws MIDletStateChangeException  { 
        list = new RecordStoreList();
        list.setCommandListener(this);
        scl = new StoreContentList("");
        Display.getDisplay(this).setCurrent(list);
        
    }

    protected void pauseApp() {
    
    }

    protected void destroyApp(boolean arg0)  {
    
    }
    
    /**
     * This method exit the midlet.
     */
    public void exitMIDlet() {
        Display.getDisplay(this).setCurrent(null);
        destroyApp(true);
        notifyDestroyed();
    }
   
    public void commandAction(Command c, Displayable arg1) {
        if (c == list.getExitCommand()) {
            exitMIDlet();   
        } else if (c==list.getViewCommand()) {
            int index = list.getSelectedIndex();
            String storeName = list.getString(index);
            scl = new StoreContentList(storeName);
            scl.showStoreContent(storeName);
            scl.setCommandListener(this);
            Display.getDisplay(this).setCurrent(scl);
            
        } else if (c==scl.getBackCommand()) {
            scl=null;
            Display.getDisplay(this).setCurrent(list);
        }
    }
    
    class StoreContentList extends List {
        Command backCommand;

        
        public StoreContentList(String name) {
            super(name + " content:", List.IMPLICIT);
            this.addCommand(getBackCommand());
        }

        public void showStoreContent(String name){
            if (!name.equals("")) {
                try {
                    this.addCommand(getBackCommand());
                    System.out.println("Opening ar");
                    AbstractRecordStore ar = AbstractRecordStore.openRecordStore(name, false);
                    System.out.println("Getting re");
                    RecordEnumeration re = ar.enumerateRecords(null, null, true);
                    
                    this.setTitle(name + " " + ar.getNumRecords());
                
                    int recordId=0;
                    if (re==null) {
                        System.out.println("re is null");
                        return;
                    }
                    if (ar.getNumRecords()==0) {
                        System.out.println("ar is empty");
                        return;
                    }
                    while(re!=null&&re.hasNextElement()) {
                        recordId = re.nextRecordId();
                        byte[] record = ar.getRecord(recordId);
                        
                        if (record!=null) {
                            scl.append("ID: " + recordId + " Length: " + record.length, null);
                        } else {
                            scl.append("ID: " + recordId + " Null Record", null);
                        }
                    }
                    System.out.println("Closing ar");
                    ar.closeRecordStore();
                } catch (RecordStoreException ex) {
                    ex.printStackTrace();
                }
            }
        }
        public Command getBackCommand() {
            if (backCommand == null) {
                    backCommand = new Command("Back", Command.BACK, 0);
                }
            return backCommand;
        }
    }
    
}

 
