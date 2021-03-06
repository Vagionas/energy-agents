package uk.ac.cam.eeci.energyagents;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A control strategy for the heating system of dwellings.
 *
 * A control strategy is a -- potentially dynamic -- controller that decides
 * on switch states of the heating system (on/off) and heating set points. It
 * can base its decisions on the current point in time and on the people that
 * occupy the dwelling at the current moment, but doesn't have to.
 *
 */
public abstract class HeatingControlStrategy {
    // should rather be an interface, but due to the reference mechanism must be an abstract class

    /**
     * Determines the current heating set point for the heating system of a dwelling.
     *
     * @param timeStamp The current time stamp.
     * @param peopleInDwelling The people that currently occupy the dwelling.
     * @return the heating set point for the heating system; can be empty in
     *         which case a switch off of the heating system is demanded,
     *         should it be turned on.
     */
    public abstract CompletableFuture<Optional<Double>> heatingSetPoint(ZonedDateTime timeStamp,
                                                                        Set<PersonReference> peopleInDwelling);

}
