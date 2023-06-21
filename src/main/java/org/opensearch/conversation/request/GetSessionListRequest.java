package org.opensearch.conversation.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.common.io.stream.InputStreamStreamInput;
import org.opensearch.common.io.stream.OutputStreamStreamOutput;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.opensearch.action.ValidateActions.addValidationError;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class GetSessionListRequest extends ActionRequest {
    private int from;
    private int size;

    @Builder
    public GetSessionListRequest(int from, int size) {
        this.from = from;
        this.size = size;
    }

    public GetSessionListRequest(StreamInput in) throws IOException {
        super(in);
        this.from = in.readInt();
        this.size = in.readInt();
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException exception = new ActionRequestValidationException();
        if (from <= 0 || size <= 0) {
            exception = addValidationError("from and size can not be less than or equal to 0", exception);
        }

        return exception;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeInt(from);
        out.writeInt(size);
    }

    public static GetSessionListRequest fromActionRequest(ActionRequest actionRequest) {
        if (actionRequest instanceof GetSessionListRequest) {
            return (GetSessionListRequest) actionRequest;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamStreamOutput osso = new OutputStreamStreamOutput(baos)) {
            actionRequest.writeTo(osso);
            try (StreamInput input = new InputStreamStreamInput(new ByteArrayInputStream(baos.toByteArray()))) {
                return new GetSessionListRequest(input);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse ActionRequest into GetSessionListRequest", e);
        }
    }
}
