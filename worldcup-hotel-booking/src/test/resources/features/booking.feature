Feature: Booking rooms
  Scenario: The guest books an available room
    Given a room is available for the requested dates
    When the guest creates a booking for that room
    Then the booking should be created with PENDING status