/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatteryTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.IntentTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.ColorMatrixTile;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Platform implementation of the quick settings tile host **/
public final class QSTileHost implements QSTile.Host, Tunable {
    private static final String TAG = "QSTileHost";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public static final String TILES_SETTING = "sysui_qs_tiles";

    private final Context mContext;
    private final PhoneStatusBar mStatusBar;
    private final LinkedHashMap<String, QSTile<?>> mTiles = new LinkedHashMap<>();
    protected final ArrayList<String> mTileSpecs = new ArrayList<>();
    private final BluetoothController mBluetooth;
    private final LocationController mLocation;
    private final RotationLockController mRotation;
    private final NetworkController mNetwork;
    private final ZenModeController mZen;
    private final HotspotController mHotspot;
    private final CastController mCast;
    private final Looper mLooper;
    private final FlashlightController mFlashlight;
    private final UserSwitcherController mUserSwitcherController;
    private final UserInfoController mUserInfoController;
    private final KeyguardMonitor mKeyguard;
    private final SecurityController mSecurity;
    private final BatteryController mBattery;
    private final StatusBarIconController mIconController;
    private final TileServices mServices;

    private final List<Callback> mCallbacks = new ArrayList<>();

    public QSTileHost(Context context, PhoneStatusBar statusBar,
                      BluetoothController bluetooth, LocationController location,
                      RotationLockController rotation, NetworkController network,
                      ZenModeController zen, HotspotController hotspot,
                      CastController cast, FlashlightController flashlight,
                      UserSwitcherController userSwitcher, UserInfoController userInfo,
                      KeyguardMonitor keyguard, SecurityController security,
                      BatteryController battery, StatusBarIconController iconController) {
        mContext = context;
        mStatusBar = statusBar;
        mBluetooth = bluetooth;
        mLocation = location;
        mRotation = rotation;
        mNetwork = network;
        mZen = zen;
        mHotspot = hotspot;
        mCast = cast;
        mFlashlight = flashlight;
        mUserSwitcherController = userSwitcher;
        mUserInfoController = userInfo;
        mKeyguard = keyguard;
        mSecurity = security;
        mBattery = battery;
        mIconController = iconController;

        final HandlerThread ht = new HandlerThread(QSTileHost.class.getSimpleName(),
                Process.THREAD_PRIORITY_BACKGROUND);
        ht.start();
        mLooper = ht.getLooper();

        mServices = new TileServices(this, mLooper);

        TunerService.get(mContext).addTunable(this, TILES_SETTING);
    }

    public PhoneStatusBar getPhoneStatusBar() {
        return mStatusBar;
    }

    public void destroy() {
        TunerService.get(mContext).removeTunable(this);
    }

    @Override
    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public Collection<QSTile<?>> getTiles() {
        return mTiles.values();
    }

    @Override
    public void startActivityDismissingKeyguard(final Intent intent) {
        mStatusBar.postStartActivityDismissingKeyguard(intent, 0);
    }

    @Override
    public void startActivityDismissingKeyguard(PendingIntent intent) {
        mStatusBar.postStartActivityDismissingKeyguard(intent);
    }

    @Override
    public void startRunnableDismissingKeyguard(Runnable runnable) {
        mStatusBar.postQSRunnableDismissingKeyguard(runnable);
    }

    @Override
    public void warn(String message, Throwable t) {
        // already logged
    }

    @Override
    public void collapsePanels() {
        mStatusBar.postAnimateCollapsePanels();
    }

    @Override
    public void openPanels() {
        mStatusBar.postAnimateOpenPanels();
    }

