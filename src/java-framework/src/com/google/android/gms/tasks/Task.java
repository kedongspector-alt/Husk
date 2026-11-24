package com.google.android.gms.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/** Minimal Task<T> on Husk's boot classpath. Apps using the Play Integrity library
 *  also pull in Task from the APK, but Husk's copy wins because it loads first.
 *  Just enough surface for the common listeners most apps use. */
public abstract class Task<T> {
    public abstract boolean isComplete();
    public abstract boolean isSuccessful();
    public abstract boolean isCanceled();
    public abstract T getResult();
    public abstract Exception getException();

    public abstract Task<T> addOnSuccessListener(OnSuccessListener<? super T> l);
    public abstract Task<T> addOnSuccessListener(Executor e, OnSuccessListener<? super T> l);
    public abstract Task<T> addOnFailureListener(OnFailureListener l);
    public abstract Task<T> addOnFailureListener(Executor e, OnFailureListener l);
    public abstract Task<T> addOnCompleteListener(OnCompleteListener<T> l);
    public abstract Task<T> addOnCompleteListener(Executor e, OnCompleteListener<T> l);
    public abstract Task<T> addOnCanceledListener(OnCanceledListener l);
}
