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

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.UnknownLanguageException;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.Url;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A web url represents a url that is used to address locations within the web
 * application, such as HTML pages or module actions.
 */
public class WebUrlImpl extends UrlImpl implements WebUrl {

  /** Serial version uid */
  private static final long serialVersionUID = -5815146954734580746L;

  /** The logging facility */
  private static Logger log_ = LoggerFactory.getLogger(WebUrlImpl.class);

  /** Regular expression for /path/to/resource/work_de.html */
  private final static Pattern pathInspector = Pattern.compile("^(.*)/(work|index|live|[0-9]*)(_[a-zA-Z]+)?\\.([a-zA-Z0-9]+)$");

  /** Regular expression for /path/to/resource/work/de/html */
  private final static Pattern segmentInspector = Pattern.compile("^(.*?)(/work|index|live|[0-9]*)?(/[a-zA-Z][a-zA-Z]+)?(/[a-zA-Z0-9]+)?/$");

  /** The associated site */
  protected Site site = null;

  /** The url version */
  protected long version = -1;

  /** The language */
  protected Language language = null;

  /** The link */
  private transient String link_ = null;

  /** The url flavor */
  protected String flavor = null;

  /**
   * Constructor for a url with the given path, a version of <code>LIVE</code>,
   * a language matching the site default language and an <code>HTML</code>
   * flavor, unless version, flavor and language are encoded in the url using
   * either of these two schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   */
  public WebUrlImpl(Site site, String path) {
    super('/');
    this.site = site;
    this.path = analyzePath(path, '/');
  }

  /**
   * Constructor for a url with the given path, a version of <code>LIVE</code>
   * and an <code>HTML</code> flavor, unless version, flavor or language are
   * encoded in the url using either of these two schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * 
   * @param site
   *          the associated site
   * @param url
   *          the url
   */
  public WebUrlImpl(Site site, Url url) {
    this(site, url.getPath());
  }

  /**
   * Constructor for a url with the given path added to <code>url</code>, a
   * version of <code>LIVE</code> and an <code>HTML</code> flavor, unless
   * version and/or flavor are encoded in the url using either of these two
   * schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * 
   * @param site
   *          the associated site
   * @param url
   *          the url
   * @param path
   *          the path to append
   */
  public WebUrlImpl(Site site, Url url, String path) {
    this(site, concat(url.getPath(), path, '/'));
  }

  /**
   * Constructor for a url with the given path and version and an
   * <code>HTML</code> flavor, unless the flavor is encoded in the url using
   * either of these two schemes:
   * <ul>
   * <li>
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * </li>
   * <li><code>path/to/resource/version/language/flavor</code></li>
   * </ul>
   * <p>
   * Note that even if the version is encoded in the url path, the one passed as
   * the argument to this constructor will be used.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @param version
   *          the url version
   */
  public WebUrlImpl(Site site, String path, long version) {
    this(site, path);
    this.version = version;
  }

