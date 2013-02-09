/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.repository.ContentRepositoryOperation;
import ch.entwine.weblounge.common.repository.ContentRepositoryOperationListener;

/**
 * This listener implementation will, upon successful or failed operation
 * execution, call {@link Object#notifyAll()} on itself, so users can perform a
 * wait operation and therefore synchronize with the operation execution.
 */
public final class NotifyingOperationListener implements ContentRepositoryOperationListener {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ContentRepositoryOperationListener#executionSucceeded(ch.entwine.weblounge.common.repository.ContentRepositoryOperation)
   */
  public void executionSucceeded(ContentRepositoryOperation<?> operation) {
    synchronized (this) {
      this.notifyAll();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ContentRepositoryOperationListener#executionFailed(ch.entwine.weblounge.common.repository.ContentRepositoryOperation,
   *      java.lang.Throwable)
   */
  public void executionFailed(ContentRepositoryOperation<?> operation,
      Throwable t) {
    synchronized (this) {
      this.notifyAll();
    }
  }

}
