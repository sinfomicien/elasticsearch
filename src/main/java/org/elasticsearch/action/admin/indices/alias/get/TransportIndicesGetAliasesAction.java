/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.elasticsearch.action.admin.indices.alias.get;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.support.master.TransportMasterNodeOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.AliasMissingException;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.List;
import java.util.Map;

/**
 */
public class TransportIndicesGetAliasesAction extends TransportMasterNodeOperationAction<IndicesGetAliasesRequest, IndicesGetAliasesResponse> {

    @Inject
    public TransportIndicesGetAliasesAction(Settings settings, TransportService transportService, ClusterService clusterService, ThreadPool threadPool) {
        super(settings, transportService, clusterService, threadPool);
    }

    @Override
    protected String transportAction() {
        return IndicesGetAliasesAction.NAME;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.MANAGEMENT;
    }

    @Override
    protected IndicesGetAliasesRequest newRequest() {
        return new IndicesGetAliasesRequest();
    }

    @Override
    protected IndicesGetAliasesResponse newResponse() {
        return new IndicesGetAliasesResponse();
    }

    @Override
    protected IndicesGetAliasesResponse masterOperation(IndicesGetAliasesRequest request, ClusterState state) throws ElasticSearchException {
        String[] concreteIndices = state.metaData().concreteIndices(request.indices(), request.ignoreIndices(), true);
        request.indices(concreteIndices);

        @SuppressWarnings("unchecked") // ImmutableList to List results incompatible type
        Map<String, List<AliasMetaData>> result = (Map) state.metaData().findAliases(request.aliases(), request.indices());
        if (result.isEmpty()) {
            throw new AliasMissingException(request.aliases());
        }
        return new IndicesGetAliasesResponse(result);
    }

}
