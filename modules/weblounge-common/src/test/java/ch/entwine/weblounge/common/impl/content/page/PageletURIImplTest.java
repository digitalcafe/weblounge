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

package ch.entwine.weblounge.common.impl.content.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link PageletURIImpl}.
 */
public class PageletURIImplTest {
  
  /** Page identifier */
  protected ResourceURI uri = null;
  
  /** Associated site */
  protected Site site = null;
  
  /** Path to the page */
  protected String path = "/test";
  
  /** Enclosing composer */
  protected String composer = "main";
  
  /** Pagelet position */
  protected int position = 1;
  
  /** The pagelet location instance under test */
  protected PageletURIImpl location = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    uri = new PageURIImpl(site, path);
    location = new PageletURIImpl(uri, composer, position);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageletURIImpl#getSite()}.
   */
  @Test
  public void testGetSite() {
    assertEquals(site, location.getSite());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageletURIImpl#getPageURI()}.
   */
  @Test
  public void testGetURI() {
    assertEquals(uri, location.getPageURI());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageletURIImpl#getComposer()}.
   */
  @Test
  public void testGetComposer() {
    assertEquals(composer, location.getComposer());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageletURIImpl#getPosition()}.
   */
  @Test
  public void testGetPosition() {
    assertEquals(position, location.getPosition());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageletURIImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    uri = new PageURIImpl(site, path);
    location = new PageletURIImpl(uri, composer, position);
    PageletURI sameLocation = new PageletURIImpl(uri, composer, position);
    assertEquals(location, sameLocation);
    assertFalse(location.equals(new PageletURIImpl(new PageURIImpl(site, "/test2"), composer, position)));
    assertFalse(location.equals(new PageletURIImpl(uri, "main2", position)));
    assertFalse(location.equals(new PageletURIImpl(uri, composer, position + 1)));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.page.PageletURIImpl#compareTo(ch.entwine.weblounge.common.content.page.PageletURI)}.
   */
  @Test
  public void testCompareTo() {
    PageletURI previous = new PageletURIImpl(uri, composer, position - 1);
    PageletURI next = new PageletURIImpl(uri, composer, position + 1);
    assertEquals(1, location.compareTo(previous));
    assertEquals(0, location.compareTo(location));
    assertEquals(-1, location.compareTo(next));
  }

}
