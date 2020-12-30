# Driver for Tuya Zigbee Lighting Dimmer modules

This is a driver for *Tuya zigbee lighting dimmer modules* for the [Hubitat Elevation](https://hubitat.com/) platform.

Currently supported devices:

| Model                     | What                             | Identifies via Zigbee as        |
| :------------------------ | :------------------------------- | :------------------------------ |
| QS-Zigbee-D02-TRIAC-LN    | 1 channel (1-gang) dimmer module | TS110F <br/> `_TYZB01_qezuin6k` |
| QS-Zigbee-D02-TRIAC-2C-LN | 2 channel (2-gang) dimmer module | TS110F <br/> `_TYZB01_v8gtiaed` |


## How to install

Install this code as a user driver, from the Hubitat Elevation web based interface:

1. Click "Drivers Code" then click "New Driver" then "Import"

2. Enter the Import URL then click "Import"

   https://raw.githubusercontent.com/matt-hammond-001/hubitat-code/master/drivers/tuya-zigbee-dimmer-module.groovy

3. Click "Save"


## Features

**Supports basic turning on/off and setting the dimmer level only.**. These tuya switch modules do not appear to support more advanced functionality such as setting the duration of a change of level; setting step sizes or starting/stopping a continuous change.

The driver has the following features:

* **Minimum level (percentage)** ... lets you set the minimum brightness level that the dimmer will use.

    Experiment with setting the dimmer brightness level to see how low it can go without the lights turning off.
    
    Then set that percentage level as the *minimum level*. From now on, setting the dimmer to 0% will set it to this minimum level and no lower.

* **Turn on when level adjusted** ... turn this option on and whenever you adjust the dimmer level, the dimmer will automatically switch on.

## 2 channel dimmers

When used with a single channel dimmer, the driver creates a single device.

For dimmers with multiple channels, it will create a separate child device for each channel. The switch and dimmer controls of the parent will control the first channel.

Settings for the parent device and first channel are shared and are synced. Changing settings for the first child will change them for the parent, and changing them for the parent will also change them for the first child (but not other children)

## Author and licence

Copyright (c) 2020, Matt Hammond

All rights reserved.

This driver is made available under the BSD 3-clause licence. See full licence in comment block at the start of the source code.
