package org.universe.cfx;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.List;

public class TransferInterceptor extends AbstractPhaseInterceptor<Message> {

    private MessageHook handler;

    static public abstract class MessageHook {
        public abstract void hook(MessageInfo message);
    }

    public static void addServerHook(JaxWsServerFactoryBean factory, final MessageHook hook)
    {
        String phase = Phase.PRE_INVOKE;
        TransferInterceptor me = enshureMe(factory.getInInterceptors(), phase, hook);
    }

    public static void addClientHook(JaxWsProxyFactoryBean factory, final MessageHook hook)
    {
        String phase = Phase.PRE_MARSHAL;
        TransferInterceptor me = enshureMe(factory.getOutInterceptors(), phase, hook);
    }

    // What the hell. list allows only one instance of the class
    static TransferInterceptor enshureMe(List interceptors, String phase, final MessageHook newHook)
    {
        TransferInterceptor me = null;
        for(int i=0; i<interceptors.size(); i++)
        {
            Interceptor<Message> o = (Interceptor<Message>) interceptors.get(i);
            if (o instanceof TransferInterceptor)
                me = (TransferInterceptor) o;
        }

        if (me == null)
        {
            me = new TransferInterceptor(phase, newHook);
            interceptors.add(me);
        }
        else
        {
            final MessageHook prev = me.handler;
            me.handler = new MessageHook() {
                @Override
                public void hook(MessageInfo message) {
                    prev.hook(message);
                    newHook.hook(message);
                }
            };
        }

        return me;
    }

    private TransferInterceptor(String phase, MessageHook handler) {
        super(phase);
        // addAfter("");

        if (handler == null)
            throw new IllegalArgumentException("handler argument is null");

        this.handler = handler;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        MessageInfo info = new MessageInfo(message);
        handler.hook(info);
    }
}
