package org.universe.jcl;

import org.universe.jcl.apparency.ThreadSafe;

@ThreadSafe
// initialValue() может бросать исключение, которое пробрасывается в get()
// пока initialValue() падает, очередной get() вызывает снова initialValue()
public class ReliableThreadLocal<T> {

    ThreadLocal<State> state = new ThreadLocal<State>();

    public ReliableThreadLocal() {
    }

    final public T get() throws Exception {

        State s = state.get();
        if (s != null && s.SuccessInit)
        {
            return s.Value;
        }
        else
        {
            T initValue = this.initialValue();
            State newState = new State();
            newState.SuccessInit = true;
            newState.Value = initValue;
            state.set(newState);
            return initValue;
        }
    }

    protected T initialValue() throws Exception {
        return null;
    }

    public void set(T value)
    {
        State newState = new State();
        newState.SuccessInit = true;
        newState.Value = value;
        state.set(newState);
    }

    public void reset()
    {
        state.set(new State());
    }

    // Default - uninitilzied state
    class State
    {
        boolean SuccessInit = false;
        public T Value;
    }

}
