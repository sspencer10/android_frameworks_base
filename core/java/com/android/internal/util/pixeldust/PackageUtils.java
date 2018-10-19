/*
* Copyright (C) 2014 The OmniROM Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.internal.util.pixeldust;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.util.List;

public class PackageUtils {

    private static final String TAG = "PackageUtils";

    public static boolean isAppInstalled(Context context, String appUri) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(appUri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAvailableApp(String packageName, Context context) {
        Context mContext = context;
        final PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            int enabled = pm.getApplicationEnabledSetting(packageName);
            return enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

   /**
     * Checks if a specific package is installed.
     *
     * @param context     The context to retrieve the package manager
     * @param packageName The name of the package
     * @return Whether the package is installed or not.
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            if (pm != null) {
                List<ApplicationInfo> packages = pm.getInstalledApplications(0);
                for (ApplicationInfo packageInfo : packages) {
                    if (packageInfo.packageName.equals(packageName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
        return false;
    }

    public static boolean isPackageEnabled(String packageName, PackageManager pm) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException notFound) {
            return false;
        }
    }

    /**
     * Checks if a specific service is running.
     *
     * @param context     The context to retrieve the activity manager
     * @param serviceName The name of the service
     * @return Whether the service is running or not
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (services != null) {
            for (ActivityManager.RunningServiceInfo info : services) {
                if (info.service != null) {
                    if (info.service.getClassName() != null && info.service.getClassName()
                            .equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
