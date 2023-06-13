package org.opensearch.conversation.rest;

import org.opensearch.client.node.NodeClient;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.RestRequest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.opensearch.conversation.plugin.ConversationPlugin.CONVERSATION_BASE_URI;

public class RestCreateSessionAction extends BaseRestHandler {
    private static final String CREATE_SESSION_ACTION = "create_session_action";

    @Override
    public String getName() {
        return CREATE_SESSION_ACTION;
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        return null;
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(RestRequest.Method.POST, String.format(Locale.ROOT, "%s/_create", CONVERSATION_BASE_URI)));
    }
}
