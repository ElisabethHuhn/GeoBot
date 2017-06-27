package com.asc.msigeosystems.geobot;

/**
 * Created by Elisabeth Huhn on 6/25/2017.
 * This exists just to check the intermediate results of a conversion
 */

class GBLamberConversionCheck {

    double mLatitude;
    double mLongitude;
    double mDeltaLatitude;
    double mu;
    double mR;
    double mDeltaLongitude;
    double mCovergenceAngle;
    double mEPrime;
    double mEasting;
    double mNPrime;
    double mNorthing;
    double mScaleFactor;

    double getLatitude() {
        return mLatitude;
    }
    void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    double getLongitude() {
        return mLongitude;
    }
    void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    double getDeltaLatitude() {
        return mDeltaLatitude;
    }
    void setDeltaLatitude(double deltaLatitude) {
        mDeltaLatitude = deltaLatitude;
    }

    double getMu() {
        return mu;
    }
    void setMu(double mu) {
        this.mu = mu;
    }

    double getR() {
        return mR;
    }
    void setR(double r) {
        mR = r;
    }

    double getDeltaLongitude() {
        return mDeltaLongitude;
    }
    void setDeltaLongitude(double deltaLongitude) {
        mDeltaLongitude = deltaLongitude;
    }

    double getCovergenceAngle() {
        return mCovergenceAngle;
    }
    void setCovergenceAngle(double covergenceAngle) {
        mCovergenceAngle = covergenceAngle;
    }

    double getEPrime() {
        return mEPrime;
    }
    void setEPrime(double EPrime) {
        mEPrime = EPrime;
    }

    double getEasting() {
        return mEasting;
    }
    void setEasting(double easting) {
        mEasting = easting;
    }

    double getNPrime() {
        return mNPrime;
    }
    void setNPrime(double NPrime) {
        mNPrime = NPrime;
    }

    double getNorthing() {
        return mNorthing;
    }
    void setNorthing(double northing) {
        mNorthing = northing;
    }

    double getScaleFactor() {
        return mScaleFactor;
    }
    void setScaleFactor(double scaleFactor) {
        mScaleFactor = scaleFactor;
    }
}
