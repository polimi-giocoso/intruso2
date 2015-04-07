package it.giocoso.trovaintruso.util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeString(long time){

        String timer = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );

        return timer;
    }

}
