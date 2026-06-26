package za.driver;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import za.driver.service.AppServices;
import za.driver.ui.BackgroundTasks;
import za.driver.ui.MainFrame;

public class Application {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception on " + thread.getName() + ": " + throwable);
            SwingUtilities.invokeLater(() -> BackgroundTasks.showError(
                    null,
                    "Unexpected Error",
                    throwable instanceof Exception exception
                            ? exception
                            : new RuntimeException(throwable)));
        });

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            try {
                AppServices services = AppServices.create();
                MainFrame frame = new MainFrame(services);
                frame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to start Driver: " + ex.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
