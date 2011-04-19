/*
* Copyright 2010-2011 Research In Motion Limited.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package blackberry.core.threading;

class InterruptibleThread extends Thread {
    
    /*
     * We don't want to use ".this" as the lock object since subclasses may
     * potentially synchronize on it for long periods of time, preventing
     * interruption. Use an explicit lock object.
     */
    private Object  _lock;
    
    private boolean _isInterrupted;
    private int     _softInterrupt;

    /**
     * Default constructor for classes that wish to extend InterruptibleThread.
     */
    InterruptibleThread() {
        _lock = new Object();
    }

    /**
     * Construct a new InterruptibleThread with a given name.
     * 
     * @param name
     *            the name of the new thread.
     */
    InterruptibleThread( String name ) {
        super( name );
        _lock = new Object();
    }

    /**
     * Construct a new InterruptibleThread with a Runnable.
     * 
     * @param r
     *            The Runnable that will be run in the new thread.
     */
    InterruptibleThread( final Runnable r ) {
        super( r );
        _lock = new Object();
    }

    /**
     * Construct a new InterruptibleThread with a Runnable and a given name.
     * 
     * @param r
     *            The Runnable that will be run in the new thread.
     * @param name
     *            the name of the new thread.
     */
    InterruptibleThread( final Runnable r, String name ) {
        super( r, name );
        _lock = new Object();
    }

    /**
     * Get the currently running <code>InterruptibleThread</code>, or null if
     * the current thread is not an instance of <code>InterruptibleThread</code>.
     */
    static InterruptibleThread getCurrent() {
        Thread thread = Thread.currentThread();
        if( thread instanceof InterruptibleThread ) {
            return (InterruptibleThread) thread;
        }
        return null;
    }

    /**
     * Interrupt this thread. In addition to calling
     * <code>super.interrupt()</code>, which will break the thread out of any
     * <code>wait()</code> calls with an <code>InterruptedException</code>, this
     * method sets the interrupted flag (which can be checked using
     * <code>isInterrupted()</code> and <code>throwIfInterrupted()</code> or the
     * static versions <code>isCurrentInterrupted()</code> and
     * <code>throwIfCurrentInterrupted()</code>) and closes any registered
     * <code>InputStream</code> and <code>Connection</code> objects which should
     * trigger <code>IOException</code>s if read from.
     */
    /* @Override */
    public void interrupt() {
        synchronized( _lock ) {
            if( _isInterrupted ) {
                // Already interrupted
                return;
            }
            _isInterrupted = true;
        }
        super.interrupt();
    }

    /**
     * Do a soft interrupt on this thread. This basically does nothing except
     * set a thread-specific interrupt level that can be queried by
     * <code>getInterruptLevel()</code> and reset by <code>reset()</code>. If
     * the thread was previously soft-interrupted, the higher of the two levels
     * is kept.
     * 
     * @param level
     *            The level of the soft interrupt. Higher values indicate more
     *            urgent (less "soft") interrupts. A value of zero clears the
     *            soft interrupt.
     * 
     * @throws IllegalArgumentException
     *             if <code>level</code> is less than zero.
     */
    void interrupt( int level ) throws IllegalArgumentException {
        if( level < 0 ) {
            throw new IllegalArgumentException();
        }
        synchronized( _lock ) {
            if( level == 0 || level > _softInterrupt ) {
                _softInterrupt = level;
            }
        }
    }

    /**
     * Subclasses should override this method with their specific run code
     * instead of overriding the <code>run()</code> method. This method is
     * equivalent to the <code>Thread.run()</code> method. By default this calls
     * <code>super.run()</code>.
     */
    void joinableRun() {
        super.run();
    }

    /**
     * A run implementation that includes a hook for <code>waitJoin()</code>.
     * This implementation is final so that the hook cannot be removed by
     * subclasses. Subclasses that wish to extend this class with their own
     * <code>run()</code> method should override the <code>joinableRun()</code>
     * method instead. If this thread is created with a <code>Runnable</code>,
     * it will be run normally.
     */
    /* @Override */
    public final void run() {
        try {
            joinableRun();
        } finally {
            synchronized( _lock ) {
                _lock.notifyAll();
            }
        }
    }

    /**
     * Reset the thread. This method will clear the interrupt and soft interrupt
     * flags and unregister any registered objects.
     */
    void reset() {
        synchronized( _lock ) {
            _isInterrupted = false;
            _softInterrupt = 0;
        }
    }

    /**
     * Reset the currently-running <code>InterruptibleThread</code>, if there is
     * one. If the current thread is not an <code>InterruptibleThread</code>, no
     * action is performed.
     */
    static void resetCurrent() {
        InterruptibleThread current = getCurrent();
        if( current != null ) {
            current.reset();
        }
    }

    /**
     * Check if the thread has been interrupted.
     * 
     * @return true if the instance has been interrupted, false otherwise.
     *         Unlike the Java <code>isInterrupted()</code> method, calling this
     *         method will NOT clear the flag.
     */
    boolean isInterrupted() {
        synchronized( _lock ) {
            return _isInterrupted;
        }
    }

    /**
     * Check if the thread has been softly interrupted.
     * 
     * @return the level of the soft interruption. A value greater than zero
     *         indicates a soft interrupt.
     * 
     * @throws Interruption
     *             if the thread has been interrupted.
     */
    int getInterruptLevel() throws Interruption {
        synchronized( _lock ) {
            if( _isInterrupted ) {
                throw new Interruption();
            }
            return _softInterrupt;
        }
    }

    /**
     * Check if the currently running <code>InterruptibleThread</code> has been
     * interrupted.
     * 
     * @return true if the currently running thread is an
     *         <code>InterruptibleThread</code> AND it has been interrupted.
     *         Returns false otherwise.
     */
    static boolean isCurrentInterrupted() {
        InterruptibleThread thread = getCurrent();
        if( thread != null ) {
            return thread.isInterrupted();
        }
        return false;
    }

    /**
     * Check if the thread has been interrupted; if it has, throw an
     * <code>Interruption</code>.
     * 
     * @throws Interruption
     *             if the thread has been interrupted.
     */
    void throwIfInterrupted() /* throws Interruption */{
        synchronized( _lock ) {
            if( _isInterrupted ) {
                throw new Interruption();
            }
        }
    }

    /**
     * Check if the currently running <code>InterruptibleThread</code> has been
     * interrupted; if it has, throw an <code>Interruption</code>.
     * 
     * @return true if the currently running thread is an
     *         <code>InterruptibleThread</code> AND it has not yet been
     *         interrupted. false if the currently running thread is NOT an
     *         <code>InterruptibleThread</code>. This is provided to allow users
     *         to optionally avoid future calls to this method, since a false
     *         return value means the thread cannot be interrupted.
     * @throws Interruption
     *             if the currently running thread is an
     *             <code>InterruptibleThread</code> AND it has been interrupted.
     */
    static boolean throwIfCurrentInterrupted() throws Interruption {
        // This could be written using calls to getCurrent() and throwIfInterrupted(),
        // but this call is likely to be called often inside loops and such, so
        // inline those calls for optimization
        Thread t = Thread.currentThread();
        if( t instanceof InterruptibleThread ) {
            InterruptibleThread it = (InterruptibleThread) t;
            synchronized( it._lock ) {
                if( it._isInterrupted ) {
                    throw new Interruption();
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Extend RuntimeException so it's unchecked
     */
    static class Interruption extends RuntimeException {
        
    }
    
}
