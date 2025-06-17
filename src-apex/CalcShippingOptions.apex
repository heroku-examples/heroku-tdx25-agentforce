HerokuAppLink.ActionsService service = new HerokuAppLink.ActionsService();
HerokuAppLink.ActionsService.calculateShippingOptions_Request request = new HerokuAppLink.ActionsService.calculateShippingOptions_Request();
request.body = new HerokuAppLink.ActionsService_CalculateShippingOptionsRequest();
request.body.productId = 'xyz';
System.debug(service.calculateShippingOptions(request));
