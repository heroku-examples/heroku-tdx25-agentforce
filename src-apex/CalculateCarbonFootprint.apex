HerokuAppLink.ActionsService service = new HerokuAppLink.ActionsService();
HerokuAppLink.ActionsService.calculateCarbonFootprint_Request request = new HerokuAppLink.ActionsService.calculateCarbonFootprint_Request();
request.body = new HerokuAppLink.ActionsService_CarbonFootprintRequest();
request.body.flightid = '1234';
System.debug(service.calculateCarbonFootprint(request));