    @Override
    public Looper getLooper() {
        return mLooper;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public BluetoothController getBluetoothController() {
        return mBluetooth;
    }

    @Override
    public LocationController getLocationController() {
        return mLocation;
    }

    @Override
    public RotationLockController getRotationLockController() {
        return mRotation;
    }

    @Override
    public NetworkController getNetworkController() {
        return mNetwork;
    }

    @Override
    public ZenModeController getZenModeController() {
        return mZen;
    }

    @Override
    public HotspotController getHotspotController() {
        return mHotspot;
    }

    @Override
    public CastController getCastController() {
        return mCast;
    }

    @Override
    public FlashlightController getFlashlightController() {
        return mFlashlight;
    }

    @Override
    public KeyguardMonitor getKeyguardMonitor() {
        return mKeyguard;
    }

    @Override
    public UserSwitcherController getUserSwitcherController() {
        return mUserSwitcherController;
    }

    @Override
    public UserInfoController getUserInfoController() {
        return mUserInfoController;
    }

    @Override
    public BatteryController getBatteryController() {
        return mBattery;
    }

    public SecurityController getSecurityController() {
        return mSecurity;
    }

    public TileServices getTileServices() {
        return mServices;
    }

    public StatusBarIconController getIconController() {
        return mIconController;
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (!TILES_SETTING.equals(key)) {
            return;
        }
        if (DEBUG) Log.d(TAG, "Recreating tiles");
        final List<String> tileSpecs = loadTileSpecs(mContext, newValue);
        if (tileSpecs.equals(mTileSpecs)) return;
        for (Map.Entry<String, QSTile<?>> tile : mTiles.entrySet()) {
            if (!tileSpecs.contains(tile.getKey())) {
                if (DEBUG) Log.d(TAG, "Destroying tile: " + tile.getKey());
                tile.getValue().destroy();
            }
        }
        final LinkedHashMap<String, QSTile<?>> newTiles = new LinkedHashMap<>();
        for (String tileSpec : tileSpecs) {
            if (mTiles.containsKey(tileSpec)) {
                QSTile<?> tile = mTiles.get(tileSpec);
                if (DEBUG) Log.d(TAG, "Adding " + tile);
                newTiles.put(tileSpec, tile);
            } else {
                if (DEBUG) Log.d(TAG, "Creating tile: " + tileSpec);
                try {
                    QSTile<?> tile = createTile(tileSpec);
                    if (tile != null) {
                        tile.setTileSpec(tileSpec);
                        newTiles.put(tileSpec, tile);
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Error creating tile for spec: " + tileSpec, t);
                }
            }
        }
        mTileSpecs.clear();
        mTileSpecs.addAll(tileSpecs);
        mTiles.clear();
        mTiles.putAll(newTiles);
        for (int i = 0; i < mCallbacks.size(); i++) {
            mCallbacks.get(i).onTilesChanged();
        }
    }

    @Override
    public void removeTile(String tileSpec) {
        ArrayList<String> specs = new ArrayList<>(mTileSpecs);
        specs.remove(tileSpec);
        Settings.Secure.putStringForUser(mContext.getContentResolver(), TILES_SETTING,
                TextUtils.join(",", specs), ActivityManager.getCurrentUser());
    }

    public QSTile<?> createTile(String tileSpec) {
        if (tileSpec.equals("wifi")) return WifiTile.isSupported(this)
                ? new WifiTile(this) : null;
        else if (tileSpec.equals("bt")) return BluetoothTile.isSupported(this)
                ? new BluetoothTile(this) : null;
        else if (tileSpec.equals("cell")) return CellularTile.isSupported(this)
                ? new CellularTile(this) : null;
        else if (tileSpec.equals("dnd")) return DndTile.isSupported(this)
                ? new DndTile(this) : null;
        else if (tileSpec.equals("inversion")) return new ColorInversionTile(this);
        else if (tileSpec.equals("airplane")) return new AirplaneModeTile(this);
        else if (tileSpec.equals("work")) return new WorkModeTile(this);
        else if (tileSpec.equals("rotation")) return new RotationLockTile(this);
        else if (tileSpec.equals("flashlight")) return new FlashlightTile(this);
        else if (tileSpec.equals("location")) return new LocationTile(this);
        else if (tileSpec.equals("cast")) return new CastTile(this);
        else if (tileSpec.equals("hotspot")) return new HotspotTile(this);
        else if (tileSpec.equals("user")) return new UserTile(this);
        else if (tileSpec.equals("battery")) return new BatteryTile(this);
        else if (tileSpec.equals(ColorMatrixTile.COLOR_MATRIX_SPEC))
            return new ColorMatrixTile(this);
        // Intent tiles.
        else if (tileSpec.startsWith(IntentTile.PREFIX)) return IntentTile.create(this,tileSpec);
        else if (tileSpec.startsWith(CustomTile.PREFIX)) return CustomTile.create(this,tileSpec);
        else {
            Log.w(TAG, "Bad tile spec: " + tileSpec);
            return null;
        }
    }

    public static List<String> loadTileSpecs(Context context, String tileList) {
        final Resources res = context.getResources();
        final String defaultTileList = res.getString(R.string.quick_settings_tiles_default);
        if (tileList == null) {
            tileList = res.getString(R.string.quick_settings_tiles);
            if (DEBUG) Log.d(TAG, "Loaded tile specs from config: " + tileList);
        } else {
            if (DEBUG) Log.d(TAG, "Loaded tile specs from setting: " + tileList);
        }
        final ArrayList<String> tiles = new ArrayList<String>();
        boolean addedDefault = false;
        for (String tile : tileList.split(",")) {
            tile = tile.trim();
            if (tile.isEmpty()) continue;
            if (tile.equals("default")) {
                if (!addedDefault) {
                    tiles.addAll(Arrays.asList(defaultTileList.split(",")));
                    addedDefault = true;
                }
            } else {
                tiles.add(tile);
            }
        }
        return tiles;
    }
}
