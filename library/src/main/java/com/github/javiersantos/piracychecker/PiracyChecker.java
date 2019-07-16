package com.github.javiersantos.piracychecker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.github.javiersantos.licensing.AESObfuscator;
import com.github.javiersantos.licensing.LibraryChecker;
import com.github.javiersantos.licensing.LibraryCheckerCallback;
import com.github.javiersantos.licensing.ServerManagedPolicy;
import com.github.javiersantos.piracychecker.activities.LicenseActivity;
import com.github.javiersantos.piracychecker.enums.AppType;
import com.github.javiersantos.piracychecker.enums.Display;
import com.github.javiersantos.piracychecker.enums.InstallerID;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError;
import com.github.javiersantos.piracychecker.enums.PirateApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

@SuppressWarnings({"WeakerAccess", "unused"})
@SuppressLint("HardwareIds")
public class PiracyChecker {

    private static final String LIBRARY_PREFERENCES_NAME = "license_check";

    // Library configuration/customizations
    private Context context;
    private String unlicensedDialogTitle;
    private String unlicensedDialogDescription;
    private Display display;
    @ColorRes
    private int colorPrimary;
    @ColorRes
    private int colorPrimaryDark;
    private boolean withLightStatusBar;
    @LayoutRes
    private int layoutXML = -1;
    private boolean enableLVL;
    private boolean enableSigningCertificate;
    private boolean enableUnauthorizedAppsCheck;
    private boolean enableStoresCheck;
    private boolean enableEmulatorCheck;
    private boolean enableDeepEmulatorCheck;
    private boolean enableDebugCheck;
    private boolean enableFoldersCheck;
    private boolean enableAPKCheck;
    private boolean saveToSharedPreferences;
    private boolean blockUnauthorized;
    private SharedPreferences preferences;
    private String preferenceSaveResult;
    private String preferenceBlockUnauthorized;
    private String licenseBase64;
    private String signature;
    private List<InstallerID> installerIDs;
    private PiracyCheckerCallback callback;
    private ArrayList<PirateApp> extraApps;

    // LVL
    private LibraryChecker libraryLVLChecker;
    // Dialog
    private PiracyCheckerDialog dialog;

    public PiracyChecker(Context context) {
        this(context, context.getString(R.string.app_unlicensed),
             context.getString(R.string.app_unlicensed_description));
    }

    public PiracyChecker(Context context, String title, String description) {
        this.context = context;
        this.unlicensedDialogTitle = title;
        this.unlicensedDialogDescription = description;
        this.display = Display.DIALOG;
        this.installerIDs = new ArrayList<>();
        this.extraApps = new ArrayList<>();
        this.colorPrimary = R.color.colorPrimary;
        this.colorPrimaryDark = R.color.colorPrimaryDark;
    }

    public PiracyChecker(Context context, @StringRes int title, @StringRes int description) {
        this(context, context.getString(title), context.getString(description));
    }

    public PiracyChecker enableGooglePlayLicensing(String licenseKeyBase64) {
        this.enableLVL = true;
        this.licenseBase64 = licenseKeyBase64;
        return this;
    }

    public PiracyChecker enableSigningCertificate(String signature) {
        this.enableSigningCertificate = true;
        this.signature = signature;
        return this;
    }

    public PiracyChecker enableInstallerId(InstallerID... installerID) {
        this.installerIDs.addAll(Arrays.asList(installerID));
        return this;
    }

    @Deprecated
    public PiracyChecker enableUnauthorizedAppsCheck() {
        this.enableUnauthorizedAppsCheck = true;
        return this;
    }

    public PiracyChecker enableUnauthorizedAppsCheck(boolean enable) {
        this.enableUnauthorizedAppsCheck = enable;
        return this;
    }

    public PiracyChecker blockIfUnauthorizedAppUninstalled(SharedPreferences preferences,
                                                           @NonNull String preferenceName) {
        this.blockUnauthorized = true;
        this.preferenceBlockUnauthorized = preferenceName;
        saveToSharedPreferences(preferences);
        return this;
    }

    public PiracyChecker blockIfUnauthorizedAppUninstalled(String preferencesName,
                                                           @NonNull String preferenceName) {
        this.blockUnauthorized = true;
        this.preferenceBlockUnauthorized = preferenceName;
        saveToSharedPreferences(preferencesName);
        return this;
    }

    @Deprecated
    public PiracyChecker enableStoresCheck() {
        this.enableStoresCheck = true;
        return this;
    }

