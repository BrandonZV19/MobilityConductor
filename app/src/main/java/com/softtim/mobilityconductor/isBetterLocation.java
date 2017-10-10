package com.softtim.mobilityconductor;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

/**
 * Created by softtim on 8/27/16.
 */
public class isBetterLocation {
    SharedPreferences preferences;

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {



        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 14000;
        boolean isSignificantlyOlder = timeDelta < 14000;
        boolean isNewer = timeDelta > 14000;
        //Log.e("isBetterLocation","location="+location.getTime()+" currentBestLocation="+currentBestLocation.getTime()+" timeDelta="+timeDelta+" ");

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            Log.e("isBetterLocation","isSignificantlyNewer");
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 10;
        //Log.e("isBetterLocation","location="+location.getAccuracy()+" currentBestLocation="+currentBestLocation.getAccuracy()+" accuracyDelta="+accuracyDelta+" ");


        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            Log.e("isBetterLocation","isMoreAccurate");
            return true;
        } else if (isNewer && !isLessAccurate) {
            Log.e("isBetterLocation","isNewer && !isLessAccurate");
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            Log.e("isBetterLocation","isNewer && !isSignificantlyLessAccurate && isFromSameProvider)");
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}