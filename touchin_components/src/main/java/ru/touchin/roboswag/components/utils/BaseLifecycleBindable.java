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

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 18/04/16.
 * Simple implementation of {@link LifecycleBindable}. Could be used to not implement interface but use such object inside.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class BaseLifecycleBindable implements LifecycleBindable {

    private static final String UNTIL_DESTROY_METHOD = "untilDestroy";
    private static final String UNTIL_STOP_METHOD = "untilStop";

    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isInAfterSaving = BehaviorSubject.create();

    /**
     * Call it on parent's onCreate method.
     */
    public void onCreate() {
        isCreatedSubject.onNext(true);
    }

    /**
     * Call it on parent's onStart method.
     */
    public void onStart() {
        isStartedSubject.onNext(true);
    }

    /**
     * Call it on parent's onResume method.
     * It is needed as sometimes onSaveInstanceState() calling after onPause() with no onStop call. So lifecycle object going in stopped state.
     * In that case onResume will be called after onSaveInstanceState so lifecycle object is becoming started.
     */
    public void onResume() {
        isInAfterSaving.onNext(false);
    }

    /**
     * Call it on parent's onSaveInstanceState method.
     */
    public void onSaveInstanceState() {
        isInAfterSaving.onNext(true);
    }

    /**
     * Call it on parent's onStop method.
     */
    public void onStop() {
        isStartedSubject.onNext(false);
    }

    /**
     * Call it on parent's onDestroy method.
     */
    public void onDestroy() {
        isCreatedSubject.onNext(false);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(observable, Actions.empty(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD), Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(observable, onNextAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD), Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return untilStop(observable, onNextAction, onErrorAction, Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return until(observable.delay(item -> isInAfterSaving.first(inAfterSaving -> !inAfterSaving)),
                isStartedSubject.map(started -> !started),
                onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(single, Actions.empty(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(single, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single,
                                      @NonNull final Action1<T> onSuccessAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return untilStop(single.toObservable(), onSuccessAction, onErrorAction, Actions.empty());
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(completable, Actions.empty(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable,
                                  @NonNull final Action0 onCompletedAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(completable, onCompletedAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable,
                                  @NonNull final Action0 onCompletedAction,
                                  @NonNull final Action1<Throwable> onErrorAction) {
        return untilStop(completable.toObservable(), Actions.empty(), onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(observable, Actions.empty(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD), Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(observable, onNextAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD), Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return untilDestroy(observable, onNextAction, onErrorAction, Actions.empty());
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return until(observable, isCreatedSubject.map(created -> !created), onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(single, Actions.empty(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(single, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single,
                                         @NonNull final Action1<T> onSuccessAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return until(single.toObservable(), isCreatedSubject.map(created -> !created), onSuccessAction, onErrorAction, Actions.empty());
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(completable, Actions.empty(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable, @NonNull final Action0 onCompletedAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(completable, onCompletedAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
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

    @NonNull
    private Action1<Throwable> getActionThrowableForAssertion(@NonNull final String codePoint, @NonNull final String method) {
        return throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on " + method + " at " + codePoint, throwable));
    }

}