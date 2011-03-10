/**
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

package com.funambol.push;

import java.util.Vector;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Date;

import com.funambol.util.Log;

/**
 * This class queues sync requests and perform basic requests merging that can
 * be used for client to server push.
 * The class provides the ability to manage a queue of requests and merge
 * requests that are "equivalent", giving the user the ability to implement its
 * own equivalence concept.
 * 
 * A request is described by a SyncRequest which is a general interface that
 * clients must implement. Each request is stored in the queue with an
 * associated time at which the request shall be served. When a new request
 * comes in, the scheduler checks if the this requests "contains" other requests
 * already in the queue. In such a case the requests are combined. When requests
 * are combined, the scheduler removes what was already in the queue, and
 * store the new request in the proper position (depending only on its
 * interval).
 *
 */
public class SyncScheduler {

    private Vector requestQueue;
    private SyncSchedulerListener ssListener;
    private Timer scheduleTimer = null;
    private RequestTimer rq = null;

    /**
     * Construct a scheduler with the given listener.
     *
     * @param ssListener the listener. This cannot be null otherwise NPE
     * exceptions will be generated when trying to fire a sync
     */
    public SyncScheduler(SyncSchedulerListener ssListener) {
        this.ssListener = ssListener;
        requestQueue = new Vector();
    }
    
    /**
     * Add a sync request to the SyncScheduler.
     * @param syncRequest object to be added
     *
     */
    public void addRequest(SyncRequest syncRequest){
        Log.trace("[SyncScheduler.addRequest]");
        if (syncRequest.getInterval() == 0){
            doSync(syncRequest);
        }else{
            SchedulerRequest sr = new SchedulerRequest(syncRequest);
            checkQueue(sr);
        }
    }
                                                 
    
    /**
     * Set a Listener to the SyncScheduler.
     * @param listenet to be added
     */
    public void setListener(SyncSchedulerListener ssListener){
        this.ssListener = ssListener;
    }

  

    /**
     * Manage the request queue, adding, replacing or merge the new request
     * @param syncRequest
     */
    private void checkQueue(SchedulerRequest schedulReq){
        Log.trace("[SyncScheduler.checkQueue]");
        int queueSize = requestQueue.size();
        Log.trace("[SyncScheduler.checkQueue] requests in queue: " + queueSize);
        boolean setTimer = false;
        boolean requestCombined = false;
        boolean insertedRequest = false;

        if(queueSize > 0){
            // Check if it's possible merge or replace a request, putting the new return request in the
            // ordered position         
           for (int i=0; i< queueSize; i++){
                SchedulerRequest srCombined = schedulReq.combine((SchedulerRequest)requestQueue.elementAt(i));
                if(srCombined != null){
                    Log.info("[SyncScheduler] Request was merged");
                    requestCombined = true;
                    requestQueue.removeElementAt(i);

                    if(!requestQueue.isEmpty()){
                        queueSize = requestQueue.size();
                        for (int pos = 0; pos<queueSize; pos++) {
                            long expTime = ((SchedulerRequest)requestQueue.elementAt(pos)).getExpirationTime();
                            if (srCombined.getExpirationTime() < expTime) {
                                Log.trace("[SyncScheduler] putting combined request in "+pos+"position");
                                requestQueue.insertElementAt(srCombined, pos);
                                insertedRequest = true;
                                if(pos==0){
                                    setTimer = true;
                                }
                                break;
                            }else if (pos == queueSize-1) {
                                Log.trace("[SyncScheduler] putting new request in last position");
                                requestQueue.addElement(schedulReq);
                                insertedRequest = true;
                            }
                        }
                    } else {
                        Log.trace("[SyncScheduler] the queue is empty... adding the combined request");
                        requestQueue.addElement(srCombined);
                        setTimer = true;
                        insertedRequest = true;
                    }
                }

                if (insertedRequest) {
                    break;
                }
            }

            // It's not possible to merge o replace the request than put
            // the request on the rigth position
            if (!requestCombined) {
                Log.info("[SyncScheduler] Request cannot be merged, enqueue a new one");
                for (int i=0; i< queueSize; i++) {
                    long expTime = ((SchedulerRequest)requestQueue.elementAt(i)).getExpirationTime();
                    if (schedulReq.getExpirationTime() < expTime) {
                        Log.trace("[SyncScheduler] putting new request in "+i+" position");
                        requestQueue.insertElementAt(schedulReq, i);
                        if (i==0) {
                            setTimer = true;
                        }
                        break;
                    } else if(i == queueSize-1) {
                        Log.trace("[SyncScheduler] putting new request in last position");
                        requestQueue.addElement(schedulReq);
                    }
                }
            }
        } else {
            Log.debug("[SyncScheduler] first element into the queue");
            requestQueue.addElement(schedulReq);
            setTimer = true;
        }

        if (setTimer) {
            Log.info("[SyncScheduler] needs to set a timer");
            setTimer(((SchedulerRequest)requestQueue.elementAt(0)).getExpirationTime());
        }
    }


