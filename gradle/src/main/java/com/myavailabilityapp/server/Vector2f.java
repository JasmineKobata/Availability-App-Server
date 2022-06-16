package com.myavailabilityapp.server;

import java.util.Date;

public class Vector2f<T> {
    public float x;
    public float y;
    public Date start;
    public Date end;
    public T first;
    public T second;

    public Vector2f() {

    }
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Vector2f(T first, T second) {
        this.first = first;
        this.second = second;
    }
}
