package com.heroku.java.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Shipping Calculation", description = "Calculates the shipping options for a product given external data.")
@RestController
@RequestMapping("/api/shipping/")
public class ShippingOptionsService {

    private static final Logger logger = LoggerFactory.getLogger(ShippingOptionsService.class);

    @Operation(
        summary = "Calculate Shipping Options",
        description = "Calculates the shipping options for a product given external data.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Response containing the calculated shipping options.")
        }
    )
    @PostMapping("/calculateShippingOptions")
    public CalculateShippingOptionsResponse calculateShippingOptions(
            @org.springframework.web.bind.annotation.RequestBody
            @RequestBody(
                description = "Request to determine shipping options for a product, including the Salesforce record ID of the product being shipped.",
                content = @Content(schema = @Schema(implementation = CalculateShippingOptionsRequest.class))
            ) CalculateShippingOptionsRequest request,
            HttpServletRequest httpServletRequest) {
        logger.info("Processing shipping options calculation for product: {}", request.productId);
        // Mocked Response Data
        CalculateShippingOptionsResponse response = new CalculateShippingOptionsResponse();
        response.product = new ProductInfo();
        response.product.productId = request.productId;
        response.product.name = "Smart Gadget X";
        response.product.weight = 1.25;
        response.product.dimensions = new Dimensions();
        response.product.dimensions.length = 30;
        response.product.dimensions.width = 20;
        response.product.dimensions.height = 15;
        response.product.category = "Electronics";
        response.product.price = 99.99;
        response.shippingOptions = new ArrayList<>();
        ShippingOption option1 = new ShippingOption();
        option1.carrier = "CodyShipping";
        option1.service = "Standard Delivery";
        option1.estimatedDeliveryDays = 5;
        option1.cost = 9.99;
        option1.carbonFootprint = 2.2;
        response.shippingOptions.add(option1);
        ShippingOption option2 = new ShippingOption();
        option2.carrier = "AstroShipping";
        option2.service = "Express Delivery";
        option2.estimatedDeliveryDays = 2;
        option2.cost = 19.99;
        option2.carbonFootprint = 3.5;
        response.shippingOptions.add(option2);
        ShippingOption option3 = new ShippingOption();
        option3.carrier = "AppyShipping";
        option3.service = "Overnight Shipping";
        option3.estimatedDeliveryDays = 1;
        option3.cost = 29.99;
        option3.carbonFootprint = 4.8;
        response.shippingOptions.add(option3);
        response.recommendedOption = option1;
        response.timestamp = Instant.now().toString();
        response.units = new HashMap<>();
        response.units.put("weight", "kg");
        response.units.put("dimensions", "cm");
        response.units.put("cost", "USD");
        response.units.put("carbonFootprint", "kg CO2");
        return response;
    }

    @Schema(description = "Request to determine shipping options for a product, including the Salesforce record ID of the product being shipped.")
    public static class CalculateShippingOptionsRequest {
        @Schema(example = "PROD123456", description = "The Salesforce record ID of the product.")
        public String productId;
    }

    @Schema(description = "Response containing the calculated shipping options for the given product. Describe the results in natural language text to the user.")
    public static class CalculateShippingOptionsResponse {
        public ProductInfo product;
        public List<ShippingOption> shippingOptions;
        public ShippingOption recommendedOption;
        public String timestamp;
        public Map<String, String> units;
    }

    @Schema(description = "Product details including weight and dimensions.")
    public static class ProductInfo {
        public String productId;
        public String name;
        public double weight;
        public Dimensions dimensions;
        public String category;
        public double price;
    }

    @Schema(description = "Product dimensions in centimeters.")
    public static class Dimensions {
        public double length;
        public double width;
        public double height;
    }

    @Schema(description = "Available shipping options for the product.")
    public static class ShippingOption {
        public String carrier;
        public String service;
        public int estimatedDeliveryDays;
        public double cost;
        public double carbonFootprint;
    }
}
