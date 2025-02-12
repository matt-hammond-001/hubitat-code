/* 
=============================================================================
Hubitat Elevation Driver for
Tuya Zigbee dimmer modules (1-Gang and 2-Gang)

    https://github.com/matt-hammond-001/hubitat-code

-----------------------------------------------------------------------------
This code is licensed as follows:

BSD 3-Clause License

Copyright (c) 2020, Matt Hammond
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-----------------------------------------------------------------------------
ver 0.2.0  11/12/2021 Matt Hammond - added _TZ3000_7ysdnebc
ver 0.2.1  2021/12/29 kkossev      - added cluster 0003 to the fingerprint, model _TZ3000_7ysdnebc
ver 0.2.2  2022/05/28 kkossev      - moved model and inClusters to modelConfigs, added _TZE200_vm1gyrso 3-Gang Dimmer module
ver 0.2.3  2022/08/30 kkossev      - added TS110E _TZ3210_ngqk6jia fingerprint
ver 0.2.4  2022/09/19 kkossev      - added TS0601 _TZE200_w4cryh2i fingerprint
ver 0.2.5  2022/10/19 kkossev      - TS0601 level control; infoLogging
ver 0.2.6  2022/10/22 kkossev      - importURL to dev. branch; toggle() for TS0601; 'autoOn' for TS0601; level scaling for TS0601; minLevel and maxLevel receive/send for TS0601; bugfixes for TS0601 single EP devices
ver 0.2.7  2022/11/11 kkossev      - added _TZE200_ip2akl4w _TZE200_1agwnems _TZE200_la2c2uo9 _TZE200_579lguh2 _TZE200_fjjbhx9d _TZE200_drs6j6m5; secure the while loops coode when deleting and creating child devices;
ver 0.2.8  2022/11/13 kkossev      - _TZE200_ip2akl4w fingerprint hardcoded
ver 0.2.9  2022/12/10 kkossev      - deleting child devices bug fix; added _TZE200_fvldku9h Tuya Fan Switch; unscheduling old periodic jobs; Tuya Time Sync';
ver 0.2.10 2023/01/02 kkossev      - added _TZE200_e3oitdyu 
ver 0.2.11 2023/02/19 kkossev      - added TS110E _TZ3210_k1msuvg6; TS0601 _TZE200_r32ctezx fan controller; changed importURL to dev. branch; dp=4 - type of light source?; added GLEDOPTO GL-SD-001; 1-gang modles bug fixes;
ver 0.2.12 2023/03/12 kkossev      - more debug logging; fixed incorrect on/off status reporting bug for the standard ZCL dimmers; added autoRefresh option for GLEDOPTO
ver 0.3.0  2023/03/12 kkossev      - bugfix: TS110E/F configiration for the automatic level reporting was not working.
ver 0.4.0  2023/03/25 kkossev      - added TS110E _TZ3210_pagajpog; added advancedOptions; added forcedProfile; added deviceProfilesV2; added initialize() command; sendZigbeeCommands() in all Command handlers; configure() and updated() do not re-initialize the device!; setDeviceNameAndProfile(); destEP here and there
ver 0.4.1  2023/03/31 kkossev      - added new TS110E_GIRIER_DIMMER product profile (Girier _TZ3210_k1msuvg6 support @jshimota); installed() initialization and configuration sequence changed'; fixed GIRIER Toggle command not working; added _TZ3210_4ubylghk
ver 0.4.2  2023/04/10 kkossev      - added TS110E_LONSONHO_DIMMER; decode correction level/10; fixed exception for non-existent child device; all Current States are cleared on Initialize; Lonsonho brightness control; Hubitat 'F2 bug' patched; Lonsonho change level uses cluster 0x0008
ver 0.4.3  2023/04/12 kkossev      - numEps bug fix; generic ZCL dimmer support; patch for Girier firmware bug on Refresh command 01 reporting off state; DeviceWrapper fixes; added TS0505B_TUYA_BULB; bugfix when endpointId is different than 01
ver 0.4.4  2023/04/23 kkossev      - added capability 'Health Check'; Lonsonho dimmers setLevel working now (parent device) !
ver 0.4.5  2023/05/17 kkossev      - removed obsolete deviceSimulation options; added _TZ3210_ngqk6jia fingerprint1-gang
ver 0.4.6  2023/06/11 kkossev      - child devices creation critical bug fix.
ver 0.5.0  2023/06/14 kkossev      - added trace logging; fixed healthStatus offline for TS0601 and Lonsonho 2nd gang; temporary disabled the initialize() command; changed _TZ3210_ngqk6jia to Lonsonho TS011E group; fixed TS0601 1st gang not working
ver 0.5.1  2023/06/15 kkossev      - added TS110E _TZ3210_3mpwqzuu 2 gang; fixed minLevel bug scaling; added RTT measurement in the ping command; added rxCtr, txCtr, switchCtr, leveCtr; _TZ3210_4ubylghk inClusters correction; TS110E_LONSONHO_DIMMER group model bug fix;
ver 0.5.2  2023/06/19 kkossev      - added digital/physical; checkDriverVersion fix; _TZ3210_ngqk6jia ping fix;
ver 0.6.0  2023/07/30 kkossev      - child devices ping(), toggle(), physical/digital, healthStatus offline bug fixes; added [refresh] event info;
ver 0.6.1  2023/08/23 kkossev      - bugfix: _TZE200_e3oitdyu model changed to TS0601; initialize button re-enabled (loads all defaults!); cmdTime state secured;
ver 0.6.2  2023/09/09 kkossev      - added TS0601 _TZE204_zenj4lxv Moes ZigBee 2-Gang Dimmer; _TZE204_1v1dxkck (3-gang); _TZE204_hlx9tnzb (1-gang); stopping dimmer countdowns;
ver 0.6.3  2023/10/15 kkossev      - setting ledMode, powerOnMode lightType for TS0601 dimmers
ver 0.6.4  2024/07/22 hhorigian    - added new version of:  TS110E _TZ3210_pagajpog 2 gang Dimmer, with  Application id= (44). New inClusters:0005,0004,0006,0008,E001,1000,0003,0000
ver 0.7.0  2024/07/24 kkossev      - TS0601 _TZE200_r32ctezx moved to new TS0601_LERLINK_FAN group; resolved merge conflicts;
ver 0.7.1  2024/07/29 hhorigian    - added new version of:  TS110E _TZ3210_tkkb1ym8 2 gang Dimmer
ver 0.7.2  2024/09/02 kkossev      - _TZE200_1agwnems MG-ZD01W 1-Gang Dimmer - inClusters fingerprint correction;
ver 0.7.3  2024/09/27 kkossev      - ignoring the min and max brightness for OzSmartThings (_TZE200_1agwnems) dimmer; added TS0601 _TZE204_o9gyszw2 Avatto ZigBee 2-Gang Dimmer; TS0601 _TZE200_p0gzbqct; TS0601 _TZE204_vevc4c6g; TS110E _TZ3210_wdexaypg
ver 0.7.4  2025/01/04 kkossev      - (dev.branch) added TS0601 _TZE204_bxoo2swd @djh_wolf; changing  TS0601 _TZE200_r32ctezx fan controller level scaling and ENUM command type @jockeboling
ver 0.7.5  2025/01/26 kkossev      - (dev.branch) added TS0601 _TZE204_hwyydvqm TR-TRON Zigbee 3-Gang Switch Dimmer @Andre
ver 0.7.6  2025/02/12 hhorigian    - (dev.branch) added TS0601 _TZE204_jtbgusdc 2 dimmer Avatto @Gabriel

*
*                                   TODO: switch type configuration settings for _TZ3210_wdexaypg 'TS110E_LONSONHO_DIMMER'
*                                   TODO: LED configuration settings
*                                   TODO: Lonsonho _TZ3210_4ubylghk : bulb type :  https://github.com/zigpy/zha-device-handlers/issues/1415#issuecomment-1062843118
*                                   TODO: Lonsonho _TZ3210_pagajpog : when momentarily push switch 1. It is like it doesn't recognize it as pressing the switch, but pressing it again can cause it to go into pairing mode. @user3633
*                                   TODO: Girier _TZ3210_3mpwqzuu: physical switch not reflected in the driver @user5386
*                                   TODO: Girier _TZ3210_3mpwqzuu: Every change in intensity through the dashboard, the lamp reaches the desired level and then lowers intensity until it is turned off, (for both channels) @user5386
*                                   TODO: TS0601 3 gangs - check whether toggle() is working for the 2nd and the 3rd gang
*                                   TODO: Enable the Initialize() button w/  Yes/No selection
*                                   TODO: TS110E_GIRIER_DIMMER TS011E power_on_behavior_1, TS110E_switch_type ['toggle', 'state', 'momentary']) (TS110E_options - needsMagic())
*                                   TODO: Tuya Fan Switch support
*                                   TODO: add TS110E 'light_type', 'switch_type'
*                                   TODO: add startLevelChange/stopLevelChange (Gledopto)
*
*/

def version() { '0.7.5' }
def timeStamp() { '2025/01/26 8:11 AM' }

@Field static final Boolean _DEBUG = false

import groovy.transform.Field
import hubitat.device.HubAction
import hubitat.device.Protocol
import com.hubitat.app.DeviceWrapper
import com.hubitat.app.ChildDeviceWrapper
import com.hubitat.app.ParentDeviceWrapper

metadata {
    definition (
        name: "Tuya Zigbee dimmer module",
        namespace: "matthammonddotorg",
        author: "Matt Hammond",
        description: "Driver for Tuya zigbee dimmer modules",
        documentationLink: "https://github.com/matt-hammond-001/hubitat-code/blob/master/drivers/tuya-zigbee-dimmer-module.README.md",
        importUrl: "https://raw.githubusercontent.com/kkossev/hubitat-matt-hammond-fork/development/drivers/tuya-zigbee-dimmer-module.groovy"
    ) {
        
        capability "Configuration"
        capability "Refresh"
        capability "Light"
        capability "Switch"
        capability "SwitchLevel"
        capability 'Health Check'
        
        attribute 'healthStatus', 'enum', [ 'unknown', 'offline', 'online' ]
        attribute "rtt", "number" 
        attribute 'ledMode',     'enum', TS0601LEDOptions.options.values() as List<String>
        attribute 'powerOnMode', 'enum', TS0601PowerOnOptions.options.values() as List<String>
        attribute 'lightType',   'enum', TS110ELightTypeOptions.options.values() as List<String>

        command "toggle"
        command "initialize", [[name: "Initialize the sensor after switching drivers.  \n\r   ***** Will load device default values! *****" ]]
        
        if (_DEBUG == true) {
            command "init"
            command "tuyaTest", [
                [name:"dpCommand", type: "STRING", description: "Tuya DP Command", constraints: ["STRING"]],
                [name:"dpValue",   type: "STRING", description: "Tuya DP value", constraints: ["STRING"]],
                [name:"dpType",    type: "ENUM",   constraints: ["DP_TYPE_VALUE", "DP_TYPE_BOOL", "DP_TYPE_ENUM"], description: "DP data type"] 
            ]
            command "testRefresh", [[name: "see the live logs" ]]
            command "testParse", [[name: "test", type: "STRING", description: "test", constraints: ["STRING"]]]
            command "testX", [[name: "testX", type: "STRING", description: "testX", constraints: ["STRING"]]]
        }
        
        modelConfigs.each{ data ->
            fingerprint profileId: "0104",
                inClusters: data.value.inClusters,
                outClusters:"0019,000A",
                model: data.value.model,
                manufacturer: data.key,
                deviceJoinName: data.value.joinName
        }
    }
    
    preferences {
        input "infoEnable", "bool", title: "<b>Enable info logging</b>", required: false, defaultValue: true
        input "debugEnable", "bool", title: "<b>Enable debug logging</b>", required: false, defaultValue: DEFAULT_DEBUG_OPT
        input "autoOn", "bool", title: "<b>Turn on when level adjusted</b>", description: "<i>Switch turns on automatically when dimmer level is adjusted.</i>", required: true, multiple: false, defaultValue: true
        input "autoRefresh", "bool", title: "<b>Auto refresh when level adjusted</b>", description: "<i>Automatically send an Refresh command when dimmer level is adjusted.</i>", required: true, multiple: false, defaultValue: false
        
        input "minLevel", "number", title: "<b>Minimum level</b>", description: "<i>Minimum brightness level (%). 0% on the dimmer level is mapped to this.</i>", required: true, multiple: false, defaultValue: DEFAULT_MIN_LEVEL
            if (minLevel < 0) { minLevel = 0 } else if (minLevel > 99) { minLevel = 99 }

        input "maxLevel", "number", title: "<b>Maximum level</b>", description: "<i>Maximum brightness level (%). 100% on the dimmer level is mapped to this.</i>", required: true, multiple: false, defaultValue: DEFAULT_MAX_LEVEL
        if (maxLevel < minLevel) { maxLevel = DEFAULT_MAX_LEVEL } else if (maxLevel > DEFAULT_MAX_LEVEL) { maxLevel = DEFAULT_MAX_LEVEL }

         /*   
        if (isLonsonho()) {
            input name: 'lightType', type: 'enum', title: '<b>Light Type</b>', options: TS110ELightTypeOptions.options, description: '<i>Configures the lights type.</i>'
            input name: 'switchType', type: 'enum', title: '<b>Switch Type</b>', options: TS110ESwitchTypeOptions.options, defaultValue: TS110ESwitchTypeOptions.defaultValue, description: '<i>Configures the switch type.</i>'
        }
        */    
        if (isTS0601())  {
            input name: 'ledMode', type: 'enum', title: '<b>LED mode</b>', options: TS0601LEDOptions.options, description: '<i>Configures the LED mode.</i>'
            input name: 'powerOnMode', type: 'enum', title: '<b>Power-On mode</b>', options: TS0601PowerOnOptions.options, description: '<i>Configures the Power-On mode.</i>'
            input name: 'lightType', type: 'enum', title: '<b>Light type</b>', options: TS110ELightTypeOptions.options, description: '<i>Configures the light type.</i>'
        }

        input (name: "advancedOptions", type: "bool", title: "Advanced Options", description: "<i> </i>", defaultValue: false)
        if (advancedOptions == true) {
            input "traceEnable", "bool", title: "<b>Enable trace logging</b>", description: "<i>Even more detailed logging. Toggle it on if asked by the developer...</i>", required: false, defaultValue: false
            input (name: "forcedProfile", type: "enum", title: "<b>Device Profile</b>", description: "<i>Forcely change the Device Profile, if the model/manufacturer was not recognized automatically.<br>Warning! Manually setting a device profile may not always work!</i>", 
                   options: getDeviceProfilesMap() /*getDeviceProfiles()*/)
            input name: 'healthCheckInterval', type: 'enum', title: '<b>Healthcheck Interval</b>', options: HealthcheckIntervalOpts.options, defaultValue: HealthcheckIntervalOpts.defaultValue, required: true, description:\
                '<i>Changes how often the hub pings the bulb to check health.</i>'
        }
    }
}

