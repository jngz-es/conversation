package org.opensearch.conversation.request;

import lombok.Builder;
import lombok.Getter;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.common.io.stream.InputStreamStreamInput;
import org.opensearch.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.conversation.input.CreateConversationInput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.opensearch.action.ValidateActions.addValidationError;

@Getter
public class CreateConversationRequest extends ActionRequest {
    private CreateConversationInput createConversationInput;

    @Builder
    public CreateConversationRequest(CreateConversationInput createConversationInput) {
        this.createConversationInput = createConversationInput;
    }

    public CreateConversationRequest(StreamInput in) throws IOException {
        super(in);
        this.createConversationInput = new CreateConversationInput(in);
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException exception = new ActionRequestValidationException();
        if (createConversationInput == null) {
            exception = addValidationError("Create session input can't be null", exception);
        }

        return exception;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        this.createConversationInput.writeTo(out);
    }

    public static CreateConversationRequest fromActionRequest(ActionRequest actionRequest) {
        if (actionRequest instanceof CreateConversationRequest) {
            return (CreateConversationRequest) actionRequest;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamStreamOutput osso = new OutputStreamStreamOutput(baos)) {
            actionRequest.writeTo(osso);
            try (StreamInput input = new InputStreamStreamInput(new ByteArrayInputStream(baos.toByteArray()))) {
                return new CreateConversationRequest(input);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse ActionRequest into CreateConversationRequest", e);
        }
    }
}
