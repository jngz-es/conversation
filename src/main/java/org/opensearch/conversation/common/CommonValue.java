package org.opensearch.conversation.common;

public class CommonValue {

    public static final String SESSION_METADATA_INDEX = ".plugins-conversation-session-metadata";
    public static final String MESSAGE_INDEX = ".plugins-conversation-message";

    public static final String META = "_meta";
    public static final String SCHEMA_VERSION_FIELD = "schema_version";
    public static final Integer NO_SCHEMA_VERSION = 0;
    public static final Integer SESSION_METADATA_INDEX_SCHEMA_VERSION = 1;
    public static final Integer MESSAGE_INDEX_SCHEMA_VERSION = 1;
    public static final String CREATED_TIME_FIELD = "created_time";
    public static final String LAST_UPDATED_TIME_FIELD = "last_updated_time";
    public static final String SESSION_TITLE_FIELD = "title";
    public static final String QUESTION_FIELD = "question";
    public static final String SESSION_ID_FIELD = "session_id";
    public static final String ANSWER_FIELD = "answer";
    public static final String MODEL_ID_FIELD = "model_id";
    public static final String ML_PARAMETERS_FIELD = "parameters";
    public static final String SESSIONS_FIELD = "sessions";

    public static final String USER_FIELD_MAPPING = "      \""
            + "user"
            + "\": {\n"
            + "        \"type\": \"nested\",\n"
            + "        \"properties\": {\n"
            + "          \"name\": {\"type\":\"text\", \"fields\":{\"keyword\":{\"type\":\"keyword\", \"ignore_above\":256}}},\n"
            + "          \"backend_roles\": {\"type\":\"text\", \"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\n"
            + "          \"roles\": {\"type\":\"text\", \"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\n"
            + "          \"custom_attribute_names\": {\"type\":\"text\", \"fields\":{\"keyword\":{\"type\":\"keyword\"}}}\n"
            + "        }\n"
            + "      }\n";
    public static final String SESSION_METADATA_INDEX_MAPPING = "{\n"
            + "    \"_meta\": {\"schema_version\": "
            + SESSION_METADATA_INDEX_SCHEMA_VERSION
            + "},\n"
            + "    \"properties\": {\n"
            + "      \""
            + MODEL_ID_FIELD
            + "\" : {\"type\": \"keyword\"},\n"
            + "      \""
            + SESSION_TITLE_FIELD
            + "\" : {\"type\": \"text\"},\n"
            + "      \""
            + CREATED_TIME_FIELD
            + "\": {\"type\": \"date\", \"format\": \"strict_date_time||epoch_millis\"},\n"
            + "      \""
            + LAST_UPDATED_TIME_FIELD
            + "\": {\"type\": \"date\", \"format\": \"strict_date_time||epoch_millis\"},\n"
            + USER_FIELD_MAPPING
            + "    }\n"
            + "}";

    public static final String MESSAGE_INDEX_MAPPING = "{\n"
            + "    \"_meta\": {\"schema_version\": "
            + MESSAGE_INDEX_SCHEMA_VERSION
            + "},\n"
            + "    \"properties\": {\n"
            + "      \""
            + SESSION_ID_FIELD
            + "\": {\"type\": \"keyword\"},\n"
            + "      \""
            + QUESTION_FIELD
            + "\" : {\"type\": \"text\"},\n"
            + "      \""
            + ANSWER_FIELD
            + "\" : {\"type\": \"text\"},\n"
            + "      \""
            + CREATED_TIME_FIELD
            + "\": {\"type\": \"date\", \"format\": \"strict_date_time||epoch_millis\"},\n"
            + "      \""
            + LAST_UPDATED_TIME_FIELD
            + "\": {\"type\": \"date\", \"format\": \"strict_date_time||epoch_millis\"},\n"
            + USER_FIELD_MAPPING
            + "    }\n"
            + "}";

    public static final String MATCH_ALL_QUERY = "{\n" +
            "  \"query\": {\n" +
            "    \"match_all\": {}\n" +
            "  }\n" +
            "}";
}
