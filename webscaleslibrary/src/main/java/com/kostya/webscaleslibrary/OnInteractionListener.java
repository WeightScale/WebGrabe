package com.kostya.webscaleslibrary;

import com.kostya.webscaleslibrary.preferences.Settings;

/**
 * @author Kostya on 26.12.2016.
 */
interface OnInteractionListener {

    void onUpdateSettings(Settings settings);
    void onScaleModuleCallback(Object obj);

}
