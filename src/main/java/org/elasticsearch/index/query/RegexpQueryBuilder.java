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

package org.elasticsearch.index.query;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * A Query that does fuzzy matching for a specific value.
 *
 *
 */
public class RegexpQueryBuilder extends BaseQueryBuilder implements BoostableQueryBuilder<RegexpQueryBuilder> {

    private final String name;
    private final String regexp;

    private int flags = -1;
    private float boost = -1;
    private String rewrite;

    /**
     * Constructs a new term query.
     *
     * @param name  The name of the field
     * @param regexp The regular expression
     */
    public RegexpQueryBuilder(String name, String regexp) {
        this.name = name;
        this.regexp = regexp;
    }

    /**
     * Sets the boost for this query.  Documents matching this query will (in addition to the normal
     * weightings) have their score multiplied by the boost provided.
     */
    public RegexpQueryBuilder boost(float boost) {
        this.boost = boost;
        return this;
    }

    public RegexpQueryBuilder flags(RegexpFlag... flags) {
        int value = 0;
        if (flags.length == 0) {
            value = RegexpFlag.ALL.value;
        } else {
            for (RegexpFlag flag : flags) {
                value |= flag.value;
            }
        }
        this.flags = value;
        return this;
    }

    public RegexpQueryBuilder rewrite(String rewrite) {
        this.rewrite = rewrite;
        return this;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(RegexpQueryParser.NAME);
        if (boost == -1 && rewrite == null) {
            builder.field(name, regexp);
        } else {
            builder.startObject(name);
            builder.field("value", regexp);
            if (flags != -1) {
                builder.field("flags_value", flags);
            }
            if (boost != -1) {
                builder.field("boost", boost);
            }
            if (rewrite != null) {
                builder.field("rewrite", rewrite);
            }
            builder.endObject();
        }
        builder.endObject();
    }
}