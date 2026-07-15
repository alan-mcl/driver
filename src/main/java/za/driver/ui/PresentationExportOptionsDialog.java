package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import za.driver.presentation.CurrencyFormatter;
import za.driver.presentation.PresentationExportOptions;
import za.driver.presentation.PresentationGroupingMode;
import za.driver.service.AppServices;

public class PresentationExportOptionsDialog extends JDialog {

    private final JRadioButton bodyTypeRadio = new JRadioButton("Group by body type", true);
    private final JRadioButton priceBandRadio = new JRadioButton("Group by price band");
    private final JLabel hintLabel = new JLabel();
    private final CurrencyFormatter formatter;
    private PresentationExportOptions result;

    public PresentationExportOptionsDialog(JFrame owner, AppServices services) {
        super(owner, "Presentation Options", true);
        this.formatter = services.currencyFormatter;

        ButtonGroup group = new ButtonGroup();
        group.add(bodyTypeRadio);
        group.add(priceBandRadio);
        bodyTypeRadio.addActionListener(e -> updateHint());
        priceBandRadio.addActionListener(e -> updateHint());

        JPanel options = new JPanel();
        options.setLayout(new javax.swing.BoxLayout(options, javax.swing.BoxLayout.Y_AXIS));
        options.add(bodyTypeRadio);
        options.add(priceBandRadio);
        options.add(javax.swing.Box.createVerticalStrut(8));
        options.add(hintLabel);

        JButton exportButton = new JButton("Export");
        JButton cancelButton = new JButton("Cancel");
        exportButton.addActionListener(e -> confirm());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(exportButton);
        buttons.add(cancelButton);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(options, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        updateHint();
        getRootPane().setDefaultButton(exportButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private void updateHint() {
        if (priceBandRadio.isSelected()) {
            String bandExample = "< " + formatter.symbol() + "100k, < " + formatter.symbol() + "200k, …";
            hintLabel.setText(
                    "<html>Models are placed in bands at " + formatter.symbol()
                            + "100k steps based on their lowest effective price "
                            + "(dealer offer when set, otherwise list price).<br>"
                            + "Example sections: " + bandExample + "</html>");
        } else {
            hintLabel.setText(
                    "<html>Models are grouped into section slides by body type "
                            + "(for example Sedans, SUVs, Hatchbacks).<br>"
                            + "Within each section, models are ordered by overall score.</html>");
        }
    }

    private void confirm() {
        PresentationGroupingMode mode = priceBandRadio.isSelected()
                ? PresentationGroupingMode.PRICE_BAND
                : PresentationGroupingMode.BODY_TYPE;
        result = new PresentationExportOptions(mode);
        dispose();
    }

    public PresentationExportOptions showDialog() {
        setVisible(true);
        return result;
    }
}
