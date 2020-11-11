package pl.fintech.dragons.dragonslending.offer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@Value
public class OfferReturnDto {
  UUID id;
  BigDecimal loanAmount;
  Integer timePeriod;
  Float interestRate;
  LocalDate endDate;
  BigDecimal finalValue;
  String user;
}
