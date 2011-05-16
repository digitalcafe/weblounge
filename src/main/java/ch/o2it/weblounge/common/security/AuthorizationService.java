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

package ch.o2it.weblounge.common.security;

import ch.o2it.weblounge.common.content.Resource;

/**
 * Provides generation and interpretation of policy documents in media packages
 */
public interface AuthorizationService {

  /**
   * Determines whether the current user can take the specified action on the
   * resource.
   * 
   * @param resource
   *          the resource
   * @param action
   *          the action (e.g. read, modify, delete)
   * @return whether the current user has the correct privileges to take this
   *         action
   */
  boolean hasPermission(Resource<?> resource, String action);

  /**
   * Gets the permissions associated with this resource, as specified by its
   * XACML attachment.
   * 
   * @param resource
   *          the resource
   * @return the set of permissions and explicit denials
   * @throws MediaPackageException
   *           if the policy can not be read from the resource
   */
  AccessControlList getAccessControlList(Resource<?> resource);

  /**
   * Attaches the provided policies to a resource as a XACML attachment.
   * 
   * @param resource
   *          the resource
   * @param accessControlList
   *          the tuples of roles to actions
   * @return the resource with attached XACML policy
   * @throws MediaPackageException
   *           if the policy can not be attached to the resource
   */
  Resource<?> setAccessControl(Resource<?> resource,
      AccessControlList accessControlList);

}
