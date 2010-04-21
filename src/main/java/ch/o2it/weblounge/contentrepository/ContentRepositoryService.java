/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.contentrepository;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.bundle.BundleContentRepository;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Service that will watch out for sites, associate them with a content
 * repository if needed and then register it with the
 * <code>ContentRepositoryFactory</code> to allow for static lookup of content
 * repositories.
 * <p>
 * Using the key <code>ch.o2it.weblounge.contentrepository</code>, the concrete
 * implementation can be specified. If this property cannot be found, an
 * instance of the <code>BundleContentRepository</code> will be created which
 * will serve pages and resources from the site's bundle.
 */
public class ContentRepositoryService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContentRepositoryService.class);

  /** Configuration key for the content repository implementation */
  public static final String CONTENT_REPOSITORY = "weblounge.contentrepository";

  /** Default implementation for a content repository */
  public static final String DEFAULT_REPOSITORY_TYPE = BundleContentRepository.class.getName();

  /** The prototype class */
  protected Class<? extends ContentRepository> prototype = null;

  /** The site tracker */
  private SiteTracker siteTracker = null;

  /**
   * Callback from the OSGi environment to activate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  @SuppressWarnings("unchecked")
  public void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    Object prototypeClassName = context.getProperties().get(CONTENT_REPOSITORY);

    logger.info("Starting content repository factory");

    // Try to load the implementation from the system configuration
    if (prototypeClassName != null) {
      logger.info("Creating content repository prototype from " + prototypeClassName);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      prototype = (Class<? extends ContentRepository>) loader.loadClass((String) prototypeClassName);
    }

    // No luck here. Let's try the default
    else {
      logger.warn("No content repository implementation specified. Using bundle content repository");
      prototype = (Class<? extends ContentRepository>) Class.forName(DEFAULT_REPOSITORY_TYPE);
    }

    // Test creation of the prototype
    prototype.newInstance();

    siteTracker = new SiteTracker(this, bundleContext);
    siteTracker.open();

    logger.debug("Content repository factory activated");
  }

  /**
   * Callback from the OSGi environment to deactivate the service.
   * <p>
   * This method is configured in the <tt>Dynamic Services</tt> section of the
   * bundle.
   * 
   * @param context
   *          the component context
   */
  public void deactivate(ComponentContext context) {
    logger.debug("Deactivating content repository factory");
    siteTracker.close();
    siteTracker = null;
    logger.info("Content repository factory stopped");
  }

  /**
   * Callback from the OSGi environment when a new site is created. This method
   * will create a <code>ContentRepository</code> using
   * {@link #newContentRepository(Site)} and register it with the site.
   * 
   * @param site
   *          the site
   */
  @SuppressWarnings("unchecked")
  public void registerSite(Site site, Bundle bundle) {
    ContentRepository repository = null;
    Dictionary properties = new Hashtable();
    properties.put(Site.class.getName(), site);
    properties.put(Bundle.class.getName(), bundle);
    try {
      repository = prototype.newInstance();
      repository.connect(properties);
      ContentRepositoryFactory.register(site, repository);
    } catch (InstantiationException e) {
      logger.error("Unable to instantiate content repository " + prototype + " for site '" + site + "'", e);
    } catch (IllegalAccessException e) {
      logger.error("Illegal access while instantiating content repository " + prototype + " for site '" + site + "'", e);
    } catch (ContentRepositoryException e) {
      logger.error("Unable to connect content repository " + repository + " for site '" + site + "'", e);
    }
  }

  /**
   * Callback from the OSGi environment when a site is destroyed. This method
   * will close the <code>ContentRepository</code> an dispose of any references.
   * 
   * @param site
   *          the site
   */
  public void unregisterSite(Site site) {
    ContentRepository repository = ContentRepositoryFactory.unregister(site);
    if (repository == null)
      return;
    try {
      repository.disconnect();
    } catch (ContentRepositoryException e) {
      logger.error("Unable to disconnect content repository " + repository + " for site '" + site + "'", e);
    }
  }

  /**
   * Service tracker for {@link Site} instances.
   */
  private static class SiteTracker extends ServiceTracker {

    /** The enclosing content repository factory */
    private ContentRepositoryService factory = null;

    /**
     * Creates a new site tracker which will call back to the
     * <code>ContentRepositoryFactory</code> that created it.
     * 
     * @param factory
     *          the content repository factory
     * @param context
     *          the bundle context
     */
    public SiteTracker(ContentRepositoryService factory, BundleContext context) {
      super(context, Site.class.getName(), null);
      this.factory = factory;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
      Site site = (Site) super.addingService(reference);
      factory.registerSite(site, reference.getBundle());
      return site;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
      factory.unregisterSite((Site) service);
      super.removedService(reference, service);
    }

  }

}
