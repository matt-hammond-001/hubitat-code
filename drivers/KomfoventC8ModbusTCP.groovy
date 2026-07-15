/**
 * =============================================================================
 * 
 *  ModBus TCP Driver for Komfovent C8 controller for MVHR systems
 *
 *  Version 0.0.1
 *
 *  (c) 2026 Matt Hammond / matthammond.org
 * 
 *  https://github.com/matt-hammond-001/hubitat-code
 *
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
*/
#include matthammonddotorg.promise
#include matthammonddotorg.modbusTcpClient

import groovy.transform.Field 

@Field static enumOnOff = modbus_enumMap(0:"off",1:"on") 

@Field static enumEnabled = modbus_enumMap(0:"disabled",1:"enabled")

@Field static enumModes = modbus_enumMap(
    0:"standby", 
    1:"away",
    2:"normal",
    3:"intensive",
    4:"boost",
    5:"kitchen",
    6:"fireplace",
    7:"override",
    8:"holiday",
    9:"air quality",
    10:"off",
)

@Field static enumSchedulerOperationModes = modbus_enumMap(
	0:"Stay at Home",
    1:"Working week",
    2:"Office",
    3:"Custom"    
)

@Field static enumAutoModes = modbus_enumMap(
	0: "scheduling", 1: "air quality"
)

@Field static enumAirQualitySensorType = modbus_enumMap(
    0: "None",
    1: "CO2",
    2: "VOC",
    3: "RH",
)

@Field static enumSensor = modbus_enumMap(
    0: "None",
    1: "Sensor",
)

