package com.andbase.tractor.event;

/**
 * Created by Administrator on 2016/11/13.
 */
public class Event {
    public Object value;
    public boolean sticky = false;

    public Event(Object event, boolean sticky) {
        this.value = event;
        this.sticky = sticky;
    }

    public Event(Object event) {
        this.value = event;
    }
}
