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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.Path;

/**
 * A resource uri defines the location of a resource within a <code>Site</code>.
 * In addition, it provides a unique identifier and a resource version.
 */
public interface ResourceURI extends Path {

  /**
   * Returns a string which uniquely identifies this resource and version.
   * 
   * @return the uid
   */
  String getUID();

  /**
   * Sets the resource identifier.
   * 
   * @param identifier
   *          the identifier
   */
  void setIdentifier(String identifier);

  /**
   * Returns the page identifier.
   * 
   * @return the page identifier
   */
  String getIdentifier();

  /**
   * Sets the uri path.
   * 
   * @param path
   *          the path
   */
  void setPath(String path);

  /**
   * Sets the resource type.
   * 
   * @param type
   *          the type
   */
  void setType(String type);

  /**
   * Returns the resource type, which usually equals the class name of the
   * represented resource.
   * 
   * @return the resource type
   */
  String getType();

  /**
   * Sets the version.
   * 
   * @param version
   *          the document version
   */
  void setVersion(long version);

  /**
   * Returns the page version. The version represents the various editing stages
   * of a page. Note that there are two special version values:
   * <ul>
   * <li>{@link Resource#LIVE}: the live version of the page</li>
   * <li>{@link Resource#WORK}: the work version of the page</li>
   * </ul>
   * 
   * @return the version
   */
  long getVersion();

  /**
   * Returns the page uri for the given version.
   * 
   * @return the page uri
   */
  ResourceURI getVersion(long version);

  /**
   * Returns the associated site.
   * 
   * @return the site
   */
  Site getSite();

  /**
   * Returns the URI for the parent page. If the current URI is the site's root
   * then <code>null</code> is returned.
   * <p>
   * <b>Note:</b> The version of the parent uri will match the version of this
   * uri. Keep in mind that the parent page might not exist in that version, so
   * be careful when requesting the associated page.
   * 
   * @return the parent uri
   */
  ResourceURI getParentURI();

}