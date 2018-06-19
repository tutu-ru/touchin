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

package ru.touchin.templates;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;

import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.utils.Logic;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ServiceBinder;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Completable;
import rx.CompletableSubscriber;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 10/01/17.
 * Base class of service to extends for Touch Instinct related projects.
 * It contains {@link Logic} and could bind to it through {@link #untilDestroy(Observable)} methods.
 *
 * @param <TLogic> Type of application's {@link Logic}.
 */
public abstract class TouchinService<TLogic extends Logic> extends Service {

    //it is needed to hold strong reference to logic
    private TLogic reference;
    @NonNull
    private final Handler postHandler = new Handler();
    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create();

    /**
     * It should return specific class where all logic will be.
     *
     * @return Returns class of specific {@link Logic}.
     */
    @NonNull
    protected abstract Class<TLogic> getLogicClass();

    @Override
    public void onCreate() {
        super.onCreate();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        postHandler.post(() -> isCreatedSubject.onNext(true));
    }

    /**
     * Returns (and creates if needed) application's logic.
     *
     * @return Object which represents application's logic.
     */
    @NonNull
    protected TLogic getLogic() {
        synchronized (ViewControllerActivity.class) {
            if (reference == null) {
                reference = Logic.getInstance(this, getLogicClass());
            }
        }
        return reference;
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable {@link Observable} to subscribe until onDestroy;
     * @param <T>        Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        return untilDestroy(observable, Actions.empty(), getActionThrowableForAssertion(Lc.getCodePoint(this, 1)), Actions.empty());
    }

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable and calls onNextAction on every emitted item.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   {@link Observable} to subscribe until onDestroy;
     * @param onNextAction Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param <T>          Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction) {
        return untilDestroy(observable, onNextAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)), Actions.empty());
    }

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable and calls onNextAction and onErrorAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    {@link Observable} to subscribe until onDestroy;
     * @param onNextAction  Action which will raise on every {@link Subscriber#onNext(Object)} item;
     * @param onErrorAction Action which will raise on every {@link Subscriber#onError(Throwable)} throwable;
     * @param <T>           Type of emitted by observable items;
     * @return {@link Subscription} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return untilDestroy(observable, onNextAction, onErrorAction, Actions.empty());
    }

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
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
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return until(observable, isCreatedSubject.map(created -> !created), onNextAction, onErrorAction, onCompletedAction);
    }

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to single.
     * Don't forget to process errors if single can emit them.
     *
     * @param single {@link Single} to subscribe until onDestroy;
     * @param <T>    Type of emitted by single items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Subscription untilDestroy(@NonNull final Single<T> single) {
        return untilDestroy(single, Actions.empty(), getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to single and calls onSuccessAction on emitted item.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on {@link SingleSubscriber#onSuccess(Object)} item;
     * @param <T>             Type of emitted by single items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Subscription untilDestroy(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        return untilDestroy(single, onSuccessAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to single and calls onSuccessAction and onErrorAction on single events.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on {@link SingleSubscriber#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link SingleSubscriber#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by single items;
     * @return {@link Subscription} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Subscription untilDestroy(@NonNull final Single<T> single,
                                         @NonNull final Action1<T> onSuccessAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return until(single.toObservable(), isCreatedSubject.map(created -> !created), onSuccessAction, onErrorAction, Actions.empty());
    }

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to completable.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable {@link Completable} to subscribe until onDestroy;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    public Subscription untilDestroy(@NonNull final Completable completable) {
        return untilDestroy(completable, Actions.empty(), getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to completable and calls onCompletedAction on complete.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every {@link CompletableSubscriber#onCompleted()} item;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    public Subscription untilDestroy(@NonNull final Completable completable, @NonNull final Action0 onCompletedAction) {
        return untilDestroy(completable, onCompletedAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to completable and calls onCompletedAction and onErrorAction on completable events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Single} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on {@link CompletableSubscriber#onCompleted()} item;
     * @param onErrorAction     Action which will raise on every {@link CompletableSubscriber#onError(Throwable)} throwable;
     * @return {@link Subscription} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    public Subscription untilDestroy(@NonNull final Completable completable,
                                     @NonNull final Action0 onCompletedAction,
                                     @NonNull final Action1<Throwable> onErrorAction) {
        return until(completable.toObservable(), isCreatedSubject.map(created -> !created), Actions.empty(), onErrorAction, onCompletedAction);
    }

    @NonNull
    private <T> Subscription until(@NonNull final Observable<T> observable,
                                   @NonNull final Observable<Boolean> conditionSubject,
                                   @NonNull final Action1<T> onNextAction,
                                   @NonNull final Action1<Throwable> onErrorAction,
                                   @NonNull final Action0 onCompletedAction) {
        final Observable<T> actualObservable;
        if (onNextAction == Actions.empty() && onErrorAction == (Action1) Actions.empty() && onCompletedAction == Actions.empty()) {
            actualObservable = observable;
        } else {
            actualObservable = observable.observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(onCompletedAction)
                    .doOnNext(onNextAction)
                    .doOnError(onErrorAction);
        }

        return isCreatedSubject.first()
                .switchMap(created -> created ? actualObservable : Observable.empty())
                .takeUntil(conditionSubject.filter(condition -> condition))
                .onErrorResumeNext(throwable -> {
                    final boolean isRxError = throwable instanceof OnErrorThrowable;
                    if ((!isRxError && throwable instanceof RuntimeException)
                            || (isRxError && throwable.getCause() instanceof RuntimeException)) {
                        Lc.assertion(throwable);
                    }
                    return Observable.empty();
                })
                .subscribe();
    }

    @SuppressWarnings("CPD-END")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    @NonNull
    @Override
    public IBinder onBind(@NonNull final Intent intent) {
        return new ServiceBinder<>(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    @Override
    public void onDestroy() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        postHandler.removeCallbacksAndMessages(null);
        isCreatedSubject.onNext(false);
        super.onDestroy();
    }

    @NonNull
    private Action1<Throwable> getActionThrowableForAssertion(@NonNull final String codePoint) {
        return throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilDestroy at " + codePoint, throwable));
    }

}

