package org.mounanga.accountservice.common.enums;

/**
 * @deprecated Use {@link org.mounanga.sharedevents.enums.AccountStatus} from shared-events module instead.
 */
@Deprecated
public enum AccountStatus {
    CREATED, ACTIVATED, SUSPENDED, DELETED;

    public org.mounanga.sharedevents.enums.AccountStatus toShared() {
        return org.mounanga.sharedevents.enums.AccountStatus.valueOf(this.name());
    }

    public static AccountStatus fromShared(org.mounanga.sharedevents.enums.AccountStatus shared) {
        return AccountStatus.valueOf(shared.name());
    }
}
