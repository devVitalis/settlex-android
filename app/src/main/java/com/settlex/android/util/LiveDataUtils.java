package com.settlex.android.util;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

// TODO: Remove/Delete observeOnce
public final class LiveDataUtils {
    private LiveDataUtils() {
        // Prevent instantiation
    }

    // Observe LiveData once and auto-remove
    public static <T> void observeOnce(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
    }
}
