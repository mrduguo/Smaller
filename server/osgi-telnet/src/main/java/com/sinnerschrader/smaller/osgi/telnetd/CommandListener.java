package com.sinnerschrader.smaller.osgi.telnetd;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sinnerschrader.smaller.osgi.maven.MavenInstaller;

/**
 * @author markusw
 */
public class CommandListener extends Thread {

  private final MavenInstaller maven;

  /**
   * @param maven
   *          The {@link MavenInstaller} to use
   */
  public CommandListener(final MavenInstaller maven) {
    super();
    this.maven = maven;

    setDaemon(true);
    start();
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    ServerSocket server = null;
    try {
      server = new ServerSocket(1149);
      server.setReuseAddress(true);
      while (true) {
        handleClient(server.accept());
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (server != null) {
        try {
          server.close();
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void handleClient(final Socket client) {
    try {
      BufferedInputStream in = null;
      try {
        in = new BufferedInputStream(client.getInputStream());
        this.maven.installOrUpdate(readCommand(in).trim());
      } finally {
        if (in != null) {
          in.close();
        }
        client.close();
      }
    } catch (final IOException e) {
      // Client failed
      e.printStackTrace();
    }
  }

  private String readCommand(final InputStream in) throws IOException {
    final StringBuilder buf = new StringBuilder();
    char c = (char) in.read();
    while (c != '\n') {
      buf.append(c);
      c = (char) in.read();
    }
    return buf.toString();
  }

}
