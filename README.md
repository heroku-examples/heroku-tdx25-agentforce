# TDX25 - Supercharging Agentforce with Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://www.heroku.com/deploy?template=https://github.com/heroku-examples/heroku-tdx25-agentforce)

> [!IMPORTANT]
> If you are taking part in a Heroku Workshop please continue to refer to instructions provided.

# Requirements
- Heroku login
- Heroku CLI installed
- Heroku AppLink CLI plugin installed
- Salesforce CLI installed
- Salesforce Org with Agentforce enabled

## Org Setup

If you are not taking part in a Heroku Workshop you will need your own Salesforce org such as a Sanbdox, Developer Edition or Scratch org. To use the actions in this repository you will need some objects, data and Agentforce agents.

```
sf org login web --alias my-org
sf project deploy start -o my-org
sf org assign permset --name HerokuAgentsApps -o my-org
sf data tree import --plan ./data/master-plan.json -o my-org
```

Login to your org and confirm you can see the **Koa Cars** application and tabs, such as **Vehicles** with data.

## Local Development and Testing

Code invoked from Salesforce requires specific HTTP headers to connect back to the invoking Salesforce org. Using the `invoke.sh` script supplied with this sample, it is possible to simulate requests from Salesforce with the correct headers, enabling you to develop and test locally before deploying to test from Apex, Flow, or Agentforce. This sample leverages the `sf` CLI to allow the `invoke.sh` script to access org authentication details. Run the following commands to locally authenticate, build and run the sample:

```
mvn clean install
mvn spring-boot:run
```

In a new terminal window run the following command substituting the Id values for valid **Contact** and **Vehicle Id** records from your Salesforce org.

```
./bin/invoke.sh my-org 'http://localhost:8080/api/finance/calculateFinanceAgreement' '{"customerId": "0035g00000XyZbHAZ","vehicleId": "a04Hs00002EMj9PIAT","maxInterestRate": 3.5,"downPayment": 1000,"years": 3}'
```

You should see the following output:

```
Response from server:
{"recommendedFinanceOffer":{"finalCarPrice":41800.0,"adjustedInterestRate":3.4,"monthlyPayment":690.5,"loanTermMonths":60,"totalFinancingCost":41430.0}}
```

Run the following command substituting the Id values for valid **Flight** record from your Salesforce org.

```
./bin/invoke.sh my-org 'http://localhost:8080/api/carbon/calculateCarbonFootprint' '{"flightId": "a02Hs00001D2QtLIAV"}'
```

You should see the following output:

```
Response from server:
{"flight":{"flightNumber":"Astro Airlines-a02Hs00001D2QtLIAV","departureAirport":"SFO","arrivalAirport":"LAX","distanceKm":543,"passengerCount":1},"emissions":{"totalCo2Kg":85.794,"co2PerPassengerKg":85.794,"co2PerKmKg":0.158},"methodology":{"calculationBasis":"DEFRA 2023 emission factors per passenger-km","fuelToCo2Ratio":3.16,"radiativeForcingMultiplier":1.9,"dataSource":"DEFRA & ICAO Aviation Emissions Guidelines"},"timestamp":"2025-02-27T11:21:44.391794Z","units":{"distance":"km","emissions":"kg CO2e"}}
```

Run the following command to generate a social card.

```
./bin/invoke.sh my-org 'http://localhost:8080/api/social/renderCard' '{"line1": "Test 1", "line2": "Test 2"}'
```

You should see the following output (truncated):

```
Response from server:
{"socialCard":"iVBORw0KGgoAAAANSUhEUgAAAOYAAACSCAYAAABR2bZsAAAjiElEQVR4Xu2dB5gURRbHvTu95Kmnnp/xFCSjSEZyEAFRQEBAsqDknCQHyQILRzxyXjIIy8ICSxaJSlKJShIUwXORjIC+6//brfm6a3pmuqe7l5nd+n3f+9Cd7prunvp3Vb169eo
......MeoNeW4V3qGEqQiI3B1GdvmhQ4eyMNXiaG9RwlTYBqkxw40gUlhDCVOhiECUMBWKCEQJU6GIQJQwFYoIRAlToYhAlDAVighECVOhiECUMBWKCEQJU6GIQJQwFYoIRAlToYhAlDAVighECVOhiECUMBWKCEQJU6GIQJQwFYoIRAlToYhAlDAVighECVOhiED+DwdSYalU9//OAAAAAElFTkSuQmCC"}
```

## Deploying and Testing from Apex

To test from Apex, Flow and other tools within your Salesforce org you must deploy the code and publish it into your org. The following commands create a Heroku application and configure the Heroku AppLink add-on. This add-on and associated buildpack allows secure authenticated access from within your code and visibility of your code from Apex, Flow and Agentforce. After this configuration, code is not accessible from the public internet, only from within an authorized Salesforce org.

```
heroku create
heroku buildpacks:add --index=1 heroku/heroku-applink-service-mesh
heroku buildpacks:add heroku/java
heroku config:set HEROKU_APP_ID="$(heroku apps:info --json | jq -r '.app.id')"
heroku addons:create heroku-applink --wait
heroku salesforce:connect my-org
heroku salesforce:publish api-docs.yaml --client-name ActionsService --connection-name my-org --authorization-connected-app-name ActionsServiceConnectedApp --authorization-permission-set-name ActionsServicePermissions
```

Once imported grant permissions to users to invoke your code using the following `sf` command:

```
sf org assign permset --name ActionsService -o my-org
sf org assign permset --name ActionsServicePermissions -o my-org
```

Deploy the Heroku application and confirm it has started.

```
git push heroku main
heroku logs
```

Navigate to your orgs **Setup** menu and search for **Heroku** then click **Apps** to confirm your application has been imported.

### Invoking from Apex

Now that you have imported your Heroku application lets invoke some of its actions.

Run the following Apex code to invoke your code in an synchronous manner (waits for response).

Be sure to edit the file to replace the record Ids for the Vehicle and Customer (Contact) with those from your org.

```
sf apex run < ./src-apex/CalculateFinanceAgreement.apex
```

For reference the above file contains the following Apex code.

```
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
```

Inspect the debug log output sent to to the console and you should see the generated price (the value will vary):

```
07:56:11.212 (3213672014)|USER_DEBUG|[1]|DEBUG|Final Car Price:41800
```

## Deploying and Testing from Agentforce

Complete the steps above to test from Apex. After this Agents and actions will have been deployed to your org. For instructions on how to interact with those agents, such as the Koa Cars agent, please refer to the workshop Agent testing steps [here](https://workshops-content.ukoreh.com/applink-agentforce/heroku-applink.html), ignore all other setup steps.
