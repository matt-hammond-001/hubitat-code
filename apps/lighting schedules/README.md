# Lighting schedules

This is an app for the [Hubitat Elevation](https://hubitat.com/) platform.

It provides an easy way to schedule on/off switch devices such as lights according to timings.

* Timings are written simply and quickly like this:
   ```
   sunrise-20 - 0720, 13:00-14:00, sunset+30 - 00:00
   ```

* It can be set to be active at all times, or only for certain *modes*. 

* It can be set to constantly force lights on/off, or to only turn them on/off at the start or end of the timing periods you defined.


## How to install

Install this code as a custom application, from the Hubitat Elevation web based interface:

1. Click "Apps Code" then click "New App" then "Import"

2. Enter the Import URL then click "Import"

   https://raw.githubusercontent.com/matt-hammond-001/hubitat-code/master/apps/lighting%20schedules/lighting-schedules-parent.groovy

3. Click "Save"

4. Repeat steps 1-3 for the child app:

   https://raw.githubusercontent.com/matt-hammond-001/hubitat-code/master/apps/lighting%20schedules/lighting-schedules-child.groovy

NOTE: You must install the parent app before the child app.


## How to use Lighting Schedules

### Choosing switches

Select a set of on/off switch devices that will be controlled.

### Specifying time periods

For each switch, enter a set of simple timing intervals to describe when the lights should be on or off.

For each switch you can type in one or more time periods, separated nby spaces or commas. Each time period is witten in the form *START_TIME - END_TIME* where the start
and end times can be:
* `HH:MM` - a time of day in 24h format
* `sunrise` or `sunset`
* `sunrise+/-OFFSET` or `sunset+/-OFFSET` - a time before or after sunrise or sunset by OFFSET minutes   

Here are some examples:
 * 0900-1120
 * 0900-1120; 1500-1600, 2300-0500
 * 12:00-13:00, 15:25-15:26
 * sunrise-12:00, 15:00-sunset+30
 * sunrise-30 - 0800, 1200-1300, sunset-25 - 2359

This is re-evaluated every day, so *sunrise* and *sunset* will always be accurate.

### Controlling behaviour

Lighting Schedules allows you to choose how it behaves for each switch, during time periods, and outside time periods, and depending on the current MODE.

Choose what modes it should be active within. If it is not active in all modes, then you can specify how it behaves when a mode change takes place.

When active, you can set whether it causes switches to be switched on/off when a period begins, or ends, or during the whole time. This allows you, for example, to create a setup that will ensure a light gets turned off at certain times, but doesn't repeatedly force it to be on or off during the whole time of a period, or outside of a period.

## Examples

### When at home, ensure outdoor light stays on during evening and off at all other times

   * Switching behaviour:
      * During specified periods: *Check and turn ON regularly*
      * At other times: *Check and turn OFF regularly*
      * Active in all modes: *no*
      * Active in modes: *At HOME*
      * When becoming ACTIVE due to a MODE change: *Immediately turn ON or OFF according to the schedule*
      * When becoming INACTIVE due to a MODE change: *Turn all OFF immediately*
   * Timings
      * *sunset-30 - 00:00*


### Ensure a light gets turned off at midnight and at 1am, but isn't controlled at other times:

   * Switching behaviour:
      * During specified periods: *Check and turn oOFF only when previously not in a period.* 
      * At other times: *Do nothing*
      * Active in all modes: *yes* 
   * Timings
      * *00:00-00:30, 01:00-01:30*


## Author and licence

Copyright (c) 2020, Matt Hammond

All rights reserved.

This app is made available under the BSD 3-clause licence. See full licence in comment block at the start of the source code.
