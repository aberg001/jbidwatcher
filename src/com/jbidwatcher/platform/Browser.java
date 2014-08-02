package com.jbidwatcher.platform;
/*
 * Copyright (c) 2000-2007, CyberFOX Software, Inc. All Rights Reserved.
 *
 * Developed by mrs (Morgan Schweers)
 */

import com.jbidwatcher.util.queue.MessageQueue;
import com.jbidwatcher.util.queue.MQFactory;
import com.jbidwatcher.util.config.JConfig;
import com.jbidwatcher.util.browser.WindowsBrowserLauncher;
import com.jbidwatcher.util.browser.BrowserLauncher;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Browser extends JConfig implements MessageQueue.Listener {
  private static Browser sInstance;

  public void messageAction(Object deQ) {
    String msg = (String)deQ;

    launchBrowser(msg);
  }

  public static String getBrowserCommand() {
    String osName = getOS();

    if(osName.equalsIgnoreCase("windows")) {
      return WindowsBrowserLauncher.getBrowser("http");
    } else {
      return "firefox";
    }
  }

  public static boolean launchBrowser(String url) {
    boolean browserLaunched = false;
    String errorMessage = "";

    if(Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(new URI(url));
        browserLaunched = true;
      } catch (URISyntaxException syntaxError) {
        errorMessage = ": " + syntaxError.getMessage();
      } catch (IOException launchFailed) {
        JConfig.log().logMessage("Failed to launch: " + launchFailed.getMessage());
      }
    }

    //  If it didn't launch, either because Desktop wasn't supported, or an error occurred, try again.
    if(!browserLaunched && errorMessage.isEmpty()) {
      //  Old/deprecated browser launching
      browserLaunched = oldBrowse(url);
    }

    if(!browserLaunched) {
      JConfig.log().logMessage("Could not browse to " + url + errorMessage);
    }

    return browserLaunched;
  }

  private static boolean oldBrowse(String url) {
    String osName = getOS();

    String launchCommand = JConfig.queryConfiguration("browser.launch." + osName);

    if(launchCommand == null) {
      launchCommand = JConfig.queryConfiguration("browser.launch");
      if(launchCommand == null) {
        launchCommand = "firefox";
      }
    }

    try {
      BrowserLauncher.openURL(url, launchCommand, JConfig.queryConfiguration("browser.override", "false").equals("true"));
    } catch(IOException e) {
      JConfig.log().handleException("Launching browser", e);
      return true;
    }
    return false;
  }

  public static void start() {
    if(sInstance == null) MQFactory.getConcrete("browse").registerListener(sInstance = new Browser());
  }
}
