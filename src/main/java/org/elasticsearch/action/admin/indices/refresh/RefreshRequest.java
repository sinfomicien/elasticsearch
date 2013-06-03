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

package org.elasticsearch.action.admin.indices.refresh;

import org.elasticsearch.action.support.broadcast.BroadcastOperationRequest;
import org.elasticsearch.action.support.broadcast.BroadcastOperationThreading;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * A refresh request making all operations performed since the last refresh available for search. The (near) real-time
 * capabilities depends on the index engine used. For example, the robin one requires refresh to be called, but by
 * default a refresh is scheduled periodically.
 *
 * @see org.elasticsearch.client.Requests#refreshRequest(String...)
 * @see org.elasticsearch.client.IndicesAdminClient#refresh(RefreshRequest)
 * @see RefreshResponse
 */
public class RefreshRequest extends BroadcastOperationRequest<RefreshRequest> {

    private boolean waitForOperations = true;

    RefreshRequest() {
    }

    public RefreshRequest(String... indices) {
        super(indices);
        // we want to do the refresh in parallel on local shards...
        operationThreading(BroadcastOperationThreading.THREAD_PER_SHARD);
    }

    public boolean waitForOperations() {
        return waitForOperations;
    }

    public RefreshRequest waitForOperations(boolean waitForOperations) {
        this.waitForOperations = waitForOperations;
        return this;
    }

    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        waitForOperations = in.readBoolean();
    }

    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(waitForOperations);
    }
}
