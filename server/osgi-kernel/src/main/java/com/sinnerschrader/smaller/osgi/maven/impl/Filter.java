package com.sinnerschrader.smaller.osgi.maven.impl;

import java.util.Arrays;
import java.util.List;

/**
 * @author markusw
 */
public interface Filter {

  /**
   * @param pom
   *          The {@link Pom} to apply the filter on
   * @return Returns true if the given {@link Pom} should be included, false
   *         otherwise.
   */
  boolean accept(Pom pom);

  /** */
  public static class CompoundFilter implements Filter {

    private final List<Filter> filters;

    /**
     * @param filters
     */
    public CompoundFilter(final Filter... filters) {
      this.filters = Arrays.asList(filters);
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      boolean accept = true;
      for (final Filter filter : filters) {
        accept &= filter.accept(pom);
      }
      return accept;
    }

  }

  /** */
  public static class AcceptAll implements Filter {

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return true;
    }

  }

  /** */
  public static class AcceptScopes implements Filter {

    private final List<String> scopes;

    /**
     * @param scopes
     */
    public AcceptScopes(final String... scopes) {
      this.scopes = Arrays.asList(scopes);
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return scopes.contains(pom.getScope());
    }

  }

  /** */
  public static class AcceptTypes implements Filter {

    private final List<String> types;

    /**
     * @param types
     */
    public AcceptTypes(final String... types) {
      this.types = Arrays.asList(types);
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return types.contains(pom.getType());
    }

  }

  /** */
  public static class NotAcceptTypes implements Filter {

    private final List<String> types;

    /**
     * @param types
     */
    public NotAcceptTypes(final String... types) {
      this.types = Arrays.asList(types);
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return !types.contains(pom.getType());
    }

  }

  /** */
  public static class AcceptOptional implements Filter {

    private final Boolean optional;

    /**
     * @param optional
     */
    public AcceptOptional(final boolean optional) {
      this.optional = optional;
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return optional.equals(pom.isOptional());
    }

  }

}
