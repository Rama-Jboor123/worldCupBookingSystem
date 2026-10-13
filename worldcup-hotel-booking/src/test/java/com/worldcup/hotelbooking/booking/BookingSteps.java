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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class BookingSteps {

    private BookingServiceImpl bookingService;
    private BookingRepository bookingRepository;

    private Booking booking;
    private Booking booking2;
    private Booking booking3;
    private Booking result;
    Exception e;
    private boolean guestCountValid;
    private String expectedResult;
    private AvailabilityServiceImpl availabilityService;
    private EnhancedPricingServiceImpl pricingService;

    @Given("a room is available for the requested dates")
    public void aRoomIsAvailableForTheRequestedDates() {

        // Mock the dependencies of BookingServiceImpl
        bookingRepository = mock(BookingRepository.class);
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

    @Given("The Guest enters a guest count that does not match the sum of adults and children")
    public void theGuestEntersAGuestCountThatDoesNotMatchTheSumOfChildren(){

         booking2 = new Booking();
        booking2.setCheckInDate(LocalDate.now().plusDays(10));
        booking2.setCheckOutDate(LocalDate.now().plusDays(12));
        booking2.setNumberOfAdults(2);
        booking2.setNumberOfChildren(0);
        booking2.setNumberOfGuests(1);

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

        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBooking(booking2);
        bookingRoom.setRoomType(roomType);


        booking2.setHotel(hotel);
        ArrayList<BookingRoom> bookingRooms = new ArrayList<>();
        bookingRooms.add(bookingRoom);
        booking2.setBookingRooms(bookingRooms);

        BookingRepository bookingRepository = mock(BookingRepository.class);
        pricingService = mock(EnhancedPricingServiceImpl.class);
        availabilityService = mock(AvailabilityServiceImpl.class);
        CancellationPolicyServiceImpl cancellationPolicyService =
                mock(CancellationPolicyServiceImpl.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentServiceImpl paymentService = mock(PaymentServiceImpl.class);

        bookingService = new BookingServiceImpl(bookingRepository,pricingService,cancellationPolicyService,availabilityService,paymentRepository,paymentService);

    }

    @When("the guest attempts to create a booking")
    public void theGuestAttemptsToCreateABooking() {
       try{
           bookingService.createBooking(booking2);
       }catch(Exception e){
           this.e = e;
       }

    }

    @Given("the guest selects check-in {string} and check-out {string}")
    public void theGuestSelectsReversedDates(String checkIn, String checkOut) {
        booking.setCheckInDate(LocalDate.parse(checkIn));
        booking.setCheckOutDate(LocalDate.parse(checkOut));
        booking2 = booking;
    }

    @Given("the guest requests {int} adults and {int} child for one room allowing {int} adults and {int} child")
    public void theGuestExceedsRoomCapacity(int adults, int children, int maxAdults, int maxChildren) {
        booking.setNumberOfAdults(adults);
        booking.setNumberOfChildren(children);
        booking.setNumberOfGuests(adults + children);
        RoomType roomType = booking.getBookingRooms().getFirst().getRoomType();
        roomType.setMaxAdults(maxAdults);
        roomType.setMaxChildren(maxChildren);
        // Exercise the production capacity calculation, rather than stubbing rejection.
        doCallRealMethod().when(availabilityService).isNumberOfGuestsValid(any(Booking.class));
        booking2 = booking;
    }

    @Then("the booking should be rejected with {string}")
    public void theBookingShouldBeRejectedWith(String message) {
        assertNotNull(e, "An invalid booking must throw an exception");
        assertInstanceOf(IllegalArgumentException.class, e);
        assertEquals(message, e.getMessage());
    }

    @Then("the booking should not be saved")
    public void theBookingShouldNotBeSaved() {
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Then("an error message should be displayed")
    public void theBookingShouldNotBeCreatedAndAnErrorMessageIsDisplayed() {
        assertNotNull(e);
      assertInstanceOf(IllegalArgumentException.class, e);
     assertEquals("Total number of guests must equal the sum of adults and children", e.getMessage());
    }


    @Given("the guest enters {int} adults and {int} children")
    public void theGuestEntersAdultsAndChildren(
            int adults, int children) {
        booking2 = new Booking();
        booking2.setCheckInDate(LocalDate.now().plusDays(10));
        booking2.setCheckOutDate(LocalDate.now().plusDays(12));
        booking2.setNumberOfAdults(adults);
        booking2.setNumberOfChildren(children);


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

        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBooking(booking2);
        bookingRoom.setRoomType(roomType);


        booking2.setHotel(hotel);
        ArrayList<BookingRoom> bookingRooms = new ArrayList<>();
        bookingRooms.add(bookingRoom);
        booking2.setBookingRooms(bookingRooms);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        pricingService = mock(EnhancedPricingServiceImpl.class);
        availabilityService = mock(AvailabilityServiceImpl.class);
        CancellationPolicyServiceImpl cancellationPolicyService =
                mock(CancellationPolicyServiceImpl.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentServiceImpl paymentService = mock(PaymentServiceImpl.class);

        bookingService = new BookingServiceImpl(bookingRepository,pricingService,cancellationPolicyService,availabilityService,paymentRepository,paymentService);



    }

    @Given("the guest enters {int} as the total number of guests")
    public void theGuestEntersTotalGuests(int total) {
        booking2.setNumberOfGuests(total);
    }

    @When("the guest validates the booking guest count")
    public void theGuestAttemptsToCreateABooking2() {

       guestCountValid=bookingService.isNumberOfGuestValid(booking2);
    }

    @Then("the guest number validation should be {word}")
    public void theGuestNumberValidationShouldBe(String result) {

        expectedResult = guestCountValid ? "valid" : "invalid";

        assertEquals(expectedResult, result);
    }



}