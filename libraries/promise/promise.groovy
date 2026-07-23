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
	category: "promise",
	description: "promise support",
	documentationLink: "",
	name: "promise",
	namespace: "matthammonddotorg"
)

/*
PROMISE API

This module implements a Promise like mechanism that supports much of the same functionality and semantics you would expect from
a JavaScript promise, including chaining.

Examples:

  Closure task = { it -> 
      if (task_succeeds) {
          it.resolve("answer")
      } else {
          it.reject(new Exception("failed"))
      }
  }

  Promise(task)
  .then { println "Suceeded with ${it}"; return [1,2,3] }
  .catch { e -> println "Failed with exception ${e}" }
  .then { abc -> println "Received as a single argument: abc=${abc}"; return [a,b,c] }
  .then_spread { a, b, c -> println "Received as spread arguments: a=${a}, b=${b}, c=${c}" }

Create a promise by passing a Closure that will be run immediately. This closure is passed a single Map object with two methods as properties.
The task should call either of these to indicate whether the promise is fulfilled or rejected:

* it.resolve() takes a single argument that will be passed to the then() handlers.

* it.reject() takes a single argument that will be passed to the next catch() handlers in the chain.
              If the argument is not an Exception it will be wrapped in one.

Calling Promise() returns a promise like object with then() and catch() methods for registering closures to be run if the promise is either fulfilled
or rejected. then() or catch() return new promise-like objects enabling a chain to be built up.

If the promise is fulfilled, the then() closures are passed the result as a single argument.
Use then_spread() instead (as shown above) if you wish to have the result automatically unpacked
to positional arguments if it is an array or list.

If the promise is rejected then an exception is passed as a single argument to the catch() closures.

What happens when a then() or catch() closure is run?...
 * If it returns a value, then it the promise resolves and the next then() in the promise chain is called, passing it that value. Arrays/lists are expanded as varargs.
 * If it returns a Promise, then whether that promise resolves or rejects will determine whether the next then() or catch() in the promise chain is called.
 * If it throws as Exception then the next catch() in the promise chain is called, passing it the expection.

Other useful shortcut behaviours:
 * If Promise() is called with no arguments, or a non-closure argument, then the promise resolves and that argument is passed to the next then() in the chain. Arrays/lists are exapaneded as varargs.
 * If then() or catch() is called with a value instead of a closure, then that value is passed to the next then() in the chain. Arrays/lists are exapaneded as varargs.

*/


import groovy.transform.Field
import groovy.lang.Closure
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Field static Map _promise = new ConcurrentHashMap()

private Map _promise_ensureData() {
    String id;
    try {
        id = "dev-${device.id}"
    } catch (MissingPropertyException e) {
        id = "app-${app.id}"
    }
    if (_promise.containsKey(id)) {
        return _promise[id]
    } else {
     	return _promise[id] = [
			promiseTasks: new ConcurrentLinkedQueue(),
        ]
    }
}

boolean isArrayOrList(v) {
    try {
		return v[0..<0] == []
    } catch (_) {
        return false
    }
}

def spread(Closure c) {
	return { a -> 
		if (isArrayOrList(a)) 
			return c(*a)
		else
			return c(*[a])
	}
}


// def ensureIsArrayOrList(v) {
//     if (isArrayOrList(v)) {
//         return v
//     } else {
//         return [v]
//     }
// }

