package com.sinnerschrader.smaller.resource;

import java.io.IOException;

/**
 * @author marwol
 */
public interface Processor {

  /**
   * @param type
   * @return True if the given type can be handled by this processor
   */
  boolean supportsType(Type type);

  /**
   * @return Returns true if this {@link Processor} is able to merge resources,
   *         false otherwise
   */
  boolean canMerge();

  /**
   * @param context
   * @param resource
   * @return Returns the transformed source
   * @throws IOException
   */
  Resource execute(Resource resource) throws IOException;

}