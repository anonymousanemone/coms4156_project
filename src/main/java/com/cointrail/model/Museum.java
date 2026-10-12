package com.cointrail.model;

/**
 * Represents a museum, identified by its unique name.
 */
public final class Museum {

    /** The name of the museum. **/
    private String name;

    /** The unique identifier of the museum, usually an acronym. **/
    private String id;

    /** Default constructor. */
    public Museum() {
    }

    /**
     * Constructs a museum.
     *
     * @param name the museum name
     * @param id the museum ID
     */
    public Museum(final String name, final String id) {
        this.name = name;
        this.id = id;
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
}
