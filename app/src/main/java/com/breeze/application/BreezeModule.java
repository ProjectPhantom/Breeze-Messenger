package com.breeze.application;

import com.breeze.EventEmitter;

public abstract class BreezeModule extends EventEmitter {
    protected BreezeAPI api;
    public BreezeModule(BreezeAPI api) {
        this.api = api;
    }
}
