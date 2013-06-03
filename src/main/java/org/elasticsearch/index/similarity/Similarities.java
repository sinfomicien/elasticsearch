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

package org.elasticsearch.index.similarity;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import org.apache.lucene.search.similarities.*;
import org.elasticsearch.common.collect.MapBuilder;

/**
 * Cache of pre-defined Similarities
 */
public class Similarities {

    private static final ImmutableMap<String, PreBuiltSimilarityProvider.Factory> PRE_BUILT_SIMILARITIES;

    static {
        MapBuilder<String, PreBuiltSimilarityProvider.Factory> similarities = MapBuilder.newMapBuilder();
        similarities.put("default", new PreBuiltSimilarityProvider.Factory("default", new DefaultSimilarity()));
        similarities.put("BM25", new PreBuiltSimilarityProvider.Factory("BM25", new BM25Similarity()));

        PRE_BUILT_SIMILARITIES = similarities.immutableMap();
    }

    private Similarities() {
    }

    /**
     * Returns the list of pre-defined SimilarityProvider Factories
     *
     * @return Pre-defined SimilarityProvider Factories
     */
    public static ImmutableCollection<PreBuiltSimilarityProvider.Factory> listFactories() {
        return PRE_BUILT_SIMILARITIES.values();
    }
}
