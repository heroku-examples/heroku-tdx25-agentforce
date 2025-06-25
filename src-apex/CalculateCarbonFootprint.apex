try {
    HerokuAppLink.ActionsService service = new HerokuAppLink.ActionsService();
    HerokuAppLink.ActionsService.calculateCarbonFootprint_Request request = new HerokuAppLink.ActionsService.calculateCarbonFootprint_Request();
    request.body = new HerokuAppLink.ActionsService_CarbonFootprintRequest();
    request.body.flightid = 'a01bn00001SAFA3AAP';
    System.debug(service.calculateCarbonFootprint(request));
} catch (Exception e) {
    System.debug(e.toString());
}