package ai.asymmetric.PGS;

import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.GameState;
import rts.units.UnitTypeTable;

import java.util.List;

public class PGSNoPlayout extends PGSSCriptChoiceRandom {

    public PGSNoPlayout(int time, int max_playouts, int la, int a_I, int a_R, UnitTypeTable a_utt, PathFinding a_pf, List<AI> scripts) {
        // initializes with a default evaluation function (SimpleSqrtEvaluationFunction3)
        super(time, max_playouts, la, a_I, a_R, new SimpleSqrtEvaluationFunction3(), a_utt, a_pf, scripts);
    }

    /**
     * Makes no playouts, just return the value given by the {@link ai.evaluation.SimpleSqrtEvaluationFunction3}
     * @param player index of the player
     * @param gs
     * @param aiPlayer
     * @param aiEnemy
     * @return
     * @throws Exception
     */
    public double eval(int player, GameState gs, AI aiPlayer, AI aiEnemy) throws Exception {
        /*AI ai1 = aiPlayer.clone();
        AI ai2 = aiEnemy.clone();

        GameState gs2 = gs.clone();
        ai1.reset();
        ai2.reset();
        int timeLimit = gs2.getTime() + LOOKAHEAD;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
            } else {
                gs2.issue(ai1.getAction(player, gs2));
                gs2.issue(ai2.getAction(1 - player, gs2));
            }
        }*/
        return evaluation.evaluate(player, 1 - player, gs);
    }
}
