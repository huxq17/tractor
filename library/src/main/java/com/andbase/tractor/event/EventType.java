package com.andbase.tractor.event;

import com.andbase.tractor.utils.LogUtils;

/**
 * Created by 2144 on 2016/11/8.
 */

public class EventType {
    public int type = -1;
    public Class<?> spareType;

    public EventType() {
    }

    public EventType(int type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        if (type != -1) {
            return type;
        } else if (spareType != null) {
            return spareType.hashCode();
        }
        LogUtils.e("type == -1 and spareType == null");
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EventType) {
            EventType eventType = (EventType) o;
            if (type != -1 && eventType.type == type || spareType != null && eventType.spareType == spareType) {
                return true;
            }
        }
        return false;
    }
}
