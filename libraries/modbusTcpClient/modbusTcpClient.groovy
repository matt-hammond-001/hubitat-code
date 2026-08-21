/*
-----------------------------------------------------------------------------
This code is licensed as follows:

BSD 3-Clause License

Copyright (c) 2026, Matt Hammond
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

library (
	author: "Matt Hammond",
	category: "modbus",
	description: "modbus/tcp client library (for drivers only)",
	documentationLink: "",
	name: "modbusTcpClient",
	namespace: "matthammonddotorg"
)


/**
-------------------------
modbus/tcp client library
-------------------------

This library implements a simple modbus/tcp client. Features:

* supported MODBUS functions:
	reading/writing single/multiple coils, discretes, input registers and holding registers.

* Promise-style API for handling responses to requests or failed requests

* Byte packing/unpacking via format specifier strings to reading/writing complex register structures

* Automatic reconnection and keep alive behaviours

* Configurable limit for maximum number of modbus requests in-flight to avoid overloading servers that cannot handle concurrent requests.

USAGE:
You must also include the promise library:

    #include matthammonddotorg.promise
    #include matthammonddotorg.modbusTcpClient

API:

Initialisation, connecting and disconnecting:

	modbus_setLogging(String level)
		where level is one of "trace", "debug", "info", "warn" (default), "error"

	modbus_connect(HOSTIP, PORT, *:options)
		where Options are:
			requestTimeoutSecs:N,      // time before request is considered timed out. NOTE this includes time spent queued waiting to be sent
			maxConcurrentRequests: 1,  // requests will be queued so that no more that this many concurrent requests are in flight (throttles rate of requests by queueing)
			keepAliveIntervalSecs: 10, // default is 0 meaning no keep alives
			keepAliveMaxFailures: 3,   // default is 3 meaning if 3 keepalives fail consecutively, then force a reconnect
			autoReconnect: true,       // automatically reconnect if connection is list
			reconnectDelaySecs: 1,     // how long to wait before automatically reconnecting
			unitId: 1,                 // default unit-id in all requests (unless overridden by specifying unitId in a request,
			minMillisBetweenRequests: 500, // minimum milliseconds to leave between sending requets to the server (throttles rate of requests by queuing)

	modbus_reconnect()
		force a reconnect

	modbus_disconnect()
		force disconnection


Reading/writing registers:

	modbus_readCoil(REGNUM, *:opts)
	.then { value -> ... }
	.catch { err -> ... }

	modbus_readCoils(REGNUM, QTY, *:opts)
	.then { values -> ... }
	.catch { err -> ... }

	modbus_readDiscrete(REGNUM, *:opts)
	.then { value -> ... }
	.catch { err -> ... }

	modbus_readDiscretes(REGNUM, QTY, *:opts)
	.then { values -> ... }
	.catch { err -> ... }

	modbus_readInput(REGNUM, format:"s", *:opts)
	.then { values -> ... }
	.catch { err -> ... }

	modbus_readInputs(REGNUM, QTY, format:"s", *:opts)
	.then { values -> ... }
	.catch { err -> ... }

	modbus_readHoldingRegister(REGNUM, format:"s", *:opts)
	.then { values -> ... }
	.catch { err -> ... }

	modbus_readHoldingRegisters(REGNUM, QTY, format:"s", *:opts)
	.then { values -> ... }
	.catch { err -> ... }

	modbus_writeCoil(REGNUM, value, *:opts)
	.then { ... }
	.catch { err -> ... }

	modbus_writeCoils(REGNUM, ...values, *:opts)
	.then { ... }
	.catch { err -> ... }

	modbus_writeHoldingRegister(REGNUM, format:"s", ...values, *:opts)
	.then { ... }
	.catch { err -> ... }

	modbus_writeHoldingRegisters(REGNUM, QTY, format:"s", ...values, *:opts)
	.then { ... }
	.catch { err -> ... }

All request methods support the following optional keyword options:

   unitId = specify the slave unit ID to use in the request to the server. Defaults to what was specified as an option when modbus_connect() was called


There is a lower level API - to write your own custom request and handle the PDU response yourself:

	modbus_request(byte[] pdu, *:opts)
	.then { byte[] responsePdu -> ... }
	.catch { err -> ... }


METHODS YOU MAY OPTIONALLY IMPLEMENT the following event handling methods:

	void modbus_connected() { ... }
	void modbus_disconnected() { ... }

	void keepAlive() {
		your implementation of this function should return a request promise
		that can be used as a "ping" to the modbus server - e.g. reading a single simple register
		if keepAliveIntervalSecs > 0 then this will be called to help keep the connection alive	
	}

Other useful methods in the API:

	Retry(N, Closure taskThatReturnsPromise)
		Runs a task up to N times until it succeeds.
		Returns a promise that resolves on success, or rejects if the action failed N times.

		Provide the task as a Closure that returns a promise-like object - e.g. return a modbus_request() or modbus_readXXX() or modbus_writeXXX()

	Parallel(promise or closure, promise or closure, ...)
		Runs one or more promises in parallel. Returns a promise that resolves once all the promises have resolved, or rejects as soon as one has rejected
		You can also pass closures that simply return a value or throws an exception


FORMAT SPECIFIERS

	Methods for reading/writing input registers and holding registers support a format specifier for
    unpacking or packing the bytes to a set of fields.

	The format specifier string describes the data layout when packing or unpacking a sequence of bytes
	to be read/written for one or more consecutive registers. It comprises format characters that specify
    the types of the data fields being packed or unpacked. Each can have an optional prefix specifying
    the endian-ness of that and subsequent fields. The default is big-endianness.

    Character     Meaning
      >             big-endian for subsequent fields (default)
      !             big-endian synonym
      <            little-endian for subsequent fields

      0             padding byte (no field - ignores when reading, 0x00 when writing)
      1             padding byte (no field - ignores when reading, 0xff when writing)

      b             signed byte field    ( 8 bits / half a register)
      B             unsigned byte field  ( 8 bits / half a register)
      s             signed short field   (16 bits / 1 register)
      S             unsigned short field (16 bits / 1 register)
      i             signed int field     (32 bits / 2 registers)
      I             unsigned int field   (32 bits / 2 registers)
      f             IEEE float field     (32 bits / 2 registers)

    If reading or writing multiple fields and the format specifier is shorter than the number of fields
    then it will be used on a loop.

	EXAMPLES:

		modbus_readHoldingRegisters(0, 4, format:"B0<sI")
		.then_spread { v1, v2, v3 -> 
			// v1 is an unsigned byte ("B")
			// the next 8 bits are ignored and not returned as an argument ("0")
			// v2 is a little-endian ("<") signed short ("s")
	        // v3 is a little-endian ("<") unsigned 32-bit int ("I")
		})

		modbus_writeHoldingRegisters(0, 4, format:"B0<sI", 253, -11325, 2000000)
		// writes this byte sequence: [253, 0, 195, 211, 128, 132, 30, 0]
        //                             byte 0  short---  long-----------

		modbus_writeHoldingRegisters(0, 4, format:"s", 16, -5000, -1)
        // writes this byte sequence: [0, 16, 236, 120, 255, 255 ]
        //                             short  short---  short---

*/
import java.lang.Math
import java.util.concurrent.ConcurrentHashMap
import java.util.Random
import groovy.transform.Field
import groovy.lang.Closure
import hubitat.helper.HexUtils

