package ai.asymmetric.PGS;

import ai.abstraction.LightRush;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.units.UnitTypeTable;

import java.util.List;

/**
 * The same as {@link PGSSCriptChoiceRandom}, but uses {@link LightRush}
 * rather than {@link ai.RandomBiasedAI} for the playouts
 *
 */
public class PGSLightRushPlayout extends PGSSCriptChoiceRandom {

    /**
     * @param time time limit for 'thinking'
     * @param max_playouts ITERATIONS_BUDGET of the {@link ai.core.AIWithComputationBudget}, not used
     * @param la maximum playout lookahead (depth)
     * @param a_I not used
     * @param a_R not used
     * @param a_utt the UnitTypeTable
     * @param a_pf the path finding algorithm
     * @param scripts the portfolio of scripts
     */
    public PGSLightRushPlayout(int time, int max_playouts, int la, int a_I, int a_R, UnitTypeTable a_utt, PathFinding a_pf, List<AI> scripts) {
        // initializes with a default evaluation function (SimpleSqrtEvaluationFunction3)
        super(time, max_playouts, la, a_I, a_R, new SimpleSqrtEvaluationFunction3(), a_utt, a_pf, scripts);

        // tricks the parent class to use LightRush instead of RandomBiasedAI for the playouts
        randAI = new LightRush(a_utt);
    }

}
