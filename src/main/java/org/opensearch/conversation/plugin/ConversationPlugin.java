/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.conversation.plugin;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionResponse;
import org.opensearch.client.Client;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.io.stream.NamedWriteableRegistry;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.IndexScopedSettings;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.SettingsFilter;
import org.opensearch.conversation.action.TransportChatAction;
import org.opensearch.conversation.action.TransportGetSessionHistoryAction;
import org.opensearch.conversation.action.TransportGetSessionListAction;
import org.opensearch.conversation.rest.RestChatAction;
import org.opensearch.conversation.rest.RestGetSessionHistoryAction;
import org.opensearch.conversation.rest.RestGetSessionListAction;
import org.opensearch.conversation.transport.ChatAction;
import org.opensearch.conversation.transport.GetSessionHistoryAction;
import org.opensearch.conversation.transport.GetSessionListAction;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.env.Environment;
import org.opensearch.env.NodeEnvironment;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.repositories.RepositoriesService;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;
import org.opensearch.script.ScriptService;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.watcher.ResourceWatcherService;

/**
 * Conversation plugin class
 */
public class ConversationPlugin extends Plugin implements ActionPlugin {
    public static final String CONVERSATION_BASE_URI = "/_plugins/_conversation";

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return List.of(
            new ActionHandler<>(ChatAction.INSTANCE, TransportChatAction.class),
            new ActionHandler<>(GetSessionListAction.INSTANCE, TransportGetSessionListAction.class),
            new ActionHandler<>(GetSessionHistoryAction.INSTANCE, TransportGetSessionHistoryAction.class)
        );
    }

    @Override
    public List<RestHandler> getRestHandlers(
        Settings settings,
        RestController restController,
        ClusterSettings clusterSettings,
        IndexScopedSettings indexScopedSettings,
        SettingsFilter settingsFilter,
        IndexNameExpressionResolver indexNameExpressionResolver,
        Supplier<DiscoveryNodes> nodesInCluster
    ) {
        return List.of(new RestChatAction(), new RestGetSessionListAction(), new RestGetSessionHistoryAction());
    }

    @Override
    public Collection<Object> createComponents(
        final Client client,
        final ClusterService clusterService,
        final ThreadPool threadPool,
        final ResourceWatcherService resourceWatcherService,
        final ScriptService scriptService,
        final NamedXContentRegistry xContentRegistry,
        final Environment environment,
        final NodeEnvironment nodeEnvironment,
        final NamedWriteableRegistry namedWriteableRegistry,
        final IndexNameExpressionResolver indexNameExpressionResolver,
        final Supplier<RepositoriesService> repositoriesServiceSupplier
    ) {
        return List.of();
    }
}