metadata { 
    definition (
        name: "Komfovent C8 Modbus TCP", 
        namespace: "matthammonddotorg", 
        author: "Matt Hammond",
        description: "Control of a Komfovent C8 controller for an MVHR, via Modbus TCP",
    ) {
        capability 'Initialize'
		capability 'Refresh'
        capability 'FilterStatus'
        capability "TemperatureMeasurement"
        
        command "disconnect"
        command "setPower", [[name:"Power", type:"ENUM", description:"Turn on/off the whole unit", constraints:enumOnOff.values]]
        command "setMode", [[name:"Mode", type:"ENUM", description:"Set mode", constraints:["away","normal","intensive","boost","auto","auto off"]]]
        command "setAuto", [[name:"Auto", type:"ENUM", description:"Set auto on/off", constraints:["off","on"]]]
        command "setEco", [[name:"State", type:"ENUM", description:"Turn on/off eco mode", constraints:enumOnOff.values]]
        command "setAutoCtrlMode", [[name:"AUTO Control mode", type:"ENUM", description:"Which control style is used for AUTO mode", constraints:enumAutoModes.values]]
        
        command "configureMode", [
            [ name:"mode", description:"Mode to configure", type:"ENUM", constraints:["away","normal","intensive","boost"]],
            [ name:"supplyFlowRate", description:"Target flow rate (m3/h)", type:"NUMBER" ],
            [ name:"extractFlowRate", description:"Target flow rate (m3/h)", type:"NUMBER" ],
            [ name:"temperatureSetpoint", description:"Target temperature (°C)", type:"NUMBER" ],
            [ name:"enableHeating", description:"Enable heating", type:"ENUM", constraints:enumOnOff.values ],
            [ name:"humiditySetpoint", description:"Target humidity (%RH)", type:"NUMBER" ],
        ]
        
        command "configureAirQuality", [
         	[ name:"sensorBasedControl", description:"Sensor based control when in AUTO", type:"ENUM", constraints:enumEnabled.values],
         	[ name:"temperatureSetpoint", description:"Temperature (°C)", type:"NUMBER"],
         	[ name:"airQualityAirQualitySetpoint", description:"CO2 (ppm) or VOC (%)", type:"NUMBER"],
         	[ name:"humiditySetpoint", description:"Humidity (%RH)", type:"NUMBER"], 
         	[ name:"minimumIntensivity", description:"Minimum intensivity when in AUTO (%)", type:"NUMBER"],
         	[ name:"maximumIntensivity", description:"Maximum intensivity when in AUTO (%)", type:"NUMBER"],
         	[ name:"heating", description:"Allow heating when in AUTO", type:"ENUM", constraints:enumOnOff.values],
         	[ name:"airQualityPollHours", description:"Polling frequency (hours)", type:"NUMBER"],
         	[ name:"airQualitySensorType", description:"What type of sensor is installed?", type:"ENUM", constraints:enumAirQualitySensorType.values],
         	[ name:"airQualityHumidityControl", description:"Humidity based control when in AUTO", type:"ENUM", constraints:enumEnabled.values],
         	[ name:"airQualityOutdoorHumidity", description:"Is there an outdoor humidity sensor?", type:"ENUM", constraints:enumSensor.values],
        ] 
                
        attribute "tileStatus", "string"
        
        attribute "onOffStatus", "enum", enumOnOff.values
        attribute "auto", "enum", enumOnOff.values
        attribute "autoModeCtrl", "enum",enumAutoModes.values
        attribute "eco", "enum", enumOnOff.values
        attribute "currentMode", "enum", enumModes.values
        attribute "schedulerOperationMode", "enum", enumSchedulerOperationModes.values
        attribute "filterImpurity", "number"
        attribute "supplyFlow", "number"
        attribute "supplyFan", "number"
        attribute "extractFlow", "number"
        attribute "extractFan", "number"
        attribute "filterImpurity", "number"
        attribute "heatExchanger", "number"
        
        attribute "firmwareVersion", "string"
        attribute "panel1FirmwareVersion", "string"
        attribute "panel2FirmwareVersion", "string"

        attribute "airQualityEnable", "enum", enumEnabled.values
        attribute "airQualityTemperatureSetpoint", "number"
        attribute "airQualityHumiditySetpoint", "number"
        attribute "airQualityMinimumIntensivity", "number"
        attribute "airQualityMaximumIntensivity", "number"
        attribute "airQualityHeating", "enum", enumOnOff.values
        attribute "airQualityPollHours", "number"
        attribute "airQualitySensorType", "enum", enumAirQualitySensorType.values
        attribute "airQualityHumidityControl", "enum", enumEnabled.values
        attribute "airQualityOutdoorHumidity", "enum", enumSensor.values
        
    } 
   
    preferences { 
        input(type: 'string', name: 'IP', title: 'Unit IP Address', description: '(IPv4 address in form of 192.168.1.45)', required: true)
        input(type: 'number', name: 'PORT', title: 'Unit Port', description: '(IPv4 port)', required: true, defaultValue: 502)

        input(type: 'number', name: 'statusPollInterval', title: 'Polling interval: status', description: 'Polling interval (seconds)', required: true, defaultValue:2)
        input(type: 'number', name: 'monitoringPollInterval', title: 'Polling interval: monitoring', description: 'Polling interval (seconds)', required: true, defaultValue:5)
        input(type: 'number', name: 'otherPollInterval', title: 'Polling interval: other settings/config', description: 'Polling interval (seconds)', required: true, defaultValue:30)
        input(type: 'number', name: 'filterReplaceThreshold', title: 'Filter replacement threshold', description: 'Impurity level (%)', required: true, defaultValue:75, range:"5..100")
        
        input "temperatureSource", "enum", title:"Which source to use for temperature attribute of MVHR", defaultValue:"Outdoors", required:true, options:["Outdoors","Extract","Supply","Panel 1","Panel 2"]
        
        input "debugEnable", "bool", title: "Enable debug logging", defaultValue:false, required: true
        input "traceEnable", "bool", title: "Enable trace logging", defaulValue:false, required: true

        input "modbusLogLevel", "enum", title:"Logging level for modbus_client", defaultValue:"warn", required:true, options:["trace","debug","info","warn","error"]
    }

}

/*
-----------------------------------------------------------------------------
Logging output
-----------------------------------------------------------------------------
*/

def logDebug(msg) {
    if (settings.debugEnable) {
        log.debug "${device.displayName}: ${msg}"
    }
}

def logTrace(msg) {
    if (settings.traceEnable) {
        log.trace "${device.displayName}: ${msg}"
    }
}

def logInfo(msg) {
    if (settings.debugEnable) { 
        log.info "${device.displayName}: ${msg}"
    }
}

def logError(msg) {
    log.error "${device.displayName}: ${msg}"
}

def logWarn(msg) {  
    log.warn "${device.displayName}: ${msg}"
}

/*
-----------------------------------------------------------------------------
Main functions
-----------------------------------------------------------------------------
*/
    
def installed() {
    state.clear()
}

def configure() {
}

def initialize() {
    state.clear()
	doInitialize()
}

def updated() {
	doInitialize()
}

def refresh() {
    pollStatus()
    pollMonitoring()
    pollOtherConfig()
    return
}

