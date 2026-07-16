package com.hospital.dto;

import java.time.Instant;
import java.util.List;

/**
 * The complete, self-contained export/import contract for one discovery
 * survey — "clean and extensible" per the spec, since it is designed to
 * also be consumed by future AI modules. Used both as the export response
 * body and the import request body, so the two are always structurally
 * identical.
 */
public record DiscoverySurveyExport(
        int exportVersion,
        Instant exportedAt,
        DiscoveryProjectExport project,
        DiscoveryProgressResponse progress,
        List<DiscoverySectionExport> sections) {

    public static final int CURRENT_VERSION = 1;
}