@Field static Map modbus_exceptions = [
	1: "Illegal Function",
    2: "Illegal Data Address",
    3: "Illegal Data value",
    4: "Server Device Failure",
    5: "Acknowledge",
    6: "Service Device Busy",
    8: "Memory Parity Error",
    0xa: "Gateway Path Unavailable",
    0xb: "Gateway Target Device Failed To Respond",
];

// ------------------------------------------------------------------------
// persistent state
// ------------------------------------------------------------------------

@Field static Map _modbus = new ConcurrentHashMap()

private Map _modbus_ensureData() {
    if ((device.id) in _modbus) {
        return _modbus[device.id]
    } else {
     	return _modbus[device.id] = [
            logLevels: [
                trace:false, debug:false, info:false, warn:true, error:true
            ],
            keepAliveFailCount: 0,
            minMillisBetweenRequests: 500,
            unsentRequests: [],
            transactions: [:],
            nextTransactionId: (now() & 0xffff),
            runIns: [:],
        ]
    }
}

// ------------------------------------------------------------------------
// helpers
// ------------------------------------------------------------------------

static Map modbus_enumMap(Map mappings) {
    return [
        fromKey: mappings,
        fromValue: mappings.collectEntries{ entry -> [entry.value, entry.key] },
        keys: mappings.keySet().collect { it },
        values: mappings.keySet().collect { it -> mappings[it] },
    ]
}

// ------------------------------------------------------------------------
// logging
// ------------------------------------------------------------------------

void modbus_setLogging(String level) {
    Map MB = _modbus_ensureData()
    def n = ["trace":0, "debug":1, "info":2, "warn":3, "error":4][level]
    if (n==null) {
        throw new Exception("modbus_setLogging() Unrecognised logging level: ${level}. Must be one of: trace, debug, warn, info, error")
    } else {
        MB.logLevels = [
            trace: n <= 0,
            debug: n <= 1,
            info:  n <= 2,
            warn:  n <= 3,
            error: n <= 4,
        ]
    }
}

