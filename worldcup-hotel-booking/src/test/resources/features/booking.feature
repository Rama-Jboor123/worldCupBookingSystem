Feature: Booking rooms
  Scenario: Room is unavailable
    Given a room is unavailable for the requested dates
    When the guest attempts to create a booking for that room
    Then an error message should be displayed indicating that the room is unavailable

  Scenario: The guest books an available room
    Given a room is available for the requested dates
    When the guest creates a booking for that room
    Then the booking should be created with PENDING status

  Scenario: The Guest Count does not match the adults + children count
   Given The Guest enters a guest count that does not match the sum of adults and children
   When the guest attempts to create a booking
   Then  an error message should be displayed

  Scenario Outline: Validate the total number of guests
    Given the guest enters <adults> adults and <children> children
    And the guest enters <total> as the total number of guests
    When the guest validates the booking guest count
    Then the guest number validation should be <result>

    Examples:
      | adults | children | total | result  |
      | 2      | 0        | 2     | valid   |
      | 2      | 1        | 3     | valid   |
      | 2      | 0        | 1     | invalid |

  @invalid_dates
  Scenario: Check-out is before check-in
    Given a room is available for the requested dates
    And the guest selects check-in "2026-11-12" and check-out "2026-11-10"
    When the guest attempts to create a booking
    Then the booking should be rejected with "Check-out date cannot be before check-in date"
    And the booking should not be saved

  @capacity
  Scenario: Guests exceed room capacity
    Given a room is available for the requested dates
    And the guest requests 3 adults and 1 child for one room allowing 2 adults and 1 child
    When the guest attempts to create a booking
    Then the booking should be rejected with "Number of guests exceeds room capacity"
    And the booking should not be saved
