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

package ru.touchin.templates.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Actions;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 12/05/16.
 * Object which is containing logic of sending messages as queue one-by-one.
 *
 * @param <TOutgoingMessage> Type of messages to send.
 */
public abstract class Chat<TOutgoingMessage> {

    private static final long RETRY_SENDING_DELAY = TimeUnit.SECONDS.toMillis(5);

    @NonNull
    private final ObservableList<TOutgoingMessage> sendingMessages = new ObservableList<>();
    @NonNull
    private final PublishSubject<?> retrySendingRequest = PublishSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isSendingInError = BehaviorSubject.create(false);
    @NonNull
    private final Scheduler sendingScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    @NonNull
    private final Observable<?> messagesToSendObservable;
    @Nullable
    private Subscription activationSubscription;

    public Chat(@Nullable final Collection<TOutgoingMessage> messagesToSend) {
        if (messagesToSend != null) {
            sendingMessages.addAll(messagesToSend);
        }

        messagesToSendObservable = sendingMessages.observeItems()
                .first()
                .concatMap(initialMessages -> {
                    final List<TOutgoingMessage> reversedMessages = new ArrayList<>(initialMessages);
                    Collections.reverse(reversedMessages);
                    return Observable.from(reversedMessages)
                            .concatWith(sendingMessages.observeChanges().concatMap(changes ->
                                    changes.getInsertedItems().isEmpty() ? Observable.empty() : Observable.from(changes.getInsertedItems())))
                            //observe on some scheduler?
                            .flatMap(message -> internalSendMessage(message).toObservable());
                });
    }

    /**
     * Returns {@link Observable} to check if sending have failed so it is in error state and user have to retry send messages.
     *
     * @return {@link Observable} to check if sending have failed.
     */
    @NonNull
    public Observable<Boolean> observeIsSendingInError() {
        return isSendingInError.distinctUntilChanged();
    }

    /**
     * Returns {@link ObservableCollection} of currently sending messages.
     *
     * @return Collection of sending messages.
     */
    @NonNull
    public ObservableCollection<TOutgoingMessage> getSendingMessages() {
        return sendingMessages;
    }

    /**
     * Returns {@link Observable} to determine if message is in cache stored on disk.
     * It is needed to not send message which is already loaded from server and cached.
     *
     * @param message Message to check if it is in cache;
     * @return {@link Observable} which is checking if message is in cache.
     */
    @NonNull
    protected abstract Observable<Boolean> isMessageInCacheObservable(@NonNull final TOutgoingMessage message);

    /**
     * Returns {@link Observable} to determine if message is in actually loaded messages.
     * It is needed to not send message which is already loaded from server and showing to user at this moment.
     *
     * @param message Message to check if it is in actual data;
     * @return {@link Observable} which is checking if message is in actual data.
     */
    @NonNull
    protected abstract Observable<Boolean> isMessageInActualObservable(@NonNull final TOutgoingMessage message);

    /**
     * Method to create {@link Observable} which is sending message to server.
     *
     * @param message Message to send;
     * @return {@link Observable} to send message.
     */
    @NonNull
    protected abstract Observable<?> createSendMessageObservable(@NonNull final TOutgoingMessage message);

    /**
     * Method to start sending message.
     *
     * @param message Message to send.
     */
    public void sendMessage(@NonNull final TOutgoingMessage message) {
        sendingMessages.add(0, message);
    }

    /**
     * Method to start sending collection of messages.
     *
     * @param messages Messages to send.
     */
    public void sendMessages(@NonNull final Collection<TOutgoingMessage> messages) {
        sendingMessages.addAll(0, messages);
    }

    /**
     * Activates chat so it will start sending messages.
     */
    public void activate() {
        if (activationSubscription != null) {
            Lc.assertion("Chat already activated");
            return;
        }
        activationSubscription = messagesToSendObservable.subscribe();
    }

    /**
     * Method to retry send messages.
     */
    public void retrySend() {
        retrySendingRequest.onNext(null);
    }

    /**
     * Method to cancel sending current message.
     */
    @NonNull
    public Observable<?> observeCancelEvent(@NonNull final TOutgoingMessage message) {
        return Observable.never();
    }

    /**
     * Deactivates chat so it will stop sending messages.
     */
    public void deactivate() {
        if (activationSubscription == null) {
            Lc.assertion("Chat not activated yet");
            return;
        }
        activationSubscription.unsubscribe();
        activationSubscription = null;
    }

    @NonNull
    private Completable internalSendMessage(@NonNull final TOutgoingMessage message) {
        final SubscriptionHolder subscriptionHolder = new SubscriptionHolder();
        return Completable
                .create(subscriber -> {
                    subscriptionHolder.subscription = sendingScheduler.createWorker().schedule(() -> {
                        final CountDownLatch blocker = new CountDownLatch(1);
                        final Subscription sendSubscription = Observable
                                .combineLatest(isMessageInCacheObservable(message), isMessageInActualObservable(message),
                                        (messageInCache, messageInActual) -> !messageInCache && !messageInActual)
                                .subscribeOn(Schedulers.computation())
                                .first()
                                .switchMap(shouldSendMessage -> shouldSendMessage
                                        ? createSendMessageObservable(message).ignoreElements() : Observable.empty())
                                .takeUntil(observeCancelEvent(message))
                                .retryWhen(attempts -> attempts.switchMap(ignored -> {
                                    isSendingInError.onNext(true);
                                    return Observable
                                            .merge(retrySendingRequest, Observable.timer(RETRY_SENDING_DELAY, TimeUnit.MILLISECONDS))
                                            .first()
                                            .doOnCompleted(() -> isSendingInError.onNext(false));
                                }))
                                .doOnUnsubscribe(blocker::countDown)
                                .subscribe(Actions.empty(), Lc::assertion, () -> sendingMessages.remove(message));
                        try {
                            blocker.await();
                        } catch (final InterruptedException exception) {
                            sendSubscription.unsubscribe();
                        }
                        subscriber.onCompleted();
                    });
                })
                .doOnUnsubscribe(() -> {
                    if (subscriptionHolder.subscription != null && !subscriptionHolder.subscription.isUnsubscribed()) {
                        subscriptionHolder.subscription.unsubscribe();
                    }
                });
    }

    private class SubscriptionHolder {

        @Nullable
        private Subscription subscription;

    }

}
