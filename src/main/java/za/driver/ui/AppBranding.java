package za.driver.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

final class AppBranding {

    private static final String ICON_RESOURCE = "/icon.png";
    private static final String LOGO_RESOURCE = "/logo.png";

    private AppBranding() {
    }

    static ImageIcon loadLogo(int maxWidthPx) {
        BufferedImage image = loadImage(LOGO_RESOURCE);
        if (image == null) {
            return null;
        }
        if (image.getWidth() <= maxWidthPx) {
            return new ImageIcon(image);
        }
        int height = Math.max(1, (int) Math.round(image.getHeight() * ((double) maxWidthPx / image.getWidth())));
        return new ImageIcon(scaleImage(image, maxWidthPx, height));
    }

    static List<Image> loadWindowIcons() {
        BufferedImage image = loadImage(ICON_RESOURCE);
        if (image == null) {
            return List.of();
        }
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};
        List<Image> icons = new ArrayList<>(sizes.length);
        for (int size : sizes) {
            icons.add(scaleImage(image, size, size));
        }
        return icons;
    }

    private static BufferedImage loadImage(String resourcePath) {
        try (InputStream input = AppBranding.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return null;
            }
            return ImageIO.read(input);
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }
}
