/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb;

// Kept as .java file to use eclipse IDE's built-in task tracking
// This file will go through the Java compiler; so keep it compilable!

// TODO: get code coverage up to where it should be!
// TODO: prototype release with: https://github.com/semantic-release/semantic-release
// TODO: create a docker hub account to publish pre-built images?
// TODO: CI: build an publish docker image (master & develop)
// TODO: lock dependencies (including transitive)
// -----   --> https://docs.gradle.org/current/userguide/dependency_locking.html
// TODO: activate more pmd categories
// TODO: add spotbugs https://spotbugs.readthedocs.io/en/latest/gradle.html
// TODO: evaluate if FindSecurityBugs applies to this project?
// -----   --> https://find-sec-bugs.github.io/
// TODO: make persistent key-value-store available to bots (JetBrains Xodus?)
// ----- https://github.com/jetbrains/xodus
// ----- https://github.com/JetBrains/xodus/wiki/Entity-Stores
// TODO: make lombok, gradle and junit5 work together
