package org.opensearch.conversation.response;

import org.opensearch.action.ActionResponse;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;

import java.io.IOException;

public class CreateConversationResponse extends ActionResponse implements ToXContentObject {
    public static final String SESSION_ID_FIELD = "session_id";

    private String sessionId;

    public CreateConversationResponse(String sessionId) {
        this.sessionId = sessionId;
    }

    public CreateConversationResponse(StreamInput in) throws IOException {
        this.sessionId = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(sessionId);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(SESSION_ID_FIELD, sessionId);
        builder.endObject();
        return builder;
    }
}
