/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.calcite.rel.rules;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Union;
import org.apache.calcite.rel.core.Values;

import org.immutables.value.Value;

/**
 * Planner rule that translates a distinct
 * {@link org.apache.calcite.rel.core.Union}
 * (<code>all</code> = <code>true</code>)
 * into an {@link org.apache.calcite.rel.core.Aggregate}
 * on top of a non-distinct {@link org.apache.calcite.rel.core.Union}
 * (<code>all</code> = <code>true</code>).
 *
 * @see CoreRules#UNION_TO_DISTINCT
 */
@Value.Enclosing
public class UnionValuesToValuesRule extends RelRule<UnionValuesToValuesRule.Config>
    implements TransformationRule {

  /**
   * Creates a UnionValuesToValuesRule.
   */
  protected UnionValuesToValuesRule(UnionValuesToValuesRule.Config config) {
    super(config);
  }

  //~ Methods ----------------------------------------------------------------

  @Override
  public boolean matches(RelOptRuleCall call) {
    Union union = call.rel(0);
    return union.all && union.getInputs().stream().allMatch(input-> input instanceof Values);
  }

  @Override
  public void onMatch(RelOptRuleCall call) {
    final Union union = call.rel(0);
    final Values values = call.rel(1);

  }

  /**
   * Rule configuration.
   */
  @Value.Immutable
  public interface Config extends RelRule.Config {
    Config DEFAULT = ImmutableUnionValuesToValuesRule.Config.of()
        .withDescription("UnionValuesToValuesRule")
        .withOperandFor(Union.class);

    @Override
    default UnionValuesToValuesRule toRule() {
      return new UnionValuesToValuesRule(this);
    }

    /**
     * Defines an operand tree for the given classes.
     */
    default UnionValuesToValuesRule.Config withOperandFor(Class<? extends Union> unionClass) {
      return withOperandSupplier(b0 ->
          b0.operand(unionClass).inputs(
              b1 -> b1.operand(RelNode.class).inputs()))
          .as(UnionValuesToValuesRule.Config.class);
    }
  }
}
