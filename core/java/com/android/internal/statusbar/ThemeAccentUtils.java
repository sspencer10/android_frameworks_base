/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.internal.statusbar;

import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.util.Log;

public class ThemeAccentUtils {
    public static final String TAG = "ThemeAccentUtils";

    private static final String[] ACCENTS = {
        "default_accent", // 0
        "com.accents.red", // 1
        "com.accents.pink", // 2
        "com.accents.purple", // 3
        "com.accents.deeppurple", // 4
        "com.accents.indigo", // 5
        "com.accents.blue", // 6
        "com.accents.lightblue", // 7
        "com.accents.cyan", // 8
        "com.accents.teal", // 9
        "com.accents.green", // 10
        "com.accents.lightgreen", // 11
        "com.accents.lime", // 12
        "com.accents.yellow", // 13
        "com.accents.amber", // 14
        "com.accents.orange", // 15
        "com.accents.deeporange", // 16
        "com.accents.brown", // 17
        "com.accents.grey", // 18
        "com.accents.bluegrey", // 19
        "com.accents.candyred", //20
        "com.accents.palered", //21
        "com.accents.extendedgreen", //22
        "com.accents.paleblue", //23
        "com.accents.jadegreen", //24
        "com.accents.black", // 25
        "com.accents.white", // 26
        "com.accents.userone", // 27
        "com.accents.usertwo", // 28
        "com.accents.userthree", // 29
        "com.accents.userfour", // 30
        "com.accents.userfive", // 31
        "com.accents.usersix", // 32
        "com.accents.userseven", // 33

    };

    private static final String[] DARK_THEMES = {
        "com.android.system.theme.dark", // 0
        "com.android.settings.theme.dark", // 1
        "com.android.systemui.theme.dark", // 2
        "com.accents.pink", //3
        "com.android.dialer.theme.dark", //4
        "com.android.contacts.theme.dark", //5
        "com.android.documentsui.theme.dark", //6
        "com.android.gboard.theme.dark", //7
    };

    private static final String[] BLACK_THEMES = {
        "com.android.system.theme.black", // 0
        "com.android.settings.theme.black", // 1
        "com.android.systemui.theme.black", // 2
        "com.accents.deeppurple", //3
        "com.android.dialer.theme.black", //4
        "com.android.contacts.theme.black", //5
        "com.android.documentsui.theme.black", //6
        "com.android.gboard.theme.black", //7
    };

    private static final String[] SHISHUNIGHTS_THEMES = {
        "com.android.system.theme.shishunights", // 0
        "com.android.settings.theme.shishunights", // 1
        "com.android.systemui.theme.shishunights", // 2
        "com.accents.red", //3
        "com.android.dialer.theme.shishunights", //4
        "com.android.contacts.theme.shishunights", //5
        "com.android.documentsui.theme.shishunights", //6
        "com.google.android.apps.wellbeing.theme.shishunights", //7
        "com.android.gboard.theme.shishunights", //8
    };

    private static final String[] CHOCOLATE_THEMES = {
        "com.android.system.theme.chocolate", // 0
        "com.android.settings.theme.chocolate", // 1
        "com.android.systemui.theme.chocolate", // 2
        "com.accents.candyred", //3
        "com.android.dialer.theme.chocolate", //4
        "com.android.contacts.theme.chocolate", //5
        "com.android.documentsui.theme.chocolate", //6
        "com.google.android.apps.wellbeing.theme.chocolate", //7
        "com.android.gboard.theme.chocolate", //8
    };

    private static final String STOCK_DARK_THEME = "com.android.systemui.theme.dark";