    @Deprecated
    public PiracyChecker enableDebugCheck() {
        this.enableDebugCheck = true;
        return this;
    }

    public PiracyChecker enableStoresCheck(boolean enable) {
        this.enableStoresCheck = enable;
        return this;
    }

    public PiracyChecker enableDebugCheck(boolean enable) {
        this.enableDebugCheck = enable;
        return this;
    }

    public PiracyChecker enableAPKCheck(boolean enable) {
        this.enableAPKCheck = enable;
        return this;
    }

    public PiracyChecker enableEmulatorCheck(boolean deepCheck) {
        this.enableEmulatorCheck = true;
        this.enableDeepEmulatorCheck = deepCheck;
        return this;
    }

    public PiracyChecker enableFoldersCheck(boolean foldersCheck) {
        this.enableFoldersCheck = foldersCheck;
        return this;
    }

    public PiracyChecker addAppToCheck(PirateApp... apps) {
        this.extraApps.addAll(Arrays.asList(apps));
        return this;
    }

    public PiracyChecker saveResultToSharedPreferences(SharedPreferences preferences,
                                                       @NonNull String preferenceName) {
        this.saveToSharedPreferences = true;
        this.preferenceSaveResult = preferenceName;
        saveToSharedPreferences(preferences);
        return this;
    }

    public PiracyChecker saveResultToSharedPreferences(String preferencesName,
                                                       @NonNull String preferenceName) {
        this.saveToSharedPreferences = true;
        this.preferenceSaveResult = preferenceName;
        saveToSharedPreferences(preferencesName);
        return this;
    }

    private void saveToSharedPreferences(SharedPreferences preferences) {
        if (preferences != null) {
            this.preferences = preferences;
        } else {
            try {
                this.preferences = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
            } catch (Exception e) {
                this.preferences = context.getSharedPreferences(LIBRARY_PREFERENCES_NAME,
                                                                Context.MODE_PRIVATE);
            }
        }
    }

    private void saveToSharedPreferences(String preferencesName) {
        if (preferencesName != null) {
            this.preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        } else {
            try {
                this.preferences = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
            } catch (Exception e) {
                this.preferences = context.getSharedPreferences(LIBRARY_PREFERENCES_NAME,
                                                                Context.MODE_PRIVATE);
            }
        }
    }

    public PiracyChecker display(Display display) {
        this.display = display;
        return this;
    }

    public PiracyChecker withActivityColors(@ColorRes int colorPrimary,
                                            @ColorRes int colorPrimaryDark,
                                            boolean withLightStatusBar) {
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
        this.withLightStatusBar = withLightStatusBar;
        return this;
    }

    public PiracyChecker withActivityLayout(@LayoutRes int layout) {
        this.layoutXML = layout;
        return this;
    }

    public PiracyChecker callback(PiracyCheckerCallback callback) {
        this.callback = callback;
        return this;
    }

    public void destroy() {
        dismissDialog();
        destroyLVLChecker();
        context = null;
    }

    public void start() {
        if (callback == null) {
            this.callback = new PiracyCheckerCallback() {
                @Override
                public void allow() {
                }

                @Override
                public void dontAllow(@NonNull PiracyCheckerError error, @Nullable PirateApp app) {
                    if (context instanceof Activity && ((Activity) context).isFinishing()) {
                        return;
                    }

                    String dialogContent = unlicensedDialogDescription;
                    if (app != null)
                        dialogContent = context.getString(R.string.unauthorized_app_found,
                                                          app.getName());
                    else if (error.equals(PiracyCheckerError.BLOCK_PIRATE_APP))
                        dialogContent = context.getString(R.string.unauthorized_app_blocked);

                    if (display == Display.DIALOG) {
                        dismissDialog();
                        dialog = PiracyCheckerDialog.newInstance(unlicensedDialogTitle,
                                                                 dialogContent);
                        if (dialog != null) {
                            dialog.show(context);
                        } else {
                            Log.e("PiracyChecker", "Unlicensed dialog was not built properly. " +
                                    "Make sure your context is an instance of Activity");
                        }
                    } else {
                        Intent intent = new Intent(context, LicenseActivity.class)
                                .putExtra("content", dialogContent)
                                .putExtra("colorPrimary", colorPrimary)
                                .putExtra("colorPrimaryDark", colorPrimaryDark)
                                .putExtra("withLightStatusBar", withLightStatusBar)
                                .putExtra("layoutXML", layoutXML);
                        context.startActivity(intent);
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                        destroy();
                    }
                }
            };
        }
        verify(callback);
    }

