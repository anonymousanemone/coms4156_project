package com.cointrail.model;

/**
 * Represents an acquisition (a "lot"): every object that came to the same
 * museum from the same donor in the same year by the same type.
 */
public final class Acquisition {

    /** The acquisition ID. */
    private String id;

    /** The museum name. */
    private String museum;

    /** The donor ID, or null if the donor is unknown. */
    private String donorId;

    /** The acquisition year, or null if undated. */
    private Integer year;

    /** The acquisition type. */
    private AcquisitionType type;

    /** Default constructor. */
    public Acquisition() {
    }

    /**
     * Constructs an acquisition.
     *
     * @param id the acquisition ID
     * @param museum the museum name
     * @param donorId the donor ID, or null
     * @param year the acquisition year, or null
     * @param type the acquisition type
     */
    public Acquisition(
        final String id,
        final String museum,
        final String donorId,
        final Integer year,
        final AcquisitionType type
    ) {
        this.id = id;
        this.museum = museum;
        this.donorId = donorId;
        this.year = year;
        this.type = type;
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
     * Gets the museum name.
     *
     * @return the museum name
     */
    public String getMuseum() {
        return museum;
    }

    /**
     * Sets the museum name.
     *
     * @param museum the museum name
     */
    public void setMuseum(final String museum) {
        this.museum = museum;
    }

    /**
     * Gets the donor ID.
     *
     * @return the donor ID, or null
     */
    public String getDonorId() {
        return donorId;
    }

    /**
     * Sets the donor ID.
     *
     * @param donorId the donor ID
     */
    public void setDonorId(final String donorId) {
        this.donorId = donorId;
    }

    /**
     * Gets the year.
     *
     * @return the year, or null
     */
    public Integer getYear() {
        return year;
    }

    /**
     * Sets the year.
     *
     * @param year the year
     */
    public void setYear(final Integer year) {
        this.year = year;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public AcquisitionType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(final AcquisitionType type) {
        this.type = type;
    }

}
