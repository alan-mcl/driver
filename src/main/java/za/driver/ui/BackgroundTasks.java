package za.driver.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public final class BackgroundTasks {

    private BackgroundTasks() {
    }

    public static <T> void run(
            Component parent,
            Callable<T> background,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {
        parent.setEnabled(false);
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return background.call();
            }

            @Override
            protected void done() {
                parent.setEnabled(true);
                parent.setCursor(Cursor.getDefaultCursor());
                try {
                    onSuccess.accept(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof Exception exception) {
                        onError.accept(exception);
                    } else {
                        onError.accept(new RuntimeException(cause));
                    }
                }
            }
        }.execute();
    }

    public static void showError(Component parent, String title, Exception error) {
        String message = error.getMessage() != null ? error.getMessage() : error.toString();
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
