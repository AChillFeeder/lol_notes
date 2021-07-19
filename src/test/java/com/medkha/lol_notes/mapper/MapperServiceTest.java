package com.medkha.lol_notes.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.medkha.lol_notes.dto.ChampionEssentielsDto;
import com.medkha.lol_notes.dto.DeathDTO;
import com.medkha.lol_notes.dto.DeathFilterOption;
import com.medkha.lol_notes.dto.GameDTO;
import com.medkha.lol_notes.dto.ReasonDTO;
import com.medkha.lol_notes.entities.Death;
import com.medkha.lol_notes.entities.Game;
import com.medkha.lol_notes.entities.Reason;
import com.medkha.lol_notes.mapper.impl.MapperServiceImpl;
import com.merakianalytics.orianna.types.core.staticdata.Champion;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class MapperServiceTest {

    private final MapperService mapper;

    public MapperServiceTest() {
        this.mapper = new MapperServiceImpl();
    }

    @Test void checkChampionEssentielMapping() {
        Champion champion = mock(Champion.class);
        when(champion.getId()).thenReturn(1);
        when(champion.getName()).thenReturn("test");

        ChampionEssentielsDto championEssentielsDto = mapper.convert(champion, ChampionEssentielsDto.class);

        assertAll(
                () -> assertEquals(champion.getId(), championEssentielsDto.getId()),
                () -> assertEquals(champion.getName(), championEssentielsDto.getName())
        );
    }

    @Test
    void checkMappingOfDeathDAO() {
        assertAll(
                () -> assertEquals(sampleDeathWithId(), this.mapper.convert(sampleDeathDTOWithId(), Death.class)),
                () -> assertEquals(sampleDeathDTOWithId(), this.mapper.convert(sampleDeathDTOWithId(), DeathDTO.class))
        );
    }

    @Test
    void mapValidClassDtoToParamName(){
        // given
        Class<GameDTO> gameDTOClass = GameDTO.class;
        Class<Game> gameClass = Game.class;

        // when
        String resultValid = this.mapper.mapClassDtoToParamName(gameDTOClass);
        String resultInvalid = this.mapper.mapClassDtoToParamName(gameClass);

        // then
        assertAll(
                () -> assertEquals("game", resultValid),
                () -> assertEquals("", resultInvalid)
        );
    }

    /**
     * I'm trying to make this test valable even in the future, but probably i should change this test.
     */
    @Test
    void mapInterfaceImplementationsToParams() {

        // when
        Set<String> result =
                this.mapper.convertInterfaceImplementationsToParamsByInterfaceAndSingleClassMapperFunction(
                        DeathFilterOption.class,
                        mapper::mapClassDtoToParamName);
        // then
        assertAll(
                () -> assertTrue(result.contains("game")),
                () -> assertTrue(result.contains("reason")),
                () -> assertTrue(result.size() > 0),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> this.mapper.convertInterfaceImplementationsToParamsByInterfaceAndSingleClassMapperFunction(
                                GameDTO.class,
                                mapper::mapClassDtoToParamName))
        );

    }
    private GameDTO sampleGameDTOWithId(){
        GameDTO game = new GameDTO();
        game.setChampionId(10);
        game.setRoleName("SOLO");
        game.setLaneName("MIDLANE");
        game.setId((long) 1);
        return game;
    }

    private Game sampleGameWithId() {
        Game game = new Game();
        game.setChampionId(10);
        game.setRoleName("SOLO");
        game.setLaneName("MIDLANE");
        game.setId((long) 1);
        return game;
    }

    private ReasonDTO sampleReasonDTOWithId(){
        ReasonDTO reason = new ReasonDTO();
        reason.setId((long) 1);
        reason.setDescription("sample reason");
        return reason;
    }

    private Reason sampleReasonWithId(){
        Reason reason = new Reason("sample reason");
        reason.setId((long) 1);
        return reason;
    }

    private DeathDTO sampleDeathDTOWithId(){
        DeathDTO death = new DeathDTO();
        death.setId((long)1);
        death.setMinute(1);
        death.setGame(GameDTO.copy(sampleGameDTOWithId()));
        death.setReason(ReasonDTO.copy(sampleReasonDTOWithId()));
        return death;
    }
    private Death sampleDeathWithId(){
        Death death = new Death(1,sampleReasonWithId(), sampleGameWithId());
        death.setId((long)1);
        return death;
    }

}
