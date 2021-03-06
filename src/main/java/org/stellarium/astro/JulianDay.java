/*
Copyright (C) 2000 Liam Girdwood <liam@nova-ioe.org>
Copyright (C) 2003 Fabien Chereau
Copyright (C) 2006 Jerome Beau

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Library General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/
package org.stellarium.astro;

import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * The JulianDay supports turning Gregorian and Julian (Caesar) dates
 * to/from Julius Scaliger's clever Julian Day Numbers.  In 1583,
 * Julius had a great idea, and started to count the number of days
 * starting on -4712-01-01 at noon (Z).  This is a wonderful
 * "intermediate form" is still used for calendar calculations today!
 *
 * @author Kleanthes Koniaris
 */
public class JulianDay {
    public static final double J2000 = 2451545.0;

    // Human readable (easy printf) date format

    public static double dateToJulian(Date date) {
        JulianDay day2 = new JulianDay(date);
        return day2.julianDay();
    }

    /**
     * Calculate tm struct from julian day
     *
     * @param jd Julian Day
     * @return A TM structure that represents the Julian Day.
     */
    public static Date julianToDate(double jd) {
        JulianDay day2 = new JulianDay(jd);
        return new Date(day2.year() - 1900, day2.month() - 1, day2.dayOfMonth(), day2.hour(), day2.minute(), day2.second());
    }

    /**
     * Calculate julian day from system time.
     */
    public static double getJulianFromSys() {
        return dateToJulian(new Date());
    }

    private double julianDay;
    /**
     * year
     */
    private int y;
    /**
     * month
     */
    private int mo;
    /**
     * day
     */
    private int day;
    /**
     * hour
     */
    private int h;
    /**
     * minute
     */
    private int m;
    /**
     * second
     */
    private int s;
    /**
     * the day fraction (d <-> {day, h, m, s})
     */
    private double d;

    private static SimpleTimeZone z;
    private static final double THIRTYPLUS;

    private static final int SECONDS_IN_DAY = 24 * 60 * 60;
    private static final double HALF_SECOND = 0.5;

    /**
     * Returns the Julian Day, i.e., the number of days since 12h GMT
     * in the year -4712, aka 4713 B.C.
     */
    public double julianDay() {
        return julianDay;
    }

    /**
     * Reterns the day as a float that also accounts for H, M and S.
     * For example, the middle (noon) of the 3rd day would be
     * represented as 3.5.
     */
    public double d() {
        return d;
    }

    /**
     * Returns the year.
     */
    public int year() {
        return y;
    }

    /**
     * Returns the month.  Note that this is the Calendar.JANUARY ==
     * 0, but we return one for January, so be careful.  What were
     * they thinking at Sun to do such a non-standard thing---and for
     * no good reason, as far as I can tell!
     */
    public int month() {
        return mo;
    }

    /**
     * Returns the day of the month.
     */
    public int dayOfMonth() {
        return day;
    }

    /**
     * Returns the day hour (0..23).
     */
    public int hour() {
        return h;
    }

    /**
     * Returns the minute of the hour (0..59).
     */
    public int minute() {
        return m;
    }

    /**
     * Ruterns the second of the hour.
     */
    public int second() {
        return s;
    }

    private void setD() {
        d = (double) day + ((double) (s + 60 * (m + 60 * h))) / (double) SECONDS_IN_DAY;
    }

    /**
     * Turns the current time into a JulianDay.
     */
    public JulianDay() throws Exception {
        this(new Date());
    }

    public JulianDay(Date someDate) {
        y = 1900 + someDate.getYear();
        mo = 1 + someDate.getMonth(); // +1 cuz January is 0 to Sun!
        day = someDate.getDate();
        h = someDate.getHours();
        m = someDate.getMinutes();
        s = someDate.getSeconds();
        setD();
        setJulianDay();
    }

    /**
     * Makes a new JulianDay, probably by means of adding offsets to
     * JulianDates generated by other means.
     *
     * @param julianDay
     */
    public JulianDay(double julianDay) {
        assert julianDay >= 0 : "We can only handle positive Julian Days.";
        this.julianDay = julianDay;
        double jd = julianDay + HALF_SECOND;
        int z = (int) Math.floor(jd);
        double f = jd - z;      // a fraction between zero and one
        int a = z;
        if (z >= 2299161) {     // (typo in the book)
            int alpha = (int) Math.floor((z - 1867216.25) / 36524.25);
            a += 1 + alpha - (int) Math.floor(alpha / 4.0);
        }
        int b = a + 1524;
        int c = (int) Math.floor((b - 122.1) / 365.25);
        int dd = (int) Math.floor(365.25 * c);
        int e = (int) Math.floor((b - dd) / THIRTYPLUS);
        // and then,
        this.d = b - dd - Math.floor(THIRTYPLUS * e) + f;
        setDayHourMinuteSecond();
        mo = e - ((e < 14) ? 1 : 13);
        // sometimes the year is too high by one
        y = c - ((mo > 2) ? 4716 : 4715);
    }

    /**
     * Creates a new JulianDay given a year, month, day, hour (24h),
     * minute and second.
     *
     * @param day
     * @param h
     * @param m
     * @param mo
     * @param s
     * @param y
     */
    public JulianDay(int y, int mo, int day, int h, int m, int s) {
        this.y = y;
        this.mo = mo;
        this.day = day;
        this.h = h;
        this.m = m;
        this.s = s;
        setD();
        setJulianDay();
    }

    /**
     * Creates a new JulianDay given a year, month, and day-fraction,
     * where 12.5 means noon on the 12th day of the month.
     *
     * @param d
     * @param mo
     * @param y
     */
    public JulianDay(int y, int mo, double d) {
        this.y = y;
        this.mo = mo;
        this.d = d;
        setDayHourMinuteSecond();
        setJulianDay();
    }

    /**
     * Sets day, hour, minute and second on the basis of d.
     */
    private void setDayHourMinuteSecond() {
        int seconds = (int) Math.round(SECONDS_IN_DAY * d); // how many seconds in the day, d
        s = seconds % 60;
        int minutes = (seconds - s) / 60;
        m = minutes % 60;
        int hours = (minutes - m) / 60;
        h = hours % 24;
        day = (hours - h) / 24;
    }

    static {
        JulianDay.z = new SimpleTimeZone(0, "Z");
        THIRTYPLUS = 30.6001;
    }

    /**
     * Is the point in time after 4 October 1582?
     *
     * @return
     */
    public boolean dateIsGregorian() {
        // 4 October 1582
        if (y > 1582) return true;
        if (y < 1582) return false;
        // shit, somebody is messing with us!
        if (mo > 10) return true;
        if (mo < 10) return false;
        // now it is evident that they're fucking with us---or it is
        // my Monte-Carlo testing.  :)
        if (day > 4) return true;
        if (day < 4) return false;
        return false;           // this is probably the last non-Gregorian date?
    }

    /**
     * Returns the Juilan Day, the number of days since 12h GMT in
     * the year -4712, aka 4713 B.C.
     */
    private void setJulianDay() {
        int year = y;
        int month = mo;

        // here we go with Jean Meeus, p. 61 of 2nd edition of Astronomical Algorithms
        if (month <= 2) {
            year += -1;
            month += 12;
        }
        int b;
        if (dateIsGregorian()) {
            int a = (int) Math.floor(((double) year) / 100.0);
            b = 2 - a + (int) Math.floor(a / 4.0);
        } else {
            b = 0;
        }

        julianDay = Math.floor(365.25 * (year + 4716)) + Math.floor(THIRTYPLUS * (month + 1)) + d + b - 1524.5;
    }
}
