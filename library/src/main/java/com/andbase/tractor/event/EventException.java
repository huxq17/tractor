package com.andbase.tractor.event;

/**
 * Created by huxq17 on 2016/11/9.
 */

public class EventException extends RuntimeException {
    public EventException(String msg) {
        super(msg);
    }
    public EventException(Throwable throwable){
        super(throwable);
    }
}
