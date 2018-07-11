package ru.tutu.avia.tutucities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class AviaDatabase {
    private static final int BUFFER_SIZE = 32768;
    private static final String DATABASE_NAME = "avia.db";

    @NonNull
    private final Observable<SQLiteDatabase> databaseObservable;
    @NonNull
    private final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    public AviaDatabase(@NonNull final Context context) {
        databaseObservable = Observable
                .fromCallable(() -> {
                    if (!context.getDatabasePath(DATABASE_NAME).exists()) {
                        try {
                            copyDatabase(context);
                        } catch (final IOException exception) {
                            return null;
                        }
                    }
                    return SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);
                })
                .switchMap(sqLiteDatabase -> Observable
                        .<SQLiteDatabase>create(subscriber -> subscriber.onNext(sqLiteDatabase))
                        .doOnUnsubscribe(() -> {
                            if (sqLiteDatabase != null) {
                                sqLiteDatabase.close();
                            }
                        }))
                .subscribeOn(scheduler)
                .onErrorReturn(throwable -> null)
                .replay(1)
                .refCount();
    }

    private void copyDatabase(@NonNull final Context context) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getResources().openRawResource(R.raw.avia);
            outputStream = new FileOutputStream(context.getDatabasePath(DATABASE_NAME));
            final byte[] buffer = new byte[BUFFER_SIZE];
            int length = inputStream.read(buffer);
            while (length > 0) {
                outputStream.write(buffer, 0, length);
                length = inputStream.read(buffer);
            }

            outputStream.flush();
        } finally {
            if (outputStream != null) {
                closeStreamQuietly(outputStream);
            }
            if (inputStream != null) {
                closeStreamQuietly(inputStream);
            }
        }
    }

    private void closeStreamQuietly(@NonNull final Closeable stream) {
        try {
            stream.close();
        } catch (final IOException exception) {
            //nothing
        }
    }

    @NonNull
    public Observable<List<QueryResult>> getDestinationsByCityId(final int cityId) {
        return getDestinationsQueryResult(
                "select rm_city.id as cityId, rm_city.name_rus as city, rm_city.iata as cityCode, rm_city.gsga as cityCodeRus, "
                + "rm_country.id as countryId, rm_country.short_name as country, rm_country.iso as countryCode from rm_city "
                + "inner join rm_country on (rm_country.id = rm_city.country_id) "
                + "inner join rm_represent_city on (rm_city.id = rm_represent_city.object_id) "
                + "inner join rm_airport on (rm_airport.city_id = rm_city.id) "
                + "where (rm_city.id == ?) group by rm_city.id "
                + "order by rm_city.weight desc",
                "" + cityId);
    }

    @NonNull
    public Observable<List<QueryResult>> getDestinations(@NonNull final String query) {
        return getDestinationsQueryResult(
                "select rm_city.id as cityId, rm_city.name_rus as city, rm_city.iata as cityCode, rm_city.gsga as cityCodeRus, "
                + "rm_country.id as countryId, rm_country.short_name as country, rm_country.iso as countryCode from rm_city "
                + "inner join rm_country on (rm_country.id = rm_city.country_id) "
                + "inner join rm_represent_city on (rm_city.id = rm_represent_city.object_id) "
                + "inner join rm_airport on (rm_airport.city_id = rm_city.id) "
                + "where (rm_represent_city.lower_value match ?) group by rm_city.id "
                + "order by rm_city.weight desc, rm_city.lower_name_rus like ? desc,rm_city.name_rus limit 20",
                "+" + query + "*", query + "%");
    }

    @NonNull
    public Observable<List<QueryResult>> getPopularDestinations(@NonNull final String... popularCities) {
        final StringBuilder args = new StringBuilder();
        for (int i = 0; i < popularCities.length; i++) {
            if (i > 0) {
                args.append(',');
            }
            args.append('?');
        }
        return getDestinationsQueryResult(
                "select rm_city.id as cityId, rm_city.name_rus as city, rm_city.iata as cityCode, rm_city.gsga as cityCodeRus, "
                + "rm_country.id as countryId, rm_country.short_name as country, rm_country.iso as countryCode from rm_city "
                + "inner join rm_country on (rm_country.id = rm_city.country_id) "
                + "inner join rm_represent_city on (rm_city.id = rm_represent_city.object_id) "
                + "inner join rm_airport on (rm_airport.city_id = rm_city.id) "
                + "where rm_city.name_rus in (" + args.toString() + ") "
                + "group by rm_city.id "
                + "order by rm_city.weight desc",
                popularCities);
    }

    @NonNull
    private Observable<List<QueryResult>> getDestinationsQueryResult(@NonNull final String sql, @NonNull final String... selectionArgs) {
        return databaseObservable
                .map(database -> {
                    if (database == null) {
                        return Collections.<QueryResult>emptyList();
                    }
                    final Cursor cursor = database.rawQuery(sql, selectionArgs);

                    final List<QueryResult> results = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        results.add(new QueryResult(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                                cursor.getString(4), cursor.getString(5), cursor.getString(6)));
                    }
                    cursor.close();
                    return results;
                })
                .subscribeOn(scheduler)
                .first()
                .observeOn(Schedulers.computation());
    }

}
