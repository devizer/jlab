package org.universe.jcl;

import org.universe.jcl.apparency.GuardedBy;
import org.universe.jcl.apparency.ThreadSafe;

@ThreadSafe
public abstract class Lazy<T> {

    Object ExecutionAndPublicationSync = new Object();
    Object PublicationSync = new Object();
    Object BasicSync = new Object();

    @GuardedBy("BasicSync")
    LazyState UnsafeState = new LazyState();

    public enum Mode
    {
        // get() может вернуть разные экземпляры,
        // рекомендуется только для данных, которые можно спустить в коллектор
        None,

        // get() всегда возвращает один экземпляр, но initialValue() может быть вызван несколько раз
        // редко используется, когда нужно сравнение референсов
        // initialValue() может быть вызван другим потоком, т.е.
        // синхронизируется только публикация успешного вызова initialValue()
        PublicationOnly,

        // get всегда возвращает один экземпляр, initialValue всегда вызывается только раз
        // рекомендуется для unmanaged-ресурсов,
        ExecutionAndPublication,
    }

    private final Mode LazyThreadSafetyMode;

    public Lazy(Mode lazyThreadSafetyMode) {
        LazyThreadSafetyMode = lazyThreadSafetyMode;
    }

    public Lazy() {
        this(Mode.None);
    }


    protected abstract T initialValue() throws Exception;

    @GuardedBy("None | PublicationSync | ExecutionAndPublicationSync")
    final public T get() throws Exception {

        LazyState prev = syncGetState();
        if (prev.InitPerformed && prev.Exception == null)
        {
            if (prev.Exception != null)
                throw prev.Exception;
            else
                return prev.Value;
        }

        // Или первый вызов, или предыдущий вызов get() упал
        if (LazyThreadSafetyMode == Mode.None)
        {
            LazyState newState = tryInit();
            syncSetState(newState);

            if (newState.Exception != null)
                throw newState.Exception;
            else
                return newState.Value;
        }
        else if (LazyThreadSafetyMode == Mode.PublicationOnly)
        {
            LazyState newState = tryInit();

            synchronized (PublicationSync)
            {
                LazyState copy = syncGetState();
                if (copy.InitPerformed && copy.Exception == null)
                    return copy.Value;

                syncSetState(newState);
                if (newState.Exception != null)
                    throw newState.Exception;
                else
                    return newState.Value;
            }
        }
        else if (LazyThreadSafetyMode == Mode.ExecutionAndPublication)
        {
            synchronized (ExecutionAndPublicationSync)
            {
                LazyState copy = syncGetState();
                if (copy.InitPerformed && copy.Exception == null)
                    return copy.Value;

                LazyState newState = tryInit();
                syncSetState(newState);
                if (newState.Exception != null)
                    throw newState.Exception;
                else
                    return newState.Value;
            }
        }

        throw new IllegalStateException("LazyThreadSafetyMode value '" + LazyThreadSafetyMode + "' is unknown");
    }

    class LazyState
    {
        public boolean InitPerformed = false;
        public Exception Exception = null;
        public T Value = null;

        LazyState() {
        }

        LazyState(boolean initPerformed, Exception exception, T value) {
            InitPerformed = initPerformed;
            Exception = exception;
            this.Value = value;
        }
    }

    LazyState syncGetState()
    {
        synchronized (BasicSync)
        {
            return new LazyState(UnsafeState.InitPerformed, UnsafeState.Exception, UnsafeState.Value);
        }
    }

    void syncSetState(boolean initSuccess, Exception exception, T value)
    {
        synchronized (BasicSync)
        {
            UnsafeState.Exception = exception;
            UnsafeState.InitPerformed = initSuccess;
            UnsafeState.Value = value;
        }
    }

    void syncSetState(LazyState state)
    {
        synchronized (BasicSync)
        {
            UnsafeState.Exception = state.Exception;
            UnsafeState.Value = state.Value;
            UnsafeState.InitPerformed = state.InitPerformed;
        }
    }

    LazyState tryInit()
    {
        try
        {
            T initValue = this.initialValue();
            return new LazyState(true, null, initValue);
        }
        catch(Exception ex)
        {
            return new LazyState(true, ex, null);
        }

    }
}
