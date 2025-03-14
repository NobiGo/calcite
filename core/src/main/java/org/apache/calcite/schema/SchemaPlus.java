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
package org.apache.calcite.schema;

import org.apache.calcite.materialize.Lattice;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.lookup.Lookup;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Extension to the {@link Schema} interface.
 *
 * <p>Given a user-defined schema that implements the {@link Schema} interface,
 * Calcite creates a wrapper that implements the {@code SchemaPlus} interface.
 * This provides extra functionality, such as access to tables that have been
 * added explicitly.
 *
 * <p>A user-defined schema does not need to implement this interface, but by
 * the time a schema is passed to a method in a user-defined schema or
 * user-defined table, it will have been wrapped in this interface.
 *
 * <p>SchemaPlus is intended to be used by users but not instantiated by them.
 * Users should only use the SchemaPlus they are given by the system.
 * The purpose of SchemaPlus is to expose to user code, in a read only manner,
 * some of the extra information about schemas that Calcite builds up when a
 * schema is registered. It appears in several SPI calls as context; for example
 * {@link SchemaFactory#create(SchemaPlus, String, java.util.Map)} contains a
 * parent schema that might be a wrapped instance of a user-defined
 * {@link Schema}, or indeed might not.
 */
public interface SchemaPlus extends Schema {

  /**
   * Returns a lookup object to find sub schemas.
   */
  @Override Lookup<? extends SchemaPlus> subSchemas();
  /**
   * Returns the parent schema, or null if this schema has no parent.
   */
  @Nullable SchemaPlus getParentSchema();

  /**
   * Returns the name of this schema.
   *
   * <p>The name must not be null, and must be unique within its parent.
   * The root schema is typically named "".
   */
  String getName();

  // override with stricter return
  @Deprecated @Override @Nullable SchemaPlus getSubSchema(String name);

  /** Adds a schema as a sub-schema of this schema, and returns the wrapped
   * object. */
  SchemaPlus add(String name, Schema schema);

  /** Adds a table to this schema. */
  void add(String name, Table table);

  /** Removes a table from this schema, used e.g. to clean-up temporary tables. */
  default boolean removeTable(String name) {
    // Default implementation provided for backwards compatibility, to be removed before 2.0
    return false;
  }


  /** Adds a function to this schema. */
  void add(String name, Function function);

  /** Adds a type to this schema.  */
  void add(String name, RelProtoDataType type);

  /** Adds a lattice to this schema. */
  void add(String name, Lattice lattice);

  @Override boolean isMutable();

  /** Returns an underlying object. */
  <T extends Object> @Nullable T unwrap(Class<T> clazz);

  void setPath(ImmutableList<ImmutableList<String>> path);

  void setCacheEnabled(boolean cache);

  boolean isCacheEnabled();
}
