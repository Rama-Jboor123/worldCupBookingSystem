Feature: Booking rooms
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
