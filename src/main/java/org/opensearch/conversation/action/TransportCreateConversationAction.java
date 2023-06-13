package org.opensearch.conversation.action;

import lombok.extern.log4j.Log4j2;
import org.opensearch.action.ActionListener;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.HandledTransportAction;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.inject.Inject;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.conversation.input.CreateConversationInput;
import org.opensearch.conversation.memory.opensearch.OpensearchIndicesHandler;
import org.opensearch.conversation.request.CreateConversationRequest;
import org.opensearch.conversation.response.CreateConversationResponse;
import org.opensearch.conversation.transport.CreateConversationAction;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportService;

import java.time.Instant;
import java.util.Map;

import static org.opensearch.conversation.common.CommonValue.SESSION_METADATA_INDEX;
import static org.opensearch.conversation.input.CreateConversationInput.MODEL_ID_FIELD;
import static org.opensearch.conversation.input.CreateConversationInput.USER_ID_FIELD;

@Log4j2
public class TransportCreateConversationAction extends HandledTransportAction<ActionRequest, CreateConversationResponse> {
    private final TransportService transportService;
    private final ClusterService clusterService;
    private final OpensearchIndicesHandler indicesHandler;
    private final Client client;

    @Inject
    public TransportCreateConversationAction(
            TransportService transportService,
            ActionFilters actionFilters,
            ClusterService clusterService,
            OpensearchIndicesHandler indicesHandler,
            Client client
    ) {
        super(CreateConversationAction.NAME, transportService, actionFilters, CreateConversationRequest::new);
        this.transportService = transportService;
        this.clusterService = clusterService;
        this.indicesHandler = indicesHandler;
        this.client = client;
    }

    @Override
    protected void doExecute(Task task, ActionRequest request, ActionListener<CreateConversationResponse> listener) {
        CreateConversationRequest createConversationRequest = CreateConversationRequest.fromActionRequest(request);
        CreateConversationInput createConversationInput = createConversationRequest.getCreateConversationInput();
        if (createConversationInput.getUserId() == null) {
            throw new IllegalArgumentException("The user id is required.");
        }
        if (createConversationInput.getModelId() == null) {
            throw new IllegalArgumentException("The model id is required.");
        }

        try {
            Instant now = Instant.now();
            log.info("connector created, indexing into the connector system index");
            indicesHandler.initSessionMetaIndex(ActionListener.wrap(indexCreated -> {
                if (!indexCreated) {
                    listener.onFailure(new RuntimeException("No response to create session metadata index"));
                    return;
                }

                try (ThreadContext.StoredContext context = client.threadPool().getThreadContext().stashContext()) {
                    ActionListener<IndexResponse> indexResponseListener = ActionListener.wrap(r -> {
                        log.info("Session metadata saved into index, result:{}, session id: {}", r.getResult(), r.getId());
                        CreateConversationResponse response = new CreateConversationResponse(r.getId());
                        listener.onResponse(response);
                    }, e -> { listener.onFailure(e); });

                    IndexRequest indexRequest = new IndexRequest(SESSION_METADATA_INDEX);
                    indexRequest.source(Map.of(USER_ID_FIELD, createConversationInput.getUserId(),
                            MODEL_ID_FIELD, createConversationInput.getModelId(),
                            "created_time", Instant.now().toEpochMilli()));
                    indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                    client.index(indexRequest, ActionListener.runBefore(indexResponseListener, () -> context.restore()));
                } catch (Exception e) {
                    log.error("Failed to save session metadata", e);
                    listener.onFailure(e);
                }
            }, e -> {
                log.error("Failed to init session metadata index", e);
                listener.onFailure(e);
            }));

        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("Failed to create session ", illegalArgumentException);
            listener.onFailure(illegalArgumentException);
        } catch (Exception e) {
            // todo need to specify what exception
            log.error("Failed to create session ", e);
            listener.onFailure(e);
        }
    }
}
