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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BookingSteps {

    private BookingServiceImpl bookingService;

    private Booking booking;
    private Booking result;

    private AvailabilityServiceImpl availabilityService;
    private EnhancedPricingServiceImpl pricingService;

    @Given("a room is available for the requested dates")
    public void aRoomIsAvailableForTheRequestedDates() {

        // Mock the dependencies of BookingServiceImpl
        BookingRepository bookingRepository = mock(BookingRepository.class);
        pricingService = mock(EnhancedPricingServiceImpl.class);
        availabilityService = mock(AvailabilityServiceImpl.class);
        CancellationPolicyServiceImpl cancellationPolicyService =
                mock(CancellationPolicyServiceImpl.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentServiceImpl paymentService = mock(PaymentServiceImpl.class);

        // save() returns the same booking that was passed in
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Guest count is valid for the room capacity
        when(availabilityService.isNumberOfGuestsValid(any(Booking.class)))
                .thenReturn(true);

        // Room is available
        when(availabilityService.checkAvailability(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class),
                anyInt()
        )).thenReturn(true);

        // Price of the stay
        when(pricingService.calculateTotalStayPrice(
                any(Booking.class),
                any(Hotel.class),
                any(RoomType.class),
                anyInt()
        )).thenReturn(new BigDecimal("200.00"));

        // The real service under test
        bookingService = new BookingServiceImpl(
                bookingRepository,
                pricingService,
                cancellationPolicyService,
                availabilityService,
                paymentRepository,
                paymentService
        );

        // Test hotel
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setCountry("Palestine");
        hotel.setCity("Bethlehem");

        // Test room type
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Standard Room");
        roomType.setHotel(hotel);
        roomType.setMaxAdults(2);
        roomType.setMaxChildren(1);
        roomType.setBasePrice(new BigDecimal("100.00"));
        roomType.setTotalRooms(10);

        // Test booking
        booking = new Booking();
        booking.setHotel(hotel);
        booking.setCheckInDate(LocalDate.now().plusDays(10));
        booking.setCheckOutDate(LocalDate.now().plusDays(12));
        booking.setNumberOfGuests(2);
        booking.setNumberOfAdults(2);
        booking.setNumberOfChildren(0);

        // Test booking room
        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBooking(booking);
        bookingRoom.setRoomType(roomType);
        bookingRoom.setNumberOfRooms(1);

        booking.getBookingRooms().add(bookingRoom);
    }

    @When("the guest creates a booking for that room")
    public void theGuestCreatesABookingForThatRoom() {
        result = bookingService.createBooking(booking);
    }

    @Then("the booking should be created with PENDING status")
    public void theBookingShouldBeCreatedWithPendingStatus() {
        assertNotNull(result);
        assertEquals(Booking.BookingStatus.PENDING, result.getStatus());
    }
}