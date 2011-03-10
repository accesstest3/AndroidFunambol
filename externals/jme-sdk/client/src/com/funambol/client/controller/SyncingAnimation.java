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
package com.funambol.client.controller;

import com.funambol.client.ui.Bitmap;

public class SyncingAnimation {

    private int currentFrame;

    private Thread anim;

    private Bitmap bitmaps[];

    private boolean resetAtTheEnd = true;

    public SyncingAnimation(Bitmap[] bitmaps) {
        this.bitmaps = bitmaps;
    }

    public SyncingAnimation(boolean resetAtTheEnd) {
        this.resetAtTheEnd = resetAtTheEnd;
    }

    public void setAnimationIcons(Bitmap[] icons) {
        this.bitmaps = icons;
    }

    /**
     * Begin the animation for a sync in progress
     */
    public void startAnimation() {
        stopAnimation();
        if (anim == null) {
            anim = new Thread() {
                public void run() {
                    SyncingAnimation.this.runAnimation();
                }
            };
        }
        anim.start();
    }

    /**
     * Stop the animation for a sync in progress
     */
    public void stopAnimation() {
        if (anim != null && anim.isAlive()) {
            anim.interrupt();
            try {
                anim.join();
            } catch (InterruptedException e) { }
            anim = null;
        }
    }

    /**
     * @return true if the animation is currently running
     */
    public boolean isRunning() {
        return (anim != null && anim.isAlive());
    }

    protected void showBitmap(Bitmap bitmap) {
    }

    /**
     * Thread.run function for the animation
     */
    private void runAnimation() {

        if (bitmaps == null) {
            return;
        }
        try {
            while (true) {
                // While syncing a source is always selected, so we need to
                // set the selected icon in this case
                showBitmap(bitmaps[currentFrame]);
                int newFrame = (currentFrame + 1) % (bitmaps.length);
                currentFrame = newFrame;
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            if (resetAtTheEnd) {
                showBitmap(bitmaps[0]);
                currentFrame = 0;
            }
            return;
        }
    }
}

