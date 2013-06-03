/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.indices.template.put;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.master.MasterNodeOperationRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.internal.InternalIndicesAdminClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.util.Map;

/**
 *
 */
public class PutIndexTemplateRequestBuilder extends MasterNodeOperationRequestBuilder<PutIndexTemplateRequest, PutIndexTemplateResponse, PutIndexTemplateRequestBuilder> {

    public PutIndexTemplateRequestBuilder(IndicesAdminClient indicesClient) {
        super((InternalIndicesAdminClient) indicesClient, new PutIndexTemplateRequest());
    }

    public PutIndexTemplateRequestBuilder(IndicesAdminClient indicesClient, String name) {
        super((InternalIndicesAdminClient) indicesClient, new PutIndexTemplateRequest(name));
    }

    /**
     * Sets the template match expression that will be used to match on indices created.
     */
    public PutIndexTemplateRequestBuilder setTemplate(String template) {
        request.template(template);
        return this;
    }

    /**
     * Sets the order of this template if more than one template matches.
     */
    public PutIndexTemplateRequestBuilder setOrder(int order) {
        request.order(order);
        return this;
    }

    /**
     * Set to <tt>true</tt> to force only creation, not an update of an index template. If it already
     * exists, it will fail with an {@link org.elasticsearch.indices.IndexTemplateAlreadyExistsException}.
     */
    public PutIndexTemplateRequestBuilder setCreate(boolean create) {
        request.create(create);
        return this;
    }

    /**
     * The settings to created the index template with.
     */
    public PutIndexTemplateRequestBuilder setSettings(Settings settings) {
        request.settings(settings);
        return this;
    }

    /**
     * The settings to created the index template with.
     */
    public PutIndexTemplateRequestBuilder setSettings(Settings.Builder settings) {
        request.settings(settings);
        return this;
    }

    /**
     * The settings to crete the index template with (either json/yaml/properties format)
     */
    public PutIndexTemplateRequestBuilder setSettings(String source) {
        request.settings(source);
        return this;
    }

    /**
     * The settings to crete the index template with (either json/yaml/properties format)
     */
    public PutIndexTemplateRequestBuilder setSettings(Map<String, Object> source) {
        request.settings(source);
        return this;
    }

    /**
     * Adds mapping that will be added when the index template gets created.
     *
     * @param type   The mapping type
     * @param source The mapping source
     */
    public PutIndexTemplateRequestBuilder addMapping(String type, String source) {
        request.mapping(type, source);
        return this;
    }

    /**
     * The cause for this index template creation.
     */
    public PutIndexTemplateRequestBuilder cause(String cause) {
        request.cause(cause);
        return this;
    }

    /**
     * Adds mapping that will be added when the index template gets created.
     *
     * @param type   The mapping type
     * @param source The mapping source
     */
    public PutIndexTemplateRequestBuilder addMapping(String type, XContentBuilder source) {
        request.mapping(type, source);
        return this;
    }

    /**
     * Adds mapping that will be added when the index gets created.
     *
     * @param type   The mapping type
     * @param source The mapping source
     */
    public PutIndexTemplateRequestBuilder addMapping(String type, Map<String, Object> source) {
        request.mapping(type, source);
        return this;
    }

    /**
     * The template source definition.
     */
    public PutIndexTemplateRequestBuilder setSource(XContentBuilder templateBuilder) {
        request.source(templateBuilder);
        return this;
    }

    /**
     * The template source definition.
     */
    public PutIndexTemplateRequestBuilder setSource(Map templateSource) {
        request.source(templateSource);
        return this;
    }

    /**
     * The template source definition.
     */
    public PutIndexTemplateRequestBuilder setSource(String templateSource) {
        request.source(templateSource);
        return this;
    }

    /**
     * The template source definition.
     */
    public PutIndexTemplateRequestBuilder setSource(BytesReference templateSource) {
        request.source(templateSource);
        return this;
    }

    /**
     * The template source definition.
     */
    public PutIndexTemplateRequestBuilder setSource(byte[] templateSource) {
        request.source(templateSource);
        return this;
    }

    /**
     * The template source definition.
     */
    public PutIndexTemplateRequestBuilder setSource(byte[] templateSource, int offset, int length) {
        request.source(templateSource, offset, length);
        return this;
    }

    /**
     * Timeout to wait for the index creation to be acknowledged by current cluster nodes. Defaults
     * to <tt>10s</tt>.
     */
    public PutIndexTemplateRequestBuilder setTimeout(TimeValue timeout) {
        request.timeout(timeout);
        return this;
    }

    /**
     * Timeout to wait for the index creation to be acknowledged by current cluster nodes. Defaults
     * to <tt>10s</tt>.
     */
    public PutIndexTemplateRequestBuilder setTimeout(String timeout) {
        request.timeout(timeout);
        return this;
    }

    @Override
    protected void doExecute(ActionListener<PutIndexTemplateResponse> listener) {
        ((IndicesAdminClient) client).putTemplate(request, listener);
    }
}
