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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Permission;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse page data.
 */
public abstract class AbstractResourceReaderImpl<S extends ResourceContent, T extends Resource<S>> extends WebloungeContentReader implements ResourceReader<S, T> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractResourceReaderImpl.class);

  /** Parser factory */
  private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  protected WeakReference<SAXParser> parserRef = null;

  /** The resource object */
  protected T resource = null;

  /** The root tag */
  protected String rootTag = null;

  /** Flag to indicate whether the page header should be read */
  protected boolean readHeader = true;

  /** Flag to indicate whether the page body should be read */
  protected boolean readBody = true;

  protected enum ParserContext {
    Document, Resource, Head, Body, Content
  };

  /** The parser context */
  protected ParserContext parserContext = ParserContext.Document;

  /**
   * Creates a new resource reader reader that will parse the XML data and store
   * it in the <code>Resource</code> object that is returned by the
   * {@link #read} method.
   * 
   * @param rootTag
   *          name of the root tag
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public AbstractResourceReaderImpl(String rootTag)
      throws ParserConfigurationException, SAXException {
    if (rootTag == null)
      throw new IllegalArgumentException("Root tag name must not be null");
    this.rootTag = rootTag.toLowerCase();
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  /**
   * This method is called when a <code>Page</code> object is instantiated.
   * 
   * @param is
   *          the xml input stream
   * @param uri
   *          the page uri
   * 
   * @throws IOException
   *           if reading the input stream fails
   */
  public T read(InputStream is, Site site) throws SAXException, IOException,
  ParserConfigurationException {
    reset();
    resource = createResource(site);
    readHeader = true;
    readBody = true;
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return resource;
  }

  /**
   * Creates an empty instance of the resource to read.
   * 
   * @param site
   *          the site
   * @return the empty resource
   */
  protected abstract T createResource(Site site);

  /**
   * This method is called to read the head section of a resource.
   * 
   * @param is
   *          the xml input stream
   * @param site
   *          the site
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public T readHeader(InputStream is, Site site) throws SAXException,
  IOException, ParserConfigurationException {
    if (resource == null) {
      resource = createResource(site);
    }
    readHeader = true;
    readBody = false;
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return resource;
  }

  /**
   * This method is called to read the body of a resource.
   * 
   * @param is
   *          the xml input stream
   * @param site
   *          the site
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public T readBody(InputStream is, Site site) throws SAXException,
  IOException, ParserConfigurationException {
    if (resource == null) {
      resource = createResource(site);
    }
    readHeader = false;
    readBody = true;
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return resource;
  }

  /**
   * Sets the resource that needs to be further enriched with content from an
   * xml document.
   * 
   * @param resource
   *          the page
   */
  public void init(T resource) {
    this.resource = resource;
  }

  /**
   * Resets this parser instance.
   */
  @Override
  public void reset() {
    this.resource = null;
    this.parserContext = ParserContext.Document;
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.entwine.weblounge.common.security.User)
   */
  @Override
  protected void setOwner(User owner) {
    resource.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#allow(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    resource.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    resource.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  @Override
  protected void setModified(User modifier, Date date) {
    resource.setModified(modifier, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    resource.setPublished(publisher, startDate, endDate);
  }

  /**
   * The parser found the start of an element. Information about this element as
   * well as the attached attributes are passed to this method.
   * 
   * @param uri
   *          information about the namespace
   * @param local
   *          the local name of the element
   * @param raw
   *          the raw name of the element
   * @param attrs
   *          the element's attributes
   */
  @Override
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {

    // read the page url
    if (rootTag.equals(raw)) {
      parserContext = ParserContext.Resource;
      resource.getURI().setType(rootTag);
      resource.getURI().setIdentifier(attrs.getValue("id"));
      if (StringUtils.isNotBlank(attrs.getValue("path")))
        resource.getURI().setPath(attrs.getValue("path"));
      if (StringUtils.isNotBlank(attrs.getValue("version"))) {
        long version = ResourceUtils.getVersion(attrs.getValue("version"));
        resource.getURI().setVersion(version);
      }
    }

    // in the header
    else if ("head".equals(raw)) {
      parserContext = ParserContext.Head;
    }

    // in the body
    else if ("body".equals(raw)) {
      parserContext = ParserContext.Body;
    }

    if (readHeader) {

      // title, subject and the like
      if ("title".equals(raw) || "subject".equals(raw) || "description".equals(raw) || "coverage".equals(raw) || "rights".equals(raw)) {
        String language = attrs.getValue("language");
        if (language != null) {
          Language l = LanguageUtils.getLanguage(language);
          clipboard.put("language", l);
        } else {
          clipboard.remove("language");
        }
      }

    }

    super.startElement(uri, local, raw, attrs);

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    if (readHeader && parserContext.equals(ParserContext.Head)) {

      // Promote
      if ("promote".equals(raw)) {
        resource.setPromoted("true".equals(characters.toString()));
      }

      // Flavor
      else if ("flavor".equals(raw)) {
        resource.setFlavor(characters.toString());
      }

      // Title
      else if ("title".equals(raw)) {
        Language l = (Language) clipboard.remove("language");
        resource.setTitle(characters.toString(), l);
      }

      // Description
      else if ("description".equals(raw)) {
        Language l = (Language) clipboard.remove("language");
        resource.setDescription(characters.toString(), l);
      }

      // Coverage
      else if ("coverage".equals(raw)) {
        Language l = (Language) clipboard.remove("language");
        resource.setCoverage(characters.toString(), l);
      }

      // Rights
      else if ("rights".equals(raw)) {
        Language l = (Language) clipboard.remove("language");
        resource.setRights(characters.toString(), l);
      }

      // Subject
      else if ("subject".equals(raw)) {
        resource.addSubject(characters.toString());
      }

      // Series
      else if ("series".equals(raw)) {
        resource.addSeries(characters.toString());
      }

      // Pagelock
      else if ("locked".equals(raw)) {
        User user = (User) clipboard.remove("user");
        if (user != null)
          resource.lock(user);
      }

    }

    // Head
    if ("head".equals(raw)) {
      parserContext = ParserContext.Resource;
    }

    // Body
    else if ("body".equals(raw)) {
      parserContext = ParserContext.Resource;
    }

    // Have the super implementation handle the unkown
    else {
      super.endElement(uri, local, raw);
    }

  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  @Override
  public void warning(SAXParseException e) {
    logger.warn("Warning while reading {}: {}", resource, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  @Override
  public void error(SAXParseException e) {
    logger.warn("Error while reading {}: {}", resource, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  @Override
  public void fatalError(SAXParseException e) {
    logger.warn("Fatal error while reading {}: {}", resource, e.getMessage());
  }

}