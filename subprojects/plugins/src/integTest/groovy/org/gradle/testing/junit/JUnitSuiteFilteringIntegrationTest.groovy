/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.gradle.testing.junit

import org.gradle.integtests.fixtures.DefaultTestExecutionResult
import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.testing.fixture.JUnitCoverage

@TargetCoverage({JUnitCoverage.LARGE_COVERAGE})
public class JUnitSuiteFilteringIntegrationTest extends MultiVersionIntegrationSpec {
    def "passing a suite argument to --tests runs all tests in the suite"() {
        buildFile << """
            apply plugin: 'java'
            repositories { mavenCentral() }
            dependencies { testCompile 'junit:junit:${version}' }
            test { useJUnit() }
        """
        file("src/test/java/FooTest.java") << """
            import org.junit.Test;
            public class FooTest {
                @Test
                public void testFoo() { }
            }
        """
        file("src/test/java/FooServerTest.java") << """
            import org.junit.Test;
            public class FooServerTest {
                @Test
                public void testFooServer() { }
            }
        """
        file("src/test/java/BarTest.java") << """
            import org.junit.Test;
            public class BarTest {
                @Test
                public void testBar() { }
            }
        """
        file("src/test/java/AllFooTests.java") << """
            import org.junit.runner.RunWith;
            import org.junit.runners.Suite;
            import org.junit.runners.Suite.SuiteClasses;
            @RunWith(Suite.class)
            @SuiteClasses({FooTest.class, FooServerTest.class})
            public class AllFooTests {
            }
        """

        when:
        run("test", "--tests", "*AllFooTests")

        then:
        def result = new DefaultTestExecutionResult(testDirectory)

        result.assertTestClassesExecuted("FooTest", "FooServerTest")
        result.testClass("FooTest").assertTestCount(1, 0, 0);
        result.testClass("FooTest").assertTestsExecuted("testFoo")
        result.testClass("FooServerTest").assertTestCount(1, 0, 0);
        result.testClass("FooServerTest").assertTestsExecuted("testFooServer")
    }
}
