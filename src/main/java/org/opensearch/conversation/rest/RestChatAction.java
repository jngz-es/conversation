/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.rest;

import org.opensearch.client.node.NodeClient;
import org.opensearch.conversation.input.ChatInput;
import org.opensearch.conversation.request.ChatRequest;
import org.opensearch.conversation.transport.ChatAction;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.action.RestToXContentListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken;
import static org.opensearch.conversation.plugin.ConversationPlugin.CONVERSATION_BASE_URI;

public class RestChatAction extends BaseRestHandler {
    private static final String CREATE_SESSION_ACTION = "chat_action";

    @Override
    public String getName() {
        return CREATE_SESSION_ACTION;
    }

    private ChatRequest getRequest(RestRequest request) throws IOException {
        if (!request.hasContent()) {
            throw new IOException("Chat request has empty body");
        }
        XContentParser parser = request.contentParser();
        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser);
        ChatInput chatInput = ChatInput.parse(parser);
        return new ChatRequest(chatInput);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        ChatRequest chatRequest = getRequest(request);
        return channel -> client.execute(ChatAction.INSTANCE, chatRequest, new RestToXContentListener<>(channel));
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(RestRequest.Method.POST, String.format(Locale.ROOT, "%s/_chat", CONVERSATION_BASE_URI)));
    }
}
