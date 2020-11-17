package pl.fintech.dragons.dragonslending.payment.account.application

import pl.fintech.dragons.dragonslending.common.events.EventPublisher
import pl.fintech.dragons.dragonslending.payment.account.domain.AccountRepository
import pl.fintech.dragons.dragonslending.payment.account.domain.BankApiService
import pl.fintech.dragons.dragonslending.payment.account.domain.MoneyTransferEvent
import pl.fintech.dragons.dragonslending.security.AuthenticationFacade
import spock.lang.Specification

import static pl.fintech.dragons.dragonslending.payment.account.AccountFixtures.USER_ID
import static pl.fintech.dragons.dragonslending.payment.account.AccountFixtures.getACCOUNT

class WithdrawMoneyCommandHandlerTest extends Specification {

    AccountRepository accountRepository = Mock(AccountRepository)
    EventPublisher eventPublisher = Mock(EventPublisher)
    AuthenticationFacade authenticationFacade = Mock(AuthenticationFacade)
    BankApiService bankApiService = Mock(BankApiService)
    WithdrawMoneyCommandHandler withdrawMoneyCommandHandler = new WithdrawMoneyCommandHandler(accountRepository, eventPublisher, authenticationFacade, bankApiService)

    def "should withdraw money from account and publish event"() {
        given:
        UUID requestedAccountNumber = UUID.randomUUID()
        BigDecimal amountToWithdraw = BigDecimal.TEN
        authenticationFacade.idOfCurrentLoggedUser() >> USER_ID
        accountRepository.getOne(USER_ID) >> ACCOUNT

        when:
        withdrawMoneyCommandHandler.withdraw(requestedAccountNumber, amountToWithdraw)

        then:
        1 * bankApiService.requestWithdraw(requestedAccountNumber, amountToWithdraw)
        1 * eventPublisher.publish(_ as MoneyTransferEvent.MoneyWithdrawn)
    }
}