    private void verify(final PiracyCheckerCallback verifyCallback) {
        // Library will check first the non-LVL methods since LVL is asynchronous and could take
        // some seconds to give a result
        if (!verifySigningCertificate()) {
            verifyCallback.dontAllow(PiracyCheckerError.SIGNATURE_NOT_VALID, null);
        } else if (!verifyInstallerId()) {
            verifyCallback.dontAllow(PiracyCheckerError.INVALID_INSTALLER_ID, null);
        } else if (!verifyUnauthorizedApp()) {
            verifyCallback.dontAllow(PiracyCheckerError.BLOCK_PIRATE_APP, null);
        } else {
            if (enableLVL) {
                String deviceId = Settings.Secure.getString(context.getContentResolver(),
                                                            Settings.Secure.ANDROID_ID);
                destroyLVLChecker();
                libraryLVLChecker =
                        new LibraryChecker(
                                context,
                                new ServerManagedPolicy(context,
                                                        new AESObfuscator(
                                                                SaltUtils.getSalt(context),
                                                                context.getPackageName(),
                                                                deviceId)),
                                licenseBase64);
                libraryLVLChecker.checkAccess(new LibraryCheckerCallback() {
                    @Override
                    public void allow(int reason) {
                        doExtraVerification(verifyCallback, true);
                    }

                    @Override
                    public void dontAllow(int reason) {
                        doExtraVerification(verifyCallback, false);
                    }

                    @Override
                    public void applicationError(int errorCode) {
                        verifyCallback.onError(
                                PiracyCheckerError.getCheckerErrorFromCode(errorCode));
                    }
                });
            } else {
                doExtraVerification(verifyCallback, true);
            }
        }
    }

    private boolean verifySigningCertificate() {
        return !enableSigningCertificate ||
                LibraryUtils.verifySigningCertificate(context, signature);
    }

    private boolean verifyInstallerId() {
        return installerIDs.isEmpty() || LibraryUtils.verifyInstallerId(context, installerIDs);
    }

    private boolean verifyUnauthorizedApp() {
        return !blockUnauthorized || !preferences.getBoolean(preferenceBlockUnauthorized, false);
    }

    private void doExtraVerification(PiracyCheckerCallback verifyCallback,
                                     boolean possibleSuccess) {
        PirateApp app = LibraryUtils.getPirateApp(context, enableUnauthorizedAppsCheck,
                                                  enableStoresCheck, enableFoldersCheck,
                                                  enableAPKCheck, extraApps);
        if (possibleSuccess) {
            if (enableDebugCheck && LibraryUtils.isDebug(context)) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                verifyCallback.dontAllow(PiracyCheckerError.USING_DEBUG_APP, null);
            } else if (enableEmulatorCheck && LibraryUtils.isInEmulator(enableDeepEmulatorCheck)) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                verifyCallback.dontAllow(PiracyCheckerError.USING_APP_IN_EMULATOR, null);
            } else if (app != null) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                if (preferences != null && blockUnauthorized && app.getType() == AppType.PIRATE)
                    preferences.edit().putBoolean(preferenceBlockUnauthorized, true).apply();
                verifyCallback.dontAllow(app.getType() == AppType.STORE
                                         ? PiracyCheckerError.THIRD_PARTY_STORE_INSTALLED
                                         : PiracyCheckerError.PIRATE_APP_INSTALLED, app);
            } else {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, true).apply();
                verifyCallback.allow();
            }
        } else {
            if (app != null) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                if (preferences != null && blockUnauthorized && app.getType() == AppType.PIRATE)
                    preferences.edit().putBoolean(preferenceBlockUnauthorized, true).apply();
                verifyCallback.dontAllow(app.getType() == AppType.STORE
                                         ? PiracyCheckerError.THIRD_PARTY_STORE_INSTALLED
                                         : PiracyCheckerError.PIRATE_APP_INSTALLED, app);
            } else {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                verifyCallback.dontAllow(PiracyCheckerError.NOT_LICENSED, null);
            }
        }
    }

    private void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void destroyLVLChecker() {
        if (libraryLVLChecker != null) {
            libraryLVLChecker.finishAllChecks();
            libraryLVLChecker.onDestroy();
            libraryLVLChecker = null;
        }
    }
}