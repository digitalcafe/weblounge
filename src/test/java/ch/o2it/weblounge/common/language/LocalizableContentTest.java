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

package ch.o2it.weblounge.common.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.language.LocalizableContent;

import org.junit.Test;

/**
 * Test case for the {@link LocalizableContent} implementation.
 */
public class LocalizableContentTest extends LocalizableObjectImplTest {

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#clear()}.
   */
  @Test
  public void testClear() {
    content.clear();
    assertEquals(0, content.size());
    assertFalse(content.supportsLanguage(english));
    assertFalse(content.supportsLanguage(french));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#isEmpty()}.
   */
  @Test
  public void testIsEmpty() {
    assertFalse(content.isEmpty());
    content.clear();
    assertTrue(content.isEmpty());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(2, content.size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#values()}.
   */
  @Test
  public void testValues() {
    assertEquals(2, content.values().size());
    assertTrue(content.values().contains(englishLocale.getDisplayLanguage()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#put(java.lang.Object, ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testPut() {
    content.put(englishLocale.getDisplayLanguage(), english);
    assertEquals(2, content.size());
    content.put(italianLocale.getDisplayLanguage(), italian);
    assertEquals(3, content.size());
    assertTrue(content.supportsLanguage(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#get()}.
   */
  @Test
  public void testGet() {
    assertEquals(englishLocale.getDisplayLanguage(), content.get());
    assertEquals(englishLocale.getDisplayLanguage(), content.get(italian));
    assertTrue(content.get(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#get(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(english, content.getLanguage());
    assertEquals(englishLocale.getDisplayLanguage(), content.get(italian));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#get(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testGetLanguageBoolean() {
    assertEquals(englishLocale.getDisplayLanguage(), content.get(italian, false));
    assertTrue(content.get(italian, true) == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#clone()}.
   */
  @Test
  public void testClone() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#toString()}.
   */
  @Test
  public void testToString() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#toString(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testToStringLanguage() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.LocalizableContent#toString(ch.o2it.weblounge.common.language.Language, boolean)}.
   */
  @Test
  public void testToStringLanguageBoolean() {
    fail("Not yet implemented"); // TODO
  }

}
