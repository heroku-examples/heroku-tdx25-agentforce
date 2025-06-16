public without sharing class GetRecentOrders {
    @InvocableMethod(
        label='Get Recent Orders'
        description='Retrieves the recent orders along with the products in that order'
    )
    public static List<String> getRecentOrders(
        List<OrderRequest> requests
    ) {
        OrderRequest orWrapper = requests[0];
        
        List<Order> orders = [SELECT Id, OrderNumber, (SELECT Id, Selected_Size__c, Product2.Name FROM OrderItems) FROM Order WHERE ShipToContactId =: orWrapper.contactId];
        
        return new List<String> {JSON.serialize(orders)};
    }

    public class OrderRequest {
        @InvocableVariable(
            required=true
            description='18 digit Salesforce ID of the contact for whom orders must be retrieved.'
        )
        public String contactId;
    
    }
}