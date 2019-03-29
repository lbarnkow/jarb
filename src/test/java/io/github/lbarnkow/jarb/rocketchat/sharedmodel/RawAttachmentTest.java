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

import static com.google.common.truth.Truth.assertThat;

import io.github.lbarnkow.jarb.api.Attachment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class RawAttachmentTest {

  @Test
  void testConvertList() {
    // given
    List<RawAttachment> rawAttachments = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      rawAttachments.add(createRawAttachment());
    }

    // when
    List<Attachment> attachments = RawAttachment.convertList(rawAttachments);

    // then
    assertThat(attachments.size()).isEqualTo(rawAttachments.size());
    for (int i = 0; i < attachments.size(); i++) {
      assertEqual(rawAttachments.get(i), attachments.get(i));
    }
  }

  @Test
  void testConvertEmptyList() {
    // given

    // when
    List<Attachment> attachments1 = RawAttachment.convertList(Collections.emptyList());
    List<Attachment> attachments2 = RawAttachment.convertList(null);

    // then
    assertThat(attachments1).isEmpty();
    assertThat(attachments2).isEmpty();
  }

  @Test
  void testOfList() {
    // given
    List<Attachment> attachments = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      attachments.add(createAttachment());
    }

    // when
    List<RawAttachment> rawAttachments = RawAttachment.of(attachments);

    // then
    assertThat(attachments.size()).isEqualTo(rawAttachments.size());
    for (int i = 0; i < attachments.size(); i++) {
      assertEqual(rawAttachments.get(i), attachments.get(i));
    }
  }

  @Test
  void testOfEmptyList() {
    // given

    // when
    List<RawAttachment> rawAttachments1 = RawAttachment.of(Collections.emptyList());
    List<RawAttachment> rawAttachments2 = RawAttachment.of((List<Attachment>) null);

    // then
    assertThat(rawAttachments1).isEmpty();
    assertThat(rawAttachments2).isEmpty();
  }

  private RawAttachment createRawAttachment() {
    return RawAttachment.builder() //
        .color("color") //
        .text("text") //
        .ts(Instant.now().toString()) //
        .thumbUrl("thumb_url") //
        .messageLink("message_link") //
        .collapsed(true) //
        .authorName("author_name") //
        .authorLink("author_link") //
        .authorIcon("author_icon") //
        .title("title") //
        .titleLink("title_link") //
        .titleLinkDownload(true) //
        .imageUrl("image_url") //
        .audioUrl("audio_url") //
        .videoUrl("video_url") //
        .build();
  }

  private Attachment createAttachment() {
    return Attachment.builder() //
        .color("color") //
        .text("text") //
        .timestamp(Instant.now()) //
        .thumbUrl("thumb_url") //
        .messageLink("message_link") //
        .collapsed(true) //
        .authorName("author_name") //
        .authorLink("author_link") //
        .authorIcon("author_icon") //
        .title("title") //
        .titleLink("title_link") //
        .titleLinkDownload(true) //
        .imageUrl("image_url") //
        .audioUrl("audio_url") //
        .videoUrl("video_url") //
        .build();
  }

  private void assertEqual(RawAttachment rawAttachment, Attachment attachment) {
    assertThat(attachment.getColor()).isEqualTo(rawAttachment.getColor());
    assertThat(attachment.getText()).isEqualTo(rawAttachment.getText());
    assertThat(attachment.getTimestamp().toString()).isEqualTo(rawAttachment.getTs());
    assertThat(attachment.getThumbUrl()).isEqualTo(rawAttachment.getThumbUrl());
    assertThat(attachment.getMessageLink()).isEqualTo(rawAttachment.getMessageLink());
    assertThat(attachment.isCollapsed()).isEqualTo(rawAttachment.isCollapsed());
    assertThat(attachment.getAuthorName()).isEqualTo(rawAttachment.getAuthorName());
    assertThat(attachment.getAuthorLink()).isEqualTo(rawAttachment.getAuthorLink());
    assertThat(attachment.getAuthorIcon()).isEqualTo(rawAttachment.getAuthorIcon());
    assertThat(attachment.getTitle()).isEqualTo(rawAttachment.getTitle());
    assertThat(attachment.getTitleLink()).isEqualTo(rawAttachment.getTitleLink());
    assertThat(attachment.isTitleLinkDownload()).isEqualTo(rawAttachment.isTitleLinkDownload());
    assertThat(attachment.getImageUrl()).isEqualTo(rawAttachment.getImageUrl());
    assertThat(attachment.getAudioUrl()).isEqualTo(rawAttachment.getAudioUrl());
    assertThat(attachment.getVideoUrl()).isEqualTo(rawAttachment.getVideoUrl());
  }

}
