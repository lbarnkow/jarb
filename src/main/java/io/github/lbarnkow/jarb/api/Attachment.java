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

package io.github.lbarnkow.jarb.api;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

/**
 * An attachment to a chat <code>Message</code>.
 *
 * @author lbarnkow
 */
@Value
@Builder
public class Attachment {
  /**
   * The color you want the order on the left side to be, any value background-css
   * supports.
   */
  String color;

  /**
   * The text to display for this attachment, it is different than the message's
   * text.
   */
  String text;

  /**
   * Displays the time next to the text portion.
   */
  Instant timestamp;

  /**
   * An image that displays to the left of the text, looks better when this is
   * relatively small.
   */
  String thumbUrl;

  /**
   * Only applicable if the ts is provided, as it makes the time clickable to this
   * link.
   */
  String messageLink;

  /**
   * Causes the image, audio, and video sections to be hiding when collapsed is
   * true.
   */
  boolean collapsed;

  /**
   * Name of the author.
   */
  String authorName;

  /**
   * Providing this makes the author name clickable and points to this link.
   */
  String authorLink;

  /**
   * Displays a tiny icon to the left of the Author’s name.
   */
  String authorIcon;

  /**
   * Title to display for this attachment, displays under the author.
   */
  String title;

  /**
   * Providing this makes the title clickable, pointing to this link.
   */
  String titleLink;

  /**
   * When this is true, a download icon appears and clicking this saves the link
   * to file.
   */
  boolean titleLinkDownload;

  /**
   * The image to display, will be “big” and easy to see.
   */
  String imageUrl;

  /**
   * Audio file to play, only supports what html audio does.
   */
  String audioUrl;

  /**
   * Video file to play, only supports what html video does.
   */
  String videoUrl;
}