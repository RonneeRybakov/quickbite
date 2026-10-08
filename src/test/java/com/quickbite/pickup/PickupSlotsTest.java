
package com.quickbite.pickup;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.quickbite.menu.MenuItem;
import com.quickbite.menu.Restaurant;

class PickupSlotsTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-07T10:00:00Z"),
            ZoneOffset.UTC);

    private final PickupSlots pickupSlots = new PickupSlots(clock);

    private final Restaurant pizzaPlace = new Restaurant(
            "pizza-palace", "Pizza Palace", "Italian",
            List.of(new MenuItem("p1", "Pizza", new BigDecimal("12.50"))));

    private final Restaurant sushiPlace = new Restaurant(
            "sushi-corner", "Sushi Corner", "Japanese",
            List.of(new MenuItem("s1", "Sushi", new BigDecimal("9.00"))));

    // PBI-05: Configure pickup slots

    @Test
    void restaurantHasConfiguredPickupSlots() {
        List<LocalDateTime> slots = pickupSlots.availableFor(pizzaPlace);

        assertFalse(slots.isEmpty());
        assertTrue(slots.contains(
                LocalDateTime.of(2026, 10, 7, 12, 0)));
    }

    @Test
    void differentRestaurantsHaveDifferentPickupSlots() {
        LocalDateTime pizzaTime = LocalDateTime.of(
                2026, 10, 7, 18, 30);

        assertTrue(pickupSlots.isValid(pizzaPlace, pizzaTime));
        assertFalse(pickupSlots.isValid(sushiPlace, pizzaTime));
    }

    // PBI-05: Validate pickup times

    @Test
    void validFuturePickupTimeIsAccepted() {
        LocalDateTime selected = LocalDateTime.of(
                2026, 10, 7, 12, 0);

        assertTrue(pickupSlots.isValid(pizzaPlace, selected));
    }

    @Test
    void pastPickupTimeIsRejected() {
        LocalDateTime past = LocalDateTime.of(
                2026, 10, 6, 12, 0);

        assertFalse(pickupSlots.isValid(pizzaPlace, past));
    }

    @Test
    void unlistedPickupTimeIsRejected() {
        LocalDateTime unlisted = LocalDateTime.of(
                2026, 10, 7, 15, 45);

        assertFalse(pickupSlots.isValid(pizzaPlace, unlisted));
    }

    @Test
    void missingPickupTimeIsRejected() {
        assertFalse(pickupSlots.isValid(pizzaPlace, ""));
        assertFalse(pickupSlots.isValid(pizzaPlace, (String) null));
    }

    @Test
    void invalidDateFormatIsRejected() {
        assertFalse(pickupSlots.isValid(
                pizzaPlace, "not-a-date"));
    }

    @Test
    void pickupTimeOutsideBookingWindowIsRejected() {
        LocalDateTime tooFar = LocalDateTime.of(
                2026, 11, 20, 12, 0);

        assertFalse(pickupSlots.isValid(pizzaPlace, tooFar));
    }

    @Test
    void allAvailableSlotsAreInTheFuture() {
        LocalDateTime now = LocalDateTime.now(clock);

        for (LocalDateTime slot : pickupSlots.availableFor(pizzaPlace)) {
            assertTrue(slot.isAfter(now));
        }
    }

    @Test
    void validSubmittedStringIsAccepted() {
        assertTrue(pickupSlots.isValid(
                pizzaPlace, "2026-10-07T12:00"));
    }
}
