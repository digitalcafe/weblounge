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

package ch.entwine.weblounge.common.repository;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;

import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Service component that assists the content repository in serializing and
 * deserializing resources.
 */
public interface ResourceSerializer<S extends ResourceContent, T extends Resource<S>> {

  /**
   * Returns the resource type that this serializer can handle. Usually, this
   * will reflect the class name of the serialized resource.
   * 
   * @return the resource type
   */
  String getType();

  /**
   * Returns the resource content's mime type.
   * 
   * @param resourceContent
   *          the resource content
   * @return the content type
   */
  String getMimeType(S resourceContent);

  /**
   * Returns <code>true</code> if this serializer supports file content of this
   * type.
   * 
   * @param mimeType
   *          the content mime type
   * @return <code>true</code> if the serializer supports content of this type
   */
  boolean supports(String mimeType);

  /**
   * Returns a <code>ResourceReader</code> for the type of resources that is
   * supported by this serializer.
   * 
   * @return a new resource reader
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs during parser instantiation
   */
  ResourceReader<S, T> getReader() throws ParserConfigurationException,
      SAXException;

  /**
   * Returns the list of metadata to be added to the search index or
   * <code>null</code> if nothing needs to be added.
   * <p>
   * Implementations are free to either provide a merged version of live and
   * work version or simply return the plain live version.
   * <p>
   * Note that either one of <code>liveResource</code> and
   * <code>workResource</code> may be <code>null</code>.
   * 
   * @param resource
   *          the live resource
   */
  List<ResourceMetadata<?>> toMetadata(Resource<?> resource);

  /**
   * Takes the metadata and returns the resource representation.
   * 
   * @param site
   *          the site
   * @param metadata
   *          the resource metadata
   * @return the resource
   */
  Resource<?> toResource(Site site, List<ResourceMetadata<?>> metadata);

  /**
   * Returns a search result item from the resource metadata which contains the
   * data returned by the search index.
   * 
   * @param site
   *          the associated site
   * @param relevance
   *          the relevance of the search result
   * @param metadata
   *          the metadata
   * @return the search result item
   */
  SearchResultItem toSearchResultItem(Site site, double relevance,
      List<ResourceMetadata<?>> metadata);

  /**
   * Returns a <code>ResourceContentReader</code> for the type of resources that
   * is supported by this serializer.
   * 
   * @return the resource reader
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs during parser instantiation
   */
  ResourceContentReader<S> getContentReader()
      throws ParserConfigurationException, SAXException;

  /**
   * Creates a new resource for this type.
   * 
   * @param site
   *          the site
   * @return the new resource
   */
  Resource<S> newResource(Site site);

  /**
   * Creates a new image resource and add the existing EXIF data to the image
   * resource.
   * 
   * @param site
   *          the site
   * @param is
   *          the resource
   * @param user
   *          the creating user
   * @param language
   *          the language for the metadata
   * @return the new resource
   */
  Resource<S> newResource(Site site, InputStream is, User user,
      Language language);

  /**
   * Returns an object for preview generation or <code>null</code> if the
   * resource type does not provide preview generation.
   * 
   * @param resource
   *          the resource
   * @return the preview generator
   */
  PreviewGenerator getPreviewGenerator(Resource<?> resource);

  /**
   * Returns a renderer that can be used to display the resource, e. g as part
   * of a search result.
   * 
   * @return the preview renderer
   */
  PageletRenderer getSearchResultRenderer(Resource<?> resource);

}
