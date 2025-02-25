package com.heroku.java.services;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/social/")
@Tag(name = "Services for Managing Social Cards for Marketing Campaigns")
public class SocialCardService {
    
    @Operation(summary = "Renders a custom social card for marketing campaigns.")
    @PostMapping("/renderCard")
    public RenderCardResponse renderCard(        
        @RequestBody(description = "Use the line1 and line2 properites to customize", content = @Content(schema = @Schema(implementation = RenderCardRequest.class))) @org.springframework.web.bind.annotation.RequestBody
        RenderCardRequest request) throws Exception {

        // Load the logo
        InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logo.png");
        if (logoStream == null) {
            throw new IllegalArgumentException("Logo not found in resources.");
        }
        BufferedImage logo = ImageIO.read(logoStream);

        // Desired logo width
        int logoWidth = 400;
        double aspectRatio = (double) logo.getHeight() / logo.getWidth();
        int logoHeight = (int) (logoWidth * aspectRatio);

        // Calculate the text dimensions
        Font font = new Font("Arial", Font.BOLD, 16);
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempGraphics = tempImage.createGraphics();
        tempGraphics.setFont(font);
        FontMetrics metrics = tempGraphics.getFontMetrics();
        int textWidth1 = metrics.stringWidth(request.line1); // Width of the first line
        int textWidth2 = metrics.stringWidth(request.line2); // Width of the second line
        int lineHeight = metrics.getHeight();
        tempGraphics.dispose();

        // Determine badge dimensions
        int padding = 15; // Reduced padding
        int textMargin = 5; // Reduced margin inside the message box
        int boxHeight = (2 * lineHeight) + 2 * textMargin; // Adjust height based on text size
        int badgeWidth = Math.max(Math.max(textWidth1, textWidth2) + 2 * (padding + textMargin), logoWidth + 2 * padding);
        int badgeHeight = logoHeight + boxHeight + (3 * padding); // Adjust height to remove extra space

        // Create a transparent image for the badge
        BufferedImage badge = new BufferedImage(badgeWidth, badgeHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = badge.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, badgeWidth, badgeHeight);

        // Resize the logo with high-quality interpolation
        BufferedImage resizedLogo = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gResize = resizedLogo.createGraphics();
        gResize.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gResize.drawImage(logo, 0, 0, logoWidth, logoHeight, null);
        gResize.dispose();

        // Draw the Heroku logo centered at the top
        int logoX = (badgeWidth - logoWidth) / 2;
        int logoY = padding;
        g2d.drawImage(resizedLogo, logoX, logoY, null);

        // Rotate and draw the text box at an opposite angle
        AffineTransform originalTransform = g2d.getTransform();
        double angle = Math.toRadians(10); // Rotate clockwise by 10 degrees (opposite direction)
        int boxX = (badgeWidth - Math.max(textWidth1, textWidth2) - 2 * textMargin) / 2;
        int boxY = logoY + logoHeight; // Position directly below the logo with minimal padding
        g2d.rotate(angle, boxX + Math.max(textWidth1, textWidth2) / 2.0 + textMargin, boxY + boxHeight / 2.0); // Rotate around the center of the box

        // Draw a drop shadow for the text box
        g2d.setColor(new Color(0, 0, 0, 128)); // Semi-transparent black for the shadow
        g2d.fillRect(boxX + 5, boxY + 5, Math.max(textWidth1, textWidth2) + 2 * textMargin, boxHeight); // Offset shadow

        // Draw the angled white box
        g2d.setColor(Color.WHITE);
        g2d.fillRect(boxX, boxY, Math.max(textWidth1, textWidth2) + 2 * textMargin, boxHeight);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(boxX, boxY, Math.max(textWidth1, textWidth2) + 2 * textMargin, boxHeight);

        // Draw the two lines of text centered within the box
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        // Calculate independent horizontal positions for each line
        int textX1 = boxX + ((Math.max(textWidth1, textWidth2) + 2 * textMargin - textWidth1) / 2); // Center line1 horizontally
        int textX2 = boxX + ((Math.max(textWidth1, textWidth2) + 2 * textMargin - textWidth2) / 2); // Center line2 horizontally
        int textYStart = boxY + textMargin + metrics.getAscent(); // Vertical position of the first line
        g2d.drawString(request.line1, textX1, textYStart); // First line
        g2d.drawString(request.line2, textX2, textYStart + lineHeight); // Second line

        // Restore the original transformation
        g2d.setTransform(originalTransform);
        g2d.dispose();

        // Convert to Base64-encoded PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(badge, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        RenderCardResponse response = new RenderCardResponse();
        response.socialCard = Base64.getEncoder().encodeToString(imageBytes);
        return response;
    }

    @Schema(description = "Request to customize the rendering of the social card.")
    public static class RenderCardRequest {
        public String line1;
        public String line2;
    }

    @Schema(description = "Response from rendering social card contains base64 encoded png image.")
    public static class RenderCardResponse {
        public String socialCard;
    }
}