void _modbus_logTrace(msg) {
    Map MB = _modbus_ensureData()
    if (!(MB?.logLevels) || MB.logLevels.trace)
    log.trace("modbus_client: ${msg}")
}

void _modbus_logDebug(msg) {
    Map MB = _modbus_ensureData()
    if (!(MB?.logLevels) || MB.logLevels.debug)
    	log.debug("modbus_client: ${msg}")
}

void _modbus_logInfo(msg) {
    Map MB = _modbus_ensureData()
    if (!(MB?.logLevels) || MB.logLevels.info)
    	log.info("modbus_client: ${msg}")
}

void _modbus_logWarn(msg) {
    Map MB = _modbus_ensureData()
    if (!(MB?.logLevels) || MB.logLevels.warn)
    	log.warn("modbus_client: ${msg}")
}

void _modbus_logError(msg) {
    Map MB = _modbus_ensureData()
    if (!(MB?.logLevels) || MB.logLevels.error)
    	log.error("modbus_client: ${msg}")
}


// ------------------------------------------------------------------------
// utils
// ------------------------------------------------------------------------

synchronized void _modbus_clearTimeout(methodName) {
    Map MB = _modbus_ensureData()
    def existingScheduledJob = MB.runIns.remove(methodName)
    if (existingScheduledJob != null) {
        cancelRunIn(existingScheduledJob)
    }
}

synchronized void _modbus_setTimeout(delaySecs, String methodName) {
    _modbus_setTimeoutMillis(delaySecs*1000, methodName)
}

synchronized void _modbus_setTimeoutMillis(delayMillis, String methodName) {
    Map MB = _modbus_ensureData()
    def cancelRes = new Exception("Nothing cancelled");
    def existingScheduledJob = MB.runIns.remove(methodName)
//    if (existingScheduledJob != null) {
//        cancelRes = cancelRunIn(existingScheduledJob)
//    }
    try {
        Map opts = [overwrite:true]
        if (delayMillis <= 0) {
            opts.misfire = 'ignore'
            delayMillis = 0
        }
        MB.runIns[methodName] = runInMillis(delayMillis, methodName, opts)
        _modbus_logDebug "runIn ${delaySecs} handle ${MB.runIns[methodName]}"
    } catch (e) {
        _modbus_logError "_modbus_setTimeoutMillis(${delayMillis}, ${methodName}) : ${e} : existingScheduledJob=${existingScheduledJob} cancelRunIn() returned ${cancelRes} runIns=${MB.runIns}"
    }
}


private byte[] _modbus_toUint16(v) {
    return [ (v >> 8) & 0xff, v & 0xff ]
}

private long _modbus_fromUint16(bytes, i) {
    return ((bytes[i] & 0xff) << 8) + (bytes[i+1] & 0xff)
}

def invoke(methodName, ...args) {
    try {
        return this."$methodName"(*args)
    } catch (MissingMethodException e) {
        _modbus_logDebug "invoke() exception caught: ${e}"
        if (e.getMethod() == methodName) {
            _modbus_logWarn "No ${methodName}() method"
	        return null
        } else {
	        throw e
        }
    } catch (e) {
        throw e
    }
}

// ------------------------------------------------------------------------
// socket connection
// ------------------------------------------------------------------------

void modbus_connect(Map options=[:], ip, port) {
    _modbus_clearTimeout "modbus_reconnect"

    Map MB = _modbus_ensureData()
    MB.ip                    = ip as String
    MB.port                  = port as int
    MB.unitId                = ((options?.unitId ?: 1) as int) & 0xff
    MB.requestTimeoutSecs    = Math.max(1, options?.requestTimeoutSecs ?: 10) as int
	MB.maxConcurrentRequests = Math.max(1, options?.maxConcurrentRequests ?: 1) as int
	MB.keepAliveIntervalSecs = Math.max(0, options?.keepAliveIntervalSecs ?: 0) as int
    MB.keepAliveMaxFailures  = Math.max(1, options?.keepAliveMaxFailures ?: 3) as int
	MB.autoReconnect         = (!!(options?.autoReconnect)) ?: true
    MB.reconnectDelaySecs    = Math.max(1, options?.reconnectDelaySecs ?: 1) as int
    MB.minMillisBetweenRequests = Math.max(0, options?.minMillisBetweenRequests ?: 0) as long
    
    MB.unsentRequests.clear()
    MB.transactions.clear()
	                       
    modbus_reconnect()
}

