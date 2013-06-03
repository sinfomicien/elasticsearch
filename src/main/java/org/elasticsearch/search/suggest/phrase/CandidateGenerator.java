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

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGenerator.Candidate;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGenerator.CandidateSet;

//TODO public for tests
public abstract class CandidateGenerator {

    public abstract boolean isKnownWord(BytesRef term) throws IOException;

    public abstract long frequency(BytesRef term) throws IOException;

    public CandidateSet drawCandidates(BytesRef term) throws IOException {
        CandidateSet set = new CandidateSet(Candidate.EMPTY,  createCandidate(term));
        return drawCandidates(set);
    }
    
    public Candidate createCandidate(BytesRef term) throws IOException {
        return createCandidate(term, frequency(term), 1.0);
    }
    public abstract Candidate createCandidate(BytesRef term, long frequency, double channelScore) throws IOException;

    public abstract CandidateSet drawCandidates(CandidateSet set) throws IOException;

}