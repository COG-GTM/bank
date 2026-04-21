package org.mounanga.collectionsservice.dto;

import lombok.*;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DelinquencySummaryDTO {
    private DelinquencyBucket bucket;
    private long accountCount;
    private BigDecimal totalOutstandingBalance;
}
