package com.m3z0id.tzbot4j.tzLib.net.c2s;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface Identifiable {
    byte getRequestId();
}