void modbus_reconnect() {
    _modbus_clearTimeout "modbus_reconnect"
    Map MB = _modbus_ensureData()
    if (MB?.ip == null || MB?.port == null) {
        throw new Exception("cannot call modbus_reconnect() before calling modbus_connect()")
    }
    modbus_disconnect()
    
    try {
     	interfaces.rawSocket.connect(MB.ip, MB.port, byteInterface:true)
        state.connected = true
        invoke "modbus_connected"
		_modbus_reset_keepAlive()
    } catch (e) {
		_modbus_logError "Error trying to connect: ${e}"            
    }
}

void modbus_disconnect() {
    try {
        _modbus_clearTimeout "modbus_reconnect"
        _modbus_stop_keepAlive()
        state.connected = false
     	interfaces.rawSocket.disconnect()
        invoke "modbus_disconnected"
    } catch (e) {
		_modbus_logError "Error trying to connect: ${e}"            
    }
}

    
void socketStatus(String message) {
    Map MB = _modbus_ensureData()
    _modbus_logError("socketStatus: ${message}")
	modbus_disconnect()
    if (message == 'receive error: String index out of range: -1') {
        // This is some error condition that repeats every 15ms.
        // Probably a bug in the rawsocket code.  Close the connection to prevent
        // the log being flooded with error messages.
        // Note: this may no longer be needed
    }
    else if (message == 'receive error: Read timed out') {
    }
    if (MB.autoReconnect) {
        _modbus_setTimeout(MB.reconnectDelaySecs, "modbus_reconnect")
    }
}


def _modbus_reset_keepAlive() {
    Map MB = _modbus_ensureData()
    MB.keepAliveFailCount = 0
    _modbus_clearTimeout("_modbus_do_keepAlive")
    if (MB.keepAliveIntervalSecs > 0) {
		_modbus_setTimeout(MB.keepAliveIntervalSecs, "_modbus_do_keepAlive")
    }    
}

def _modbus_stop_keepAlive() {
    _modbus_clearTimeout("_modbus_do_keepAlive")
}

def _modbus_do_keepAlive() {
    _modbus_logDebug "Calling keepAlive()"
    def promise = invoke "keepAlive";
    _modbus_logDebug "keepAlive() returned ${promise}"
    if (promise != null) {
	    promise.then { _modbus_logDebug "keepAlive():promise resolved ${it}" }
    	promise.catch { e -> 
		    _modbus_logDebug "keepAlive():promise rejected ${e}"
		    Map MB = _modbus_ensureData()
            MB.keepAliveFailCount += 1
	        _modbus_logDebug "keepAliveFailCount = ${MB.keepAliveFailCount}"
            if (MB.keepAliveFailCount > MB.keepAliveMaxFailures) {
	            _modbus_logWarn "Invoking reconnect due to keepalive failure"
	            _modbus_setTimeout(MB.reconnectDelaySecs, "modbus_reconnect")
            } else {
	            _modbus_logDebug "keepAlive try again : failCount=${MB.keepAliveFailCount} max=${MB.keepAliveMaxFailures}"
				_modbus_setTimeout(1, "_modbus_do_keepAlive")
            }
        }
    }
}

// ------------------------------------------------------------------------
// request and transaction queue housekeeping
// ------------------------------------------------------------------------

synchronized void modbus_process() {
    Map MB = _modbus_ensureData()
	long whenNext = Long.MAX_VALUE;
    
    _modbus_logDebug "Entering modbus_process(). unsentRequests=${MB.unsentRequests.size()} transactions=${MB.transactions.size()} connected=${state.connected}"

    // send queued requests, if connected and number in progress has not reached limit
    whenNext = Math.min(whenNext, _modbus_sendQueued())

    // cancel any timed out queued requests - e.g. because of too big a backlog or because not connected
    whenNext = Math.min(whenNext, _modbus_purgeQueued())
    
    // in flight transactions - cancell all if not connected, or individually if transaction has timed out
	whenNext = Math.min(whenNext, _modbus_purgeTransactions())
    
    if (whenNext < Long.MAX_VALUE) {
		long millis = Math.max(50, whenNext-now())
        _modbus_logDebug "Scheduling next modbus_process() in ${millis} ms. unsentRequests=${MB.unsentRequests.size()} transactions=${MB.transactions.size()} connected=${state.connected}"
        try {
		    _modbus_setTimeoutMillis(millis, "modbus_process")
        } catch (e) {
            _modbus_logError "modbus_process() error in _modbus_setTimeoutMillis(${millis},\"modbus_process\") : ${e}"
        }
    } else {
        _modbus_logDebug "Not scheduling another modbus_process()"
    }
}

