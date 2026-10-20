package com.cointrail.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a donor, with every spelling of their name seen on import.
 */
public final class Donor {

    /** The donor ID. */
    private String id;

    /** The normalized donor name. */
    private String name;

    /** The donor names exactly as they appeared in imports. */
    private Set<String> nameVariants = new LinkedHashSet<>();

    /** Other information about the donor, such as authority IDs, or null. */
    private String additionalInfo;

    /** Default constructor. */
    public Donor() {
    }

    /**
     * Constructs a donor.
     *
     * @param id the donor ID
     * @param name the normalized donor name
     * @param nameVariants the name variants seen
     */
    public Donor(
        final String id,
        final String name,
        final Set<String> nameVariants
    ) {
        this.id = id;
        this.name = name;
        this.nameVariants = new LinkedHashSet<>(nameVariants);
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
     * Gets the normalized name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the normalized name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the name variants.
     *
     * @return the name variants
     */
    public Set<String> getNameVariants() {
        return nameVariants;
    }

    /**
     * Sets the name variants.
     *
     * @param nameVariants the name variants
     */
    public void setNameVariants(final Set<String> nameVariants) {
        this.nameVariants = new LinkedHashSet<>(nameVariants);
    }

    /**
     * Gets the additional info.
     *
     * @return the additional info, or null
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Sets the additional info.
     *
     * @param additionalInfo the additional info
     */
    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /** Adds a name variant.
     * 
     * @param nameVariant the name variant to add
     */
    public void addNameVariant(final String nameVariant) {
        this.nameVariants.add(nameVariant);
    }

    /** Removes a name variant. 
     * 
     * @param nameVariant the name variant to remove
     */
    public void removeNameVariant(final String nameVariant) {
        this.nameVariants.remove(nameVariant); 
    }
}
