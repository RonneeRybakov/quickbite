
package com.quickbite.pickup;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.quickbite.menu.Restaurant;

/** Configures and validates pickup times for each restaurant. */
public class PickupSlots {

    private final Clock clock;

    private static final Map<String, List<LocalTime>> SCHEDULE = Map.of(
            "pizza-palace", List.of(
                    LocalTime.of(12, 0),
                    LocalTime.of(12, 30),
                    LocalTime.of(18, 0),
                    LocalTime.of(18, 30)),
            "sushi-corner", List.of(
                    LocalTime.of(12, 0),
                    LocalTime.of(12, 30),
                    LocalTime.of(19, 0),
                    LocalTime.of(19, 30)),
            "burger-barn", List.of(
                    LocalTime.of(11, 30),
                    LocalTime.of(12, 0),
                    LocalTime.of(17, 30),
                    LocalTime.of(18, 0)));

    public PickupSlots() {
        this(Clock.systemDefaultZone());
    }

    public PickupSlots(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns the configured future pickup slots
     * for the next seven days, including today.
     */
    public List<LocalDateTime> availableFor(Restaurant restaurant) {
        List<LocalDateTime> slots = new ArrayList<>();
        List<LocalTime> times = SCHEDULE.getOrDefault(
                restaurant.id(), List.of());

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = LocalDate.now(clock);

        for (int day = 0; day < 7; day++) {
            LocalDate date = today.plusDays(day);

            for (LocalTime time : times) {
                LocalDateTime slot = LocalDateTime.of(date, time);

                if (slot.isAfter(now)) {
                    slots.add(slot);
                }
            }
        }

        return List.copyOf(slots);
    }

    /**
     * Checks that the pickup time is in the future
     * and belongs to the restaurant's configured slots.
     */
    public boolean isValid(Restaurant restaurant, LocalDateTime selected) {
        return selected != null
                && availableFor(restaurant).contains(selected);
    }

    /** Parses a submitted pickup time and validates it. */
    public boolean isValid(Restaurant restaurant, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            LocalDateTime selected = LocalDateTime.parse(value);
            return isValid(restaurant, selected);
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }
}
