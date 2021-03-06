package usspg31.tourney.model.pairingstrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

import usspg31.tourney.controller.PreferencesManager;
import usspg31.tourney.model.Pairing;
import usspg31.tourney.model.Pairing.PairingFlag;
import usspg31.tourney.model.PairingHelper;
import usspg31.tourney.model.Player;
import usspg31.tourney.model.PlayerScore;
import usspg31.tourney.model.PossibleScoring;
import usspg31.tourney.model.Tournament;

public class SwissSystem implements PairingStrategy {
    private static final Logger log = Logger.getLogger(SwissSystem.class
            .getName());

    @Override
    public ArrayList<Pairing> generatePairing(Tournament tournament) {

        ArrayList<Pairing> result = new ArrayList<>();
        Pairing partResult;
        ArrayList<PlayerScore> mergedScoreTable = new ArrayList<>();

        // generate the score table for the remaining player in the tournament
        mergedScoreTable = PairingHelper.mergeScoreRemainingPlayer(tournament);
        Collections.sort(mergedScoreTable);

        if (tournament.getRounds().size() == 0) {
            // random pairings in the first round of the tournament
            Random randomGenerator = new Random();
            ArrayList<Player> randomList = new ArrayList<>();
            randomList.addAll(tournament.getRemainingPlayers());
            int randomNumber;

            while (randomList.size() >= PairingHelper.findPhase(
                    tournament.getRounds().size(), tournament)
                    .getNumberOfOpponents()) {
                partResult = new Pairing();
                partResult.setFlag(Pairing.PairingFlag.IGNORE);

                for (int i = 0; i < PairingHelper.findPhase(
                        tournament.getRounds().size(), tournament)
                        .getNumberOfOpponents(); i++) {
                    randomNumber = randomGenerator.nextInt(randomList.size());

                    partResult.getScoreTable().add(
                            PairingHelper.generateEmptyScore(
                                    randomList.get(randomNumber), tournament
                                            .getRuleSet().getPossibleScores()
                                            .size()));

                    partResult.getOpponents().add(randomList.get(randomNumber));
                    randomList.remove(randomNumber);
                }

                result.add(partResult);
            }
            log.info("Pairings: " + result.size());
            log.info("List size: " + randomList.size());
            for (int i = 0; i < randomList.size(); i++) {
                partResult = new Pairing();
                partResult.setFlag(PairingFlag.IGNORE);
                partResult.getOpponents().add(randomList.get(i));
                partResult.getScoreTable().add(
                        PairingHelper.generateEmptyScore(randomList.get(i),
                                tournament.getRuleSet().getPossibleScores()
                                        .size()));
                for (PossibleScoring byeScore : tournament.getRuleSet()
                        .getPossibleScores()) {
                    partResult
                            .getScoreTable()
                            .get(0)
                            .getScore()
                            .add(byeScore.getPriority(),
                                    byeScore.getByeValue(tournament,
                                            randomList.get(i)));
                }

                result.add(partResult);
            }
        } else {
            int count;
            while (mergedScoreTable.size() >= PairingHelper.findPhase(
                    tournament.getRounds().size(), tournament)
                    .getNumberOfOpponents()) {
                partResult = new Pairing();
                partResult.setFlag(PairingFlag.IGNORE);

                log.info("Mergescoretable size: " + mergedScoreTable.size());
                for (PlayerScore mergedScoreTableEntry : mergedScoreTable) {
                    log.info("Player id: "
                            + mergedScoreTableEntry.getPlayer().getId());
                }

                for (int i = 0; i < PairingHelper.findPhase(
                        tournament.getRounds().size(), tournament)
                        .getNumberOfOpponents(); i++) {
                    count = 0;

                    partResult.getOpponents().add(
                            mergedScoreTable.get(mergedScoreTable.size() - 1)
                                    .getPlayer());
                    if (i == PairingHelper.findPhase(
                            tournament.getRounds().size(), tournament)
                            .getNumberOfOpponents() - 1) {
                        while (PairingHelper.isThereASimilarPairings(
                                partResult, tournament, result)) {
                            log.info("Similiar Pairing encounterd");
                            partResult.getOpponents().remove(
                                    mergedScoreTable.get(mergedScoreTable
                                            .size() - 1));

                            if (count == mergedScoreTable.size()) {
                                count = 0;
                                partResult.getOpponents().add(
                                        mergedScoreTable.get(
                                                mergedScoreTable.size() - 1)
                                                .getPlayer());

                            } else {
                                count++;
                                partResult.getOpponents().add(
                                        mergedScoreTable.get(
                                                mergedScoreTable.size() - 1
                                                        - count).getPlayer());
                            }
                        }

                        partResult.getScoreTable().add(
                                PairingHelper.generateEmptyScore(
                                        mergedScoreTable.get(
                                                mergedScoreTable.size() - 1
                                                        - count).getPlayer(),
                                        tournament.getRuleSet()
                                                .getPossibleScores().size()));
                        mergedScoreTable.remove(mergedScoreTable.size() - 1
                                - count);
                    } else {
                        partResult.getScoreTable().add(
                                PairingHelper.generateEmptyScore(
                                        mergedScoreTable.get(
                                                mergedScoreTable.size() - 1)
                                                .getPlayer(), tournament
                                                .getRuleSet()
                                                .getPossibleScores().size()));
                        mergedScoreTable.remove(mergedScoreTable.size() - 1);
                    }

                }

                result.add(partResult);
            }

            for (int i = 0; i < mergedScoreTable.size(); i++) {
                partResult = new Pairing();
                partResult.setFlag(PairingFlag.IGNORE);
                partResult.getOpponents().add(
                        mergedScoreTable.get(i).getPlayer());
                partResult.getScoreTable().add(
                        PairingHelper.generateEmptyScore(mergedScoreTable
                                .get(i).getPlayer(), tournament.getRuleSet()
                                .getPossibleScores().size()));
                for (PossibleScoring byeScore : tournament.getRuleSet()
                        .getPossibleScores()) {
                    partResult
                            .getScoreTable()
                            .get(0)
                            .getScore()
                            .add(byeScore.getPriority(),
                                    byeScore.getByeValue(tournament,
                                            mergedScoreTable.get(i).getPlayer()));
                }

                result.add(partResult);
            }
        }
        return result;

    }

    @Override
    public String getName() {
        return PreferencesManager.getInstance().localizeString(
                "pairingstrategy.swisssystem.name");
    }
}
