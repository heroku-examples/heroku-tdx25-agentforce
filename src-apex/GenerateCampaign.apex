// Generate campaign social card using Heroku app imported via AppLink
HerokuAppLink.ActionsService service = new HerokuAppLink.ActionsService();
HerokuAppLink.ActionsService.renderCard_Request request = new HerokuAppLink.ActionsService.renderCard_Request();
request.body = new HerokuAppLink.ActionsService_RenderCardRequest();
request.body.line1 = 'Supercharging Apex, Flow and';
request.body.line2 = 'Agentforce with Heroku AppLink';
String socialCardBase64 = service.renderCard(request).Code200.socialCard;

// Create a new Campaign record
Campaign newCampaign = new Campaign(
    Name = 'Supercharging Apex, Flow and Agentforce with Heroku',
    Status = 'Planned',
    Type = 'Social Media',
    StartDate = Date.today(),
    EndDate = Date.today().addDays(30)
);
insert newCampaign;

// Convert Base64 to Blob
Blob imageBlob = EncodingUtil.base64Decode(socialCardBase64);

// Create ContentVersion (stores the image in Salesforce Files)
ContentVersion contentVersion = new ContentVersion(
    Title = 'Campaign Social Card',
    PathOnClient = 'social_card.png',
    VersionData = imageBlob,
    FirstPublishLocationId = newCampaign.Id // Associates file with Campaign
);
insert contentVersion;

System.debug('Campaign created and social card attached.');
System.debug(URL.getOrgDomainUrl().toExternalForm() + '/' + newCampaign.Id );
