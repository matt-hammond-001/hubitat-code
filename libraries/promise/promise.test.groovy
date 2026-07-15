import groovy.test.GroovyTestCase
import groovy.test.GroovyTestSuite 
import junit.framework.Test 
import junit.textui.TestRunner 
import groovy.transform.Field

evaluate('''
def library(Map opts=[:]) { }; 
''' + (new File("./promise.groovy").text))


class PromiseTests extends GroovyTestCase {
   def p;
   Map logs;
   List tasks
   List seq;
   Closure pump;

   def setup() {
      p = new promise()
      tasks = []
      seq = []
      logs = [
         error: [],
         warn: [],
         info: [],
         debug: [],
         trace: [],
      ]

      p.app = [id:5]
      p.log = [
         error: { it -> logs.error.add it },
         warn: { it -> logs.warn.add it },
         info: { it -> logs.info.add it },
         debug: { it -> logs.debug.add it },
         trace: { it -> logs.trace.add it },
      ]

      p.now = { return 5 }

      p.runIn = { when, methodName, opts ->
         assertEquals(when,0)
         assertEquals(opts?.misfire,true)
         tasks.add(methodName) }
      
      pump = {
         while (tasks.size()) {
            String task = tasks.pop()
            p."${task}"()
         }
      }
   }

	void testSmoke() {
      setup();
      def _ = p.Promise {it -> it.resolve()}
      pump()
   }

   void testPromiseTaskRunsAfter() {
      setup();
      boolean ran = false
      def promise = p.Promise { ran=true }
      assertFalse("Task does not run immediately after promise is instantiated",ran)
      pump()
      assertTrue("Task has run later", ran)
   }

   void testPromiseResolvesNoArgThen() {
      setup();
      boolean resolved=false
      p.Promise { it -> it.resolve() }
      .then { resolved = true; assertEquals("No argument to resolve passes null to then()", it, null)}
      assertFalse("Not yet resolved", resolved)
      pump()
      assertTrue("Resolved", resolved)
   }

   void testPromiseResolvesNotArrayOrListArgsThen() {
      for(def v in [5 as byte, 5 as Number, 5.5 as float, "hello" as String, true as boolean, [a:5] as Map, new Exception("hello")]) {
         setup();
         boolean resolved=false
         p.Promise { it -> it.resolve(v) }
         .then { resolved = true; assertEquals("${v} argument to resolve passes ${v} to then()", it, v)}
         assertFalse("Not yet resolved", resolved)
         pump()
         assertTrue("Resolved", resolved)
      }
   }

   void testPromiseResolvesArrayOrListArgThen() {
      for(def v in [ [1,2,3] as int[], [1,2,3] as List<int>]) {
         setup();
         boolean resolved=false
         p.Promise { it -> it.resolve([1,2,3]) }
         .then { ...args -> resolved = true; assertEquals("${v} argument to resolve passes as separate args ${args} to then()", args, v) };
         assertFalse("Not yet resolved", resolved)
         pump()
         assertTrue("Resolved", resolved)

      }
   }

   void testPromiseRejectsCatch() {
      setup()
      def v = new Exception("hello")
      boolean rejected=false
      p.Promise { it -> it.reject(v)}
      .catch { e -> rejected=true; assertSame("Exception is passed to catch()", e, v)}
      assertFalse("Not yet rejected", rejected)
      pump()
      assertTrue("Rejected", rejected)      
   }

   void testPromiseThrowsCatch() {
      setup()
      def v = new Exception("hello")
      boolean rejected=false
      p.Promise { it -> throw v}
      .catch { e -> rejected=true; assertSame("Exception is passed to catch()", e, v)}
      assertFalse("Not yet rejected", rejected)
      pump()
      assertTrue("Rejected", rejected)      
   }

   void testPromiseResolvesIfNotTask() {
      setup()
      p.Promise()
      .then { it -> assertEquals("no arg is null",null,it); seq.add(1) }
      assertEquals("Not resolved yet", [], seq)
      pump()
      assertEquals("then() was called", [1], seq)
   }

   void testPromiseRejectsIfException() {
      def e = new Exception("hello")
      setup()
      p.Promise(e)
      .then { seq += ["then",it] }
      .catch { seq += ["catch",it] }
      assertEquals("Not resolved yet", [], seq)
      pump()
      assertEquals("then() was called", ["catch",e], seq)
   }

   void testPromiseResolvesIfNotTask2() {
      setup()
      p.Promise(5)
      .then { it -> assertEquals("5 becomes arg",5,it); seq.add(1) }
      assertEquals("Not resolved yet", [], seq)
      pump()
      assertEquals("then() was called", [1], seq)
   }

   void testPromiseMultipleThens() {
      setup()
      def x = p.Promise(5)
      x.then { it -> assertEquals(5,it); seq += ["then1",1] }
      x.then { it -> assertEquals(5,it); seq += ["then2",2] }
      x.then { it -> assertEquals(5,it); seq += ["then3",3] }
      assertEquals("Not resolved yet", [], seq)
      pump()
      assertEquals("then() was called", ["then1",1,"then2",2,"then3",3], seq)
   }

