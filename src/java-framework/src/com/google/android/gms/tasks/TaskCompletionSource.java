package com.google.android.gms.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/** The piece we actually need: a way to construct a completed Task<T> from our
 *  stub code. Minimal, thread-safe, dispatches listeners inline (fine for a stub). */
public final class TaskCompletionSource<T> {
    private final Impl<T> task = new Impl<>();

    public Task<T> getTask() { return task; }
    public void setResult(T result) { task.complete(result, null, false); }
    public void setException(Exception e) { task.complete(null, e, false); }
    public boolean trySetResult(T result) { return task.complete(result, null, true); }
    public boolean trySetException(Exception e) { return task.complete(null, e, true); }

    private static final class Impl<T> extends Task<T> {
        private final Object lock = new Object();
        private boolean complete;
        private T result;
        private Exception exception;
        private final List<Runnable> waiters = new ArrayList<>();

        boolean complete(T r, Exception e, boolean swallowDupes) {
            List<Runnable> toFire;
            synchronized (lock) {
                if (complete) {
                    if (swallowDupes) return false;
                    throw new IllegalStateException("Task already complete");
                }
                result = r; exception = e; complete = true;
                toFire = new ArrayList<>(waiters); waiters.clear();
            }
            for (Runnable w : toFire) try { w.run(); } catch (Throwable ignored) {}
            return true;
        }
        private void onDone(Runnable r) {
            boolean fireNow;
            synchronized (lock) { fireNow = complete; if (!fireNow) waiters.add(r); }
            if (fireNow) try { r.run(); } catch (Throwable ignored) {}
        }

        @Override public boolean isComplete() { synchronized (lock) { return complete; } }
        @Override public boolean isSuccessful() { synchronized (lock) { return complete && exception == null; } }
        @Override public boolean isCanceled() { return false; }
        @Override public T getResult() {
            synchronized (lock) {
                if (!complete) throw new IllegalStateException("not complete");
                if (exception != null) throw new RuntimeException(exception);
                return result;
            }
        }
        @Override public Exception getException() { synchronized (lock) { return exception; } }

        @Override public Task<T> addOnSuccessListener(final OnSuccessListener<? super T> l) {
            onDone(new Runnable() { public void run() { if (isSuccessful()) l.onSuccess(result); } });
            return this;
        }
        @Override public Task<T> addOnSuccessListener(Executor e, final OnSuccessListener<? super T> l) {
            onDone(new Runnable() { public void run() {
                if (isSuccessful()) e.execute(new Runnable() { public void run() { l.onSuccess(result); } });
            }});
            return this;
        }
        @Override public Task<T> addOnFailureListener(final OnFailureListener l) {
            onDone(new Runnable() { public void run() { if (exception != null) l.onFailure(exception); } });
            return this;
        }
        @Override public Task<T> addOnFailureListener(Executor e, final OnFailureListener l) {
            onDone(new Runnable() { public void run() {
                if (exception != null) e.execute(new Runnable() { public void run() { l.onFailure(exception); } });
            }});
            return this;
        }
        @Override public Task<T> addOnCompleteListener(final OnCompleteListener<T> l) {
            onDone(new Runnable() { public void run() { l.onComplete(Impl.this); } });
            return this;
        }
        @Override public Task<T> addOnCompleteListener(Executor e, final OnCompleteListener<T> l) {
            onDone(new Runnable() { public void run() { e.execute(new Runnable() { public void run() { l.onComplete(Impl.this); } }); } });
            return this;
        }
        @Override public Task<T> addOnCanceledListener(OnCanceledListener l) { return this; }
    }
}
