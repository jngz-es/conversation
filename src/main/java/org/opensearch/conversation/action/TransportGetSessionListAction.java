/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.action;

import static org.opensearch.conversation.common.CommonValue.ANSWER_FIELD;
import static org.opensearch.conversation.common.CommonValue.CREATED_TIME_FIELD;
import static org.opensearch.conversation.common.CommonValue.MATCH_ALL_QUERY;
import static org.opensearch.conversation.common.CommonValue.QUESTION_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_METADATA_INDEX;
import static org.opensearch.conversation.common.CommonValue.SESSION_TITLE_FIELD;

import java.io.IOException;
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
import org.opensearch.common.xcontent.LoggingDeprecationHandler;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.conversation.memory.opensearch.OpensearchIndicesHandler;
import org.opensearch.conversation.request.GetSessionListRequest;
import org.opensearch.conversation.response.GetSessionHistoryResponse;
import org.opensearch.conversation.response.GetSessionListResponse;
import org.opensearch.conversation.transport.GetSessionListAction;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.fetch.subphase.FetchSourceContext;
import org.opensearch.search.sort.SortOrder;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportService;

@Log4j2
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TransportGetSessionListAction extends HandledTransportAction<ActionRequest, GetSessionListResponse> {
    private final TransportService transportService;
    private final OpensearchIndicesHandler indicesHandler;
    private final Client client;
    private final NamedXContentRegistry xContentRegistry;

    @Inject
    public TransportGetSessionListAction(
        TransportService transportService,
        ActionFilters actionFilters,
        OpensearchIndicesHandler indicesHandler,
        Client client,
        NamedXContentRegistry xContentRegistry
    ) {
        super(GetSessionListAction.NAME, transportService, actionFilters, GetSessionListRequest::new);
        this.transportService = transportService;
        this.indicesHandler = indicesHandler;
        this.client = client;
        this.xContentRegistry = xContentRegistry;
    }

    @Override
    protected void doExecute(Task task, ActionRequest request, ActionListener<GetSessionListResponse> listener) {
        GetSessionListRequest getSessionListRequest = GetSessionListRequest.fromActionRequest(request);
        int from = getSessionListRequest.getFrom();
        int size = getSessionListRequest.getSize();

        try (ThreadContext.StoredContext context = client.threadPool().getThreadContext().stashContext()) {
            // TODO: scroll search here
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.fetchSource(new FetchSourceContext(true, Strings.EMPTY_ARRAY, Strings.EMPTY_ARRAY));
            searchSourceBuilder.from(from);
            searchSourceBuilder.size(size);
            XContentParser queryParser = XContentType.JSON.xContent()
                .createParser(xContentRegistry, LoggingDeprecationHandler.INSTANCE, MATCH_ALL_QUERY);
            searchSourceBuilder.parseXContent(queryParser);
            searchSourceBuilder.sort(CREATED_TIME_FIELD, SortOrder.ASC);
            SearchRequest searchRequest = new SearchRequest().source(searchSourceBuilder).indices(SESSION_METADATA_INDEX);

            CountDownLatch latch = new CountDownLatch(1);
            LatchedActionListener latchedActionListener = new LatchedActionListener<SearchResponse>(ActionListener.wrap(r -> {
                log.info("Completed Get Sessions Request");
                List<GetSessionListResponse.Element> sessions = new ArrayList<>();
                SearchHit[] hits = r.getHits().getHits();
                if (hits != null && hits.length > 0) {
                    for (int i = 0; i < hits.length; i++) {
                        SearchHit hit = hits[i];
                        String sessionId = String.valueOf(hit.getId());
                        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                        String title = (String) sourceAsMap.get(SESSION_TITLE_FIELD);
                        Instant createdTime = Instant.ofEpochMilli((Long) sourceAsMap.get(CREATED_TIME_FIELD));
                        sessions.add(
                                GetSessionListResponse.Element.builder().sessionId(sessionId).title(title).createdTime(createdTime).build()
                        );
                    }
                    listener.onResponse(GetSessionListResponse.builder().sessions(sessions).build());
                } else {
                    listener.onFailure(new RuntimeException("No hits returned from session meta index."));
                }
            }, e -> {
                log.error("Failed to search session meta index", e);
            }), latch);
            client.search(searchRequest, latchedActionListener);

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        } catch (IOException e) {
            log.error("Failed to create parser for sessions match all query. " + e);
            listener.onFailure(e);
        }
    }
}
