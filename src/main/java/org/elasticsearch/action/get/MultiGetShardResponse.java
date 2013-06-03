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

package org.elasticsearch.action.get;

import gnu.trove.list.array.TIntArrayList;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiGetShardResponse extends ActionResponse {

    TIntArrayList locations;
    List<GetResponse> responses;
    List<MultiGetResponse.Failure> failures;

    MultiGetShardResponse() {
        locations = new TIntArrayList();
        responses = new ArrayList<GetResponse>();
        failures = new ArrayList<MultiGetResponse.Failure>();
    }

    public void add(int location, GetResponse response) {
        locations.add(location);
        responses.add(response);
        failures.add(null);
    }

    public void add(int location, MultiGetResponse.Failure failure) {
        locations.add(location);
        responses.add(null);
        failures.add(failure);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        int size = in.readVInt();
        locations = new TIntArrayList(size);
        responses = new ArrayList<GetResponse>(size);
        failures = new ArrayList<MultiGetResponse.Failure>(size);
        for (int i = 0; i < size; i++) {
            locations.add(in.readVInt());
            if (in.readBoolean()) {
                GetResponse response = new GetResponse();
                response.readFrom(in);
                responses.add(response);
            } else {
                responses.add(null);
            }
            if (in.readBoolean()) {
                failures.add(MultiGetResponse.Failure.readFailure(in));
            } else {
                failures.add(null);
            }
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeVInt(locations.size());
        for (int i = 0; i < locations.size(); i++) {
            out.writeVInt(locations.get(i));
            if (responses.get(i) == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                responses.get(i).writeTo(out);
            }
            if (failures.get(i) == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                failures.get(i).writeTo(out);
            }
        }
    }
}