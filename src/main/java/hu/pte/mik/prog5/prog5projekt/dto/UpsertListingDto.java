package hu.pte.mik.prog5.prog5projekt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record UpsertListingDto(
        @NotNull Long bookId,
        @Pattern(regexp = "NEW|GOOD|USED|WORN") String condition,
        @Pattern(regexp = "SELL|BUY|TRADE|GIVEAWAY") String type,
        @Pattern(regexp = "ACTIVE|RESERVED|CLOSED") String status,
        @Min(0) int priceHuf,
        @Size(max = 2000) String note
) {}
