package org.opensearch.conversation.transport;

import org.opensearch.action.ActionType;
import org.opensearch.conversation.response.CreateConversationResponse;

public class CreateConversationAction extends ActionType<CreateConversationResponse> {
    public static CreateConversationAction INSTANCE = new CreateConversationAction();
    public static final String NAME = "cluster:admin/opensearch/conversation/create";

    private CreateConversationAction() {
        super(NAME, CreateConversationResponse::new);
    }
}
