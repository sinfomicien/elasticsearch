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

import com.spatial4j.core.shape.Shape;
import org.elasticsearch.common.geo.GeoJSONShapeSerializer;
import org.elasticsearch.common.geo.SpatialStrategy;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * {@link QueryBuilder} that builds a GeoShape Query
 */
public class GeoShapeQueryBuilder extends BaseQueryBuilder implements BoostableQueryBuilder<GeoShapeQueryBuilder> {

    private final String name;

    private SpatialStrategy strategy = null;

    private final Shape shape;

    private float boost = -1;

    private final String indexedShapeId;
    private final String indexedShapeType;

    private String indexedShapeIndex;
    private String indexedShapeFieldName;

    /**
     * Creates a new GeoShapeQueryBuilder whose Query will be against the
     * given field name using the given Shape
     *
     * @param name  Name of the field that will be queried
     * @param shape Shape used in the query
     */
    public GeoShapeQueryBuilder(String name, Shape shape) {
        this(name, shape, null, null);
    }

    /**
     * Creates a new GeoShapeQueryBuilder whose Query will be against the given field name
     * and will use the Shape found with the given ID in the given type
     *
     * @param name             Name of the field that will be queried
     * @param indexedShapeId   ID of the indexed Shape that will be used in the Query
     * @param indexedShapeType Index type of the indexed Shapes
     */
    public GeoShapeQueryBuilder(String name, String indexedShapeId, String indexedShapeType) {
        this(name, null, indexedShapeId, indexedShapeType);
    }

    private GeoShapeQueryBuilder(String name, Shape shape, String indexedShapeId, String indexedShapeType) {
        this.name = name;
        this.shape = shape;
        this.indexedShapeId = indexedShapeId;
        this.indexedShapeType = indexedShapeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoShapeQueryBuilder boost(float boost) {
        this.boost = boost;
        return this;
    }

    /**
     * Defines which spatial strategy will be used for building the geo shape query. When not set, the strategy that
     * will be used will be the one that is associated with the geo shape field in the mappings.
     *
     * @param strategy The spatial strategy to use for building the geo shape query
     * @return this
     */
    public GeoShapeQueryBuilder strategy(SpatialStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    /**
     * Sets the name of the index where the indexed Shape can be found
     *
     * @param indexedShapeIndex Name of the index where the indexed Shape is
     * @return this
     */
    public GeoShapeQueryBuilder indexedShapeIndex(String indexedShapeIndex) {
        this.indexedShapeIndex = indexedShapeIndex;
        return this;
    }

    /**
     * Sets the name of the field in the indexed Shape document that has the Shape itself
     *
     * @param indexedShapeFieldName Name of the field where the Shape itself is defined
     * @return this
     */
    public GeoShapeQueryBuilder indexedShapeFieldName(String indexedShapeFieldName) {
        this.indexedShapeFieldName = indexedShapeFieldName;
        return this;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(GeoShapeQueryParser.NAME);

        builder.startObject(name);

        if (strategy != null) {
            builder.field("strategy", strategy.getStrategyName());
        }

        if (shape != null) {
            builder.startObject("shape");
            GeoJSONShapeSerializer.serialize(shape, builder);
            builder.endObject();
        } else {
            builder.startObject("indexed_shape")
                    .field("id", indexedShapeId)
                    .field("type", indexedShapeType);
            if (indexedShapeIndex != null) {
                builder.field("index", indexedShapeIndex);
            }
            if (indexedShapeFieldName != null) {
                builder.field("shape_field_name", indexedShapeFieldName);
            }
            builder.endObject();
        }

        if (boost != -1) {
            builder.field("boost", boost);
        }

        builder.endObject();
    }

}
