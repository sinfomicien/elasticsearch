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

package org.elasticsearch.test.unit.transport;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.transport.TransportRequestOptions.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public abstract class AbstractSimpleTransportTests {

    protected ThreadPool threadPool;

    protected TransportService serviceA;
    protected TransportService serviceB;
    protected DiscoveryNode serviceANode;
    protected DiscoveryNode serviceBNode;

    @BeforeMethod
    public void setUp() {
        threadPool = new ThreadPool();
        build();
        serviceA.connectToNode(serviceBNode);
        serviceB.connectToNode(serviceANode);
    }

    @AfterMethod
    public void tearDown() {
        serviceA.close();
        serviceB.close();

        threadPool.shutdown();
    }

    protected abstract void build();

    @Test
    public void testHelloWorld() {
        serviceA.registerHandler("sayHello", new BaseTransportRequestHandler<StringMessageRequest>() {
            @Override
            public StringMessageRequest newInstance() {
                return new StringMessageRequest();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void messageReceived(StringMessageRequest request, TransportChannel channel) {
                assertThat("moshe", equalTo(request.message));
                try {
                    channel.sendResponse(new StringMessageResponse("hello " + request.message));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertThat(e.getMessage(), false, equalTo(true));
                }
            }
        });

        TransportFuture<StringMessageResponse> res = serviceB.submitRequest(serviceANode, "sayHello",
                new StringMessageRequest("moshe"), new BaseTransportResponseHandler<StringMessageResponse>() {
            @Override
            public StringMessageResponse newInstance() {
                return new StringMessageResponse();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void handleResponse(StringMessageResponse response) {
                assertThat("hello moshe", equalTo(response.message));
            }

            @Override
            public void handleException(TransportException exp) {
                exp.printStackTrace();
                assertThat("got exception instead of a response: " + exp.getMessage(), false, equalTo(true));
            }
        });

        try {
            StringMessageResponse message = res.get();
            assertThat("hello moshe", equalTo(message.message));
        } catch (Exception e) {
            assertThat(e.getMessage(), false, equalTo(true));
        }

        serviceA.removeHandler("sayHello");
    }

    @Test
    public void testVoidMessageCompressed() {
        serviceA.registerHandler("sayHello", new BaseTransportRequestHandler<TransportRequest.Empty>() {
            @Override
            public TransportRequest.Empty newInstance() {
                return TransportRequest.Empty.INSTANCE;
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void messageReceived(TransportRequest.Empty request, TransportChannel channel) {
                try {
                    channel.sendResponse(TransportResponse.Empty.INSTANCE, TransportResponseOptions.options().withCompress(true));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertThat(e.getMessage(), false, equalTo(true));
                }
            }
        });

        TransportFuture<TransportResponse.Empty> res = serviceB.submitRequest(serviceANode, "sayHello",
                TransportRequest.Empty.INSTANCE, TransportRequestOptions.options().withCompress(true), new BaseTransportResponseHandler<TransportResponse.Empty>() {
            @Override
            public TransportResponse.Empty newInstance() {
                return TransportResponse.Empty.INSTANCE;
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void handleResponse(TransportResponse.Empty response) {
            }

            @Override
            public void handleException(TransportException exp) {
                exp.printStackTrace();
                assertThat("got exception instead of a response: " + exp.getMessage(), false, equalTo(true));
            }
        });

        try {
            TransportResponse.Empty message = res.get();
            assertThat(message, notNullValue());
        } catch (Exception e) {
            assertThat(e.getMessage(), false, equalTo(true));
        }

        serviceA.removeHandler("sayHello");
    }

    @Test
    public void testHelloWorldCompressed() {
        serviceA.registerHandler("sayHello", new BaseTransportRequestHandler<StringMessageRequest>() {
            @Override
            public StringMessageRequest newInstance() {
                return new StringMessageRequest();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void messageReceived(StringMessageRequest request, TransportChannel channel) {
                assertThat("moshe", equalTo(request.message));
                try {
                    channel.sendResponse(new StringMessageResponse("hello " + request.message), TransportResponseOptions.options().withCompress(true));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertThat(e.getMessage(), false, equalTo(true));
                }
            }
        });

        TransportFuture<StringMessageResponse> res = serviceB.submitRequest(serviceANode, "sayHello",
                new StringMessageRequest("moshe"), TransportRequestOptions.options().withCompress(true), new BaseTransportResponseHandler<StringMessageResponse>() {
            @Override
            public StringMessageResponse newInstance() {
                return new StringMessageResponse();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void handleResponse(StringMessageResponse response) {
                assertThat("hello moshe", equalTo(response.message));
            }

            @Override
            public void handleException(TransportException exp) {
                exp.printStackTrace();
                assertThat("got exception instead of a response: " + exp.getMessage(), false, equalTo(true));
            }
        });

        try {
            StringMessageResponse message = res.get();
            assertThat("hello moshe", equalTo(message.message));
        } catch (Exception e) {
            assertThat(e.getMessage(), false, equalTo(true));
        }

        serviceA.removeHandler("sayHello");
    }

    @Test
    public void testErrorMessage() {
        serviceA.registerHandler("sayHelloException", new BaseTransportRequestHandler<StringMessageRequest>() {
            @Override
            public StringMessageRequest newInstance() {
                return new StringMessageRequest();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void messageReceived(StringMessageRequest request, TransportChannel channel) throws Exception {
                assertThat("moshe", equalTo(request.message));
                throw new RuntimeException("bad message !!!");
            }
        });

        TransportFuture<StringMessageResponse> res = serviceB.submitRequest(serviceANode, "sayHelloException",
                new StringMessageRequest("moshe"), new BaseTransportResponseHandler<StringMessageResponse>() {
            @Override
            public StringMessageResponse newInstance() {
                return new StringMessageResponse();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void handleResponse(StringMessageResponse response) {
                assertThat("got response instead of exception", false, equalTo(true));
            }

            @Override
            public void handleException(TransportException exp) {
                assertThat("bad message !!!", equalTo(exp.getCause().getMessage()));
            }
        });

        try {
            res.txGet();
            assertThat("exception should be thrown", false, equalTo(true));
        } catch (Exception e) {
            assertThat("bad message !!!", equalTo(e.getCause().getMessage()));
        }

        serviceA.removeHandler("sayHelloException");
    }

    @Test
    public void testDisconnectListener() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        TransportConnectionListener disconnectListener = new TransportConnectionListener() {
            @Override
            public void onNodeConnected(DiscoveryNode node) {
                throw new RuntimeException("Should not be called");
            }

            @Override
            public void onNodeDisconnected(DiscoveryNode node) {
                latch.countDown();
            }
        };
        serviceA.addConnectionListener(disconnectListener);
        serviceB.close();
        assertThat(latch.await(5, TimeUnit.SECONDS), equalTo(true));
    }

    @Test
    public void testTimeoutSendExceptionWithNeverSendingBackResponse() throws Exception {
        serviceA.registerHandler("sayHelloTimeoutNoResponse", new BaseTransportRequestHandler<StringMessageRequest>() {
            @Override
            public StringMessageRequest newInstance() {
                return new StringMessageRequest();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void messageReceived(StringMessageRequest request, TransportChannel channel) {
                assertThat("moshe", equalTo(request.message));
                // don't send back a response
//                try {
//                    channel.sendResponse(new StringMessage("hello " + request.message));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    assertThat(e.getMessage(), false, equalTo(true));
//                }
            }
        });

        TransportFuture<StringMessageResponse> res = serviceB.submitRequest(serviceANode, "sayHelloTimeoutNoResponse",
                new StringMessageRequest("moshe"), options().withTimeout(100), new BaseTransportResponseHandler<StringMessageResponse>() {
            @Override
            public StringMessageResponse newInstance() {
                return new StringMessageResponse();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void handleResponse(StringMessageResponse response) {
                assertThat("got response instead of exception", false, equalTo(true));
            }

            @Override
            public void handleException(TransportException exp) {
                assertThat(exp, instanceOf(ReceiveTimeoutTransportException.class));
            }
        });

        try {
            StringMessageResponse message = res.txGet();
            assertThat("exception should be thrown", false, equalTo(true));
        } catch (Exception e) {
            assertThat(e, instanceOf(ReceiveTimeoutTransportException.class));
        }

        serviceA.removeHandler("sayHelloTimeoutNoResponse");
    }

    @Test
    public void testTimeoutSendExceptionWithDelayedResponse() throws Exception {
        serviceA.registerHandler("sayHelloTimeoutDelayedResponse", new BaseTransportRequestHandler<StringMessageRequest>() {
            @Override
            public StringMessageRequest newInstance() {
                return new StringMessageRequest();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void messageReceived(StringMessageRequest request, TransportChannel channel) {
                TimeValue sleep = TimeValue.parseTimeValue(request.message, null);
                try {
                    Thread.sleep(sleep.millis());
                } catch (InterruptedException e) {
                    // ignore
                }
                try {
                    channel.sendResponse(new StringMessageResponse("hello " + request.message));
                } catch (IOException e) {
                    e.printStackTrace();
                    assertThat(e.getMessage(), false, equalTo(true));
                }
            }
        });

        TransportFuture<StringMessageResponse> res = serviceB.submitRequest(serviceANode, "sayHelloTimeoutDelayedResponse",
                new StringMessageRequest("300ms"), options().withTimeout(100), new BaseTransportResponseHandler<StringMessageResponse>() {
            @Override
            public StringMessageResponse newInstance() {
                return new StringMessageResponse();
            }

            @Override
            public String executor() {
                return ThreadPool.Names.GENERIC;
            }

            @Override
            public void handleResponse(StringMessageResponse response) {
                assertThat("got response instead of exception", false, equalTo(true));
            }

            @Override
            public void handleException(TransportException exp) {
                assertThat(exp, instanceOf(ReceiveTimeoutTransportException.class));
            }
        });

        try {
            StringMessageResponse message = res.txGet();
            assertThat("exception should be thrown", false, equalTo(true));
        } catch (Exception e) {
            assertThat(e, instanceOf(ReceiveTimeoutTransportException.class));
        }

        // sleep for 400 millis to make sure we get back the response
        Thread.sleep(400);

        for (int i = 0; i < 10; i++) {
            final int counter = i;
            // now, try and send another request, this times, with a short timeout
            res = serviceB.submitRequest(serviceANode, "sayHelloTimeoutDelayedResponse",
                    new StringMessageRequest(counter + "ms"), options().withTimeout(100), new BaseTransportResponseHandler<StringMessageResponse>() {
                @Override
                public StringMessageResponse newInstance() {
                    return new StringMessageResponse();
                }

                @Override
                public String executor() {
                    return ThreadPool.Names.GENERIC;
                }

                @Override
                public void handleResponse(StringMessageResponse response) {
                    assertThat("hello " + counter + "ms", equalTo(response.message));
                }

                @Override
                public void handleException(TransportException exp) {
                    exp.printStackTrace();
                    assertThat("got exception instead of a response for " + counter + ": " + exp.getDetailedMessage(), false, equalTo(true));
                }
            });

            StringMessageResponse message = res.txGet();
            assertThat(message.message, equalTo("hello " + counter + "ms"));
        }

        serviceA.removeHandler("sayHelloTimeoutDelayedResponse");
    }

    static class StringMessageRequest extends TransportRequest {

        private String message;

        StringMessageRequest(String message) {
            this.message = message;
        }

        StringMessageRequest() {
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            message = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(message);
        }
    }

    static class StringMessageResponse extends TransportResponse {

        private String message;

        StringMessageResponse(String message) {
            this.message = message;
        }

        StringMessageResponse() {
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            message = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(message);
        }
    }
}
