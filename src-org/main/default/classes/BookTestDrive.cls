public with sharing class BookTestDrive {
    
    @InvocableMethod(
        label='Book Test Drive' 
        description='Creates a new Test Drive record for a customer and vehicle with a scheduled date.'
    )
    public static List<String> bookTestDrive(List<TestDriveRequest> requests) {
        List<String> responses = new List<String>();
        
        List<Test_Drive__c> testDrives = new List<Test_Drive__c>();
        
        for (TestDriveRequest request : requests) {
            try {
                // Validate required fields
                if (request.VehicleId == null || request.CustomerId == null || request.TestDriveDate == null) {
                    responses.add('Error: Missing required fields (Vehicle, Customer, or Test Drive Date).');
                    continue;
                }
                
                // Create Test Drive record
                Test_Drive__c newTestDrive = new Test_Drive__c(
                    Vehicle__c = request.VehicleId,
                    Customer__c = request.CustomerId,
                    Test_Drive_Date__c = request.TestDriveDate,
                    Sales_Agent__c = request.SalesAgentId,
                    Status__c = 'Scheduled'
                );
                testDrives.add(newTestDrive);
                
            } catch (Exception e) {
                responses.add('Error: ' + e.getMessage());
            }
        }

        // Insert records if there are valid ones
        if (!testDrives.isEmpty()) {
            try {
                insert testDrives;
                responses.add('Success: Test Drive(s) booked successfully.');
            } catch (Exception e) {
                responses.add('Error: ' + e.getMessage());
            }
        }
        
        return responses;
    }

    // Invocable Variable Class
    public class TestDriveRequest {
        @InvocableVariable(
            label='Vehicle ID' 
            description='The ID of the Vehicle for which the test drive is being scheduled.' 
            required=true
        )
        public String VehicleId;

        @InvocableVariable(
            label='Customer ID' 
            description='The ID of the Customer who is booking the test drive.' 
            required=true
        )
        public String CustomerId;

        @InvocableVariable(
            label='Test Drive Date' 
            description='The date and time when the test drive is scheduled.' 
            required=true
        )
        public DateTime TestDriveDate;

        @InvocableVariable(
            label='Sales Agent ID' 
            description='(Optional) The ID of the Sales Agent handling the test drive booking.' 
            required=false
        )
        public String SalesAgentId;
    }
}