package org.mounanga.accountservice.common.enums;

/**
 * @deprecated Use {@link org.mounanga.sharedevents.enums.OperationType} from shared-events module instead.
 */
@Deprecated
public enum OperationType {
    CREDIT, DEBIT;

    public org.mounanga.sharedevents.enums.OperationType toShared() {
        return org.mounanga.sharedevents.enums.OperationType.valueOf(this.name());
    }

    public static OperationType fromShared(org.mounanga.sharedevents.enums.OperationType shared) {
        return OperationType.valueOf(shared.name());
    }
}
