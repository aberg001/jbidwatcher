package com.jbidwatcher.ui;

import com.jbidwatcher.util.queue.MQFactory;
import com.jbidwatcher.util.queue.MessageQueue;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
* User: Morgan
* Date: Feb 11, 2009
* Time: 4:35:36 PM
* To change this template use File | Settings | File Templates.
*/
class TabStatusPanel extends JPanel {
  private JLabel mStatus;
  private JButton mCloseButton;
  private JProgressBar mLoadProgress;

  public TabStatusPanel(String tabName) {
    super(new BorderLayout());
    setBorder(LineBorder.createBlackLineBorder());
    mStatus = new JLabel("Status area");
    mStatus.setHorizontalAlignment(JLabel.CENTER);
    mCloseButton = new JButton("x");
    Insets i = mCloseButton.getMargin();
    System.err.println("(" + i.left + ", " + i.top + ") - (" + i.right + ", " + i.bottom + ")");
    i = new Insets(0, 0, 0, 0);
    mCloseButton.setMargin(i);
    mCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    mLoadProgress = new JProgressBar(0, 99);
    mLoadProgress.setVisible(false);
    add(mLoadProgress, BorderLayout.WEST);
    add(mStatus, BorderLayout.CENTER);
    add(mCloseButton, BorderLayout.EAST);
    setBackground(Color.decode("0xEDD400"));
    setVisible(false);
    String queueName = tabName + " Tab";
    MQFactory.addQueue(queueName, new SwingMessageQueue());
    MQFactory.getConcrete(queueName).registerListener(new MessageQueue.Listener() {
      public void messageAction(Object deQ) {
        final String cmd = deQ.toString();
        if (cmd.equals("HIDE")) setVisible(false);
        if (cmd.equals("SHOW")) setVisible(true);
        if (cmd.startsWith("PROGRESS")) markProgress(cmd, mLoadProgress);
        if (cmd.startsWith("REPORT")) {
          mStatus.setText(cmd.substring(7));
          setVisible(true);
        }
      }
    });
  }

  private void markProgress(String cmd, JProgressBar loadProgress) {
    if (cmd.length() < 9) {
      loadProgress.setVisible(!loadProgress.isVisible());
    } else {
      String levelCmd = cmd.substring(9);
      int level = loadProgress.getValue();
      try {
        level = Integer.parseInt(levelCmd);
      } catch (NumberFormatException e) {
        loadProgress.setStringPainted(true);
        loadProgress.setString(levelCmd);
      }
      loadProgress.setValue(level);
    }
  }
}
