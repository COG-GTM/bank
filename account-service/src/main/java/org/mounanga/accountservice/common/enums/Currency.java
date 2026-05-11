package org.mounanga.accountservice.common.enums;

/**
 * @deprecated Use {@link org.mounanga.sharedevents.enums.Currency} from shared-events module instead.
 */
@Deprecated
public enum Currency {
    EUR, USD, GBP, CAD, CHF, CNY, RUB, NGN, XAF, XOF, ZAR, TND, MAD;

    public org.mounanga.sharedevents.enums.Currency toShared() {
        return org.mounanga.sharedevents.enums.Currency.valueOf(this.name());
    }

    public static Currency fromShared(org.mounanga.sharedevents.enums.Currency shared) {
        return Currency.valueOf(shared.name());
    }
}
