/*
 * #%L
 * GarethHealy :: Camel File Loadbalancer :: Core
 * %%
 * Copyright (C) 2013 - 2015 Gareth Healy
 * %%
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
 * #L%
 */
package com.garethahealy.camel.file.loadbalancer.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.camel.component.file.GenericFile;
import org.junit.Assert;
import org.junit.Test;

public class PriorityFileFilterTest {

    @Test
    public void getPriorityNameReturnsCorrectValue() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 1, 1);
        filter.init();

        Assert.assertNotNull(filter.getPriorityName());
        Assert.assertEquals("0", filter.getPriorityName());
    }

    @Test
    public void getPossiblePrioritiesReturnsOne() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 1, 1);
        filter.init();

        Set<Integer> answer = filter.getPossiblePriorities();

        Assert.assertNotNull(answer);
        Assert.assertEquals(1, answer.size());

        List<Integer> answerList = new ArrayList<Integer>(answer);

        Assert.assertEquals(new Integer(0), answerList.get(0));
    }

    @Test
    public void getPossiblePrioritiesReturnsMultiplesOf3StartingAt0Until10() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 3, 10);
        filter.init();

        Set<Integer> answer = filter.getPossiblePriorities();

        Assert.assertNotNull(answer);
        Assert.assertEquals(4, answer.size());

        List<Integer> answerList = new ArrayList<Integer>(answer);

        Assert.assertEquals(new Integer(0), answerList.get(0));
        Assert.assertEquals(new Integer(3), answerList.get(1));
        Assert.assertEquals(new Integer(6), answerList.get(2));
        Assert.assertEquals(new Integer(9), answerList.get(3));
    }

    @Test
    public void getPossiblePrioritiesReturnsMultiplesOf3StartingAt1Until10() {
        PriorityFileFilter filter = new PriorityFileFilter(1, 3, 10);
        filter.init();

        Set<Integer> answer = filter.getPossiblePriorities();

        Assert.assertNotNull(answer);
        Assert.assertEquals(3, answer.size());

        List<Integer> answerList = new ArrayList<Integer>(answer);

        Assert.assertEquals(new Integer(1), answerList.get(0));
        Assert.assertEquals(new Integer(4), answerList.get(1));
        Assert.assertEquals(new Integer(7), answerList.get(2));
    }

    @Test
    public void getPossiblePrioritiesReturnsMultiplesOf3StartingAt0Until1() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 3, 1);
        filter.init();

        Set<Integer> answer = filter.getPossiblePriorities();

        Assert.assertNotNull(answer);
        Assert.assertEquals(1, answer.size());

        List<Integer> answerList = new ArrayList<Integer>(answer);

        Assert.assertEquals(new Integer(0), answerList.get(0));
    }

    @Test
    public void getPossiblePrioritiesReturnsMultiplesOf3StartingAt1Until1() {
        PriorityFileFilter filter = new PriorityFileFilter(1, 3, 1);
        filter.init();

        Set<Integer> answer = filter.getPossiblePriorities();

        Assert.assertNotNull(answer);
        Assert.assertEquals(1, answer.size());

        List<Integer> answerList = new ArrayList<Integer>(answer);

        Assert.assertEquals(new Integer(1), answerList.get(0));
    }

    @Test
    public void acceptOnlyOneFileStartingAt0() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 3, 1);
        filter.init();

        for (int i = 0; i < 1; i++) {
            GenericFile file1 = new GenericFile();
            file1.setFileName("file0");

            Boolean answer = filter.accept(file1);

            Assert.assertNotNull(answer);

            if (i == 0) {
                Assert.assertTrue(answer);
            } else {
                Assert.assertFalse(answer);
            }
        }
    }

    @Test
    public void acceptOnlyOneFileStartingAt1() {
        PriorityFileFilter filter = new PriorityFileFilter(1, 3, 1);
        filter.init();

        for (int i = 0; i < 1; i++) {
            GenericFile file1 = new GenericFile();
            file1.setFileName("file1");

            Boolean answer = filter.accept(file1);

            Assert.assertNotNull(answer);

            if (i == 1) {
                Assert.assertTrue(answer);
            } else {
                Assert.assertFalse(answer);
            }
        }
    }

    @Test
    public void acceptFourFilesStartingAt0() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 3, 10);
        filter.init();

        for (int i = 0; i < 10; i++) {
            GenericFile file1 = new GenericFile();
            file1.setFileName("file0");

            Boolean answer = filter.accept(file1);

            Assert.assertNotNull(answer);

            if (i == 0 || i == 3 || i == 6 || i == 9) {
                Assert.assertTrue(answer);
            } else {
                Assert.assertFalse(answer);
            }
        }
    }

    @Test
    public void accept3FilesStartingAt1() {
        PriorityFileFilter filter = new PriorityFileFilter(1, 3, 10);
        filter.init();

        for (int i = 0; i < 10; i++) {
            GenericFile file1 = new GenericFile();
            file1.setFileName("file0");

            Boolean answer = filter.accept(file1);

            Assert.assertNotNull(answer);

            if (i == 1 || i == 4 || i == 7) {
                Assert.assertTrue(answer);
            } else {
                Assert.assertFalse(answer);
            }
        }
    }

    @Test
    public void accept7FilesStartingAt0Until20() {
        PriorityFileFilter filter = new PriorityFileFilter(0, 3, 20);
        filter.init();

        for (int i = 0; i < 20; i++) {
            GenericFile file1 = new GenericFile();
            file1.setFileName("file0");

            Boolean answer = filter.accept(file1);

            Assert.assertNotNull(answer);

            if (i == 0 || i == 3 || i == 6 || i == 9 || i == 12 || i == 15 || i == 18) {
                Assert.assertTrue(answer);
            } else {
                Assert.assertFalse(answer);
            }
        }
    }
}

