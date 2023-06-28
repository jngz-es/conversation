/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.action;

import static org.opensearch.conversation.common.CommonValue.ANSWER_FIELD;
import static org.opensearch.conversation.common.CommonValue.CREATED_TIME_FIELD;
import static org.opensearch.conversation.common.CommonValue.MESSAGE_INDEX;
import static org.opensearch.conversation.common.CommonValue.QUESTION_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_ID_FIELD;
import static org.opensearch.ml.common.utils.StringUtils.gson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import org.opensearch.action.ActionListener;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.LatchedActionListener;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.HandledTransportAction;
import org.opensearch.client.Client;
import org.opensearch.common.Strings;
import org.opensearch.common.inject.Inject;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.conversation.memory.opensearch.OpensearchIndicesHandler;
import org.opensearch.conversation.request.GetSessionHistoryRequest;
import org.opensearch.conversation.response.GetSessionHistoryResponse;
import org.opensearch.conversation.transport.GetSessionHistoryAction;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.ml.common.exception.MLException;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.fetch.subphase.FetchSourceContext;
import org.opensearch.search.sort.SortOrder;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportService;

@Log4j2
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TransportGetSessionHistoryAction extends HandledTransportAction<ActionRequest, GetSessionHistoryResponse> {
    private final TransportService transportService;
    private final OpensearchIndicesHandler indicesHandler;
    private final Client client;
    private final NamedXContentRegistry xContentRegistry;

    @Inject
    public TransportGetSessionHistoryAction(
        TransportService transportService,
        ActionFilters actionFilters,
        OpensearchIndicesHandler indicesHandler,
        Client client,
        NamedXContentRegistry xContentRegistry
    ) {
        super(GetSessionHistoryAction.NAME, transportService, actionFilters, GetSessionHistoryRequest::new);
        this.transportService = transportService;
        this.indicesHandler = indicesHandler;
        this.client = client;
        this.xContentRegistry = xContentRegistry;
    }

    @Override
    protected void doExecute(Task task, ActionRequest request, ActionListener<GetSessionHistoryResponse> listener) {
        GetSessionHistoryRequest getSessionHistoryRequest = GetSessionHistoryRequest.fromActionRequest(request);
        String sessionId = getSessionHistoryRequest.getSessionId();
        int from = getSessionHistoryRequest.getFrom();
        int size = getSessionHistoryRequest.getSize();

        try (ThreadContext.StoredContext context = client.threadPool().getThreadContext().stashContext()) {
            // TODO: scroll search here
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder(SESSION_ID_FIELD, sessionId);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.fetchSource(new FetchSourceContext(true, Strings.EMPTY_ARRAY, Strings.EMPTY_ARRAY));
            searchSourceBuilder.from(from);
            searchSourceBuilder.size(size);
            searchSourceBuilder.query(termQueryBuilder);
            searchSourceBuilder.sort(CREATED_TIME_FIELD, SortOrder.ASC);
            SearchRequest searchRequest = new SearchRequest().source(searchSourceBuilder).indices(MESSAGE_INDEX);

            CountDownLatch latch = new CountDownLatch(1);
            LatchedActionListener latchedActionListener = new LatchedActionListener<SearchResponse>(ActionListener.wrap(r -> {
                log.info("Completed Get History Request");
                List<GetSessionHistoryResponse.Element> steps = new ArrayList<>();
                SearchHit[] hits = r.getHits().getHits();
                if (hits != null && hits.length > 0) {
                    for (int i = 0; i < hits.length; i++) {
                        SearchHit hit = hits[i];
                        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                        String question = (String) sourceAsMap.get(QUESTION_FIELD);
                        String answer = (String) sourceAsMap.get(ANSWER_FIELD);
                        Instant createdTime = Instant.ofEpochMilli((Long) sourceAsMap.get(CREATED_TIME_FIELD));
                        steps.add(
                            GetSessionHistoryResponse.Element.builder().question(question).answer(answer).createdTime(createdTime).build()
                        );
                    }
                    listener.onResponse(GetSessionHistoryResponse.builder().sessionId(sessionId).steps(steps).build());
                } else {
                    listener.onFailure(new RuntimeException("No hits returned from session history index."));
                }
            }, e -> {
                log.error("Failed to search history index", e);
            }), latch);
            client.search(searchRequest, latchedActionListener);

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
