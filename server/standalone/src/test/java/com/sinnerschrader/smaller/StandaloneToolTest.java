package com.sinnerschrader.smaller;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.sinnerschrader.smaller.chain.Result;
import com.sinnerschrader.smaller.clients.common.Logger;
import com.sinnerschrader.smaller.clients.common.Util;
import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Zip;
import com.sinnerschrader.smaller.internal.Server;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;

/**
 * @author markusw
 */
public class StandaloneToolTest extends AbstractToolTest {

  private static ServerRunnable serverRunnable;

  private final Util util = new Util(new Logger() {
    @Override
    public void debug(final String message) {
      System.out.println(message);
    }
  });

  /** */
  @BeforeClass
  public static void startServer() {
    serverRunnable = new ServerRunnable();
    new Thread(serverRunnable).start();
    try {
      Thread.sleep(1500);
    } catch (final InterruptedException e) {
    }
  }

  /** */
  @AfterClass
  public static void stopServer() {
    serverRunnable.stop();
  }

  /**
   * @see com.sinnerschrader.smaller.AbstractBaseTest#runToolChain(java.lang.String,
   *      com.sinnerschrader.smaller.AbstractBaseTest.ToolChainCallback)
   */
  @Override
  protected void runToolChain(final String file,
      final ToolChainCallback callback) throws Exception {
    final Enumeration<URL> urls = getClass().getClassLoader()
        .getResources(file);
    if (!urls.hasMoreElements()) {
      fail(String.format("Test sources '%s' not found", file));
    }

    boolean deleteSource = false;
    File jarContent = null;
    File source = null;
    try {
      final URL url = urls.nextElement();
      if ("jar".equals(url.getProtocol())) {
        final int idx = url.getFile().indexOf('!');
        final String jar = url.getFile().substring(5, idx);
        final String entryPath = url.getFile().substring(idx + 1);
        jarContent = File.createTempFile("smaller-standalone-test-input",
            ".dir");
        deleteSource = true;
        jarContent.delete();
        jarContent.mkdirs();
        Zip.unzip(new File(jar), jarContent);
        source = new File(jarContent, entryPath);
      } else {
        source = new File(url.toURI().getPath());
      }

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Zip.zip(baos, source);
      final byte[] bytes = this.util.send("127.0.0.1", "1148",
          baos.toByteArray());
      final File zip = File.createTempFile("smaller-standalone-test-response",
          "zip");
      try {
        zip.delete();
        FileUtils.writeByteArrayToFile(zip, bytes);
        final File dir = File.createTempFile(
            "smaller-standalone-test-response", ".dir");
        try {
          dir.delete();
          dir.mkdirs();
          Zip.unzip(zip, dir);
          callback.test(mapResult(dir, getManifest(source)));
        } finally {
          FileUtils.deleteDirectory(dir);
        }
      } finally {
        zip.delete();
      }
    } finally {
      if (deleteSource && jarContent != null) {
        FileUtils.deleteDirectory(jarContent);
      }
    }
  }

  private Result mapResult(final File dir, final Manifest manifest)
      throws IOException {
    File js = null;
    File css = null;
    final String[] outs = manifest.getNext().getOut();
    if (outs.length > 1) {
      if (outs[0].endsWith("js")) {
        js = new File(dir, outs[0]);
        css = new File(dir, outs[1]);
      } else {
        css = new File(dir, outs[0]);
        js = new File(dir, outs[1]);
      }
    } else if (outs.length > 0) {
      if (outs[0].endsWith("js")) {
        js = new File(dir, outs[0]);
      } else {
        css = new File(dir, outs[0]);
      }
    }
    final Result result = new Result();
    if (js != null) {
      result.setJs(new StringResource(null, Type.JS, js.getAbsolutePath(),
          FileUtils.readFileToString(js)));
    }
    if (css != null) {
      result.setCss(new StringResource(null, Type.CSS, css.getAbsolutePath(),
          FileUtils.readFileToString(css)));
    }
    return result;
  }

  private static class ServerRunnable implements Runnable {

    private final Server server;

    public ServerRunnable() {
      this.server = new Server();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      this.server.start();
    }

    public void stop() {
      this.server.stop();
    }

  }

}
