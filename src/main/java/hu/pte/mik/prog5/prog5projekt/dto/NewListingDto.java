package hu.pte.mik.prog5.prog5projekt.dto;

import jakarta.validation.constraints.*;

public record NewListingDto(
        @NotNull Long ownerId,
        @NotNull Long bookId,
        @NotBlank String condition,  // NEW/LIKE_NEW/GOOD/USED
        @NotBlank String type,       // EXCHANGE/GIVEAWAY/SELL
        @PositiveOrZero Integer priceHuf,
        String note
) {}
