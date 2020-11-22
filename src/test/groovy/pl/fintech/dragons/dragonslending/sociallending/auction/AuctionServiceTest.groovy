package pl.fintech.dragons.dragonslending.sociallending.auction

import pl.fintech.dragons.dragonslending.sociallending.auction.calculator.AuctionCalculator
import pl.fintech.dragons.dragonslending.sociallending.auction.dto.AuctionQueryDto
import pl.fintech.dragons.dragonslending.sociallending.auction.dto.AuctionRequest
import pl.fintech.dragons.dragonslending.sociallending.identity.UserFixture
import pl.fintech.dragons.dragonslending.sociallending.identity.application.UserDto
import pl.fintech.dragons.dragonslending.sociallending.identity.application.UserService
import spock.lang.Specification

import java.nio.file.AccessDeniedException

import static AuctionFixtureData.*

class AuctionServiceTest extends Specification {
    AuctionRepository auctionRepository = Mock(AuctionRepository)
    AuctionCalculator auctionCalculator = Mock(AuctionCalculator)
    UserService userService = Mock(UserService)
    AuctionService auctionService = new AuctionService(auctionRepository, auctionCalculator, userService)

    def "Should get auction by id"() {
        given:
        mockUserById()
        mockRepositoryGetOne()
        mockAuctionCalculator()

        when:
        def auctionQueryDto = auctionService.getAuction(AUCTION_ID)

        then:
        auctionQueryDto == AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username)
    }

    def "Should return list of all auctions"() {
        given:
        mockUserById()
        mockAuctionCalculator()
        List<AuctionQueryDto> auctionList = [AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username),
                                             AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username)]
        auctionRepository.findAll() >> AUCTION_LIST

        when:
        def auctionQueryDto = auctionService.getPublicAuctions()

        then:
        auctionQueryDto == auctionList
        and:
        auctionQueryDto.size() == 2
    }

    def "Should return list of all current logged user auctions"() {
        given:
        mockCurrentLoggedUser()
        mockAuctionCalculator()
        List<AuctionQueryDto> auctionList = [
                AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username),
                AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username)]
        auctionRepository.findAllByUserId(UserFixture.USER_ID) >> AUCTION_LIST
        userService.getUser(UserFixture.USER_ID) >> UserFixture.USER_DTO

        when:
        def auctionQueryDto = auctionService.getCurrentUserAuctions()

        then:
        auctionQueryDto == auctionList
        and:
        auctionQueryDto.size() == 2
    }

    def "Should return list of all auctions without current logged user auctions"() {
        given:
        mockCurrentLoggedUser()
        mockAuctionCalculator()
        List<AuctionQueryDto> auctionList = [
                AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username),
                AUCTION.toAuctionDto(CALCULATION_DTO, UserFixture.USER_DTO.username)]
        auctionRepository.findAllByUserIdIsNot(UserFixture.USER_ID) >> AUCTION_LIST
        userService.getUser(UserFixture.USER_ID) >> UserFixture.USER_DTO

        when:
        def auctionQueryDto = auctionService.getAllNotCurrentUserAuctions()

        then:
        auctionQueryDto == auctionList
        and:
        auctionQueryDto.size() == 2
    }

    def "Should create new auction"() {
        given:
        mockCurrentLoggedUser()

        when:
        def auctionId = auctionService.saveAuctionDto(AUCTION_REQUEST)

        then:
        auctionId != null
    }

    def "Should update auction"() {
        given:
        mockCurrentLoggedUser()
        mockRepositoryGetOne()

        when:
        def auctionId = auctionService.updateAuctionDto(AUCTION_REQUEST)

        then:
        auctionId != null
    }

    def "Should throw illegal argument exception during update auction when auction id is null"() {
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

    def "Should throw access denied exception during update auction when this auction is not assign to current logged user"() {
        given:
        mockRepositoryGetOne()
        userService.getCurrentLoggedUser() >> UserDto.builder().id(UUID.randomUUID()).build()

        when:
        auctionService.updateAuctionDto(AUCTION_REQUEST)

        then:
        thrown(AccessDeniedException)
    }


    def "Should delete auction"() {
        given:
        mockRepositoryGetOne()
        userService.getCurrentLoggedUser() >> UserFixture.USER_DTO

        when:
        auctionService.deleteAuction(AUCTION_ID)


        then:
        1 * auctionRepository.deleteById(AUCTION_ID)
    }


    def "Should throw access denied exception during deleting auction when this auction is not assign to current logged user"() {
        given:
        mockRepositoryGetOne()
        userService.getCurrentLoggedUser() >> UserDto.builder().id(UUID.randomUUID()).build()

        when:
        auctionService.deleteAuction(AUCTION_ID)

        then:
        thrown(AccessDeniedException)
    }




    void mockRepositoryGetOne() {
        auctionRepository.getOne(AUCTION_ID) >> AUCTION
    }

    void mockAuctionCalculator() {
        auctionCalculator.calculate(AUCTION) >> CALCULATION_DTO
    }

    void mockUserById() {
        userService.getUser(UserFixture.USER_ID) >> UserFixture.USER_DTO
    }

    void mockCurrentLoggedUser() {
        userService.getCurrentLoggedUser() >> UserFixture.USER_DTO
    }
}