def uninstalled() { 
	unschedule()
    modbus_disconnect()
}
 
 
/* 
-----------------------------------------------------------------------------
Implementations
-----------------------------------------------------------------------------
*/ 

def doInitialize() {
    unschedule()
    
    modbus_setLogging(settings.modbusLogLevel)
    
    modbus_connect(settings.IP, settings.PORT,
		requestTimeoutSecs: 4,
		maxConcurrentRequests: 1,
		keepAliveIntervalSecs: 10,
		autoReconnect: true,
		reconnectDelaySecs: 1
    )
    
    refresh()
}

def keepAlive() { 
    return modbus_readHoldingRegister(1, format:"0B")
}

/*
-----------------------------------------------------------------------------
Regular polling/reading status, config etc
-----------------------------------------------------------------------------
*/ 

def pollStatus() {
    logDebug("Polling status")
    modbus_readHoldingRegisters(1, 6, format:"0B")
    .then { onOffStatus, autoModeCtrl, ecoMode, autoMode, currentMode, schedOpMode ->
        logDebug "onOff=${onOffStatus} autoModeCtrl=${autoModeCtrl} ecoMode=${ecoMode} autoMode=${autoMode} currentMode=${currentMode} schedOpMode=${schedOpMode}"
        sendEvent name:"onOffStatus", value:enumOnOff.fromKey[onOffStatus]
        sendEvent name:"autoModeCtrl", value:enumAutoModes.fromKey[autoModeCtrl]
        sendEvent name:"eco", value:enumOnOff.fromKey[ecoMode]
        sendEvent name:"auto", value:enumOnOff.fromKey[autoMode]
		sendEvent name:"currentMode", value:enumModes.fromKey[currentMode]
        sendEvent name:"schedulerOperationMode", value:enumSchedulerOperationModes.fromKey[schedOpMode]
    } 
    .catch { e ->
        logError "Error reading status: ${e}"
    }
    .then {
        runIn(settings.statusPollInterval, "pollStatus")
    }
}


def getBits(v, offset, n) {
    return (v>>offset) & ((1<<n)-1)
}

        
def pollMonitoring() {
    logDebug("Polling monitoring")
    
    Parallel(
        modbus_readHoldingRegisters(901, 18, format:"0BssssIISSSSSSs0B0B")
        .then { heatCoolConfig, supplyTemp, extractTemp, outdoorTemp, waterTemp,
            supplyFlow, extractFlow, supplyFanPct, extractFanPct,
            heatX, eHeater, wHeater, wCooler, dxUnit,
            filterImp, airDampers ->
                logDebug "Received detailed monitoring"
                updateChild "Supply", 
                    [name:"temperature", value:(supplyTemp as float)/10, unit:"°C"],
                    [name:"flowRate", value:supplyFlow, unit:"m3/h"],
                    [name:"fanSpeed", value:supplyFanPct/10, unit:"%"]
                updateChild "Extract",
                    [name:"temperature", value:(extractTemp as float)/10, unit:"°C"],
                    [name:"flowRate", value:extractFlow, unit:"m3/h"],
                    [name:"fanSpeed", value:extractFanPct/10, unit:"%"]
                updateChild "Outdoors",  [name:"temperature", value:(outdoorTemp as float)/10, unit:"°C"]
                sendEvent name:"filterImpurity", value:filterImp, unit:"%"
                sendEvent name:"filterStatus", value:(filterImp >= settings.filterReplaceThreshold) ? "replace":"normal"
	            sendEvent name:"heatExchanger", value:heatX/10, unit:"%"
            logDebug "heatExchanger = ${heatX/10}"
        } 
        .catch { e -> logError "Error reading monitoring.detailed: ${e}" },
        
        modbus_readHoldingRegisters(946, 9, format:"s0bSs0bS00000B")
        .then { p1Temp, p1RH, p1AQ, p2Temp, p2RH, p2AQ, panels ->
                logDebug "Received panels monitoring"
            if (panels & 1) {
                updateChild "Panel 1",
                    [name:"temperature", value:(p1Temp as float)/10, unit:"°C"],
                    [name:"humidity", value:p1RH, unit:"%"]
            }
            if (panels & 2) {
                updateChild "Panel 2",
                    [name:"temperature", value:(p2Temp as float)/10, unit:"°C"],
                    [name:"humidity", value:p2RH, unit:"%"]
            }
        }
        .catch { e -> logError "Error reading monitoring.panels: ${e}" },
    )
    .then {
        updateStatusTile()
    }
    .catch { e ->
        logError "Error reading monitoring: ${e}"
   	}
    .then {
        runIn(settings.monitoringPollInterval, "pollMonitoring")
   	}
}


