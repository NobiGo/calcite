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
package org.apache.calcite.sql.type;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.sql.SqlCallBinding;
import org.apache.calcite.sql.SqlOperandCountRange;
import org.apache.calcite.sql.SqlOperator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Parameter type-checking strategy for Explicit Type.
 */
public class ExplicitOperandTypeChecker implements SqlOperandTypeChecker {
  //~ Methods ----------------------------------------------------------------

  private final RelDataType type;

  public ExplicitOperandTypeChecker(RelDataType type) {
    this.type = requireNonNull(type, "type");
  }

  @Override public boolean checkOperandTypes(
      SqlCallBinding callBinding,
      boolean throwOnFailure) {
    if (!type.isStruct()) {
      if (throwOnFailure) {
        throw callBinding.newValidationSignatureError();
      } else {
        return false;
      }
    }

    List<SqlTypeFamily> families = new ArrayList<>();

    List<RelDataTypeField> fieldList = type.getFieldList();
    for (int i = 0; i < fieldList.size(); i++) {
      RelDataTypeField field = fieldList.get(i);
      SqlTypeName sqlTypeName = field.getType().getSqlTypeName();
      if (sqlTypeName == SqlTypeName.ROW) {
        if (field.getType().equals(callBinding.getOperandType(i))) {
          families.add(SqlTypeFamily.ANY);
        }
      } else {
        families.add(
            requireNonNull(sqlTypeName.getFamily(),
                () -> "keyType.getSqlTypeName().getFamily() null, type is " + sqlTypeName));
      }
    }
    return OperandTypes.family(families).checkOperandTypes(callBinding, throwOnFailure);
  }

  @Override public SqlOperandCountRange getOperandCountRange() {
    if (type.isStruct()) {
      return SqlOperandCountRanges.of(type.getFieldCount());
    }
    // This is a type constructor for a scalar type, which is illegal (e.g. INT64(...))
    // (the validator will accept this for type aliases).
    // We pretend here it's OK, but we throw in checkOperandTypes.
    return SqlOperandCountRanges.between(0, -1);
  }

  @Override public String getAllowedSignatures(SqlOperator op, String opName) {
    StringBuilder builder = new StringBuilder();
    builder.append(opName);
    if (type.isStruct()) {
      builder.append("(");
      boolean first = true;
      for (RelDataTypeField field : type.getFieldList()) {
        if (!first) {
          builder.append(", ");
        }
        first = false;
        builder.append(field.getType());
      }
      builder.append(")");
    }
    return builder.toString();
  }
}
