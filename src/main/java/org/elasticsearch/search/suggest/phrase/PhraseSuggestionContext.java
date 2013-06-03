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
package org.elasticsearch.search.suggest.phrase;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.search.suggest.DirectSpellcheckerSettings;
import org.elasticsearch.search.suggest.Suggester;
import org.elasticsearch.search.suggest.SuggestionSearchContext.SuggestionContext;

class PhraseSuggestionContext extends SuggestionContext {
    private final BytesRef SEPARATOR = new BytesRef(" ");

    private float maxErrors = 0.5f;
    private BytesRef separator = SEPARATOR;
    private float realworldErrorLikelihood = 0.95f;
    private List<DirectCandidateGenerator> generators = new ArrayList<PhraseSuggestionContext.DirectCandidateGenerator>();
    private int gramSize = 1;
    private float confidence = 1.0f;

    private WordScorer.WordScorerFactory scorer;

    private boolean requireUnigram = true;

    public PhraseSuggestionContext(Suggester<? extends PhraseSuggestionContext> suggester) {
        super(suggester);
    }

    public float maxErrors() {
        return maxErrors;
    }

    public void setMaxErrors(Float maxErrors) {
        this.maxErrors = maxErrors;
    }

    public BytesRef separator() {
        return separator;
    }

    public void setSeparator(BytesRef separator) {
        this.separator = separator;
    }

    public Float realworldErrorLikelyhood() {
        return realworldErrorLikelihood;
    }

    public void setRealWordErrorLikelihood(Float realworldErrorLikelihood) {
        this.realworldErrorLikelihood = realworldErrorLikelihood;
    }

    public void addGenerator(DirectCandidateGenerator generator) {
        this.generators.add(generator);
    }
    
    public List<DirectCandidateGenerator> generators() {
        return this.generators ;
    }
    
    public void setGramSize(int gramSize) {
        this.gramSize = gramSize;
    }
    
    public int gramSize() {
        return gramSize;
    }
    
    public float confidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public void setModel(WordScorer.WordScorerFactory scorer) {
        this.scorer = scorer;
    }

    public WordScorer.WordScorerFactory model() {
        return scorer;
    }
    
    static class DirectCandidateGenerator extends DirectSpellcheckerSettings {
        private Analyzer preFilter;
        private Analyzer postFilter;
        private String field;
        private int size = 5;

        public String field() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public int size() {
            return size;
        }

        public void size(int size) {
            if (size <= 0) {
                throw new ElasticSearchIllegalArgumentException("Size must be positive");
            }
            this.size = size;
        }
        
        public Analyzer preFilter() {
            return preFilter;
        }

        public void preFilter(Analyzer preFilter) {
            this.preFilter = preFilter;
        }

        public Analyzer postFilter() {
            return postFilter;
        }

        public void postFilter(Analyzer postFilter) {
            this.postFilter = postFilter;
        }
        
        
    }

    public void setRequireUnigram(boolean requireUnigram) {
        this.requireUnigram  = requireUnigram;
    }
    
    public boolean getRequireUnigram() {
        return requireUnigram;
    }
   
}