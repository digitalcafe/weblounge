/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.request;

import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * DispatcherRequest
 */
public class SiteRequestWrapper extends HttpServletRequestWrapper implements WebloungeRequest {

  /** the name of the request uri include attribute */
  public static final String REQUEST_URI = "javax.servlet.include.request_uri";

  /** the name of the context path include attribute */
  public static final String CONTEXT_PATH = "javax.servlet.include.context_path";

  /** the name of the servlet path include attribute */
  public static final String SERVLET_PATH = "javax.servlet.include.servlet_path";

  /** the name of the path info include attribute */
  public static final String PATH_INFO = "javax.servlet.include.path_info";

  /** the name of the query string include attribute */
  public static final String QUERY_STRING = "javax.servlet.include.query_string";

  /** the attributes used for including requests */
  private static Set<String> includeAttrs = new HashSet<String>();

  /** initialize the set of include attributes */
  static {
    includeAttrs.add(REQUEST_URI);
    includeAttrs.add(CONTEXT_PATH);
    includeAttrs.add(SERVLET_PATH);
    includeAttrs.add(PATH_INFO);
    includeAttrs.add(QUERY_STRING);
  }

  /** the attributes of this request */
  private Map<String, Object> attrs;

  /** indicates an include request */
  private boolean include = false;

  /** the request uri */
  private String requestURI = null;

  /** the context path */
  private String contextPath = null;

  /** the servlet path */
  private String servletPath = null;

  /** the path info */
  private String pathInfo = null;

  /** the query string */
  private String queryString = null;

  /** The site */
  private final Site site;

  /** The url */
  private final WebUrl url;

  /** The request environment */
  private final Environment environment;

  /**
   * Creates a new <code>DispatcherRequest</code>.
   * 
   * @param request
   *          the original request
   * @param url
   *          the new target url
   * @param include
   *          whether to include the path elements of the original request in
   *          the attributes of the new request
   */
  public SiteRequestWrapper(WebloungeRequest request, String url,
      boolean include) {
    super(request);

    this.site = request.getSite();
    this.include = include;
    this.environment = request.getEnvironment();

    int index = url.indexOf('?');
    if (index > -1) {
      url = url.substring(0, index);
    }

    // Set the context path, servlet path and request uri
    String contextPath = request.getContextPath();
    String servletPath = "";
    String pathInfo = url.substring(contextPath.length() + servletPath.length());
    String requestURI = pathInfo;

    // Adjust the url
    this.url = new WebUrlImpl((WebUrlImpl) request.getUrl(), url);

    if (include) {
      attrs = new HashMap<String, Object>(5);
      attrs.put(REQUEST_URI, requestURI);
      attrs.put(CONTEXT_PATH, contextPath);
      attrs.put(SERVLET_PATH, servletPath);
      attrs.put(QUERY_STRING, request.getQueryString());
    } else {
      this.contextPath = contextPath;
      this.servletPath = servletPath;
      this.pathInfo = pathInfo;
      this.requestURI = requestURI;
      this.queryString = request.getQueryString();
      this.attrs = new HashMap<String, Object>(0);
    }
  }

  /**
   * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) {
    if (includeAttrs.contains(name))
      return attrs.get(name);
    return super.getAttribute(name);
  }

  /**
   * @see javax.servlet.ServletRequest#getAttributeNames()
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Enumeration getAttributeNames() {
    final Set s = new HashSet();
    for (Enumeration e = getRequest().getAttributeNames(); e.hasMoreElements();)
      s.add(e.nextElement());
    s.addAll(attrs.keySet());
    return Collections.enumeration(s);
  }

  /**
   * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
   *      java.lang.Object)
   */
  public void setAttribute(String name, Object o) {
    if (includeAttrs.contains(name))
      attrs.put(name, o);
    else
      ((WebloungeRequest) getRequest()).setAttribute(name, o);
  }

  /**
   * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
   */
  public void removeAttribute(String name) {
    if (includeAttrs.contains(name))
      attrs.remove(name);
    else
      ((WebloungeRequest) getRequest()).removeAttribute(name);
  }

  /**
   * @see javax.servlet.http.HttpServletRequest#getContextPath()
   */
  public String getContextPath() {
    if (include)
      return ((WebloungeRequest) getRequest()).getContextPath();
    return contextPath;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getEnvironment()
   */
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * @see javax.servlet.http.HttpServletRequest#getPathInfo()
   */
  public String getPathInfo() {
    if (include)
      return ((WebloungeRequest) getRequest()).getPathInfo();
    return pathInfo;
  }

  /**
   * @see javax.servlet.http.HttpServletRequest#getQueryString()
   */
  public String getQueryString() {
    if (include)
      return ((WebloungeRequest) getRequest()).getQueryString();
    return queryString;
  }

  /**
   * @see javax.servlet.http.HttpServletRequest#getRequestURI()
   */
  public String getRequestURI() {
    if (include)
      return ((WebloungeRequest) getRequest()).getRequestURI();
    return requestURI;
  }

  /**
   * @see javax.servlet.http.HttpServletRequest#getServletPath()
   */
  public String getServletPath() {
    if (include)
      return ((WebloungeRequest) getRequest()).getServletPath();
    return servletPath;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getFlavor()
   */
  public RequestFlavor getFlavor() {
    return ((WebloungeRequest) getRequest()).getFlavor();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getLanguage()
   */
  public Language getLanguage() {
    return ((WebloungeRequest) getRequest()).getLanguage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getSessionLanguage()
   */
  public Language getSessionLanguage() {
    return ((WebloungeRequest) getRequest()).getSessionLanguage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getRequestedUrl()
   */
  public WebUrl getRequestedUrl() {
    ServletRequest request = getRequest();
    while (request instanceof HttpServletRequestWrapper && !(request instanceof WebloungeRequestImpl)) {
      request = ((HttpServletRequestWrapper) request).getRequest();
    }
    return ((WebloungeRequest) request).getUrl();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getUrl()
   */
  public WebUrl getUrl() {
    return url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getUser()
   */
  public User getUser() {
    return ((WebloungeRequest) getRequest()).getUser();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.request.WebloungeRequest#getVersion()
   */
  public long getVersion() {
    return ((WebloungeRequest) getRequest()).getVersion();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getRequestURI();
  }

}
