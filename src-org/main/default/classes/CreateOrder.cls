public without sharing class CreateOrder {
    @InvocableMethod(
        label='Create Order'
        description='Creates an order for a specified product'
    )
    public static List<String> createOrder(
        List<OrderRequest> requests
    ) {
        OrderRequest orWrapper = requests[0];
        
        /** Get Contact Information **/
        Contact con = [SELECT Id, AccountId FROM Contact WHERE Id =:orWrapper.contactId];
        
        /** Get Pricebook Information **/
        Pricebook2 pb2 = [
            SELECT Id, Name, IsActive
            FROM PriceBook2
            WHERE IsStandard = TRUE
            LIMIT 1
        ];
        
        /** Create a map of Product and Pricebook Information **/
        Map<String, PricebookEntry> productNameIdMap = new Map<String, PricebookEntry>();
        for (PricebookEntry p : [
            SELECT Id, Product2.Id, Pricebook2.IsStandard, UnitPrice
            FROM PricebookEntry
            WHERE
                Pricebook2.IsStandard = TRUE
        ]) {
            productNameIdMap.put(p.Product2.Id, p);
        }
        
        /** Insert Order **/
        Order o = new Order();
        o.AccountId = con.AccountId;
        o.PriceBook2Id = pb2.Id;
        o.EffectiveDate = Date.today();
        o.Status = 'Draft';
        o.ShipToContactId = con.Id;
        insert o;
        
        /** Insert Order Line Item **/
        OrderItem op = new OrderItem();
        op.OrderId = o.Id;
        op.Product2Id = productNameIdMap.get(orWrapper.selectedProductId).Id;
        op.UnitPrice = productNameIdMap.get(orWrapper.selectedProductId).UnitPrice;
        op.Quantity = 1;
        op.Selected_Color__c = orWrapper.selectedColor;
        op.Selected_Size__c = orWrapper.selectedSize;
        insert op;
        
        /** Retrieve Order to send the order number **/
        Order insertedOrder = [SELECT OrderNumber FROM Order WHERE Id = :o.Id];
        return new List<String> {insertedOrder.OrderNumber};
    }

    public class OrderRequest {
        @InvocableVariable(
            required=true
            description='18 digit Salesforce ID of the contact for whom order must be placed.'
        )
        public String contactId;
        
        @InvocableVariable(
            required=true
            description='18 digit Salesforce ID of the product that needs to be ordered.'
        )
        public String selectedProductId;
        
        @InvocableVariable(
            required=false
            description='If applicable, selected size of the product.'
        )
        public String selectedSize;
        
        @InvocableVariable(
            required=false
            description='If applicable, selected color of the product.'
        )
        public String selectedColor;
    }
}