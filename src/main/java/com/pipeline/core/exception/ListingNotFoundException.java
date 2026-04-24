package com.pipeline.core.exception;

import java.util.UUID;
import com.pipeline.modules.listing.domain.Listing;

public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(UUID listingId) {
        super("Listing not found: " + listingId);
    }
}
