package org.tron.core.config.args;

import com.typesafe.config.Config;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class StreamingConfig {

    /**
     * Keys (names) of config
     */
    private static final String ENABLE_KEY = "enable";
    private static final String FILE_STORAGE_ROOT_KEY = "file_storage.root";

    /**
     * Default values
     */
    private static final boolean DEFAULT_ENABLE = false;
    private static final String DEFAULT_FILE_STORAGE_ROOT = "streaming-directory";

    /**
     * General information
     */
    @Getter
    @Setter
    private boolean enable;

    @Getter
    @Setter
    private String chainId;

    /**
     * File Storage config
     */
    @Getter
    @Setter
    private String fileStorageRoot;

    @Getter
    @Setter
    private List<String> fileStorageUrls;

    /**
     * Elliptic Signer config
     */
    @Getter
    @Setter
    private String ellipticSignerPrivateKeyHex;

    /**
     * Kafka Broker config
     */
    @Getter
    @Setter
    private String kafkaBrokerBootstrapServers;

    @Getter
    @Setter
    private String kafkaBrokerSecurityProtocol;

    @Getter
    @Setter
    private String kafkaBrokerSslTruststoreType;

    @Getter
    @Setter
    private String kafkaBrokerSslTruststoreLocation;

    @Getter
    @Setter
    private String kafkaBrokerSslKeystoreType;

    @Getter
    @Setter
    private String kafkaBrokerSslKeystoreLocation;

    @Getter
    @Setter
    private String kafkaBrokerSslKeyPassword;

    @Getter
    @Setter
    private String kafkaBrokerSslEndpointIdentificationAlgorithm;

    @Getter
    @Setter
    private boolean kafkaBrokerAllowAutoCreateTopics;

    /**
     * Path Generator config
     */
    @Getter
    @Setter
    private int pathGeneratorBucketSize;

    @Getter
    @Setter
    private int pathGeneratorBlockNumberPadding;

    @Getter
    @Setter
    private String pathGeneratorSpacer;

    @Getter
    @Setter
    private String pathGeneratorSuffix;

    public static boolean getEnableFromConfig(final Config config) {
        return config.hasPath(ENABLE_KEY)
                ? config.getBoolean(ENABLE_KEY)
                : DEFAULT_ENABLE;
    }

    public static String getFileStorageFromConfig(final Config config) {
        return config.hasPath(FILE_STORAGE_ROOT_KEY)
                ? config.getString(FILE_STORAGE_ROOT_KEY)
                : DEFAULT_FILE_STORAGE_ROOT;
    }
}
