package com.tagtraum.perf.gcviewer.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Display GC details.
 */
public class GCDetailsView extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String CANCEL_ACTION_MAP_KEY = "cancel";

    private JLabel msgLabel;
    private JPopupMenu popup;

    public GCDetailsView(JPopupMenu popup) {
        this.popup = popup;

        msgLabel = new JLabel();
        msgLabel.setVisible(false);
        add(msgLabel);

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_ACTION_MAP_KEY);
        getActionMap().put(CANCEL_ACTION_MAP_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                GCDetailsView.this.popup.setVisible(false);
            }
        });
    }

    public void setText(String content) {
        msgLabel.setText(content);
        Dimension size = msgLabel.getPreferredSize();
        msgLabel.setMinimumSize(size);
        msgLabel.setPreferredSize(size);
        msgLabel.setText(content);
        msgLabel.setVisible(true);
    }
}
