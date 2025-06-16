public without sharing class FlightBookingService {
    @InvocableMethod(label='Create Flight Booking' description='Creates a new flight booking and updates available seats')
    public static List<String> createBooking(List<BookingRequest> requests) {
        List<String> responses = new List<String>();
        List<Flight__c> flightsToUpdate = new List<Flight__c>();
        List<Booking__c> bookingsToInsert = new List<Booking__c>();

        for (BookingRequest req : requests) {
            Flight__c flight = [SELECT Id, Available_Economy_Seats__c, Available_Business_Seats__c, Available_FirstClass_Seats__c 
                                FROM Flight__c WHERE Id = :req.flightId LIMIT 1];

            Boolean hasAvailableSeats = false;

            // Check & Update Available Seats
            if (req.bookingClass == 'Economy' && flight.Available_Economy_Seats__c > 0) {
                flight.Available_Economy_Seats__c -= 1;
                hasAvailableSeats = true;
            } else if (req.bookingClass == 'Business' && flight.Available_Business_Seats__c > 0) {
                flight.Available_Business_Seats__c -= 1;
                hasAvailableSeats = true;
            } else if (req.bookingClass == 'First Class' && flight.Available_FirstClass_Seats__c > 0) {
                flight.Available_FirstClass_Seats__c -= 1;
                hasAvailableSeats = true;
            }

            if (!hasAvailableSeats) {
                responses.add('Error: No available seats in ' + req.bookingClass + ' class.');
                continue;
            }

            // Generate Unique PNR
            String pnr = generatePNR();

            // Create Booking Record
            Booking__c newBooking = new Booking__c(
                Contact__c = req.contactId,  // Link to Contact
                PNR__c = pnr, // Assign generated PNR
                Flight__c = req.flightId,
                Class__c = req.bookingClass,
                Meal_Preference__c = req.mealPref,
                CheckIn_Status__c = 'Not Checked-In'
            );

            bookingsToInsert.add(newBooking);
            flightsToUpdate.add(flight);
            responses.add('Success: Booking created for Contact ' + req.contactId + '! PNR: ' + pnr);
        }

        if (!bookingsToInsert.isEmpty()) {
            insert bookingsToInsert;
            update flightsToUpdate; // Update seat availability in flights
        }

        return responses;
    }

    // Method to Generate Random 6-Character PNR
    public static String generatePNR() {
        String chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'; // 36 characters
        String pnr = '';
        Integer charsLength = chars.length(); // 36
    
        for (Integer i = 0; i < 6; i++) {
            Integer randomInt = Math.abs(Crypto.getRandomInteger()); // Ensure non-negative
            Integer randomIndex = Math.mod(randomInt, charsLength);  // Valid index using Math.mod()
            pnr += chars.substring(randomIndex, randomIndex + 1);    // Get character at index
        }
    
        return pnr;
    }
    

    // **Invocable Variable Wrapper**
    public class BookingRequest {
        @InvocableVariable(label='Contact ID' required=true description='18 digit Salesforce ID of the contact for whom flight must be booked.')
        public Id contactId; 

        @InvocableVariable(label='Flight ID' required=true description='18 digit Salesforce ID of the flight for which seats need to be booked.')
        public Id flightId;

        @InvocableVariable(label='Booking Class' required=true description='Class of the booking. (Economy, Business, First Class)')
        public String bookingClass;

        @InvocableVariable(label='Meal Preference' required=false description='Preferred meal of the passenger. (None, Allergy, Vegetarian)')
        public String mealPref;
    }
}