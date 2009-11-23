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

package ch.o2it.weblounge.common.page;

import ch.o2it.weblounge.common.impl.page.PageReader;

import org.junit.Before;

import java.io.InputStream;

/**
 * Test case for the {@link PageReader}. This test case basically reads in a
 * sample page document and then executes the tests from the
 * {@link PageImplTest} class.
 * 
 * @see PageImplTest
 */
public class PageReaderTest extends PageImplTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    PageReader reader = new PageReader(mockSite);
    InputStream is = PageReaderTest.class.getResourceAsStream("/page.xml");
    page = reader.read(is, pageURI);
  }

}