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

package ru.touchin.roboswag.components.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Objects of such class controls creation and binding of specific type of RecyclerView's ViewHolders.
 * Default {@link #getItemViewType} is generating on construction of object.
 *
 * @param <TViewHolder> Type of {@link BindableViewHolder} of delegate.
 */
@SuppressWarnings("PMD.TooManyMethods")
//TooManyMethods: it's ok
public abstract class AdapterDelegate<TViewHolder extends BindableViewHolder> implements LifecycleBindable {

    @NonNull
    private final LifecycleBindable parentLifecycleBindable;
    private final int defaultItemViewType;

    public AdapterDelegate(@NonNull final LifecycleBindable parentLifecycleBindable) {
        this.parentLifecycleBindable = parentLifecycleBindable;
        this.defaultItemViewType = UiUtils.OfViews.generateViewId();
    }

    /**
     * Returns parent {@link LifecycleBindable} that this delegate created from (e.g. Activity or ViewController).
     *
     * @return Parent {@link LifecycleBindable}.
     */
    @NonNull
    public LifecycleBindable getParentLifecycleBindable() {
        return parentLifecycleBindable;
    }

    /**
     * Unique ID of AdapterDelegate.
     *
     * @return Unique ID.
     */
    public int getItemViewType() {
        return defaultItemViewType;
    }

    /**
     * Creates ViewHolder to bind item to it later.
     *
     * @param parent Container of ViewHolder's view.
     * @return New ViewHolder.
     */
    @NonNull
    public abstract TViewHolder onCreateViewHolder(@NonNull final ViewGroup parent);

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable) {
        return parentLifecycleBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return parentLifecycleBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Observable<T> observable,
                                      @NonNull final Action1<T> onNextAction,
                                      @NonNull final Action1<Throwable> onErrorAction,
                                      @NonNull final Action0 onCompletedAction) {
        return parentLifecycleBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single) {
        return parentLifecycleBindable.untilStop(single);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        return parentLifecycleBindable.untilStop(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilStop(@NonNull final Single<T> single,
                                      @NonNull final Action1<T> onSuccessAction,
                                      @NonNull final Action1<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilStop(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable) {
        return parentLifecycleBindable.untilStop(completable);
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable, @NonNull final Action0 onCompletedAction) {
        return parentLifecycleBindable.untilStop(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Subscription untilStop(@NonNull final Completable completable,
                                  @NonNull final Action0 onCompletedAction,
                                  @NonNull final Action1<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilStop(completable, onCompletedAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable) {
        return parentLifecycleBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable, @NonNull final Action1<T> onNextAction) {
        return parentLifecycleBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Observable<T> observable,
                                         @NonNull final Action1<T> onNextAction,
                                         @NonNull final Action1<Throwable> onErrorAction,
                                         @NonNull final Action0 onCompletedAction) {
        return parentLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single) {
        return parentLifecycleBindable.untilDestroy(single);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single, @NonNull final Action1<T> onSuccessAction) {
        return parentLifecycleBindable.untilDestroy(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Subscription untilDestroy(@NonNull final Single<T> single,
                                         @NonNull final Action1<T> onSuccessAction,
                                         @NonNull final Action1<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilDestroy(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable) {
        return parentLifecycleBindable.untilDestroy(completable);
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable, @NonNull final Action0 onCompletedAction) {
        return parentLifecycleBindable.untilDestroy(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Subscription untilDestroy(@NonNull final Completable completable,
                                     @NonNull final Action0 onCompletedAction,
                                     @NonNull final Action1<Throwable> onErrorAction) {
        return parentLifecycleBindable.untilDestroy(completable, onCompletedAction, onErrorAction);
    }

}