long _modbus_sendQueued() {
    Map MB = _modbus_ensureData()
    long whenNext = Long.MAX_VALUE;
//    modbus_logTrace "_modbus_sendQueued() : 
    while (MB.transactions.size() < MB.maxConcurrentRequests && MB.unsentRequests.size() > 0) {
        whenNext = (state?.modbusLastTrySendEpoch ?: 0) + MB.minMillisBetweenRequests
        if (now() < whenNext) {
            break
        }
        Map request = MB.unsentRequests.pop()
        _modbus_logTrace "Preparing to send ${request}${request.context}"
        // create request
        // create transaction record
		int transactionId = MB.nextTransactionId
        MB.nextTransactionId = (MB.nextTransactionId + 1) & 0xffff

        _modbus_logTrace "Preparing to make ADU${request.context}: ${transactionId} ${request.pdu}"
		def adu = _modbus_make_adu(transactionId, request.pdu as List, request?.unitId );
        _modbus_logTrace "ADU: ${adu}"
        Map transaction = [
            id: transactionId as int,
            promise: request.promise,
            expires: request.expires,
            context: request.context,
            req: request,
        ]
        // send
		try {
	        _modbus_logDebug "Sending ADU${transaction.context}: ${adu}"
            state.modbusLastTrySendEpoch = now()
            state.modbusLastTrySendTime = new Date().toLocaleString()
            interfaces.rawSocket.sendMessage(adu)
            state.modbusLastSentEpoch = now()
            state.modbusLastSentTime = new Date().toLocaleString()
	        MB.transactions[transactionId as int] = transaction
		} catch (e) {
			transaction.promise.reject(e)
		}
    }
    return whenNext
}

def _modbus_make_adu(int transactionId, List pdu, Integer unitId=null) {
    return HexUtils.byteArrayToHexString([
        *_modbus_toUint16(transactionId),
        *_modbus_toUint16(0),    // protocol id
        *_modbus_toUint16(pdu.size() + 1), // size includes final byte of mbap header
        unitId ?: (_modbus_ensureData().unitId), // unit ID, defaulting to one specified when modbus_connect() called
        *pdu
    ] as byte[])
}


long _modbus_purgeQueued() {
    Map MB = _modbus_ensureData()
    long now = now()
    int i=0
    long lowestExpires = Long.MAX_VALUE
    while (i < MB.unsentRequests.size()) {
        Map unsentRequest = MB.unsentRequests[i]
        if (unsentRequest.expires < now) {
	        _modbus_logTrace "_modbus_purgeQueued purging ${i} ${unsentRequest}"
            MB.unsentRequests.remove(i)
			unsentRequest.promise.reject(new Exception("Request timed out"))
        } else {
            i += 1
            lowestExpires = Math.min(lowestExpires, unsentRequest.expires)
        }
    }
    return lowestExpires
}

long _modbus_purgeTransactions() {
    Map MB = _modbus_ensureData()
    long now = now()
    _modbus_logTrace "purgeTransactions() now=${now} transactions=${MB.transactions}"
    long lowestExpires = Long.MAX_VALUE
    for(int transactionId in MB.transactions.keySet()) {
		Map transaction = MB.transactions[transactionId as int]
        if (!transaction) {
            _modbus_logError "purgeTransactions() transactions[${transactionId}] = ${transaction} in ${MB.transactions}"
        } else {
            if (!state.connected || transaction.expires < now) {
                _modbus_logTrace "purgeTransactions() purging ${transactionId}${transaction.context} because expires=${transaction.expires}"
                MB.transactions.remove(transactionId as int)
                transaction.promise.reject(new Exception("Transaction${transaction.context} timed out. No response from server"))
            } else {
                lowestExpires = Math.min(lowestExpires, transaction.expires)
            }
        }
    }
    return lowestExpires
}



// ------------------------------------------------------------------------
// reading/writing registers
// ------------------------------------------------------------------------

def modbus_readCoil(Map opts=[:], int regNum) {
    return _modbus_readbits(1, regNum, 1, *:opts)
}

def modbus_readCoils(Map opts=[:], int regNum, int qty) {
    return _modbus_readbits(1, regNum, qty, *:opts)
}

def modbus_readDiscreteInput(Map opts=[:], int regNum) {
    return _modbus_readbits(2, regNum, 1, *:opts)
}

def modbus_readDiscreteInputs(Map opts=[:], int regNum, int qty) {
    return _modbus_readbits(2, regNum, qty, *:opts)
}

