try {
    HerokuAppLink.ActionsService service = new HerokuAppLink.ActionsService();
    HerokuAppLink.ActionsService.calculateFinanceAgreement_Request request = new HerokuAppLink.ActionsService.calculateFinanceAgreement_Request();
    HerokuAppLink.ActionsService_FinanceCalculationRequest body = new HerokuAppLink.ActionsService_FinanceCalculationRequest();
    request.body = body;
    request.body.vehicleId = 'a03bn00000OGFrqAAH';
    request.body.customerId = '003bn00000OG8LsAAL';
    request.body.downPayment = 1000;
    request.body.maxInterestRate = 5.5;
    request.body.years = 3;
    System.debug('Final Car Price: ' + service.calculateFinanceAgreement(request).Code200.recommendedFinanceOffer.finalCarPrice);
} catch (Exception e) {
    System.debug(e.toString());
}