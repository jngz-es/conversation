/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.action;

import static org.opensearch.conversation.common.CommonValue.ANSWER_FIELD;
import static org.opensearch.conversation.common.CommonValue.CREATED_TIME_FIELD;
import static org.opensearch.conversation.common.CommonValue.MESSAGE_INDEX;
import static org.opensearch.conversation.common.CommonValue.MODEL_ID_FIELD;
import static org.opensearch.conversation.common.CommonValue.QUESTION_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_ID_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_METADATA_INDEX;
import static org.opensearch.conversation.common.CommonValue.SESSION_TITLE_FIELD;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.opensearch.action.ActionListener;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.HandledTransportAction;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.common.Strings;
import org.opensearch.common.inject.Inject;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.conversation.input.ChatInput;
import org.opensearch.conversation.memory.opensearch.OpensearchIndicesHandler;
import org.opensearch.conversation.request.ChatRequest;
import org.opensearch.conversation.response.ChatResponse;
import org.opensearch.conversation.transport.ChatAction;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.ml.client.MachineLearningNodeClient;
import org.opensearch.ml.common.FunctionName;
import org.opensearch.ml.common.dataset.remote.RemoteInferenceInputDataSet;
import org.opensearch.ml.common.input.remote.RemoteInferenceMLInput;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportService;

import com.google.gson.Gson;

@Log4j2
public class TransportChatAction extends HandledTransportAction<ActionRequest, ChatResponse> {
    private final TransportService transportService;
    private final OpensearchIndicesHandler indicesHandler;
    private final Client client;
    private final MachineLearningNodeClient mlClient;

    @Inject
    public TransportChatAction(
        TransportService transportService,
        ActionFilters actionFilters,
        OpensearchIndicesHandler indicesHandler,
        Client client
    ) {
        super(ChatAction.NAME, transportService, actionFilters, ChatRequest::new);
        this.transportService = transportService;
        this.indicesHandler = indicesHandler;
        this.client = client;
        mlClient = new MachineLearningNodeClient(this.client);
    }

