package com.hospital.dto;

import java.util.List;

public record CommandCenterStatusResponse(AlertSeverity overallStatus, List<AlertDto> alerts) {
}
