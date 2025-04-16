package com.heroku.java.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
    
import java.text.NumberFormat;
import java.util.Locale;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "Finance Agreement Calculation", description = "Calculates finance agreements for car purchases based on valuation, credit status, and business margins.")
@RestController
@RequestMapping("/api/finance/")
public class FinanceAgreementService {

    @Operation(
        summary = "Calculate a Finance Agreement",
        description = "Processes a finance agreement based on car valuation, customer credit profile, business margin constraints, and competitor pricing.",
        responses = { 
            @ApiResponse(responseCode = "200", description = "Response containing the calculated finance agreement."),
            @ApiResponse(responseCode = "404", description = "Vehicle not found in Salesforce."),
            @ApiResponse(responseCode = "503", description = "Salesforce connection error."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
        })
    @PostMapping("/calculateFinanceAgreement")
    public FinanceCalculationResponse calculateFinanceAgreement(
            @org.springframework.web.bind.annotation.RequestBody
            @RequestBody(
                description = "Request to compute a finance agreement for a car purchase, including the Salesforce record ID of both the customer applying for financing and the vehicle being financed.", 
                content = @Content(schema = @Schema(implementation = FinanceCalculationRequest.class)))
            FinanceCalculationRequest request,
            HttpServletRequest httpServletRequest) {
            
        // Obtain Salesforce connection for Heroku Integration add-on
        PartnerConnection connection = (PartnerConnection) httpServletRequest.getAttribute("salesforcePartnerConnection");
        if (connection == null)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Salesforce connection is not available.");

        try {
            // Query Vehicle information from Salesforce
            String soql = String.format(
                "SELECT Id, Price__c FROM Vehicle_Model__c WHERE Id = '%s' ", 
                request.vehicleId);
            QueryResult queryResult = connection.query(soql);

            // If no vehicle is found, return 404 error
            if (queryResult.getSize() == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found in Salesforce.");
            }

            // Retrieve vehicle price
            SObject vehicle = queryResult.getRecords()[0];
            double vehiclePrice = Double.parseDouble(vehicle.getField("Price__c").toString());

            // Simple finance calculations
            double loanAmount = vehiclePrice - request.downPayment;
            double annualInterestRate = Math.min(3.5, request.maxInterestRate); // Cap interest rate at 3.5% for demo
            int loanTermMonths = request.years * 12;

            // Monthly Payment Calculation (Basic Loan Formula)
            double monthlyInterestRate = (annualInterestRate / 100) / 12;
            double monthlyPayment = (loanAmount * monthlyInterestRate) / (1 - Math.pow(1 + monthlyInterestRate, -loanTermMonths));            
            double totalFinancingCost = monthlyPayment * loanTermMonths;

            // Build response
            FinanceCalculationResponse response = new FinanceCalculationResponse();
            response.recommendedFinanceOffer = new FinanceOffer();
            response.recommendedFinanceOffer.finalCarPrice = vehiclePrice;
            response.recommendedFinanceOffer.adjustedInterestRate = annualInterestRate;
            response.recommendedFinanceOffer.monthlyPayment = monthlyPayment;
            response.recommendedFinanceOffer.loanTermMonths = loanTermMonths;
            response.recommendedFinanceOffer.totalFinancingCost = totalFinancingCost;

            // Generate PDF with logo
            byte[] pdfBytes = generatePdf(response);

            // Convert PDF to Base64 and insert as ContentVersion
            SObject contentVersion = new SObject();
            contentVersion.setType("ContentVersion");
            contentVersion.setField("Title", "Finance_Agreement.pdf");
            contentVersion.setField("PathOnClient", "Finance_Agreement.pdf");
            contentVersion.setField("VersionData", pdfBytes);
            contentVersion.setField("FirstPublishLocationId", request.customerId);
            SaveResult[] saveResults = connection.create(new SObject[]{contentVersion});
            if (!saveResults[0].isSuccess())
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to upload PDF to Salesforce: " + saveResults[0].getErrors()[0].getMessage());

            return response;

        } catch (ConnectionException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to connect to Salesforce.", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }

    /**
     * Generates a PDF document containing the finance agreement details, with a company logo.
     */
    private byte[] generatePdf(FinanceCalculationResponse response) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Define a number format for currency (US format with commas and two decimal places)
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            // Load and center the logo
            InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("koacars.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                ImageData imageData = ImageDataFactory.create(logoBytes);
                Image logo = new Image(imageData);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);                
                Paragraph logoParagraph = new Paragraph().add(logo).setTextAlignment(TextAlignment.CENTER);
                document.add(logoParagraph);
            }

            // Fake address
            document.add(new Paragraph("KOA Cars, 1234 Auto Lane, Springfield, USA")
                .setFont(regularFont).setTextAlignment(TextAlignment.CENTER));

            // Add date and time
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            document.add(new Paragraph("Generated on: " + dateTime).setFont(regularFont).setTextAlignment(TextAlignment.CENTER));

            // Add Finance Agreement Summary Title
            document.add(new Paragraph("Finance Agreement Summary").setFont(boldFont).setTextAlignment(TextAlignment.CENTER));

            // Create a table with two columns containing agreement details
            float[] columnWidths = {250f, 250f}; 
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            addTableRow(table, "Final Car Price:", currencyFormat.format(response.recommendedFinanceOffer.finalCarPrice), boldFont, regularFont);
            addTableRow(table, "Adjusted Interest Rate:", response.recommendedFinanceOffer.adjustedInterestRate + "%", boldFont, regularFont);
            addTableRow(table, "Monthly Payment:", currencyFormat.format(response.recommendedFinanceOffer.monthlyPayment), boldFont, regularFont);
            addTableRow(table, "Loan Term:", response.recommendedFinanceOffer.loanTermMonths + " months", boldFont, regularFont);
            addTableRow(table, "Total Financing Cost:", currencyFormat.format(response.recommendedFinanceOffer.totalFinancingCost), boldFont, regularFont);
            document.add(table);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    /**
     * Adds a row to the finance details table.
     */
    private void addTableRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(value).setFont(regularFont)).setBorder(Border.NO_BORDER));
    }
    
    @Schema(description = "Request to compute a finance agreement for a car purchase, including the Salesforce record ID of both the customer applying for financing and the vehicle being financed.")
    public static class FinanceCalculationRequest {
        @Schema(example = "0035g00000XyZbHAZ", description = "The Salesforce record ID of the customer applying for financing.")
        public String customerId;
        @Schema(example = "a0B5g00000LkVnWEAV", description = "The Salesforce record ID of the car being financed.")
        public String vehicleId;
        @Schema(example = "3.5", description = "The maximum interest rate the user is prepared to go to (percentage).")
        public double maxInterestRate;
        @Schema(example = "1000", description = "The down payment the user is prepared to give.")
        public double downPayment;
        @Schema(example = "3", description = "The number of years to pay the finance the user is requesting.")
        public int years;
    }

    @Schema(description = "Response containing the calculated finance agreement.")
    public static class FinanceCalculationResponse {
        public FinanceOffer recommendedFinanceOffer;
    }

    @Schema(description = "Recommended finance offer based on business rules and customer affordability.")
    public static class FinanceOffer {
        public double finalCarPrice;
        public double adjustedInterestRate;
        public double monthlyPayment;
        public int loanTermMonths;
        public double totalFinancingCost;
    }
}