def updateStatusTile() {
    def p1 = fetchChild("Panel 1", false)
    def p2 = fetchChild("Panel 2", false)
    def p = p1 ?: p2
    def s = fetchChild("Supply", false)
    def e = fetchChild("Extract", false)
    def o = fetchChild("Outdoors", false)
    sendEvent(
        name:"tileStatus",
        value:
			"""<svg preserveAspectRatio=xMidYMid,meet xmlns="http://www.w3.org/2000/svg" viewBox=0,0,200,300 >""" +
        """<style>text{font:15px sans-serif;text-anchor:middle;fill:#fff}.r{fill:#f88}</style>""" +
            """<path fill=#333 stroke=#ddd stroke-width=4 d=m100,20,90,80v190H10V100z />""" +
            """<path fill=#68f d=m42,38,60,60-4,4-60-60m66,50,44-44-4-4,16-4-4,16-4-4-44,44 />""" +
            """<path fill=#f84 d=m102,98,44,44,4-4,2,14-14-2,4-4-44-44m-50,46,44-44,4,4-44,44 />""" +
            """<path fill=#fff0 stroke=#888 stroke-width=2 d=M60,80h80v40H60z${p1==null?'':'M47,216h16v22H47z'}${p2==null?'':'M137,216h16v22h-16z'} />""" +
//            """<text x=38 y=12>999 %RH</text>""" +                                      // outdoor humidity - not yet supported
            """<text x=38 y=30>${o.currentValue("temperature")?:"---"} °C</text>""" +     // outdoor temperature
            """<text x=54 y=172>${e.currentValue("temperature")?:"---"} °C</text>""" +    // extract temperature
            """<text x=54 y=190>${e.currentValue("flowRate")?:"---"} m³/h</text>""" +
            """<text x=146 y=172>${s.currentValue("temperature")?:"---"} °C</text>""" +
            """<text x=146 y=190>${s.currentValue("flowRate")?:"---"} m³/h</text>""" +
            (p1==null ? "" : (
                """<text x=55 y=232>1</text>""" +
                """<text x=55 y=254>${p1.currentValue("temperature")?:"---"} °C</text>""" +
                """<text x=55 y=272>${p1.currentValue("humidity")?:"---"} %RH</text>"""
            )) +
            (p2==null ? "" : (
                """<text x=145 y=232>2</text>""" +
                """<text x=145 y=254>${p2.currentValue("temperature")?:"---"} °C</text>""" +
                """<text x=145 y=272>${p2.currentValue("humidity")?:"---"} %RH</text>"""
            )) +
	        """<text x=100 y=66${device.currentValue("filterStatus")=="replace"?' class=r ':''}>▨ ${device.currentValue("filterImpurity")?:"---"}%</text>""" +
            """<text x=100 y=146>♻︎ ${device.currentValue("heatExchanger")?:"---"}%</text>""" +
            "</svg>"
    )
}

