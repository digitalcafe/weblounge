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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.content.Tag;
import ch.entwine.weblounge.common.request.CacheTag;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test case for {@link CacheTagSet}.
 */
public class CacheTagSetTest {

  /** The cache tag set under test */
  protected CacheTagSet set = null;

  /** Value for tag "a" */
  protected String aValue = "a";

  /** Value for tag "b" */
  protected String bValue = "b";

  /** Value for tag "c" */
  protected String cValue = "c";

  /** Sample tag "a" */
  protected CacheTagImpl a = new CacheTagImpl("a", aValue);

  /** Sample tag "b" */
  protected CacheTagImpl b = new CacheTagImpl("b", bValue);

  /** Sample tag "b" */
  protected CacheTagImpl c = new CacheTagImpl("c", cValue);

  /** Sample tag "any" */
  protected CacheTagImpl any = new CacheTagImpl("any");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    set = new CacheTagSet();
    set.add(a);
    set.add(b);
    set.add(c);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#add(java.lang.String, java.lang.Object)}
   * .
   */
  @Test
  public void testAddStringObject() {
    set.add("a", aValue);
    assertEquals(3, set.size());
    set.add("a", bValue);
    assertEquals(4, set.size());
    set.add("d", aValue);
    assertEquals(5, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#add(ch.entwine.weblounge.common.content.Tag)}
   * .
   */
  @Test
  public void testAddTag() {
    set.add(a);
    assertEquals(3, set.size());
    set.add(new CacheTagImpl("a", aValue));
    assertEquals(3, set.size());
    set.add(new CacheTagImpl("d", aValue));
    assertEquals(4, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#excludeTagsWith(java.lang.String)}
   * .
   */
  @Test
  public void testExcludeTagsWithString() {
    set.excludeTagsWith("d");
    assertTrue(set.contains(new CacheTagImpl("d", CacheTag.ANY)));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#excludeTagsWith(java.util.Collection)}
   * .
   */
  @Test
  public void testExcludeTagsWithCollectionOfString() {
    List<String> keys = new ArrayList<String>();
    keys.add("d");
    keys.add("e");
    set.excludeTagsWith(keys);
    assertTrue(set.contains(new CacheTagImpl("d", CacheTag.ANY)));
    assertTrue(set.contains(new CacheTagImpl("e", CacheTag.ANY)));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#addAll(java.util.Collection)}
   * .
   */
  @Test
  public void testAddAll() {
    List<CacheTagImpl> tags = new ArrayList<CacheTagImpl>();
    tags.add(new CacheTagImpl("d"));
    tags.add(new CacheTagImpl("e"));
    set.addAll(tags);
    assertTrue(set.contains(new CacheTagImpl("d", CacheTag.ANY)));
    assertTrue(set.contains(new CacheTagImpl("e", CacheTag.ANY)));
    assertEquals(5, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#clear()}.
   */
  @Test
  public void testClear() {
    set.clear();
    assertEquals(0, set.size());
    assertTrue(set.isEmpty());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#contains(java.lang.Object)}
   * .
   */
  @Test
  public void testContains() {
    assertTrue(set.contains(a));
    assertFalse(set.contains(new CacheTagImpl("f")));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#containsAll(java.util.Collection)}
   * .
   */
  @Test
  public void testContainsAll() {
    List<Tag> tags = new ArrayList<Tag>();
    tags.add(a);
    tags.add(c);
    assertTrue(set.containsAll(tags));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#isEmpty()}.
   */
  @Test
  public void testIsEmpty() {
    assertFalse(set.isEmpty());
    set.clear();
    assertTrue(set.isEmpty());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#iterator()}.
   */
  @Test
  public void testIterator() {
    assertNotNull(set.iterator());
    assertTrue(set.iterator().hasNext());
    Iterator<CacheTag> iter = set.iterator();
    for (int i = 0; i < 3; i++)
      iter.next();
    assertFalse(iter.hasNext());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#remove(java.lang.Object)}
   * .
   */
  @Test
  public void testRemoveObject() {
    set.remove(a);
    assertEquals(2, set.size());
    set.remove(new CacheTagImpl("e"));
    assertEquals(2, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#remove(java.lang.String, java.lang.Object)}
   * .
   */
  @Test
  public void testRemoveStringObject() {
    set.remove("a", aValue);
    assertEquals(2, set.size());
    set.remove("b", aValue);
    assertEquals(2, set.size());
    set.remove("a", bValue);
    assertEquals(2, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#removeAll(java.util.Collection)}
   * .
   */
  @Test
  public void testRemoveAll() {
    List<Tag> tags = new ArrayList<Tag>();
    tags.add(a);
    tags.add(c);
    tags.add(new CacheTagImpl("e"));
    set.removeAll(tags);
    assertEquals(1, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#removeAllByTagName(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveAllByTagName() {
    set.add(new CacheTagImpl("a", bValue));
    set.removeAllByTagName("b");
    assertEquals(3, set.size());
    set.removeAllByTagName("a");
    assertEquals(1, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#retainAll(java.util.Collection)}
   * .
   */
  @Test
  public void testRetainAll() {
    List<Tag> tags = new ArrayList<Tag>();
    tags.add(a);
    tags.add(c);
    tags.add(new CacheTagImpl("e"));
    set.retainAll(tags);
    assertEquals(2, set.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.request.CacheTagSet#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(3, set.size());
  }

}
