package org.opensearch.conversation.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.opensearch.action.ActionResponse;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;

import java.io.IOException;

import static org.opensearch.conversation.common.CommonValue.ANSWER_FIELD;
import static org.opensearch.conversation.common.CommonValue.SESSION_ID_FIELD;

@Getter
@ToString
public class ChatResponse extends ActionResponse implements ToXContentObject {
    private String sessionId;
    private String answer;

    @Builder
    public ChatResponse(String sessionId, String answer) {
        this.sessionId = sessionId;
        this.answer = answer;
    }

    public ChatResponse(StreamInput in) throws IOException {
        this.sessionId = in.readString();
        this.answer = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(sessionId);
        out.writeString(answer);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(SESSION_ID_FIELD, sessionId);
        builder.field(ANSWER_FIELD, answer);
        builder.endObject();
        return builder;
    }
}