def modbus_readHoldingRegister(Map opts=[:], int regNum) {
    return _modbus_readRegisters(3, regNum, 1, *:opts)
}

def modbus_readHoldingRegisters(Map opts=[:], int regNum, int qty) {
    return _modbus_readRegisters(3, regNum, qty, *:opts)
}

def modbus_readInputRegister(Map opts=[:], int regNum) {
    return _modbus_readRegisters(4, regNum, 1, *:opts)
}

def modbus_readInputRegisters(Map opts=[:], int regNum, int qty) {
    return _modbus_readRegisters(4, regNum, qty, *:opts)
}

def modbus_writeCoil(Map opts=[:], int regNum, value) {
    return modbus_request([5, *_modbus_toUint16(regNum-1), value!=0 ? 0xff : 0, 0], *:opts)
    .then { def (pdu) = it // parse response
        expectFuncCode(5, pdu.bytes, pdu.offset)
        // don't bother checking rest of response
        return
    }
}

def modbus_writeCoils(Map opts=[:], int regNum, ...values) {
    byte[] bits = _modbus_packBits(values)
    return modbus_request([15, *_modbus_toUint16(regNum-1), *_modbus_toUint16(values.size()), bits.size(), *bits], *:opts)
    .then { pdu -> // parse response
        expectFuncCode(15, pdu.bytes, pdu.offset)
        // don't bother checking rest of response
        return
    }
}

def modbus_writeHoldingRegister(Map opts=[:], int regNum, value) {
    _modbus_logDebug "A ${value}"
    byte[] vs = _modbus_pack(opts?.format ?: "s", [value])
    _modbus_logDebug "B"
    int s = vs.size()
    if (s!=2) {
        throw new Exception("Format does not pack to 2 bytes.")
    } else {
        return modbus_request([6, *_modbus_toUint16(regNum-1), *vs] as byte[], *:opts)
        .then { pdu -> // parse response
         	expectFuncCode(6, pdu.bytes, pdu.offset)
            // don't bother checking rest of response
            return
        }
    }
}

def modbus_writeHoldingRegisters(Map opts=[:], int regNum, int qty, ...values) {
    byte[] vs = _modbus_pack(opts?.format ?: "s", values)
    int numBytes = vs.size()
    if (numBytes==0) {
        throw new Exception("No values to write")
    } else if ((numBytes&1)==1) {
        throw new Exception("Format packs to odd number of bytes (${numBytes}). Must be even when writing holding registers")
    } else { // numBytes == 2, 4, 6, 8, ...
     	return modbus_request([16, *_modbus_toUint16(regNum-1), *_modbus_toUint16(qty), numBytes, *vs] as byte[], *:opts)
        .then { pdu -> // parse response
            expectFuncCode(16, pdu.bytes, pdu.offset)
            // don't bother checking rest of response
            return
        }
    }
}

private def _modbus_readBits(Map opts=[:], int funcCode, intRegNum, int qty) {
    return modbus_request([funcCode, *_modbus_toUint16(regNum-1), *_modbus_toUint16(qty)] as byte[], *:opts)
    .then { pdu -> // parse response
        expectFuncCode(funcCode, pdu.bytes, pdu.offset)
        expect("Response byte count", ((qty+7)>>3), pdu.bytes[pdu.offset+1] & 0xff)
	    return _modbus_unpackbits(pdu.bytes, pdu.offset+2, qty)
    }
}

private def _modbus_readRegisters(Map opts=[:], int funcCode, int regNum, int qty) {
    Map MB = _modbus_ensureData()
    String format = opts.format ?: "s"
    return modbus_request([funcCode, *_modbus_toUint16(regNum-1), *_modbus_toUint16(qty)] as byte[], *:opts)
    .then { pdu -> // parse registers response
        expectFuncCode(funcCode, pdu.bytes, pdu.offset)
        expect("Response byte count", qty*2, pdu.bytes[pdu.offset+1] & 0xff)
        return _modbus_unpack(format, pdu.bytes, pdu.offset+2, qty*2)
    }
}

void expectFuncCode(okFuncCode, byte[] pdu, int i) {
    if (pdu[i] as int & 0x80) {
        int code = (pdu[i+1] as int)&0xff
        throw new Exception("Response func code ${pdu[i]} is error:  err code = ${code} (${modbus_exceptions[code]})")
    }
    if (pdu[i] != okFuncCode as byte) {
        throw new Exception("Expected response function code ${okFuncCode} but got ${pdu[i]}")
    }
}

void expect(String what, expectation, observed) {
    if (expectation != observed) {
        throw new Exception("Expected ${what} = ${expection}. But got: ${observed}")
    }
}



