package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.util.Collections;
import java.util.List;

import lombok.Data;

// see: https://rocket.chat/docs/developer-guides/rest-api/chat/postmessage/
@MyJsonSettings
@Data
public class RawAttachment {
	private String color;
	private String text;
	private String ts;
	private String thumb_url;
	private String message_link;
	private boolean collapsed;
	private String author_name;
	private String author_link;
	private String author_icon;
	private String title;
	private String title_link;
	private boolean title_link_download;
	private String image_url;
	private String audio_url;
	private String video_url;
	private List<RawAttachmentField> fields = Collections.emptyList();
}