@Field static final Boolean DEFAULT_DEBUG_OPT = true
@Field static final int COMMAND_TIMEOUT = 10                 // Command timeout before setting healthState to offline
@Field static final Integer MAX_PING_MILISECONDS = 10000     // rtt more than 10 seconds will be ignored
@Field static final Integer REFRESH_TIMER = 5000             // refresh time in miliseconds
@Field static final String UNKNOWN =  'UNKNOWN'
@Field static final int DEFAULT_MIN_LEVEL = 0
@Field static final int DEFAULT_MAX_LEVEL = 100
@Field static final int TS110E_LONSONHO_LEVEL_ATTR = 0xF000        // (61440) 
@Field static final int TS110E_LONSONHO_BULB_TYPE_ATTR = 0xFC02
@Field static final int TS110E_LONSONHO_MIN_LEVEL_ATTR = 0xFC03
@Field static final int TS110E_LONSONHO_MAX_LEVEL_ATTR = 0xFC04
@Field static final int TS110E_LONSONHO_CUSTOM_LEVEL_CMD = 0x00F0

boolean isOzSmartThings() { device.getDataValue("manufacturer") == "_TZE200_1agwnems" }

@Field static Map HealthcheckIntervalOpts = [
    defaultValue: 10,
    options: [ 10: 'Every 10 Mins', 15: 'Every 15 Mins', 30: 'Every 30 Mins', 45: 'Every 45 Mins', '59': 'Every Hour', '00': 'Disabled' ]
]
@Field static final Map TS110ESwitchTypeOptions = [           // 0xFC00 (64512)
    defaultValue: 0,
    options     : [0: 'momentary', 1: 'toggle', 2: 'state']
]
@Field static final Map TS110ELightTypeOptions = [            // 0xFC02 (64514), type: 0x20
    defaultValue: 0,
    options     : [0: 'LED', 1: 'incandescent', 2: 'halogen']
]

@Field static final Map TS0601LEDOptions = [            // ledMode - Moes Dimmers
    defaultValue: 0,
    options     : [0: 'always off', 1: 'lit when on', 2: 'lit when off']
]
@Field static final Map TS0601PowerOnOptions = [            // ledMode - Moes Dimmers
    defaultValue: 0,
    options     : [0: 'off', 1: 'on', 2: 'last state']
]


@Field static final int TS110E_MIN_BRIGHTNESS = 0xFC03        // (64515)       // TODO: 0, 1000 -> 1, 255,  type: 0x21 Check !!!   
@Field static final int TS110E_MAX_BRIGHTNESS = 0xFC04        // (64516)       // TODO: 0, 1000 -> 1, 255,  type: 0x21 -  Check !!!   
// Girier TS110E may not support power-on-behavour?  https://github.com/Koenkk/zigbee2mqtt/issues/15902#issuecomment-1382848150     https://github.com/Koenkk/zigbee2mqtt/issues/16804 


