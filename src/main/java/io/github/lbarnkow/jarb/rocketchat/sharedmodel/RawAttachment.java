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
import lombok.val;

// see: https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/
@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawAttachment {
  private String color;
  private String text;
  private String ts;
  @JsonProperty("thumb_url")
  private String thumbUrl;
  @JsonProperty("message_link")
  private String messageLink;
  private boolean collapsed;
  @JsonProperty("author_name")
  private String authorName;
  @JsonProperty("author_link")
  private String authorLink;
  @JsonProperty("author_icon")
  private String authorIcon;
  private String title;
  @JsonProperty("title_link")
  private String titleLink;
  @JsonProperty("title_link_download")
  private boolean titleLinkDownload;
  @JsonProperty("image_url")
  private String imageUrl;
  @JsonProperty("audio_url")
  private String audioUrl;
  @JsonProperty("video_url")
  private String videoUrl;
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
        .timestamp(ts != null ? Instant.parse(ts) : null) //
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
  public static List<Attachment> convertList(List<RawAttachment> rawList) {
    if (rawList == null || rawList.isEmpty()) {
      return Collections.emptyList();
    }

    val result = new ArrayList<Attachment>();
    rawList.stream().forEach(ra -> result.add(ra.convert()));
    return result;
  }

  /**
   * Converts an <code>Attachment</code> instance to a <code>RawAttachment</code>
   * instance.
   *
   * @param a the <code>Attachment</code> instance to convert
   * @return the resulting <code>RawAttachment</code>
   */
  public static RawAttachment of(Attachment a) {
    return RawAttachment.builder() //
        .color(a.getColor()) //
        .text(a.getText()) //
        .ts(a.getTimestamp() != null ? a.getTimestamp().toString() : null) //
        .thumbUrl(a.getThumbUrl()) //
        .messageLink(a.getMessageLink()) //
        .collapsed(a.isCollapsed()) //
        .authorName(a.getAuthorName()) //
        .authorLink(a.getAuthorLink()) //
        .authorIcon(a.getAuthorIcon()) //
        .title(a.getTitle()) //
        .titleLink(a.getTitleLink()) //
        .titleLinkDownload(a.isTitleLinkDownload()) //
        .imageUrl(a.getImageUrl()) //
        .audioUrl(a.getAudioUrl()) //
        .videoUrl(a.getVideoUrl()) //
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
  public static List<RawAttachment> of(List<Attachment> list) {
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }

    val result = new ArrayList<RawAttachment>();
    list.stream().forEach(a -> result.add(RawAttachment.of(a)));
    return result;
  }
}