package org.mounanga.collectionsservice.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Component
public class CollectionsProperties {

    @Value("${collections.overdraft-threshold}")
    private BigDecimal overdraftThreshold;
}
