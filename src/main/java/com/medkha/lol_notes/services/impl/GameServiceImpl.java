package com.medkha.lol_notes.services.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import com.medkha.lol_notes.dto.GameDTO;
import com.medkha.lol_notes.entities.Game;
import com.medkha.lol_notes.exceptions.NoElementFoundException;
import com.medkha.lol_notes.mapper.MapperService;
import com.medkha.lol_notes.repositories.GameRepository;
import com.medkha.lol_notes.services.ChampionService;
import com.medkha.lol_notes.services.DeathService;
import com.medkha.lol_notes.services.GameService;
import com.medkha.lol_notes.services.QueueService;
import com.medkha.lol_notes.services.RoleAndLaneService;
import com.medkha.lol_notes.services.filters.DeathFilterService;

@Service
public class GameServiceImpl implements GameService{
	
	private static final Logger log = 
			LoggerFactory.getLogger(GameServiceImpl.class); 

	private final ChampionService championService;
	private final GameRepository gameRepository;
	private final RoleAndLaneService roleAndLaneService;
	private final QueueService queueService;
	private final DeathService deathService;
	private final DeathFilterService deathFilterService;
	private final MapperService mapperService;
	public GameServiceImpl(
			GameRepository gameRepository,
			ChampionService championService,
			RoleAndLaneService roleAndLaneService,
			QueueService queueService,
			DeathService deathService,
			DeathFilterService deathFilterService,
			MapperService mapperService) {
		this.gameRepository = gameRepository;
		this.championService = championService;
		this.roleAndLaneService = roleAndLaneService;
		this.queueService = queueService;
		this.deathService = deathService;
		this.deathFilterService = deathFilterService;
		this.mapperService = mapperService;
	}

	
	@Override
	@Transactional
	public GameDTO createGame(GameDTO gameDTO){
		try {
			championService.getChampionById(gameDTO.getChampionId());
//			TODO: see the impact of removing the lane and role exception handler.
//			isLaneExceptionHandler(gameDTO);
//			isRoleExceptionHandler(gameDTO);
			isQueueExceptionHandler(gameDTO);
			Game createdGame = this.gameRepository.save(mapperService.convert(gameDTO, Game.class));
			log.info("createGame: Game with id: " + createdGame.getId() + " created successfully.");
			return mapperService.convert(createdGame, GameDTO.class);
		} catch (InvalidDataAccessApiUsageException | NullPointerException err) {
			log.error("createGame: Game Object is null and cannot be proceed, stack error: " + err);
			throw new IllegalArgumentException("Game Object is null and cannot be proceed", err);
		}
	}

	private void isQueueExceptionHandler(GameDTO gameDTO) {
		queueService.getQueueById(gameDTO.getQueueId()).orElseThrow(
					() -> new IllegalArgumentException("Game Object has an unknown queue so can't proceed")
		);
	}

	private void isRoleExceptionHandler(GameDTO game) {
		if(game.getRoleName().isEmpty() || game.getRoleName() == null) {
			log.error("isRoleExceptionHandler: role of the game to create is empty or null, so can't proceed.");
			throw new IllegalArgumentException("role of the game to create is empty or null, so can't proceed.");
		}
		if(!roleAndLaneService.isRole(game.getRoleName())) {
			log.error("isRoleExceptionHandler: No Role with name {} was found.", game.getRoleName());
			throw new NoElementFoundException("No Role with name " + game.getRoleName() + " was found.");
		}
	}

	private void isLaneExceptionHandler(GameDTO game) {
		if(game.getLaneName().isEmpty() || game.getLaneName() == null) {
			log.error("isLaneExceptionHandler: lane of the game to create is empty or null, so can't proceed.");
			throw new IllegalArgumentException("lane of the game to create is empty or null, so can't proceed.");
		}
		if(!roleAndLaneService.isLane(game.getLaneName())) {
			log.error("isLaneExceptionHandler: No Lane with name {} was found.", game.getLaneName());
			throw new NoElementFoundException("No Lane with name " + game.getLaneName() + " was found.");
		}
	}

	@Override
	@Transactional
	public GameDTO updateGame(GameDTO gameDTO){
		try {
			findById(gameDTO.getId());
			championService.getChampionById(gameDTO.getChampionId());
			isRoleExceptionHandler(gameDTO);
			isLaneExceptionHandler(gameDTO);
			isQueueExceptionHandler(gameDTO);
			Game updatedGame = this.gameRepository.save(mapperService.convert(gameDTO, Game.class));
			log.info("updateGame: Game with id: " + updatedGame.getId() + " was updated successfully.");
			return mapperService.convert(updatedGame, GameDTO.class);
	
		} catch (InvalidDataAccessApiUsageException | NullPointerException err) {
			
			log.error("updateGame: Game Object is null and cannot be proceed");
			throw new IllegalArgumentException("Game Object is null and cannot be processed", err);
		}
	}

	@Override
	public void deleteGame(Long id) {
		GameDTO foundGame = findById(id);
		deleteAssociatedDeaths(foundGame);
		// this try catch is just a safe net, if findById doesn't include null id traitement.
		try {			
			this.gameRepository.deleteById(id);
			log.info("deleteGame: game with id: " + id + " was deleted successfully.");
		} catch (IllegalArgumentException err) {
			log.error("deleteGame: game id is null, so can't proceed.");
			throw new IllegalArgumentException("game id is null, so can't proceed.", err); 
		}
	}

	private void deleteAssociatedDeaths(GameDTO game) {
		log.info("deleteAssociatedDeaths: start deleting associated deaths of game with id: {}.", game.getId());
		this.deathFilterService.getDeathsByFilter(Collections.singletonList(game.getPredicate())).forEach(
				(d) -> {
					this.deathService.deleteDeathById(d.getId());
					log.info("deleteAssociatedDeaths: Delete death with id: {} successfully.", d.getId());
				}
		);
	}

	@Override
	public Set<GameDTO> findAllGames() {
		Set<Game> findallGamesSet = new HashSet<>(); 
		this.gameRepository.findAll().forEach(findallGamesSet::add);
		log.info("findAllGames: " + findallGamesSet.size() + " games were found.");
		
		return this.mapperService.convertSet(findallGamesSet, GameDTO.class);
	}

	@Override
	public GameDTO findById(Long id) {
		try {
			Game foundGame = this.gameRepository.findById(id).orElseThrow();
			log.info("findById: Game with id: " + id + " was found successfully.");
			return this.mapperService.convert(foundGame, GameDTO.class);
		} catch (InvalidDataAccessApiUsageException | NullPointerException err ) {
			
			log.error("findById: Game Object has null id, and cannot be processed");
			throw new IllegalArgumentException("Game Object has null id, and cannot be processed", err); 
		} catch (NoSuchElementException err) { 
			
			log.error("No Element of type Game with id " + id + " was found in the database.");
			throw new NoElementFoundException("No Element of type Game with id " + id + " was found in the database.", err); 
		}
	}

}
