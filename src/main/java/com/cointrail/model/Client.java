package com.cointrail.model;

/**
 * Represents a client in the tax API.
 */
public final class Client {

    /** The client ID. */
    private String id;

    /** The client name. */
    private String name;

    /** The client API key. */
    private String apiKey;

    /** Default constructor. */
    public Client() {
    }

    /**
     * Constructs a client.
     *
     * @param id the client ID
     * @param name the client name
     * @param apiKey the client API key
     */
    public Client(
        final String id,
        final String name,
        final String apiKey
    ) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
    }

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the API key.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key.
     *
     * @param apiKey the API key
     */
    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }
}
