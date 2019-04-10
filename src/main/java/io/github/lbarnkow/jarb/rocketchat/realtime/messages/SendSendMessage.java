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

package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * See
 * https://rocket.chat/docs/developer-guides/realtime-api/method-calls/send-message/
 * .
 * 
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendSendMessage extends BaseMethod {
  /**
   * The method name.
   */
  private static final String METHOD = "sendMessage";

  /**
   * The parameters.
   */
  private List<RawMessage> params;

  /**
   * Constructs a new instance.
   * 
   * @param message the message to send
   */
  public SendSendMessage(Message message) {
    super(METHOD);
    params = Arrays.asList(RawMessage.of(message));
  }
}
