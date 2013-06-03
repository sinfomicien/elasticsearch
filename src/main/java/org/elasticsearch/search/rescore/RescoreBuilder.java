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

package org.elasticsearch.search.rescore;

import java.io.IOException;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilderException;

public class RescoreBuilder implements ToXContent {

    private Rescorer rescorer;
    private Integer windowSize;
    
    public static QueryRescorer queryRescorer(QueryBuilder queryBuilder) {
        return new QueryRescorer(queryBuilder);
    }
    
    public RescoreBuilder rescorer(Rescorer rescorer) {
        this.rescorer = rescorer;
        return this;
    }

    public RescoreBuilder windowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (rescorer != null) {
            builder.startObject("rescore");
            if (windowSize != null) {
                builder.field("window_size", windowSize);
            }
            rescorer.toXContent(builder, params);
            builder.endObject();
        }
        return builder;
    }

    public static abstract class Rescorer implements ToXContent {
        
        private String name;

        public Rescorer(String name) {
            this.name = name;
        }
        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject(name);
            builder = innerToXContent(builder, params);
            builder.endObject();
            return builder;
        }

        protected abstract XContentBuilder innerToXContent(XContentBuilder builder, Params params) throws IOException;
        
    }
    
    public static class QueryRescorer extends Rescorer {
        private static final String NAME = "query";
        private QueryBuilder queryBuilder;
        private Float rescoreQueryWeight;
        private Float queryWeight;
        
        /**
         * Creates a new {@link QueryRescorer} instance
         * @param builder the query builder to build the rescore query from
         */
        public QueryRescorer(QueryBuilder builder) {
            super(NAME);
            this.queryBuilder = builder;
        }
        /**
         * Sets the original query weight for rescoring. The default is <tt>1.0</tt>
         */
        public QueryRescorer setQueryWeight(float queryWeight) {
            this.queryWeight = queryWeight;
            return this;
        }
        
        /**
         * Sets the original query weight for rescoring. The default is <tt>1.0</tt>
         */
        public QueryRescorer setRescoreQueryWeight(float rescoreQueryWeight) {
            this.rescoreQueryWeight = rescoreQueryWeight;
            return this;
        }

        @Override
        protected XContentBuilder innerToXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field("rescore_query", queryBuilder);
            if (queryWeight != null) {
                builder.field("query_weight", queryWeight);
            }
            if (rescoreQueryWeight != null) {
                builder.field("rescore_query_weight", rescoreQueryWeight);
            }
            return builder;
        }
    }

}
