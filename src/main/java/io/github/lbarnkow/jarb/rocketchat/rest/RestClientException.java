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

package io.github.lbarnkow.jarb.rocketchat.rest;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Exception wrapping any error occuring during a REST call to the chat server.
 *
 * @author lbarnkow
 */
public class RestClientException extends Exception {
  private static final long serialVersionUID = 245562960868828052L;

  /**
   * Constructs a new instance.
   *
   * @param object The response from the REST endpoint
   */
  public RestClientException(final Object object) {
    super("Bad response from REST endpoint: "
        + ToStringBuilder.reflectionToString(object, SHORT_PREFIX_STYLE));
  }
}
