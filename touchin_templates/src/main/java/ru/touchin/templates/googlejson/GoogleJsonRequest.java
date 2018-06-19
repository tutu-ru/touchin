/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import okhttp3.Request;
import okhttp3.RequestBody;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.templates.requests.HttpRequest;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Request that responses data in Google JSON format
 */
public abstract class GoogleJsonRequest<T> extends HttpRequest<T> {

    protected GoogleJsonRequest(@NonNull final Class<T> responseResultType) {
        super(responseResultType);
    }

    @NonNull
    @Override
    protected T parse(@NonNull final Class<T> responseResultType, @NonNull final Charset charset, @NonNull final InputStream inputStream)
            throws IOException {
        return GoogleJsonModel.DEFAULT_JSON_FACTORY.createJsonObjectParser().parseAndClose(inputStream, charset, responseResultType);
    }

    @NonNull
    @Override
    protected Request.Builder createHttpRequest() throws IOException {
        switch (getRequestType()) {
            case POST:
                if (getBody() == null) {
                    Lc.assertion("Do you forget to implement getBody() class during POST-request?");
                    return super.createHttpRequest().get();
                }
                return super.createHttpRequest().post(getBody());
            case GET:
                return super.createHttpRequest().get();
            default:
                Lc.assertion("Unknown request type " + getRequestType());
                return super.createHttpRequest().get();
        }
    }

    /**
     * Type of request. Basically GET or POST.
     *
     * @return Request type.
     */
    @NonNull
    protected abstract RequestType getRequestType();

    @Nullable
    protected RequestBody getBody() throws IOException {
        return null;
    }

    protected enum RequestType {
        GET,
        POST
    }

}