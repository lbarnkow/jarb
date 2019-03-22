package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Attachment;
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
	@Builder.Default
	private List<RawAttachmentField> fields = Collections.emptyList();

	public Attachment convert() {
		return Attachment.builder() //
				.color(color) //
				.text(text) //
				.timestamp(ts != null ? Instant.parse(ts) : null) //
				.thumbUrl(thumb_url) //
				.messageLink(message_link) //
				.collapsed(collapsed) //
				.authorName(author_name) //
				.authorLink(author_link) //
				.authorIcon(author_icon) //
				.title(title) //
				.titleLink(title_link) //
				.titleLinkDownload(title_link_download) //
				.imageUrl(image_url) //
				.audioUrl(audio_url) //
				.videoUrl(video_url) //
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
				.ts(a.getTimestamp().toString()) //
				.thumb_url(a.getThumbUrl()) //
				.message_link(a.getMessageLink()) //
				.collapsed(a.isCollapsed()) //
				.author_name(a.getAuthorName()) //
				.author_link(a.getAuthorLink()) //
				.author_icon(a.getAuthorIcon()) //
				.title(a.getTitle()) //
				.title_link(a.getTitleLink()) //
				.title_link_download(a.isTitleLinkDownload()) //
				.image_url(a.getImageUrl()) //
				.audio_url(a.getAudioUrl()) //
				.video_url(a.getVideoUrl()) //
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