package com.hospital.service;

import java.util.Set;

import io.quarkus.panache.common.Sort;

/**
 * Builds a safe {@link Sort} from user-supplied field/direction strings,
 * restricted to a per-entity allow-list so arbitrary field names can never
 * reach the generated JPQL order-by clause.
 */
final class SortSupport {

    private SortSupport() {
    }

    static Sort build(String sortBy, String sortDir, Set<String> allowedFields, String defaultField) {
        String field = (sortBy != null && allowedFields.contains(sortBy)) ? sortBy : defaultField;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.Descending
                : Sort.Direction.Ascending;
        return Sort.by(field, direction);
    }
}