    // Switches theme accent from to another or back to stock
    public static void updateAccents(IOverlayManager om, int userId, int accentSetting) {
        if (accentSetting == 0) {
            //On selecting default accent, set accent to bubblegum pink if Dark Theme is being used
            if (isUsingDarkTheme(om, userId)) {
                try {
                    om.setEnabled(DARK_THEMES[3],
                        true, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
            //On selecting default accent, set accent to spooked purple if Black Theme is being used
            } else if (isUsingBlackTheme(om, userId)) {
                try {
                    om.setEnabled(BLACK_THEMES[3],
                        true, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
            //On selecting default accent, set accent to sexy red if ShishuNights Theme is being used
            } else if (isUsingShishuNightsTheme(om, userId)) {
                try {
                    om.setEnabled(SHISHUNIGHTS_THEMES[3],
                        true, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
            //On selecting default accent, set accent to candy red if Chocolate Theme is being used
            } else if (isUsingChocolateTheme(om, userId)) {
                try {
                    om.setEnabled(CHOCOLATE_THEMES[3],
                        true, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
            } else {
                unloadAccents(om, userId);
            }
        } else if ((accentSetting < 25) || (accentSetting > 26)) {
            try {
                om.setEnabled(ACCENTS[accentSetting],
                        true, userId);
            } catch (RemoteException e) {
                Log.w(TAG, "Can't change theme", e);
            }
        } else if (accentSetting == 25) {
            try {
                // If using a dark, black or shishuinights or chocolate theme we use the white accent, otherwise use the black accent
                if (isUsingDarkTheme(om, userId) || isUsingBlackTheme(om, userId) || isUsingShishuNightsTheme(om, userId) || isUsingChocolateTheme(om, userId)) {
                    om.setEnabled(ACCENTS[26],
                            true, userId);
                } else {
                    om.setEnabled(ACCENTS[25],
                            true, userId);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Can't change theme", e);
            }
        }
    }

    // Unload all the theme accents
    public static void unloadAccents(IOverlayManager om, int userId) {
        // skip index 0
        for (int i = 1; i < ACCENTS.length; i++) {
            String accent = ACCENTS[i];
            try {
                om.setEnabled(accent,
                        false /*disable*/, userId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Check for the dark system theme
    public static boolean isUsingDarkTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(DARK_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    // Check for the black system theme
    public static boolean isUsingBlackTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(BLACK_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
     }

    // Check for the shishunights system theme
    public static boolean isUsingShishuNightsTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(SHISHUNIGHTS_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
     }

    // Check for the chocolate system theme
    public static boolean isUsingChocolateTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(CHOCOLATE_THEMES[0],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
     }

    public static void setLightDarkTheme(IOverlayManager om, int userId, boolean useDarkTheme) {
        for (String theme : DARK_THEMES) {
                try {
                    om.setEnabled(theme,
                        useDarkTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                    if (useDarkTheme) {
                        unloadStockDarkTheme(om, userId);
                    }
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    public static void setLightBlackTheme(IOverlayManager om, int userId, boolean useBlackTheme) {
        for (String theme : BLACK_THEMES) {
                try {
                    om.setEnabled(theme,
                        useBlackTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    public static void setLightShishuNightsTheme(IOverlayManager om, int userId, boolean useShishuNightsTheme) {
        for (String theme : SHISHUNIGHTS_THEMES) {
                try {
                    om.setEnabled(theme,
                        useShishuNightsTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    public static void setLightChocolateTheme(IOverlayManager om, int userId, boolean useChocolateTheme) {
        for (String theme : CHOCOLATE_THEMES) {
                try {
                    om.setEnabled(theme,
                        useChocolateTheme, userId);
                    unfuckBlackWhiteAccent(om, userId);
                } catch (RemoteException e) {
                    Log.w(TAG, "Can't change theme", e);
                }
        }
    }

    // Check for black and white accent overlays
    public static void unfuckBlackWhiteAccent(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            if (isUsingDarkTheme(om, userId) || isUsingBlackTheme(om, userId) || isUsingShishuNightsTheme(om, userId) || isUsingChocolateTheme(om, userId)) {
                themeInfo = om.getOverlayInfo(ACCENTS[25],
                        userId);
                if (themeInfo != null && themeInfo.isEnabled()) {
                    om.setEnabled(ACCENTS[25],
                            false /*disable*/, userId);
                    om.setEnabled(ACCENTS[26],
                            true, userId);
                }
            } else {
                themeInfo = om.getOverlayInfo(ACCENTS[26],
                        userId);
                if (themeInfo != null && themeInfo.isEnabled()) {
                    om.setEnabled(ACCENTS[26],
                            false /*disable*/, userId);
                    om.setEnabled(ACCENTS[25],
                            true, userId);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Unloads the stock dark theme
    public static void unloadStockDarkTheme(IOverlayManager om, int userId) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(STOCK_DARK_THEME,
                    userId);
            if (themeInfo != null && themeInfo.isEnabled()) {
                om.setEnabled(STOCK_DARK_THEME,
                        false /*disable*/, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Check for any accent overlay
    public static boolean isUsingAccent(IOverlayManager om, int userId, int accent) {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo(ACCENTS[accent],
                    userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }
}
