/*
 *  Copyright (c) 2016 Touch Instinct
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.templates.googlejson;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.api.client.http.json.JsonHttpContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.touchin.roboswag.components.utils.storables.PreferenceStore;
import ru.touchin.roboswag.core.observables.storable.Converter;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.concrete.NonNullStorable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 23/08/2016.
 * Utility class to get {@link Storable} that is storing Google Json generated object into preferences.
 */
public final class GoogleJsonPreferences {

    @NonNull
    public static <T> Storable<String, T, String> jsonStorable(@NonNull final String name,
                                                               @NonNull final Class<T> jsonClass,
                                                               @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<String, T, String>(name, jsonClass, String.class, new PreferenceStore<>(preferences), new JsonConverter<>())
                .setObserveStrategy(Storable.ObserveStrategy.CACHE_ACTUAL_VALUE)
                .build();
    }

    @NonNull
    public static <T> NonNullStorable<String, T, String> jsonStorable(@NonNull final String name,
                                                                      @NonNull final Class<T> jsonClass,
                                                                      @NonNull final SharedPreferences preferences,
                                                                      @NonNull final T defaultValue) {
        return new Storable.Builder<String, T, String>(name, jsonClass, String.class, new PreferenceStore<>(preferences), new JsonConverter<>())
                .setObserveStrategy(Storable.ObserveStrategy.CACHE_ACTUAL_VALUE)
                .setDefaultValue(defaultValue)
                .build();
    }

    @NonNull
    public static <T> Storable<String, List<T>, String> jsonListStorable(@NonNull final String name,
                                                                         @NonNull final Class<T> jsonListItemClass,
                                                                         @NonNull final SharedPreferences preferences) {
        return new Storable.Builder<>(name, List.class, String.class, new PreferenceStore<>(preferences), new JsonListConverter<>(jsonListItemClass))
                .setObserveStrategy(Storable.ObserveStrategy.CACHE_ACTUAL_VALUE)
                .build();
    }

    @NonNull
    public static <T> NonNullStorable<String, List<T>, String> jsonListStorable(@NonNull final String name,
                                                                                @NonNull final Class<T> jsonListItemClass,
                                                                                @NonNull final SharedPreferences preferences,
                                                                                @NonNull final List<T> defaultValue) {
        return new Storable.Builder<>(name, List.class, String.class, new PreferenceStore<>(preferences), new JsonListConverter<>(jsonListItemClass))
                .setObserveStrategy(Storable.ObserveStrategy.CACHE_ACTUAL_VALUE)
                .setDefaultValue(defaultValue)
                .build();
    }

    private GoogleJsonPreferences() {
    }

    public static class JsonConverter<TJsonObject> implements Converter<TJsonObject, String> {

        @Nullable
        @Override
        public String toStoreObject(@NonNull final Type jsonObjectType, @NonNull final Type stringType,
                                    @Nullable final TJsonObject object) {
            if (object == null) {
                return null;
            }
            final JsonHttpContent content = new JsonHttpContent(GoogleJsonModel.DEFAULT_JSON_FACTORY, object);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                content.writeTo(byteArrayOutputStream);
            } catch (final IOException exception) {
                throw new ShouldNotHappenException(exception);
            }
            return new String(byteArrayOutputStream.toByteArray());
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public TJsonObject toObject(@NonNull final Type jsonObjectType, @NonNull final Type stringType, @Nullable final String storeValue) {
            if (storeValue == null) {
                return null;
            }
            try {
                return (TJsonObject) GoogleJsonModel.DEFAULT_JSON_FACTORY.createJsonParser(storeValue).parse(jsonObjectType, true);
            } catch (final IOException exception) {
                throw new ShouldNotHappenException(exception);
            }
        }

    }

    public static class JsonListConverter<T> extends JsonConverter<List<T>> {

        @NonNull
        private final Class<T> itemClass;

        public JsonListConverter(@NonNull final Class<T> itemClass) {
            super();
            this.itemClass = itemClass;
        }

        @Nullable
        @Override
        public List<T> toObject(@NonNull final Type jsonObjectType, @NonNull final Type stringType, @Nullable final String storeValue) {
            if (storeValue == null) {
                return null;
            }
            try {
                return new ArrayList<>(GoogleJsonModel.DEFAULT_JSON_FACTORY.createJsonParser(storeValue).parseArray(ArrayList.class, itemClass));
            } catch (final IOException exception) {
                throw new ShouldNotHappenException(exception);
            }
        }

    }

}
