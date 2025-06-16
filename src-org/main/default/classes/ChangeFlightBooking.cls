public with sharing class ChangeFlightBooking {
    @InvocableMethod(label='Change Flight Booking' description='Updates the flight for a booking if seats are available')
    public static List<BookingUpdateResult> changeFlight(List<BookingChangeRequest> requests) {
        List<BookingUpdateResult> results = new List<BookingUpdateResult>();

        for (BookingChangeRequest request : requests) {
            BookingUpdateResult result = new BookingUpdateResult();
            result.pnr = request.pnr;

            try {
                // Fetch Booking record using PNR
                Booking__c booking = [SELECT Id, PNR__c, Flight__c, Class__c 
                                      FROM Booking__c WHERE PNR__c = :request.pnr LIMIT 1];

                // Fetch Current Flight Details
                Flight__c oldFlight = [SELECT Id, Name, Available_Economy_Seats__c, Available_Business_Seats__c, Available_FirstClass_Seats__c
                                       FROM Flight__c WHERE Id = :booking.Flight__c LIMIT 1];

                // Fetch New Flight Details using Name (Flight Number)
                Flight__c newFlight = [SELECT Id, Name, Available_Economy_Seats__c, Available_Business_Seats__c, Available_FirstClass_Seats__c
                                       FROM Flight__c WHERE Name = :request.newFlightNumber LIMIT 1];

                if (newFlight == null) {
                    result.success = false;
                    result.message = 'No flight found with the given Flight Number.';
                    results.add(result);
                    continue;
                }

                // Check seat availability in new flight
                Boolean seatAvailable = false;
                if (booking.Class__c == 'Economy' && newFlight.Available_Economy_Seats__c > 0) {
                    seatAvailable = true;
                    newFlight.Available_Economy_Seats__c--;
                    oldFlight.Available_Economy_Seats__c++;
                } else if (booking.Class__c == 'Business' && newFlight.Available_Business_Seats__c > 0) {
                    seatAvailable = true;
                    newFlight.Available_Business_Seats__c--;
                    oldFlight.Available_Business_Seats__c++;
                } else if (booking.Class__c == 'First Class' && newFlight.Available_FirstClass_Seats__c > 0) {
                    seatAvailable = true;
                    newFlight.Available_FirstClass_Seats__c--;
                    oldFlight.Available_FirstClass_Seats__c++;
                }

                if (seatAvailable) {
                    // Update Booking with new Flight
                    booking.Flight__c = newFlight.Id;

                    // Perform DML Updates
                    update booking;
                    update newFlight;
                    update oldFlight;

                    result.success = true;
                    result.message = 'Booking updated successfully!';
                } else {
                    result.success = false;
                    result.message = 'No available seats in the requested class on the new flight.';
                }
                
            } catch (Exception e) {
                result.success = false;
                result.message = 'Error: ' + e.getMessage();
            }
            
            results.add(result);
        }
        return results;
    }

    // Wrapper Class for Input
    public class BookingChangeRequest {
        @InvocableVariable(label='PNR' description='The Passenger Name Record (PNR) associated with the existing booking.' required=true)
        public String pnr;
        
        @InvocableVariable(label='New Flight Number' description='The flight number of the new flight the passenger wants to switch to.' required=true)
        public String newFlightNumber;
    }

    // Wrapper Class for Output
    public class BookingUpdateResult {
        @InvocableVariable(label='PNR' description='The PNR of the booking that was processed.')
        public String pnr;
        
        @InvocableVariable(label='Success' description='Indicates whether the flight change was successful (true) or not (false).')
        public Boolean success;
        
        @InvocableVariable(label='Message' description='A message providing details about the success or failure of the flight change.')
        public String message;
    }
}