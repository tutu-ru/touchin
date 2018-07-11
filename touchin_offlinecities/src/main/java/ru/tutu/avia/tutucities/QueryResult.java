package ru.tutu.avia.tutucities;

import android.support.annotation.NonNull;

public class QueryResult {

    public static class Index {
        public static final int CITY_ID = 0;
        public static final int CITY = 1;
        public static final int CITY_CODE = 2;
        public static final int CITY_CODE_RUS = 3;
        public static final int COUNTRY_ID = 4;
        public static final int COUNTRY = 5;
        public static final int COUNTRY_CODE = 6;
    }

    private @NonNull String cityId;
    private @NonNull String city;
    private @NonNull String cityCode;
    private @NonNull String cityCodeRus;
    private @NonNull String countryId;
    private @NonNull String country;
    private @NonNull String countryCode;

    public QueryResult(@NonNull String cityId, @NonNull String city, @NonNull String cityCode, @NonNull String cityCodeRus,
                       @NonNull String countryId, @NonNull String country, @NonNull String countryCode) {
        this.cityId = cityId;
        this.city = city;
        this.cityCode = cityCode;
        this.cityCodeRus = cityCodeRus;
        this.countryId = countryId;
        this.country = country;
        this.countryCode = countryCode;
    }

    @NonNull
    public String getCityId() {
        return cityId;
    }

    @NonNull
    public String getCity() {
        return city;
    }

    @NonNull
    public String getCityCode() {
        return cityCode;
    }

    @NonNull
    public String getCityCodeRus() {
        return cityCodeRus;
    }

    @NonNull
    public String getCountryId() {
        return countryId;
    }

    @NonNull
    public String getCountry() {
        return country;
    }

    @NonNull
    public String getCountryCode() {
        return countryCode;
    }
}
