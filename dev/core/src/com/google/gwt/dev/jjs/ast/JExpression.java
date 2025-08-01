/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Base class for all Java expressions.
 */
public abstract class JExpression extends JNode implements HasType {

  public JExpression(SourceInfo info) {
    super(info);
  }

  public abstract boolean hasSideEffects();

  /**
   * Returns a statement that executes this expression.
   */
  public JStatement makeStatement() {
    return new JExpressionStatement(getSourceInfo(), this);
  }

  /**
   * Returns a return statement that returns this expression.
   */
  public final JReturnStatement makeReturnStatement() {
    return new JReturnStatement(getSourceInfo(), this);
  }
}
