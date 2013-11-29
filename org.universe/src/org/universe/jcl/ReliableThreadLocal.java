package org.universe.jcl;

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
/*
            try
            {
*/
                T initValue = this.initialValue();
                state.set(createSuccessState(initValue));
                return initValue;
/*
            }
            catch(Exception ex)
            {
                throw ex;
            }
*/
        }
    }

    State createSuccessState(T value)
    {
        State newState = new State();
        newState.SuccessInit = true;
        newState.Value = value;
        return newState;
    }

    protected T initialValue() throws Exception {
        return null;
    }

    final public void set(T value)
    {
        state.set(createSuccessState(value));
    }

    final public void reset()
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
