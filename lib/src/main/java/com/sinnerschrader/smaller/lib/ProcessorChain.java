package com.sinnerschrader.smaller.lib;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.sinnerschrader.smaller.common.Manifest.Task;
import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.lib.processors.Processor;
import com.sinnerschrader.smaller.lib.resource.MultiResource;
import com.sinnerschrader.smaller.lib.resource.RelativeFileResourceResolver;
import com.sinnerschrader.smaller.lib.resource.Resource;

/**
 * @author marwol
 */
public class ProcessorChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorChain.class);

  /**
   * @param inputDir
   * @param outputDir
   * @param task
   * @throws IOException
   */
  public void execute(final String inputDir, final File outputDir, final Task task) {
    try {
      Resource jsSource = getMergedSourceFiles(inputDir, task, Type.JS);
      Resource cssSource = getMergedSourceFiles(inputDir, task, Type.CSS);

      String processors = task.getProcessor();
      LOGGER.info("Building processor chain: {}", processors);
      validate(task);
      if (processors.indexOf("merge") == -1) {
        processors = "merge," + processors;
      }
      for (final String name : processors.split(",")) {
        final Processor processor = createProcessor(name);
        if (processor != null) {
          LOGGER.info("Executing processor {}", name);
          if (processor.supportsType(Type.JS)) {
            jsSource = jsSource.apply(processor);
          }
          if (processor.supportsType(Type.CSS)) {
            cssSource = cssSource.apply(processor);
          }
        }
      }

      writeResult(outputDir, task, jsSource, Type.JS);
      writeResult(outputDir, task, cssSource, Type.CSS);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  private boolean validate(final Task task) {
    final String[] processors = task.getProcessor().toLowerCase().split(",");
    boolean cssembedFound = false;
    for (final String processor : processors) {
      if (processor.equals("cssembed")) {
        cssembedFound = true;
      } else if (processor.equals("yuicompressor") && cssembedFound) {
        throw new SmallerException("yuiCompressor must run before cssembed");
      }
    }

    return true;
  }

  private void writeResult(final File output, final Task task, final Resource resource, final Type type) throws IOException {
    final String outputFile = getTargetFile(output, task.getOut(), type);
    if (outputFile != null) {
      FileUtils.writeStringToFile(new File(outputFile), resource.getContents());
    }
  }

  private Resource getMergedSourceFiles(final String base, final Task task, final Type type) throws IOException {
    String multipath = null;
    List<String> files = Lists.newArrayList();
    for (String in : task.getIn()) {
      String path = new File(base, in).getAbsolutePath();
      String ext = FilenameUtils.getExtension(path);
      if (type == Type.JS && isJsSourceFile(ext)) {
        files.add(path);
      } else if (type == Type.JS && ext.equals("json")) {
        multipath = path;
        files.add(path);
      } else if (type == Type.CSS && isCssSourceFile(ext)) {
        files.add(path);
      }
      if (multipath == null) {
        multipath = path;
      }
    }
    return new MultiResource(multipath, new SourceMerger().getResources(new RelativeFileResourceResolver(base), files));
  }

  private boolean isJsSourceFile(final String ext) {
    return ext.equals("js") || ext.equals("coffee");
  }

  private boolean isCssSourceFile(final String ext) {
    return ext.equals("css") || ext.equals("less") || ext.equals("sass");
  }

  private String getTargetFile(final File base, final String[] out, final Type type) {
    String target = null;
    for (final String s : out) {
      final String ext = FilenameUtils.getExtension(s);
      switch (type) {
      case JS:
        if (ext.equals("js")) {
          target = new File(base, s).getAbsolutePath();
        }
        break;
      case CSS:
        if (ext.equals("css")) {
          target = new File(base, s).getAbsolutePath();
        }
        break;
      }
    }
    return target;
  }

  private Processor createProcessor(final String name) {
    try {
      return (Processor) Class.forName("com.sinnerschrader.smaller.lib.processors." + StringUtils.capitalize(name.toLowerCase()) + "Processor").newInstance();
    } catch (final InstantiationException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final IllegalAccessException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final ClassNotFoundException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    }
    return null;
  }

  /** */
  public enum Type {
    /** */
    JS,
    /** */
    CSS
  }

}
