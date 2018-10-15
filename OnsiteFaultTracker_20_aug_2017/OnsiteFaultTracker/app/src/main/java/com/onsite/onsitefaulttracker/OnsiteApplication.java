package com.onsite.onsitefaulttracker;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.onsite.onsitefaulttracker.connectivity.BLEManager;
import com.onsite.onsitefaulttracker.connectivity.TcpConnection;
import com.onsite.onsitefaulttracker.util.BatteryUtil;
import com.onsite.onsitefaulttracker.util.BitmapSaveUtil;
import com.onsite.onsitefaulttracker.util.BusNotificationUtil;
import com.onsite.onsitefaulttracker.util.CalculationUtil;
import com.onsite.onsitefaulttracker.util.CameraUtil;
import com.onsite.onsitefaulttracker.util.RecordUtil;
import com.onsite.onsitefaulttracker.util.SettingsUtil;

/**
 * Created by hihi on 6/6/2016.
 *
 * The Application class for this application.
 * Sets up Singletons and Utility classes.
 */
public class OnsiteApplication extends Application {

    /**
     * On Create
     *
     * Sets up singletons and Utility classes
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        // initialize the singletons used throughout this app
        SettingsUtil.initialize(this);
        CalculationUtil.initialize(this);
        CameraUtil.initialize(this);
        BatteryUtil.initialize(this);
        BitmapSaveUtil.initialize(this);
        RecordUtil.initialize(this);
        BLEManager.initialize(this);
        BusNotificationUtil.initialize(this);
        TcpConnection.initialize(this);
    }

}
