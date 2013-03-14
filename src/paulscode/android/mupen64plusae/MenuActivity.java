/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2013 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Mupen64PlusAE. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: littleguy77
 */
package paulscode.android.mupen64plusae;

import java.io.File;

import paulscode.android.mupen64plusae.input.TouchController;
import paulscode.android.mupen64plusae.persistent.AppData;
import paulscode.android.mupen64plusae.persistent.UserPrefs;
import paulscode.android.mupen64plusae.util.ChangeLog;
import paulscode.android.mupen64plusae.util.CrashTester;
import paulscode.android.mupen64plusae.util.DeviceUtil;
import paulscode.android.mupen64plusae.util.ErrorLogger;
import paulscode.android.mupen64plusae.util.FileUtil;
import paulscode.android.mupen64plusae.util.Notifier;
import paulscode.android.mupen64plusae.util.OUYAInterface;
import paulscode.android.mupen64plusae.util.PrefUtil;
import paulscode.android.mupen64plusae.util.Prompt;
import paulscode.android.mupen64plusae.util.Prompt.OnConfirmListener;
import paulscode.android.mupen64plusae.util.TaskHandler;
import paulscode.android.mupen64plusae.util.Utility;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class MenuActivity extends PreferenceActivity implements OnPreferenceClickListener,
        OnSharedPreferenceChangeListener
{
    // These constants must match the keys used in res/xml/preferences.xml
    
    private static final String ACTION_PLAY = "actionPlay";
    private static final String ACTION_DEVICE_INFO = "actionDeviceInfo";
    private static final String ACTION_CONTROLLER_INFO = "actionControllerInfo";
    private static final String ACTION_MIGRATE_SLOT_SAVES = "actionMigrateSlotSaves";
    private static final String ACTION_CRASH_TEST = "actionCrashTest";
    private static final String ACTION_RELOAD_ASSETS = "actionReloadAssets";
    private static final String ACTION_RESET_USER_PREFS = "actionResetUserPrefs";
    private static final String ACTION_HELP = "actionHelp";
    private static final String ACTION_ABOUT = "actionAbout";
    
    private static final String SCREEN_INPUT = "screenInput";
    private static final String SCREEN_TOUCHPAD = "screenTouchpad";
    private static final String SCREEN_TOUCHSCREEN = "screenTouchscreen";
    private static final String SCREEN_VIDEO = "screenVideo";
    private static final String SCREEN_AUDIO = "screenAudio";
    
    private static final String CATEGORY_SINGLE_PLAYER = "categorySinglePlayer";
    private static final String CATEGORY_GLES2_RICE = "categoryGles2Rice";
    private static final String CATEGORY_GLES2_N64 = "categoryGles2N64";
    
    private static final String TOUCHSCREEN_ENABLED = "touchscreenEnabled";
    private static final String TOUCHSCREEN_AUTO_HOLDABLES = "touchscreenAutoHoldables";
    private static final String TOUCHSCREEN_SIZE = "touchscreenSize";
    private static final String PATH_CUSTOM_TOUCHSCREEN = "pathCustomTouchscreen";
    private static final String TOUCHPAD_ENABLED = "touchpadEnabled";
    private static final String PLUGIN_VIDEO = "pluginVideo";
    private static final String VIDEO_POSITION = "videoPosition";
    private static final String PATH_HI_RES_TEXTURES = "pathHiResTextures";
    private static final String NAVIGATION_MODE = "navigationMode";
    private static final String ACRA_USER_EMAIL = "acra.user.email";
    private static final String LOCALE_OVERRIDE = "localeOverride";
    
    // App data and user preferences
    private AppData mAppData = null;
    private UserPrefs mUserPrefs = null;
    
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // Get app data and user preferences
        mAppData = new AppData( this );
        mUserPrefs = new UserPrefs( this );
        mUserPrefs.enforceLocale( this );
        
        int lastVer = mAppData.getLastAppVersionCode();
        int currVer = mAppData.appVersionCode;
        if( lastVer != currVer )
        {
            ChangeLog log = new ChangeLog( getAssets() );
            if( log.show( this, lastVer + 1, currVer ) )
            {
                mAppData.putLastAppVersionCode( currVer );
            }
        }
        
        // Disable the Xperia PLAY plugin as necessary
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        if( !mAppData.hardwareInfo.isXperiaPlay )
            prefs.edit().putBoolean( TOUCHPAD_ENABLED, false ).commit();
        // Disable the touchscreen when running on OUYA
        if( OUYAInterface.IS_OUYA_HARDWARE )
            prefs.edit().putBoolean( TOUCHSCREEN_ENABLED, false ).commit();
        
        // Ensure that any missing preferences are populated with defaults (e.g. preference added to new release)
        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );
        
        // Ensure that selected plugin names are valid
        Resources res = getResources();
        PrefUtil.validateListPreference( res, prefs, "pluginInput", R.string.pluginInput_default, R.array.pluginInput_values );
        PrefUtil.validateListPreference( res, prefs, "pluginVideo", R.string.pluginVideo_default, R.array.pluginVideo_values );
        PrefUtil.validateListPreference( res, prefs, "pluginAudio", R.string.pluginAudio_default, R.array.pluginAudio_values );
        PrefUtil.validateListPreference( res, prefs, "pluginRsp", R.string.pluginRsp_default, R.array.pluginRsp_values );
        PrefUtil.validateListPreference( res, prefs, "pluginCore", R.string.pluginCore_default, R.array.pluginCore_values );
        
        // Load user preference menu structure from XML and update view
        addPreferencesFromResource( R.xml.preferences );
        
        // Refresh the preference data wrapper
        mUserPrefs = new UserPrefs( this );
        
        // Populate the language menu
        ListPreference languagePref = (ListPreference) findPreference( LOCALE_OVERRIDE );
        languagePref.setEntryValues( mUserPrefs.localeCodes );
        languagePref.setEntries( mUserPrefs.localeNames );
        
        // Handle certain menu items that require extra processing or aren't actually preferences
        PrefUtil.setOnPreferenceClickListener( this, ACTION_DEVICE_INFO, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_CONTROLLER_INFO, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_MIGRATE_SLOT_SAVES, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_RELOAD_ASSETS, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_RESET_USER_PREFS, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_HELP, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_ABOUT, this );
        PrefUtil.setOnPreferenceClickListener( this, PATH_HI_RES_TEXTURES, this );
        
        // Handle crash tests in a particular way (see CrashTester for more info)
        findPreference( ACTION_CRASH_TEST ).setOnPreferenceClickListener( new CrashTester( this ) );
        
        // Hide certain categories altogether if they're not applicable. Normally we just rely on
        // the built-in dependency disabler, but here the categories are so large that hiding them
        // provides a better user experience.
        if( !mUserPrefs.isGles2N64Enabled )
            PrefUtil.removePreference( this, SCREEN_VIDEO, CATEGORY_GLES2_N64 );
        
        if( !mUserPrefs.isGles2RiceEnabled )
            PrefUtil.removePreference( this, SCREEN_VIDEO, CATEGORY_GLES2_RICE );
        
        if( !mAppData.hardwareInfo.isXperiaPlay )
            PrefUtil.removePreference( this, CATEGORY_SINGLE_PLAYER, SCREEN_TOUCHPAD );
        
        if( mUserPrefs.isOuyaMode )
            PrefUtil.removePreference( this, CATEGORY_SINGLE_PLAYER, SCREEN_TOUCHSCREEN );
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        sharedPreferences.unregisterOnSharedPreferenceChangeListener( this );
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        refreshViews( sharedPreferences, mUserPrefs );
        sharedPreferences.registerOnSharedPreferenceChangeListener( this );
    }
    
    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
    {
        if( key.equals( PLUGIN_VIDEO ) || key.equals( TOUCHPAD_ENABLED )
                || key.equals( NAVIGATION_MODE ) || key.equals( LOCALE_OVERRIDE ) )
        {
            // Sometimes one preference change affects the hierarchy or layout of the views. In this
            // case it's easier just to restart the activity than try to figure out what to fix.
            // Examples:
            // * Restore the preference categories that were removed in refreshViews(...)
            // * Change the input mapping layout when Xperia Play touchpad en/disabled
            finish();
            startActivity( getIntent() );
        }
        else if( key.equals( PATH_HI_RES_TEXTURES ) )
        {
            processTexturePak( sharedPreferences.getString( PATH_HI_RES_TEXTURES, "" ) );
        }
        else
        {
            // Just refresh the preference screens in place
            mUserPrefs = new UserPrefs( this );
            refreshViews( sharedPreferences, mUserPrefs );
        }
    }
    
    @TargetApi( 9 )
    @SuppressWarnings( "deprecation" )
    private void refreshViews( SharedPreferences sharedPreferences, UserPrefs user )
    {
        // Enable the play menu only if the selected game actually exists
        File selectedGame = new File( mUserPrefs.selectedGame );
        boolean isValidGame = selectedGame.exists() && selectedGame.isFile();
        PrefUtil.enablePreference( this, ACTION_PLAY, isValidGame );
        
        // Enable the input menu only if the input plug-in is not a dummy
        PrefUtil.enablePreference( this, SCREEN_INPUT, user.inputPlugin.enabled );
        
        // Enable the audio menu only if the audio plug-in is not a dummy
        PrefUtil.enablePreference( this, SCREEN_AUDIO, user.audioPlugin.enabled );
        
        // Enable the video menu only if the video plug-in is not a dummy
        PrefUtil.enablePreference( this, SCREEN_VIDEO, user.videoPlugin.enabled );
        
        // Enable the screen position prefs only if the screen is in portrait mode and not stretched
        boolean isPortrait = user.videoOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || user.videoOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        PrefUtil.enablePreference( this, VIDEO_POSITION, isPortrait && !user.isStretched );
        
        // Enable the auto-holdables pref if auto-hold is not disabled
        PrefUtil.enablePreference( this, TOUCHSCREEN_AUTO_HOLDABLES, user.isTouchscreenEnabled
                && user.touchscreenAutoHold != TouchController.AUTOHOLD_METHOD_DISABLED );
        
        // Enable the custom touchscreen prefs under certain conditions
        PrefUtil.enablePreference( this, PATH_CUSTOM_TOUCHSCREEN, user.isTouchscreenEnabled
                && user.isTouchscreenCustom );
        PrefUtil.enablePreference( this, TOUCHSCREEN_SIZE, user.isTouchscreenEnabled
                && !user.isTouchscreenCustom );
        
        // Update the summary text in a particular way for ACRA user info
        EditTextPreference pref = (EditTextPreference) findPreference( ACRA_USER_EMAIL );
        String value = pref.getText();
        if( TextUtils.isEmpty( value ) )
            pref.setSummary( getString( R.string.acraUserEmail_summary ) );
        else
            pref.setSummary( value );
    }
    
    @Override
    public boolean onPreferenceClick( Preference preference )
    {
        // Handle the clicks on certain menu items that aren't actually preferences
        String key = preference.getKey();
        
        if( key.equals( ACTION_DEVICE_INFO ) )
            actionDeviceInfo();
        
        else if( key.equals( ACTION_CONTROLLER_INFO ) )
            actionControllerInfo();
        
        else if( key.equals( ACTION_MIGRATE_SLOT_SAVES ) )
            actionMigrateSlotSaves();
        
        else if( key.equals( ACTION_RELOAD_ASSETS ) )
            actionReloadAssets();
        
        else if( key.equals( ACTION_RESET_USER_PREFS ) )
            actionResetUserPrefs();
        
        else if( key.equals( ACTION_HELP ) )
            actionHelp();
        
        else if( key.equals( ACTION_ABOUT ) )
            actionAbout();
        
        else
            // Let Android handle all other preference clicks
            return false;
        
        // Tell Android that we handled the click
        return true;
    }
    
    private void actionDeviceInfo()
    {
        String title = getString( R.string.actionDeviceInfo_title );
        String message = DeviceUtil.getCpuInfo();
        new Builder( this ).setTitle( title ).setMessage( message ).create().show();
    }
    
    private void actionControllerInfo()
    {
        String title = getString( R.string.actionControllerInfo_title );
        String message = DeviceUtil.getPeripheralInfo();
        new Builder( this ).setTitle( title ).setMessage( message ).create().show();
    }
    
    private void actionMigrateSlotSaves()
    {
        final File oldDir = new File( mAppData.oldDataDir + "/data/save/" );
        if( !oldDir.exists() )
        {
            String title = getString( R.string.actionMigrateSlotSaves_title );
            String message = getString( R.string.toast_migrateSlotSavesNotFound );
            new Builder( this ).setTitle( title ).setMessage( message ).create().show();
        }
        else
        {
            String title = getString( R.string.confirm_title );
            String message = getString( R.string.confirmMigrateSlotSaves_message );
            Prompt.promptConfirm( this, title, message, new OnConfirmListener()
            {
                @Override
                public void onConfirm()
                {
                    File newDir = new File( mUserPrefs.slotSaveDir );
                    FileUtil.copyFile( oldDir, newDir, true );
                    Notifier.showToast( MenuActivity.this, R.string.toast_migrateSlotSavesSuccess );
                }
            } );
        }
    }
    
    private void actionReloadAssets()
    {
        mAppData.putAssetVersion( 0 );
        startActivity( new Intent( this, MainActivity.class ) );
        finish();
    }
    
    private void actionResetUserPrefs()
    {
        String title = getString( R.string.confirm_title );
        String message = getString( R.string.actionResetUserPrefs_popupMessage );
        Prompt.promptConfirm( this, title, message, new OnConfirmListener()
        {
            @Override
            public void onConfirm()
            {
                // Reset the user preferences
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences( MenuActivity.this );
                preferences.unregisterOnSharedPreferenceChangeListener( MenuActivity.this );
                preferences.edit().clear().commit();
                PreferenceManager.setDefaultValues( MenuActivity.this, R.xml.preferences, true );
                
                // Rebuild the menu system by restarting the activity
                finish();
                startActivity( getIntent() );
            }
        } );
    }
    
    private void actionHelp()
    {
        String title = getString( R.string.actionHelp_title );
        String message = getString( R.string.actionHelp_message );
        String faq = getString( R.string.actionHelp_faq );
        String bug = getString( R.string.actionHelp_reportbug );
        OnClickListener listener = new OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which )
            {
                if( which == DialogInterface.BUTTON_NEUTRAL )
                    Utility.launchUri( MenuActivity.this, R.string.actionHelp_uriFaq );
                else if( which == DialogInterface.BUTTON_POSITIVE )
                    Utility.launchUri( MenuActivity.this, R.string.actionHelp_uriBug );
            }
        };
        new Builder( this ).setTitle( title ).setMessage( message )
                .setNeutralButton( faq, listener ).setNegativeButton( null, null )
                .setPositiveButton( bug, listener ).create().show();
    }
    
    private void actionAbout()
    {
        String title = getString( R.string.actionAbout_title );
        String message = getString( R.string.actionAbout_message, mAppData.appVersion,
                mAppData.appVersionCode );
        String credits = getString( R.string.actionAbout_credits );
        String changelog = getString( R.string.actionAbout_changelog );
        OnClickListener listener = new OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which )
            {
                if( which == DialogInterface.BUTTON_NEUTRAL )
                    Utility.launchUri( MenuActivity.this, R.string.actionAbout_uriCredits );
                else if( which == DialogInterface.BUTTON_POSITIVE )
                    new ChangeLog( getAssets() ).show( MenuActivity.this, 0, mAppData.appVersionCode );
            }
        };
        new Builder( this ).setTitle( title ).setMessage( message ).setNegativeButton( null, null )
                .setNeutralButton( credits, listener ).setPositiveButton( changelog, listener )
                .create().show();
    }
    
    private void processTexturePak( final String filename )
    {
        if( TextUtils.isEmpty( filename ) )
        {
            ErrorLogger.put( "Video", "pathHiResTextures",
                    "Filename not specified in MenuActivity.processTexturePak" );
            Notifier.showToast( this, R.string.pathHiResTexturesTask_errorMessage );
            return;
        }
        
        TaskHandler.Task task = new TaskHandler.Task()
        {
            @Override
            public void run()
            {
                String headerName = Utility.getTexturePackName( filename );
                if( !ErrorLogger.hasError() )
                {
                    if( TextUtils.isEmpty( headerName ) )
                    {
                        ErrorLogger
                                .setLastError( "getTexturePackName returned null in MenuActivity.processTexturePak" );
                        ErrorLogger.putLastError( "Video", "pathHiResTextures" );
                    }
                    else
                    {
                        String outputFolder = mAppData.dataDir + "/data/hires_texture/"
                                + headerName;
                        FileUtil.deleteFolder( new File( outputFolder ) );
                        Utility.unzipAll( new File( filename ), outputFolder );
                    }
                }
            }
            
            @Override
            public void onComplete()
            {
                if( ErrorLogger.hasError() )
                    Notifier.showToast( MenuActivity.this,
                            R.string.pathHiResTexturesTask_errorMessage );
                ErrorLogger.clearLastError();
            }
        };
        
        String title = getString( R.string.pathHiResTexturesTask_title );
        String message = getString( R.string.pathHiResTexturesTask_message );
        TaskHandler.run( this, title, message, task );
    }
}
