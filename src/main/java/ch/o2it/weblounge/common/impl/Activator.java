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

package ch.o2it.weblounge.common.impl;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator for the commons bundle, which will set up common services
 * like a Quartz scheduler.
 */
public class Activator {

  /** Logger */
  private static final Logger log_ = LoggerFactory.getLogger(Activator.class);
  
  /** The weblounge scheduler */
  private Scheduler scheduler = null;

  /**
   * Callback from the OSGi environment to activate the bundle.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();
    log_.info("Starting common weblounge services", this);

    // Start and register the quartz scheduler
    try {
      log_.info("Starting cron scheduler");
      StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
      scheduler = schedulerFactory.getScheduler();
      bundleContext.registerService(Scheduler.class.getName(), scheduler, null);
    } catch (SchedulerException e) {
      log_.error("Error starting cron scheduler: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Callback from the OSGi environment to deactivate the site.
   * 
   * @param context
   *          the component context
   */
  public void deactivate(ComponentContext context) throws Exception {
    log_.info("Stopping common weblounge services", this);
    scheduler.shutdown();
    log_.info("Cron daemon stopped");
  }
  
}
