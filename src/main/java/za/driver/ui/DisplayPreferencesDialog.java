package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import za.driver.model.CurrencyPreset;
import za.driver.model.DisplayPreferences;
import za.driver.presentation.CurrencyFormatter;
import za.driver.service.AppServices;

public class DisplayPreferencesDialog extends JDialog {

    private static final BigDecimal SAMPLE_PRICE = BigDecimal.valueOf(350_000);

    private final AppServices services;
    private final Runnable onSuccess;
    private final JComboBox<CurrencyPreset> presetCombo = new JComboBox<>(CurrencyPreset.values());
    private final JTextField customSymbolField = new JTextField(8);
    private final JTextField customLocaleField = new JTextField(12);
    private final JLabel previewLabel = new JLabel();
    private final JLabel customSymbolLabel = new JLabel("Symbol");
    private final JLabel customLocaleLabel = new JLabel("Locale tag");
    private final JPanel customPanel = new JPanel(new GridLayout(2, 2, 8, 8));

    public DisplayPreferencesDialog(JFrame owner, AppServices services, Runnable onSuccess) {
        super(owner, "Display Preferences", true);
        this.services = services;
        this.onSuccess = onSuccess;

        DisplayPreferences current = services.appConfigService.getDisplayPreferences();
        presetCombo.setSelectedItem(current.getPreset());
        customSymbolField.setText(current.getCustomSymbol() != null ? current.getCustomSymbol() : "");
        customLocaleField.setText(current.getCustomLocaleTag() != null ? current.getCustomLocaleTag() : "");

        presetCombo.addActionListener(e -> updateCustomFieldsEnabled());
        DocumentListener previewUpdater = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        };
        customSymbolField.getDocument().addDocumentListener(previewUpdater);
        customLocaleField.getDocument().addDocumentListener(previewUpdater);

        customPanel.add(customSymbolLabel);
        customPanel.add(customSymbolField);
        customPanel.add(customLocaleLabel);
        customPanel.add(customLocaleField);

        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 8));
        fields.add(new JLabel("Currency preset"));
        fields.add(presetCombo);
        fields.add(new JLabel(" "));
        fields.add(customPanel);
        fields.add(new JLabel("Preview"));
        fields.add(previewLabel);

        JLabel note = new JLabel(
                "<html>Prices are stored as plain numbers with no currency unit. "
                        + "This setting only changes symbols and formatting in the UI; amounts are not converted.</html>");

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener(e -> apply());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(fields, BorderLayout.CENTER);
        content.add(note, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        updateCustomFieldsEnabled();
        updatePreview();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private void updateCustomFieldsEnabled() {
        boolean custom = presetCombo.getSelectedItem() == CurrencyPreset.CUSTOM;
        customSymbolField.setEnabled(custom);
        customLocaleField.setEnabled(custom);
        customSymbolLabel.setEnabled(custom);
        customLocaleLabel.setEnabled(custom);
        updatePreview();
    }

    private void updatePreview() {
        previewLabel.setText(previewFormatter().format(SAMPLE_PRICE));
    }

    private CurrencyFormatter previewFormatter() {
        return new CurrencyFormatter(currentPreferences());
    }

    private DisplayPreferences currentPreferences() {
        DisplayPreferences prefs = new DisplayPreferences();
        prefs.setPreset((CurrencyPreset) presetCombo.getSelectedItem());
        prefs.setCustomSymbol(customSymbolField.getText().trim());
        prefs.setCustomLocaleTag(customLocaleField.getText().trim());
        return prefs;
    }

    private void apply() {
        DisplayPreferences prefs = currentPreferences();
        if (prefs.getPreset() == CurrencyPreset.CUSTOM) {
            if (prefs.getCustomSymbol() == null || prefs.getCustomSymbol().isBlank()
                    || prefs.getCustomLocaleTag() == null || prefs.getCustomLocaleTag().isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter both a symbol and locale tag for the custom preset.",
                        "Display Preferences",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        BackgroundTasks.run(
                this,
                () -> {
                    services.appConfigService.setDisplayPreferences(prefs);
                    return null;
                },
                ignored -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    dispose();
                },
                error -> BackgroundTasks.showError(this, "Save Failed", error));
    }
}