   void testPromiseThenChain() {
      setup()
      p.Promise(5)
      .then { it -> seq += ["then1", it]; return "hello" }
      .then { it -> seq += ["then2", it]; return [1,2,3] }
      .then { ...args -> seq += ["then3", args]; return null }
      .then { it -> seq += ["then4", it] }
      assertEquals("Not resolved yet", [], seq)
      pump()
      assertEquals("then() was called", ["then1", 5, "then2", "hello", "then3", [1,2,3], "then4", null], seq)
   }

   void testPromiseThenThrowsCatch() {
      def e = new Exception("hello")
      setup()
      p.Promise(5)
      .then { throw e }
      .catch { seq += [it] }
      pump()
      assertEquals("catch exception", [e], seq)
   }

   void testPromiseThenDoesNotFollowException() {
      def e = new Exception("hello")
      setup()
      p.Promise(5)
      .then { throw e }
      .then { fail("Should not have reached here") }
      .catch { seq += [it] }
      pump()
      assertEquals("catch exception", [e], seq)
   }

   void testPromiseThenReceivesCatchReturn() {
      def e = new Exception("hello")
      setup()
      p.Promise(5)
      .then { throw e }
      .then { fail("Should not have reached here") }
      .catch { seq += [e]; return 7 }
      .then { seq += ["then",it] }
      pump()
      assertEquals("catch exception", [e,"then",7], seq)
   }

   void testPromiseThenReturnsPromiseResolves() {
      setup()
      def p2
      p.Promise(5)
      .then { seq += ["then1",it]; return p.Promise { p2=it }}
      .then { seq += ["then2",it] }
      pump()
      assertEquals("First then. No second then yet", ["then1",5], seq)
      p2.resolve(99)
      pump()
      assertEquals("First then. No second then yet", ["then1",5,"then2",99], seq)
   }

   void testPromiseThenReturnsPromiseRejects() {
      def e = new Exception("hello")
      setup()
      def p2
      p.Promise(5)
      .then { seq += ["then1",it]; return p.Promise { p2=it }}
      .then { seq += ["then2",it] }
      .catch { seq += ["catch1",it]}
      pump()
      assertEquals("First then. No second then yet", ["then1",5], seq)
      p2.reject(e)
      pump()
      assertEquals("First then. No second then yet", ["then1",5,"catch1",e], seq)
   }

   void testPromiseThenReturnsPromiseThrowsException() {
      def e = new Exception("hello")
      setup()
      p.Promise(5)
      .then { seq += ["then1",it]; return p.Promise { throw e }}
      .then { seq += ["then2",it] }
      .catch { seq += ["catch1",it]}
      pump()
      assertEquals("First then. No second then yet", ["then1",5,"catch1",e], seq)
   }

   void testPromiseAlreadyResolvedRunsNewThen() {
      setup()
      def p2 = p.Promise(5)
      def p3 = p2.then { seq += ["then1",it]; return 23 }
      pump()
      assertEquals("First then runs", ["then1",5], seq)

      p2.then { seq += ["then2",it]}

      assertEquals("No change before pump", ["then1",5], seq)
      pump()
      assertEquals("Second then runs", ["then1",5,"then2",5], seq)

      p3.then { seq += ["then3",it]}
      assertEquals("No change before pump", ["then1",5,"then2",5], seq)
      pump()
      assertEquals("Third then runs", ["then1",5,"then2",5,"then3",23], seq)
   }

   void testPromiseAlreadyRejectedRunsNewThen() {
      def e = new Exception("hello")
      setup()
      def p2 = p.Promise(5)
      def p3 = p2.then { seq += ["then1",it]; throw e }
      pump()
      assertEquals("First then runs", ["then1",5], seq)

      p3.then { seq += ["then2",it]}
      p3.catch { seq += ["catch1",it]}

      assertEquals("No change before pump", ["then1",5], seq)
      pump()
      assertEquals("catch now runs", ["then1",5,"catch1",e], seq)
   }

   void testUncaughtExceptionGeneratesErrorMessage() {
      setup()
      p.Promise(5)
      .then { throw new Exception("hello") }
      pump()
      assertEquals(["Unhandled exception in promise: java.lang.Exception: hello"], logs.error)
   }

   void testCaughtExceptionDoesNotGenerateErrorMessage() {
      def e = new Exception("hello")
      setup()
      p.Promise(5)
      .then { throw e }
      .catch { seq += ["catch",it] }
      pump()
      assertEquals(["catch",e],seq)
      assertEquals([], logs.error)
   }

}

class AllTests { 
   static Test suite() { 
      def allTests = new GroovyTestSuite() 
      allTests.addTestSuite(PromiseTests.class) 
      return allTests 
   } 
} 

TestRunner.run(AllTests.suite())
