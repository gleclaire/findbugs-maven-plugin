package org.codehaus.mojo.findbugsmavenplugin.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Foo}.
 *
 * @author user@example.com (John Doe)
 */
public class FooTest {

    @Test
    public void thisAlwaysPasses() {

    }

    @Test
    @Ignore
    public void thisIsIgnored() {
    }
}