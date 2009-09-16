/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.mailer;

import java.io.IOException;
import java.util.Iterator;

/**
 * An interface for the mail transport backend.
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public interface Transport {

  /**
   * Send the email through the backend.
   * 
   * @param email
   *          the formatted email
   * @param from
   *          the sender of the mail
   * @param recipients
   *          all recipients of the mail
   * @throws IOException
   *           if an i/o error occurs
   * @throws EmailException
   *           if a protocol error occurs
   */
  public void send(StringBuffer email, Recipient from, Iterator recipients)
      throws IOException, EmailException;

}