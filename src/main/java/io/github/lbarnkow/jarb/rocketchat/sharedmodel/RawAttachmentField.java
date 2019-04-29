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

package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

/**
 * See
 * https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/#attachment-field-objects
 * .
 * 
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
public class RawAttachmentField {
  /**
   * Whether this field should be a short field.
   */
  @JsonProperty("short")
  private boolean bshort;

  /**
   * The title of this field.
   */
  private String title;

  /**
   * The value of this field, displayed underneath the title value.
   */
  private String value;
}
