package de.elite12.musikbot.backend.aspects;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class SongProviderObservationAspect {

    @Autowired
    ObservationRegistry observationRegistry;

    private Object observeProviderMethodWithURL(String name, ProceedingJoinPoint joinPoint) throws Throwable {
        String provider = joinPoint.getTarget().getClass().getSimpleName();
        String url = (String) joinPoint.getArgs()[0];

        Observation observation = Observation.createNotStarted(name, this.observationRegistry);

        observation.lowCardinalityKeyValue("provider", provider);
        observation.highCardinalityKeyValue("url", url);

        observation.start();
        try (Observation.Scope scope = observation.openScope()) {
            return joinPoint.proceed();
        } catch (Throwable e) {
            observation.error(e);
            throw e;
        } finally {
            observation.stop();
        }
    }

    @Around("execution(* de.elite12.musikbot.backend.interfaces.SongProvider.getSong(..))")
    public Object aroundGetSong(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeProviderMethodWithURL("getSong", joinPoint);
    }

    @Around("execution(* de.elite12.musikbot.backend.interfaces.SongProvider.getPlaylist(..))")
    public Object aroundGetPlaylist(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeProviderMethodWithURL("getPlaylist", joinPoint);
    }

    @Around("execution(* de.elite12.musikbot.backend.interfaces.SongProvider.getPlaylistEntry(..))")
    public Object aroundGetPlaylistEntry(ProceedingJoinPoint joinPoint) throws Throwable {
        return observeProviderMethodWithURL("getPlaylistEntry", joinPoint);
    }
}
