/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.phone.testapps.embmsfrontend;

import android.net.Uri;
import android.telephony.MbmsStreamingManager;
import android.telephony.mbms.MbmsException;
import android.telephony.mbms.StreamingService;
import android.telephony.mbms.StreamingServiceCallback;
import android.telephony.mbms.StreamingServiceInfo;
import android.widget.Toast;

public class StreamingServiceTracker {
    private class Callback extends StreamingServiceCallback {
        @Override
        public void error(int errorCode, String message) {
            String toastMessage = "Error: " + errorCode + ": " + message;
            mActivity.runOnUiThread(() ->
                    Toast.makeText(mActivity, toastMessage, Toast.LENGTH_SHORT).show());
        }

        @Override
        public void streamStateChanged(int state) {
            onStreamStateChanged(state);
        }
    }

    private final EmbmsTestStreamingApp mActivity;
    private final StreamingServiceInfo mStreamingServiceInfo;
    private StreamingService mStreamingService;
    private int mState;

    public StreamingServiceTracker(EmbmsTestStreamingApp appActivity, StreamingServiceInfo info) {
        mActivity = appActivity;
        mStreamingServiceInfo = info;
    }

    public void startStreaming(MbmsStreamingManager streamingManager) {
        try {
            mStreamingService =
                    streamingManager.startStreaming(mStreamingServiceInfo, new Callback());
        } catch (MbmsException e) {
            Toast.makeText(mActivity,
                    "Error starting streaming" + e.getErrorCode(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void dispose() {
        try {
            mStreamingService.dispose();
        } catch (MbmsException e) {
            Toast.makeText(mActivity,
                    "Error disposing stream" + e.getErrorCode(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public String getServiceId() {
        return mStreamingServiceInfo.getServiceId();
    }

    private void onStreamStateChanged(int state) {
        String toastMessage = "Stream "
                + mStreamingServiceInfo.getNames().get(mStreamingServiceInfo.getLocale())
                + " has entered state "
                + state;
        mActivity.runOnUiThread(() ->
                Toast.makeText(mActivity, toastMessage, Toast.LENGTH_SHORT).show());
        if (state == StreamingService.STATE_STARTED && mState != StreamingService.STATE_STARTED) {
            try {
                Uri streamingUri = mStreamingService.getPlaybackUri();
                mActivity.updateUriInUi(streamingUri);
            } catch (MbmsException e) {
                String errorToast = "Got error " + e.getErrorCode() + " while getting uri";
                mActivity.runOnUiThread(() ->
                        Toast.makeText(mActivity, errorToast, Toast.LENGTH_SHORT).show());
            }
        }
        mState = state;
    }
}
