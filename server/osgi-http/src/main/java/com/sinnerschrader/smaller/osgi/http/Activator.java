package com.sinnerschrader.smaller.osgi.http;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import com.sinnerschrader.smaller.lib.ProcessorChain;
import com.sinnerschrader.smaller.resource.impl.OsgiServiceProcessorFactory;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ProcessorChain chain;

  private ServiceTracker tracker;

  private final Set<HttpService> services = new HashSet<HttpService>();

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    this.chain = new ProcessorChain(new OsgiServiceProcessorFactory(context));

    this.tracker = new ServiceTracker(context, HttpService.class.getName(),
        null) {
      /**
       * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
       */
      @Override
      public Object addingService(final ServiceReference reference) {
        final HttpService service = (HttpService) super
            .addingService(reference);
        if (!Activator.this.services.contains(service)) {
          try {
            service.registerServlet("/", new Servlet(Activator.this.chain),
                null, null);
            Activator.this.services.add(service);
          } catch (final ServletException e) {
            e.printStackTrace();
          } catch (final NamespaceException e) {
            e.printStackTrace();
          }
        }
        return service;
      }

      /**
       * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
       *      java.lang.Object)
       */
      @Override
      public void removedService(final ServiceReference reference,
          final Object o) {
        final HttpService service = (HttpService) o;
        service.unregister("/");
        Activator.this.services.remove(service);
        super.removedService(reference, service);
      }
    };
    this.tracker.open();
    final HttpService service = (HttpService) this.tracker.getService();
    if (service != null) {
      try {
        service.registerServlet("/", new Servlet(this.chain), null, null);
        this.services.add(service);
      } catch (final NamespaceException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    for (final HttpService service : this.services) {
      service.unregister("/");
    }
    this.services.clear();
    if (this.tracker != null) {
      this.tracker.close();
      this.tracker = null;
    }
  }

}
