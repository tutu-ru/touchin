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

package ru.touchin.roboswag.components.utils;

import android.support.annotation.NonNull;

import rx.Completable;
import rx.CompletableSubscriber;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Gavriil Sitnikov on 15/04/16.
 * Interface that should be implemented by lifecycle-based elements ({@link android.app.Activity}, {@link android.support.v4.app.Fragment} etc.)
 * to not manually manage subscriptions.
 * Use {@link #untilStop(Observable)} method to subscribe to observable where you want and unsubscribe onStop.
 * Use {@link #untilDestroy(Observable)} method to subscribe to observable where you want and unsubscribe onDestroy.
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface LifecycleBindable {

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable {@link Observable} to subscribe until onStop;
     * @param <T>        Type of emitted by observable items;
     * @return {@link Subscription} which will unsubscribes from observable onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Observable<T> observable);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction on every emitted item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   {@link Observable} to subscribe until onStop;
     * @param onNextAction Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param <T>          Type of emitted by observable items;
     * @return {@link Subscription} which will unsubscribes from observable onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Observable<T> observable, @NonNull Action1<T> onNextAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction and onErrorAction on observable events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    {@link Observable} to subscribe until onStop;
     * @param onNextAction  Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param onErrorAction Action which will raise on every {@link Subscriber#onError(Throwable)} throwable;
     * @param <T>           Type of emitted by observable items;
     * @return {@link Subscription} which will unsubscribes from observable onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Observable<T> observable, @NonNull Action1<T> onNextAction, @NonNull Action1<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable        {@link Observable} to subscribe until onStop;
     * @param onNextAction      Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param onErrorAction     Action which will raise on every {@link Subscriber#onError(Throwable)} throwable;
     * @param onCompletedAction Action which will raise at {@link Subscriber#onCompleted()} on completion of observable;
     * @param <T>               Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Observable<T> observable,
                               @NonNull Action1<T> onNextAction, @NonNull Action1<Throwable> onErrorAction, @NonNull Action0 onCompletedAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single {@link Single} to subscribe until onStop;
     * @param <T>    Type of emitted by single item;
     * @return {@link Subscription} which will unsubscribes from single onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Single<T> single);

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single and calls onSuccessAction on the emitted item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onStop;
     * @param onSuccessAction Action which will raise on every {@link SingleSubscriber#onSuccess(Object)} item;
     * @param <T>             Type of emitted by single item;
     * @return {@link Subscription} which will unsubscribes from single onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Single<T> single, @NonNull Action1<T> onSuccessAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single and calls onSuccessAction and onErrorAction on single events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onStop;
     * @param onSuccessAction Action which will raise on every {@link SingleSubscriber#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link SingleSubscriber#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onStop.
     */
    @NonNull
    <T> Subscription untilStop(@NonNull Single<T> single, @NonNull Action1<T> onSuccessAction, @NonNull Action1<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable {@link Completable} to subscribe until onStop;
     * @return {@link Subscription} which will unsubscribes from completable onStop.
     */
    @NonNull
    Subscription untilStop(@NonNull Completable completable);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable and calls onCompletedAction on completable item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onStop;
     * @param onCompletedAction Action which will raise at {@link CompletableSubscriber#onCompleted()} on completion of observable;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onStop.
     */
    @NonNull
    Subscription untilStop(@NonNull Completable completable, @NonNull Action0 onCompletedAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable and calls onCompletedAction and onErrorAction on completable item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onStop;
     * @param onCompletedAction Action which will raise at {@link CompletableSubscriber#onCompleted()} on completion of observable;
     * @param onErrorAction     Action which will raise on every {@link CompletableSubscriber#onError(Throwable)} throwable;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onStop.
     */
    @NonNull
    Subscription untilStop(@NonNull Completable completable, @NonNull Action0 onCompletedAction, @NonNull Action1<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable {@link Observable} to subscribe until onDestroy;
     * @param <T>        Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Observable<T> observable);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction on every emitted item.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   {@link Observable} to subscribe until onDestroy;
     * @param onNextAction Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param <T>          Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Observable<T> observable, @NonNull Action1<T> onNextAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction and onErrorAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    {@link Observable} to subscribe until onDestroy;
     * @param onNextAction  Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param onErrorAction Action which will raise on every {@link Subscriber#onError(Throwable)} throwable;
     * @param <T>           Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Observable<T> observable, @NonNull Action1<T> onNextAction, @NonNull Action1<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable        {@link Observable} to subscribe until onDestroy;
     * @param onNextAction      Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param onErrorAction     Action which will raise on every {@link Subscriber#onError(Throwable)} throwable;
     * @param onCompletedAction Action which will raise at {@link Subscriber#onCompleted()} on completion of observable;
     * @param <T>               Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Observable<T> observable,
                                  @NonNull Action1<T> onNextAction, @NonNull Action1<Throwable> onErrorAction, @NonNull Action0 onCompletedAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single.
     * Don't forget to process errors if single can emit them.
     *
     * @param single {@link Single} to subscribe until onDestroy;
     * @param <T>    Type of emitted by single items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Single<T> single);

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single and calls onSuccessAction on every emitted item.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every {@link SingleSubscriber#onSuccess(Object)} item;
     * @param <T>             Type of emitted by single items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Single<T> single, @NonNull Action1<T> onSuccessAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single and calls onSuccessAction and onErrorAction on single events.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every {@link SingleSubscriber#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link SingleSubscriber#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by single items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Subscription untilDestroy(@NonNull Single<T> single, @NonNull Action1<T> onSuccessAction, @NonNull Action1<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable {@link Completable} to subscribe until onDestroy;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    Subscription untilDestroy(@NonNull Completable completable);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable and calls onCompletedAction on completable item.
     * Don't forget to process errors if single can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every {@link CompletableSubscriber#onCompleted()} item;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    Subscription untilDestroy(@NonNull Completable completable, @NonNull Action0 onCompletedAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable and calls onCompletedAction and onErrorAction on completable events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every {@link CompletableSubscriber#onCompleted()} item;
     * @param onErrorAction     Action which will raise on every {@link CompletableSubscriber#onError(Throwable)} throwable;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    Subscription untilDestroy(@NonNull Completable completable, @NonNull Action0 onCompletedAction, @NonNull Action1<Throwable> onErrorAction);

}