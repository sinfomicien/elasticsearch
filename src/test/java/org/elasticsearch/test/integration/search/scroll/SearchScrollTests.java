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

package org.elasticsearch.test.integration.search.scroll;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.test.integration.AbstractSharedClusterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 */
public class SearchScrollTests extends AbstractSharedClusterTest {

    @Test
    public void testSimpleScrollQueryThenFetch() throws Exception {
        try {
            client().admin().indices().prepareDelete("test").execute().actionGet();
        } catch (Exception e) {
            // ignore
        }
        client().admin().indices().prepareCreate("test").setSettings(ImmutableSettings.settingsBuilder().put("index.number_of_shards", 3)).execute().actionGet();
        client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        for (int i = 0; i < 100; i++) {
            client().prepareIndex("test", "type1", Integer.toString(i)).setSource(jsonBuilder().startObject().field("field", i).endObject()).execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setSize(35)
                .setScroll(TimeValue.timeValueMinutes(2))
                .addSort("field", SortOrder.ASC)
                .execute().actionGet();

        long counter = 0;

        assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
        assertThat(searchResponse.getHits().hits().length, equalTo(35));
        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
        }

        searchResponse = client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMinutes(2))
                .execute().actionGet();

        assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
        assertThat(searchResponse.getHits().hits().length, equalTo(35));
        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
        }

        searchResponse = client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMinutes(2))
                .execute().actionGet();

        assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
        assertThat(searchResponse.getHits().hits().length, equalTo(30));
        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
        }
    }

    @Test
    public void testSimpleScrollQueryThenFetchSmallSizeUnevenDistribution() throws Exception {
        try {
            client().admin().indices().prepareDelete("test").execute().actionGet();
        } catch (Exception e) {
            // ignore
        }
        client().admin().indices().prepareCreate("test").setSettings(ImmutableSettings.settingsBuilder().put("index.number_of_shards", 3)).execute().actionGet();
        client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        for (int i = 0; i < 100; i++) {
            String routing = "0";
            if (i > 90) {
                routing = "1";
            } else if (i > 60) {
                routing = "2";
            }
            client().prepareIndex("test", "type1", Integer.toString(i)).setSource("field", i).setRouting(routing).execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(matchAllQuery())
                .setSize(3)
                .setScroll(TimeValue.timeValueMinutes(2))
                .addSort("field", SortOrder.ASC)
                .execute().actionGet();

        long counter = 0;

        assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
        assertThat(searchResponse.getHits().hits().length, equalTo(3));
        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
        }

        for (int i = 0; i < 32; i++) {
            searchResponse = client().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMinutes(2))
                    .execute().actionGet();

            assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
            assertThat(searchResponse.getHits().hits().length, equalTo(3));
            for (SearchHit hit : searchResponse.getHits()) {
                assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
            }
        }

        // and now, the last one is one
        searchResponse = client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMinutes(2))
                .execute().actionGet();

        assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
        assertThat(searchResponse.getHits().hits().length, equalTo(1));
        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
        }

        // a the last is zero
        searchResponse = client().prepareSearchScroll(searchResponse.getScrollId())
                .setScroll(TimeValue.timeValueMinutes(2))
                .execute().actionGet();

        assertThat(searchResponse.getHits().getTotalHits(), equalTo(100l));
        assertThat(searchResponse.getHits().hits().length, equalTo(0));
        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(((Number) hit.sortValues()[0]).longValue(), equalTo(counter++));
        }
    }

    @Test
    public void testScrollAndUpdateIndex() throws Exception {
        try {
            client().admin().indices().prepareDelete("test").execute().actionGet();
        } catch (Exception e) {
            // ignore
        }
        client().admin().indices().prepareCreate("test").setSettings(ImmutableSettings.settingsBuilder().put("index.number_of_shards", 5)).execute().actionGet();
        client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        for (int i = 0; i < 500; i++) {
            client().prepareIndex("test", "tweet", Integer.toString(i)).setSource(
                    jsonBuilder().startObject().field("user", "kimchy").field("postDate", System.currentTimeMillis()).field("message", "test").endObject()).execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        assertThat(client().prepareCount().setQuery(matchAllQuery()).execute().actionGet().getCount(), equalTo(500l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "test")).execute().actionGet().getCount(), equalTo(500l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "test")).execute().actionGet().getCount(), equalTo(500l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "update")).execute().actionGet().getCount(), equalTo(0l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "update")).execute().actionGet().getCount(), equalTo(0l));

        SearchResponse searchResponse = client().prepareSearch()
                .setQuery(queryString("user:kimchy"))
                .setSize(35)
                .setScroll(TimeValue.timeValueMinutes(2))
                .addSort("postDate", SortOrder.ASC)
                .execute().actionGet();


        do {
            for (SearchHit searchHit : searchResponse.getHits().hits()) {
                Map<String, Object> map = searchHit.sourceAsMap();
                map.put("message", "update");
                client().prepareIndex("test", "tweet", searchHit.id()).setSource(map).execute().actionGet();
            }
            searchResponse = client().prepareSearchScroll(searchResponse.getScrollId()).setScroll(TimeValue.timeValueMinutes(2)).execute().actionGet();
        } while (searchResponse.getHits().hits().length > 0);

        client().admin().indices().prepareRefresh().execute().actionGet();
        assertThat(client().prepareCount().setQuery(matchAllQuery()).execute().actionGet().getCount(), equalTo(500l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "test")).execute().actionGet().getCount(), equalTo(0l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "test")).execute().actionGet().getCount(), equalTo(0l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "update")).execute().actionGet().getCount(), equalTo(500l));
        assertThat(client().prepareCount().setQuery(termQuery("message", "update")).execute().actionGet().getCount(), equalTo(500l));
    }
}
