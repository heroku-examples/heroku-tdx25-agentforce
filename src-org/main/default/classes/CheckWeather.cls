public with sharing class CheckWeather {
    @InvocableMethod(
        label='Check Weather'
        description='Check weather at a given location for a specific date'
    )
    public static List<WeatherResponse> getWeather(
        List<WeatherRequest> requests
    ) {
        // Retrieve the date for which we want to check the weather
        Datetime dateToCheck = (Datetime) requests[0].dateToCheck;
        String cityName = (String) requests[0].cityName;

        /**
        WeatherService.Weather weather = WeatherService.getResortWeather(
            dateToCheck, cityName
        );
         */
        
        // Create the response for Agent
        WeatherResponse response = new WeatherResponse();
        response.temperature = 50; // weather.temperature;
        response.temperatureDescription = 'Its hot'; // weather.description;
        return new List<WeatherResponse>{ response };
    }

    public class WeatherRequest {
        @InvocableVariable(
            required=false
            description='Date for which we want to check the temperature. The variable needs to be an Apex Date type with format yyyy-MM-dd.'
        )
        public Date dateToCheck;
        
        @InvocableVariable(
            required=true
            description='City Name or Location for which we want to check the temperature.'
        )
        public String cityName;
    }

    public class WeatherResponse {
        @InvocableVariable(
            description='The temperature in Celsius at the location for the provided date'
        )
        public Decimal temperature;

        @InvocableVariable(
            description='Description of weather at the given location for the provided date'
        )
        public String temperatureDescription;
    }
}