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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import ru.touchin.templates.socket.SocketEvent;
import ru.touchin.templates.socket.SocketMessageHandler;
import ru.touchin.templates.ApiModel;

/**
 * Created by Gavriil Sitnikov on 01/09/2016.
 * Socket event that response JSON objects and could be parsed by Google Json lib.
 *
 * @param <TMessage> Type of message.
 */
public class GoogleJsonSocketEvent<TMessage> extends SocketEvent<TMessage> {

    public GoogleJsonSocketEvent(@NonNull final String name, @NonNull final Class<TMessage> clz,
                                 @Nullable final SocketMessageHandler<TMessage> eventDataHandler) {
        super(name, clz, eventDataHandler);
    }

    @NonNull
    @Override
    public TMessage parse(@NonNull final byte[] data) throws IOException {
        final TMessage message = GoogleJsonModel.DEFAULT_JSON_FACTORY.createJsonParser(new String(data, "UTF-8")).parseAndClose(getMessageClass());
        if (message instanceof ApiModel) {
            ((ApiModel) message).validate();
        }
        return message;
    }

}
