package org.universe.queue;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Message {

    String Id;
    String OptionalKey;
    String QueueName;
    byte[] Message;
    Timestamp   CreatedAt;
    Timestamp   ModifiedAt;
    Timestamp   AckDate;
    int    HandlersCount;
    int    Locked;


    public Message() {
    }

    public static class Status
    {
        String Id;
        String OptionalKey;
        Timestamp CreatedAt;
        Timestamp ModifiedAt;
        Timestamp AckDate;
        int HanldersCount;
        boolean Locked;

        public Status() {
        }

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getOptionalKey() {
            return OptionalKey;
        }

        public void setOptionalKey(String optionalKey) {
            OptionalKey = optionalKey;
        }

        public Timestamp getCreatedAt() {
            return CreatedAt;
        }

        public void setCreatedAt(Timestamp createdAt) {
            CreatedAt = createdAt;
        }

        public Timestamp getModifiedAt() {
            return ModifiedAt;
        }

        public void setModifiedAt(Timestamp modifiedAt) {
            ModifiedAt = modifiedAt;
        }

        public Timestamp getAckDate() {
            return AckDate;
        }

        public void setAckDate(Timestamp ackDate) {
            AckDate = ackDate;
        }

        public int getHanldersCount() {
            return HanldersCount;
        }

        public void setHanldersCount(int hanldersCount) {
            HanldersCount = hanldersCount;
        }

        public boolean isLocked() {
            return Locked;
        }

        public void setLocked(boolean locked) {
            Locked = locked;
        }
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getOptionalKey() {
        return OptionalKey;
    }

    public void setOptionalKey(String optionalKey) {
        OptionalKey = optionalKey;
    }

    public String getQueueName() {
        return QueueName;
    }

    public void setQueueName(String queueName) {
        QueueName = queueName;
    }

    public byte[] getMessage() {
        return Message;
    }

    public void setMessage(byte[] message) {
        Message = message;
    }

    public Timestamp getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        CreatedAt = createdAt;
    }

    public Timestamp getModifiedAt() {
        return ModifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        ModifiedAt = modifiedAt;
    }

    public Timestamp getAckDate() {
        return AckDate;
    }

    public void setAckDate(Timestamp ackDate) {
        AckDate = ackDate;
    }

    public int getHandlersCount() {
        return HandlersCount;
    }

    public void setHandlersCount(int handlersCount) {
        HandlersCount = handlersCount;
    }

    public int getLocked() {
        return Locked;
    }

    public void setLocked(int locked) {
        Locked = locked;
    }
}
