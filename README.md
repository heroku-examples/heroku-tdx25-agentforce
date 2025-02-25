# TDX25 - Supercharging Agentforce with Heroku

> [!IMPORTANT]
> This repository contains several enhanced versions of the Heroku services used during TDX'25 Mini Hacks, and also as demonstrated in a subsequent Salesforce Ben webinar. The code is shared primarily for browsing purposes. Since the TDX Mini Hack applications and objects have not yet been shared, deployment is not currently possible.

# Requirements
- Heroku login
- Heroku AppLink Pilot enabled
- Heroku CLI installed
- Heroku Integration Pilot CLI plugin is installed
- Salesforce CLI installed
- Salesforce Org containing TDX'25 Mini Hack apps
- Watch the [Introduction to the Heroku AppLink Pilot for Developers](https://www.youtube.com/watch?v=T5kOGNuTCLE) video 

## Local Development and Testing

Code invoked from Salesforce requires specific HTTP headers to connect back to the invoking Salesforce org. Using the `invoke.sh` script supplied with this sample, it is possible to simulate requests from Salesforce with the correct headers, enabling you to develop and test locally before deploying to test from Apex, Flow, or Agentforce. This sample leverages the `sf` CLI to allow the `invoke.sh` script to access org authentication details. Run the following commands to locally authenticate, build and run the sample:

```
sf org login web --alias my-org
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

## Deploying and Testing from Apex and Flow

To test from Apex, Flow and other tools within your Salesforce org you must deploy the code and import it into your org. The following commands create a Heroku application and configure the Heroku Integration add-on. This add-on and associated buildpack allows secure authenticated access from within your code and visibility of your code from Apex, Flow and Agentforce. After this configuration, code is not accessible from the public internet, only from within an authorized Salesforce org.

```
heroku create
git push heroku main
```

Next install and configure the Heroku Integration add-on:

```
heroku addons:create heroku-integration
heroku buildpacks:add https://github.com/heroku/heroku-buildpack-heroku-integration-service-mesh
heroku salesforce:connect my-org --store-as-run-as-user
heroku salesforce:import api-docs.yaml --org-name my-org --client-name ActionsService
```

Trigger an application rebuild to install the Heroku Integration buildpack

```
git commit --allow-empty -m "empty commit"
git push heroku main
```

Once imported grant permissions to users to invoke your code using the following `sf` command:

```
sf org assign permset --name ActionsService -o my-org
```

Deploy the Heroku application and confirm it has started.

```
git push heroku main
heroku logs
```

Navigate to your orgs **Setup** menu and search for **Heroku** then click **Apps** to confirm your application has been imported.

### Invoking from Apex

Now that you have imported your Heroku application. The following shows an Apex code fragment the demonstrates how to invoke your code in an synchronous manner (waits for response).

```
echo \
"ExternalService.ActionsService service = new ExternalService.ActionsService();" \
"ExternalService.ActionsService.calculateFinanceAgreement_Request request = new ExternalService.ActionsService.calculateFinanceAgreement_Request();" \
"ExternalService.ActionsService_FinanceCalculationRequest body = new ExternalService.ActionsService_FinanceCalculationRequest();" \
"request.body = body;" \
"request.body.vehicleId = 'a04Hs00002EMj9PIAT';" \
"System.debug('Final Car Price: ' + service.calculateFinanceAgreement(request).Code200.recommendedFinanceOffer.finalCarPrice);" \
| sf apex run -o my-org
```

Inspect the debug log output sent to to the console and you should see the generated Quote ID output as follows:

```
07:56:11.212 (3213672014)|USER_DEBUG|[1]|DEBUG|Final Car Price:41800
```
