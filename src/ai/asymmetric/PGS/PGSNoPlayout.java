package ai.asymmetric.PGS;

import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.common.UnitScriptData;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

import java.util.List;

public class PGSNoPlayout extends PGSSCriptChoiceRandom {

    public PGSNoPlayout(int time, int max_playouts, int la, int a_I, int a_R, UnitTypeTable a_utt, PathFinding a_pf, List<AI> scripts) {
        // initializes with a default evaluation function (SimpleSqrtEvaluationFunction3)
        super(time, max_playouts, la, a_I, a_R, new SimpleSqrtEvaluationFunction3(), a_utt, a_pf, scripts);
    }

    /**
     * Replaces the default playout policy of PGS by a (hopefully) much shorter playout.
     * Plays out until the next decision point of the player, then just return the
     * value given by {@link ai.evaluation.SimpleSqrtEvaluationFunction3#evaluate}.
     * Also stops at gameover or at a maximum lookahead
     * @param player index of the player
     * @param gs state to evaluate
     * @param aiPlayer AI to issue the player's actions during the playouts
     * @param aiEnemy AI to issue the enemy's actions during the playouts
     * @return a value between [-1, 1], indicating the normalized material advantage of the state. See {@link SimpleSqrtEvaluationFunction3#evaluate}
     * @throws Exception
     */
    public double eval(int player, GameState gs, AI aiPlayer, AI aiEnemy) throws Exception {
        AI ai1 = aiPlayer.clone();
        AI ai2 = aiEnemy.clone();

        GameState gs2 = gs.clone();
        ai1.reset();
        ai2.reset();
        int dephtLimit = gs2.getTime() + LOOKAHEAD;

        boolean gameover = false;
        do {

            // issues the actions and advances the game state
            gs2.issueSafe(ai1.getAction(player, gs2));
            gs2.issueSafe(ai2.getAction(1 - player, gs2));

            gameover = gs2.cycle();

            // repeats until gameover, depthlimit or we reached a new decision point for the player
        } while (!gameover && gs2.getTime() < dephtLimit && !gs2.canExecuteAnyAction(player) );

        return evaluation.evaluate(player, 1 - player, gs2);
    }

    /**
     * Replaces the default playout policy of PGS by a (hopefully) much shorter playout.
     * Issues the action dictated by the provided scripts, then plays out randomly
     * until the next decision point of the player, then just returns the
     * value given by {@link ai.evaluation.SimpleSqrtEvaluationFunction3#evaluate}.
     * Also stops at gameover or at a maximum lookahead
     * @param player index of the player
     * @param gs state to evaluate
     * @param uScriptPlayer the script assignment for the player's units
     * @param aiEnemy AI to issue the first enemy actions
     * @return a value between [-1, 1], indicating the normalized material advantage of the state. See {@link SimpleSqrtEvaluationFunction3#evaluate}
     * @throws Exception
     */
    public double eval(int player, GameState gs, UnitScriptData uScriptPlayer, AI aiEnemy) throws Exception {
        AI ai2 = aiEnemy.clone();
        ai2.reset();
        GameState gs2 = gs.clone();

        gs2.issueSafe(getActionsUScript(player, uScriptPlayer, gs2));
        gs2.issueSafe(ai2.getAction(1 - player, gs2));

        int depthLimit = gs2.getTime() + LOOKAHEAD;
        boolean gameover;
        do {
            // implements the previously issued actions
            gameover = gs2.cycle();

            // issue the actions for the reached game state with a random biased AI
            gs2.issueSafe(randAI.getAction(player, gs2));
            gs2.issueSafe(randAI.getAction(1 - player, gs2));

            // repeats until gameover, depthlimit or we reached a new decision point for the player
        } while (!gameover && gs2.getTime() < depthLimit && !gs2.canExecuteAnyAction(player) );

        return evaluation.evaluate(player, 1 - player, gs2);
    }
}
