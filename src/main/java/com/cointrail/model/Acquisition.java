package com.cointrail.model;

/**
 * Represents an acquisition (a "lot"): every object that came to the same
 * museum from the same donor in the same year by the same type.
 */
public final class Acquisition {

    /** The acquisition ID. */
    private String id;

    /** The ID of the museum. */
    private String museumId;

    /** The donor ID, or null if the donor is unknown. */
    private String donorId;

    /** The acquisition year, or null if undated. */
    private Integer year;

    /** The acquisition type. */
    private AcquisitionType type;

    /** The museum's lot identifier, or null if it has none. */
    private String lotIdentifier;

    /** The acquisition date as the museum wrote it, or null. */
    private String acquiredOnText;

    /** Other acquisition information, such as the lot link, or null. */
    private String additionalInfo;

    /** Default constructor. */
    public Acquisition() {
    }

    /**
     * Constructs an acquisition. The optional fields (lot identifier,
     * acquired-on text, and additional info) are set afterwards.
     *
     * @param id the acquisition ID
     * @param museumId the museum ID
     * @param donorId the donor ID, or null
     * @param year the acquisition year, or null
     * @param type the acquisition type
     */
    public Acquisition(
        final String id,
        final String museumId,
        final String donorId,
        final Integer year,
        final AcquisitionType type
    ) {
        this.id = id;
        this.museumId = museumId;
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
     * Gets the museum ID.
     *
     * @return the museum ID
     */
    public String getMuseumId() {
        return museumId;
    }

    /**
     * Sets the museum ID.
     *
     * @param museumId the museum ID
     */
    public void setMuseumId(final String museumId) {
        this.museumId = museumId;
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

    /**
     * Gets the lot identifier.
     *
     * @return the lot identifier, or null
     */
    public String getLotIdentifier() {
        return lotIdentifier;
    }

    /**
     * Sets the lot identifier.
     *
     * @param lotIdentifier the lot identifier
     */
    public void setLotIdentifier(final String lotIdentifier) {
        this.lotIdentifier = lotIdentifier;
    }

    /**
     * Gets the acquisition date text.
     *
     * @return the date as the museum wrote it, or null
     */
    public String getAcquiredOnText() {
        return acquiredOnText;
    }

    /**
     * Sets the acquisition date text.
     *
     * @param acquiredOnText the date as the museum wrote it
     */
    public void setAcquiredOnText(final String acquiredOnText) {
        this.acquiredOnText = acquiredOnText;
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

}
