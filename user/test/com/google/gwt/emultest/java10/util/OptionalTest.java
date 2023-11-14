/*
 * Copyright 2023 Google Inc.
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
package com.google.gwt.emultest.java10.util;

import com.google.gwt.emultest.java.util.EmulTestBase;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Tests for java.util.Optional Java 10 API emulation.
 */
public class OptionalTest extends EmulTestBase {
  @SuppressWarnings("OptionalOfRedundantMethod")
  public void testOrElseThrow() {
    try {
      Optional.empty().orElseThrow();
      fail("Expected NoSuchElementException from empty Optional: orElseThrow");
    } catch (NoSuchElementException ignore) {
      // expected
    }

    String value = Optional.of("value").orElseThrow();
    assertEquals("value", value);
  }
}
