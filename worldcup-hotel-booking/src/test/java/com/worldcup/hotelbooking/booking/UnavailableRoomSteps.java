package com.worldcup.hotelbooking.booking;

import com.worldcup.hotelbooking.availability_pricing.availability.AvailabilityServiceImpl;
import com.worldcup.hotelbooking.availability_pricing.pricing.EnhancedPricingServiceImpl;
import com.worldcup.hotelbooking.booking.booking.Booking;
import com.worldcup.hotelbooking.booking.booking.BookingRepository;
import com.worldcup.hotelbooking.booking.booking.BookingServiceImpl;
import com.worldcup.hotelbooking.booking.bookingroom.BookingRoom;
import com.worldcup.hotelbooking.booking.cancellation.CancellationPolicyServiceImpl;
import com.worldcup.hotelbooking.catalog.hotel.Hotel;
import com.worldcup.hotelbooking.catalog.roomtype.RoomType;
import com.worldcup.hotelbooking.payment.PaymentRepository;
import com.worldcup.hotelbooking.payment.PaymentServiceImpl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UnavailableRoomSteps {

    private BookingServiceImpl bookingService;
    private AvailabilityServiceImpl availabilityService;
    private BookingRepository bookingRepository;

    private Booking booking;
    private RoomType roomType;
    private Exception exception;

    @Given("a room is unavailable for the requested dates")
    public void aRoomIsUnavailableForTheRequestedDates() {

        // Mock dependencies
        bookingRepository = mock(BookingRepository.class);
        EnhancedPricingServiceImpl pricingService =
                mock(EnhancedPricingServiceImpl.class);
        availabilityService = mock(AvailabilityServiceImpl.class);

        CancellationPolicyServiceImpl cancellationPolicyService =
                mock(CancellationPolicyServiceImpl.class);
        PaymentRepository paymentRepository =
                mock(PaymentRepository.class);
        PaymentServiceImpl paymentService =
                mock(PaymentServiceImpl.class);

        // Guest count is valid
        when(availabilityService.isNumberOfGuestsValid(any(Booking.class)))
                .thenReturn(true);

        // Room is unavailable
        when(availabilityService.checkAvailability(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class),
                anyInt()
        )).thenReturn(false);

        // Create the service
        bookingService = new BookingServiceImpl(
                bookingRepository,
                pricingService,
                cancellationPolicyService,
                availabilityService,
                paymentRepository,
                paymentService
        );

        // Create test hotel
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setCountry("Palestine");
        hotel.setCity("Bethlehem");

        // Create test room type
        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Standard Room");
        roomType.setHotel(hotel);
        roomType.setMaxAdults(2);
        roomType.setMaxChildren(1);
        roomType.setBasePrice(new BigDecimal("100.00"));
        roomType.setTotalRooms(1);

        // Create test booking
        booking = new Booking();
        booking.setHotel(hotel);
        booking.setCheckInDate(LocalDate.now().plusDays(10));
        booking.setCheckOutDate(LocalDate.now().plusDays(12));
        booking.setNumberOfGuests(2);
        booking.setNumberOfAdults(2);
        booking.setNumberOfChildren(0);

        // Add room to booking
        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBooking(booking);
        bookingRoom.setRoomType(roomType);
        bookingRoom.setNumberOfRooms(1);

        booking.getBookingRooms().add(bookingRoom);
    }

    @When("the guest attempts to create a booking for that room")
    public void theGuestAttemptsToCreateABookingForThatRoom() {

        try {
            bookingService.createBooking(booking);
        } catch (Exception e) {
            exception = e;
        }
    }

    @Then("an error message should be displayed indicating that the room is unavailable")
    public void anErrorMessageShouldIndicateThatTheRoomIsUnavailable() {

        assertNotNull(exception, "Expected booking creation to fail");

        assertEquals(
                "Not enough rooms available for room type: " + roomType.getName(),
                exception.getMessage()
        );

        verify(availabilityService).checkAvailability(
                eq(roomType.getId()),
                eq(booking.getCheckInDate()),
                eq(booking.getCheckOutDate()),
                anyInt()
        );

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}