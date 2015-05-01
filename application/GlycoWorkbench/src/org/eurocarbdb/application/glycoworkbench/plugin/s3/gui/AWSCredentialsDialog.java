/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 *
 * Copyright 2006 James Murty, 2008 Zmanda Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eurocarbdb.application.glycoworkbench.plugin.s3.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.jets3t.gui.ErrorDialog;
import org.jets3t.gui.HyperlinkActivatedListener;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSDevPayCredentials;

/**
 * Dialog box for obtaining a user's AWS Credentials, where the dialog is simply
 * a wrapping for a {@link LoginCredentialsPanel}.
 *
 * @author James Murty
 * @author Nikolas Coukouma
 */
public class AWSCredentialsDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = -8201015667689728582L;

    private LoginCredentialsPanel loginCredentialsPanel = null;
    private JButton okButton = null;
    private boolean isConfirmed = false;

    private final Insets insetsZero = new Insets(0, 0, 0, 0);
    private final Insets insetsDefault = new Insets(3, 5, 3, 5);

    /**
     * Displays a dialog box prompting for a user's AWS credentials
     *
     * @param ownerFrame
     * the frame that will own the dialog
     * @param askForFriendlyName
     * if true, the dialog will prompt the user for a "friendly" name they want to give to their
     * AWS credentials - such as a nickname they can use to distinguish between multiple AWS accounts.
     * @param hyperlinkListener
     * the listener that will act on any hyperlink events triggered by the user clicking on HTTP links.
     */
    public AWSCredentialsDialog(Frame ownerFrame, boolean askForFriendlyName, HyperlinkActivatedListener hyperlinkListener) {
        super(ownerFrame, "AWS Credentials", true);

        this.loginCredentialsPanel = new LoginCredentialsPanel(askForFriendlyName, hyperlinkListener);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        buttonsPanel.add(cancelButton, new GridBagConstraints(0, 0,
            1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insetsZero, 0, 0));
        buttonsPanel.add(okButton, new GridBagConstraints(1, 0,
            1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insetsZero, 0, 0));

        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(loginCredentialsPanel, new GridBagConstraints(0, 0,
            1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insetsDefault, 0, 0));
        this.getContentPane().add(buttonsPanel, new GridBagConstraints(0, 1,
            1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetsDefault, 0, 0));

        // Set default ENTER and ESCAPE buttons.
        this.getRootPane().setDefaultButton(okButton);
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            private static final long serialVersionUID = -6225706489569112809L;

            public void actionPerformed(ActionEvent actionEvent) {
                isConfirmed = false;
                setVisible(false);
            }
        });

        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(ownerFrame);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(okButton)) {
            String[] inputErrors = loginCredentialsPanel.checkForInputErrors();
            if (inputErrors.length == 0) {
                isConfirmed = true;
                this.setVisible(false);
            } else {
                // Sanity-check provided information
                String errorMessages = "<html>Please correct the following errors:<ul>";
                for (int i = 0; i < inputErrors.length; i++) {
                    errorMessages += "<li>" + inputErrors[i] + "</li>";
                }
                errorMessages += "</ul></html>";

                ErrorDialog.showDialog(this, null, errorMessages, null);
            }
        } else if ("Cancel".equals(e.getActionCommand())) {
            isConfirmed = false;
            this.setVisible(false);
        }
     }

    /**
     * @return
     * true if the OK button was pressed, false otherwise (ie if the dialog was cancelled)
     */
    public boolean isConfirmed() {
        return isConfirmed;
    }

    /**
     * @return
     * the AWS Access Key provided by the user.
     */
    public String getAWSAccessKey() {
        return loginCredentialsPanel.getAWSAccessKey().trim();
    }

    /**
     * @return
     * the AWS Secret Key provided by the user.
     */
    public String getAWSSecretKey() {
        return loginCredentialsPanel.getAWSSecretKey().trim();
    }

    /**
     * @return
     * whether or not DevPay authentication should be used
     */
    public boolean getUsingDevPay() {
        return loginCredentialsPanel.getUsingDevPay();
    }

    /**
     * @return
     * the AWS User Token provided by the user.
     */
    public String getAWSUserToken() {
        return loginCredentialsPanel.getAWSUserToken().trim();
    }

    /**
     * @return
     * the AWS Product Token provided by the user.
     */
    public String getAWSProductToken() {
        return loginCredentialsPanel.getAWSProductToken().trim();
    }

    /**
     * @return
     * the Friendly Name (nickname) provided by the user, or an empty string if the user was not
     * prompted to provide one (the askForFriendlyName option was false).
     */
    public String getFriendlyName() {
        return loginCredentialsPanel.getFriendlyName().trim();
    }

    /**
     * Displays a dialog box prompting for a user's AWS credentials.
     *
     * @param ownerFrame
     * the frame that will own the dialog
     * @param askForFriendlyName
     * if true, the dialog will prompt the user for a "friendly" name they want to give to their
     * AWS credentials - such as a nickname they can use to distinguish between multiple AWS accounts.
     * @param hyperlinkListener
     * the listener that will act on any hyperlink events triggered by the user clicking on HTTP links.
     */
    public static AWSCredentials showDialog(Frame ownerFrame, boolean askForFriendlyName, HyperlinkActivatedListener hyperlinkListener) {
        AWSCredentialsDialog dialog = new AWSCredentialsDialog(
            ownerFrame, askForFriendlyName, hyperlinkListener);
        dialog.setVisible(true);

        AWSCredentials awsCredentials = null;
        if (dialog.isConfirmed()) {
            if (dialog.getUsingDevPay()) {
                awsCredentials = new AWSDevPayCredentials(
                    dialog.getAWSAccessKey(),
                    dialog.getAWSSecretKey(),
                    dialog.getAWSUserToken(),
                    dialog.getAWSProductToken(),
                    dialog.getFriendlyName());
            } else {
                awsCredentials = new AWSCredentials(
                    dialog.getAWSAccessKey(),
                    dialog.getAWSSecretKey(),
                    dialog.getFriendlyName());
            }
        } else {
            awsCredentials = null;
        }
        dialog.dispose();
        return awsCredentials;
    }

}
