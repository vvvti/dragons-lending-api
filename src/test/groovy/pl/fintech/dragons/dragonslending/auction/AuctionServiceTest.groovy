package pl.fintech.dragons.dragonslending.auction

import pl.fintech.dragons.dragonslending.auction.calculator.AuctionCalculator
import pl.fintech.dragons.dragonslending.auction.dto.AuctionQueryDto
import pl.fintech.dragons.dragonslending.auction.dto.AuctionRequest
import pl.fintech.dragons.dragonslending.identity.application.UserDto
import pl.fintech.dragons.dragonslending.identity.application.UserService
import spock.lang.Specification

import java.nio.file.AccessDeniedException

import static pl.fintech.dragons.dragonslending.auction.AuctionFixture.*
import static pl.fintech.dragons.dragonslending.identity.UserFixture.getUSER_DTO
import static pl.fintech.dragons.dragonslending.identity.UserFixture.getUSER_ID

class AuctionServiceTest extends Specification {
    AuctionRepository auctionRepository = Mock(AuctionRepository)
    AuctionCalculator auctionCalculator = Mock(AuctionCalculator)
    UserService userService = Mock(UserService)
    AuctionService auctionService = new AuctionService(auctionRepository, auctionCalculator, userService)

    def "should get auctionQueryDto by id"() {
        given:
        mockUserById()
        mockRepositoryGetOne()
        mockAuctionCalculator()

        when:
        def auctionQueryDto = auctionService.getAuction(AUCTION_ID)

        then:
        auctionQueryDto == AUCTION.toAuctionDto(CALCULATION_DTO, USER_DTO.username)
    }

    def "should return list of auctionQueryDto"() {
        given:
        List<AuctionQueryDto> auctionList = [AUCTION.toAuctionDto(CALCULATION_DTO, USER_DTO.username), AUCTION.toAuctionDto(CALCULATION_DTO, USER_DTO.username)]
        mockUserById()
        mockRepositoryFindAll()
        mockAuctionCalculator()

        when:
        def auctionQueryDto = auctionService.getAuctions()

        then:
        auctionQueryDto == auctionList
        and:
        auctionQueryDto.size() == 2
    }


    def "should create new auction"() {
        given:
        mockCurrentLoggedUser()

        when:
        def auctionId = auctionService.saveAuctionDto(AUCTION_REQUEST)

        then:
        auctionId != null
    }

    def "should update auction"() {
        given:
        mockCurrentLoggedUser()
        mockRepositoryGetOne()

        when:
        def auctionId = auctionService.updateAuctionDto(AUCTION_REQUEST)

        then:
        auctionId != null
    }

    def "should throw illegal argument exception during update auction when auction id is null"() {
        given:
        AuctionRequest auctionRequest = AuctionRequest.builder()
                .loanAmount(AUCTION_REQUEST.loanAmount)
                .timePeriod(AUCTION_REQUEST.timePeriod)
                .interestRate(AUCTION_REQUEST.interestRate)
                .endDate(AUCTION_REQUEST.endDate)
                .build()

        when:
        auctionService.updateAuctionDto(auctionRequest)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw access denied exception during update auction when this auction is not assign to current logged user"() {
        given:
        mockRepositoryGetOne()
        userService.getCurrentLoggedUser() >> UserDto.builder().id(UUID.randomUUID()).build()

        when:
        auctionService.updateAuctionDto(AUCTION_REQUEST)

        then:
        thrown(AccessDeniedException)
    }


    void mockRepositoryGetOne() {
        auctionRepository.getOne(AUCTION_ID) >> AUCTION
    }

    void mockRepositoryFindAll() {
        auctionRepository.findAll() >> AUCTION_LIST
    }

    void mockAuctionCalculator() {
        auctionCalculator.calculate(AUCTION) >> CALCULATION_DTO
    }

    void mockUserById() {
        userService.getUser(USER_ID) >> USER_DTO
    }

    void mockCurrentLoggedUser() {
        userService.getCurrentLoggedUser() >> USER_DTO
    }
}