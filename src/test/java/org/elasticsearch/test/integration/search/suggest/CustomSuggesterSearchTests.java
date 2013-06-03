/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.test.integration.search.suggest;

import com.google.common.collect.Lists;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.RandomStringGenerator;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.test.integration.AbstractNodesTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class CustomSuggesterSearchTests extends AbstractNodesTests {

    private Client client;

    @BeforeClass
    public void createNodes() throws Exception {
        ImmutableSettings.Builder settings = settingsBuilder().put("plugin.types", CustomSuggesterPlugin.class.getName());
        startNode("server1", settings);
        client = client("server1");

        client.prepareIndex("test", "test", "1").setSource(jsonBuilder()
                .startObject()
                .field("name", "arbitrary content")
                .endObject())
                .setRefresh(true).execute().actionGet();
        client.admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForYellowStatus().execute().actionGet();
    }

    @AfterClass
    public void closeNodes() {
        client.close();
        closeAllNodes();
    }

    @Test
    public void testThatCustomSuggestersCanBeRegisteredAndWork() throws Exception {
        String randomText = RandomStringGenerator.randomAlphanumeric(10);
        String randomField = RandomStringGenerator.randomAlphanumeric(10);
        String randomSuffix = RandomStringGenerator.randomAlphanumeric(10);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("test").setTypes("test").setFrom(0).setSize(1);
        XContentBuilder query = jsonBuilder().startObject()
                .startObject("suggest")
                    .startObject("someName")
                        .field("text", randomText)
                        .startObject("custom")
                            .field("field", randomField)
                            .field("suffix", randomSuffix)
                        .endObject()
                    .endObject()
                .endObject()
            .endObject();
        searchRequestBuilder.setExtraSource(query.bytes());

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        List<Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestions = Lists.newArrayList(searchResponse.getSuggest().getSuggestion("someName").iterator());
        assertThat(suggestions, hasSize(2));
        assertThat(suggestions.get(0).getText().string(), is(String.format(Locale.ROOT, "%s-%s-%s-12", randomText, randomField, randomSuffix)));
        assertThat(suggestions.get(1).getText().string(), is(String.format(Locale.ROOT, "%s-%s-%s-123", randomText, randomField, randomSuffix)));
    }

}
