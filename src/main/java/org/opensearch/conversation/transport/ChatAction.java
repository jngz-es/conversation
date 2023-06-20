package org.opensearch.conversation.transport;

import org.opensearch.action.ActionType;
import org.opensearch.conversation.response.ChatResponse;

public class ChatAction extends ActionType<ChatResponse> {
    public static ChatAction INSTANCE = new ChatAction();
    public static final String NAME = "cluster:admin/opensearch/conversation/chat";

    private ChatAction() {
        super(NAME, ChatResponse::new);
    }
}
