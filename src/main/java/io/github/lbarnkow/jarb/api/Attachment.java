package io.github.lbarnkow.jarb.api;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Attachment {
  String color;
  String text;
  Instant timestamp;
  String thumbUrl;
  String messageLink;
  boolean collapsed;
  String authorName;
  String authorLink;
  String authorIcon;
  String title;
  String titleLink;
  boolean titleLinkDownload;
  String imageUrl;
  String audioUrl;
  String videoUrl;
}