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

package ch.entwine.weblounge.common.impl.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Test case for the {@link CronJobTrigger}.
 */
public class CronJobTriggerTest {

  /** Trigger firing several times per minute */
  protected CronJobTrigger minuteTrigger = null;

  /** Cron expression for the minutes trigger */
  protected String minutesExpression = "1,27,34 * * * *";

  /** Minutes that the trigger will fire */
  protected int[] minutes = new int[] { 1, 27, 34 };

  /** Trigger firing several times per hour */
  protected CronJobTrigger hourTrigger = null;

  /** Cron expression for the hours trigger */
  protected String hoursExpression = "0 4,23 * * *";

  /** Hours that the trigger will fire */
  protected int[] hours = new int[] { 4, 23 };

  /** Trigger firing several times per month */
  protected CronJobTrigger dayOfMonthTrigger = null;

  /** Cron expression for the day of month trigger */
  protected String dayOfMonthExpression = "0 0 2,12,26 * *";

  /** Days that the trigger will fire */
  protected int[] daysOfMonth = new int[] { 2, 12, 26 };

  /** Trigger firing several times per year */
  protected CronJobTrigger monthTrigger = null;

  /** Cron expression for the day of month trigger */
  protected String monthExpression = "0 0 1 1,jun,11 *";

  /** Months that the trigger will fire */
  protected int[] months = new int[] { 1, 6, 11 };

  /** Trigger firing several times per week */
  protected CronJobTrigger dayOfWeekTrigger = null;

  /** Cron expression for the day of week trigger */
  protected String dayOfWeekExpression = "0 0 * 1 mon,5";

  /** Days that the trigger will fire */
  protected int[] daysOfWeek = new int[] { 1, 5 };

  /** Today */
  protected Date now = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    minuteTrigger = new CronJobTrigger(minutesExpression);
    hourTrigger = new CronJobTrigger(hoursExpression);
    dayOfMonthTrigger = new CronJobTrigger(dayOfMonthExpression);
    monthTrigger = new CronJobTrigger(monthExpression);
    dayOfWeekTrigger = new CronJobTrigger(dayOfWeekExpression);
    now = new Date();
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#CronJobTrigger()}
   * .
   */
  @Test
  public void testCronJobTrigger() {
    Date now = new Date();
    CronJobTrigger emptyTrigger = new CronJobTrigger();
    Calendar c = Calendar.getInstance();
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.MINUTE, 1);
    Date triggerDate = c.getTime();
    assertEquals(triggerDate, emptyTrigger.getNextExecutionAfter(now));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#getNextExecutionAfter(java.util.Date)}
   * .
   */
  @Test
  public void testGetNextExecutionAfter() {
    Calendar c = null;
    Date expectedFireDate = null;

    // minute
    expectedFireDate = rollUp(Calendar.getInstance(), Calendar.MINUTE, minutes);
    assertEquals(expectedFireDate, minuteTrigger.getNextExecutionAfter(now));

    // hour
    c = Calendar.getInstance();
    c.set(Calendar.MINUTE, 0);
    expectedFireDate = rollUp(c, Calendar.HOUR_OF_DAY, hours);
    assertEquals(expectedFireDate, hourTrigger.getNextExecutionAfter(now));

    // day of month
    c = Calendar.getInstance();
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    expectedFireDate = rollUp(c, Calendar.DAY_OF_MONTH, daysOfMonth);
    assertEquals(expectedFireDate, dayOfMonthTrigger.getNextExecutionAfter(now));

    // month
    c = Calendar.getInstance();
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.DAY_OF_MONTH, 1);
    expectedFireDate = rollUp(c, Calendar.MONTH, months);
    assertEquals(expectedFireDate, monthTrigger.getNextExecutionAfter(now));

