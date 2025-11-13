package com.example.usermanagement.config;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRegistry {
    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    public SseEmitter create() {
        SseEmitter e = new SseEmitter(0L);
        emitters.add(e);
        e.onCompletion(() -> emitters.remove(e));
        e.onTimeout(() -> emitters.remove(e));
        return e;
    }

    public Set<SseEmitter> getEmitters() { return emitters; }
}
