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

package ch.entwine.weblounge.taglib.content;

import static ch.entwine.weblounge.common.content.SearchQuery.Quantifier.All;

import ch.entwine.weblounge.common.content.PageSearchResultItem;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ContentRepositoryUnavailableException;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;

/**
 * This tag is used to gather a list of pages satisfying certain criteria such
 * as the page type, search keywords etc.
 */
public class PageListTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = -1825541321489778143L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageListTag.class.getName());

  /** The list of keywords */
  private List<String> subjects = null;

  /** The number of page headers to return */
  private int count = 10;

  /** The page headers */
  private SearchResult pages = null;

  /** The iteration index */
  private int index = -1;

  /** The pagelet from the request */
  private Pagelet pagelet = null;

  /** The current page */
  private Page page = null;

  /** The current preview */
  private Composer preview = null;

  /** The current page's url */
  private WebUrl url = null;

  /** List of required headlines */
  private List<String> requiredPagelets = null;

  /**
   * Creates a new page header list tag.
   */
  public PageListTag() {
    requiredPagelets = new ArrayList<String>();
    subjects = new ArrayList<String>();
    reset();
  }

  /**
   * Returns the current page. This method serves as a way for embedded tags
   * like the {@link PagePreviewTag} to get to their data.
   * 
   * @return the page
   */
  public Page getPage() {
    return page;
  }

  /**
   * Returns the current preview. This method serves as a way for embedded tags
   * like the {@link PagePreviewTag} to get to their data.
   * 
   * @return the current page preview
   */
  public Composer getPagePreview() {
    return preview;
  }

  /**
   * Returns the current page's url. This method serves as a way for embedded
   * tags like the {@link PagePreviewTag} to get to their data.
   * 
   * @return the page's url
   */
  public WebUrl getPageUrl() {
    return url;
  }

  /**
   * Sets the number of page headers to load. If this attribute is omitted, then
   * all headers are returned.
   * 
   * @param count
   *          the number of page headers
   */
  public void setCount(String count) {
    if (count == null)
      throw new IllegalArgumentException("Count must be a positive integer");
    try {
      this.count = Integer.parseInt(count);
    } catch (NumberFormatException e) {
      this.count = Integer.MAX_VALUE;
    }
  }

  /**
   * Sets the list of page keywords to look up. The keywords must consist of a
   * list of strings, separated by either ",", ";" or " ".
   * 
   * @param value
   *          the keywords
   */
  public void setKeywords(String value) {
    if (value == null)
      throw new IllegalArgumentException("Keywords cannot be null");
    StringTokenizer tok = new StringTokenizer(value, ",;");
    while (tok.hasMoreTokens()) {
      subjects.add(tok.nextToken().trim());
    }
  }

  /**
   * Indicates the required headlines. The headline element types need to be
   * passed in as comma separated strings, e. g.
   * 
   * <pre>
   * text/title, repository/image
   * </pre>
   * 
   * @param value
   *          the headlines
   */
  public void setRequireheadlines(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",;");
    while (tok.hasMoreTokens()) {
      String headline = tok.nextToken().trim();
      String[] parts = headline.split("/");
      if (parts.length != 2)
        throw new IllegalArgumentException("Required headlines '" + value + "' are malformed. Required is 'module1/pagelet1, module2/pagelet2, ...");
      requiredPagelets.add(headline);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {

    // Don't do work if not needed (which is the case during precompilation)
    if (RequestUtils.isPrecompileRequest(request))
      return SKIP_BODY;

    // Make sure we start at the beginning
    index = 0;

    // Save the current pagelet so that we can restore it later
    pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);

    stashAttribute(PageListTagExtraInfo.PREVIEW_PAGE);
    stashAttribute(PageListTagExtraInfo.PREVIEW);


    try {
      return (loadNextPage()) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    } catch (ContentRepositoryUnavailableException e) {
      response.invalidate();
      return SKIP_BODY;
    } catch (ContentRepositoryException e) {
      throw new JspException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
   */
  @Override
  public int doAfterBody() throws JspException {
    index++;
    try {
      if (index < count && loadNextPage())
        return EVAL_BODY_AGAIN;
      else
        return SKIP_BODY;
    } catch (ContentRepositoryUnavailableException e) {
      response.invalidate();
      return SKIP_BODY;
    } catch (ContentRepositoryException e) {
      throw new JspException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    removeAndUnstashAttributes();
    request.setAttribute(WebloungeRequest.PAGELET, pagelet);
    return super.doEndTag();
  }

  /**
   * Loads the next page, puts it into the request and returns <code>true</code>
   * if a suitable page was found, false otherwise.
   * 
   * @return <code>true</code> if a suitable page was found
   * @throws ContentRepositoryException
   *           if loading the pages fails
   * @throws ContentRepositoryUnavailableException
   *           if the repository is offline
   */
  private boolean loadNextPage() throws ContentRepositoryException,
  ContentRepositoryUnavailableException {
    Site site = request.getSite();

    // Check if headers have already been loaded
    if (pages == null) {
      ContentRepository repository = site.getContentRepository();
      if (repository == null) {
        logger.debug("Unable to load content repository for site '{}'", site);
        throw new ContentRepositoryUnavailableException();
      }

      // Specify which pages to load
      SearchQuery query = new SearchQueryImpl(site);
      query.withVersion(Resource.LIVE);

      // Add the keywords (or)
      for (String subject : subjects) {
        query.withSubject(subject);
      }

      // Add the pagelets required on stage (and)
      if (requiredPagelets.size() > 0) {
        List<Pagelet> pagelets = new ArrayList<Pagelet>();
        for (String headline : requiredPagelets) {
          String[] parts = headline.split("/");
          if (parts.length > 1) {
            Pagelet pagelet = new PageletImpl(parts[0], parts[1]);
            pagelets.add(pagelet);
          }
        }
        query.withPagelets(All, pagelets.toArray(new Pagelet[pagelets.size()])).inStage();
      }

      // Order by date and limit the result set
      query.sortByPublishingDate(SearchQuery.Order.Descending);
      query.withLimit(count);

      // Finally Load the pages
      pages = repository.find(query);
    }

    boolean found = false;
    PageSearchResultItem item = null;
    Page page = null;
    WebUrl url = null;

    // Look for the next header
    while (!found && index < pages.getItems().length) {
      SearchResultItem candidateItem = pages.getItems()[index];
      if (!(candidateItem instanceof PageSearchResultItem)) {
        index++;
        continue;
      }
      item = (PageSearchResultItem) candidateItem;

      // Store the important properties
      url = item.getUrl();
      page = item.getPage();

      // TODO security check

      found = true;
    }

    // Set the headline in the request and add caching information
    if (found && page != null) {
      this.page = page;
      this.preview = new ComposerImpl("stage", page.getPreview());
      this.url = url;
      pageContext.setAttribute(PageListTagExtraInfo.PREVIEW_PAGE, page);
      pageContext.setAttribute(PageListTagExtraInfo.PREVIEW, preview);
      
      // Add cache tags
      response.addTag(CacheTag.Resource, page.getURI().getIdentifier());
      if (url != null)
        response.addTag(CacheTag.Url, url.getPath());
      
      // Adjust modification date
      response.setModificationDate(page.getLastModified());
    }

    return found;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    count = 10;
    index = 0;
    page = null;
    pagelet = null;
    pages = null;
    preview = null;
    requiredPagelets.clear();
    subjects.clear();
    url = null;
  }

}