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
package ch.entwine.weblounge.taglib.security;

import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.security.Role;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * The body of this tag is only evaluated if the user has a certain role.
 * 
 * @author Tobias Wunden
 * @version 1.0 Mon Aug 05 2002
 * @since WebLounge 1.0
 */

public class RoleCheckTag extends WebloungeTag {

  /** serial uid */
  private static final long serialVersionUID = 8899627757239254637L;

  /** the roles to be checked */
  private List<Role> oneOf_;

  /** the roles to be checked */
  private List<Role> allOf_;

  /** the role context */
  private String context_;

  /** the role identifier */
  private String id_;

  /**
   * Constructor for class RoleCheckTag.
   */
  public RoleCheckTag() {
    oneOf_ = new ArrayList<Role>();
    allOf_ = new ArrayList<Role>();
  }

  /**
   * Sets the role context, e. g. <code>system</code>. Use this setter in
   * conjuction with {@link #setRoleId(String)}.
   * 
   * @param value
   *          the role context
   */
  public void setContext(String value) {
    context_ = value;
  }

  /**
   * Sets the role identifier. Use this setter in conjunction with
   * {@link #setContext(String)}.
   * 
   * @param value
   *          the role identifier
   */
  public void setRoleid(String value) {
    id_ = value;
  }

  /**
   * Specifies which role has to be acquired for the tag body to be displayed.
   * The role definition must consist of the form <code>context:id</code>.
   * 
   * @param value
   *          the role
   */
  public void setRole(String value) throws JspTagException {
    try {
      oneOf_.add(new RoleImpl(value));
      allOf_.add(new RoleImpl(value));
    } catch (IllegalArgumentException e) {
      throw new JspTagException(e);
    }
  }

  /**
   * Specifies a roleset. The user must own one of these roles for the tag body
   * to be displayed. The roleset must be provided as a coma separated list of
   * role definitions, e.g. <code>system:admin, system:editor</code>.
   * 
   * @param value
   *          the roleset
   */
  public void setOneof(String value) throws JspTagException {
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    String role;
    while (tok.hasMoreTokens()) {
      role = tok.nextToken();
      try {
        oneOf_.add(new RoleImpl(role));
      } catch (IllegalArgumentException e) {
        throw new JspTagException(e);
      }
    }
  }

  /**
   * Specifies a roleset. The user must own all of these roles for the tag body
   * to be displayed. The roleset must be provided as a coma separated list of
   * role definitions, e.g. <code>system:admin, system:editor</code>.
   * 
   * @param value
   *          the roleset
   */
  public void setAllof(String value) throws JspTagException {
    StringTokenizer tok = new StringTokenizer(value, ",");
    String role;
    while (tok.hasMoreTokens()) {
      role = tok.nextToken();
      try {
        allOf_.add(new RoleImpl(role));
      } catch (IllegalArgumentException e) {
        throw new JspTagException(e);
      }
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    if (context_ != null && id_ != null) {
      Role role;
      try {
        role = new RoleImpl(context_ + ":" + id_);
      } catch (IllegalArgumentException e) {
        throw new JspTagException(e);
      }
      allOf_.add(role);
      oneOf_.add(role);
    }
    return super.doStartTag();
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    reset();
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  public void reset() {
    allOf_ = new ArrayList<Role>();
    oneOf_ = new ArrayList<Role>();
    super.reset();
  }

  /**
   * Returns <code>true</code> if the user has one out of the "oneof" roleset.
   * 
   * @param user
   *          the user to check
   * @param site
   *          the site context
   * @return <code>true</code> if the user has one of the roles
   */
  protected boolean hasOneOf(User user, Site site) {
    for(Role role : oneOf_) {
      try {
        if(SecurityUtils.userHasRole(user, role)) 
          return true;
      } catch (IllegalArgumentException e) {
        
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the user has all out of the "allof" roleset.
   * 
   * @param user
   *          the user to check
   * @param site
   *          the site context
   * @return <code>true</code> if the user has all of the roles
   */
  protected boolean hasAllOf(User user, Site site) {
    for(Role role : allOf_) {
      if(!SecurityUtils.userHasRole(user, role)) 
        return false;
    }
    return true;
  }

}