def pollOtherConfig() {
    logDebug("Polling other")

    Parallel(
        modbus_readHoldingRegisters(1000,6, format:"III")
        .then { rawFw, rawP1Fw, rawP2Fw -> 
            sendEvent name:"firmwareVersion", value:"${getBits(rawFw,28,4)}.${getBits(rawFw,24,4)}.${getBits(rawFw,20,4)}.${getBits(rawFw,12,8)}.${getBits(rawFw,0,12)}"
            sendEvent name:"panel1FirmwareVersion", value:"${getBits(rawP1Fw,24,8)}.${getBits(rawP1Fw,20,4)}.${getBits(rawP1Fw,12,8)}.${getBits(rawP1Fw,0,12)}"
            sendEvent name:"panel2FirmwareVersion", value:"${getBits(rawP2Fw,24,8)}.${getBits(rawP2Fw,20,4)}.${getBits(rawP2Fw,12,8)}.${getBits(rawP2Fw,0,12)}"
        }
        .catch { e -> logError "Error reading firmware info: ${e}" },
        
	    modbus_readHoldingRegisters(205, 12, format:"0BsSS0B0B0B0B0B000B0B")
        .then { airqEnable, tempSetpoint, airqSetpoint, rhSetpoint, airqMinIntensivity, airqMaxIntensivity, airqHeat, airqPollHours, airqSensor, rhEnable, outdoorRhSensor ->
            sendEvent name:"airQualityEnable", value:enumEnabled.fromKey[airqEnable]
            sendEvent name:"airQualityTemperatureSetpoint", value:(tempSetpoint as float)/10
            sendEvent name:"airQualityAirQualitySetpoint", value:airqSetpoint
            sendEvent name:"airQualityHumiditySetpoint", value:rhSetpoint
            sendEvent name:"airQualityMinimumIntensivity", value: airqMinIntensivity
            sendEvent name:"airQualityMaximumIntensivity", value: airqMaxIntensivity
            sendEvent name:"airQualityHeating", value:enumOnOff.fromKey[airqHeat]
            sendEvent name:"airQualityPollHours", value:airqPollHours, unit:"hours"
            sendEvent name:"airQualitySensorType", value:enumAirQualitySensorType.fromKey[airqSensor]
            sendEvent name:"airQualityHumidityControl", value:enumEnabled.fromKey[rhEnable]
            sendEvent name:"airQualityOutdoorHumidity", value:enumSensor.fromKey[outdoorRhSensor]
        }
        .catch { e -> logError "Error reading air quality config: ${e}" },
    )
    .catch { e ->
        logError "Error reading config/other: ${e}"
   	}
	.then {
        runIn(settings.otherPollInterval, "pollOtherConfig")
   	}
}


def disconnect() {
    modbus_disconnect()
}

def modbus_connected() {
	logDebug "Connected"
}

def modbus_disconnected() {
	logDebug "Disconnected"
}

/*
-----------------------------------------------------------------------------
Commands
-----------------------------------------------------------------------------
*/

def setPower(onOff) {
    modbus_writeHoldingRegister(1, enumOnOff.fromValue[onOff], format:"0b")
    .catch { e ->
        logError "Error setting power to ${mode}: ${e}"
    }
}

def setMode(mode) {
    switch (mode) {
        case "auto":
        	setAuto("on")
       		break
        case "auto off":
        	setAuto("off")
       		break
        default:
		    modbus_writeHoldingRegister(5, enumModes.fromValue[mode], format:"0b")
    }
}

def setAuto(onOff) {
	modbus_writeHoldingRegister(4, (onOff=="on")?1:0, format:"0b")
}

def setEco(onOff) {
    modbus_writeHoldingRegister(3, enumOnOff.fromValue[onOff], format:"0b")
}

def setAutoCtrlMode(mode) {
    logDebug "setAutoCtrlMode(${mode} - ${enumAutoModes.fromValue[mode]})"
	boolean isAirQ = enumAutoModes.fromValue[mode];
    modbus_writeHoldingRegister(205, isAirQ?1:0, format:"0b")
    modbus_writeHoldingRegister(205, isAirQ?1:0, format:"0b")
}

def configureMode(mode, supplyFlow=null, extractFlow=null, heatingSetpoint=null, heatingEnable=null, humiditySetpoint=null) { 
    logDebug "configureMode(${mode}, ${supplyFlow}, ${extractFlow}, ${heatingSetpoint}, ${heatingEnable}, ${humiditySetpoint})"
    
    int r1, r2;
    switch (mode) {
        case "away":
        	r1=100; r2=159
	        break
        case "normal":
        	r1=106; r2=160
	        break
        case "intensive":
        	r1=112; r2=161
	        break
        case "boost":
        	r1=118; r2=162
	        break
		default:
            logError("configureMode - Mode not recognised: ${mode}")
            return
    }

    if (supplyFlow != null) {
        logDebug "Setting ${mode} ${r1+0} supply flow to ${supplyFlow}"
        modbus_writeHoldingRegisters(r1 + 0, 2, supplyFlow, format:"I")
        logDebug "Done"
    }
    if (extractFlow != null) {
        modbus_writeHoldingRegisters(r1 + 2, 2, format:"I", extractFlow)
    }
    if (heatingSetpoint != null) {
        modbus_writeHoldingRegister(r1 + 4, format:"S", (heatingSetpoint*10) as int)
    }
    if (heatingEnable != null) {
        modbus_writeHoldingRegister(r1 + 5, format:"0B", enumOnOff.fromValue[heatingEnable])
    }
    if (humiditySetpoint != null) {
        modbus_writeHoldingRegister(r2, format:"0B", (humiditySetpoint*10) as int)
    }
}
        