// ------------------------------------------------------------------------
// queuing requests and parsing responses
// ------------------------------------------------------------------------

synchronized def modbus_request(Map opts=[:], byte [] pdu) {
    Map MB = _modbus_ensureData()
    _modbus_logDebug ("modbus_request context=${opts?.context} ... ${opts?.context != null} ... ${(opts?.context!= null) ? " < ${opts?.context} >" : "<--no context-->"}")
    return Promise( {
	    state.modbusLastRequestEnqueuedEpoch = now()
		state.modbusLastRequestEnqueuedTime = new Date().toLocaleString()

        MB.unsentRequests.add([
            pdu: pdu,
            promise: it,
            unitId: opts?.unitId,
            expires: now() + MB.requestTimeoutSecs*1000,
            context: (opts?.context != null) ? " &lt;${opts?.context}&gt;" : "",
        ])
        try {
	        _modbus_setTimeoutMillis(0, "modbus_process")
        } catch (e) {
            _modbus_logError "modbus_request(${opts},${pdu}) error in _modbus_setTimeoutMillis(${millis},\"modbus_process\") : ${e}"
        }            
    })
}

def parse(message) {
	Map MB = _modbus_ensureData()
	_modbus_reset_keepAlive()
    _modbus_logDebug "parse(${message})"
    state.modbusLastCheckinEpoch = now()
	state.modbusLastCheckinTime = new Date().toLocaleString()
    
    try {
        byte[] msg = HexUtils.hexStringToByteArray(message)
		// assume message might contain more than one ADU
        int i = 0;
        while (i<msg.size()) {
            try {
	            i = _modbus_parse_adu(msg, i)
            } catch (e) {
                _modbus_logError("parse() msgloop: ${e}")
            }
        }
    } catch (e) {
        _modbus_logError("parse(): ${e}")
    }
    modbus_process();
}

int _modbus_parse_adu(byte[] msg, int i) {
    Map MB = _modbus_ensureData()
    
    if (msg.size() < 7) {
	    throw new Exception("ADU not long enough - only ${msg.size()} bytes")
	}
    
    int transactionId = _modbus_fromUint16(msg, i)
    long pdu_len = _modbus_fromUint16(msg, i+4) - 1
    // ignore protocol header in i+2, i+3
	// ignore unit identifier in i+6

    Map transaction = MB.transactions[transactionId as int]

    if (transaction == null) {
        _modbus_logError("Unrecognised transaction received id=${transactionId}")
    } else {
	    _modbus_logDebug("Received response for transaction id=${transactionId}${transaction.context} pdu_len=${pdu_len}")
        MB.transactions.remove(transactionId as int)
        try {
	        transaction.promise.resolve([bytes:msg, offset:i+7, len:pdu_len, transaction:transaction])
        } catch (e) {
            _modbus_logError("Error when handling received PDU for transaction ${transactionId}${transaction.context}")
        }
    }
    return i+7+pdu_len
}

// ------------------------------------------------------------------------
// pack/unpack bits or words
// ------------------------------------------------------------------------

byte[] _modbus_packbits(values) {
    byte[] bytes = new byte[(values.size()+7)>>3]
    for(int i=0; i<values.size(); i++) {
        if (values[i]) {
        	bytes[i>>3] |= (1<<(i&7))
        }
    }
	return bytes
}

byte[] _modbus_unpackbits(bytes, int i, int regNum, int maxQty) {
	List values = new byte[(maxQty+7)>>3]
    for(int j=0; j<values.size(); j++) {
		values[j] = ( bytes[i+(j>>3)] & (1<<(j&7)) ) ? 1 : 0
    }
	return values
}


