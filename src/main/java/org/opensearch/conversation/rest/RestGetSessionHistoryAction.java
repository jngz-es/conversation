/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.rest;

import static org.opensearch.conversation.plugin.ConversationPlugin.CONVERSATION_BASE_URI;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.opensearch.client.node.NodeClient;
import org.opensearch.common.Strings;
import org.opensearch.conversation.request.GetSessionHistoryRequest;
import org.opensearch.conversation.transport.GetSessionListAction;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.action.RestToXContentListener;

public class RestGetSessionHistoryAction extends BaseRestHandler {
    private static final String GET_SESSION_HISTORY_ACTION = "get_session_history_action";

    @Override
    public String getName() {
        return GET_SESSION_HISTORY_ACTION;
    }

    private GetSessionHistoryRequest getRequest(RestRequest request) throws IOException {
        String sessionId = request.param("sessionId");
        if (Strings.isNullOrEmpty(sessionId)) {
            throw new IllegalArgumentException("The sessionId is required in history request.");
        }
        int pageSize = request.paramAsInt("pageSize", 10);
        int currentPage = request.paramAsInt("currentPage", 1);
        int from = (currentPage - 1) * pageSize + 1;
        return new GetSessionHistoryRequest(sessionId, from, pageSize);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        GetSessionHistoryRequest getSessionHistoryRequest = getRequest(request);
        return channel -> client.execute(GetSessionListAction.INSTANCE, getSessionHistoryRequest, new RestToXContentListener<>(channel));
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(RestRequest.Method.POST, String.format(Locale.ROOT, "%s/history", CONVERSATION_BASE_URI)));
    }
}
