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
package org.elasticsearch.search.highlight;

import com.google.common.collect.Maps;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.vectorhighlight.*;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.search.vectorhighlight.SimpleBoundaryScanner2;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.StringText;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.search.fetch.FetchPhaseExecutionException;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.highlight.vectorhighlight.SourceScoreOrderFragmentsBuilder;
import org.elasticsearch.search.highlight.vectorhighlight.SourceSimpleFragmentsBuilder;
import org.elasticsearch.search.internal.SearchContext;

import java.util.Map;

/**
 *
 */
public class FastVectorHighlighter implements Highlighter {

    private static final String CACHE_KEY = "highlight-fsv";
    private final Boolean termVectorMultiValue;

    @Inject
    public FastVectorHighlighter(Settings settings) {
        this.termVectorMultiValue = settings.getAsBoolean("search.highlight.term_vector_multi_value", true);
    }

    @Override
    public String[] names() {
        return new String[] { "fvh", "fast-vector-highlighter" };
    }

    @Override
    public HighlightField highlight(HighlighterContext highlighterContext) {
        SearchContextHighlight.Field field = highlighterContext.field;
        SearchContext context = highlighterContext.context;
        FetchSubPhase.HitContext hitContext = highlighterContext.hitContext;
        FieldMapper<?> mapper = highlighterContext.mapper;

        if (!(mapper.fieldType().storeTermVectors() && mapper.fieldType().storeTermVectorOffsets() && mapper.fieldType().storeTermVectorPositions())) {
            throw new ElasticSearchIllegalArgumentException("the field [" + field.field() + "] should be indexed with term vector with position offsets to be used with fast vector highlighter");
        }

        Encoder encoder = field.encoder().equals("html") ? Encoders.HTML : Encoders.DEFAULT;

        if (!hitContext.cache().containsKey(CACHE_KEY)) {
            hitContext.cache().put(CACHE_KEY, new HighlighterEntry());
        }
        HighlighterEntry cache = (HighlighterEntry) hitContext.cache().get(CACHE_KEY);

        try {
            MapperHighlightEntry entry = cache.mappers.get(mapper);
            FieldQuery fieldQuery = null;
            if (entry == null) {
                FragListBuilder fragListBuilder;
                BaseFragmentsBuilder fragmentsBuilder;

                BoundaryScanner boundaryScanner = SimpleBoundaryScanner2.DEFAULT;
                if (field.boundaryMaxScan() != SimpleBoundaryScanner2.DEFAULT_MAX_SCAN || field.boundaryChars() != SimpleBoundaryScanner2.DEFAULT_BOUNDARY_CHARS) {
                    boundaryScanner = new SimpleBoundaryScanner2(field.boundaryMaxScan(), field.boundaryChars());
                }

                if (field.numberOfFragments() == 0) {
                    fragListBuilder = new SingleFragListBuilder();

                    if (mapper.fieldType().stored()) {
                        fragmentsBuilder = new SimpleFragmentsBuilder(field.preTags(), field.postTags(), boundaryScanner);
                    } else {
                        fragmentsBuilder = new SourceSimpleFragmentsBuilder(mapper, context, field.preTags(), field.postTags(), boundaryScanner);
                    }
                } else {
                    fragListBuilder = field.fragmentOffset() == -1 ? new SimpleFragListBuilder() : new SimpleFragListBuilder(field.fragmentOffset());
                    if (field.scoreOrdered()) {
                        if (mapper.fieldType().stored()) {
                            fragmentsBuilder = new ScoreOrderFragmentsBuilder(field.preTags(), field.postTags(), boundaryScanner);
                        } else {
                            fragmentsBuilder = new SourceScoreOrderFragmentsBuilder(mapper, context, field.preTags(), field.postTags(), boundaryScanner);
                        }
                    } else {
                        if (mapper.fieldType().stored()) {
                            fragmentsBuilder = new SimpleFragmentsBuilder(field.preTags(), field.postTags(), boundaryScanner);
                        } else {
                            fragmentsBuilder = new SourceSimpleFragmentsBuilder(mapper, context, field.preTags(), field.postTags(), boundaryScanner);
                        }
                    }
                }
                fragmentsBuilder.setDiscreteMultiValueHighlighting(termVectorMultiValue);
                entry = new MapperHighlightEntry();
                entry.fragListBuilder = fragListBuilder;
                entry.fragmentsBuilder = fragmentsBuilder;
                if (cache.fvh == null) {
                    // parameters to FVH are not requires since:
                    // first two booleans are not relevant since they are set on the CustomFieldQuery (phrase and fieldMatch)
                    // fragment builders are used explicitly
                    cache.fvh = new org.apache.lucene.search.vectorhighlight.FastVectorHighlighter();
                }
                CustomFieldQuery.highlightFilters.set(field.highlightFilter());
                if (field.requireFieldMatch()) {
                    if (cache.fieldMatchFieldQuery == null) {
                        // we use top level reader to rewrite the query against all readers, with use caching it across hits (and across readers...)
                        cache.fieldMatchFieldQuery = new CustomFieldQuery(context.parsedQuery().query(), hitContext.topLevelReader(), true, field.requireFieldMatch());
                    }
                    fieldQuery = cache.fieldMatchFieldQuery;
                } else {
                    if (cache.noFieldMatchFieldQuery == null) {
                        // we use top level reader to rewrite the query against all readers, with use caching it across hits (and across readers...)
                        cache.noFieldMatchFieldQuery = new CustomFieldQuery(context.parsedQuery().query(), hitContext.topLevelReader(), true, field.requireFieldMatch());
                    }
                    fieldQuery = cache.noFieldMatchFieldQuery;
                }
                cache.mappers.put(mapper, entry);
            }

            String[] fragments;

            // a HACK to make highlighter do highlighting, even though its using the single frag list builder
            int numberOfFragments = field.numberOfFragments() == 0 ? Integer.MAX_VALUE : field.numberOfFragments();
            int fragmentCharSize = field.numberOfFragments() == 0 ? Integer.MAX_VALUE : field.fragmentCharSize();
            // we highlight against the low level reader and docId, because if we load source, we want to reuse it if possible
            fragments = cache.fvh.getBestFragments(fieldQuery, hitContext.reader(), hitContext.docId(), mapper.names().indexName(), fragmentCharSize, numberOfFragments,
                    entry.fragListBuilder, entry.fragmentsBuilder, field.preTags(), field.postTags(), encoder);

            if (fragments != null && fragments.length > 0) {
                return new HighlightField(field.field(), StringText.convertFromStringArray(fragments));
            }
        } catch (Exception e) {
            throw new FetchPhaseExecutionException(context, "Failed to highlight field [" + highlighterContext.fieldName + "]", e);
        }

        return null;
    }

    private class MapperHighlightEntry {
        public FragListBuilder fragListBuilder;
        public FragmentsBuilder fragmentsBuilder;

        public org.apache.lucene.search.highlight.Highlighter highlighter;
    }

    private class HighlighterEntry {
        public org.apache.lucene.search.vectorhighlight.FastVectorHighlighter fvh;
        public FieldQuery noFieldMatchFieldQuery;
        public FieldQuery fieldMatchFieldQuery;
        public Map<FieldMapper, MapperHighlightEntry> mappers = Maps.newHashMap();
    }

    private static class Encoders {
        public static Encoder DEFAULT = new DefaultEncoder();
        public static Encoder HTML = new SimpleHTMLEncoder();
    }
}
