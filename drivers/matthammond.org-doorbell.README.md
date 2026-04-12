# Driver for matthammond.org zigbee doorbell hack

This is a driver for a [custom zigbee doorbell hack](https://github.com/matt-hammond-001/zigbee-esp32c6-doorbell) for the [Hubitat Elevation](https://hubitat.com/) platform.

The hardware and code is based around a SeeedStudio / XIAO ESP32-C6 board and expressif's libraries for the arduino IDE.

## How to install

Install this code as a user driver, from the Hubitat Elevation web based interface:

1. Click "Drivers Code" then click "New Driver" then "Import"

2. Enter the Import URL then click "Import"

   https://raw.githubusercontent.com/matt-hammond-001/hubitat-code/master/matthammond.org-doorbell.groovy

3. Click "Save"

## Features and behaviour

Implements the following "capabilities", meaning it can be treated as any of the following:

* Pushable button (button number 1)
* Motion sensor
* Presence sensor
* On/Off Switch (read only)

By default, if you "manually" push the button via hubitat (e.g. via the driver UI)
then the button will automatically "release" after a few seconds delay. The exact delay can be set as a preference. Minimum delay is 1 second.

## Author and licence

Copyright (c) 2020, Matt Hammond

All rights reserved.

This driver is made available under the BSD 3-clause licence. See full licence in comment block at the start of the source code.
