/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.jsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;

import com.funambol.syncml.spds.SyncManager;
import com.funambol.syncml.spds.SyncConfig;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.DeviceConfig;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.client.CacheTracker;
import com.funambol.syncml.client.FileSyncSource;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValueMemoryStore;
import com.funambol.storage.StringKeyValueFileStore;
import com.funambol.platform.FileAdapter;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.util.ConsoleAppender;

public class JSync {

    private String args[];
    private String username  = null;
    private String password  = null;
    private String url       = null;
    private int    logLevel  = Log.DISABLED;
    private int    syncMode  = SyncML.ALERT_CODE_FAST;
    private String remoteUri = "briefcase";
    private boolean raw      = false;
    private int customMsgSize = 16*1024;
    private String customDeviceId = null;
    private String customUserAgent = null;
    private String sourceType = null;
    private String sourceEncoding = null;
    private String cmdOrder[] = null;
    private boolean md5 = false;

    public JSync(String args[]) {
        this.args = args;
    }

    public void run() {
        // Parse the arguments
        parseArgs();

        ConsoleAppender appender = new ConsoleAppender();
        Log.initLog(appender);
        Log.setLogLevel(logLevel);

        SyncConfig  config  = new SyncConfig();
        // Apply customized device config
        DeviceConfig dc = new DeviceConfig();
        if (customDeviceId != null) {
            dc.devID = customDeviceId;
        }
        if (customUserAgent != null) {
            config.userAgent = customUserAgent;
        }
        dc.setMaxMsgSize(customMsgSize);
        config.deviceConfig = dc;
        // Set credentials
        config.syncUrl  = url;
        config.userName = username;
        config.password = password;

        if (md5) {
            config.preferredAuthType = SyncML.AUTH_TYPE_MD5;
        }

        config.compress = false;
        SyncManager manager = new SyncManager(config);
        SourceConfig sc = new SourceConfig(SourceConfig.BRIEFCASE, SourceConfig.FILE_OBJECT_TYPE, remoteUri);

        // If a configuration exists, then load it
        try {
            FileAdapter ssConfig = new FileAdapter("briefcaseconfig.dat");
            if (ssConfig.exists()) {
                InputStream is = ssConfig.openInputStream();
                DataInputStream dis = new DataInputStream(is);
                sc.deserialize(dis);
                is.close();
                ssConfig.close();
            }
        } catch (IOException ioe) {
            System.err.println("Cannot load configuration");
        }

        // --raw is a utility flag when syncing files. It defines type and
        // encoding. Its effect can be overwritten by specifying type and
        // encoding
        if (!raw) {
            sc.setType(SourceConfig.FILE_OBJECT_TYPE);
            sc.setEncoding(SyncSource.ENCODING_NONE);
        } else {
            sc.setType(SourceConfig.BRIEFCASE_TYPE);
            sc.setEncoding(SyncSource.ENCODING_B64);
        }

        // Set custom source type and encoding
        if (sourceType != null) {
            sc.setType(sourceType);
        }
        if (sourceEncoding != null) {
            sc.setEncoding(sourceEncoding);
        }

        sc.setRemoteUri(remoteUri);

        StringKeyValueFileStore ts = new StringKeyValueFileStore("briefcasecache.txt");
        CacheTracker ct = new CacheTracker(ts);
        sc.setSyncMode(syncMode);
        FileSyncSource fss = new FileSyncSource(sc, ct, "./briefcase/");

        if (cmdOrder != null) {
            manager.setCmdProcessingOrder(cmdOrder);
        }

        try {
            manager.sync(fss);
            // Save the configuration
            FileAdapter ssConfig = new FileAdapter("briefcaseconfig.dat");
            OutputStream os = ssConfig.openOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            sc.serialize(dos);
            dos.close();
            ssConfig.close();
        } catch (Exception e) {
            Log.error(e.toString());
        }

    }

    public static void main(String args[]) {
        JSync jsync = new JSync(args);
        jsync.run();
    }

    private void parseLogLevel(String log) {
        if (log.equals("none")) {
            logLevel = Log.DISABLED;
        } else if (log.equals("info")) {
            logLevel = Log.INFO;
        } else if (log.equals("debug")) {
            logLevel = Log.DEBUG;
        } else if (log.equals("trace")) {
            logLevel = Log.TRACE;
        } else {
            System.err.println("Unknown log level " + log);
            usage();
            System.exit(1);
        }
    }

