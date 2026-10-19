package com.cointrail.model;

/**
 * How a museum acquired an object.
 */
public enum AcquisitionType {

    /** Donated to the museum. */
    GIFT,

    /** Bequeathed to the museum. (Given by a deceased person) */
    BEQUEST,

    /** Bought by the museum. */
    PURCHASE,

    /** Swapped for other objects. */
    EXCHANGE,

    /** Moved from another institution or collection. */
    TRANSFER,

    /** Found in a museum-run excavation. */
    EXCAVATION,

    /** Lent to the museum. */
    LOAN,

    /** Not one of the other enums. */
    OTHER,
    
    /** The museum does not say how it was acquired. */
    UNKNOWN
}