  /**
   * Constructor for a url with the given path, version and flavor.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the url path
   * @param version
   *          the required version
   * @param flavor
   *          the url flavor
   */
  public WebUrlImpl(Site site, String path, long version, String flavor) {
    super(path, '/');
    this.site = site;
    this.version = version;
    this.flavor = flavor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink()
   */
  public String getLink() {
    if (link_ == null) {
      try {
        link_ = URLEncoder.encode(getLink(-1, null, null), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        log_.error("Unexpected error while urlencoding link {}", link_, e);
      }
      link_ = link_.replaceAll("%2F", "/");
    }
    return link_;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(long)
   */
  public String getLink(long version) {
    return getLink(version, null, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(ch.o2it.weblounge.common.language.Language)
   */
  public String getLink(Language language) {
    return getLink(-1, language, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(java.lang.String)
   */
  public String getLink(String flavor) {
    return getLink(-1, null, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLink(long,
   *      ch.o2it.weblounge.common.language.Language, java.lang.String)
   */
  public String getLink(long version, Language language, String flavor) {
    StringBuffer selector = new StringBuffer();
    boolean hasVersion = false;
    if (version >= 0 || this.version > 0 || language != null || this.language != null || flavor != null || this.flavor != null) {
      if (version < 0)
        version = this.version;
      if (version == Page.LIVE)
        selector.append("index");
      else if (version == Page.WORK) {
        selector.append("work");
      } else if (version >= 0) {
        selector.append(Long.toString(version));
      } else {
        selector.append("index");
      }
      hasVersion = true;
    }

    // Language
    if (language != null)
      selector.append("_").append(language.getIdentifier());
    else if (this.language != null) {
      selector.append("_").append(this.language.getIdentifier());
    }

    // Flavor
    if (flavor != null)
      selector.append(".").append(flavor.toLowerCase());
    else if (this.flavor != null) {
      selector.append(".").append(this.flavor.toLowerCase());
    } else if (hasVersion) {
      selector.append(".").append(RequestFlavor.HTML.toExtension());
    }

    return UrlSupport.concat(path, selector.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getFlavor()
   */
  public String getFlavor() {
    return flavor;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getVersion()
   */
  public long getVersion() {
    return version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.url.WebUrl#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * Returns the hash code for this url. The method includes the super
   * implementation and adds sensitivity for the site and the url extension.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return super.hashCode() | site.hashCode() >> 16;
  }

  /**
   * Returns true if the given object is a url itself and describes the same url
   * than this object, including the associated site and possible url
   * extensions.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object object) {
    if (object instanceof WebUrl) {
      WebUrl url = (WebUrl) object;
      return (super.equals(object) && version == url.getVersion() && (language == null && url.getLanguage() == null || (language != null && url.getLanguage() != null && language.equals(url.getLanguage()))) && (flavor == null && url.getFlavor() == null || (flavor != null && url.getFlavor() != null && flavor.equals(url.getFlavor()))) && site.equals(url.getSite()));
    } else if (object instanceof Url) {
      return super.equals(object);
    }
    return false;
  }

  /**
   * Strips version and flavor from this url. Version and flavor can either be
   * encoded as
   * <code>path/to/resource/&lt;version&gt;_&lt;language&gt;.&lt;flavor&gt;</code>
   * or as <code>path/to/resource/version/language/flavor</code>.
   * 
   * @param path
   *          the full path
   * @param separator
   *          path separator character
   * @return the directory path
   */
  protected String analyzePath(String path, char separator) {
    path = trim(path);
    Matcher pathMatcher = pathInspector.matcher(path);
    if (pathMatcher.matches()) {

      // Version
      String v = pathMatcher.group(2);
      if ("index".equals(v) || "live".equals(v)) {
        this.version = Page.LIVE;
      } else if ("work".equals(v)) {
        this.version = Page.WORK;
      } else if (v != null && !"".equals(v)) {
        try {
          this.version = Long.parseLong(v);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unable to extract version from url " + path);
        }
      }

      // Language
      String l = pathMatcher.group(3);
      if (l != null && !"".equals(l)) {
        l = l.substring(1);
        Language language = site.getLanguage(l);
        if (language == null) {
          throw new UnknownLanguageException(l);
        }
        this.language = language;
      }

      // Flavor
      String f = pathMatcher.group(4);
      if (f != null && !"".equals(f))
        this.flavor = f.toLowerCase();
      return trim(pathMatcher.group(1));
    }

    // Try the segmented approach for /path/to/resource/work/de/html
    Matcher segmentMatcher = segmentInspector.matcher(path);
    if (segmentMatcher.matches()) {
      int group = segmentMatcher.groupCount();
      while (segmentMatcher.group(group) == null || "".equals(segmentMatcher.group(group)))
        group--;

      if (group < 2)
        return trim(segmentMatcher.group(1));

      // Test group for flavor
      String f = segmentMatcher.group(group);
      if (f == null || "".equals(f)) {
        group--;
      } else {
        if (f.startsWith("/"))
          f = f.substring(1);
        try {
          this.flavor = RequestFlavor.parseString(f).toExtension();
          group--;
        } catch (IllegalArgumentException e) {
          log_.debug("Found non-standard flavor {}", f);
        }
      }

      // Done?
      if (group < 1)
        return trim(segmentMatcher.group(1));

      // Test group for language
      String l = segmentMatcher.group(group);
      if (l == null || "".equals(l)) {
        group--;
      } else {
        if (l.startsWith("/"))
          l = l.substring(1);
        Language language = site.getLanguage(l);
        if (language != null) {
          this.language = language;
          group--;
        } else {
          try {
            language = LanguageSupport.getLanguage(l);
            if (language != null) {
              this.language = site.getDefaultLanguage();
              group--;
            }
          } catch (UnknownLanguageException e) {
            // Nothing to do, definitely not a language identifier
          }
        }
      }

      // Done?
      if (group < 1)
        return trim(segmentMatcher.group(1));

      // Test group for version
      String v = segmentMatcher.group(group);
      if (v == null || "".equals(v))
        return trim(segmentMatcher.group(1));

      if (v.startsWith("/"))
        v = v.substring(1);
      if ("index".equals(v) || "live".equals(v)) {
        this.version = Page.LIVE;
        group--;
      } else if ("work".equals(v)) {
        this.version = Page.WORK;
        group--;
      } else {
        try {
          this.version = Long.parseLong(v);
          group--;
        } catch (NumberFormatException e) {
          // Nothing to do, definitely not a version identifier
        }
      }

      StringBuffer rest = new StringBuffer();
      int i = 1;
      while (i <= group) {
        if (segmentMatcher.group(i) != null)
          rest.append(segmentMatcher.group(i));
        i++;
      }
      return trim(rest.toString());
    }
    return trim(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.url.UrlImpl#toString()
   */
  @Override
  public String toString() {
    return getLink();
  }

}