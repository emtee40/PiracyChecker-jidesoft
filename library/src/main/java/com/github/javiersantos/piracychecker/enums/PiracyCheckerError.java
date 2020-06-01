package com.github.javiersantos.piracychecker.enums;

public enum PiracyCheckerError {
    SIGNATURE_NOT_VALID("This app is using another signature. The original APK has been modified."),
    USING_DEBUG_APP("This is a debug build."),
    NOT_LICENSED("This user is not using a licensed application from Google Play."),
    PIRATE_APP_INSTALLED("At least one pirate app has been detected on device."),
    USING_APP_IN_EMULATOR("This app is being used in an emulator."),
    INVALID_INSTALLER_ID("This app has been installed from a non-allowed source."),
    BLOCK_PIRATE_APP(
            "At least one pirate app has been detected and the app must be reinstalled when all unauthorized apps are uninstalled.."),
    THIRD_PARTY_STORE_INSTALLED("At least one third-party store has been detected on device."),

    // Other errors
    NON_MATCHING_UID("Application UID doesn\'t match."),
    INVALID_PACKAGE_NAME("Application package name is invalid."),
    CHECK_IN_PROGRESS("License check is in progress."),
    UNKNOWN("Unknown error."),
    MISSING_PERMISSION("Application misses the \'com.android.vending.CHECK_LICENSE\' permission."),
    INVALID_PUBLIC_KEY("Application public key is invalid."),
    NOT_MARKET_MANAGED("Not market managed error.");

    private final String text;

    PiracyCheckerError(final String text) {
        this.text = text;
    }

    public static PiracyCheckerError getCheckerErrorFromCode(int errorCode) {
        switch (errorCode) {
            case 1212:
                return PiracyCheckerError.INVALID_PACKAGE_NAME;
            case 2373:
                return PiracyCheckerError.NON_MATCHING_UID;
            case 96431:
                return PiracyCheckerError.NOT_MARKET_MANAGED;
            case 42619:
                return PiracyCheckerError.CHECK_IN_PROGRESS;
            case 512643:
                return PiracyCheckerError.INVALID_PUBLIC_KEY;
            case 6321:
                return PiracyCheckerError.MISSING_PERMISSION;
            default:
                return PiracyCheckerError.UNKNOWN;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

}