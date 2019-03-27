package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;
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
  @JsonAlias("thumb_url")
  private String thumbUrl;
  @JsonAlias("message_link")
  private String messageLink;
  private boolean collapsed;
  @JsonAlias("author_name")
  private String authorName;
  @JsonAlias("author_link")
  private String authorLink;
  @JsonAlias("author_icon")
  private String authorIcon;
  private String title;
  @JsonAlias("title_link")
  private String titleLink;
  @JsonAlias("title_link_download")
  private boolean titleLinkDownload;
  @JsonAlias("image_url")
  private String imageUrl;
  @JsonAlias("audio_url")
  private String audioUrl;
  @JsonAlias("video_url")
  private String videoUrl;
  @Builder.Default
  private List<RawAttachmentField> fields = Collections.emptyList();

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

  public static List<Attachment> convertList(List<RawAttachment> rawList) {
    if (rawList == null || rawList.isEmpty()) {
      return Collections.emptyList();
    }

    val result = new ArrayList<Attachment>();
    rawList.stream().forEach(ra -> result.add(ra.convert()));
    return result;
  }

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

  public static List<RawAttachment> of(List<Attachment> list) {
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }

    val result = new ArrayList<RawAttachment>();
    list.stream().forEach(a -> result.add(RawAttachment.of(a)));
    return result;
  }
}