@Field static def modelConfigs = [
    "_TYZB01_v8gtiaed": [ numEps: 2, model: "TS110F", inClusters: "0000,0004,0005,0006,0008",     joinName: "Tuya Zigbee 2-Gang Dimmer module" ],                // '2 gang smart dimmer switch module with neutral'
    "_TYZB01_qezuin6k": [ numEps: 1, model: "TS110F", inClusters: "0000,0004,0005,0006,0008",     joinName: "Tuya Zigbee 1-Gang Dimmer module" ],                // '1 gang smart dimmer switch module with neutral'
    "_TZ3000_ktuoyvt5": [ numEps: 1, model: "TS110F", inClusters: "0000,0004,0005,0006,0008",     joinName: "Tuya Zigbee 1-Gang Switch module" ],                // '1 gang smart        switch module without neutral'
    "_TZ3000_92chsky7": [ numEps: 2, model: "TS110F", inClusters: "0000,0004,0005,0006,0008",     joinName: "Tuya Zigbee 2-Gang Dimmer module (no-neutral)" ],   // '2 gang smart dimmer switch module without neutral'
    "_TZ3000_7ysdnebc": [ numEps: 2, model: "TS110F", inClusters: "0000,0004,0005,0003,0006,0008",joinName: "Tuya 2CH Zigbee dimmer module" ],
    "_TZE200_vm1gyrso": [ numEps: 3, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Tuya Zigbee 3-Gang Dimmer module" ],    
    "_TZE200_whpb9yts": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Tuya Zigbee 1-Gang Dimmer module" ],                 // 'Zigbee smart dimmer' Larkkey
    "_TZE200_ebwgzdqq": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Tuya Zigbee 1-Gang Dimmer module" ],                 // Larkkey
    "_TZE200_9i9dt8is": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Tuya Zigbee 1-Gang Dimmer module" ],                 // Earda/Tuya (dimmer) EDM-1ZAA-EU 
    "_TZE200_dfxkcots": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Tuya Zigbee 1-Gang Dimmer module" ],                 // Earda/Tuya (dimmer) EDM-1ZAA-EU 
    "gq8b1uv":          [ numEps: 1, model: "gq8b1uv", inClusters: "0000,0004,0005,0006,0008",    joinName: "TUYATEC Zigbee smart dimmer" ],                      //  TUYATEC Zigbee smart dimmer
//    "_TZ3210_ngqk6jia": [ numEps: 2, model: "TS110E", inClusters: "0005,0004,0006,0008,EF00,0000", joinName: "Lonsonho 2-gang Dimmer module"],                  // https://www.aliexpress.com/item/4001279149071.html
    "_TZ3210_zxbtub8r": [ numEps: 1, model: "TS110E", inClusters: "0004,0005,0003,0006,0008,EF00,0000", joinName: "GIRIER Dimmer module 1 ch."],                  // not tested
    "_TZ3210_lfbz816s": [ numEps: 1, model: "TS110F", inClusters: "0004,0005,0003,0006,0008,EF00,0000", joinName: "Fantem Dimmer module 1 ch."],                  // not tested     Model ZB006-X manufactured by Fantem
    "_TZ3210_ebbfkvoy": [ numEps: 1, model: "TS110F", inClusters: "0004,0005,0003,0006,0008,EF00,0000", joinName: "Fantem Dimmer module 1 ch."],                  // not tested     Model ZB006-X manufactured by Fantem
    "_TZE200_w4cryh2i": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Moes Zigbee Rotary/Touch Light Dimmer" ],            // https://community.hubitat.com/t/moes-zigbee-dimmer-touch/101195 
    "_TZE200_ip2akl4w": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Moes Zigbee 1-Gang Dimmer module" ],                 // https://community.hubitat.com/t/tuya-moes-1-2-3-gang-dimmer/104596/5?u=kkossev 
    "_TZE200_1agwnems": [ numEps: 1, model: "TS0601", inClusters: "0000,0004,0005,EF00",          joinName: "MG-ZD01W 1-Gang Dimmer" ],                           // https://community.hubitat.com/t/zigbee-dimmer-pairing-issues/142460/5?u=kkossev  OzSmartThings
    "_TZE200_la2c2uo9": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Moes Zigbee 1-Gang Dimmer module" ],                 // not tested
    "_TZE200_579lguh2": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Moes Zigbee 1-Gang Dimmer module" ],                 // not tested
    "_TZE200_fjjbhx9d": [ numEps: 2, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Moes Zigbee 2-Gang Dimmer module" ],                 // https://community.hubitat.com/t/tuya-moes-1-2-3-gang-dimmer/104596/5?u=kkossev 
    "_TZE200_drs6j6m5": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Lifud Model LF-AAZ030-0750-42" ],                    // https://community.hubitat.com/t/tuya-moes-1-2-3-gang-dimmer/104596/25?u=kkossev
    "_TZE200_fvldku9h": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Tuya Fan Switch" ] ,                                 // https://www.aliexpress.com/item/4001242513879.html
    "_TZE200_r32ctezx": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "Lerlink T2-Z67/T2-W67 Fan Switch" ],                 // https://github.com/isakharov/zigbee-herdsman-converters/blob/0bffc49faf69244711677a5735f54d8dd8ea8f7c/devices/tuya.js#L4117-L4143                               // https://www.aliexpress.us/item/3256804518783061.html https://github.com/Koenkk/zigbee2mqtt/issues/12793
    "_TZE200_3p5ydos3": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000",          joinName: "BSEED Zigbee Dimmer" ],                              // https://www.bseed.com/collections/zigbee-series/products/bseed-eu-russia-new-zigbee-touch-wifi-light-dimmer-smart-switch
    "_TZE200_e3oitdyu": [ numEps: 2, model: "TS0601", inClusters: "0000,0004,0005,EF00",          joinName: "Moes ZigBee Dimmer Switch 2CH"],                     // https://community.hubitat.com/t/moes-dimmer-module-2ch/110512 
    "_TZ3210_k1msuvg6": [ numEps: 1, model: "TS110E", inClusters: "0004,0005,0003,0006,0008,EF00,0000", joinName: "Girier Zigbee 1-Gang Dimmer module"],          // https://community.hubitat.com/t/girier-tuya-zigbee-3-0-light-switch-module-smart-diy-breaker-1-2-3-4-gang-supports-2-way-control/104546/36?u=kkossev
    "GLEDOPTO":         [ numEps: 1, model: "GL-SD-001", inClusters: "0000,0003,0004,0005,0006,0008,1000", joinName: "Gledopto Triac Dimmer"],                    //
    "_TZ3210_pagajpog": [ numEps: 2, model: "TS110E", inClusters: "0005,0004,0006,0008,E001,0000", joinName: "Lonsonho Tuya Smart Zigbee Dimmer"],                // https://community.hubitat.com/t/release-tuya-lonsonho-1-gang-and-2-gang-zigbee-dimmer-module-driver/60372/76?u=kkossev
    "_TZ3210_4ubylghk": [ numEps: 2, model: "TS110E", inClusters: "0004,0005,0006,0008,0300,EF00,0000", joinName: "Lonsonho Tuya Smart Zigbee Dimmer"],           // https://community.hubitat.com/t/driver-support-for-tuya-dimmer-module-model-ts110e-manufacturer-tz3210-4ubylghk/116077?u=kkossev
    "_TZ3210_ngqk6jia": [ numEps: 1, model: "TS110E", inClusters: "0003,0005,0004,0006,0008,E001,1000,0000", joinName: "Lonsonho Smart Zigbee Dimmer"],           // KK
    "_TZ3210_tkkb1ym8": [ numEps: 2, model: "TS110E", inClusters: "0004,0005,0003,0006,0008,EF00,0000", joinName: "Smart Zigbee Dimmer 2-gang"],                  // QA - Vartan - BR
    "_TZ3210_3mpwqzuu": [ numEps: 2, model: "TS110E", inClusters: "0005,0004,0006,0008,E001,0000", joinName: "Tuya Smart Zigbee Dimmer"],                         // https://community.hubitat.com/t/driver-support-for-tuya-dimmer-module-model-ts110e-manufacturer-tz3210-4ubylghk/116077/26?u=kkossev
    "_TZE204_hlx9tnzb": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000", joinName: "Moes Zigbee 1-Gang Switch Dimmer"],                           // 
    "_TZE204_zenj4lxv": [ numEps: 2, model: "TS0601", inClusters: "0004,0005,EF00,0000", joinName: "Moes Zigbee 2-Gang Switch Dimmer"],                           // https://www.aliexpress.com/item/1005005870551288.html 
    "_TZE204_1v1dxkck": [ numEps: 3, model: "TS0601", inClusters: "0004,0005,EF00,0000", joinName: "Moes Zigbee 3-Gang Switch Dimmer"],                           // 
    "_TZ3210_pagajpog": [ numEps: 2, model: "TS110E", inClusters: "0005,0004,0006,0008,E001,1000,0003,0000", joinName: "Smart Zigbee Dimmer 2-gang"],             // Vartan - BR
    "_TZE204_o9gyszw2": [ numEps: 2, model: "TS0601", inClusters: "0004,0005,EF00,0000", joinName: "Avatto Zigbee 2-Gang Switch Dimmer"],                         // https://s.click.aliexpress.com/e/_DdmDzJx https://community.hubitat.com/t/ts0601-toggle-not-working/143432?u=kkossev
    "_TZE204_jtbgusdc": [ numEps: 2, model: "TS0601", inClusters: "0004,0005,EF00,0000", joinName: "Avatto Zigbee 2-Gang Switch Dimmer"],                         // https://s.click.aliexpress.com/e/_DdmDzJx https://community.hubitat.com/t/ts0601-toggle-not-working/143432?u=kkossev
    "_TZE200_p0gzbqct": [ numEps: 1, model: "TS0601", inClusters: "0000,0004,0005,EF00", joinName: "Moes Zigbee Rotary Dimmer"],                                  // https://s.click.aliexpress.com/e/_DEZTclF https://community.hubitat.com/t/re-release-tuya-zigbee-dimmer-module-w-healthstatus/120180/16?u=kkossev
    "_TZE204_vevc4c6g": [ numEps: 1, model: "TS0601", inClusters: "0004,0005,EF00,0000", joinName: "Tuya Touch Dimmer"],                                          // https://www.rtx24.pl/sciemniacz-dotykowy-1obw-bi-zigbee-tuya-rtx-sh-p-766.html 
    "_TZ3210_wdexaypg": [ numEps: 2, model: "TS110E", inClusters: "0004,0005,0003,0006,0008,EF00", joinName: "Tuya Zigbee 2-Gang Dimmer"],                        // https://community.hubitat.com/t/driver-support-for-zigbee-model-ts110e-zigbee-manufacturer-tz3210-ngqk6jia/99817/17?u=kkossev
    "_TZE204_bxoo2swd": [ numEps: 2, model: "TS0601", inClusters: "0000,0004,0005,EF00",          joinName: "Moes zm-105b-m dimmer 2CH"],                         // https://community.hubitat.com/t/looking-for-small-zigbee-dimmer-module/131239/7?u=kkossev
    "_TZE204_hwyydvqm": [ numEps: 3, model: "TS0601", inClusters: "0000,0004,0005,EF00", joinName: "TR-TRON Zigbee 3-Gang Switch Dimmer"],                        // 
]

def getNumEps() {return config()?.numEps ?: 1}
def isParent()  {return getParent() == null }
def isChild()   {return getParent() != null }
def config() { return modelConfigs[device.getDataValue("manufacturer")] }

@Field static final Map deviceProfilesV2 = [
    "TS110F_DIMMER"  : [
            description   : "TS110F Tuya Dimmers",
            models        : ["TS110F"],
            fingerprints  : [
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,0006,0008", outClusters:"0019,000A", model:"TS110F", manufacturer:"_TYZB01_v8gtiaed", deviceJoinName: "Tuya Zigbee 2-Gang Dimmer module"],            // '2 gang smart dimmer switch module with neutral'
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,0006,0008", outClusters:"0019,000A", model:"TS110F", manufacturer:"_TYZB01_qezuin6k", deviceJoinName: "Tuya Zigbee 1-Gang Dimmer module"],           // '1 gang smart dimmer switch module with neutral'
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,0006,0008", outClusters:"0019,000A", model:"TS110F", manufacturer:"_TZ3000_ktuoyvt5", deviceJoinName: "Tuya Zigbee 1-Gang Dimmer module"],            // '1 gang smart        switch module without neutral'
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,0006,0008", outClusters:"0019,000A", model:"TS110F", manufacturer:"_TZ3000_92chsky7", deviceJoinName: "Tuya Zigbee 2-Gang Dimmer module (no-neutral)"], // '2 gang smart dimmer switch module without neutral' - Lonsonho ???
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,0003,0006,0008", outClusters:"0019,000A", model:"TS110F", manufacturer:"_TZ3000_7ysdnebc", deviceJoinName: "Tuya 2CH Zigbee dimmer module"]       
            ],
            deviceJoinName: "TS110F Tuya Dimmer",
            capabilities  : ["SwitchLevel": true],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],

    "TS110E_DIMMER"  : [
            description   : "TS110E Tuya Dimmers",
            models        : ["TS110E"],
            fingerprints  : [
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0005,0004,0006,0008,EF00,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_3mpwqzuu", deviceJoinName: "Tuya 2-gang Dimmer module"]             // https://community.hubitat.com/t/driver-support-for-tuya-dimmer-module-model-ts110e-manufacturer-tz3210-4ubylghk/116077/26?u=kkossev
            ],
            deviceJoinName: "TS110E Tuya Dimmer",
            capabilities  : ["SwitchLevel": true],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],
    
    "TS110E_GIRIER_DIMMER"  : [
            description   : "TS110E Girier Dimmers",
            models        : ["TS110F"],
            fingerprints  : [
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,0003,0006,0008,EF00,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_zxbtub8r", deviceJoinName: "GIRIER Dimmer module 1 ch."],         // not tested
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,0003,0006,0008,EF00,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_k1msuvg6", deviceJoinName: "Girier Zigbee 1-Gang Dimmer module"], // https://community.hubitat.com/t/girier-tuya-zigbee-3-0-light-switch-module-smart-diy-breaker-1-2-3-4-gang-supports-2-way-control/104546/36?u=kkossev
            ],
            deviceJoinName: "TS110E Girier Dimmer",
            capabilities  : ["SwitchLevel": true],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],
    
    "TS110E_LONSONHO_DIMMER"  : [    // uses 0xF000 (cluster 8) for brightness control // TODO: 0, 1000 -> 1, 254 (don't go to 255!!) // (uint16) # 0xF000 reported values are 10-1000, must be converted to 0-254  => value = (value + 4 - 10) * 254 // (1000 - 10) 
            description   : "TS110E Lonsonho Dimmers",        // https://github.com/zigpy/zha-device-handlers/blob/5bbe4e0c668d826baeed178e4085b98d2a5d1740/zhaquirks/tuya/ts110e.py
            models        : ["TS110E"],
            fingerprints  : [
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0005,0004,0006,0008,E001,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_pagajpog", deviceJoinName: "Lonsonho Tuya Smart Zigbee Dimmer"],        // https://community.hubitat.com/t/release-tuya-lonsonho-1-gang-and-2-gang-zigbee-dimmer-module-driver/60372/76?u=kkossev
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0005,0004,0006,0008,E001,1000,0003,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_pagajpog", deviceJoinName: "Smart Zigbee Dimmer 2-gang"],     // Vartan - BR 
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,0003,0006,0008,EF00,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_tkkb1ym8", deviceJoinName: "Smart Zigbee Dimmer 2-gang"],       	  // Vartan - BR - QA
		        [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,0006,0008,0300,EF00,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_4ubylghk", deviceJoinName: "Lonsonho Tuya Smart Zigbee Dimmer"],   // https://community.hubitat.com/t/driver-support-for-tuya-dimmer-module-model-ts110e-manufacturer-tz3210-4ubylghk/116077?u=kkossev
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0003,0005,0004,0006,0008,E001,1000,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_ngqk6jia",joinName: "Lonsonho Smart Zigbee Dimmer"],          // KK
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,0003,0006,0008,EF00,0000", outClusters:"0019,000A", model:"TS110E", manufacturer:"_TZ3210_wdexaypg", deviceJoinName: "Smart Zigbee Dimmer 2-gang"],       	  // https://community.hubitat.com/t/driver-support-for-zigbee-model-ts110e-zigbee-manufacturer-tz3210-ngqk6jia/99817/17?u=kkossev
            ],
            deviceJoinName: "TS110E Lonsonho Dimmer",
            capabilities  : ["SwitchLevel": true],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],
    
    "TS0601_DIMMER"  : [
            description   : "TS0601 Tuya Dimmers",
            models        : ["TS0601"],
            fingerprints  : [
                [numEps: 3, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_vm1gyrso", deviceJoinName: "Tuya Zigbee 3-Gang Dimmer module"],
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_whpb9yts", deviceJoinName: "Tuya Zigbee 1-Gang Dimmer module"],        // 'Zigbee smart dimmer'
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_ebwgzdqq", deviceJoinName: "Tuya Zigbee 1-Gang Dimmer module"],
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_9i9dt8is", deviceJoinName: "Tuya Zigbee 1-Gang Dimmer module"],
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_dfxkcots", deviceJoinName: "Tuya Zigbee 1-Gang Dimmer module"],
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_w4cryh2i", deviceJoinName: "Moes Zigbee Rotary/Touch Light Dimmer"],   // https://community.hubitat.com/t/moes-zigbee-dimmer-touch/101195 
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_ip2akl4w", deviceJoinName: "Moes Zigbee 1-Gang Dimmer module"],        // https://community.hubitat.com/t/tuya-moes-1-2-3-gang-dimmer/104596/5?u=kkossev 
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_1agwnems", deviceJoinName: "MG-ZD01W 1-Gang Dimmer"],                  // https://community.hubitat.com/t/zigbee-dimmer-pairing-issues/142460/5?u=kkossev OzSmartThings
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_la2c2uo9", deviceJoinName: "Moes Zigbee 1-Gang Dimmer module"],        // not tested
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_579lguh2", deviceJoinName: "Moes Zigbee 1-Gang Dimmer module"],        // not tested
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_fjjbhx9d", deviceJoinName: "Moes Zigbee 2-Gang Dimmer module"],        // https://community.hubitat.com/t/tuya-moes-1-2-3-gang-dimmer/104596/5?u=kkossev 
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_drs6j6m5", deviceJoinName: "Lifud Model LF-AAZ030-0750-42"],           // https://community.hubitat.com/t/tuya-moes-1-2-3-gang-dimmer/104596/25?u=kkossev
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_3p5ydos3", deviceJoinName: "BSEED Zigbee 1-Gang Dimmer module"],       // not tested
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_e3oitdyu", deviceJoinName: "Moes ZigBee Dimmer Switch 2CH"],           // https://community.hubitat.com/t/moes-dimmer-module-2ch/110512 
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_hlx9tnzb", deviceJoinName: "Moes ZigBee 1-Gang Dimmer"],               // https://github.com/Koenkk/zigbee-herdsman-converters/blob/23bee5ddfca1f62fc0a03d40fbf788b0b7fe2fc3/src/devices/tuya.ts#L1265
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_zenj4lxv", deviceJoinName: "Moes ZigBee 2-Gang Dimmer"],               // https://community.hubitat.com/t/moes-dimmer-module-2ch/110512 
                [numEps: 3, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_1v1dxkck", deviceJoinName: "Moes ZigBee 3-Gang Dimmer"],               // https://github.com/Koenkk/zigbee-herdsman-converters/blob/23bee5ddfca1f62fc0a03d40fbf788b0b7fe2fc3/src/devices/tuya.ts#L1265
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_w4cryh2i", deviceJoinName: "Moes Zigbee Rotary/Touch Light Dimmer"],   // https://community.hubitat.com/t/re-release-beta-tuya-zigbee-dimmer-module-w-healthstatus/120180/16?u=kkossev
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_o9gyszw2", deviceJoinName: "Avatto ZigBee 2-Gang Dimmer"],             // https://github.com/Koenkk/zigbee2mqtt/issues/22175#issuecomment-2053713724 https://github.com/Koenkk/zigbee2mqtt/issues/22175
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_jtbgusdc", deviceJoinName: "Avatto ZigBee 2-Gang Dimmer"],             // https://github.com/Koenkk/zigbee2mqtt/issues/22175#issuecomment-2053713724 https://github.com/Koenkk/zigbee2mqtt/issues/22175
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_p0gzbqct", deviceJoinName: "Moes Zigbee Rotary Dimmer"],               // 
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_vevc4c6g", deviceJoinName: "Tuya touch dimmer"],                       //  https://github.com/Koenkk/zigbee2mqtt/issues/21980
                [numEps: 2, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_bxoo2swd", deviceJoinName: "Moes zm-105b-m dimmer 2CH"],               // https://community.hubitat.com/t/moes-dimmer-module-2ch/110512 
                [numEps: 3, profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE204_hwyydvqm", deviceJoinName: "TR-TRON Zigbee 3-Gang Switch Dimmer"],     // @Andre Gaspar
            ],
            deviceJoinName: "TS0601 Tuya Dimmer",
            capabilities  : ["SwitchLevel": true],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],
    
    "TS0601_FAN"  : [
            description   : "TS0601 Fan Switch",
            models        : ["TS0601"],
            fingerprints  : [
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_fvldku9h", deviceJoinName: "Tuya Fan Switch"]                         // https://www.aliexpress.com/item/4001242513879.html
            ],
            deviceJoinName: "TS0601 Fan Switch",
            capabilities  : ["SwitchLevel": false],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],

    "TS0601_LERLINK_FAN"  : [
            description   : "TS0601 LERLINK Fan Switch",
            models        : ["TS0601"],
            fingerprints  : [
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_r32ctezx", deviceJoinName: "Lerlink T2-Z67/T2-W67 Fan Switch"]                         // https://www.aliexpress.us/item/3256804518783061.html https://github.com/Koenkk/zigbee2mqtt/issues/12793 https://github.com/isakharov/zigbee-herdsman-converters/blob/0bffc49faf69244711677a5735f54d8dd8ea8f7c/devices/tuya.js#L4117-L4143 
            ],
            deviceJoinName: "TS0601 LERLINK Fan Switch",
            capabilities  : ["SwitchLevel": false],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],
    
    "TS0505B_TUYA_BULB"  : [
            description   : "TS0505B Tuya Bulb",
            models        : ["TS0601"],
            fingerprints  : [
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0003,0004,0005,0006,1000,0008,0300,EF00,0000", outClusters:"0019,000A", model:"TS0505B", manufacturer:"_TZ3210_wxa85bwk", deviceJoinName: "Tuya Bulb"],       // KK
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0003,0004,0005,0006,1000,0008,0300,EF00,0000", outClusters:"0019,000A", model:"TS0505B", manufacturer:"_TZ3210_r5afgmkl", deviceJoinName: "Tuya Bulb"],       // https://community.hubitat.com/t/tuya-zigbee-bulb/115563?u=kkossev
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0003,0004,0005,0006,1000,0008,0300,EF00,0000", outClusters:"0019,000A", model:"TS0505B", manufacturer:"_TZ3210_eejm8dcr", deviceJoinName: "Tuya LED Strip"],  // https://community.hubitat.com/t/c8-gledopto-light-strip-controllers/114775/9?u=kkossev
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0003,0004,0005,0006,1000,0008,0300,EF00,0000", outClusters:"0019,000A", model:"TS0505B", manufacturer:"_TZ3000_qqjaziws", deviceJoinName: "Tuya LED Strip"],  // https://community.hubitat.com/t/anyone-used-this-tuya-led-strip-controller-with-success/55593/21?u=kkossev
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,1000,0008,0300,EF00", outClusters:"0019,000A", model:"TS0505B", manufacturer:"_TZ3210_zexrfbzd", deviceJoinName: "Tuya Bulb"],       // https://community.hubitat.com/t/zigbee-bulb-paired-as-device/107530/3?u=kkossev
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,1000,0008,0300,EF00", outClusters:"0019,000A", model:"TS0505B", manufacturer:"_TZ3000_cmaky9gq", deviceJoinName: "Ikuu LED Strip"]   // https://community.hubitat.com/t/mercator-ikuu/70404/191?u=kkossev
            ],
            deviceJoinName: "Tuya Bulb",
            capabilities  : ["SwitchLevel": true],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ],
    
    "OTHER_OEM_DIMMER"  : [
            description   : "Other OEM Dimmer",
            models        : ["gq8b1uv", "GL-SD-001"],
            fingerprints  : [
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"gq8b1uv", manufacturer:"gq8b1uv", deviceJoinName: "TUYATEC Zigbee smart dimmer"],                         // https://www.aliexpress.com/item/4001242513879.html
                [numEps: 1, profileId:"0104", endpointId:"01", inClusters:"0004,0005,EF00,0000", outClusters:"0019,000A", model:"GL-SD-001", manufacturer:"GLEDOPTO", deviceJoinName: "Gledopto Triac Dimmer"]                          // https://www.aliexpress.us/item/3256804518783061.html https://github.com/Koenkk/zigbee2mqtt/issues/12793
            ],
            deviceJoinName: "Other OEM Dimmer",
            capabilities  : ["SwitchLevel": false],
            attributes    : ["healthStatus": "unknown", "powerSource": "mains"],
            configuration : [],
            preferences   : []
    ]    
]
def getDW()                { return isParent() ? this : getParent() }
def getModelGroup()        { return getDW().state.deviceProfile ?:"UNKNOWN" }
def getDeviceProfilesMap() {deviceProfilesV2.values().description as List<String>}
def isTS0601()             { return getDW().getModelGroup().contains("TS0601") }
def isFanController()      { return getDW().getModelGroup().contains("TS0601_FAN") }
def isLerlinkFanController() { return getDW().getModelGroup().contains("TS0601_LERLINK_FAN") }
def isTS110E()             { return getDW().getModelGroup().contains("TS110E_DIMMER") }
def isGirier()             { return getDW().getModelGroup().contains("TS110E_GIRIER_DIMMER") }
def isLonsonho()           { return getDW().getModelGroup().contains("TS110E_LONSONHO_DIMMER") }
def isTuyaBulb()           { return getDW().getModelGroup().contains("TS0505B_TUYA_BULB") }
def isUnknown()            { return getDW().getModelGroup().contains("UNKNOWN") }

void parse(String description) {
    checkDriverVersion()
    if (state.stats != null) state.stats['rxCtr'] = (state.stats['rxCtr'] ?: 0) + 1 else state.stats=[:]
    logDebug "parse: received raw description: ${description}"
    if (isParent()) {
        def descMap = [:]
        try {
            descMap = zigbee.parseDescriptionAsMap(description)
        }
        catch (e) {
            logWarn "exception ${e} caught while parsing description:  ${description}"
        }
        logTrace "parse: received descMap: ${descMap}"
        if (description.startsWith("catchall")) {
            logTrace "parse: catchall clusterId=${descMap?.clusterId} command=${descMap?.command} data=${descMap?.data}"
            if (descMap?.command == "0B") {
                logDebug "parse: catchall received confirmation from clusterId=${descMap?.clusterId} for command=${descMap?.command} data=${descMap?.data}"
                return
            }
            else if (descMap?.clusterId == "EF00") {
                parseTuyaCluster(descMap)
            }
            else if(descMap?.clusterId == "0000") {
                parseBasicCluster(descMap)
                descMap.remove('additionalAttrs')?.each { final Map map -> parseBasicCluster(descMap + map) }
            }
            else {
                logWarn "unprocessed catchall:  ${description}"
            }
            sendHealthStatusEventAll('online')
            unschedule('deviceCommandTimeout')            
            return
        }
        //
        Integer value = 0
        if (descMap?.value != null && descMap?.encoding != "42") {
            try {
                value = Integer.parseInt(descMap.value, 16)
            }
            catch (e) {
                logWarn "exception ${e} caught while converting ${descMap?.value} to integer, encoding is ${descMap?.encoding}"
            }
        }
        def child = this
        def isFirst = true
        if (descMap?.endpoint != null) {
            child = getChildByEndpointId(descMap.endpoint)
            isFirst = (endpointIdToIndex(descMap.endpoint) == 0)
        }
        //
        sendHealthStatusEventAll('online')
        unschedule('deviceCommandTimeout')
        //
        if (descMap.data != null && descMap.data?.size()>=3 && descMap.data[2] == "86" && descMap.command == "01") {
            logDebug "Read attribute response: unsupported Attributte ${descMap.data[1] + descMap.data[0]} cluster ${descMap.clusterId}"
            return
        }
        switch (descMap.clusterInt) {
            case 0x0000: // basic cluster
                parseBasicCluster( descMap )
                break
            case 0x0006: // switch state
                logTrace "parse: on/off cluster 0x0006 command ${descMap?.command} attribute ${descMap?.attrId} value ${value}"
                if (descMap?.command == "07" && descMap?.data.size() >= 1) {
                    logDebug "parse: received Configure Reporting Response for cluster:${descMap.clusterInt} attribute ${escMap?.attrId}, data=${descMap.data} (Status: ${descMap.data[0]=="00" ? 'Success' : '<b>Failure</b>'})"
                    break
                }
                if (descMap?.command == "0B" && descMap?.data.size() >= 2) {
                    String clusterCmd = descMap.data[0]
                    def status = descMap.data[1]
                    logTrace "parse: received ZCL Command Response for cluster ${descMap.clusterInt} command ${descMap?.command} attribute ${descMap?.attrId}, data=${descMap.data} (Status: ${descMap.data[1]=="00" ? 'Success' : '<b>Failure</b>'})"
                    break
                }
                if (descMap?.attrId == "0000") {
                    if (isGirier() && descMap?.command == "01") {
                        logDebug "parse: IGNORING command Response for cluster ${descMap.clusterInt} command ${descMap?.command} attribute ${escMap?.attrId}"
                    }
                    else {
                        logTrace "on/off  endpoint=${descMap?.endpoint} isFirst=${isFirst} this=${this} child=${child} value=${value}"
                        child?.onSwitchState(value)
                        if (isFirst && child != this) {
                            logTrace "parse: replicating switchState in parent"
                            onSwitchState(value)
                        } 
                        else {
                            logTrace "parse: isFirst=${isFirst} this=${this} child=${child} value=${value}"
                        }
                    }
                }
                else if (descMap?.attrId == "4001") {
                    logDebug "On Time is ${value}"
                }
                else if (descMap?.attrId == "4002") {
                    logDebug "Off Wait Time is ${value}"
                }
                else {
                    logDebug "parse: unsupported attribute ${descMap?.attrId} cluster ${descMap.clusterId} command ${descMap?.command}, data=${descMap.data}"
                }
                break
            case 0x0008: // switch level state
                if (descMap?.command == "07" && descMap?.data.size() >= 1) {
                    logDebug "parse: received Configure Reporting Response for cluster:${descMap.clusterId} , data=${descMap.data} (Status: ${descMap.data[0]=="00" ? 'Success' : '<b>Failure</b>'})"
                    break
                }
                if (descMap?.command == "0B" && descMap?.data.size() >= 2) {
                    String clusterCmd = descMap.data[0]
                    def status = descMap.data[1]
                    logDebug "parse: received ZCL Command Response for cluster ${descMap.clusterId} command ${descMap?.command}, data=${descMap.data} (Status: ${descMap.data[1]=="00" ? 'Success' : '<b>Failure</b>'})"
                    break
                }
                logDebug "parse: switch level cluster 0x0008 endpoint ${descMap?.endpoint} command ${descMap?.command} attrId ${descMap?.attrId} value raw ${value})"
                if (descMap?.attrId == "0000" || descMap?.attrId == "F000") {
                    if (isTuyaBulb() && descMap?.attrId == "0000") {
                        logDebug "parse: Tuya Bulb: child.onSwitchLevel value=${value} child=${child}"
                    }                    
                    else if (isLonsonho() && descMap?.attrId == "F000") {
                        value = (value / 10) as int
                    }
                    else if (descMap?.attrId == "0000") {
                        logDebug "parse: Tuya/Girier/OEM: child.onSwitchLevel value=${value} child=${child}"
                    }
                    else {
                        logDebug "parse: SKIPPING  attrId ${descMap?.attrId} value raw ${value} !"        // TODO !!
                        break
                    }
                    if (child != null) {
                        child?.onSwitchLevel((value) as int)
                    }
                    if (isFirst && child != this) {
                        logDebug "parse: replicating switchLevel in parent"
                        onSwitchLevel(value)
                    }
                } else if (descMap?.attrId == "000F") {
                    logInfo "Tuya options are (0x${descMap.value})"
                } else if (descMap?.attrId == "FC02") {
                    //value = hexStrToUnsignedInt(descMap.value)
                    logInfo "light type is '${TS110ELightTypeOptions.options[value]}' (0x${descMap.value})"
                    //device.updateSetting('lightType', [value: value.toString(), type: 'enum'])
                } else if (descMap?.attrId == "FC03") {
                    logInfo "minLevel is ${value} (0x${descMap.value})"
                    //device.updateSetting('minLevel', [value: value, type: 'number'])
                } else if (descMap?.attrId == "FC04") {
                    logInfo "maxLevel is ${value} (0x${descMap.value})"
                    //device.updateSetting('maxLevel', [value: value, type: 'number'])
                }
                else {
                    logDebug "parse: UNPROCESSED attrubute ${descMap?.attrId} switch level cluster 0x0008 command ${descMap?.command}  value raw: (${value})"
                }
                break
            case 0x8021: 
                logDebug "parse: received bind response, data=${descMap.data} (Sequence Number:${descMap.data[0]}, Status: ${descMap.data[1]=="00" ? 'Success' : '<b>Failure</b>'})"
                break
            default :
                logWarn "parse: UNPROCESSED endpoint=${descMap?.endpoint} cluster=${descMap?.cluster} command=${descMap?.command} attrInt = ${descMap?.attrInt} value= ${descMap?.value} data=${descMap?.data}"
                break
        }
    } 
    else {
        throw new Exception("parse() called incorrectly by child")
    }
}

/**
 * Zigbee Basic Cluster Parsing
 * @param descMap Zigbee message in parsed map format
 */
void parseBasicCluster(final Map descMap) {
    switch (descMap.attrInt as Integer) {
        case PING_ATTR_ID: // Using 0x01 read as a simple ping/pong mechanism
            logTrace "Tuya check-in message (attribute ${descMap.attrId} reported: ${descMap.value})"
            def now = new Date().getTime()
            if (this.state.lastTx == null) this.state.lastTx = [:]
            def timeRunning = now.toInteger() - (this.state.lastTx["pingTime"] ?: '0').toInteger()
            if (timeRunning < MAX_PING_MILISECONDS) {
                sendRttEvent()
            }
            else  {
                logDebug "Tuya check-in (0x01)"
            }
            break
        case FIRMWARE_VERSION_ID:
            final String version = descMap.value ?: 'unknown'
            log.info "device firmware version is ${version}"
            updateDataValue('softwareBuild', version)
            break
        default:
            logDebug "zigbee received unknown Basic cluster attribute 0x${descMap.attrId} (value ${descMap.value})"
            break
    }
}

@Field static final int FIRMWARE_VERSION_ID = 0x4000
@Field static final int PING_ATTR_ID = 0x01


/*
-----------------------------------------------------------------------------
Setting up child devices
-----------------------------------------------------------------------------
*/

def createChildDevices() {
    def numEps = getNumEps()
    
    if (numEps == 1) {
        numEps = 0
        logDebug "createChildDevices: single channel dimmer, no child devices are needed"
    } else {
        logDebug "createChildDevices: about to delete any existing child devices:  ${numEps} expected , found ${getChildDevices().size()}"
    }

    def index = getChildDevices().size()
    if (index == null || index == 0)   {
        logDebug "createChildDevices: no child devices to be deleted"
    }
    else {
        for (int i=0; i<index; i++) {
            def dni = indexToChildDni(i)
            if (dni != null) {
                logInfo "Deleting child ${i} with dni ${dni}"
                deleteChildDevice(dni)    
            }
            else {
                logWarn "createChildDevices: child device ${i} DNI was not found!"
            }
        }
    }
    if (numEps <= 1) {
        logDebug "createChildDevices: no child devices to be created"
        return
    }
    
    logDebug "createChildDevices: about to create ${numEps} child devices"   
    for (int i=0; i<numEps; i++) {
        // create child devices
        def dni = indexToChildDni(i)
        def endpointId = indexToEndpointId(i)
        logInfo "Creating child ${i} with dni ${dni}"
        addChildDevice(
            "Tuya Zigbee dimmer module",
            dni,
            [
                completedSetup: true,
                label: "${device.displayName} (CH${endpointId})",
                isComponent:true,
                componentName: "ch${endpointId}",
                componentLabel: "Channel ${endpointId}"
            ]
        )
    }
}


// called from initialized(); returns the bind+configuration commands or null
def listenChildDevices() {
    ArrayList<String> cmds = []

    if (isTS0601()) {
        return null
    }
    logTrace "listenChildDevices: getChildEndpointIds() = ${getChildEndpointIds()} size=${getChildEndpointIds().size()}"
    getChildEndpointIds().each{ endpointId ->
        //log.trace "endpointId = ${endpointId}"
        if (endpointId != null && endpointId != 0 && endpointId != 0xF2) {
            cmds += [
                //bindings
                "zdo bind 0x${device.deviceNetworkId} 0x${endpointId} 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x${endpointId} 0x01 0x0008 {${device.zigbeeId}} {}", "delay 200",
                //reporting
                "he cr 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 0 0x10 1 0xFFFE {}","delay 200",
                "he cr 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 0 0x20 1 0xFFFE {}", "delay 200",
            ] + cmdRefresh(endpointIdToChildDni(endpointId))
        }
    }
    logTrace "listenChildDevices: returning binding and reporting configuration commands: ${cmds}"
    return cmds
}

/*
-----------------------------------------------------------------------------
Propagate settings changes both ways between Parent and First Child
-----------------------------------------------------------------------------
*/

def onChildSettingsChange(childDni, childSettings) {
    def i = endpointIdToIndex(childDniToEndpointId(childDni))
    logInfo "updating settings for child device #${i+1} ... ${childSettings}"
    if (i==0) {
        device.updateSetting("minLevel",childSettings.minLevel)
        device.updateSetting("maxLevel",childSettings.maxLevel)
        device.updateSetting("autoOn",childSettings.autoOn)
    }
    else {
        logWarn "onChildSettingsChange: skipped onChildSettingsChange() for child device #${i+1}"
    }
}

def onParentSettingsChange(parentSettings) {
    logInfo "updating settings for the parent device ... ${parentSettings}"
    device.updateSetting("minLevel",parentSettings.minLevel)
    device.updateSetting("maxLevel",parentSettings.maxLevel)
    device.updateSetting("autoOn",parentSettings.autoOn)
    /*
    if (isTS0601()) {
        logWarn "Min/Max levels NOT changed on the parent device"
    }
*/
}
 
/*
-----------------------------------------------------------------------------
Command handlers - send Zigbee commands to the device

if child, then ask parent to act on its behalf
if parent, then act on endpoint 1
-----------------------------------------------------------------------------
*/

// sends Zigbee commands to refresh the switch and the level
def refresh() {
    getDW().scheduleCommandTimeoutCheck()
    if (state.states == null) state.states = [:]
    state.states["isRefresh"] = true    
    runInMillis( REFRESH_TIMER, clearRefreshRequest, [overwrite: true])                 // 3 seconds
    if (isParent()) {
        logDebug "refresh: parent ${indexToChildDni(0)}"
        ArrayList<String> cmds = cmdRefresh(indexToChildDni(0))
        sendZigbeeCommands(cmds)
    } else {
        logDebug "refresh: child ${device.deviceNetworkId}"
        parent?.doActions( parent?.cmdRefresh(device.deviceNetworkId) )
    }
}

// sends Zigbee commands to turn the switch on
def on() {
    getDW().scheduleCommandTimeoutCheck()
    setCmdTimeNow()
    if (isParent()) {
        sendZigbeeCommands(cmdSwitch(indexToChildDni(0), 1))
    } else {
        parent?.doActions( parent?.cmdSwitch(device.deviceNetworkId, 1) )
    }
}

// sends Zigbee commands to turn the switch off
def off() {
    setCmdTimeNow()
    getDW().scheduleCommandTimeoutCheck()
    if (isParent()) {
        sendZigbeeCommands(cmdSwitch(indexToChildDni(0), 0))
    } else {
        parent?.doActions( parent?.cmdSwitch(device.deviceNetworkId, 0) )
    }
}

// sends Zigbee commands to toggle the switch
def toggle() {
    setCmdTimeNow()
    getDW().scheduleCommandTimeoutCheck()
    logTrace "toggle: ... getParent()=${getParent()}"
    if (isParent()) {
        sendZigbeeCommands(cmdSwitchToggle(indexToChildDni(0)))
    } else {
        parent?.doActions( parent.cmdSwitchToggle(device.deviceNetworkId))
    }
}

// sends Zigbee commands to set level
def setLevel(level, duration=0) {
    setCmdTimeNow()
    getDW().scheduleCommandTimeoutCheck()
    if (settings.autoRefresh == true) {
        runIn(1, 'refresh')
    }
    if (isParent()) {
        def value = levelToValue(level)
        sendZigbeeCommands(cmdSetLevel(indexToChildDni(0), value, duration))
    } else {
        def value = levelToValue(level)
        parent?.doActions( parent?.cmdSetLevel(device.deviceNetworkId, value, duration) )
    }
}

// sends Zigbee commands to ping the device
def ping() {
    ArrayList<String> cmds
    getDW().scheduleCommandTimeoutCheck()
    // temporary disable it            getDW().scheduleCommandTimeoutCheck()
    if (state.lastTx == null) { state.lastTx = [:] }
    def now = new Date().getTime()
   
    logTrace "isParent() = ${isParent()}   isChild() = ${isChild()}"
    if (isParent())
    {
        // parent device
        state.lastTx["pingTime"] = now
        cmds =  ["he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0000 0x0001 {}", "delay 200",] 
        logDebug "ping parent: pingTime = ${state.lastTx}"
    }
    else {
        // child device
        ParentDeviceWrapper pp = getParent()
        state.lastTx["pingTime"] = now
        // set parent device states also
        parent.setPingTime(now)
        parent.setCmdTime(now)
        cmds =  ["he rattr 0x${pp.device.deviceNetworkId} 0x${pp.device.endpointId} 0x0000 0x0001 {}", "delay 200",] 
        logDebug "ping child: pingTime = ${state.lastTx}"        
        
    }
    setCmdTimeNow()
    sendZigbeeCommands(cmds)
}

void setPingTime(val){
    state.lastTx["pingTime"] = val
}

void setCmdTime(val){
    state.lastTx["cmdTime"] = val
}

void setCmdTimeNow(){
    def now = new Date().getTime()
    if (state.lastTx == null) { state.lastTx = [:] }
    state.lastTx["cmdTime"] = now
    if (isChild()) {
        parent.setCmdTime(now)
    }
}

// TODO - not working!
void setState(st, index, val) {
    logTrace "setState ($st, $index, $val)"
    def str = "$st[\"$index\"]"
    logTrace "str=${str}"
    state.$str = val 
}

def clearRefreshRequest()   { if (state.states == null) {state.states = [:] }; state.states["isRefresh"] = false }
def clearIsDigital()        { if (state.states == null) {state.states = [:] }; state.states["isDigital"] = false }
def switchDebouncingClear() { if (state.states == null) {state.states = [:] }; state.states["debounce"]  = false }

/*
---------------------------------------------------------------------------------------------------
Hub Action (cmd) generators - only return ArrayList<String> Zigbee commands to the calling function
---------------------------------------------------------------------------------------------------
*/


// returns Zigbee commands to refresh the switch and the level
def cmdRefresh(String childDni) {
    def endpointId = childDniToEndpointId(childDni)
    logDebug "cmdRefresh: (childDni=${childDni} endpointId=${endpointId})  isParent()=${isParent()}"
    setCmdTimeNow()
    if (isLonsonho() || isTuyaBulb()) {
        return [
            "he rattr 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 0 {}",      "delay 100",
            "he rattr 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 0xF000 {}", "delay 100"
        ]
    }
    else {
        return [
            "he rattr 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 0 {}",
            "delay 100",
            "he rattr 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 0 {}",
            "delay 100"
        ]
    }
}

// returns Zigbee commands to togle the switch
def cmdSwitchToggle(String childDni) {
    def endpointId = childDniToEndpointId(childDni)
    def child = getChildByEndpointId(endpointId)
    String currentState = child.device.currentState('switch', true).value
    if (currentState == 'on') {
        return cmdSwitch(childDni, 0) + cmdRefresh(childDni)
    }
    else {
        return cmdSwitch(childDni, 1) + cmdRefresh(childDni)
    }
}

// returns Zigbee commands for on or off
def cmdSwitch(String childDni, onOff) {
    ArrayList<String> cmds = []
    def endpointId = childDniToEndpointId(childDni)
    logTrace "cmdSwitch: childDni=${childDni} onOff=${onOff} endpointId=${endpointId}"
    setCmdTimeNow()
    onOff = onOff ? "1" : "0"
    
    if (isTS0601()) {
        def dpValHex  = zigbee.convertToHexString(onOff as int, 2) 
        def cmd = childDni[-2..-1]
        def dpCommand = cmd == "01" ? "01" : cmd == "02" ? "07" : cmd == "03" ? "0F" : null
        cmds = sendTuyaCommand(dpCommand, DP_TYPE_BOOL, dpValHex)       
        logTrace "cmdSwitch: sending cmdSwitch command=${dpCommand} value=${onOff} ($dpValHex) cmds=${cmds}"
    }
    else {
        cmds = ["he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 ${onOff} {}"]
        logTrace "cmdSwitch: sending cmdSwitch endpointId=${endpointId} value=${onOff} cmds=${cmds}"
    }
   return cmds
}

// returns Zigbee commands for level control, depending on the device profile, as per the already scaled value
def cmdSetLevel(String childDni, value, duration) {
    def endpointId = childDniToEndpointId(childDni)
    value = value.toInteger()
    value = value < 1 ? 0 : value

    duration = (duration * 10).toInteger()
    def child = getChildByEndpointId(endpointId)
    logTrace "cmdSetLevel: child=${child} childDni=${childDni} value=${value} duration=${duration}"
    setCmdTimeNow()
    ArrayList<String> cmdsTuya = []
    ArrayList<String> cmdTS011 = []
    
    if (isTS0601()) {
        value = (value*10) as int 
        def dpValHex  = zigbee.convertToHexString(value as int, 8) 
        def dpType = DP_TYPE_VALUE
        def cmd = childDni[-2..-1]
        def dpCommand = cmd == "01" ? "02" : cmd == "02" ? "08" : cmd == "03" ? "10" : null
        if (isFanController()) {
            dpCommand = "04"
            dpValHex  = zigbee.convertToHexString((value/10) as int, 8) 
        }
        else if (isLerlinkFanController()) {
            dpCommand = "03"
            dpValHex  = zigbee.convertToHexString(value as int, 2) 
            // dpType is ENUM!
            dpType = DP_TYPE_ENUM
        }
        logDebug "cmdSetLevel: TS0601: sending cmdSetLevel command=${dpCommand} value=${value} ($dpValHex)"
        cmdsTuya = sendTuyaCommand(dpCommand, DP_TYPE_VALUE, dpValHex)
        if (child?.isAutoOn() && (device.currentState('switch', true)?.value ?: "") != 'on') {
            logDebug "cmdSetLevel: AutoOn: sending cmdSwitch on for switch #${endpointId}"
            cmdsTuya += cmdSwitch(childDni, 1)
        }
        logDebug "cmdSetLevel: TS0601: sending cmdsTuya=${cmdsTuya}"
        return cmdsTuya
    }
    else if (isLonsonho()) {
        // Lonsonho brightness values are * 10       // unsigned 16 bit int 
        value = value * 10
        cmdTS011 = [
            "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 0xF0  { 0x${intTo16bitUnsignedHex(value)} 0x${intTo16bitUnsignedHex(duration)} }",
        ]
        logDebug "LONSONHO: cmdSetLevel: sending value ${value} cmdTS011=${cmdTS011}"
    }
    else if ( isTuyaBulb()) {
        cmdTS011 = [
            "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 4  { 0x${intTo8bitUnsignedHex(value)} 0x${intTo16bitUnsignedHex(duration)} }",
        ]
        logDebug "LONSONHO: cmdSetLevel: sending value ${value} cmdTS011=${cmdTS011}"
    }
    else { // all other dimmers, different that EF00 and Lonsonho
        cmdTS011 = [
            "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0008 4 { 0x${intTo8bitUnsignedHex(value)} 0x${intTo16bitUnsignedHex(duration)} }",
        ]
        logDebug "GIRIER/others: cmdSetLevel: sending value ${value} cmdTS011=${cmdTS011}"
    }
    
    if (child?.isAutoOn()) {
        cmdTS011 += "he cmd 0x${device.deviceNetworkId} 0x${endpointId} 0x0006 1 {}"
    }
    logDebug "cmdSetLevel: sending cmdTS011=${cmdTS011}"
    return cmdTS011
}




/*
-----------------------------------------------------------------------------
Parent only code
-----------------------------------------------------------------------------
*/

// replaced with sendZigbeeCommands
def doActions(List<String> cmds) {
    if (isParent()) {
        sendZigbeeCommands( cmds )
        logDebug "doActions: Sending actions: ${cmds}"
    } else {
        throw new Exception("doActions() called incorrectly by child")
    }
}


/*
-----------------------------------------------------------------------------
Tuya cluster EF00 specific code
-----------------------------------------------------------------------------
*/

def tuyaBlackMagic() {
    
    def ep = safeToInt(state.destinationEP ?: 01)
    if (ep==null || ep==0) ep = 1
    
    return zigbee.readAttribute(0x0000, [0x0004, 0x000, 0x0001, 0x0005, 0x0007, 0xfffe], [destEndpoint :ep], delay=200)
}

def parseTuyaCluster( descMap ) {
    if (descMap.clusterId != "EF00") {
        return null
    }
    if (descMap.command == "0B") {
        logDebug "parseTuyaCluster: Tuya command 0x0B data=${descMap.data}"
        return null
    }
    if (descMap.command == "24") {
        logDebug "parseTuyaCluster: Tuya Time Sync request data=${descMap.data}"
        def offset = 0
        try {
            offset = location.getTimeZone().getOffset(new Date().getTime())
        }
        catch(e) {
            logWarn "Cannot resolve current location. please set location in Hubitat location setting. Setting timezone offset to zero"
        }
        def cmds = zigbee.command(0xEF00, 0x24, "0008" +zigbee.convertToHexString((int)(now()/1000),8) +  zigbee.convertToHexString((int)((now()+offset)/1000), 8))
        logDebug "parseTuyaCluster: sending time data : ${cmds}"
        cmds.each{ sendHubCommand(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE)) }
        return null
    }
    def cmd = descMap.data[2]
    def value = getAttributeValue(descMap.data)
    switch (cmd) {
        case "01" : // Switch1
            handleTuyaClusterSwitchCmd(cmd, value)
            break
        case "02" : // Brightness1 (switch level state) (or countdown for LerlinkFanController)
            if (isLerlinkFanController()) {
                logDebug "parseTuyaCluster: received: LerlinkFanController countdown cmd=${cmd} value=${value}"
                if (value != 0 ) {
                    stopCountdown(cmd, value)
                }
            }
            else {
                logDebug "parseTuyaCluster: received: Tuya brighntness(level) cmd=${cmd} value=${value}"
                handleTuyaClusterBrightnessCmd(cmd, value / 10 as int)    // 0 ... 1000
            }
            break        
        case "03" : // Minimum brightness1 (or fan speed for LerlinkFanController)
            if (isLerlinkFanController()) {
                logDebug "parseTuyaCluster: received: LerlinkFanController cmd=${cmd} value=${value}"
                handleTuyaClusterBrightnessCmd(cmd, value as int)
            }
            else {
                def switchNumber =  "01"
                logDebug "parseTuyaCluster: received: minimum brightness switch#${switchNumber} is ${value/10 as int} (raw=${value})"
                handleTuyaClusterMinBrightnessCmd(cmd, value / 10 as int)   // 0 ... 1000 default 20 ?
            }
            break
        case "04" : // (04) level for _TZE200_fvldku9h ;  Tuya type of light source for all others?
            if (isFanController()) {
                handleTuyaClusterBrightnessCmd(cmd, value as int)
            } else {
                //logDebug "parseTuyaCluster: received: Tuya type of light source cmd=${cmd} value=${value}"    // (LED, halogen, incandescent)
                String modeName = TS110ELightTypeOptions.options[value as int]
                String description = "Light type is $modeName"
                device.updateSetting('lightType', [value: value.toString(), type: 'enum'])
                sendEvent(name:'lightType', value: modeName, descriptionText: description, type: 'physical')
                logInfo "$description"
            }
            break
        case "05" : // Maximum brightness1
            def switchNumber = "01"
            logDebug "parseTuyaCluster: received: maximum brightness switch#${switchNumber} is ${value/10 as int} (raw=${value})"
            handleTuyaClusterMaxBrightnessCmd(cmd, value / 10 as int)
            break
        case "06" : // Countdown1
            def switchNumber = "01"
            logDebug "Countdown ${switchNumber} is ${value}s"
            if (value != 0 ) {
                stopCountdown(cmd, value)
            }
            break
        case "07" : // Switch2
            handleTuyaClusterSwitchCmd(cmd, value)
            break
        case "08" : // Brightness2
            logDebug "parseTuyaCluster: received: Tuya brighntness(level) cmd=${cmd} value=${value}"
            handleTuyaClusterBrightnessCmd(cmd, value / 10 as int)
            break        
        case "09" : // Minimum brightness2
            def switchNumber = "02"
            logDebug "parseTuyaCluster: received: minimum brightness switch#${switchNumber} is ${value / 10 as int} (raw=${value})"
            handleTuyaClusterMinBrightnessCmd(cmd, value / 10 as int)
            break
        case "0A" : // (10)    // _TZE204_o9gyszw2 switch type 2: 0-Toggle (Flip), 1-State (Sync), 2-Momentary (Button)
            logDebug "parseTuyaCluster: SwitchType2 Tuya dp= ${cmd} fn=${value}"        // default 0
            break
        case "0B" : // (11) Maximum brightness2 (or power-on status for LerlinkFanController)
            if (isLerlinkFanController()) {
                logDebug "parseTuyaCluster: received: LerlinkFanController power-on status cmd=${cmd} value=${value}"
                String modeName = TS0601PowerOnOptions.options[value as int]
                String description = "Power-On mode is $modeName"
                device.updateSetting('powerOnMode', [value: value.toString(), type: 'enum'])
                sendEvent(name:'powerOnMode', value: modeName, descriptionText: description, type: 'physical')
                logInfo "$description"
            }
            else {
                def switchNumber = "02"
                logDebug "parseTuyaCluster: received: maximum brightness switch#${switchNumber} is ${value / 10 as int} (raw=${value})"
                handleTuyaClusterMaxBrightnessCmd(cmd, value / 10 as int)
            }
            break
        case "0C" : // (12) Countdown2
            def switchNumber = "02"
            logDebug "Countdown ${switchNumber} is ${value}s"
            if (value != 0 ) {
                stopCountdown(cmd, value)
            }
            break
        case "0E" : // (14)
            //logInfo "Power-on Status Setting is ${value}"
            String modeName = TS0601PowerOnOptions.options[value as int]
            String description = "Power-On mode is $modeName"
            device.updateSetting('powerOnMode', [value: value.toString(), type: 'enum'])
            sendEvent(name:'powerOnMode', value: modeName, descriptionText: description, type: 'physical')
            logInfo "$description"
            break
        case "0F" : // (15) Switch3
            handleTuyaClusterSwitchCmd(cmd, value)
            break
        case "10" : // (16) Brightness3
            logDebug "parseTuyaCluster: received: Tuya brighntness(level) cmd=${cmd} value=${value}"
            handleTuyaClusterBrightnessCmd(cmd, value/10 as int)
            break        
        case "11" : // (17) Minimum brightness3
            def switchNumber = "03"
            logDebug "parseTuyaCluster: received: minimum brightness switch#${switchNumber} is ${value/10 as int} (raw=${value})"
            handleTuyaClusterMinBrightnessCmd(cmd, value/10 as int)
            break
        case "12" : // (18)
            logDebug "parseTuyaCluster: Unknown Tuya dp= ${cmd} fn=${value}"
            break
        case "13" : // (19) Maximum brightness3
            def switchNumber = "03"
            logDebug "parseTuyaCluster: received: maximum brightness switch#${switchNumber} is ${value/10 as int} (raw=${value})"
            handleTuyaClusterMaxBrightnessCmd(cmd, value/10 as int)
            break
        case "14" : // (20) Countdown3
            def switchNumber = "03"
            logDebug "Countdown ${switchNumber} is ${value}s"
            if (value != 0 ) {
                stopCountdown(cmd, value)
            }
            break
        case "15" : // (21)
            String modeName = TS0601LEDOptions.options[value as int]
            String description = "LED mode is $modeName"
            device.updateSetting('ledMode', [value: value.toString(), type: 'enum'])
            sendEvent(name:'ledMode', value: modeName, descriptionText: description, type: 'physical')
            logInfo "$description"
            break
        case "1A" : // (26)
            logInfo "Switch backlight ${value}"
            break
        case "28" : // (40)    // TS0601 Moes dimmer
            logDebug "parseTuyaCluster: Unknown Tuya dp= ${cmd} fn=${value}"
            break
        case "40" : // (64)
            logDebug "parseTuyaCluster: Unknown Tuya dp= ${cmd} fn=${value}"
            break
        default :
            logWarn "parseTuyaCluster: UNHANDLED Tuya cmd=${cmd} value=${value}"
            break
    }
}

void stopCountdown(cmd, value) {
    logWarn "stopping Countdown cmd=${cmd} value=${value}"
    def dpValHex  = zigbee.convertToHexString(0, 8) 
    sendZigbeeCommands(sendTuyaCommand(cmd, DP_TYPE_VALUE, dpValHex))
}


def parseBasicCluster( descMap ) {
    switch (descMap.attrId) {
        case "0001" :
            logDebug "parseBasicCluster: Tuya check-in ${descMap.attrId} (${descMap.value})"
            break
        case "0004" : // attrInt = 4 value= _TZE200_vm1gyrso data=null
            logDebug "parseBasicCluster: Tuya check-in ${descMap.attrId} (${descMap.value})"
            break
        default :
            logWarn "parseBasicCluster: unprocessed Basic cluster endpoint=${descMap?.endpoint} cluster=${descMap?.cluster} command=${descMap?.command} attrInt = ${descMap?.attrInt} value= ${descMap?.value} data=${descMap?.data}"
            break
    }
    
}

def handleTuyaClusterSwitchCmd(cmd,value) {
    def switchNumber = cmd == "01" ? "01" : cmd == "07" ? "02" : cmd == "0F" ? "03" : null
    logInfo "Switch ${switchNumber} is ${value==0 ? "off" : "on"}"
    if (getNumEps() == 1) {
        onSwitchState(value)
    }
    else {
        def child = getChildByEndpointId(switchNumber)
        def isFirst = 0 == endpointIdToIndex(switchNumber)
        child?.onSwitchState(value)
        if (isFirst && child != this) {
            logTrace "handleTuyaClusterSwitchCmd: Replicating switchState in parent"
            onSwitchState(value)
        }
    }
}

def handleTuyaClusterBrightnessCmd(cmd, value) {
    def switchNumber = cmd == "02" ? "01" : cmd == "08" ? "02" : cmd == "10" ? "03" : "01"
    if (isLerlinkFanController())  {
        logDebug "handleTuyaClusterBrightnessCmd: Ignoring scaling the ${value} level for LerlinkFanController"
        scaledValue = value
    }
    else {
        scaledValue = valueToLevel(value)
        logInfo "Brightness ${switchNumber} is ${scaledValue}% (${value})"
    }
    if (getNumEps() == 1)  {
        onSwitchLevel(value)
    }
    else {
        def child = getChildByEndpointId(switchNumber)
        def isFirst = 0 == endpointIdToIndex(switchNumber)
        child?.onSwitchLevel(value)
        if (isFirst && child != this) {
            logTrace "handleTuyaClusterBrightnessCmd: Replicating switchLevel in parent"
            onSwitchLevel(value)
        }
    }
}

def handleTuyaClusterMinBrightnessCmd(cmd, value) {
    if (isOzSmartThings()) {
        logDebug "handleTuyaClusterMinBrightnessCmd: Ignoring minBrightness ${value} for OzSmartThings"
        return
    }
    def switchNumber = cmd == "03" ? "01" : cmd == "09" ? "02" : cmd == "11" ? "03" : null
    if (getNumEps() == 1) {
        device.updateSetting("minLevel", [value: value , type:"number"])
        logInfo "minLevel parameter was updated to ${value}%"
    }
    else {
        def child = getChildByEndpointId(switchNumber)
        logDebug "handleTuyaClusterMinBrightnessCmd: cmd=${cmd} switchNumber=${switchNumber} child = ${child}"
        def isFirst = 0 == endpointIdToIndex(switchNumber)
        child?.updateSetting("minLevel", [value: value , type:"number"])
        logInfo "minLevel parameter for switch #${switchNumber} was updated to ${value}%"
        if (isFirst && child != this) {
            logDebug "handleTuyaClusterMinBrightnessCmd: Replicating minBrightness in parent"
            device.updateSetting("minLevel", [value: value , type:"number"])
        }
    }
}

def handleTuyaClusterMaxBrightnessCmd(cmd, value) {
    if (isOzSmartThings()) {
        logDebug "handleTuyaClusterMaxBrightnessCmd: Ignoring maxBrightness ${value} for OzSmartThings"
        return
    }
    def switchNumber = cmd == "05" ? "01" : cmd == "0B" ? "02" : cmd == "13" ? "03" : null
    if (getNumEps() == 1) {
        device.updateSetting("maxLevel", [value: value , type:"number"])
        logInfo "maxLevel parameter was updated to ${value}%"
    }
    else {
        def child = getChildByEndpointId(switchNumber)
        logDebug "handleTuyaClusterMaxBrightnessCmd: child = ${child}"
        def isFirst = 0 == endpointIdToIndex(switchNumber)
        child?.updateSetting("maxLevel", [value: value , type:"number"])
        logInfo "maxLevel parameter for switch #${switchNumber} was updated to ${value}%"
        if (isFirst && child != this) {
            logDebug "handleTuyaClusterMaxBrightnessCmd: Replicating maxBrightness in parent"
            device.updateSetting("maxLevel", [value: value , type:"number"])
        }
    }
}



private int getAttributeValue(ArrayList _data) {
    int retValue = 0
    try {    
        if (_data.size() >= 6) {
            int dataLength = zigbee.convertHexToInt(_data[5]) as Integer
            int power = 1;
            for (i in dataLength..1) {
                retValue = retValue + power * zigbee.convertHexToInt(_data[i+5])
                power = power * 256
            }
        }
    }
    catch ( e ) {
        logWarn "Exception caught while parsing Tuya data : ${_data}"
    }
    return retValue
}

private sendTuyaCommand(int dp, int dpType, int fnCmd, int fnCmdLength) {
    def ep = safeToInt(state.destinationEP)
    if (ep==null || ep==0) ep = 1
       
	def dpHex = zigbee.convertToHexString(dp, 2)
	def dpTypeHex = zigbee.convertToHexString(dpType, 2)
	def fnCmdHex = zigbee.convertToHexString(fnCmd, fnCmdLength)
	logDebug("sendTuyaCommand: dp=0x${dpHex}, dpType=0x${dpTypeHex}, fnCmd=0x${fnCmdHex}, fnCmdLength=${fnCmdLength}")
	def message = (randomPacketId().toString()
				   + dpHex
				   + dpTypeHex
				   + zigbee.convertToHexString((fnCmdLength / 2) as int, 4)
				   + fnCmdHex)
	logTrace("sendTuyaCommand: message=${message}")
	zigbee.command(CLUSTER_TUYA, ZIGBEE_COMMAND_SET_DATA, [destEndpoint :ep], message)
}

private randomPacketId() {
	return zigbee.convertToHexString(new Random().nextInt(65536), 4)
}


String getEventType()
{
    if (state.lastTx == null) state.lastTx = [:]
    def timeElapsed = now().toInteger() - (state.lastTx["cmdTime"] ?: '0').toInteger()
    return (timeElapsed <= MAX_PING_MILISECONDS) ? "digital" : "physical"
}
                
/*
-----------------------------------------------------------------------------
Child only code
-----------------------------------------------------------------------------
*/

def onSwitchState(value) {
    def map = [:] 
    if (state.stats != null) state.stats['switchCtr'] = (state.stats['switchCtr'] ?: 0) + 1 else state.stats=[:]
    def valueText = value == 0 ? "off" : "on"
    map.name = "switch"
    map.value = valueText
    map.type = getEventType()
    if (state.states == null) { state.states = [:] }
    boolean isRefresh = state.states["isRefresh"] ?: false
    if (isRefresh == true) {
        map.descriptionText = "${device.displayName} is ${map.value} [Refresh]"
        map.isStateChange = true
    }
    else {
        map.descriptionText = "${device.displayName} was turned ${map.value} [${map.type}]"
    } 
    logInfo "${map.descriptionText}"
    sendEvent(map)
}

def onSwitchLevel(value) {
    def map = [:] 
    if (state.stats != null) state.stats['levelCtr'] = (state.stats['levelCtr'] ?: 0) + 1 else state.stats=[:]
    def level = valueToLevel(safeToInt(value))
    map.name = "level"
    map.value = level
    map.unit = "%"
    map.type = getEventType()
    if (state.states == null) { state.states = [:] }
    boolean isRefresh = state.states["isRefresh"] ?: false
    if (isRefresh == true) {
        map.descriptionText = "${device.displayName} level is ${map.value} [Refresh]"
        map.isStateChange = true
    }
    else {
        map.descriptionText = "${device.displayName} level was set to ${map.value} [${map.type}]"
    }         
    logTrace "onSwitchLevel: Value=${value} level=${level} (value=${value})"
    logInfo "${map.descriptionText}"
    sendEvent(map)
}
    
/*
-----------------------------------------------------------------------------
Parent only helper functions
-----------------------------------------------------------------------------
*/
    
@Field static def childDniPattern = ~/^([0-9A-Za-z]+)-([0-9A-Za-z]+)$/

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// mappings between index, endpointId and childDni
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

def indexToEndpointId(i) {
    return intTo8bitUnsignedHex(i+1)
}

def endpointIdToIndex(i) {
    //log.trace "endpointIdToIndex i=${i}"
    return Integer.parseInt(i,16) - 1
}

def endpointIdToChildDni(endpointId) {
    //log.trace "endpointIdToChildDni ${endpointId} = ${device.deviceNetworkId} - ${endpointId}"
    //def childDni = "${device.deviceNetworkId}-${endpointId}"
    //log.trace "childDni = ${childDni}"
    return "${device.deviceNetworkId}-${endpointId}"
}

def indexToChildDni(i) {
    //log.trace "indexToChildDni(${i}): ${endpointIdToChildDni(indexToEndpointId(i))}"
    return endpointIdToChildDni(indexToEndpointId(i))
}

def childDniToEndpointId(childDni) {
    def match = childDni =~ childDniPattern
    logTrace "childDniToEndpointId: childDni=${childDni} childDniPattern=${childDniPattern} match[0][1]=${match[0][1]} match[0][2]=${match[0][2]} device.deviceNetworkId=${device.deviceNetworkId} match=${match}" 
    if (match) {
//        if (match[0][1] == device.deviceNetworkId) {
            return match[0][2]
 //       }
    }
    logTrace "childDniToEndpointId: childDni=${childDni} childDniPattern=${childDniPattern} match[0][1]=${match[0][1]} match[0][2]=${match[0][2]} device.deviceNetworkId=${device.deviceNetworkId} match=${match}" 
    //return null
    match[0][2]
}

def childDnisToEndpointIds(List<String> childDnis) {
    return childDnis.collect{cDni -> childDniToEndpointId(cDni)}
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// retrieving children, or retrieving parent if no children and endpointId
// is first index
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

def getChildByEndpointId(endpointId) {
    logTrace "getChildByEndpointId endpointId=${endpointId}"
    if (endpointId == null) return this
    if (endpointIdToIndex(endpointId) == 0 && getChildDevices().size() == 0) {
        logTrace "getChildByEndpointId returning this: ${this}"
        return this
    } else {
        def cd = getChildDevice(endpointIdToChildDni(endpointId))
        logTrace "getChildByEndpointId(${endpointId}) returning getChildDevice: ${cd} (childDNI=${endpointIdToChildDni(endpointId)})"
        if (cd == null) {
            // try obtaining the child device from the list ... in case the parent DNI was changed!
            cd =  getChildDevices()[(endpointId as int) -1]
            logTrace "parent DNI was changed... cd=${cd}"
        }
        return cd
    }
}

def getChildEndpointIds() {
    if (getChildDevices().size() == 0) {
        //logDebug "getChildEndpointIds() : no childs"
        return [getDestinationEP()/*device.endpointId*/]
    } else {
        //logDebug "getChildEndpointIds() : size = ${getChildDevices().size()}"
        return getChildDevices().collect{childDniToEndpointId(it.getDni())}
    }
}

private String getChildId(childDevice) {
    return childDevice.deviceNetworkId.substring(childDevice.deviceNetworkId.length() - 2)
}

/*
-----------------------------------------------------------------------------
Child only helper functions
-----------------------------------------------------------------------------
*/

def getDni() {
    return device.deviceNetworkId
}

def isAutoOn() {
    return settings.autoOn
}

def levelToValue(BigDecimal level) {
    return levelToValue(level.toInteger())
}

// converts Hubitat level 0..100 to device brightness level ( 0..255, 0..100 depending on the device profile)  
def levelToValue(Integer level) {
    if (level == 0) {
        return 0    // skip the scaling to enable auto off!
    }
    def mult = 1.0F
    if (isTS0601()) {// ? 1.0 : 2.55 
        mult = 1.0
    }
    else if (isLonsonho()) {
        mult = 1.0
    }
    else {    // including isTuyaBulb()
        mult =  2.55
    }
    Integer minLevel100 = Math.round((settings.minLevel ?: DEFAULT_MIN_LEVEL) * mult)
    Integer maxLevel100 = Math.round((settings.maxLevel ?: DEFAULT_MAX_LEVEL) * mult)
    
    def reScaled = scaleBetween(level,minLevel100, maxLevel100, 0, 100)
    logTrace "levelToValue: level=${level} reScaled=${reScaled} (mult=${mult}, minLevel100=${minLevel100}, maxLevel100=${maxLevel100})"
    return reScaled
}

def valueToLevel(BigDecimal value) {
    return valueToLevel(value.toInteger())
}

// converts device brightness value 0..255 to Hubitat re-scaled level 0..100    //  # 0xF000 reported values are 10-1000 -> divided by 10 = 1-100
def valueToLevel(Integer value) {
    def mult = 1.0F
    if (isTS0601()) {// ? 1.0 : 2.55 
        mult = 1.0
    }
    else if (isLonsonho()) {
        mult = 1.0
    }
    else {    // including isTuyaBulb()
        mult =  2.55
    }
    Integer minValue255 = Math.round((settings.minLevel ?: DEFAULT_MIN_LEVEL) * mult)
    Integer maxValue255 = Math.round((settings.maxLevel ?: DEFAULT_MAX_LEVEL) * mult)
    //log.trace "mult=${mult}, minValue255=${minValue255}, maxValue255=${maxValue255}"
    
    if (value < minValue255) value = minValue255
    if (value > maxValue255) value = maxValue255
    
    def reScaled = rescale(value, minValue255, maxValue255, 0, 100)
    logTrace "valueToLevel: raw value:${value} reScaled=${reScaled} (isParent=${isParent()}, isTS0601=${isTS0601()})"
    return reScaled
}

/*
-----------------------------------------------------------------------------
Standard handlers
-----------------------------------------------------------------------------
*/

def getDeviceInfo() {
    return "model=${getDW().device.getDataValue('model')} manufacturer=${getDW().device.getDataValue('manufacturer')} destinationEP=${state.destinationEP ?: UNKNOWN} <b>deviceProfile=${state.deviceProfile ?: UNKNOWN}</b>"
}

//  will be the first function called just once when paired as a new device. Not called again on consequent re-pairings !
def installed() {
    logDebug "<b>installed()</b> ... model=${device.getDataValue('model')} manufacturer=${device.getDataValue('manufacturer')}"
    sendEvent(name: 'level', value: 0, unit: '%')
    sendEvent(name: 'switch', value: 'off')
    sendEvent(name: 'healthStatus', value: 'unknown')
    initializeVars( fullInit = true )
    // TuyaBlackMagic + create child devices
    initialized()    
}

def init() {
    installed()
}

// called every time the device is paired to the HUB (both as new or as an existing device)
def configure() {
    logDebug "<b>configure()</b> ..."
    setDestinationEP()
    checkDriverVersion()
    if (state.deviceProfile == null) {
        setDeviceNameAndProfile()
    }
    else {
        logInfo "the selected ${state.deviceProfile} device profile was not changed!"
    }
    updated()
}

void getAllProperties() {
    log.trace 'Properties:'    
    device.properties.each { it->
        log.debug it
    }
    log.trace 'Settings:'    
    settings.each { it->
        log.debug "${it.key} =  ${it.value}"    // https://community.hubitat.com/t/how-do-i-get-the-datatype-for-an-app-setting/104228/6?u=kkossev
    }    
    log.trace 'Done'    
}

// delete all Preferences
void deleteAllSettings() {
    settings.each { it->
        log.debug "deleting ${it.key}"
        //this.removeSetting("${it.key}")
        device.removeSetting("${it.key}")
    }
    logInfo  "All settings (preferences) DELETED"
}

// delete all attributes
void deleteAllCurrentStates() {
    device.properties.supportedAttributes.each { it->
        log.debug "deleting $it"
        device.deleteCurrentState("$it")
    }
    logInfo "All current states (attributes) DELETED"
}

// delete all State Variables
void deleteAllStates() {
    state.each { it->
        log.debug "deleting state ${it.key}"
    }
    state.clear()
    logInfo "All States DELETED"
}

void deleteAllScheduledJobs() {
    unschedule()
    logInfo "All scheduled jobs DELETED"
}


def loadAllDefaults() {
    logWarn "loadAllDefaults() !!!"
    deleteAllSettings()
    deleteAllCurrentStates()
    deleteAllScheduledJobs()
    deleteAllStates()
}

// called on hub startup if driver specifies capability "Initialize" (otherwise is not required or automatically called if present)
def initialize() {
    log.info "<b>initialize()</b> ... ${getDeviceInfo()}"
    loadAllDefaults()
    initializeVars( fullInit = true )
    // TuyaBlackMagic + create child devices
    initialized()
    configure()
}

// called from configure()
def setDestinationEP() {
    def ep = device.getEndpointId()
    if (ep != null && ep != 'F2') {
        this.state.destinationEP = ep
        logDebug "setDestinationEP() destinationEP = ${state.destinationEP}"
    }
    else {
        logWarn "setDestinationEP() Destination End Point not found or invalid(${ep}), activating the F2 bug patch!"
        this.state.destinationEP = "01"    // fallback EP
    }      
}

def getDestinationEP() {
    return state.destinationEP ?: device.endpointId ?: "01"
}

def resetStats() {
    state.stats = [:]
    state.states = [:]
    state.lastRx = [:]
    state.lastTx = [:]
//    state.health = [:]
    state.stats["rxCtr"] = 0
    state.stats["txCtr"] = 0
//    state.states["isDigital"] = false
    state.states["isRefresh"] = false
//    state.health["offlineCtr"] = 0
 //   state.health["checkCtr3"] = 0
}

void initializeVars( boolean fullInit = false ) {
    log.info "InitializeVars( fullInit = ${fullInit} )..."
    if (fullInit == true) {
        /* settings = [:] */ // exception when copying settings to the child devices! // use clearSetting(key) 
        unschedule()
        state.clear()
        resetStats()        
        device.deleteCurrentState()
        state.driverVersion = driverVersionAndTimeStamp()
    }
    if (state.stats == null)  { state.stats  = [:] }
//    if (state.states == null) { state.states = [:] }
    if (state.lastRx == null) { state.lastRx = [:] }
    if (state.lastTx == null) { state.lastTx = [:] }
    if (state.lastTx["cmdTime"] == null)  state.lastTx["cmdTime"] = now()
    if (this.state.lastTx["pingTime"] == null)  this.state.lastTx["pingTime"] = now()
//    if (state.health == null) { state.health = [:] }  
    
    if (fullInit == true || state.deviceProfile == null) {
        setDeviceNameAndProfile()
    }
    if (fullInit == true || settings.logEnable == null) device.updateSetting("logEnable", [value: DEFAULT_DEBUG_OPT, type: 'bool'] )
    if (fullInit == true || settings.traceEnable == null) device.updateSetting("traceEnable", [value: false, type: 'bool'])
    
    if (fullInit == true || settings.txtEnable == null) device.updateSetting("txtEnable", [value: true, type: 'bool'])
    if (fullInit == true || settings?.minLevel == null) device.updateSetting("minLevel", [value:DEFAULT_MIN_LEVEL, type:"number"]) 
    if (fullInit == true || settings?.maxLevel == null) device.updateSetting("maxLevel", [value:DEFAULT_MAX_LEVEL, type:"number"]) 
    if (fullInit == true || settings?.healthCheckInterval == null) device.updateSetting('healthCheckInterval', [value: HealthcheckIntervalOpts.defaultValue.toString(), type: 'enum'])
    if (fullInit == true)  { device.updateSetting('ledMode', [value: null, type: 'enum'])}           //  no ledMode by default!
    if (fullInit == true)  { device.updateSetting('powerOnMode', [value: null, type: 'enum'])}       //  no powerOnMode by default!
    if (fullInit == true)  { device.updateSetting('lightType', [value: null, type: 'enum'])}         //  no lightType by default!
}

// will be called when user selects Save Preferences
def updated() {
    logDebug "<b>updated()</b> ... ${getDeviceInfo()}"
    unschedule()
    checkDriverVersion()
    
    if (settings?.logEnable) {
        logTrace settings
        runIn(86400, logsOff)
    }    

    if (isParent()) {
        int interval = (settings.healthCheckInterval as Integer) ?: 0
        if (interval > 0) {
            log.info "scheduling health check every ${interval} minutes"
            scheduleDeviceHealthCheck(interval)
        }
    }
    else {
        logDebug "skipping the health check for a child device"
    }
      
    // version 0.3.1
    if (settings?.forcedProfile != null) {
        logDebug "state.deviceProfile=${state.deviceProfile}, settings.forcedProfile=${settings?.forcedProfile}, getProfileKey()=${getProfileKey(settings?.forcedProfile)}"
        if (getProfileKey(settings?.forcedProfile) != state.deviceProfile) {
            logWarn "changing the device profile from ${state.deviceProfile} to ${getProfileKey(settings?.forcedProfile)}"
            state.deviceProfile = getProfileKey(settings?.forcedProfile)
            logInfo "press F5 to refresh the page"
        }
    }
    else {
        logDebug "forcedProfile is not set"
    }
    
    //
    if (isTS0601()) {
        ArrayList<String> cmdsTuya = []
        def cmd = "01"
        def eps = getNumEps()
        logDebug "config().numEps = ${eps}"
        if (eps > 1) {
            if (isParent()) {
                cmd = "01"
                logInfo "### updating the settings for the ${eps} gangs device -  ${device.getDataValue("manufacturer")} - <b>FIRST CHANNEL ONLY</b>"
            }
            else {
                logTrace "this.device.getData() = ${this.device.getData()}"
                cmd = this.device.getData().componentName[-2..-1]
                logInfo "### updating settings for child device ${this.device.getData().componentName} ... device #${cmd}"
            }
        }
        else {    // single EP device
            logInfo "### updating settings for device ${device.getDataValue("manufacturer")}"
        }
        // minLevel
        Integer value = Math.round(this.minLevel * 10)
        def dpValHex  = zigbee.convertToHexString(value as int, 8) 
        logDebug "updated() minLevel value = ${this.minLevel} (raw=$value)"
        def dpCommand = cmd == "01" ? "03" : cmd == "02" ? "09" : cmd == "03" ? "11" : null
        logDebug "sending minLevel command=${dpCommand} value=${value} ($dpValHex)"
        if (isParent()) cmdsTuya += sendTuyaCommand(dpCommand, DP_TYPE_VALUE, dpValHex)
        else cmdsTuya += parent?.sendTuyaCommand(dpCommand, DP_TYPE_VALUE, dpValHex)
        // maxLevel
        value = Math.round(this.maxLevel * 10)
        dpValHex  = zigbee.convertToHexString(value as int, 8) 
        logDebug "updated() maxLevel  value = ${this.maxLevel} (raw=$value)"
        dpCommand = cmd == "01" ? "05" : cmd == "02" ? "0B" : cmd == "03" ? "13" : null
        logDebug "sending maxLevel command=${dpCommand} value=${value} ($dpValHex)"
        if (isParent()) cmdsTuya += sendTuyaCommand(dpCommand, DP_TYPE_VALUE, dpValHex)
        else cmdsTuya += parent?.sendTuyaCommand(dpCommand, DP_TYPE_VALUE, dpValHex)
        // lightType
        if (settings.lightType != null) {
            value = settings.lightType as int
            dpValHex  = zigbee.convertToHexString(value as int, 2) 
            logDebug "updated() sending lightType value ${value} (TS110ELightTypeOptions.options[value as int])"
            dpCommand = "04"
            if (isParent()) cmdsTuya += sendTuyaCommand(dpCommand, DP_TYPE_ENUM, dpValHex)
            else cmdsTuya += parent?.sendTuyaCommand(dpCommand, DP_TYPE_ENUM, dpValHex)
        }
        // powerOnMode
        if (settings.powerOnMode != null) {
            value = settings.powerOnMode as int
            dpValHex  = zigbee.convertToHexString(value as int, 2) 
            logDebug "updated() sending powerOnMode value ${value} (TS0601PowerOnOptions.options[value as int])"
            if (isLerlinkFanController()) { dpCommand = "0B" }
            else { dpCommand = "0E" }
            if (isParent()) cmdsTuya += sendTuyaCommand(dpCommand, DP_TYPE_ENUM, dpValHex)
            else cmdsTuya += parent?.sendTuyaCommand(dpCommand, DP_TYPE_ENUM, dpValHex)
        }
        // ledMode
        if (settings.ledMode != null) {
            value = settings.ledMode as int
            dpValHex  = zigbee.convertToHexString(value as int, 2) 
            logDebug "updated() sending ledMode value ${value} (TS0601LEDOptions.options[value as int])"
            dpCommand = "15"
            if (isParent()) cmdsTuya += sendTuyaCommand(dpCommand, DP_TYPE_ENUM, dpValHex)
            else cmdsTuya += parent?.sendTuyaCommand(dpCommand, DP_TYPE_ENUM, dpValHex)
        }
        //
        hubitat.device.HubMultiAction allActions = new hubitat.device.HubMultiAction()
        cmdsTuya.each {
            allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE))
        }
        sendHubCommand(allActions)        
    } // TS0601 only
    
    if (isParent()) {
        logDebug "updated parent->child"
        getChildByEndpointId(indexToEndpointId(0))?.onParentSettingsChange(settings)
    } else {
        logDebug "updated child->parent"
        parent?.onChildSettingsChange(device.deviceNetworkId, settings)
    }
    // ver 1.3.1 03/25/2023 commented out
    //return initialized()
}

// custom initialization method, called from installed()
def initialized() {
    logDebug "<b>initialized()</b> ... ${getDeviceInfo()}"
    ArrayList<String> cmds = []
    unschedule() // added 12/10/2022
    logDebug "initialized() device.getData() = ${device.getData()}"
    
    if (isParent()) {
        createChildDevices()   
        if (true /*device.getDataValue("model") == "TS0601"*/) {    // TODO !! must be called for TS0011E also! TODO: isTuya() !
            logDebug "spelling tuyaBlackMagic()"
            cmds += tuyaBlackMagic()
        }
        else {
            logDebug "tuyaBlackMagic() was skipped for model ${device.getDataValue('model')}"
        }
        ArrayList<String> configCmds = listenChildDevices()
        if (configCmds != null) {
            cmds += configCmds
        }
        // 
        sendZigbeeCommands(cmds)
    }
    else {
        logDebug "skipping initialized() for child device"
    }
}



/*
-----------------------------------------------------------------------------
Shared helper functions
-----------------------------------------------------------------------------
*/
import java.lang.Math;

def rescale(value, fromLo, fromHi, toLo, toHi) {
    return Math.round((((value-fromLo)*(toHi-toLo))/(fromHi-fromLo)+toLo))
}

def scaleBetween(unscaledNum, minAllowed, maxAllowed, min, max) {
      return Math.round(  (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed )
}

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(), 2)
}

/*
-----------------------------------------------------------------------------
Logging output
-----------------------------------------------------------------------------
*/

def logTrace(msg) {
    if (settings.traceEnable) {
        log.trace "${device.displayName} " + msg
    }
}

def logDebug(msg) {
    if (settings.debugEnable) {
        log.debug "${device.displayName} " + msg
    }
}

def logInfo(msg) {
    if (settings.infoEnable) {
        log.info "${device.displayName} " + msg
    }
}

def logWarn(msg) {
    if (settings.debugEnable) {
        log.warn "${device.displayName} " + msg
    }
}



//////////////////////////////////////////////////////////////////////////////

private getCLUSTER_TUYA()       { 0xEF00 }
private getSETDATA()            { 0x00 }
private getSETTIME()            { 0x24 }

// Tuya Commands
private getTUYA_REQUEST()       { 0x00 }
private getTUYA_REPORTING()     { 0x01 }
private getTUYA_QUERY()         { 0x02 }
private getTUYA_STATUS_SEARCH() { 0x06 }
private getTUYA_TIME_SYNCHRONISATION() { 0x24 }

// tuya DP type
private getDP_TYPE_RAW()        { "01" }    // [ bytes ]
private getDP_TYPE_BOOL()       { "01" }    // [ 0/1 ]
private getDP_TYPE_VALUE()      { "02" }    // [ 4 byte value ]
private getDP_TYPE_STRING()     { "03" }    // [ N byte string ]
private getDP_TYPE_ENUM()       { "04" }    // [ 0-255 ]
private getDP_TYPE_BITMAP()     { "05" }    // [ 1,2,4 bytes ] as bits

private sendTuyaCommand(dp, dp_type, fncmd) {
    ArrayList<String> cmds = []
    def ep = safeToInt(state.destinationEP)
    if (ep==null || ep==0) ep = 1
    
    cmds += zigbee.command(CLUSTER_TUYA, SETDATA, [destEndpoint :ep], PACKET_ID + dp + dp_type + zigbee.convertToHexString((int)(fncmd.length()/2), 4) + fncmd )
    logDebug "${device.displayName} sendTuyaCommand = ${cmds}"
    return cmds
}

private getPACKET_ID() {
    return randomPacketId()
}

Integer safeToInt(val, Integer defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

Double safeToDouble(val, Double defaultVal=0.0) {
	return "${val}"?.isDouble() ? "${val}".toDouble() : defaultVal
}

void sendZigbeeCommands(ArrayList<String> cmd) {
    logDebug "sendZigbeeCommands: ${cmd}"
    hubitat.device.HubMultiAction allActions = new hubitat.device.HubMultiAction()
    cmd.each {
            allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE))
            if (state.stats != null) state.stats['txCtr'] = (state.stats['txCtr'] ?: 0) + 1 else state.stats=[:]
    }
    sendHubCommand(allActions)
}

def driverVersionAndTimeStamp() {version() + ' ' + timeStamp() + ((_DEBUG==true) ? " debug version!" : " ")}

def checkDriverVersion() {
    if (state.driverVersion == null || driverVersionAndTimeStamp() != state.driverVersion) {
        logInfo "updating the settings from the current driver version ${state.driverVersion} to the new version ${driverVersionAndTimeStamp()}"
        initializeVars(fullInit = false)
        if (state.lastRx == null || state.stats == null || state.lastTx == null) {
            resetStats()
        }
        scheduleDeviceHealthCheck()        
        state.driverVersion = driverVersionAndTimeStamp()
    }
}

def updateTuyaVersion() {
    def application = device.getDataValue("application") 
    if (application != null) {
        def ver = zigbee.convertHexToInt(application)
        def str = ((ver&0xC0)>>6).toString() + "." + ((ver&0x30)>>4).toString() + "." + (ver&0x0F).toString()
        if (device.getDataValue("tuyaVersion") != str) {
            device.updateDataValue("tuyaVersion", str)
            logInfo "tuyaVersion set to $str"
        }
    }
    else {
        return null
    }
}

def getProfileKey(String valueStr) {
    def key = null
    deviceProfilesV2.each {  profileName, profileMap ->
        if (profileMap.description.equals(valueStr)) {
            key = profileName
        }
    }
    return key
}

def getDeviceNameAndProfile( model=null, manufacturer=null) {
    def deviceName         = UNKNOWN
    def deviceProfile      = UNKNOWN
    String deviceModel        = model != null ? model : device.getDataValue('model') ?: UNKNOWN
    String deviceManufacturer = manufacturer != null ? manufacturer : device.getDataValue('manufacturer') ?: UNKNOWN
    deviceProfilesV2.each { profileName, profileMap ->
        profileMap.fingerprints.each { fingerprint ->
            if (fingerprint.model == deviceModel && fingerprint.manufacturer == deviceManufacturer) {
                deviceProfile = profileName
                deviceName = fingerprint.deviceJoinName ?: deviceProfilesV2[deviceProfile].deviceJoinName ?: UNKNOWN
                logDebug "<b>found exact match</b> for model ${deviceModel} manufacturer ${deviceManufacturer} : <b>profileName=${deviceProfile}</b> deviceName =${deviceName}"
                return [deviceName, deviceProfile]
            }
        }
    }
    if (deviceProfile == UNKNOWN) {
        logWarn "model ${deviceModel} manufacturer ${deviceManufacturer} <b>NOT FOUND!</b> deviceName=${deviceName} profileName=${deviceProfile}"
    }
    return [deviceName, deviceProfile]
}

// called from TODO
def setDeviceNameAndProfile( model=null, manufacturer=null) {
    def (String deviceName, String deviceProfile) = getDeviceNameAndProfile(model, manufacturer)
    if (deviceProfile == null) {
        logWarn "unknown model ${deviceModel} manufacturer ${deviceManufacturer}"
        // don't change the device name when unknown
        state.deviceProfile = UNKNOWN
    }
    def dataValueModel = model != null ? model : device.getDataValue('model') ?: UNKNOWN
    def dataValueManufacturer  = manufacturer != null ? manufacturer : device.getDataValue('manufacturer') ?: UNKNOWN
    if (deviceName != NULL && deviceName != UNKNOWN  ) {
        device.setName(deviceName)
        state.deviceProfile = deviceProfile
        //logDebug "before: forcedProfile = ${settings.forcedProfile} to be set to ${deviceProfilesV2[deviceProfile].description}"
        device.updateSetting("forcedProfile", [value:deviceProfilesV2[deviceProfile].description, type:"enum"])
        //pause(1)
        //logDebug "after : forcedProfile = ${settings.forcedProfile}"
        logInfo "device model ${dataValueModel} manufacturer ${dataValueManufacturer} was set to : <b>deviceProfile=${deviceProfile} : deviceName=${deviceName}</b>"
    } else {
        logWarn "device model ${dataValueModel} manufacturer ${dataValueManufacturer} was not found!"
    }    
}

//----------------------------

/**
 * sends 'rtt'event (after a ping() command)
 * @param null: calculate the RTT in ms
 *        value: send the text instead ('timeout', 'n/a', etc..)
 * @return none
 */
void sendRttEvent( String value=null) {
    def now = new Date().getTime()
    def timeRunning = now.toInteger() - (this.state.lastTx["pingTime"] ?: now()).toInteger()
    if (timeRunning <= 0) return
    def descriptionText = "Round-trip time is ${timeRunning} (ms)"
    if (value == null) {
    	def cd = getChildDevices()
        cd.each { child ->
            logTrace "child=${child}"
            child?.sendEvent(name: 'rtt', value: timeRunning, descriptionText: descriptionText, unit: "ms", type: "physical")
            child?.logInfo "${descriptionText}"
        }   
        this.logInfo "${descriptionText}"
        this.sendEvent(name: "rtt", value: timeRunning, descriptionText: descriptionText, unit: "ms", type: "physical")    
    }
    else {
    	def cd = getChildDevices()
        cd.each { child ->
            logTrace "child=${child}"
            child?.sendEvent(name: 'rtt', value: value, descriptionText: descriptionText, unit: "ms", type: "physical")
            child?.logInfo "${descriptionText}"
        }   
        descriptionText = "Round-trip time is ${value}"
        this.logInfo "${descriptionText}"
        this.sendEvent(name: "rtt", value: value, descriptionText: descriptionText, unit: "ms",type: "physical")    
    }
}


private void sendHealthStatusEventAll(String status) {
    logTrace "sendHealthStatusEventAll: ${status}   DW=${getDW()}     size = ${getChildDevices().size()}"
    
    if (device.currentValue('healthStatus') != status) {
        String descriptionText = "healthStatus was set to ${status}"
    	def cd = getChildDevices()
        cd.each { child ->
            logTrace"child=${child}"
            child?.sendEvent(name: 'healthStatus', value: status, descriptionText: descriptionText)
            child?.logInfo "${descriptionText}"
        }   
        this.sendEvent(name: 'healthStatus', value: status, descriptionText: descriptionText)
        this.logInfo "${descriptionText}"
    }
}

private void scheduleCommandTimeoutCheck(int delay = COMMAND_TIMEOUT) {
    logTrace "scheduleCommandTimeoutCheck: delay=${delay} isParent=${isParent()}"
    runIn(delay, 'deviceCommandTimeout', [overwrite: true, misfire: "ignore"])
}


void deviceCommandTimeout() {
    logWarn 'no response received (device offline?)'
    sendHealthStatusEventAll('offline')
}

private void scheduleDeviceHealthCheck() {
    int interval = (settings.healthCheckInterval as Integer) ?: HealthcheckIntervalOpts.defaultValue
    if (interval > 0) {
        log.info "scheduling health check every ${interval} minutes"
        scheduleDeviceHealthCheck(interval)
    }
}

private void scheduleDeviceHealthCheck(int intervalMins) {
    Random rnd = new Random()
    getDW().schedule("${rnd.nextInt(59)} ${rnd.nextInt(9)}/${intervalMins} * ? * * *", 'ping')
}

void logsOff() {
    logInfo 'debug logging disabled...'
    device.updateSetting('debugEnable', [value: 'false', type: 'bool'])
    device.updateSetting('traceEnable', [value: 'false', type: 'bool'])
}

//----------------------------

def tuyaTest( dpCommand, dpValue, dpTypeString ) {
    ArrayList<String> cmds = []
    def dpType   = dpTypeString=="DP_TYPE_VALUE" ? DP_TYPE_VALUE : dpTypeString=="DP_TYPE_BOOL" ? DP_TYPE_BOOL : dpTypeString=="DP_TYPE_ENUM" ? DP_TYPE_ENUM : null
    def dpValHex = dpTypeString=="DP_TYPE_VALUE" ? zigbee.convertToHexString(dpValue as int, 8) : dpValue

    logWarn "${device.displayName}  sending TEST command=${dpCommand} value=${dpValue} ($dpValHex) type=${dpType}"

    sendZigbeeCommands( sendTuyaCommand(dpCommand, dpType, dpValHex) )
}

def testParse(String description) {
    log.warn "test parsing : ${description}"
    parse( description)
    log.warn "--- end ---"
}

def moveToLevelTuya( level, delay) {
    /*
            moveToLevelTuya: {
                ID: 240,        0xF0
                parameters: [
                    {name: 'level', type: DataType.uint16},
                    {name: 'transtime', type: DataType.uint16},
                ],
            },

*/
    
    
}

def testRefresh() {
    logWarn "testRefresh: see the live logs"
    ArrayList<String> cmds = []
    if (state.states == null) state.states = [:]
    state.states["isRefresh"] = true    
    def ep = safeToInt(getDestinationEP())
    cmds = zigbee.readAttribute(0x0000, [0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006], [destEndpoint:ep], delay=200)
    // commands : off:0 on:1 toggle:2
    cmds += zigbee.readAttribute(0x0006, 0x0000, [destEndpoint:ep], delay = 200)    // On/Off
    cmds += zigbee.readAttribute(0x0006, 0x4000, [destEndpoint:ep], delay = 200)    // globalSceneCtrl, boolean
    cmds += zigbee.readAttribute(0x0006, 0x4001, [destEndpoint:ep], delay = 200)    // OnTime, uint16
    cmds += zigbee.readAttribute(0x0006, 0x4002, [destEndpoint:ep], delay = 200)    // OffWaitTime, uint16
    cmds += zigbee.readAttribute(0x0006, 0x4003, [destEndpoint:ep], delay = 200)    // startUpOnOff, enum8
    cmds += zigbee.readAttribute(0x0006, 0x5000, [destEndpoint:ep], delay = 200)    // tuyaBacklightSwitch, enum8
    cmds += zigbee.readAttribute(0x0006, 0x8001, [destEndpoint:ep], delay = 200)    // IndicatorMode: 1 (backlight), enum8
    cmds += zigbee.readAttribute(0x0006, 0x8002, [destEndpoint:ep], delay = 200)    // RestartStatus: 2 , enum8
    cmds += zigbee.readAttribute(0x0006, 0x8004, [destEndpoint:ep], delay = 200)    // OperationMode, enum8
    
    // commands: moveToLevel:0 (level:uint8, transtime:uint16)              move:1 (movemode:uint8, rate:uint8)                        step:2 (stepmode:uint8, stepsize:uint8, transtime:uint16 )          stop:3 ()
    // commands: moveToLevelWithOnOff:4 (level:uint8, transtime:uint16)     moveWithOnOff:5 (movemode:uint8, rate:uint8)               stepWithOnOff:6 (stepmode:uint8, stepsize:uint8, transtime:uint16 )
    // commands: stopWithOnOff:7 ()                                         moveToLevelTuya:0xF0 (level:uint16, transtime:uint16)
    // commands: 
    cmds += zigbee.readAttribute(0x0008, 0x0000, [destEndpoint:ep], delay = 200)    // current level, uint8
    cmds += zigbee.readAttribute(0x0008, 0x0001, [destEndpoint:ep], delay = 200)    // remainingTime, uint16 - GLEDOPTO, Philips Hue
    cmds += zigbee.readAttribute(0x0008, 0x0002, [destEndpoint:ep], delay = 200)    // minLevel, uint8
    cmds += zigbee.readAttribute(0x0008, 0x0003, [destEndpoint:ep], delay = 200)    // maxLevel, uint8
    cmds += zigbee.readAttribute(0x0008, 0x000F, [destEndpoint:ep], delay = 200)    // options, bitmap8      - Philips Hue
    cmds += zigbee.readAttribute(0x0008, 0x0010, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0x0011, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0x0012, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0x0013, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0x0014, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0xF000, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0x00F0, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0xFC02, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0xFC03, [destEndpoint:ep], delay = 200)    // 
    cmds += zigbee.readAttribute(0x0008, 0xFC04, [destEndpoint:ep], delay = 200)    // 

    cmds += zigbee.readAttribute(0xE000, 0xD001, [destEndpoint:ep], delay = 200)    // encoding:42, value:AAAA; attrId: D001, encoding: 48, value: 020006
    cmds += zigbee.readAttribute(0xE000, 0xD002, [destEndpoint:ep], delay = 200)    // encoding: 48, value: 02000A
    cmds += zigbee.readAttribute(0xE000, 0xD003, [destEndpoint:ep], delay = 200)
    
    cmds += zigbee.readAttribute(0xE001, 0xD010, [destEndpoint:ep], delay = 200)    // powerOnBehavior: {ID: 0xD010, type: DataType.enum8},
    cmds += zigbee.readAttribute(0xE001, 0xD030, [destEndpoint:ep], delay = 200)    // switchType: {ID: 0xD030, type: DataType.enum8},
    
    sendZigbeeCommands(cmds)
}

def getParentDNI() {
    if (isParent()) {
        logTrace "Parent device DNI: ${device.deviceNetworkId}"
        return device.deviceNetworkId
    }
    else {
        def parentDeviceId = parent?.deviceNetworkId?.split("-")[0]
        if (parentDeviceId) {
            logTrace "Parent device DNI: ${parentDeviceId}"
            return parentDeviceId
        } 
        else {
            logWarn "No parent device found."
            return null
        }
    }
}

def setState( st, val )
{
    logTrace "setState ($st, $val)"
    state."$st" = val
}

def testX( var ) {
    /*
    def cd = getChildDevices()
    log.warn "cd=${cd}"
        cd.each { child ->
            child.setState("test", "test")
            logTrace "child=${child}    child.getState() = ${child.getState()}"
        } 
    */
    parent.setState("parentState", "parentTest")

}

// https://developer.tuya.com/en/docs/iot/tuya-smart-dimmer-switch-single-phrase-input-without-neutral-line?id=K9ik6zvokodvn#subtitle-10-Private%20cluster 
// https://github.com/Koenkk/zigbee2mqtt/discussions/7608#discussioncomment-1614827
/*
ID	    Name	                        Data type	    Range	        Defualt value
0xFC00	Level control max min	        uint16 -0x21    0x0000 - 0xffff	0x01ff
0xFC02	Level control bulb type	        uint8 -0x20	    0x00–0xFF	    0x00
0xFC03	Level control scr state	        uint8 -0x20	    0x00–0xFF	    0x01
0xFC04	Level control current percentage uint8 -0x20	0x00–0xFF	    0x01
0xFC05	Level control min percentage	uint8 -0x20	    0x00–0xFF	    0x01
*/


/*
// https://github.com/jonnylangefeld/home-assistant/blob/0a947da5d38421947b9945420ac96d467b6c3392/deps/zhaquirks/tuya/ts110e.py
TUYA_LEVEL_ATTRIBUTE = 0xF000            // (uint16) # 0xF000 reported values are 10-1000, must be converted to 0-254  => value = (value + 4 - 10) * 254 // (1000 - 10) 
//                                          # convert dim values to 10-1000 =>             brightness = args[0] * (1000 - 10) // 254 + 10
TUYA_BULB_TYPE_ATTRIBUTE = 0xFC02        // (enum8)    LED = 0x00    INCANDESCENT = 0x01    HALOGEN = 0x02
TUYA_MIN_LEVEL_ATTRIBUTE = 0xFC03        // (uint16)
TUYA_MAX_LEVEL_ATTRIBUTE = 0xFC04    	 // (uint16)
TUYA_CUSTOM_LEVEL_COMMAND = 0x00F0       // 
*/

/*
https://developer.tuya.com/en/docs/iot/tuya-zigbee-lighting-access-standard?id=K9ik6zvod83fi
https://developer.tuya.com/en/docs/iot/tuya-zigbee-lighting-dimmer-swith-access-standard?id=K9ik6zvlvbqyw


_TZE200_1agwnems  ->  check https://gist.github.com/Koenkk/9c36da807c3768a85aaed9a352c74e96  !!! https://community.hubitat.com/t/zigbee-dimmer-pairing-issues/142460/5?u=kkossev 
                            https://github.com/Koenkk/zigbee2mqtt/issues/13800#issuecomment-1247578685
                            https://github.com/Koenkk/zigbee2mqtt/issues/20098
                            https://github.com/Koenkk/zigbee2mqtt/issues/21791 
                            https://github.com/zigpy/zha-device-handlers/issues/2278#issuecomment-1472866621
SmartThings : https://drive.google.com/file/d/1eqLByV3P_9wEDrZV9bQvIWGg2MIT5ySB/view

*/
