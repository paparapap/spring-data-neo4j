/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.neo4j.lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class AfterDeleteEventTests {
    @Configuration
    @EnableNeo4jRepositories
    static class TestConfig extends Neo4jConfiguration {
        @Bean
        GraphDatabaseService graphDatabaseService() {
            return new TestGraphDatabaseFactory().newImpermanentDatabase();
        }

        @Bean
        ApplicationListener<AfterDeleteEvent<Program>> afterDeleteEventApplicationListener() {
            return new ApplicationListener<AfterDeleteEvent<Program>>() {

                @Override
                public void onApplicationEvent(AfterDeleteEvent<Program> event) {
                    afterProgramDeleteEvents.add(event.getEntity());
                    lastEvent = Event.AFTER_DELETE;
                }
            };
        }


    }

    @Autowired
    Neo4jTemplate template;

    @Autowired
    GraphDatabaseService graphDatabaseService;

    enum Event { NONE, BEFORE_DELETE, AFTER_DELETE }

    static Event lastEvent = Event.NONE;
    static List<Program> afterProgramDeleteEvents = new ArrayList<Program>();


    @BeforeTransaction
    public void beforeTransaction() {
        Neo4jHelper.cleanDb(template);
    }

    @Before
    public void before() {
        lastEvent = Event.NONE;
        afterProgramDeleteEvents.clear();
    }

    @Test
    public void shouldFireAfterEntityIsDeleted() throws Exception {
        assertEquals(Event.NONE, lastEvent);
        assertThat(afterProgramDeleteEvents, hasSize(0));

        Program sark = template.save(new Program("Sark"));
        template.delete(sark);

        assertThat(afterProgramDeleteEvents, hasSize(1));
        assertEquals(Event.AFTER_DELETE, lastEvent);
    }

}