def configureAirQuality(useSensor=null, temperatureSetpoint=null, airqSetpoint=null, humiditySetpoint=null, minIntensivity=null, maxIntensivity=null, heating=null, pollHours=null, sensorType=null, useHumidity=null, outdoorSensor=null) {
    logDebug "configureAirQuality(${useSensor}, ${temperatureSetpoint}, ${airqSetpoint}, ${humiditySetpoint}, ${minIntensivity}, ${minIntensivity}, ${heating}, ${pollHours}, ${sensorType}, ${useHumidity}, ${outdoorSensor})"

    Parallel(
        { if (useSensor != null)
            return modbus_writeHoldingRegister(205, format:"0B", enumEnabled.fromValue[useSensor])
        }, 
        { if (temperatureSetpoint != null)
            return modbus_writeHoldingRegister(206, format:"s", constrainRange(temperatureSetpoint*10, 50, 400) as int)
        },
        { if (airqSetpoint != null)
            return modbus_writeHoldingRegister(207, format:"S", constrainRange(airqSetpoint0,2000))
        },
        { if (humiditySetpoint != null)
         return modbus_writeHoldingRegister(208, format:"S", constrainRange(humiditySetpoint,10,90))
        },
        { if (minIntensivity != null)
            return modbus_writeHoldingRegister(209, format:"0B", constrainRange(minIntensivity,20,100))
        },
        { if (maxIntensivity != null)
            return modbus_writeHoldingRegister(210, format:"0B", constrainRange(maxIntensivity,20,100))
        },
        { if (heating != null)
            return modbus_writeHoldingRegister(211, format:"0B", enumOnOff.fromValue[heating])
        },
        { if (pollHours != null)
            return modbus_writeHoldingRegister(212, format:"0B", constrainRange(pollHours,1,24))
        },
        { if (sensorType != null)
            return modbus_writeHoldingRegister(213, format:"0B", enumAirQualitySensorType.fromValue[sensorType])
        },
        { if (useHumidity != null)
            return modbus_writeHoldingRegister(215, format:"0B", enumEnabled.fromValue[useHumidity])
        },
        { if (outdoorSensor != null)
            return modbus_writeHoldingRegister(216, format:"0B", enumSensor.fromValue[outdoorSensor])
        },
    )
    .then { logDebug "configureAirQuality() ... success" }
    .catch { e -> logError "Error configuring air quality settings: ${e}" }
    .then {
        pollOtherConfig()
    }.then { throw new Exception("check if unhandled exception is detected and reported") }
}

/*
-----------------------------------------------------------------------------
Child devices
-----------------------------------------------------------------------------
*/

def fetchChild(String childName, boolean create=true) {
	String thisId = device.id
    def cd = getChildDevice("${thisId}-${childName}")
    if (!cd && create) {
		String deviceType
        List<Map> defaultValues = []
        switch (childName) {
            case "Panel 1":
            case "Panel 2":
            	namespace = "hubitat"
            	deviceType = "Generic Component Temperature Humidity Sensor"
            	break
            
            case "Supply":
            case "Extract":
            	namespace = "matthammonddotorg"
            	deviceType = "Generic Component MVHR Pathway"
            	break
            
            case "Outdoors":
            	namespace = "hubitat"
            	deviceType = "Generic Component Temperature Sensor"
            	break
            
            default:
                logError("fetchChild() unrecognised name: ${childName}")
        }
        if (deviceType) {
            cd = addChildDevice(namespace, deviceType, "${thisId}-${childName}", [name: "${device.displayName} ${childName}", isComponent: true])
            cd.parse(defaultValues)
        }
    }
    return cd 
}

def updateChild(String name, ...updates) {
    def child = fetchChild(name)
    child.parse updates.collect {
        if (it.name == "temperature" && name == settings.temperatureSource) {
            logTrace "Updating MVHR temperature as that of ${name} : ${it}"
            sendEvent(*:it)
        }
        return [descriptionText:"${child.displayName} ${it.name} is ${it.value}", *:it]
    }
}
    
void componentRefresh(cd){
	def childName = cd.deviceNetworkId.substring("${device.id}-".size())
    logDebug("received refresh request from ${childName}")
    switch (childName) {
     	case "Panel 1":
        case "Panel 2":
        case "Supply":
        case "Extract":
        case "Outdoors":
        	pollMonitoring()
        	break
    }
}

def constrainRange(v, lo, hi) {
    if (v<lo) return lo
    if (v>hi) return hi
    return v
}

