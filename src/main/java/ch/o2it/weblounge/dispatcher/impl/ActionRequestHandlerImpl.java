/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl;

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.request.Http11Constants;
import ch.o2it.weblounge.common.impl.request.Http11Utils;
import ch.o2it.weblounge.common.impl.site.ActionPool;
import ch.o2it.weblounge.common.impl.url.UrlMatcherImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.PageTemplate;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.UrlMatcher;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.dispatcher.ActionRequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

/**
 * This handler can be used to register {@link Action}s. If a request matches
 * the url space of registered action, it will handle the request by forwarding
 * it to the action.
 */
public final class ActionRequestHandlerImpl implements ActionRequestHandler {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ActionRequestHandlerImpl.class);

  /** The registered actions */
  private Map<UrlMatcher, ActionPool> actions = null;

  /** Known urls */
  private Map<WebUrl, ActionPool> urlCache = null;

  /**
   * Creates a new action request handler.
   */
  public ActionRequestHandlerImpl() {
    actions = new HashMap<UrlMatcher, ActionPool>();
    urlCache = new HashMap<WebUrl, ActionPool>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.ActionRequestHandler#register(ch.o2it.weblounge.common.site.Action)
   */
  public void register(Action action) {
    if (action == null)
      throw new IllegalArgumentException("Action configuration cannot be null");

    // Create a url matcher
    UrlMatcher matcher = new UrlMatcherImpl(action);

    // Register the action
    synchronized (actions) {
      ActionPool pool = new ActionPool(action);
      actions.put(matcher, pool);
    }
    log_.debug("Action handler '{}' registered for {}", action, matcher);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.ActionRequestHandler#unregister(ch.o2it.weblounge.common.site.Action)
   */
  public boolean unregister(Action action) {
    UrlMatcher matcher = new UrlMatcherImpl(action);

    // Find the actions and the namespaces that it was registered under
    synchronized (actions) {
      if (actions.remove(matcher) != null)
        log_.info("Unregistering action '{}' from {}", action, matcher);
      else {
        log_.warn("Tried to unregister unknown action '{}'", action);
        return false;
      }
    }

    // Remove entries from the url cache
    synchronized (urlCache) {
      Iterator<WebUrl> cacheIterator = urlCache.keySet().iterator();
      while (cacheIterator.hasNext()) {
        WebUrl url = cacheIterator.next();
        if (matcher.matches(url)) {
          log_.debug("Removing {} from action url cache", url);
          cacheIterator.remove();
        }
      }
    }

    log_.debug("Action handler '{}' unregistered", action);
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.dispatcher.RequestHandler#service(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {
    WebUrl url = request.getUrl();
    RequestFlavor contentFlavor = request.getFlavor();
    Mode processingMode = Mode.Default;

    // Try to get hold of an action handler
    Action action = null;
    try {
      action = getActionForUrl(url);
      if (action == null) {
        log_.debug("No action found to handle {}", url);
        return false;
      }
    } catch (Exception e) {
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException e2) { /* never mind */ }
      return true;
    }

    // Check the request method. We won't handle just everything
    String requestMethod = request.getMethod();
    if (!Http11Utils.checkDefaultMethods(requestMethod, response)) {
      log_.debug("Actions are not supposed to handle {} requests", requestMethod);
      try {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      } catch (IOException e) { /* never mind */
      }
      return true;
    }

    // Is the requested content flavor supported?
    if (!action.supportsFlavor(contentFlavor)) {
      log_.warn("Content flavor {} is not supported by action {}", contentFlavor, action);
      try {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      } catch (IOException e) { /* never mind */
      }
      return true;
    }

    // Check if the page is already part of the cache. If so, our task is
    // already done!
    if (request.getVersion() == Page.LIVE) {
      long validTime = Renderer.DEFAULT_VALID_TIME;
      long recheckTime = Renderer.DEFAULT_RECHECK_TIME;

      // Create the set of tags that identify the request output
      CacheTagSet cacheTags = createCacheTags(request, action);

      // Check if the page is already part of the cache
      if (response.startResponse(cacheTags, validTime, recheckTime)) {
        log_.debug("Page handler answered request for {} from cache", request.getUrl());
        return true;
      }

      processingMode = Mode.Cached;
    } else if (Http11Constants.METHOD_HEAD.equals(request.getMethod())) {
      // handle HEAD requests
      Http11Utils.startHeadResponse(response);
      processingMode = Mode.Head;
    } else if (request.getVersion() == Page.WORK) {
      response.setHeader("Expires", "0");
      response.setHeader("Last-Modified", WebloungeDateFormat.formatStatic(new Date()));
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
      response.setHeader("Pragma", "no-cache");
    }

    log_.debug("Action handler {} will handle {}", action, url);

    // Call the service method depending on the flavor
    switch (contentFlavor) {
      case HTML:
        serveHTML(action, request, response);
        break;
      case XML:
        serveXML(action, request, response);
        break;
      case JSON:
        serveJSON(action, request, response);
        break;
    }

    // Finish cache handling
    switch (processingMode) {
      case Cached:
        response.endResponsePart();
        break;
      case Head:
        Http11Utils.endHeadResponse(response);
        break;
      default:
        break;
    }

    // TODO: Return action to pool
    action.passivate();

    return true;
  }

  /**
   * This method has the action serve the <code>HTML</code> flavor.
   * 
   * @param action
   *          the action
   * @param request
   *          the http request
   * @param response
   *          the http response
   */
  private void serveHTML(Action action, WebloungeRequest request,
      WebloungeResponse response) {
    
    WebUrl url = request.getUrl();

    // Load the target page used to render the action
    Page page = null;
    try {
      page = getTargetPage(action, request);
      // TODO: Check access rights with action handler configuration
      // TODO: If there is no page, create a virtual one with either the
      // default template or the template specified in the configuration
      // or the request
      if (page == null) {
        log_.error("No page available to serve action {}", action);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
    } catch (IOException e) {
      log_.error("Error loading target page for action {} at {}", action, url);
      return;
    }

    // Get hold of the page template
    PageTemplate template = null;
    try {
      template = getPageTemplate(page, request);
    } catch (IllegalStateException e) {
      log_.warn(e.getMessage());
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException e1) { /* never mind */
      }
    }

    // Finally, let's get some work done!
    try {
      log_.debug("Action handler {} will handle {}", action, url);
      request.setAttribute(WebloungeRequest.REQUEST_ACTION, action);
      request.setAttribute(WebloungeRequest.REQUEST_PAGE, page);
      action.setTemplate(template);
      action.setPage(page);
      action.configure(request, response, RequestFlavor.HTML);
      response.setHeader("Content-Type", "text/html; charset=utf-8");
      if (action.startResponse(request, response) == Action.EVAL_REQUEST) {
        PageRequestHandlerImpl.getInstance().service(request, response);
      }
    } catch (Throwable e) {
      log_.error("Error processing action '{}' for {}", action, request.getUrl());
      log_.error(e.getMessage(), e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException e1) { /* never mind */
      }
    } finally {
      request.removeAttribute(WebloungeRequest.REQUEST_ACTION);
      request.removeAttribute(WebloungeRequest.REQUEST_PAGE);
    }
  }

  /**
   * This method has the action serve the <code>XML</code> flavor.
   * 
   * @param action
   *          the action
   * @param request
   *          the http request
   * @param response
   *          the http response
   */
  private void serveXML(Action action, WebloungeRequest request,
      WebloungeResponse response) {
    try {
      action.configure(request, response, RequestFlavor.XML);
      response.setHeader("Content-Type", "text/xml; charset=utf-8");
      action.startXMLResponse(request, response);
    } catch (Throwable e) {
      log_.error("Error processing action '{}' for {}", action, request.getUrl());
      log_.error(e.getMessage(), e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException e1) { /* never mind */
      }
    }
  }

  /**
   * This method has the action serve the <code>XML</code> flavor.
   * 
   * @param action
   *          the action
   * @param request
   *          the http request
   * @param response
   *          the http response
   */
  private void serveJSON(Action action, WebloungeRequest request,
      WebloungeResponse response) {
    try {
      action.configure(request, response, RequestFlavor.JSON);
      response.setHeader("Content-Type", "text/json; charset=utf-8");
      action.startJSONResponse(request, response);
    } catch (Throwable e) {
      log_.error("Error processing action '{}' for {}", action, request.getUrl());
      log_.error(e.getMessage(), e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException e1) { /* never mind */
      }
    }
  }

  /**
   * Returns the primary set of cache tags for the given request and action.
   * 
   * @param request
   *          the request
   * @param action
   *          the action
   * @return the cache tags
   */
  protected CacheTagSet createCacheTags(WebloungeRequest request, Action action) {
    CacheTagSet cacheTags = new CacheTagSet();
    cacheTags.add(CacheTag.Url, request.getUrl().getPath());
    cacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
    cacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
    cacheTags.add(CacheTag.User, request.getUser().getLogin());
    cacheTags.add(CacheTag.Site, request.getSite().getIdentifier());
    cacheTags.add(CacheTag.Module, action.getModule().getIdentifier());
    cacheTags.add(CacheTag.Action, action.getIdentifier());
    Enumeration<?> pe = request.getParameterNames();
    int parameterCount = 0;
    while (pe.hasMoreElements()) {
      parameterCount++;
      String key = pe.nextElement().toString();
      String[] values = request.getParameterValues(key);
      for (String value : values) {
        cacheTags.add(key, value);
      }
    }
    cacheTags.add(CacheTag.Parameters, Integer.toString(parameterCount));
    return cacheTags;
  }

  /**
   * Returns the template that will be used to handle this request. If the
   * template cannot be found or used for some reason, an
   * {@link IllegalStateException} is thrown.
   * 
   * @param page
   *          the page
   * @param request
   *          the request
   * @return the template
   * @throws IllegalStateException
   *           if the template cannot be found
   */
  protected PageTemplate getPageTemplate(Page page, WebloungeRequest request)
      throws IllegalStateException {
    Site site = request.getSite();
    String templateId = (String) request.getAttribute(WebloungeRequest.REQUEST_TEMPLATE);
    PageTemplate template = null;
    if (templateId != null) {
      template = site.getTemplate(templateId);
      if (template == null) {
        throw new IllegalStateException("Page template " + templateId + " specified by request was not found");
      }
    } else {
      template = site.getTemplate(page.getTemplate());
      if (template == null) {
        throw new IllegalStateException("Page template " + templateId + " specified by page " + page + " was not found");
      }
    }
    return template;
  }

  /**
   * Returns the action handler that is registered to serve the given url or
   * <code>null</code> if no such handler exists.
   * 
   * @param url
   *          the url
   * @return the handler
   */
  protected Action getActionForUrl(WebUrl url) throws Exception {
    ActionPool actionPool = urlCache.get(url);

    // Nothing is in the cache, let's see if this is simply the first time
    // that this action is being called
    if (actionPool == null) {
      for (Entry<UrlMatcher, ActionPool> entry : actions.entrySet()) {
        if (entry.getKey().matches(url)) {
          actionPool = entry.getValue();
          break;
        }
      }

      // Still nothing?
      if (actionPool == null) {
        log_.debug("No action registered to handle {}", url);
        return null;
      }

      // Register the url for future reference
      urlCache.put(url, actionPool);
    }

    // Get an action worker and return it
    // TODO: Instantiate the action

    return (Action) actionPool.borrowObject();
  }

  /**
   * Tries to determine the target page for the action result. The
   * <code>target-url</code> request parameter will be considered as well as the
   * action configuration. In any case, the site's homepage will be the
   * fallback.
   * <p>
   * Should a target page be configured either through the request or through
   * action configuration, and should that url not be present, this method will
   * return <code>null</code>.
   * 
   * @param action
   *          the action
   * @param request
   *          the weblounge request
   * @return the target page
   * @throws IOException
   *           if the target page cannot be loaded
   */
  protected Page getTargetPage(Action action, WebloungeRequest request)
      throws IOException {

    PageURI target = null;
    Page page = null;
    Site site = request.getSite();
    boolean targetForced = false;

    // Check if a target-page parameter was passed
    if (request.getParameter(Action.TARGET) != null) {
      String targetUrl = request.getParameter(Action.TARGET);
      targetForced = true;
      try {
        String decocedTargetUrl = null;
        String encoding = request.getCharacterEncoding();
        if (encoding == null)
          encoding = "utf-8";
        decocedTargetUrl = URLDecoder.decode(targetUrl, encoding);
        target = new PageURIImpl(site, decocedTargetUrl);
      } catch (UnsupportedEncodingException e) {
        log_.warn("Error while decoding target url {}: {}", targetUrl, e.getMessage());
        target = new PageURIImpl(site, "/");
      }
    }

    // Check the action configuration
    else if (action.getPageURI() != null) {
      target = action.getPageURI();
      targetForced = true;
    }

    // Nothing found, let's choose the site's homepage
    else {
      target = new PageURIImpl(site, "/");
    }

    // We are about to render the action output in the composers of the target
    // page. This is why we have to make sure that this target page exists,
    // otherwise the user will get a 404.
    page = site.getPage(target);
    if (page == null) {
      if (targetForced) {
        log_.warn("Output of action '{}' is configured to render on non existing page {}", action, target);
        return null;
      }

      // Fall back to site homepage
      target = new PageURIImpl(site, "/");
      page = site.getPage(target);
      if (page == null) {
        log_.warn("Site {} has no homepage as fallback to render actions", site);
        return null;
      }
    }

    return page;
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getIdentifier()
   */
  public String getIdentifier() {
    return "actionhandler";
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "action request handler";
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getIdentifier().hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

}