Map Promise(taskOrResolution=null) { 
	Closure task;
	if (taskOrResolution == null) {
		task = { it.resolve() }
	} else if (taskOrResolution instanceof Closure) {
		task = taskOrResolution
	} else if (taskOrResolution instanceof Exception) {
		task = { it.reject(taskOrResolution) }
	} else {
		task = { it.resolve(taskOrResolution) }
	}

	boolean fulfilled = false
	boolean rejected = false
	boolean pending = true
	def result = null

	List<Closure> fulfilledCbs = []
	List<Closure> rejectedCbs = []

	Closure _resolve = { it=null ->
		if (!rejected) {
			if (!fulfilled) {
				result = it
				pending = false
				fulfilled = true
			}
			while (fulfilledCbs.size() > 0) {
				Closure cb = fulfilledCbs.remove(0)
				cb(result)
			}
		}
	}

	Closure _reject= { it=null ->
		if (!fulfilled) {
			if (!rejected) {
                if (it instanceof Exception) {
                    result = it
                } else {
                    result = new Exception("${it}")
                }
                pending = false
				rejected = true
			}
            if (rejectedCbs.size() == 0) {
                log.error "Unhandled exception in promise: ${result}"
            } else while (rejectedCbs.size() > 0) {
				Closure cb = rejectedCbs.remove(0)
				cb(result)
            }
		}
	}

	Closure _then = { onFulfilled, onRejected=null ->
		return Promise { it ->
			if (pending || fulfilled) {
				fulfilledCbs.add { args ->
					try {
						if (onFulfilled instanceof Closure) {
							def r = onFulfilled(args)
							if (r instanceof Map && r?.__is_promise__ == true) {
								r.then( it.resolve )
								r.catch( it.reject )
							} else {
								it.resolve(r)
							}
						} else {
							it.resolve(onFulfilled ?: args)
						}
					} catch (e) {
						it.reject(e)
					}
				}
			}
			if (pending || rejected) {
				rejectedCbs.add { arg ->
					try {
						if (onRejected instanceof Closure) {
							def r = onRejected(arg)
							if (r instanceof Map && r?.__is_promise__ == true) {
								r.then( it.resolve )
								r.catch( it.reject )
							} else {
								it.resolve(r)
							}
						} else if (onRejected instanceof Exception) {
                            it.reject(onRejected)
                        } else if (onRejected == null) {
							it.reject(arg)
						}
					} catch (e) {
						it.reject(e)
					}
				}
			}
			if (!pending) {
				Map P = _promise_ensureData()
				if (fulfilled)
				    P.promiseTasks.add([task:_resolve, param:null])
				if (rejected)
				    P.promiseTasks.add([task:_reject, param:null])
				runIn(0, "_promiseTaskRunner", [misfire: true])
			}
		}
	}

	Closure _catch = { onRejected -> 
		return _then(null, onRejected)
	}

    Map P = _promise_ensureData()
    P.promiseTasks.add([task:task, param:[resolve:_resolve, reject:_reject]])
	runIn(0, "_promiseTaskRunner", [misfire: true])
    
    return [
		then: _then,
        then_spread: { onFulfilled, onRejected=null -> _then(spread(onFulfilled),onRejected) },
		catch: _catch,
		__is_promise__: true,
	]
}

void _promiseTaskRunner() {
    Map P = _promise_ensureData()
    while (P.promiseTasks.size() > 0) {
        Map data = P.promiseTasks.remove()
        try {
            data.task (data.param)
        } catch (e) {
			data.param.reject(e)
//            throw new Exception("Unhandled exception in promise: ${e}")
        }
    }
}

/**
 * Runs one or more promises in parallel and returns a promise that resolves once all promises have resolved, or rejects if at least one rejects
 * option: allBeforeReject: false (default) ... if true then wait for all promises to reject/resolve before rejecting.
 */
def Parallel(Map opts=[:], ...promises) {
    return Promise { it ->
	    int n = promises.size()
        Closure resolve = it.resolve
        Closure reject = it.reject
        def firstErr = null
    
        for(int i=0; i<promises.size(); i++) {
            Map p;
            if (promises[i] instanceof Map && promises[i].__is_promise__) {
                p = promises[i]
            } else {
                p = Promise().then promises[i]
            }
            p
            .catch { e ->
                if (firstErr == null) { firstErr = e }
                if (!(opts?.allBeforeReject)) {
                    reject(e)
                }
            }
            .then {
                n--;
                if (n==0) {
                    if (firstErr == null) {
                        resolve(null)
                    } else {
                        reject(firstErr)
                    }
                }
            }
        }
    }
}

/**
 * Run a promise that returns a closure up to N times if it fails
 * before passin on that failure, or success.
 */
def Retry(int n, Closure actionReturnsPromise) {
    return Promise { it ->
        Closure resolve = it.resolve
        Closure reject = it.reject
        Closure retry; // declare before assigning closure, otherwise closure cannot access itself
        retry = {
	        actionReturnsPromise()
	        .then(resolve)
	        .catch { e ->
                n=n-1
	            if (n<=0) {
	                reject(e)
	            } else {
	                retry()
	            }
	        }
        }
        retry()
	};
}
