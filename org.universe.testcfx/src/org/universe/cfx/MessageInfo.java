package org.universe.cfx;

import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Human readable adapter to org.apache.cxf.message.Message
public class MessageInfo {

    public final Message Msg;

    public MessageInfo(Message msg) {
        Msg = msg;
    }

    public boolean isOutgoing()
    {
        return Msg == Msg.getExchange().getOutMessage()
                || Msg == Msg.getExchange().getOutFaultMessage();
    }

    public OperationInfo getOperationInfo()
    {
        BindingOperationInfo b = Msg.getExchange().getBindingOperationInfo();
        return b == null ? null : b.getOperationInfo();
    }

    // {namespace}localPart
    public String getOperationName()
    {
        OperationInfo o = getOperationInfo();
        return o == null ? "N/A" : o.getName().toString();
    }

    // on server side
    public String getActionKey()
    {
        OperationInfo o = getOperationInfo();
        return getAboslutePath() + "::" + (o == null ? "N/A" : o.getName().getLocalPart());
    }

    // Outbound only
    public void putStringHeader(String key, String value)
    {
        Object rh = Msg.get(Message.PROTOCOL_HEADERS);
        Map headers = rh instanceof Map ? (Map) rh : null;
        if (headers != null)
            headers.put(key, Collections.singletonList(value));
    }

    // Inbound only
    public String getStringHeader(String key)
    {
        Object rh = Msg.get(Message.PROTOCOL_HEADERS);
        Map headers = rh instanceof Map ? (Map) rh : null;
        if (headers != null)
        {
            Object raw = headers.get(key);
            if (raw instanceof List)
            {
                List list =(List) raw;
                return list.size() > 0 ? list.get(0).toString() : null;
            }
            if (raw != null)
                return raw.toString();
        }
        return null;
    }

    // Inbound only?
    public int getContentLength()
    {
        String l = getStringHeader("Content-Length");
        int len = 0;
        try { len = Integer.parseInt(l); }
        catch (NumberFormatException ex)
        {
            len = 0;
        }

        return len;
    }

    private HttpServletRequest getHttpServletRequest()
    {
        Object r = Msg.get("HTTP.REQUEST");
        return (r instanceof HttpServletRequest)
                ? (HttpServletRequest) r: null;
    }

    // URL: org.apache.cxf.request.url, http://localhost:9000/helloWorld
    // Path: org.apache.cxf.request.uri, /helloWorld
    // HTTP.REQUEST: (POST /helloWorld)@26403478 org.eclipse.jetty.server.Request@192e296
    //               _endp -> _local _remote
    public String getRemoteHost()
    {
        HttpServletRequest r = getHttpServletRequest();
        return r == null ? null : r.getRemoteHost();
    }

    // 127.0.0.1
    public String getRemoteAddr()
    {
        HttpServletRequest r = getHttpServletRequest();
        return r == null ? null : r.getRemoteAddr();
    }

    // 43539
    public int getRemotePort()
    {
        HttpServletRequest r = getHttpServletRequest();
        return r == null ? 0 : r.getRemotePort();
    }


    // @example: http://localhost:9000/helloWorld
    public String getFullPath()
    {
        Object o = Msg.get("org.apache.cxf.request.url");
        return o instanceof String ? (String) o : null;
    }

    // @example: /helloWorld
    public String getAboslutePath()
    {
        Object o = Msg.get("org.apache.cxf.request.uri");
        return o instanceof String ? (String) o : null;
    }

}
