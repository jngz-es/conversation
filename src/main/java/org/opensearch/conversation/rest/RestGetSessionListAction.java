package org.opensearch.conversation.rest;

import org.opensearch.client.node.NodeClient;
import org.opensearch.conversation.request.GetSessionListRequest;
import org.opensearch.conversation.transport.GetSessionListAction;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.action.RestToXContentListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.opensearch.conversation.plugin.ConversationPlugin.CONVERSATION_BASE_URI;

public class RestGetSessionListAction extends BaseRestHandler {
    private static final String GET_SESSION_LIST_ACTION = "get_session_list_action";

    @Override
    public String getName() {
        return GET_SESSION_LIST_ACTION;
    }

    private GetSessionListRequest getRequest(RestRequest request) throws IOException {
        int pageSize = request.paramAsInt("pageSize", 10);
        int currentPage = request.paramAsInt("currentPage", 1);
        int from = (currentPage - 1) * pageSize + 1;
        return new GetSessionListRequest(from, pageSize);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        GetSessionListRequest getSessionListRequest = getRequest(request);
        return channel -> client.execute(GetSessionListAction.INSTANCE, getSessionListRequest, new RestToXContentListener<>(channel));
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(RestRequest.Method.POST, String.format(Locale.ROOT, "%s/sessions", CONVERSATION_BASE_URI)));
    }
}