    private void parseSyncMode(String mode) {
        if (mode.equals("fast")) {
            syncMode = SyncML.ALERT_CODE_FAST;
        } else if (mode.equals("slow")) {
            syncMode = SyncML.ALERT_CODE_SLOW;
        } else if (mode.equals("refresh_from_server")) {
            syncMode = SyncML.ALERT_CODE_REFRESH_FROM_SERVER;
        } else if (mode.equals("refresh_from_client")) {
            syncMode = SyncML.ALERT_CODE_REFRESH_FROM_CLIENT;
        } else if (mode.equals("one_way_from_server")) {
            syncMode = SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER;
        } else if (mode.equals("one_way_from_client")) {
            syncMode = SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT;
        } else {
            System.err.println("Unknown sync mode " + mode);
            usage();
            System.exit(1);
        }
    }

    private String[] parseCmdOrder(String order) {
        order = order.trim();
        String c[] = StringUtil.split(order, ",");
        if (c.length != 3) {
            System.err.println("Invalid command order. Expected three values");
            usage();
            System.exit(1);
        }
        boolean add = false;
        boolean update = false;
        boolean delete = false;
        String res[] = new String[3];
        for(int i=0;i<c.length;++i) {
            c[i] = c[i].toLowerCase();
            if (c[i].equals("d")) {
                res[i] = SyncML.TAG_DELETE;
                delete = true;
            } else if (c[i].equals("u")) {
                res[i] = SyncML.TAG_REPLACE;
                update = true;
            } else if (c[i].equals("a")) {
                res[i] = SyncML.TAG_ADD;
                add = true;
            } else {
                System.err.println("Invalid command order. Unknown value: " + c[i]);
                usage();
                System.exit(1);
            }
        }
        if (!add || !update || !delete) {
            System.err.println("Invalid command order " + order);
            usage();
            System.exit(1);
        }
        return res;
    }


    private void parseArgs() {
        int i = 0;
        while(i<args.length) {
            String arg = args[i];
            if (arg.equals("--user")) {
                if (i+1 < args.length) {
                    username = args[++i];
                } else {
                    System.err.println("Missing username");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--pwd")) {
                if (i+1 < args.length) {
                    password = args[++i];
                } else {
                    System.err.println("Missing password");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--url")) {
                if (i+1 < args.length) {
                    url = args[++i];
                } else {
                    System.err.println("Missing url");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--log")) {
                if (i+1 < args.length) {
                    parseLogLevel(args[++i]);
                } else {
                    System.err.println("Missing log level");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--mode")) {
                if (i+1 < args.length) {
                    parseSyncMode(args[++i]);
                } else {
                    System.err.println("Missing sync mode");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--uri")) {
                if (i+1 < args.length) {
                    remoteUri = args[++i];
                } else {
                    System.err.println("Missing remote uri");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--raw")) {
                raw = true;
            } else if (arg.equals("--type")) {
                if (i+1 < args.length) {
                    sourceType = args[++i];
                } else {
                    System.err.println("Missing type");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--encoding")) {
                if (i+1 < args.length) {
                    sourceEncoding = args[++i];
                } else {
                    System.err.println("Missing encoding");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--devid")) {
                if (i+1 < args.length) {
                    customDeviceId = args[++i];
                } else {
                    System.err.println("Missing device id");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--ua")) {
                if (i+1 < args.length) {
                    customUserAgent = args[++i];
                } else {
                    System.err.println("Missing user agent");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--maxmsgsize")) {
                if (i+1 < args.length) {
                    customMsgSize = Integer.parseInt(args[++i]);
                } else {
                    System.err.println("Missing max msg size");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--cmdorder")) {
                if (i+1 < args.length) {
                    cmdOrder = parseCmdOrder(args[++i]);
                } else {
                    System.err.println("Missing cmd order");
                    usage();
                    System.exit(1);
                }
            } else if (arg.equals("--md5")) {
                md5 = true;
            } else {
                System.err.println("Invalid option: " + arg);
                usage();
                System.exit(1);
            }
            ++i;
        }

        if (username == null || password == null || url == null) {
            usage();
            System.exit(2);
        }
    }

    private void usage() {
        System.out.println("JSync options");
        StringBuffer options = new StringBuffer();
        
        options.append("--user <username>\n")
               .append("--pwd <password>\n")
               .append("--url <url>\n")
               .append("--log trace|debug|info|error|none\n")
               .append("--mode fast|slow|refresh_from_client|refresh_from_server|one_way_from_client|one_way_from_server\n")
               .append("--uri remote_uri\n")
               .append("--raw\n")
               .append("--type source_mime_type\n")
               .append("--encoding none|b64\n")
               .append("--cmdorder value where value is a combination of D,A,U. Example: D,U,A\n")
               .append("--md5 tries md5 authentication first")
               .append("--devid device id")
               .append("--ua user agent");

        System.out.println(options);
    }


}

