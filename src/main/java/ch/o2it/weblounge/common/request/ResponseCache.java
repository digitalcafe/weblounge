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

package ch.o2it.weblounge.common.request;

import ch.o2it.weblounge.common.site.Site;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ResponseCache</code> is used to cache responses generated using the
 * <code>WebloungeResponse</code> object. It's implementation makes sure that
 * the response that is handed over in the first place is tweaked (wrapped) in a
 * way that the output that is written to it is streamed to the cache as well as
 * to the client.
 * <p>
 * The structure of the response cache is hierarchical in that there is a
 * general response, consisting of stacked response parts. This supports content
 * structures that have different lifetimes, e. g. a page might become invalid
 * but some pieces on it live much longer. When a client requests that
 * invalidated page, the output renderer needs to produce the enclosing content
 * (template) from scratch but could choose to reuse the cached pieces.
 * <p>
 * The two methods
 * {@link #startResponse(CacheTag[], WebloungeRequest, WebloungeResponse, long, long)}
 * and {@link #startResponsePart(CacheTag[], HttpServletResponse, long, long)}
 * are used to create an entry in the cache. They will return <code>null</code>
 * if the entry is already present, meaning that the cached version will be sent
 * to the client. If a <code>CacheHandle</code> is returned, then a new cache
 * entry, identified by the handle, was created and the output written to the
 * response will be sent to both the cache and the client.
 * <p>
 * Note that in order to take advantage of the cache, it is crucial that you tag
 * your responses and their parts in a useful manner.
 */
public interface ResponseCache {

  /**
   * Wraps the given response into a cacheable http servlet response.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return the wrapped response
   */
  HttpServletResponse createCacheableResponse(HttpServletRequest request,
      HttpServletResponse response);

  /**
   * Starts a cacheable response. By calling this method, a new response wrapper
   * is generated which will write the response output to the cache as well as
   * to the client.
   * <p>
   * If the method returns <code>null</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * Otherwise the data was not found but will be put into the cache when
   * {@link #endResponse(WebloungeResponse)} is called.
   * 
   * @param uniqueTags
   *          the tags identifying this response
   * @param request
   *          the request
   * @param response
   *          the response
   * @param validTime
   *          the valid time in milliseconds
   * @param recheckTime
   *          the recheck time in milliseconds
   * @return <code>null</code> if the content was found in the cache
   */
  CacheHandle startResponse(CacheTag[] uniqueTags, WebloungeRequest request,
      WebloungeResponse response, long validTime, long recheckTime);

  /**
   * Starts a cacheable response. By calling this method, a new response wrapper
   * is generated which will write the response output to the cache as well as
   * to the client.
   * <p>
   * If the method returns <code>null</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * Otherwise, the data was not found but will be put into the cache when
   * {@link #endResponse(WebloungeResponse)} is called.
   * 
   * @param request
   *          the request
   * @param response
   *          the response
   * @return <code>null</code> if the response was found in the cache
   */
  CacheHandle startResponse(CacheHandle handle, WebloungeRequest request,
      WebloungeResponse response);

  /**
   * Tell the cache service that writing the response to the client is now
   * finished and that the cache buffer containing the response may be written
   * to the cache.
   * 
   * @param response
   *          the servlet response
   */
  boolean endResponse(WebloungeResponse response);

  /**
   * Tell the cache service that writing the response to the client failed for
   * some reason. In this case, the cache will discard the cached data for this
   * response and directly write the data out to the client.
   * <p>
   * Once this method has been called, all subsequent calls to
   * <code>endResponse</code>, <code>startResponsePart</code> and
   * <code>endtResponsePart</code> will have no effect.
   * 
   * @param response
   *          the servlet response
   */
  void invalidate(WebloungeResponse response);

  /**
   * Starts caching a sub portion of the current response, identified by a set
   * of cache tags.
   * <p>
   * Dividing the cached response into parts has the advantage, that, if for
   * example on part of a page becomes invalid, the other parts remain in the
   * cache and only the invalidated part and the page in whole have to be
   * rebuilt.
   * <p>
   * If the method returns <code>null</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * Otherwise the data was not found but will be put into the cache.
   * 
   * @param uniqueTags
   *          the tag set identifying the response part
   * @param request
   *          the servlet requests
   * @param response
   *          the servlet response
   * @param validTime
   *          the valid time in milliseconds
   * @param recheckTime
   *          the recheck time in milliseconds
   * @return the <code>CacheHandle</code> of the response part or
   *         <code>null</code> if the response part was found in the cache
   */
  CacheHandle startResponsePart(CacheTag[] uniqueTags,
      HttpServletRequest request, HttpServletResponse response, long validTime,
      long recheckTime);

  /**
   * Starts caching a sub portion of the current response, identified by
   * <code>handle</code>. Dividing the cached response into parts has the
   * advantage, that, if for example on part of a page becomes invalid, the
   * other parts remain in the cache and only the invalidated part and the page
   * in whole have to be rebuilt.
   * <p>
   * If the method returns <code>true</code>, then the response part was found
   * in the cache and has been directly written to the response from the cache.
   * If it returns <code>false</code>, the data was not found but will be put
   * into the cache.
   * 
   * @param handle
   *          the response part identifier
   * @param response
   *          the servlet response
   * @return boolean <code>true</code> if the response part was found in the
   *         cache
   */
  boolean startResponsePart(CacheHandle handle, HttpServletResponse response);

  /**
   * Tells the cache that the data identified by <code>handle</code> is complete
   * and may be written to the cache.
   * 
   * @param handle
   *          the response part identifier. <br>
   *          NOTE: This MUST be the same instance that was used to start the
   *          corresponding response part!
   * @param response
   *          the servlet response
   */
  void endResponsePart(CacheHandle handle, HttpServletResponse response);

  /**
   * Tells the cache of the given site to throw away the data identified by the
   * given set of tags.
   * @param tags
   *          the set of tags
   * @param site
   *          the site
   * 
   * @return the handles of the removed elements
   */
  void invalidate(CacheTag[] tags, Site site);

  /**
   * Tells the cache of the given site to throw away the data identified by
   * <code>handle</code>.
   * @param handle
   *          the cache data identifier
   * @param site
   *          the site
   * 
   * @return the handle or <code>null</code>
   */
  void invalidate(CacheHandle handle, Site site);

  /**
   * Asks the cache to load those elements into memory that match at least the
   * given set of tags.
   * 
   * @param site
   *          the site
   * @param tags
   *          the tags to match
   */
  void preload(Site site, CacheTag[] tags);

}