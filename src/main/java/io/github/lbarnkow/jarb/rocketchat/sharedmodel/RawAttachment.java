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
import io.github.lbarnkow.jarb.api.Attachment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * See
 * https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/#attachments-detail
 * .
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
@SuppressWarnings("PMD.TooManyFields")
public class RawAttachment {
  /**
   * The color you want the order on the left side to be, any value background-css
   * supports.
   */
  private String color;

  /**
   * The text to display for this attachment, it is different than the message’s
   * text.
   */
  private String text;

  /**
   * Displays the time next to the text portion.
   */
  @SuppressWarnings("PMD.ShortVariable")
  private String ts;

  /**
   * An image that displays to the left of the text, looks better when this is
   * relatively small.
   */
  @JsonProperty("thumb_url")
  private String thumbUrl;

  /**
   * Only applicable if the ts is provided, as it makes the time clickable to this
   * link.
   */
  @JsonProperty("message_link")
  private String messageLink;

  /**
   * Causes the image, audio, and video sections to be hiding when collapsed is
   * true.
   */
  private boolean collapsed;

  /**
   * Name of the author.
   */
  @JsonProperty("author_name")
  private String authorName;

  /**
   * Providing this makes the author name clickable and points to this link.
   */
  @JsonProperty("author_link")
  private String authorLink;

  /**
   * Displays a tiny icon to the left of the Author’s name.
   */
  @JsonProperty("author_icon")
  private String authorIcon;

  /**
   * Title to display for this attachment, displays under the author.
   */
  private String title;

  /**
   * Providing this makes the title clickable, pointing to this link.
   */
  @JsonProperty("title_link")
  private String titleLink;

  /**
   * When this is true, a download icon appears and clicking this saves the link
   * to file.
   */
  @JsonProperty("title_link_download")
  private boolean titleLinkDownload;

  /**
   * The image to display, will be “big” and easy to see.
   */
  @JsonProperty("image_url")
  private String imageUrl;

  /**
   * Audio file to play, only supports what html audio does.
   */
  @JsonProperty("audio_url")
  private String audioUrl;

  /**
   * Video file to play, only supports what html video does.
   */
  @JsonProperty("video_url")
  private String videoUrl;

  /**
   * An array of Attachment Field Objects.
   */
  @Builder.Default
  private List<RawAttachmentField> fields = Collections.emptyList();

  /**
   * Converts this instance to an <code>Attachment</code> instance.
   *
   * @return the resulting <code>Attachment</code>
   */
  public Attachment convert() {
    return Attachment.builder() //
        .color(color) //
        .text(text) //
        .timestamp(ts == null ? null : Instant.parse(ts)) //
        .thumbUrl(thumbUrl) //
        .messageLink(messageLink) //
        .collapsed(collapsed) //
        .authorName(authorName) //
        .authorLink(authorLink) //
        .authorIcon(authorIcon) //
        .title(title) //
        .titleLink(titleLink) //
        .titleLinkDownload(titleLinkDownload) //
        .imageUrl(imageUrl) //
        .audioUrl(audioUrl) //
        .videoUrl(videoUrl) //
        .build();
  }

  /**
   * Converts a <code>List</code> of <code>RawAttachment</code> instances to
   * <code>Attachment</code> instances.
   *
   * @param rawList the <code>List</code> of <code>RawAttachment</code> instances
   *                to convert
   * @return the resulting <code>Attachment</code> instances
   */
  public static List<Attachment> convertList(final List<RawAttachment> rawList) {
    if (rawList == null || rawList.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Attachment> result = new ArrayList<>();
    rawList.stream().forEach(ra -> result.add(ra.convert()));
    return result;
  }

  /**
   * Converts an <code>Attachment</code> instance to a <code>RawAttachment</code>
   * instance.
   *
   * @param attachment the <code>Attachment</code> instance to convert
   * @return the resulting <code>RawAttachment</code>
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RawAttachment of(final Attachment attachment) {
    return RawAttachment.builder() //
        .color(attachment.getColor()) //
        .text(attachment.getText()) //
        .ts(attachment.getTimestamp() == null ? null : attachment.getTimestamp().toString()) //
        .thumbUrl(attachment.getThumbUrl()) //
        .messageLink(attachment.getMessageLink()) //
        .collapsed(attachment.isCollapsed()) //
        .authorName(attachment.getAuthorName()) //
        .authorLink(attachment.getAuthorLink()) //
        .authorIcon(attachment.getAuthorIcon()) //
        .title(attachment.getTitle()) //
        .titleLink(attachment.getTitleLink()) //
        .titleLinkDownload(attachment.isTitleLinkDownload()) //
        .imageUrl(attachment.getImageUrl()) //
        .audioUrl(attachment.getAudioUrl()) //
        .videoUrl(attachment.getVideoUrl()) //
        .build();
  }

  /**
   * Converts a <code>List</code> of <code>Attachment</code> instances to
   * <code>RawAttachment</code> instances.
   *
   * @param list the <code>List</code> of <code>Attachment</code> instances to
   *             convert
   * @return the resulting <code>RawAttachment</code> instances
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static List<RawAttachment> of(final List<Attachment> list) {
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }

    final List<RawAttachment> result = new ArrayList<>();
    list.stream().forEach(a -> result.add(RawAttachment.of(a)));
    return result;
  }
}