    private void resetQueue(){
        requestQueue.removeAllElements();

    }

    /**
     * This method is invoked when syncRequest is requested immediately
     * (interval=0). This request cleans all other equivalent requests in queue
     * (aka requests that are can be merged with this one)
     */
    private void doSync(SyncRequest syncRequest) {
        Log.trace("[SyncScheduler] doSync");
        //Check if it's present the request in the queue to remove it
        if(requestQueue.size() > 0) {
            for (int i=0; i< requestQueue.size(); i++) {
                SyncRequest queuedReq = ((SchedulerRequest)requestQueue.elementAt(i)).getSyncRequest();
                SyncRequest merged = syncRequest.merge(queuedReq);
                if (merged != null) {

                    Log.trace("[SyncScheduler] removing old request");
                    SchedulerRequest or = (SchedulerRequest)requestQueue.elementAt(i);
                    Log.trace("was scheduled at: " + new Date(or.getExpirationTime()));

                    requestQueue.removeElementAt(i);
                    if(i==0) {
                        if(rq != null) {
                            rq.cancel();
                            scheduleTimer.cancel();
                        }
                    }
                    // What we sync is the merged version
                    syncRequest = merged;
                }
            }
        }

        if(scheduleTimer != null){
            scheduleTimer.cancel();
        }
        callListener(syncRequest);
    }

    private void doScheludedSync(){
        Log.trace("[SyncScheduler] doScheludedSync");
        SyncRequest requestToSync = ((SchedulerRequest)requestQueue.elementAt(0)).getSyncRequest();
        requestQueue.removeElementAt(0);

        callListener(requestToSync);
    }

    private void setTimer(long startTime){
        if(rq!=null){
            Log.trace("[SyncScheduler] Cancelling previous timer");
            rq.cancel();
            scheduleTimer.cancel();
        }

        scheduleTimer = new Timer();
        rq = new RequestTimer();
        Log.debug("[SyncScheduler] timer set at: "+new Date(startTime));
        try{
            scheduleTimer.schedule(rq, new Date(startTime));
        }catch(Exception ex){
            Log.error("SyncScheduler - ex scheduling timer: "+ex.getMessage());
        }
    }

    private void callListener(SyncRequest syncRequest){
        //restart timer if in the queue is present an other request
        if(!requestQueue.isEmpty()){
            long nextTimer = ((SchedulerRequest)requestQueue.elementAt(0)).getExpirationTime();
            Log.debug("[SyncScheduler] starting sync... reschedule timer for next request at: " + 
                      new Date(nextTimer));
            setTimer(nextTimer);
        }

        Log.debug("[SyncScheduler] starting sync at : "+new Date(System.currentTimeMillis()));
        ssListener.sync(syncRequest.getRequestContent());
    }

    //------------------------ SchedulerRequest ---------------------------------//
    // used to incapsule the request with the expiration time

    private class SchedulerRequest{

        private SyncRequest mySyncRequest;
        private long expirationTime;

        public SchedulerRequest(SyncRequest synReq){
            mySyncRequest = synReq;
            setExpTime(mySyncRequest.getInterval());
        }

        private void setExpTime(long expTime){
            expirationTime = System.currentTimeMillis() + expTime;
        }

        long getExpirationTime(){
            return expirationTime;
        }
        
        SyncRequest getSyncRequest(){
            return mySyncRequest;
        }

        void setSyncRequest(SyncRequest syncRequest){
            mySyncRequest = syncRequest;
        }

        /**
         * Check if it is possible to merge or to replace the new request with
         * the existing one
         *
         * @param queuedRequest
         * @return SchedulerRequest if it's possible to combine two or more
         *          requests, or null in it's not possible.
         */
        SchedulerRequest combine(SchedulerRequest queuedRequest) {

            SyncRequest sr = queuedRequest.getSyncRequest();
            //mySyncRequest is the new request
            SyncRequest merged = mySyncRequest.merge(sr);
            if (merged != null) {
                Log.trace("[SyncScheduler] Requests can be combined and scheduled at: "
                          + new Date(System.currentTimeMillis() + mySyncRequest.getInterval()));
                queuedRequest.setExpTime(mySyncRequest.getInterval());
                queuedRequest.setSyncRequest(merged);

                return queuedRequest;
            }
            return null;
        }
    }

    //------------------------ RequestTimer ---------------------------------//

    private class RequestTimer extends TimerTask{

        RequestTimer() {
        }

        public void run() {
            doScheludedSync();
        }
    }
}