    // day of week
    c = Calendar.getInstance();
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.DAY_OF_MONTH, 1);
    expectedFireDate = rollUp(c, Calendar.MONTH, new int[] { 1 });
    expectedFireDate = rollUp(c, Calendar.DAY_OF_WEEK, daysOfWeek);
    assertEquals(expectedFireDate, dayOfWeekTrigger.getNextExecutionAfter(now));
  }

  /**
   * Moves the calendar into the future by increasing the given field until it
   * reaches the next matching value.
   * 
   * @param c
   *          the calendar
   * @param field
   *          the field index
   * @param fieldValues
   *          possible field values
   * @return the calendars new date
   */
  private Date rollUp(Calendar c, int field, int[] fieldValues) {
    boolean matches = false;

    // Align milliseconds and seconds
    c.setFirstDayOfWeek(Calendar.SUNDAY);
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);

    // Make sure we move past the current time
    Calendar now = Calendar.getInstance();

    int offset = 0;
    switch (field) {
      case Calendar.MONTH:
        offset = 1;
        break;
      case Calendar.DAY_OF_WEEK:
        offset = -1;
        break;
      default:
        offset = 0;
    }

    while (!c.after(now)) {
      c.add(field, 1);
    }
    
    while (!matches) {
      int calendarValue = c.get(field) + offset;
      for (int v : fieldValues) {
        if (calendarValue == v) {
          matches = true;
          break;
        }
      }
      if (!matches)
        c.add(field, 1);
    }

    return c.getTime();

    // c.setFirstDayOfWeek(Calendar.SUNDAY);
    // c.set(Calendar.MILLISECOND, 0);
    // c.set(Calendar.SECOND, 0);

    // int offset = 0;
    // Calendar now = Calendar.getInstance();
    // now.set(Calendar.MILLISECOND, 0);
    // now.set(Calendar.SECOND, 0);
    //
    // switch (field) {
    // case Calendar.MONTH:
    // offset = 1;
    // break;
    // case Calendar.DAY_OF_WEEK:
    // offset = -1;
    // break;
    // default:
    // offset = 0;
    // }
    // while (!matches) {
    // for (int i : fieldValues) {
    // int calendarValue = c.get(field) + offset;
    // if (i == calendarValue) {
    // matches = c.after(now);
    // }
    // }
    // if (!matches) {
    // c.add(field, 1);
    // matches = true;
    // }
    // }

  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#getMinutes()}
   * .
   */
  @Test
  public void testGetMinutes() {
    assertEquals(minutes.length, minuteTrigger.getMinutes().length);
    assertEquals(1, hourTrigger.getMinutes().length);
    assertEquals(1, dayOfMonthTrigger.getMinutes().length);
    assertEquals(1, monthTrigger.getMinutes().length);
    assertEquals(1, dayOfWeekTrigger.getMinutes().length);

    // Additional testing for minutes trigger
    for (int i = 0; i < minutes.length; i++)
      assertEquals(minutes[i], minuteTrigger.getMinutes()[i]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#getHours()}
   * .
   */
  @Test
  public void testGetHours() {
    assertEquals(24, minuteTrigger.getHours().length);
    assertEquals(2, hourTrigger.getHours().length);
    assertEquals(1, dayOfMonthTrigger.getHours().length);
    assertEquals(1, monthTrigger.getHours().length);
    assertEquals(1, dayOfWeekTrigger.getHours().length);

    // Additional testing for hours trigger
    for (int i = 0; i < hours.length; i++)
      assertEquals(hours[i], hourTrigger.getHours()[i]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#getDaysOfMonth()}
   * .
   */
  @Test
  public void testGetDaysOfMonth() {
    assertEquals(31, minuteTrigger.getDaysOfMonth().length);
    assertEquals(31, hourTrigger.getDaysOfMonth().length);
    assertEquals(3, dayOfMonthTrigger.getDaysOfMonth().length);
    assertEquals(1, monthTrigger.getDaysOfMonth().length);
    assertEquals(31, dayOfWeekTrigger.getDaysOfMonth().length);

    // Additional testing for the day of month trigger
    for (int i = 0; i < daysOfMonth.length; i++)
      assertEquals(daysOfMonth[i], dayOfMonthTrigger.getDaysOfMonth()[i]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#getMonths()}
   * .
   */
  @Test
  public void testGetMonths() {
    assertEquals(12, minuteTrigger.getMonths().length);
    assertEquals(12, hourTrigger.getMonths().length);
    assertEquals(12, dayOfMonthTrigger.getMonths().length);
    assertEquals(3, monthTrigger.getMonths().length);
    assertEquals(1, dayOfWeekTrigger.getMonths().length);

    // Additional testing for month trigger
    for (int i = 0; i < months.length; i++)
      assertEquals(months[i], monthTrigger.getMonths()[i]);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.scheduler.CronJobTrigger#getDaysOfWeek()}
   * .
   */
  @Test
  public void testGetDaysOfWeek() {
    assertEquals(7, minuteTrigger.getDaysOfWeek().length);
    assertEquals(7, hourTrigger.getDaysOfWeek().length);
    assertEquals(7, dayOfMonthTrigger.getDaysOfWeek().length);
    assertEquals(7, monthTrigger.getDaysOfWeek().length);
    assertEquals(2, dayOfWeekTrigger.getDaysOfWeek().length);

    // Additional testing for the day of week trigger
    for (int i = 0; i < daysOfWeek.length; i++)
      assertEquals(daysOfWeek[i], dayOfWeekTrigger.getDaysOfWeek()[i]);
  }

  /**
   * Test method for asterisk expressions.
   */
  @Test
  public void testAsteriskOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* * * * *");
    assertEquals(60, trigger.getMinutes().length);
    assertEquals(24, trigger.getHours().length);
    assertEquals(7, trigger.getDaysOfWeek().length);
    assertEquals(31, trigger.getDaysOfMonth().length);
    assertEquals(12, trigger.getMonths().length);
  }

  /**
   * Test method for range expressions like asterisk/3 in the hour field, which
   * results in these hours: <code>0, 3, 6, 9, 12, 15, 18, 21</code>.
   */
  @Test
  public void testModuloOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* */3 * * *");
    assertEquals(8, trigger.getHours().length);
    assertEquals(0, trigger.getHours()[0]);
    assertEquals(3, trigger.getHours()[1]);
    assertEquals(6, trigger.getHours()[2]);
    assertEquals(9, trigger.getHours()[3]);
    assertEquals(12, trigger.getHours()[4]);
    assertEquals(15, trigger.getHours()[5]);
    assertEquals(18, trigger.getHours()[6]);
    assertEquals(21, trigger.getHours()[7]);
  }

  /**
   * Test method for modulo overlap expressions like asterisk/61 in the minutes
   * field, which results in once every hour.
   */
  @Test
  public void testModuloOperatorOverlap() {
    CronJobTrigger trigger = new CronJobTrigger("*/61 * * * *");
    assertEquals(1, trigger.getMinutes().length);
    assertEquals(0, trigger.getMinutes()[0]);
    assertEquals(24, trigger.getHours().length);
  }

  /**
   * Test method for range expressions like 1,3 (1,3).
   */
  @Test
  public void testCommaOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* 1,3 * * *");
    assertEquals(2, trigger.getHours().length);
    assertEquals(1, trigger.getHours()[0]);
    assertEquals(3, trigger.getHours()[1]);
  }

  /**
   * Test method for range expressions like 1-6 (1,2,3,4,5,6).
   */
  @Test
  public void testRangeOperator() {
    CronJobTrigger trigger = new CronJobTrigger("* 1-3 * * *");
    assertEquals(3, trigger.getHours().length);
    assertEquals(1, trigger.getHours()[0]);
    assertEquals(2, trigger.getHours()[1]);
    assertEquals(3, trigger.getHours()[2]);
  }

  /**
   * Test method for <code>@restart</code>.
   */
  @Test
  public void testRestart() {
    CronJobTrigger rebootTrigger = new CronJobTrigger("@restart");
    assertEquals(now, rebootTrigger.getNextExecutionAfter(now));
  }

  /**
   * Test method for <code>@yearly</code>, which is equivalent to
   * <code>0 0 1 1 *</code>.
   */
  @Test
  public void testYearly() {
    CronJobTrigger yearlyTrigger = new CronJobTrigger("@yearly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 1 1 *");
    assertTrue(compare(yearlyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@annually</code>, which is equivalent to
   * <code>0 0 1 1 *</code> and of course <code>@yearly</code>.
   */
  @Test
  public void testAnnually() {
    CronJobTrigger annuallyTrigger = new CronJobTrigger("@annually");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 1 1 *");
    assertTrue(compare(annuallyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@monthly</code>, which is equivalent to
   * <code>0 0 1 * *</code>.
   */
  @Test
  public void testMonthly() {
    CronJobTrigger monthlyTrigger = new CronJobTrigger("@monthly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 1 * *");
    assertTrue(compare(monthlyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@weekly</code>, which is equivalent to
   * <code>0 0 * * 0</code>.
   */
  @Test
  public void testWeekly() {
    CronJobTrigger weeklyTrigger = new CronJobTrigger("@weekly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 * * 0");
    assertTrue(compare(weeklyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@daily</code>, which is equivalent to
   * <code>0 0 * * *</code>.
   */
  @Test
  public void testDaily() {
    CronJobTrigger dailyTrigger = new CronJobTrigger("@daily");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 * * *");
    assertTrue(compare(dailyTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@midnight</code>, which is equivalent to
   * <code>0 0 * * *</code> and of course to <code>@daily</code>.
   */
  @Test
  public void testMidnight() {
    CronJobTrigger midnightTrigger = new CronJobTrigger("@midnight");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 0 * * *");
    assertTrue(compare(midnightTrigger, equivalentTrigger));
  }

  /**
   * Test method for <code>@hourly</code>, which is equivalent to
   * <code>0 * * * *</code>.
   */
  @Test
  public void testHourly() {
    CronJobTrigger hourlyTrigger = new CronJobTrigger("@hourly");
    CronJobTrigger equivalentTrigger = new CronJobTrigger("0 * * * *");
    assertTrue(compare(hourlyTrigger, equivalentTrigger));
  }

  /**
   * Compares to cron triggers.
   * 
   * @param a
   *          trigger a
   * @param b
   *          trigger b
   */
  private boolean compare(CronJobTrigger a, CronJobTrigger b) {
    // minutes
    assertEquals(a.getMinutes().length, b.getMinutes().length);
    for (short i = 0; i < a.getMinutes().length; i++) {
      assertEquals(a.getMinutes()[i], b.getMinutes()[i]);
    }
    // hours
    assertEquals(a.getHours().length, b.getHours().length);
    for (short i = 0; i < a.getHours().length; i++) {
      assertEquals(a.getHours()[i], b.getHours()[i]);
    }
    // daysOfWeek
    assertEquals(a.getDaysOfWeek().length, b.getDaysOfWeek().length);
    for (short i = 0; i < a.getDaysOfWeek().length; i++) {
      assertEquals(a.getDaysOfWeek()[i], b.getDaysOfWeek()[i]);
    }
    // month
    assertEquals(a.getMonths().length, b.getMonths().length);
    for (short i = 0; i < a.getMonths().length; i++) {
      assertEquals(a.getMonths()[i], b.getMonths()[i]);
    }
    // daysOfMonth
    assertEquals(a.getDaysOfMonth().length, b.getDaysOfMonth().length);
    for (short i = 0; i < a.getDaysOfMonth().length; i++) {
      assertEquals(a.getDaysOfMonth()[i], b.getDaysOfMonth()[i]);
    }
    return true;
  }

}
