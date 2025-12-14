package hu.pte.mik.prog5.prog5projekt.dto;

import jakarta.validation.constraints.*;

public record NewListingDto(
         Long ownerId,
        @NotNull Long bookId,
        @NotBlank String condition,
        @NotBlank String type,
        @PositiveOrZero Integer priceHuf,
        String note
) {}
