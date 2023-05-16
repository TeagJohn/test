package auto_testcase_generation.testdatagen.strategy;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.dse.logger.AkaLogger;

import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.testdatagen.se.IPathConstraints;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.PathConstraints;

/**
 * Choose a random unvisited branch in the current test path.
 * 
 * @author Duc Anh Nguyen
 *
 */
public class RandomUnvisitedBranchesSelection extends AbstractPathSelectionStrategy {
	final static AkaLogger logger = AkaLogger.get(RandomUnvisitedBranchesSelection.class);

	@Override
	public PathSelectionOutput negateTheOriginalPathConstraints() {
		PathSelectionOutput output = new PathSelectionOutput();

		IPathConstraints negatedConstraints = null;
		boolean foundNegatedCondition = false;
		Set<Integer> negatedIndexs = new HashSet<>();
		ConditionCfgNode negatedCfgNode;

		do {
			int negatedConstraintsIndexCandidate = new Random().nextInt(getOriginalConstraints().size());

			switch (((PathConstraints) getOriginalConstraints()).get(negatedConstraintsIndexCandidate).getType()) {
			case PathConstraint.ADDITIONAL_TYPE:
				negatedIndexs.add(negatedConstraintsIndexCandidate);
				continue;
			case PathConstraint.CREATE_FROM_DECISION:
				if (!negatedIndexs.contains(negatedConstraintsIndexCandidate)) {
					negatedIndexs.add(negatedConstraintsIndexCandidate);

					// If the negated condition is
					// not visited in all of two
					// branches
					negatedConstraints = getOriginalConstraints().negateConditionAt(negatedConstraintsIndexCandidate);

					// logger.debug("[Optimization]: negatedConstraints: " + negatedConstraints);
					// logger.debug("[Optimization]: getSolvedPathConstraints: " +
					// getSolvedPathConstraints());

					// If the negated path constraints are not solved before
					if (!getSolvedPathConstraints().contains(negatedConstraints)) {
						logger.debug("[Optimization] Add path constraints");
						getSolvedPathConstraints().add(negatedConstraints);

						negatedCfgNode = (ConditionCfgNode) ((PathConstraints) negatedConstraints)
								.get(negatedConstraintsIndexCandidate).getCfgNode();
						if (negatedCfgNode != null)
							if ((negatedCfgNode.getFalseNode() != null && !negatedCfgNode.isVisitedFalseBranch())
									|| (negatedCfgNode.getTrueNode() != null
											&& !negatedCfgNode.isVisitedTrueBranch())) {
								foundNegatedCondition = true;
							}
					} else
						logger.debug("[Optimization] Duplicate path constraints");
				}
				break;
			}
		} while (!foundNegatedCondition && negatedIndexs.size() < getOriginalConstraints().size());

		if (foundNegatedCondition)
			output.setNegatedPathConstraints(negatedConstraints);
		else
			output.setNegatedPathConstraints(null);

		if (negatedIndexs.size() == getOriginalConstraints().size())
			output.setNegateAllConditions(true);
		else
			output.setNegateAllConditions(false);
		return output;

	}
}
