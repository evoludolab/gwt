/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.CompilerContext;
import com.google.gwt.dev.javac.testing.impl.JavaResourceBase;
import com.google.gwt.dev.javac.testing.impl.MockResource;
import com.google.gwt.dev.javac.testing.impl.MockResourceOracle;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;

import com.google.gwt.thirdparty.guava.common.hash.Hashing;
import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Base class for tests that need a mock type compilation state and everything
 * that goes with it (compilation units, type oracle, resources, ...).
 */
public abstract class CompilationStateTestBase extends TestCase {

  /**
   * Tweak this if you want to see the log output.
   */
  public static TreeLogger createTreeLogger() {
    boolean reallyLog = false;
    if (reallyLog) {
      AbstractTreeLogger logger = new PrintWriterTreeLogger();
      logger.setMaxDetail(TreeLogger.WARN);
      return logger;
    }
    return TreeLogger.NULL;
  }

  public static Set<GeneratedUnit> getGeneratedUnits(
      MockResource... sourceFiles) {
    Set<GeneratedUnit> units = new HashSet<GeneratedUnit>();
    for (final MockResource sourceFile : sourceFiles) {
      units.add(new GeneratedUnit() {
        @Override
        public long creationTime() {
          return sourceFile.getLastModified();
        }

        @Override
        public String getSource() {
          return sourceFile.getString();
        }

        @Override
        public String getSourceMapPath() {
          return getTypeName().replace(".", "/") + ".java";
        }

        @Override
        public long getSourceToken() {
          return -1;
        }

        @Override
        public String getStrongHash() {
          return Hashing.murmur3_128().hashString(getSource(), StandardCharsets.UTF_8).toString();
        }

        @Override
        public String getTypeName() {
          return Shared.getTypeName(sourceFile);
        }

        @Override
        public String optionalFileLocation() {
          return sourceFile.getLocation();
        }
      });
    }
    return units;
  }

  static void assertUnitsChecked(Collection<CompilationUnit> units) {
    for (CompilationUnit unit : units) {
      assertFalse(unit.isError());
      assertTrue(unit.getCompiledClasses().size() > 0);
    }
  }

  protected CompilerContext compilerContext;

  /**
   * Ensure a clean cache at the beginning of every test run!
   */
  protected final CompilationStateBuilder isolatedBuilder = new CompilationStateBuilder();

  protected MockResourceOracle oracle;

  protected CompilationState state;

  protected CompilationStateTestBase() {
    oracle = new MockResourceOracle(JavaResourceBase.getStandardResources());
    compilerContext = new CompilerContext();
    rebuildCompilationState();
  }

  protected void addGeneratedUnits(MockResource... sourceFiles) {
    try {
      state.addGeneratedCompilationUnits(createTreeLogger(),
          getGeneratedUnits(sourceFiles));
    } catch (UnableToCompleteException e) {
      throw new RuntimeException(e);
    }
  }

  protected void rebuildCompilationState() {
    try {
      state = isolatedBuilder.doBuildFrom(
          createTreeLogger(), compilerContext, oracle.getResources());
    } catch (UnableToCompleteException e) {
      throw new RuntimeException(e);
    }
  }

  protected void validateCompilationState(String... generatedTypeNames) {
    // Save off the reflected collections.
    Map<String, CompilationUnit> unitMap = state.getCompilationUnitMap();
    Collection<CompilationUnit> units = state.getCompilationUnits();

    // Validate that we have as many units as resources.
    assertEquals(oracle.getResources().size() + generatedTypeNames.length,
        units.size());

    // Validate that the collections are consistent with each other.
    assertEquals(new HashSet<CompilationUnit>(unitMap.values()),
        new HashSet<CompilationUnit>(units));

    // Save off a mutable copy of the source map and generated types to compare.
    Set<String> resourcePathNames = new HashSet<String>(
        oracle.getPathNames());
    Set<String> generatedTypes = new HashSet<String>(
        Arrays.asList(generatedTypeNames));
    assertEquals(resourcePathNames.size() + generatedTypes.size(), units.size());
    for (Entry<String, CompilationUnit> entry : unitMap.entrySet()) {
      // Validate source file internally consistent.
      String className = entry.getKey();
      CompilationUnit unit = entry.getValue();
      assertEquals(className, unit.getTypeName());

      // Find the matching resource (and remove it).
      if (generatedTypes.contains(className)) {
        // Not always true due to caching! A source unit for FOO can b
        // identical to the generated FOO and already be cached.
        // assertTrue(unit.isGenerated());
        assertNotNull(generatedTypes.remove(className));
      } else {
        String partialPath = className.replace('.', '/') + ".java";
        assertTrue(resourcePathNames.contains(partialPath));
        // TODO: Validate the source file matches the resource.
        assertNotNull(resourcePathNames.remove(partialPath));
      }
    }
    // The mutable sets should be empty now.
    assertEquals(0, resourcePathNames.size());
    assertEquals(0, generatedTypes.size());
  }
}
