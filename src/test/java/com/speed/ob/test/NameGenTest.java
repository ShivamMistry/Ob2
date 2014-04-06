package com.speed.ob.test;

import com.speed.ob.util.NameGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * See LICENSE.txt for license info
 */
@RunWith(JUnit4.class)
public class NameGenTest {

    @Test
    public void correct2ndOrderOutput() {
        NameGenerator generator = new NameGenerator(28);
        assertEquals("ab", generator.next());
    }

    @Test
    public void correct1stOrderOutput() {
        NameGenerator generator = new NameGenerator(1);
        assertEquals("a", generator.next());
    }

    @Test
    public void correct3rdOrderOutput() {
        NameGenerator generator = new NameGenerator(703);
        assertEquals("aaa", generator.next());
    }

    @Test
    public void correct4thOrderOutput() {
        NameGenerator generator = new NameGenerator(18280);
        assertEquals("aaab", generator.next());
    }

    @Test
    public void testOnly1stOrder() {
        for (int i = 0; i < 676; i++) {
            NameGenerator ng = new NameGenerator();
            assertEquals(1, ng.next().length());
        }
    }

}