    @Override
    protected void doExecute(Task task, ActionRequest request, ActionListener<ChatResponse> listener) {
        log.error("executing chat 3");
        ChatRequest chatRequest = ChatRequest.fromActionRequest(request);
        ChatInput chatInput = chatRequest.getChatInput();
        if (chatInput.getModelId() == null) {
            throw new IllegalArgumentException("The model id is required.");
        }

        // Get most recent 20 rounds of session history
        log.error("getting history");
        List<String> historicalMessages = new ArrayList<>();
        if (!Strings.isNullOrEmpty(chatInput.getSessionId())) {
            int sz = 20;
            try (ThreadContext.StoredContext context = client.threadPool().getThreadContext().stashContext()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder(SESSION_ID_FIELD, chatInput.getSessionId());
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.size(sz);
                searchSourceBuilder.query(termQueryBuilder);
                searchSourceBuilder.sort(CREATED_TIME_FIELD, SortOrder.DESC);
                SearchRequest searchRequest = new SearchRequest(MESSAGE_INDEX).source(searchSourceBuilder);

                client.search(searchRequest, ActionListener.runBefore(ActionListener.wrap(r -> {
                    log.debug("Completed search request to get most recent 20 rounds of session history.");
                    SearchHit[] hits = r.getHits().getHits();
                    if (hits != null && hits.length > 0) {
                        for (int i = Math.min(hits.length, sz) - 1; i >= 0; i--) {
                            SearchHit hit = hits[i];
                            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                            String question = (String) sourceAsMap.get(QUESTION_FIELD);
                            String answer = (String) sourceAsMap.get(ANSWER_FIELD);
                            historicalMessages.add(question);
                            historicalMessages.add(answer);
                        }
                    } else {
                        log.error("Not found most recent messages.");
                    }
                }, e -> {
                    log.error("Failed to get most recent messages from session history index. " + e);
                    listener.onFailure(e);
                }), () -> context.restore()));
            } catch (Exception e) {
                log.error("Failed to get most recent messages from session history index outside. " + e);
                listener.onFailure(e);
            }
        }
        log.error("got the history");

        Map<String, String> params = chatInput.getParameters();
//        params.put("chat_history", new Gson().toJson(historicalMessages));
        params.put("chat_history", StringUtils.join(historicalMessages, '\n'));
        log.error("parameters is ");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            log.error("***" + entry.getKey() + " : " + entry.getValue());
        }
        RemoteInferenceMLInput mlInput = new RemoteInferenceMLInput(FunctionName.REMOTE, new RemoteInferenceInputDataSet(params));
        log.error("mlInput is : ({}) ", mlInput);
        log.error("chatInput model id is : ({}) ", chatInput.getModelId());
        mlClient.predict(chatInput.getModelId(), mlInput, ActionListener.wrap(mlOutput -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStreamStreamOutput outputStreamStreamOutput = new OutputStreamStreamOutput(byteArrayOutputStream);
            mlOutput.writeTo(outputStreamStreamOutput);
            String answer = byteArrayOutputStream.toString();
            log.error("Chat output is : ({}) ", mlOutput);
            log.error("Chat response for input {} is : ({}) ", chatInput, answer);

            try {
                Instant now = Instant.now();
                AtomicReference<String> sessionId = new AtomicReference<>(chatInput.getSessionId());
                if (Strings.isNullOrEmpty(sessionId.get())) {
                    log.info("Ingesting session meta index.");
                    indicesHandler.initSessionMetaIndex(ActionListener.wrap(indexCreated -> {
                        if (!indexCreated) {
                            listener.onFailure(new RuntimeException("No response to create session meta index"));
                            return;
                        }
                        try (ThreadContext.StoredContext context = client.threadPool().getThreadContext().stashContext()) {
                            ActionListener<IndexResponse> indexResponseListener = ActionListener.wrap(r -> {
                                sessionId.set(r.getId());
                                log.info(
                                    "Session meta has been saved into index, result:{}, session id: {}",
                                    r.getResult(),
                                    sessionId.get()
                                );
                            }, e -> { listener.onFailure(e); });

                            IndexRequest indexRequest = new IndexRequest(SESSION_METADATA_INDEX);
                            String title = chatInput.getParameters().get(QUESTION_FIELD);
                            indexRequest.source(
                                Map.of(
                                    SESSION_TITLE_FIELD,
                                    title,
                                    MODEL_ID_FIELD,
                                    chatInput.getModelId(),
                                    CREATED_TIME_FIELD,
                                    Instant.now().toEpochMilli()
                                )
                            );
                            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                            client.index(indexRequest, ActionListener.runBefore(indexResponseListener, () -> context.restore()));
                        } catch (Exception e) {
                            log.error("Failed to save session metadata", e);
                            listener.onFailure(e);
                        }
                    }, e -> {
                        log.error("Failed to ingest session metadata index", e);
                        listener.onFailure(e);
                    }));
                }

                log.info("Ingesting message index.");
                indicesHandler.initMessageIndex(ActionListener.wrap(indexCreated -> {
                    if (!indexCreated) {
                        listener.onFailure(new RuntimeException("No response to create message index"));
                        return;
                    }

                    try (ThreadContext.StoredContext context = client.threadPool().getThreadContext().stashContext()) {
                        ActionListener<IndexResponse> indexResponseListener = ActionListener.wrap(r -> {
                            log.info("Messages have been saved into index, result:{}, session id: {}", r.getResult(), sessionId.get());
                            ChatResponse response = ChatResponse.builder().sessionId(sessionId.get()).answer(answer).build();
                            listener.onResponse(response);
                        }, e -> { listener.onFailure(e); });

                        IndexRequest indexRequest = new IndexRequest(MESSAGE_INDEX);
                        indexRequest.source(
                            Map.of(
                                SESSION_ID_FIELD,
                                sessionId.get(),
                                QUESTION_FIELD,
                                chatInput.getParameters().get(QUESTION_FIELD),
                                ANSWER_FIELD,
                                answer,
                                CREATED_TIME_FIELD,
                                Instant.now().toEpochMilli()
                            )
                        );
                        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                        client.index(indexRequest, ActionListener.runBefore(indexResponseListener, () -> context.restore()));
                    } catch (Exception e) {
                        log.error("Failed to save messages", e);
                        listener.onFailure(e);
                    }
                }, e -> {
                    log.error("Failed to ingest messages index", e);
                    listener.onFailure(e);
                }));

            } catch (IllegalArgumentException illegalArgumentException) {
                log.error("Failed to chat ", illegalArgumentException);
                listener.onFailure(illegalArgumentException);
            } catch (Exception e) {
                log.error("Failed to chat ", e);
                listener.onFailure(e);
            }
        }, e -> { listener.onFailure(e); }));

    }
}
