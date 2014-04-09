package se.st.cs.uni_saarland.de.longreachbluethooth.GUI.user;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.Zombie;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.*;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class represents a user GUI for a more
 * convenient usage of the stuff we already did
 */
public class UserGuiConnected extends JPanel implements ActionListener {

    public final int WIDTH = 500;
    public final int HEIGHT = 100;

    private UserInterface iface;
    public static final String FTA_START = "send file [R]";
    public static final String FTA_STOP  = "send file [S]";
    JButton fileTransferActive;
    public static final String FTP_START = "rec file [R]";
    public static final String FTP_STOP  = "rec file [S]";
    JButton fileTransferPassive;
    public static final String KEY_START = "keyb. [R]";
    public static final String KEY_STOP  = "keyb. [S]";
    JButton keyboardInteraction;
    JButton disconnect;

    public UserGuiConnected(UserInterface iface) {
        this.iface = iface;
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.add(new JPanel());
        fileTransferActive = new JButton(FTA_START);
        fileTransferActive.addActionListener(this);
        this.add(fileTransferActive);
        this.add(new JPanel());
        fileTransferPassive = new JButton(FTP_START);
        fileTransferPassive.addActionListener(this);
        this.add(fileTransferPassive);
        this.add(new JPanel());
        keyboardInteraction = new JButton(KEY_START);
        keyboardInteraction.addActionListener(this);
        this.add(keyboardInteraction);
        this.add(new JPanel());
        disconnect = new JButton("disconnect");
        disconnect.addActionListener(this);
        this.add(disconnect);
        this.add(new JPanel());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            if(actionEvent.getSource() == fileTransferActive) {
                if(fileTransferActive.getText().compareTo(FTA_START) == 0) {
                   iface.startService(ServiceName.FileTransferActive);
                   fileTransferActive.setText(FTA_STOP);
                } else {
                    iface.stopService(ServiceName.FileTransferActive);
                    fileTransferActive.setText(FTA_START);
                }
            }
            if(actionEvent.getSource() == fileTransferPassive) {
                if(fileTransferPassive.getText().compareTo(FTP_START) == 0) {
                    iface.startService(ServiceName.FileTransferPassive);
                    fileTransferPassive.setText(FTP_STOP);
                } else {
                    iface.stopService(ServiceName.FileTransferPassive);
                    fileTransferPassive.setText(FTP_START);
                }
            }
            if(actionEvent.getSource() == keyboardInteraction) {
                if(keyboardInteraction.getText().compareTo(KEY_START) == 0) {
                    iface.startService(ServiceName.KeyboardInteraction);
                    keyboardInteraction.setText(KEY_STOP);
                } else {
                    iface.stopService(ServiceName.KeyboardInteraction);
                    keyboardInteraction.setText(KEY_START);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Service Exception", JOptionPane.ERROR_MESSAGE);
        }
        if(actionEvent.getSource() == disconnect) {
            try {
                iface.disconnect();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,e.getMessage(),"Connection Exception",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void serviceHasStopped(ServiceName name) {
        switch (name) {
            case KeyboardInteraction:
                keyboardInteraction.setText(KEY_START);
                break;

            case FileTransferActive:
                fileTransferActive.setText(FTA_START);
                break;

            case FileTransferPassive:
                fileTransferPassive.setText(FTP_START);
                break;
        }
    }
}
