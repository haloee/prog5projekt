package hu.pte.mik.prog5.prog5projekt.dto;

import jakarta.validation.constraints.*;

public record NewBookDto(
        @NotBlank String title,
        @NotBlank String author,
        @Pattern(regexp = "\\d{10}|\\d{13}", message = "ISBN must be 10 or 13 digits") String isbn,
        @Min(1400) @Max(2100) Integer publishedYear,
        @NotNull Long createdByUserId
) {}
