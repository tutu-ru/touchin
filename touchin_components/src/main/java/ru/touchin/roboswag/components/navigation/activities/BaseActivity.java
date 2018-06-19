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

package ru.touchin.roboswag.components.navigation.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import ru.touchin.roboswag.components.utils.BaseLifecycleBindable;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.Optional;
import ru.touchin.roboswag.core.utils.pairs.HalfNullablePair;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 08/03/2016.
 * Base activity to use in components repository.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseActivity extends AppCompatActivity
        implements LifecycleBindable {

    private static final String ACTIVITY_RESULT_CODE_EXTRA = "ACTIVITY_RESULT_CODE_EXTRA";
    private static final String ACTIVITY_RESULT_DATA_EXTRA = "ACTIVITY_RESULT_DATA_EXTRA";

    @NonNull
    private final ArrayList<OnBackPressedListener> onBackPressedListeners = new ArrayList<>();
    @NonNull
    private final BaseLifecycleBindable baseLifecycleBindable = new BaseLifecycleBindable();
    private boolean resumed;

    @NonNull
    private final BehaviorSubject<Optional<HalfNullablePair<Integer, Intent>>> lastActivityResult
            = BehaviorSubject.create(new Optional<HalfNullablePair<Integer, Intent>>(null));

    /**
     * Returns if activity resumed.
     *
     * @return True if resumed.
     */
    public boolean isActuallyResumed() {
        return resumed;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onCreate();
        restoreLastActivityResult(savedInstanceState);
    }

    private void restoreLastActivityResult(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        lastActivityResult.onNext(new Optional<>(new HalfNullablePair<>(savedInstanceState.getInt(ACTIVITY_RESULT_CODE_EXTRA),
                savedInstanceState.getParcelable(ACTIVITY_RESULT_DATA_EXTRA))));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this) + " requestCode: " + requestCode + "; resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            lastActivityResult.onNext(new Optional<>(new HalfNullablePair<>(requestCode, data)));
        }
    }

    /**
     * Observes activity result by request code coming from {@link #onActivityResult(int, int, Intent)}
     *
     * @param requestCode Unique code to identify activity result;
     * @return {@link Observable} which will emit data (Intents) from other activities (endlessly).
     */
    @NonNull
    public Observable<Intent> observeActivityResult(final int requestCode) {
        return lastActivityResult
                .concatMap(optional -> {
                    final HalfNullablePair<Integer, Intent> activityResult = optional.get();
                    if (activityResult == null || activityResult.getFirst() != requestCode) {
                        return Observable.empty();
                    }
                    return Observable.just(activityResult.getSecond() != null ? activityResult.getSecond() : new Intent())
                            .doOnNext(result -> lastActivityResult.onNext(new Optional<>(null)));
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        resumed = true;
        baseLifecycleBindable.onResume();
    }

    @Override
    protected void onPause() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        resumed = false;
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle stateToSave) {
        super.onSaveInstanceState(stateToSave);
        baseLifecycleBindable.onSaveInstanceState();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        final HalfNullablePair<Integer, Intent> activityResult = lastActivityResult.getValue().get();
        if (activityResult != null) {
            stateToSave.putInt(ACTIVITY_RESULT_CODE_EXTRA, activityResult.getFirst());
            if (activityResult.getSecond() != null) {
                stateToSave.putParcelable(ACTIVITY_RESULT_DATA_EXTRA, activityResult.getSecond());
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    @Override
    protected void onStop() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onDestroy();
        super.onDestroy();
    }

    /**
     * Hides device keyboard that is showing over {@link Activity}.
     * Do NOT use it if keyboard is over {@link android.app.Dialog} - it won't work as they have different {@link Activity#getWindow()}.
     */
    public void hideSoftInput() {
        if (getCurrentFocus() == null) {
            return;
        }
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        getWindow().getDecorView().requestFocus();
    }

    /**
     * Shows device keyboard over {@link Activity} and focuses {@link View}.
     * Do NOT use it if keyboard is over {@link android.app.Dialog} - it won't work as they have different {@link Activity#getWindow()}.
     * Do NOT use it if you are not sure that view is already added on screen.
     * Better use it onStart of element if view is part of it or onConfigureNavigation if view is part of navigation.
     *
     * @param view View to get focus for input from keyboard.
     */
    public void showSoftInput(@NonNull final View view) {
        view.requestFocus();
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Return the color value associated with a particular resource ID.
     * Starting in {@link android.os.Build.VERSION_CODES#M}, the returned
     * color will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return int A single color value in the form 0xAARRGGBB.
     */
    @ColorInt
    public int getColorCompat(@ColorRes final int resId) {
        return ContextCompat.getColor(this, resId);
    }

    /**
     * Returns a drawable object associated with a particular resource ID.
     * Starting in {@link android.os.Build.VERSION_CODES#LOLLIPOP}, the
     * returned drawable will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return Drawable An object that can be used to draw this resource.
     */
    @NonNull
    public Drawable getDrawableCompat(@DrawableRes final int resId) {
        return ContextCompat.getDrawable(this, resId);
    }

    public void addOnBackPressedListener(@NonNull final OnBackPressedListener onBackPressedListener) {
        onBackPressedListeners.add(onBackPressedListener);
    }

    public void removeOnBackPressedListener(@NonNull final OnBackPressedListener onBackPressedListener) {
        onBackPressedListeners.remove(onBackPressedListener);
    }

    @Override
    public void onBackPressed() {
        for (final OnBackPressedListener onBackPressedListener : onBackPressedListeners) {
            if (onBackPressedListener.onBackPressed()) {
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            supportFinishAfterTransition();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
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

    @SuppressWarnings("CPD-END")
    /*
     * Interface to be implemented for someone who want to intercept device back button pressing event.
     */
    public interface OnBackPressedListener {

        /**
         * Calls when user presses device back button.
         *
         * @return True if it is processed by this object.
         */
        boolean onBackPressed();

    }

}
