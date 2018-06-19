/*
 *  Copyright (c) 2017 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.roboswag.components.views;


import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import ru.touchin.roboswag.components.utils.BaseLifecycleBindable;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;


/**
 * Created by Gavriil Sitnikov on 18/05/17.
 * FrameLayout that realizes LifecycleBindable interface.
 */
@SuppressWarnings({"CPD-START", "PMD.TooManyMethods"})
public class LifecycleView extends FrameLayout implements LifecycleBindable {

    @NonNull
    private final BaseLifecycleBindable baseLifecycleBindable;
    private boolean created;
    private boolean started;

    public LifecycleView(@NonNull final Context context) {
        super(context);
        baseLifecycleBindable = new BaseLifecycleBindable();
    }

    public LifecycleView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        baseLifecycleBindable = new BaseLifecycleBindable();
    }

    public LifecycleView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        baseLifecycleBindable = new BaseLifecycleBindable();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onCreate();
        if (!started && getWindowSystemUiVisibility() == VISIBLE) {
            onStart();
        }
    }

    /**
     * Calls when view attached to window and ready to use.
     */
    protected void onCreate() {
        created = true;
        baseLifecycleBindable.onCreate();
    }

    /**
     * Calls when view's window showed or state restored.
     */
    protected void onStart() {
        started = true;
        baseLifecycleBindable.onStart();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Parcelable state) {
        super.onRestoreInstanceState(state);
        if (created && !started) {
            onStart();
        }
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        started = false;
        baseLifecycleBindable.onSaveInstanceState();
        return super.onSaveInstanceState();
    }

    /**
     * Calls when view's window hided or state saved.
     */
    protected void onStop() {
        started = false;
        baseLifecycleBindable.onStop();
    }

    /**
     * Calls when view detached from window.
     */
    protected void onDestroy() {
        if (started) {
            onStop();
        }
        created = false;
        baseLifecycleBindable.onDestroy();
    }

    @Override
    protected void onDetachedFromWindow() {
        onDestroy();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(final int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            if (created && !started) {
                baseLifecycleBindable.onStart();
            }
        } else if (started) {
            baseLifecycleBindable.onStop();
        }
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single) {
        return baseLifecycleBindable.untilStop(single);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        return baseLifecycleBindable.untilStop(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single,
                                      @NonNull final Action1<T> onSuccessAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable) {
        return baseLifecycleBindable.untilStop(completable);
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable, @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilStop(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable,
                                  @NonNull final Action0 onCompletedAction,
                                  @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(completable, onCompletedAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single) {
        return baseLifecycleBindable.untilDestroy(single);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        return baseLifecycleBindable.untilDestroy(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single,
                                         @NonNull final Action1<T> onSuccessAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable) {
        return baseLifecycleBindable.untilDestroy(completable);
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable, @NonNull final Action0 onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable,
                                     @NonNull final Action0 onCompletedAction,
                                     @NonNull final Action1<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(completable, onCompletedAction, onErrorAction);
    }

}
