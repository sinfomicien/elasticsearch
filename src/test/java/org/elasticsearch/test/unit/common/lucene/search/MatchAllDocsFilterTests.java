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

package org.elasticsearch.test.unit.common.lucene.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.lucene.search.XConstantScoreQuery;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 */
public class MatchAllDocsFilterTests {

    @Test
    public void testMatchAllDocsFilter() throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(dir, new IndexWriterConfig(Lucene.VERSION, Lucene.STANDARD_ANALYZER));

        Document document = new Document();
        document.add(new TextField("_id", "1", Field.Store.YES));
        document.add(new TextField("text", "lucene", Field.Store.YES));
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new TextField("_id", "2", Field.Store.YES));
        document.add(new TextField("text", "lucene release", Field.Store.YES));
        indexWriter.addDocument(document);

        IndexReader reader = IndexReader.open(indexWriter, true);
        IndexSearcher searcher = new IndexSearcher(reader);

        XConstantScoreQuery query = new XConstantScoreQuery(Queries.MATCH_ALL_FILTER);
        long count = Lucene.count(searcher, query);
        assertThat(count, equalTo(2l));

        reader.close();
        indexWriter.close();
    }
}