List _modbus_unpack(format, bytes, i, nBytes) {
    List values = []
    int iEnd = i+nBytes
    while (i < iEnd) {
        String endian=">"
        for(code in format) {
            switch (code) {
            case ">":
            case "!":
                endian=">"
                break
            case "<":
                endian="<"
                break
            case "0":
            case "1":
                assert iEnd >= i+1
                i += 1
                break
            case "b":
                assert iEnd >= i+1
                values.add(bytes[i] as int)
                i += 1
                break
            default:
                switch (endian+code) {
                case ">B":
                case "<B":
                    assert iEnd >= i+1
                    values.add((bytes[i] & 0xff) as int)
                    i += 1
                    break
                case ">s":
                    assert iEnd >= i+2
                    values.add(((bytes[i] as byte)<<8) as int | (bytes[i+1] as int & 0xff))
                    i+=2
                    break
                case "<s":
                    assert iEnd >= i+2
                    values.add(((bytes[i+1] as byte)<<8) as int | (bytes[i] as int & 0xff))
                    i+=2
                    break
                case ">S":
                    assert iEnd >= i+2
                    values.add(((bytes[i] as int & 0xff)<<8) | (bytes[i+1] as int & 0xff))
                    i+=2
                    break
                case "<S":
                    assert iEnd >= i+2
                    values.add(((bytes[i+1] as int & 0xff)<<8) | (bytes[i] as int & 0xff))
                    i+=2
                    break
                case ">i":
                    assert iEnd >= i+4
                    values.add(((bytes[i] as byte)<<24) | ((bytes[i+1] & 0xff)<<16)  | ((bytes[i+2] & 0xff)<<8)  | ((bytes[i+3] & 0xff)))
                    i+=4
                    break
                case "<i":
                    assert iEnd >= i+4
                    values.add(((bytes[i+3] as byte)<<24) | ((bytes[i+2] & 0xff)<<16)  | ((bytes[i+1] & 0xff)<<8)  | ((bytes[i] & 0xff)))
                    i+=4
                    break
                case ">I":
                    assert iEnd >= i+4
                    values.add(((bytes[i] as long & 0xff)<<24) | ((bytes[i+1] & 0xff)<<16)  | ((bytes[i+2] & 0xff)<<8)  | ((bytes[i+3] & 0xff)))
                    i+=4
                    break
                case "<I":
                    assert iEnd >= i+4
                    values.add(((bytes[i+3] as long & 0xff)<<24) | ((bytes[i+2] & 0xff)<<16)  | ((bytes[i+1] & 0xff)<<8)  | ((bytes[i] & 0xff)))
                    i+=4
                    break
                case ">f":
                    assert iEnd >= i+4
                    values.add(Float.intBitsToFloat(((bytes[i] as int & 0xff)<<24) | ((bytes[i+1] & 0xff)<<16)  | ((bytes[i+2] & 0xff)<<8)  | ((bytes[i+3] & 0xff))))
                    i+=4
                    break
                case "<f":
                    assert iEnd >= i+4
                    values.add(Float.intBitsToFloat(((bytes[i+3] as int & 0xff)<<24) | ((bytes[i+2] & 0xff)<<16)  | ((bytes[i+1] & 0xff)<<8)  | ((bytes[i] & 0xff))))
                    i+=4
                    break
                default:
                throw new Exception("Unrecgnised unpack code: ${code}")
                    break
                }
            }
        }
    }
    return values
}

byte[] _modbus_pack(String format, values) {
    List bytes = []
    int i=0
    while (i < values.size()) {
        String endian=">"
        for (code in format) {
            switch (code) {
            case ">":
            case "!":
                endian=">"
                break
            case "<":
                endian="<"
                break
            case "0":
                bytes.add(0 as byte)
                break
            case "1":
                bytes.add(-1 as byte)
                break
            case "?":
                bytes.add(0 as byte)
                break
            case "b":
            case "B":
                assert(values.size() > i)
                bytes.add(values[i] as byte)
                i += 1
                break
            default:
                assert(values.size() > i)
                int v = values[i]
                switch (endian+code) {
                case ">s":
                case ">S":
                    bytes += [ ((v >> 8) & 0xff) as byte, (v & 0xff) as byte ]
                    break
                case "<s":
                case "<S":
                    bytes += [ (v & 0xff) as byte, ((v >> 8) & 0xff) as byte ]
                    break
                case ">i":
                case ">I":
                    bytes += [ ((v >> 24) & 0xff) as byte, ((v >> 16) & 0xff) as byte, ((v >> 8) & 0xff) as byte, (v & 0xff) as byte ]
                    break
                case "<i":
                case "<I":
                    bytes += [ (v & 0xff) as byte, ((v >> 8) & 0xff) as byte, ((v >> 16) & 0xff) as byte, ((v >> 24) & 0xff) as byte ]
                    break
                case ">f":
                    int f = Float.floatToIntBits(v)
                    bytes += [ ((f >> 24) & 0xff) as byte, ((f >> 16) & 0xff) as byte, ((f >> 8) & 0xff) as byte, (f & 0xff) as byte ]
                    break
                case "<f":
                    int f = Float.floatToIntBits(v)
                    bytes += [ (f & 0xff) as byte, ((f >> 8) & 0xff) as byte, ((f >> 16) & 0xff) as byte, ((f >> 24) & 0xff) as byte ]
                    break
                default:
                    throw new Exception("Unrecgnised unpack code: ${code}")
                }
                i += 1
            }
        }
    }
    return bytes
}
